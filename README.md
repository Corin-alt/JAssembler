# Simplified Assembler Simulator

A Java program that reads a `.ass` file containing simplified assembly instructions, then fetches, decodes, and executes them.

## Build & Run

```bash
javac -d out src/*.java
java -cp out Main <file.ass> [--debug]
```

The `--debug` flag prints the CPU state (registers, flags) at each instruction.

## Architecture

```
src/
  Main.java         Entry point, argument parsing
  Assembler.java    Fetch (file reading) + Decode (parsing instructions & labels)
  CPU.java          Execute (fetch-decode-execute cycle, registers, flags, stack)
  Instruction.java  Decoded instruction representation
```

**Cycle:**
1. **Fetch** — read the instruction at the current Program Counter (PC)
2. **Decode** — parse opcode and operands (done at assembly time, two-pass)
3. **Execute** — perform the operation, update registers and flags

## Registers & Values

| Element    | Description                                      |
|------------|--------------------------------------------------|
| `R0`–`R7`  | 8 general-purpose 32-bit integer registers       |
| Decimal    | `42`, `-7`                                       |
| Hex        | `0xFF`                                           |
| Binary     | `0b1010`                                         |

## Instruction Set

### Data Transfer

| Instruction      | Description                     |
|------------------|---------------------------------|
| `MOV dest, src`  | Load a value into a register    |

### Arithmetic

| Instruction      | Description                     |
|------------------|---------------------------------|
| `ADD dest, src`  | `dest = dest + src`             |
| `SUB dest, src`  | `dest = dest - src`             |
| `MUL dest, src`  | `dest = dest * src`             |
| `DIV dest, src`  | `dest = dest / src` (integer)   |
| `MOD dest, src`  | `dest = dest % src`             |

### Bitwise

| Instruction      | Description                     |
|------------------|---------------------------------|
| `AND dest, src`  | Bitwise AND                     |
| `OR  dest, src`  | Bitwise OR                      |
| `XOR dest, src`  | Bitwise XOR                     |
| `NOT dest`       | Bitwise NOT                     |
| `SHL dest, n`    | Shift left by n bits            |
| `SHR dest, n`    | Shift right by n bits           |

### Comparison & Jumps

| Instruction      | Description                     |
|------------------|---------------------------------|
| `CMP a, b`       | Compare a and b (updates flags) |
| `JMP label`      | Unconditional jump              |
| `JEQ label`      | Jump if equal (Z=1)             |
| `JNE label`      | Jump if not equal (Z=0)         |
| `JLT label`      | Jump if less than               |
| `JGT label`      | Jump if greater than            |
| `JLE label`      | Jump if less or equal           |
| `JGE label`      | Jump if greater or equal        |

### Stack & Subroutines

| Instruction      | Description                     |
|------------------|---------------------------------|
| `PUSH src`       | Push a value onto the stack     |
| `POP dest`       | Pop a value into a register     |
| `CALL label`     | Call a subroutine               |
| `RET`            | Return from subroutine          |

### I/O & Control

| Instruction      | Description                     |
|------------------|---------------------------------|
| `PRINT src`      | Print a value to stdout         |
| `INPUT dest`     | Read an integer from stdin      |
| `HLT`            | Halt the program                |
| `NOP`            | No operation                    |

## Syntax

- **Labels** — a name followed by `:` on its own line (e.g. `loop:`)
- **Comments** — everything after `;` is ignored
- **Operands** — registers (`R0`–`R7`) or immediate values (decimal, hex, binary)

## Examples

### Factorial (5! = 120)

```asm
MOV R0, 5
MOV R1, 1

loop:
    CMP R0, 0
    JEQ end
    MUL R1, R0
    SUB R0, 1
    JMP loop

end:
    PRINT R1    ; 120
    HLT
```

### Fibonacci (first 10 terms)

```asm
MOV R0, 0
MOV R1, 1
MOV R2, 10

loop:
    CMP R2, 0
    JEQ end
    PRINT R0
    MOV R3, R1
    ADD R1, R0
    MOV R0, R3
    SUB R2, 1
    JMP loop

end:
    HLT
```

### Subroutines

```asm
MOV R0, 7
MOV R1, 3
PUSH R0
PUSH R1
CALL addition
PRINT R0        ; 10
HLT

addition:
    POP R5
    POP R4
    MOV R0, R4
    ADD R0, R5
    RET
```

More examples in the `exemples/` directory.
