import java.util.*;

public class CPU {

    private final int[] registers = new int[8]; // R0-R7
    private int pc = 0;                         // Program Counter
    private int sp = 1023;                      // Stack Pointer (top of stack)
    private final int[] stack = new int[1024];

    private boolean zeroFlag = false;
    private boolean negativeFlag = false;

    private boolean halted = false;

    private final List<Instruction> program;
    private final Map<String, Integer> labels;
    private final Deque<Integer> callStack = new ArrayDeque<>();
    private final Scanner scanner = new Scanner(System.in);

    public CPU(List<Instruction> program, Map<String, Integer> labels) {
        this.program = program;
        this.labels = labels;
    }

    public void run(boolean debug) {
        while (!halted && pc < program.size()) {
            Instruction instr = fetch();
            if (debug) {
                System.out.printf("[DEBUG] PC=%d | %s | Regs=%s | Z=%b N=%b%n",
                        pc - 1, instr, Arrays.toString(registers), zeroFlag, negativeFlag);
            }
            execute(instr);
        }
        if (!halted) {
            System.out.println("Program terminated (end of file reached).");
        }
    }

    private Instruction fetch() {
        return program.get(pc++);
    }

    private void execute(Instruction instr) {
        switch (instr.opCode) {
            case MOV -> execMov(instr);
            case ADD -> execArith(instr, (a, b) -> a + b);
            case SUB -> execArith(instr, (a, b) -> a - b);
            case MUL -> execArith(instr, (a, b) -> a * b);
            case DIV -> execDiv(instr, false);
            case MOD -> execDiv(instr, true);
            case AND -> execArith(instr, (a, b) -> a & b);
            case OR  -> execArith(instr, (a, b) -> a | b);
            case XOR -> execArith(instr, (a, b) -> a ^ b);
            case NOT -> execNot(instr);
            case SHL -> execArith(instr, (a, b) -> a << b);
            case SHR -> execArith(instr, (a, b) -> a >> b);
            case CMP -> execCmp(instr);
            case JMP -> execJmp(instr, true);
            case JEQ -> execJmp(instr, zeroFlag);
            case JNE -> execJmp(instr, !zeroFlag);
            case JLT -> execJmp(instr, negativeFlag && !zeroFlag);
            case JGT -> execJmp(instr, !negativeFlag && !zeroFlag);
            case JLE -> execJmp(instr, negativeFlag || zeroFlag);
            case JGE -> execJmp(instr, !negativeFlag || zeroFlag);
            case PUSH -> execPush(instr);
            case POP  -> execPop(instr);
            case CALL -> execCall(instr);
            case RET  -> execRet();
            case PRINT -> execPrint(instr);
            case INPUT -> execInput(instr);
            case HLT -> {
                halted = true;
                System.out.println("Program halted (HLT).");
            }
            case NOP -> {}
        }
    }

    private void execMov(Instruction instr) {
        int reg = parseRegister(instr.operand1, instr.line);
        registers[reg] = resolveValue(instr.operand2, instr.line);
    }

    private void execArith(Instruction instr, ArithOp op) {
        int reg = parseRegister(instr.operand1, instr.line);
        int val = resolveValue(instr.operand2, instr.line);
        registers[reg] = op.apply(registers[reg], val);
        updateFlags(registers[reg]);
    }

    private void execDiv(Instruction instr, boolean modulo) {
        int reg = parseRegister(instr.operand1, instr.line);
        int val = resolveValue(instr.operand2, instr.line);
        if (val == 0) error(instr.line, "Division by zero");
        registers[reg] = modulo ? registers[reg] % val : registers[reg] / val;
        updateFlags(registers[reg]);
    }

    private void execNot(Instruction instr) {
        int reg = parseRegister(instr.operand1, instr.line);
        registers[reg] = ~registers[reg];
        updateFlags(registers[reg]);
    }

    private void execCmp(Instruction instr) {
        int a = resolveValue(instr.operand1, instr.line);
        int b = resolveValue(instr.operand2, instr.line);
        int result = a - b;
        updateFlags(result);
    }

    private void execJmp(Instruction instr, boolean condition) {
        if (!condition) return;
        String label = instr.operand1;
        if (!labels.containsKey(label)) error(instr.line, "Unknown label: " + label);
        pc = labels.get(label);
    }

    private void execPush(Instruction instr) {
        if (sp < 0) error(instr.line, "Stack overflow");
        stack[sp--] = resolveValue(instr.operand1, instr.line);
    }

    private void execPop(Instruction instr) {
        if (sp >= stack.length - 1) error(instr.line, "Stack underflow");
        int reg = parseRegister(instr.operand1, instr.line);
        registers[reg] = stack[++sp];
    }

    private void execCall(Instruction instr) {
        String label = instr.operand1;
        if (!labels.containsKey(label)) error(instr.line, "Unknown label: " + label);
        callStack.push(pc);
        pc = labels.get(label);
    }

    private void execRet() {
        if (callStack.isEmpty()) error(-1, "RET without matching CALL");
        pc = callStack.pop();
    }

    private void execPrint(Instruction instr) {
        int val = resolveValue(instr.operand1, instr.line);
        System.out.println(val);
    }

    private void execInput(Instruction instr) {
        int reg = parseRegister(instr.operand1, instr.line);
        System.out.print("Input (integer) > ");
        if (!scanner.hasNextInt()) error(instr.line, "Invalid input (integer expected)");
        registers[reg] = scanner.nextInt();
    }

    private int resolveValue(String operand, int line) {
        if (operand == null) error(line, "Missing operand");
        String upper = operand.toUpperCase();
        if (upper.startsWith("R") && upper.length() == 2 && Character.isDigit(upper.charAt(1))) {
            return registers[upper.charAt(1) - '0'];
        }
        try {
            if (upper.startsWith("0X")) return Integer.parseInt(operand.substring(2), 16);
            if (upper.startsWith("0B")) return Integer.parseInt(operand.substring(2), 2);
            return Integer.parseInt(operand);
        } catch (NumberFormatException e) {
            error(line, "Invalid operand: " + operand);
            return 0;
        }
    }

    private int parseRegister(String operand, int line) {
        if (operand == null) error(line, "Register expected");
        String upper = operand.toUpperCase();
        if (upper.startsWith("R") && upper.length() == 2 && Character.isDigit(upper.charAt(1))) {
            int idx = upper.charAt(1) - '0';
            if (idx < 0 || idx > 7) error(line, "Invalid register: " + operand + " (R0-R7)");
            return idx;
        }
        error(line, "Invalid register: " + operand + " (expected R0-R7)");
        return 0;
    }

    private void updateFlags(int result) {
        zeroFlag = (result == 0);
        negativeFlag = (result < 0);
    }

    private void error(int line, String msg) {
        String prefix = line >= 0 ? "Error at line " + line + ": " : "Error: ";
        throw new RuntimeException(prefix + msg);
    }

    @FunctionalInterface
    private interface ArithOp {
        int apply(int a, int b);
    }
}
