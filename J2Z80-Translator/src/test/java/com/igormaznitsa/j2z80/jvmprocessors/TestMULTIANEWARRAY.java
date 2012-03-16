/*
 * Copyright 2012 Igor Maznitsa (http://www.igormaznitsa.com)
 * 
 * This file is part of the JVM to Z80 translator project (hereinafter referred to as J2Z80).
 *
 * J2Z80 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J2Z80 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2Z80.  If not, see <http://www.gnu.org/licenses/>. 
 */
package com.igormaznitsa.j2z80.jvmprocessors;

import org.apache.bcel.generic.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestMULTIANEWARRAY extends AbstractTestBasedOnMemoryManager {

    @Test(timeout=3000L)
    public void testCreateSingleArray() throws Exception {
        push(1000);
        assertAllocateCommand(new Instruction[]{new MULTIANEWARRAY(0, (short) 1)}, calculateMemorySize(1000));
        assertEquals(getInitialMemoryAddress()+3,pop());
        assertStackEmpty();
    }

    @Test(timeout=3000L)
    public void testCreateTwoDimensionSingleElementArrays() throws Exception {
        final int dim1 = 1;
        final int dim2 = 1;
        
        push(dim1);
        push(dim2);
        assertAllocateCommand(new Instruction[]{new MULTIANEWARRAY(0, (short) 2)}, calculateMemorySize(dim1,dim2));
        assertEquals(getInitialMemoryAddress()+3,pop());
        assertStackEmpty();
 
        // check that addresses are not null
        assertEquals(dim1,peekw(getInitialMemoryAddress()+1));
        for(int i=0;i<dim1;i++){
            final int arrayAddress = peekw(getInitialMemoryAddress()+3+(i*2));
            assertEquals(dim2,peekw(arrayAddress-2));
        }
    }
    
    @Test(timeout=3000L)
    public void testCreateTwoDimensionNoSingleElementArrays() throws Exception {
        final int dim1 = 233;
        final int dim2 = 20;
        
        push(dim1);
        push(dim2);
        assertAllocateCommand(new Instruction[]{new MULTIANEWARRAY(0, (short) 2)}, calculateMemorySize(dim1,dim2));
        assertEquals(getInitialMemoryAddress()+3,pop());
        assertStackEmpty();
        
        // check that addresses are not null
        assertEquals(dim1,peekw(getInitialMemoryAddress()+1));
        for(int i=0;i<dim1;i++){
            final int arrayAddress = peekw(getInitialMemoryAddress()+3+(i*2));
            assertEquals(dim2,peekw(arrayAddress-2));
        }
        
    }

    @Test(timeout=3000L)
    public void testCreateThreeDimensionNoSingleElementArrays() throws Exception {
        final int dim1 = 4;
        final int dim2 = 11;
        final int dim3 = 23;
        
        push(dim1);
        push(dim2);
        push(dim3);
        
        assertAllocateCommand(new Instruction[]{new MULTIANEWARRAY(0, (short) 3)}, calculateMemorySize(dim1,dim2,dim3));
        assertEquals(getInitialMemoryAddress()+3,pop());
        assertStackEmpty();
    }

    @Test(timeout=3000L)
    public void testCreateFourDimensionNoSingleElementArrays() throws Exception {
        final int dim1 = 2;
        final int dim2 = 3;
        final int dim3 = 5;
        final int dim4 = 8;
        
        push(dim1);
        push(dim2);
        push(dim3);
        push(dim4);
        assertAllocateCommand(new Instruction[]{new MULTIANEWARRAY(0, (short) 4)}, calculateMemorySize(dim1,dim2,dim3,dim4));
        assertEquals(getInitialMemoryAddress()+3,pop());
        assertStackEmpty();
    }

    @Test
    public void testCalculateMemorySize() {
        assertEquals(31, calculateMemorySize(2,1,2));
    }
    
    private int calculateMemorySize(final int ... dimensions){
        int result = 0;
        
        int prev = 1;
        
        for(final int i : dimensions){
            result += prev * (i*2+3);
            prev *= i;
        }
        
        return result;
    }
}
