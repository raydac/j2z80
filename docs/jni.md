# JNI native methods

j2z80 implements Java `native` methods by loading Z80 assembly from a resource
whose name matches the declaring class. This page describes the calling convention
used by those native methods.

## Register conventions

- The alternate register set may be used without restrictions.
- Preserve `HL`, `BC`, and `DE` unless a register pair carries the method result.
- `IY` and `AF` may be changed.
- `IX` points to the current invocation's argument frame. Restore it before
  returning. The invoke epilogue uses `IX` to restore `SP` and then pops the
  previous `IX`. If a ROM routine or helper borrows `IX`, save and restore it
  around the call.

## Arguments and frame layout

Each argument slot is one 16-bit word (two bytes). An instance method has the
receiver as its first slot; a static method has no receiver slot.

| Java type                                                     | Slots |
|---------------------------------------------------------------|------:|
| `boolean`, `byte`, `char`, `short`, `int`, `float`, reference |     1 |
| `long`, `double`                                              |     2 |

`float` occupies one slot containing IEEE binary16. `long` and `double` occupy
two slots. In each wide value, the low word is in the first slot (at the lower
`IX` offset), followed by the high word. The translator exchanges wide-value
words before building the frame so that this layout is consistent.

Within a slot, the high byte is at `(IX-offset+1)` and the low byte at
`(IX-offset)`. For offset zero, the low byte is `(IX)`.

### Instance method

For `native void method(Object A, int B, int C)`, the receiver is the first slot:

```text
; IX+0: receiver
LD H,(IX+1)       ; receiver high byte
LD L,(IX-0)       ; receiver low byte

; IX-2: A
LD B,(IX-2+1)     ; A high byte
LD C,(IX-2)       ; A low byte

; IX-4: B, IX-6: C
```

Offsets increase by two bytes for each subsequent slot. The receiver and
references are 16-bit addresses.

### Static method

For `native static void method(Object A, int B, int C)`, there is no receiver:

| Argument | High byte | Low byte |
|----------|-----------|----------|
| `A`      | `(IX+1)`  | `(IX)`   |
| `B`      | `(IX-1)`  | `(IX-2)` |
| `C`      | `(IX-3)`  | `(IX-4)` |

Equivalently, the slot bases are `IX+0`, `IX-2`, and `IX-4`.

### Wide arguments

For `native static void method(long value, int count)`, the first two slots hold
the `long`:

| Value part        | High byte | Low byte |
|-------------------|-----------|----------|
| `value` low word  | `(IX+1)`  | `(IX)`   |
| `value` high word | `(IX-1)`  | `(IX-2)` |
| `count`           | `(IX-3)`  | `(IX-4)` |

`double` uses the same two-slot layout. The arithmetic operand stack keeps the
low word on top; the translator arranges the argument frame separately.

## Return values

| Java return type                                              | Native result                       |
|---------------------------------------------------------------|-------------------------------------|
| `void`                                                        | `BC` and `DE` are ignored           |
| `int`, `boolean`, `byte`, `char`, `short`, `float`, reference | `BC`                                |
| `long`, `double`                                              | Low word in `BC`, high word in `DE` |

The invoke epilogue preserves `BC` and `DE` while tearing down the frame.

## Example: Java declaration and assembly body

Declare native methods in Java:

```java
package com.igormaznitsa.memory;

public class MemoryAccessor {
  public static native void writeWordToMemory(int address, int value);
  public static native int readWordFromMemory(int address);
}
```

Place `MemoryAccessor.a80` beside the class as a classpath resource. Its method
labels use the declaring class, method name, and JVM descriptor:

```asm
com.igormaznitsa.memory.MemoryAccessor.writeWordToMemory#[II]V:
    LD H,(IX-0+1) ; address into HL
    LD L,(IX-0)
    LD B,(IX-2+1) ; value into BC
    LD C,(IX-2)

    LD (HL),C
    INC HL
    LD (HL),B
    RET

com.igormaznitsa.memory.MemoryAccessor.readWordFromMemory#[I]I:
    LD H,(IX-0+1)
    LD L,(IX-0)
    LD C,(HL)
    INC HL
    LD B,(HL)
    RET
```

The example reads and writes little-endian words. On return from `readWordFromMemory`,
the result is in `BC`.

## Checked exceptions

Checked exceptions from translated Java methods are reported through the hidden
cell `___PENDING_EXCEPTION`. Zero means that no exception is pending. A translated
method declaring checked exceptions clears the cell on entry and sets it when
returning an exception. The caller checks the cell after the call: a matching
covering `catch` handler receives the exception; otherwise the exception is
propagated to the caller's caller.

The translator resolves handler dispatch while translating the method; it does
not use a runtime unwind table. For example:

```java
final class ExceptionExample {
  static final class Signal extends Exception {
  }

  static int parse(int value) throws Signal {
    if (value < 0) {
      throw new Signal();
    }
    return value;
  }

  static int read(int value) {
    try {
      return parse(value);
    } catch (Signal signal) {
      return 0;
    }
  }
}
```

Only checked exception types are supported. Declarations and catch handlers for
`RuntimeException`, `Error`, or their subclasses, and classes extending those
types, are rejected during translation. Explicitly throwing unchecked exceptions
is unsupported. Runtime faults such as integer divide-by-zero and failed
`checkcast` still use `___ATHROW_PROCESSING_CODE_ADDRESS`, whose default is zero;
they do not become catchable checked exceptions.

## Memory manager

Implement `com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager` on a class
to include the memory manager. It provides labels and helpers such as
`___BEFORE_INVOKE`, `___AFTER_INVOKE`, and allocation routines. Other optional
additions include `NeedsFloatArithmeticManager`, `NeedsLongArithmeticManager`,
and `NeedsDoubleArithmeticManager`, under
`com.igormaznitsa.j2z80.api.additional`.

The heap and stack grow towards each other: the heap grows bottom-up and the
stack top-down. Use the assembler's `DEFS` directive in a JNI assembly resource
to reserve a static block. Dynamic memory can be allocated with `new byte[]` or
other supported object and array allocations.

## Assembly resources and instruction set

For a native method, the translator can load a class-level assembly resource named
for the declaring class with the `.a80`, `.asm`, or `.z80` extension. It can also
load a per-method file named `ClassName#methodName` with one of those extensions;
the method name must be unique in the class. A same-named `.bin` resource can be
included as raw bytes. Resources are typically stored alongside the class on the
classpath. The embedded assembler supports documented Z80 instructions and
project-specific directives; see [Embedded Z80 Assembler](asm.md) for instruction
operand rules and directives.

## Address space and interrupts

The translator does not manage memory extenders and supports only the regular
64 KB address space.

Interrupts and `HALT` may be used, but take care on ROM-hosted targets such as
the ZX Spectrum 48K. Leaving interrupts enabled around `HALT` can let the ROM
keyboard scan treat SPACE (or another key) as BREAK and restart BASIC. The
Spectrum demo natives use a short busy delay for frame sync instead of `HALT`,
and call `DI` after the ROM BEEPER. Before calling Spectrum ROM routines, set
`ERR-NR` to `#FF` and clear the new-key flag.
