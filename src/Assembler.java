import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Assembler {

    private final List<Instruction> program = new ArrayList<>();
    private final Map<String, Integer> labels = new LinkedHashMap<>();

    public void assemble(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filePath));

        // First pass: collect labels
        int instrIndex = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = stripComment(lines.get(i)).trim();
            if (line.isEmpty()) continue;

            if (line.endsWith(":")) {
                String label = line.substring(0, line.length() - 1).trim();
                if (labels.containsKey(label)) {
                    throw new RuntimeException("Line " + (i + 1) + ": duplicate label: " + label);
                }
                labels.put(label, instrIndex);
            } else {
                instrIndex++;
            }
        }

        // Second pass: decode instructions
        for (int i = 0; i < lines.size(); i++) {
            String line = stripComment(lines.get(i)).trim();
            if (line.isEmpty() || line.endsWith(":")) continue;

            Instruction instr = decode(line, i + 1);
            program.add(instr);
        }
    }

    public void execute(boolean debug) {
        if (program.isEmpty()) {
            System.out.println("No instructions to execute.");
            return;
        }
        if (debug) {
            System.out.println("=== Debug mode enabled ===");
            System.out.println("Labels: " + labels);
            System.out.println("Instructions: " + program.size());
            System.out.println("=========================");
        }
        CPU cpu = new CPU(program, labels);
        cpu.run(debug);
    }

    private Instruction decode(String line, int lineNum) {
        String[] parts = line.split("\\s+", 2);
        String mnemonic = parts[0].toUpperCase();

        Instruction.OpCode opCode;
        try {
            opCode = Instruction.OpCode.valueOf(mnemonic);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Line " + lineNum + ": unknown instruction: " + mnemonic);
        }

        String operand1 = null;
        String operand2 = null;

        if (parts.length > 1) {
            String operands = parts[1].trim();
            int commaIdx = operands.indexOf(',');
            if (commaIdx >= 0) {
                operand1 = operands.substring(0, commaIdx).trim();
                operand2 = operands.substring(commaIdx + 1).trim();
            } else {
                operand1 = operands.trim();
            }
        }

        validateOperands(opCode, operand1, operand2, lineNum);
        return new Instruction(opCode, operand1, operand2, lineNum);
    }

    private void validateOperands(Instruction.OpCode op, String op1, String op2, int line) {
        switch (op) {
            case MOV, ADD, SUB, MUL, DIV, MOD, AND, OR, XOR, SHL, SHR, CMP -> {
                if (op1 == null || op2 == null)
                    throw new RuntimeException("Line " + line + ": " + op + " requires 2 operands");
            }
            case NOT, PUSH, POP, PRINT, INPUT, JMP, JEQ, JNE, JLT, JGT, JLE, JGE, CALL -> {
                if (op1 == null)
                    throw new RuntimeException("Line " + line + ": " + op + " requires 1 operand");
            }
            case RET, HLT, NOP -> {}
        }
    }

    private String stripComment(String line) {
        int idx = line.indexOf(';');
        return idx >= 0 ? line.substring(0, idx) : line;
    }
}
