public class Instruction {

    public enum OpCode {
        MOV, ADD, SUB, MUL, DIV, MOD,
        AND, OR, XOR, NOT, SHL, SHR,
        CMP,
        JMP, JEQ, JNE, JLT, JGT, JLE, JGE,
        PUSH, POP,
        CALL, RET,
        PRINT, INPUT,
        HLT,
        NOP
    }

    public final OpCode opCode;
    public final String operand1;
    public final String operand2;
    public final int line;

    public Instruction(OpCode opCode, String operand1, String operand2, int line) {
        this.opCode = opCode;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.line = line;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(opCode.name());
        if (operand1 != null) sb.append(" ").append(operand1);
        if (operand2 != null) sb.append(", ").append(operand2);
        return sb.toString();
    }
}
