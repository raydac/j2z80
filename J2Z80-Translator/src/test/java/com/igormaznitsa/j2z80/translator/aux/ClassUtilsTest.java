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
package com.igormaznitsa.j2z80.translator.aux;

import com.igormaznitsa.j2z80.api.additional.J2ZAdditionalBlock;
import com.igormaznitsa.j2z80.api.additional.NeedsATHROWManager;
import com.igormaznitsa.j2z80.api.additional.NeedsINVOKEINTERFACEManager;
import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.jvmprocessors.Processor_INVOKEINTERFACE;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
