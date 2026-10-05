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

## Example 1: `void` native method with two arguments

This static method receives two one-slot arguments: the address at `IX+0` and
the value at `IX-2`. It writes the low byte of `value` to the supplied address.

```java
package demo;

public class NativeMemory {
  public static native void writeByte(int address, int value);
}
```

Put `NativeMemory.a80` beside the class as a classpath resource. The method label
contains the method descriptor: `[II]V` means two `int` arguments and `void`
return.

```asm
; NativeMemory.a80
demo.NativeMemory.writeByte#[II]V:
    PUSH HL
    LD H,(IX-0+1) ; address into HL
    LD L,(IX-0)
    LD A,(IX-2)    ; low byte of value
    LD (HL),A
    POP HL
    RET
```

The method preserves `HL`, which it uses as a temporary; `AF` may be changed.

## Example 2: `int` native method with one argument and a checked exception

Unlike translated Java methods, a native method's assembly body must maintain the
pending-exception cell itself. This example uses a pre-created exception object:
the native function takes one `int`, returns its value when nonnegative, and
reports `ParseFailure` for a negative value.

```java
package demo;

public class ParseFailure extends Exception {
}
```

```java
package demo;

public class NativeParser {
  private static final ParseFailure FAILURE = new ParseFailure();

  public static native int parse(int value) throws ParseFailure;

  public static int parseOrZero(int value) {
    try {
      return parse(value);
    } catch (ParseFailure failure) {
      return 0;
    }
  }
}
```

Put the implementation in `demo/NativeParser.a80`. The label below matches the
Java declaration `parse(int): int` (`[I]I` is its JVM descriptor). The native
implementation clears the cell before either outcome, sets it to the exception
reference on failure, and returns the ordinary result in `BC`:

```asm
; demo/NativeParser.a80
demo.NativeParser.parse#[I]I:
    PUSH HL                      ; preserve caller's HL
    LD HL,0
    LD (___PENDING_EXCEPTION),HL ; clear pending exception

    LD C,(IX-0)                  ; argument: int value
    LD B,(IX-0+1)
    BIT 7,B                      ; negative signed 16-bit value?
    JR NZ,NP.FAILURE

    POP HL
    RET                          ; BC still contains the input value

NP.FAILURE:
    LD BC,(demo.NativeParser.FAILURE#Ldemo.ParseFailurej)
    LD (___PENDING_EXCEPTION),BC ; report the exception object
    LD BC,0                      ; return value is ignored on exception
    POP HL
    RET
```

`___PENDING_EXCEPTION` is the hidden cell used by checked-exception handling.
The static-field label above is the assembler form of
`NativeParser.FAILURE`; field descriptors are encoded in labels, with `/`
replaced by `.` and `;` by `j`. The pre-created exception instance is reused for
each failure.

After `parse` returns, the translated caller checks the cell. A matching
covering `catch` handler receives the exception; if none matches, it is
propagated to the caller. The translator resolves handler dispatch while
translating the method; it does not use a runtime unwind table. Translated Java
methods that declare checked exceptions clear the cell on entry and set it when
returning an exception; native assembly must perform these steps explicitly.

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

JNI assembly and its `DEFS` blocks are included in the generated image before
the memory manager, so the initial heap pointer is after that static data.
Ordinary upward heap allocations therefore do not overwrite static native data
inside the image, as long as the image and stack are placed in non-overlapping
memory.

The allocator does not check allocations against `SP`, the configured stack
top, or the 64 KB address-space boundary. Heap/stack collision and address
wraparound are therefore possible if the program exhausts available memory.
Native code that allocates dynamic memory must coordinate its memory range with
the Java heap; the translator does not reserve or track native-managed buffers.

`j2z80.Heap.forget(Object)` is only valid for a non-null ordinary object
allocated on the Java heap. It rewinds to that object's header and releases it
and all later allocations. Do not pass arrays, ROM-resident data, or references
owned by native code: the runtime does not validate the reference or its
allocation history, and a subsequent allocation can overwrite memory at the
rewound address.

## Assembly resources and instruction set

For a native method, the translator can load a class-level assembly resource named
for the declaring class with the `.a80`, `.asm`, or `.z80` extension. It can also
load a per-method file named `ClassName#methodName` with one of those extensions;
the method name must be unique in the class. A same-named `.bin` resource can be
included as raw bytes. Resources are typically stored alongside the class on the
classpath. The embedded assembler supports documented Z80 instructions and
project-specific directives. Operand rules and directives are documented in
`docs/asm.md`.

## Address space and interrupts

The translator does not manage memory extenders and supports only the regular
64 KB address space.

Interrupts and `HALT` may be used, but take care on ROM-hosted targets such as
the ZX Spectrum 48K. Leaving interrupts enabled around `HALT` can let the ROM
keyboard scan treat SPACE (or another key) as BREAK and restart BASIC. The
Spectrum demo natives use a short busy delay for frame sync instead of `HALT`,
and call `DI` after the ROM BEEPER. Before calling Spectrum ROM routines, set
`ERR-NR` to `#FF` and clear the new-key flag.
