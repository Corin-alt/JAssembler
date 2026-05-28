import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Two-pass assembler that transforms a source file into a program executable by the {@link CPU}.
 * <p>
 * <b>Pass 1</b>: collects labels and their positions.<br>
 * <b>Pass 2</b>: decodes instructions and validates operands.
 * <p>
 * Cleaned lines (comments stripped, whitespace trimmed) are cached between
 * the two passes to avoid redundant processing.
 */
public class Assembler {

    private final List<Instruction> program = new ArrayList<>();
    private final Map<String, Integer> labels = new HashMap<>();

    /**
     * Assembles the given source file into a list of instructions and a label table.
     *
     * @param filePath path to the source file ({@code .ass})
     * @throws IOException if the file cannot be read
     */
    public void assemble(String filePath) throws IOException {
        List<String> rawLines = Files.readAllLines(Path.of(filePath));
        int lineCount = rawLines.size();
        String[] cleaned = new String[lineCount];

        for (int i = 0; i < lineCount; i++) {
            String line = rawLines.get(i);
            int semi = line.indexOf(';');
            cleaned[i] = (semi >= 0 ? line.substring(0, semi) : line).trim();
        }

        int instrIndex = 0;
        for (int i = 0; i < lineCount; i++) {
            String line = cleaned[i];
            if (line.isEmpty()) continue;

            if (line.charAt(line.length() - 1) == ':') {
                String label = line.substring(0, line.length() - 1).trim();
                if (labels.containsKey(label)) {
                    throw new RuntimeException("Line " + (i + 1) + ": duplicate label: " + label);
                }
                labels.put(label, instrIndex);
            } else {
                instrIndex++;
            }
        }

        for (int i = 0; i < lineCount; i++) {
            String line = cleaned[i];
            if (line.isEmpty() || line.charAt(line.length() - 1) == ':') continue;
            program.add(decode(line, i + 1));
        }
    }

    /**
     * Executes the assembled program on a new {@link CPU}.
     *
     * @param debug if {@code true}, enables debug mode with CPU state printed at each step
     */
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
        new CPU(program, labels).run(debug);
    }

    /**
     * Decodes a source line into an {@link Instruction}.
     *
     * @param line    the cleaned line (no comments, no leading/trailing whitespace)
     * @param lineNum the source line number for diagnostics
     * @return the decoded instruction
     */
    private Instruction decode(String line, int lineNum) {
        int spaceIdx = line.indexOf(' ');
        if (spaceIdx < 0) spaceIdx = line.indexOf('\t');

        String mnemonic;
        String rest;
        if (spaceIdx >= 0) {
            mnemonic = line.substring(0, spaceIdx).toUpperCase();
            rest = line.substring(spaceIdx + 1).trim();
        } else {
            mnemonic = line.toUpperCase();
            rest = null;
        }

        Instruction.OpCode opCode;
        try {
            opCode = Instruction.OpCode.valueOf(mnemonic);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Line " + lineNum + ": unknown instruction: " + mnemonic);
        }

        String operand1 = null;
        String operand2 = null;

        if (rest != null && !rest.isEmpty()) {
            int commaIdx = rest.indexOf(',');
            if (commaIdx >= 0) {
                operand1 = rest.substring(0, commaIdx).trim();
                operand2 = rest.substring(commaIdx + 1).trim();
            } else {
                operand1 = rest;
            }
        }

        validateOperands(opCode, operand1, operand2, lineNum);
        return new Instruction(opCode, operand1, operand2, lineNum);
    }

    /**
     * Validates that the number of operands matches the operation code.
     *
     * @param op   the operation code
     * @param op1  the first operand (may be {@code null})
     * @param op2  the second operand (may be {@code null})
     * @param line the source line number for diagnostics
     */
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
}
