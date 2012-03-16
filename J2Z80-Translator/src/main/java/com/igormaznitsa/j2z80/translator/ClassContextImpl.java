package com.igormaznitsa.j2z80.translator;

import com.igormaznitsa.j2z80.ClassContext;
import com.igormaznitsa.j2z80.ids.*;
import java.util.*;
import java.util.Map.Entry;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;

class ClassContextImpl implements ClassContext {

    private final TranslatorImpl theTranslator;
    private final Map<ClassID, ClassMethodInfo> classIds = new HashMap<ClassID, ClassMethodInfo>();
    private final Set<ClassID> classesWithJNI = new HashSet<ClassID>();

    public ClassContextImpl(final TranslatorImpl translator) {
        this.theTranslator = translator;
    }
  
    void init(){
        int classIdCounter = 0;

        for (final ClassGen c : theTranslator.workingArchive.getClasses().values()) {
            final ClassID classId = new ClassID(c);
            final ClassMethodInfo classInfo = new ClassMethodInfo(c, null, classIdCounter);
            classIds.put(classId, classInfo);

            if (!c.isInterface()){
                for(final Method m : c.getMethods()){
                    if (m.isNative()){
                        classesWithJNI.add(classId);
                        break;
                    }
                }
            }
            
            classIdCounter++;
        }
    }
    
    Set<ClassID> getClassesWithDetectedJNI(){
        return classesWithJNI;
    }
    
    Set<Entry<ClassID,ClassMethodInfo>> getAllRegisteredClasses() {
        return classIds.entrySet();
    }
    
    @Override
    public Integer findClassUID(final ClassID classId) {
        final ClassMethodInfo info = classIds.get(classId);
        if (info != null) {
            return Integer.valueOf(info.getUID());
        }
        return null;
    }

    @Override
    public Set<ClassID> findAllClassesImplementInterface(final String interfaceName) {
        final Set<ClassID> result = new HashSet<ClassID>();

        final ClassGen classGen = findClassForID(new ClassID(interfaceName));
        if (classGen.isInterface()) {

            for (final ClassGen cgen : theTranslator.workingArchive.getClasses().values()) {
                for (final String name : cgen.getInterfaceNames()) {
                    if (interfaceName.equals(name)) {
                        if (cgen.isInterface()) {
                            final Set<ClassID> thatInterface = findAllClassesImplementInterface(name);
                            result.addAll(thatInterface);
                        } else {
                            result.add(new ClassID(cgen));

                            final List<String> successors = findAllClassSuccessors(cgen.getClassName());
                            if (!successors.isEmpty()) {
                                for (final String className : successors) {
                                    result.add(new ClassID(className));
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
 
    @Override
    public Iterable<ClassID> getAllClasses() {
        return classIds.keySet();
    }

    ClassMethodInfo findClassInfoForID(final ClassID classID){
        return classIds.get(classID);
    }
    
    @Override
    public ClassGen findClassForID(final ClassID classID) {
        final ClassMethodInfo classMethodInfo = classIds.get(classID);
        return classMethodInfo == null ? null : classMethodInfo.getClassInfo();
    }
    @Override
    public boolean isAccessible(final ClassGen classGen, final String superClassName) {
        ClassGen tmpClassGen = classGen;
        if (superClassName.equals(tmpClassGen.getClassName())) {
            return true;
        }
        while (tmpClassGen != null) {
            final String superCName = tmpClassGen.getSuperclassName();
            if (superClassName.equals(superCName)) {
                return true;
            }

            tmpClassGen = theTranslator.workingArchive.findClassForName(superCName);
        }
        return false;
    }

    @Override
    public List<String> findAllClassAncestors(final String className) {
        final List<String> result = new ArrayList<String>();

        String tmpClassName = className;

        while (!"java.lang.Object".equals(tmpClassName)) {
            if (!className.equals(tmpClassName)) {
                result.add(tmpClassName);
            }
            final ClassGen classGen = findClassForID(new ClassID(tmpClassName));
            tmpClassName = classGen.getSuperclassName();
        }
        return result;
    }

    @Override
    public List<String> findAllClassSuccessors(final String className) {
        final List<String> result = new ArrayList<String>();
        for (final ClassGen cls : theTranslator.workingArchive.getClasses().values()) {
            if (className.equals(cls.getClassName())) {
                continue;
            }
            if (isAccessible(cls, className)) {
                result.add(cls.getClassName());
            }
        }
        return result;
    }
}
