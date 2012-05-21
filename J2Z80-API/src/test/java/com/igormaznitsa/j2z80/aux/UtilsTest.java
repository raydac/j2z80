package com.igormaznitsa.j2z80.aux;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class UtilsTest {
    
    @Test
    public void testCheckPathForAntPattern_Positive() {
        assertTrue(Utils.checkPathForAntPattern("/hello/world.xml", "/**/*.xml"));
        assertTrue(Utils.checkPathForAntPattern("hello/world.xml", "/**/*.xml"));
        assertTrue(Utils.checkPathForAntPattern("hello/some/world.xml", "/**/*.xml"));
        assertTrue(Utils.checkPathForAntPattern("test.res", "/**/test.res"));
        assertTrue(Utils.checkPathForAntPattern("test.ReS", "/**/test.res"));
    }

    @Test
    public void testCheckPathForAntPattern_Negative() {
        assertFalse(Utils.checkPathForAntPattern("/hello/world.properties", "/**/*.xml"));
        assertFalse(Utils.checkPathForAntPattern("hello/world.xml", "/**/*.ppp"));
    }
}
