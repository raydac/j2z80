package com.igormaznitsa.j2z80.api.additional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {
    ElementType.TYPE,
    ElementType.CONSTRUCTOR,
    ElementType.FIELD,
    ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface J2Z80Ignore {
}
