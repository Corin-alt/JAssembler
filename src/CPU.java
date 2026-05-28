import java.util.*;

/**
 * Virtual processor that executes an assembled program.
 * <p>
 * The CPU provides 8 general-purpose registers (R0-R7), a 1024-word stack,
 * a call stack for {@code CALL}/{@code RET}, and two flags (zero and negative)
 * updated by arithmetic operations and {@code CMP}.
 */
public class CPU {

    private static final int REGISTER_COUNT = 8;
    private static final int STACK_SIZE = 1024;

    private final int[] registers = new int[REGISTER_COUNT];
    private int pc;
    private int sp = STACK_SIZE - 1;
    private final int[] stack = new int[STACK_SIZE];

    private boolean zeroFlag;
    private boolean negativeFlag;
    private boolean halted;

    private final List<Instruction> program;
    private final Map<String, Integer> labels;
    private final Deque<Integer> callStack = new ArrayDeque<>();
    private Scanner scanner;

    /**
     * Creates a CPU loaded with the given program and label table.
     *
     * @param program ordered list of instructions to execute
     * @param labels  map associating each label name to its instruction index
     */
    public CPU(List<Instruction> program, Map<String, Integer> labels) {
        this.program = program;
        this.labels = labels;
    }

    /**
     * Runs the loaded program until {@code HLT} is encountered or the end of the program is reached.
     *
     * @param debug if {@code true}, prints the CPU state at each instruction
     */
    public void run(boolean debug) {
        int size = program.size();
        while (!halted && pc < size) {
            Instruction instr = program.get(pc++);
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

    /**
     * Dispatches execution of an instruction to the appropriate handler method.
     *
     * @param instr the instruction to execute
     */
    private void execute(Instruction instr) {
        switch (instr.opCode()) {
            case MOV   -> execMov(instr);
            case ADD   -> execArith(instr, (a, b) -> a + b);
            case SUB   -> execArith(instr, (a, b) -> a - b);
            case MUL   -> execArith(instr, (a, b) -> a * b);
            case DIV   -> execDiv(instr, false);
            case MOD   -> execDiv(instr, true);
            case AND   -> execArith(instr, (a, b) -> a & b);
            case OR    -> execArith(instr, (a, b) -> a | b);
            case XOR   -> execArith(instr, (a, b) -> a ^ b);
            case NOT   -> execNot(instr);
            case SHL   -> execArith(instr, (a, b) -> a << b);
            case SHR   -> execArith(instr, (a, b) -> a >> b);
            case CMP   -> execCmp(instr);
            case JMP   -> execJmp(instr, true);
            case JEQ   -> execJmp(instr, zeroFlag);
            case JNE   -> execJmp(instr, !zeroFlag);
            case JLT   -> execJmp(instr, negativeFlag && !zeroFlag);
            case JGT   -> execJmp(instr, !negativeFlag && !zeroFlag);
            case JLE   -> execJmp(instr, negativeFlag || zeroFlag);
            case JGE   -> execJmp(instr, !negativeFlag || zeroFlag);
            case PUSH  -> execPush(instr);
            case POP   -> execPop(instr);
            case CALL  -> execCall(instr);
            case RET   -> execRet();
            case PRINT -> execPrint(instr);
            case INPUT -> execInput(instr);
            case HLT   -> { halted = true; System.out.println("Program halted (HLT)."); }
            case NOP   -> {}
        }
    }

    private void execMov(Instruction instr) {
        int reg = parseRegister(instr.operand1(), instr.line());
        registers[reg] = resolveValue(instr.operand2(), instr.line());
    }

    private void execArith(Instruction instr, ArithOp op) {
        int reg = parseRegister(instr.operand1(), instr.line());
        int val = resolveValue(instr.operand2(), instr.line());
        int result = op.apply(registers[reg], val);
        registers[reg] = result;
        updateFlags(result);
    }

    private void execDiv(Instruction instr, boolean modulo) {
        int reg = parseRegister(instr.operand1(), instr.line());
        int val = resolveValue(instr.operand2(), instr.line());
        if (val == 0) error(instr.line(), "Division by zero");
        int result = modulo ? registers[reg] % val : registers[reg] / val;
        registers[reg] = result;
        updateFlags(result);
    }

    private void execNot(Instruction instr) {
        int reg = parseRegister(instr.operand1(), instr.line());
        int result = ~registers[reg];
        registers[reg] = result;
        updateFlags(result);
    }

    private void execCmp(Instruction instr) {
        int a = resolveValue(instr.operand1(), instr.line());
        int b = resolveValue(instr.operand2(), instr.line());
        updateFlags(a - b);
    }

    /**
     * Executes a conditional jump. The label is resolved with a single map lookup.
     */
    private void execJmp(Instruction instr, boolean condition) {
        if (!condition) return;
        Integer target = labels.get(instr.operand1());
        if (target == null) error(instr.line(), "Unknown label: " + instr.operand1());
        pc = target;
    }

    private void execPush(Instruction instr) {
        if (sp < 0) error(instr.line(), "Stack overflow");
        stack[sp--] = resolveValue(instr.operand1(), instr.line());
    }

    private void execPop(Instruction instr) {
        if (sp >= STACK_SIZE - 1) error(instr.line(), "Stack underflow");
        int reg = parseRegister(instr.operand1(), instr.line());
        registers[reg] = stack[++sp];
    }

    private void execCall(Instruction instr) {
        Integer target = labels.get(instr.operand1());
        if (target == null) error(instr.line(), "Unknown label: " + instr.operand1());
        callStack.push(pc);
        pc = target;
    }

    private void execRet() {
        if (callStack.isEmpty()) error(-1, "RET without matching CALL");
        pc = callStack.pop();
    }

    private void execPrint(Instruction instr) {
        System.out.println(resolveValue(instr.operand1(), instr.line()));
    }

    private void execInput(Instruction instr) {
        if (scanner == null) scanner = new Scanner(System.in);
        int reg = parseRegister(instr.operand1(), instr.line());
        System.out.print("Input (integer) > ");
        if (!scanner.hasNextInt()) error(instr.line(), "Invalid input (integer expected)");
        registers[reg] = scanner.nextInt();
    }

    /**
     * Resolves an operand to an integer value: register (R0-R7), hexadecimal (0x),
     * binary (0b) or decimal literal.
     * <p>
     * Avoids allocating a {@code toUpperCase()} string by testing characters directly.
     *
     * @param operand the string representing the operand
     * @param line    the source line number for diagnostics
     * @return the resolved integer value
     */
    private int resolveValue(String operand, int line) {
        if (operand == null) error(line, "Missing operand");
        int len = operand.length();
        char c0 = operand.charAt(0);

        if ((c0 == 'R' || c0 == 'r') && len == 2) {
            char c1 = operand.charAt(1);
            if (c1 >= '0' && c1 <= '7') return registers[c1 - '0'];
        }

        try {
            if (len > 2 && c0 == '0') {
                char c1 = operand.charAt(1);
                if (c1 == 'x' || c1 == 'X') return Integer.parseInt(operand, 2, len, 16);
                if (c1 == 'b' || c1 == 'B') return Integer.parseInt(operand, 2, len, 2);
            }
            return Integer.parseInt(operand);
        } catch (NumberFormatException e) {
            error(line, "Invalid operand: " + operand);
            return 0;
        }
    }

    /**
     * Parses an operand as a register identifier (R0-R7).
     *
     * @param operand the string representing the register
     * @param line    the source line number for diagnostics
     * @return the register index (0 to 7)
     */
    private int parseRegister(String operand, int line) {
        if (operand == null) error(line, "Register expected");
        if (operand.length() == 2) {
            char c0 = operand.charAt(0);
            char c1 = operand.charAt(1);
            if ((c0 == 'R' || c0 == 'r') && c1 >= '0' && c1 <= '7') {
                return c1 - '0';
            }
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
