# Embedded Z80 Assembler

j2z80 includes a small assembler that translates assembly source into a binary
image. It implements a subset of documented Z80 instructions and a set of
project-specific directives; it is not a general-purpose Z80 assembler.

## Complete example

This example shows the syntax used by the assembler: an origin and entry point,
an `EQU` constant, a loop with a local label, and data and storage directives.

```asm
        ORG #8000
        ENT START

COUNT:  EQU 3

START:  LD HL,MESSAGE
        LD B,COUNT
@COPY:  LD A,(HL)
        INC HL
        DJNZ @COPY
        RET

MESSAGE: DEFM "Hi!"
BYTES:   DEFB #41,66,%01000011
WORDS:   DEFW START,-1
BUFFER:  DEFS 4
        END
```

The assembler accepts decimal (`66`), hexadecimal (`#41`), and binary (`%01000011`) integer literals. `DEFW` stores
words in little-endian byte order.
The example's data labels are also available to instructions and expressions.

## Source lines and labels

An instruction or directive may optionally have a label followed by a colon.
Labels may also appear alone on a line. A semicolon starts a comment outside a
quoted string. Mnemonics are case-insensitive. Register names are
case-insensitive, including indirect forms such as `(BC)`, `(DE)`, `(IX)`, and
`(IY)`. Labels are case-sensitive.

Global labels must be unique and must not start with `@`. Use them for routine
entry points, data, and constants. A global label can be referenced before or
after its definition:

```asm
        JP START
MESSAGE: DEFM "Ready"
START:  LD HL,MESSAGE
        RET
```

Local labels start with `@` and may be redefined. A reference resolves to the
nearest matching definition in source order: a forward reference uses the next
definition, and a backward reference uses the previous one. This lets separate
code blocks reuse short branch labels:

```asm
FIRST:  LD A,1
        JR Z,@DONE
        INC A
@DONE:  RET

SECOND: LD A,2
        JR Z,@DONE
        DEC A
@DONE:  RET
```

`CLRLOC` clears the current local-label table. Use it when starting a new
independent assembly section that reuses local names; do not clear it while a
forward local-label reference is still unresolved.

```asm
        CLRLOC
        LD B,3
@AGAIN: DJNZ @AGAIN
        CLRLOC
        LD B,2
@AGAIN: DJNZ @AGAIN
```

## Instructions and operand limits

The assembler recognizes the following instruction mnemonics:

```text
ADC ADD AND BIT CALL CCF CP CPD CPDR CPI CPIR CPL DAA DEC DI DJNZ EI EX EXX
HALT IM IN INC IND INDR INI INIR JP JR LD LDD LDDR LDI LDIR NEG NOP OR OTDR
OTIR OUT OUTD OUTI POP PUSH RES RET RETI RETN RL RLA RLCA RLC RLD RR RRA RRCA
RRD RRC RRC RST SBC SCF SET SLA SRA SRL SUB XOR
```

Only implemented operand forms are accepted; a documented Z80 instruction
outside those forms is not necessarily supported. Undocumented instructions
are not supported.

- IX/IY indexed memory operands use the form `(IX+displacement)` or
  `(IY-displacement)`. The `+` or `-` is required, including for offset zero.
  Displacements are signed bytes in **-128..127**:

  ```asm
  LD A,(IX-1)       ; read from the address IX-1
  LD (IX+2),A       ; write to IX+2
  LD B,(IY+0)       ; access the byte addressed by IY
  LD (IY-2),#FF     ; store an immediate byte
  ```

  The displacement may be an expression or label, for example
  `LD A,(IX+FIELD_OFFSET)`. The instruction must support indexed addressing;
  indexed operands are not valid for every Z80 instruction.
- ALU 8-bit immediates handled as signed-or-unsigned bytes accept **-128..255**.
  For example, both `CP -128` and `CP 255` are accepted. Other operands can
  have narrower rules: `LD r,n`, `IN`, and `OUT` require **0..255**. `DEFB`
  also takes **0..255**.
- `DEFW` accepts signed or unsigned 16-bit values (**-32768..65535**) and emits
  the low byte first.
- Relative branches (`JR` and `DJNZ`) require a signed 8-bit displacement from
  the instruction following the branch. `JR` supports the conditions `NZ`, `Z`,
  `NC`, and `C`.
- `LD (SP),n` is rejected; it is not a documented Z80 instruction.

## Directives

| Directive                     | Behavior                                                                                                           |
|-------------------------------|--------------------------------------------------------------------------------------------------------------------|
| `ORG <expression>`            | Sets the program counter to an address from 0 through 65535.                                                       |
| `ENT <expression>`            | Sets the entry-point address; it emits no bytes.                                                                   |
| `DEFB <expression>, ...`      | Emits one byte per expression; each value must be 0 through 255.                                                   |
| `DEFW <expression>, ...`      | Emits one little-endian word per expression; values may be signed or unsigned 16-bit.                              |
| `DEFM "<text>"`               | Emits one raw byte per character; characters above 255 are rejected. Escape sequences are not decoded.             |
| `DEFS <length>`               | Emits the requested number of zero-filled bytes; length must be 0 through 65535.                                   |
| `<label>: EQU <expression>`   | Defines a numeric label value without emitting bytes.                                                              |
| `CLRLOC`                      | Clears all currently defined local labels.                                                                         |
| `END`                         | Stops processing the rest of the source file.                                                                      |
| `ASSERT <value-or-text>, ...` | Prints the supplied values/text during assembly passes; it does not test a condition or fail when a value is zero. |

`EQU` expressions can refer to labels defined later. Forward `EQU` references
are resolved by repeated passes; circular dependencies fail assembly.

```asm
SIZE:   EQU DATA_END-DATA
START:  DEFW DATA,SIZE
DATA:   DEFB 1,2,3
DATA_END:
```

## Expressions

Expressions support decimal, `#`-prefixed hexadecimal, `%`-prefixed binary,
labels, the current program-counter symbol `$`, and `+` and `-` operations.
Operations are evaluated from left to right; there is no multiplication,
division, or parenthesis-based precedence.

```asm
        ORG #8000
BASE:   EQU #1000
        DEFW BASE+4
HERE:   DEFW $
        DEFB 10-3
```

Quoted strings can also be used as expression operands. Their characters are
packed into an integer by shifting left one byte and appending each character;
this is not the same as `DEFM`, which emits the characters directly. Expression
strings support `\n`, `\t`, `\b`, `\r`, `\f`, and `\'`. For example,
`"    "` evaluates to `#20202020`.

## Address layout and program counter

The program counter begins at zero and advances as instructions and data are
emitted. `ORG` changes it; it does not itself emit bytes. The generated image
covers the range from the first emitted byte to the last, with zero-filled gaps
between emitted regions. The entry point defaults to zero unless set by `ENT`.

`ASSERT` is currently a diagnostic-print directive, not a build-time assertion.
For example, `ASSERT SIZE,"bytes"` prints the evaluated value and text during
the assembler passes; it does not check that `SIZE` satisfies a condition and
may print more than once.
