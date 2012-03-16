package com.igormaznitsa.impl;

public class MemoryFillerFactory {
    public static final int FILLER_JAVA = 12364;
    public static final int FILLER_NATIVE = 2212;
    
    private static final Object [] fillers = new Object[]{(Object)new MemoryFillerJava(), (Object)new MemoryFillerNative()};
    
    static {
        for(int i=0;i<fillers.length;i++){
            if (!(fillers[i] instanceof MemoryFiller)){
               fillers[i] = null;
            }
        }
    }
    
    
    public static MemoryFiller getFiller(final int type){
        switch(type){
            case FILLER_JAVA : return (MemoryFiller)fillers[0];
            case FILLER_NATIVE : return (MemoryFiller)fillers[1];
            default: return null;
        }
    }
}
