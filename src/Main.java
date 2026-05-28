public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Main <file.ass> [--debug]");
            System.out.println();
            printHelp();
            System.exit(1);
        }

        String filePath = args[0];
        boolean debug = args.length > 1 && args[1].equals("--debug");

        Assembler assembler = new Assembler();
        try {
            assembler.assemble(filePath);
            assembler.execute(debug);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("=== Simplified Assembler ===");
        System.out.println();
        System.out.println("Registers : R0 to R7");
        System.out.println("Values    : decimal (42), hex (0xFF), binary (0b1010)");
        System.out.println("Labels    : name followed by ':' (e.g. loop:)");
        System.out.println("Comments  : everything after ';'");
        System.out.println();
        System.out.println("Available instructions:");
        System.out.println("  MOV  dest, src    - Load a value into a register");
        System.out.println("  ADD  dest, src    - Addition (dest = dest + src)");
        System.out.println("  SUB  dest, src    - Subtraction (dest = dest - src)");
        System.out.println("  MUL  dest, src    - Multiplication (dest = dest * src)");
        System.out.println("  DIV  dest, src    - Integer division (dest = dest / src)");
        System.out.println("  MOD  dest, src    - Modulo (dest = dest % src)");
        System.out.println("  AND  dest, src    - Bitwise AND");
        System.out.println("  OR   dest, src    - Bitwise OR");
        System.out.println("  XOR  dest, src    - Bitwise XOR");
        System.out.println("  NOT  dest         - Bitwise NOT");
        System.out.println("  SHL  dest, n      - Shift left");
        System.out.println("  SHR  dest, n      - Shift right");
        System.out.println("  CMP  a, b         - Compare a and b (updates flags)");
        System.out.println("  JMP  label        - Unconditional jump");
        System.out.println("  JEQ  label        - Jump if equal (Z=1)");
        System.out.println("  JNE  label        - Jump if not equal (Z=0)");
        System.out.println("  JLT  label        - Jump if less than");
        System.out.println("  JGT  label        - Jump if greater than");
        System.out.println("  JLE  label        - Jump if less or equal");
        System.out.println("  JGE  label        - Jump if greater or equal");
        System.out.println("  PUSH src          - Push a value onto the stack");
        System.out.println("  POP  dest         - Pop a value into a register");
        System.out.println("  CALL label        - Call a subroutine");
        System.out.println("  RET               - Return from subroutine");
        System.out.println("  PRINT src         - Print a value");
        System.out.println("  INPUT dest        - Read an integer from input");
        System.out.println("  HLT               - Halt the program");
        System.out.println("  NOP               - No operation");
    }
}
