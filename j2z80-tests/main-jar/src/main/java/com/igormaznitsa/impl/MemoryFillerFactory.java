/* 
 * Copyright 2019 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
