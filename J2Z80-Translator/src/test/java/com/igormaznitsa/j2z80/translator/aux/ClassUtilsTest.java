package com.igormaznitsa.j2z80.translator.aux;

import com.igormaznitsa.j2z80.api.additional.*;
import com.igormaznitsa.j2z80.jvmprocessors.Processor_INVOKEINTERFACE;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;

public class ClassUtilsTest {
    
    @Test
    public void testfindAllAdditionalBlocksInClass() {
        final Set<Class<? extends J2ZAdditionalBlock>> foundAdditions  = ClassUtils.findAllAdditionalBlocksInClass(Processor_INVOKEINTERFACE.class);
        assertEquals("Must have 3 additional blocks", 3, foundAdditions.size());
        assertTrue("Must have "+NeedsATHROWManager.class.getCanonicalName(), foundAdditions.contains(NeedsATHROWManager.class));
        assertTrue("Must have "+NeedsINVOKEINTERFACEManager.class.getCanonicalName(), foundAdditions.contains(NeedsINVOKEINTERFACEManager.class));
        assertTrue("Must have "+NeedsMemoryManager.class.getCanonicalName(), foundAdditions.contains(NeedsMemoryManager.class));
    }

}
