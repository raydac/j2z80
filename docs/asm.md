# Embedded Z80 Assembler

The translator includes a small embedded Z80 assembler. It is used to convert generated assembly source code into its
binary representation. The assembler supports only officially documented Z80 instructions. In addition, it provides
several special directives to control the translation process and to manage labels.

## Labels

There are two types of labels:

### Global Labels

A global label must be unique; only one label with a given name may exist.

### Local Labels

Local labels may be redefined. A local label name starts with the `@` character.  
To avoid name collisions or unexpected behavior, all currently known local labels can be cleared using the `CLRLOC`
directive.

## Operand rules

- Mnemonics and register operands are matched case-insensitively (`ld a,(bc)`, `JP (IY)`, and so on).
- IX/IY displacements are signed bytes in **-128..127** (for example `ADD A,(IX-1)`).
- Immediate 8-bit operands accept **-128..255**, so both `CP -128` and `CP 255` assemble.
- `LD (SP),n` is rejected (not a documented instruction).

## Special Directives

**ENT `<address>`**  
Defines the entry point of the code.

**ORG `<address>`**  
Sets the current start address for code generation.

**DEFB `<byte>, ..., <byte>`**  
Stores one or more byte values in memory (one memory cell per value).

**DEFW `<word>, ..., <word>`**  
Stores one or more word values in memory (two memory cells per value).

**DEFM `<string>`**  
Stores a string as raw bytes: one byte per Java character in the range 0..255. Characters above 255 are rejected.

**DEFS `<length>`**  
Reserves a block of memory of the specified length in bytes.

**CLRLOC**  
Clears all previously defined local labels.

**`<label>:` EQU `<expression>`**  
Assigns a numeric value to a label. Forward EQU chains are resolved by repeated evaluation until values stabilize; a
circular dependency throws.

## Expressions

Only very simple expressions are supported. The assembler recognizes only the `+` and `-` operators. The `$` symbol may
be used to refer to the current program counter (PC).

Strings may also be used in a limited way within expressions. Their characters are packed into a 4-byte word. For
example:

```
"    " → 0x20202020
```

Strings support the following escape sequences:

- `\t` — tab
- `\r` — carriage return
- `\n` — newline
- `\b` — backspace
- `\"` — double quote
- `\'` — single quote
- `\\` — backslash
