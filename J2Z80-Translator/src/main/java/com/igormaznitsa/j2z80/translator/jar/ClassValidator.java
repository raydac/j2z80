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
package com.igormaznitsa.j2z80.translator.jar;

import com.igormaznitsa.j2z80.aux.MutableObjectContainer;
import com.igormaznitsa.j2z80.aux.Utils;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

public enum ClassValidator {

;
        
    public static String validateClass(final ClassGen cgen) {
        final MutableObjectContainer<String> result = new MutableObjectContainer<String>();

        return checkClassFlags(cgen, result)
                && checkConstantPool(cgen, result)
                && checkFields(cgen, result)
                && checkMethods(cgen, result)
                ? null : result.get();
    }

    private static boolean checkClassFlags(final ClassGen cgen, final MutableObjectContainer<String> result) {
        if (cgen.isEnum()) {
            result.set("Enum is not supported");
        }

        return result.isNull();
    }

    private static boolean checkConstantPool(final ClassGen cgen, final MutableObjectContainer<String> result) {
        final ConstantPoolGen cpool = cgen.getConstantPool();

        for (int index = 1; index < cpool.getSize(); index++) {
            final Constant curconst = cpool.getConstant(index);

            if (curconst instanceof ConstantUtf8) {
                final String str = ((ConstantUtf8) curconst).getBytes();
                if (str.length() > 255) {
                    result.set("Too long string detected, it contains more than 255 chars [" + str + ']');
                    break;
                }

                for (final char chr : str.toCharArray()) {
                    if ((chr & 0xFF00) != 0) {
                        result.set("Detected a 16 bit coded symbol \'" + chr + "\', but only 8 bit chars allowed [\'" + str + "\']");
                        break;
                    }
                }
            } else if (curconst instanceof ConstantInteger) {
                final int value = ((ConstantInteger) curconst).getBytes();
                if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                    result.set("Integer value must be bounded in 16 bit [" + Utils.intToString(value) + ']');
                    break;
                }
            } else if (curconst instanceof ConstantDouble) {
                final double value = ((ConstantDouble) curconst).getBytes();
                result.set("Double values are not allowed [" + value + ']');
                break;
            } else if (curconst instanceof ConstantFloat) {
                final float value = ((ConstantFloat) curconst).getBytes();
                result.set("Float values are not allowed [" + value + ']');
                break;
            } else if (curconst instanceof ConstantLong) {
                final long value = ((ConstantLong) curconst).getBytes();
                result.set("Long values are not allowed [" + Utils.longToString(value) + ']');
                break;
            }
        }

        return result.isNull();
    }

    private static boolean checkFields(final ClassGen cgen, final MutableObjectContainer<String> result) {
        boolean immediateBreak = false;
        for (final Field field : cgen.getFields()) {
            if (immediateBreak) {
                break;
            }

            final Type type = field.getType();
            final String fieldName = field.getName();

            switch (type.getType()) {
                case Constants.T_LONG: {
                    result.set("Detected disallowed 'long' field [" + fieldName + ']');
                    immediateBreak = true;
                }
                break;
                case Constants.T_DOUBLE: {
                    result.set("Detected disallowed 'double' field [" + fieldName + ']');
                    immediateBreak = true;
                }
                break;
                case Constants.T_FLOAT: {
                    result.set("Detected disallowed 'float' field [" + fieldName + ']');
                    immediateBreak = true;
                }
                break;
            }
        }

        return result.isNull();
    }

    private static boolean checkMethods(final ClassGen cgen, final MutableObjectContainer<String> result) {
        boolean immediateBreak = false;
        for (final Method method : cgen.getMethods()) {
            if (immediateBreak) {
                break;
            }

            final String methodName = method.getName() + ' ' + method.getSignature();

            final Type returnType = method.getReturnType();

            switch (returnType.getType()) {
                case Constants.T_LONG: {
                    result.set("Method returns disallowed 'long' result [" + methodName + ']');
                    immediateBreak = true;
                }
                break;
                case Constants.T_DOUBLE: {
                    result.set("Method returns disallowed 'double' result [" + methodName + ']');
                    immediateBreak = true;
                }
                break;
                case Constants.T_FLOAT: {
                    result.set("Method returs disallowed 'float' field [" + methodName + ']');
                    immediateBreak = true;
                }
                break;
            }

            // check args
            if (result.isNull()) {
                for (final Type arg : method.getArgumentTypes()) {

                    switch (arg.getType()) {
                        case Constants.T_LONG: {
                            result.set("Method needs disallowed 'long' argument [" + methodName + ']');
                            immediateBreak = true;
                        }
                        break;
                        case Constants.T_DOUBLE: {
                            result.set("Method needs disallowed 'double' argument [" + methodName + ']');
                            immediateBreak = true;
                        }
                        break;
                        case Constants.T_FLOAT: {
                            result.set("Method needs disallowed 'float' argument [" + methodName + ']');
                            immediateBreak = true;
                        }
                        break;
                    }
                }
            }

        }

        return result.isNull();
    }
}
