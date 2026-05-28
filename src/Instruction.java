/**
 * Immutable representation of an assembly instruction.
 * <p>
 * Each instruction consists of an operation code ({@link OpCode}),
 * zero to two operands, and the source line number for error diagnostics.
 *
 * @param opCode   the operation code of the instruction
 * @param operand1 the first operand, or {@code null} if absent
 * @param operand2 the second operand, or {@code null} if absent
 * @param line     the line number in the source file (1-based)
 */
public record Instruction(OpCode opCode, String operand1, String operand2, int line) {

    /**
     * Set of operation codes supported by the assembler.
     */
    public enum OpCode {
        /** Load a value into a register. */
        MOV,
        /** Addition. */
        ADD,
        /** Subtraction. */
        SUB,
        /** Multiplication. */
        MUL,
        /** Integer division. */
        DIV,
        /** Modulo. */
        MOD,
        /** Bitwise AND. */
        AND,
        /** Bitwise OR. */
        OR,
        /** Bitwise XOR. */
        XOR,
        /** Bitwise complement (NOT). */
        NOT,
        /** Left shift. */
        SHL,
        /** Right shift. */
        SHR,
        /** Compare two values and update flags. */
        CMP,
        /** Unconditional jump. */
        JMP,
        /** Jump if equal (Z=1). */
        JEQ,
        /** Jump if not equal (Z=0). */
        JNE,
        /** Jump if less than. */
        JLT,
        /** Jump if greater than. */
        JGT,
        /** Jump if less than or equal. */
        JLE,
        /** Jump if greater than or equal. */
        JGE,
        /** Push a value onto the stack. */
        PUSH,
        /** Pop a value from the stack into a register. */
        POP,
        /** Call a subroutine. */
        CALL,
        /** Return from a subroutine. */
        RET,
        /** Print a value to standard output. */
        PRINT,
        /** Read an integer from standard input. */
        INPUT,
        /** Halt the program. */
        HLT,
        /** No operation. */
        NOP
    }

    /**
     * Returns a human-readable representation of the instruction (e.g. {@code "ADD R0, R1"}).
     *
     * @return the formatted string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(opCode.name());
        if (operand1 != null) sb.append(' ').append(operand1);
        if (operand2 != null) sb.append(", ").append(operand2);
        return sb.toString();
    }
}
