# Translation model and limitations

j2z80 translates compiled bytecode into native Z80 instructions. Its target has 16-bit words and a 64 KB address
space, so Java types do not always retain their JVM width or precision.

## Data representation

| Java type                 | Representation                                  | Notes                                                                       |
|---------------------------|-------------------------------------------------|-----------------------------------------------------------------------------|
| `boolean`, `byte`, `char` | One 16-bit stack slot; array elements are 8-bit | `char` and string characters are limited to 0..255.                         |
| `short`, `int`            | One signed 16-bit slot                          | Arithmetic and shifts operate at 16 bits; constants must fit −32768..32767. |
| `float`                   | IEEE binary16 in one slot                       | Half precision; `f2i` clamps to signed 16-bit range.                        |
| `long`                    | Signed 32-bit in two slots                      | Constants must fit signed 32-bit range.                                     |
| `double`                  | IEEE binary32 in two slots                      | Same precision as `float`, not Java's 64-bit precision.                     |
| Reference                 | One 16-bit address                              | `null` is address 0.                                                        |

`long` and `double` return in BC:DE, low word first. `int`, `float`, and references return in BC. Integer division by
zero and runtime faults generally jump through the configurable athrow address (0 by default), rather than raising a
catchable Java runtime exception.

Array elements use these widths: 8 bits for `byte`, `boolean`, and `char`; 16 bits for `short`, `int`, `float`, and
references; 32 bits for `long` and `double`. Integer operations wrap at their supported width. `float` division by zero
produces infinity; `drem` uses truncating remainder.

## Language and runtime support

Classes, fields, constructors, virtual and interface calls, `instanceof`, and `checkcast` are supported. A null
`checkcast` succeeds. `synchronized` is ignored; monitor instructions discard their reference. The standard Java
library is absent: `java.lang.Object` provides a constructor and address-based `hashCode`, while string literals are
length-prefixed raw 8-bit data and `String` methods are unavailable.

Only checked exceptions are translated. Methods declaring `throws` use a hidden pending-exception cell; handlers are
resolved during translation. `finally` runs when leaving a try block, including on return, break, continue, and throw.
Unchecked exceptions cannot be declared, thrown, caught, or subclassed.

One-dimensional arrays of primitive and reference types are supported and zero-initialized. Arrays store a size byte
and length word before their elements; multi-dimensional arrays contain references. Array accesses have no null or
bounds checks. `multianewarray` of `long` and `double` is unsupported. Locals are limited to 64 slots because they use
signed IX-relative addressing.

Instances use a 4-byte header and are allocated from an upward-growing bump heap. There is no garbage collector or
allocation boundary check. `ZSystem.top()` reports the heap pointer; `ZSystem.forget(instance)` rewinds to that
instance, releasing it and later allocations. Allocate an owner before its child objects and arrays. Arrays themselves
are not valid arguments to `forget`.

Records translate with component accessors, `equals`, and `hashCode`; `toString()` returns `null`. Record reference
components other than records compare by identity. Enums support constants, fields, `ordinal()`, `values()`, and
`switch`; `name()`, `valueOf`, `compareTo`, and `getDeclaringClass` are not supported. Lambdas and method references
are unsupported.

## Static data and native methods

Contiguous static `byte[]` initializers with at least 16 elements can be stored in the program image instead of being
copied to the heap. They still count toward the 64 KB image, and stores change the embedded data in place. Other
non-class JAR resources are embedded as raw labeled bytes unless excluded with `<excludeResources>`.

Native methods use classpath `.a80`, `.asm`, or `.z80` files (or matching per-method files); see the
[JNI guide](jni.md) for frame layout, arguments, return values, and runtime helpers.
