/**
 * Entry point for the JAssembler application.
 * <p>
 * Usage: {@code java Main <file.ass> [--debug]}
 * <p>
 * Assembles and executes a source file written in a simplified assembly language.
 * The {@code --debug} flag enables CPU state printing at each instruction.
 */
public class Main {

    /**
     * Assembles then executes the source file passed as argument.
     *
     * @param args {@code args[0]}: path to the source file;
     *             {@code args[1]} (optional): {@code --debug}
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Main <file.ass> [--debug]");
            System.out.println();
            printHelp();
            System.exit(1);
        }

        String filePath = args[0];
        boolean debug = args.length > 1 && "--debug".equals(args[1]);

        Assembler assembler = new Assembler();
        try {
            assembler.assemble(filePath);
            assembler.execute(debug);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Prints the full help message: registers, value formats, and instruction set.
     */
    private static void printHelp() {
        System.out.println("""
                === Simplified Assembler ===

                Registers : R0 to R7
                Values    : decimal (42), hex (0xFF), binary (0b1010)
                Labels    : name followed by ':' (e.g. loop:)
                Comments  : everything after ';'

                Available instructions:
                  MOV  dest, src    - Load a value into a register
                  ADD  dest, src    - Addition (dest = dest + src)
                  SUB  dest, src    - Subtraction (dest = dest - src)
                  MUL  dest, src    - Multiplication (dest = dest * src)
                  DIV  dest, src    - Integer division (dest = dest / src)
                  MOD  dest, src    - Modulo (dest = dest % src)
                  AND  dest, src    - Bitwise AND
                  OR   dest, src    - Bitwise OR
                  XOR  dest, src    - Bitwise XOR
                  NOT  dest         - Bitwise NOT
                  SHL  dest, n      - Shift left
                  SHR  dest, n      - Shift right
                  CMP  a, b         - Compare a and b (updates flags)
                  JMP  label        - Unconditional jump
                  JEQ  label        - Jump if equal (Z=1)
                  JNE  label        - Jump if not equal (Z=0)
                  JLT  label        - Jump if less than
                  JGT  label        - Jump if greater than
                  JLE  label        - Jump if less or equal
                  JGE  label        - Jump if greater or equal
                  PUSH src          - Push a value onto the stack
                  POP  dest         - Pop a value into a register
                  CALL label        - Call a subroutine
                  RET               - Return from subroutine
                  PRINT src         - Print a value
                  INPUT dest        - Read an integer from input
                  HLT               - Halt the program
                  NOP               - No operation""");
    }
}
