package simple;

import java.io.FileReader;

public class TestTypeChecker {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java simple.TestTypeChecker <path_to_test_file>");
            System.exit(1);
        }
        String filePath = args[0];
        runTest(filePath);
    }

    private static void runTest(String filePath) {
        System.out.println("--- Running Test Case: " + filePath + " ---");
        try {
            // 1. Create the lexer and parser
            FileReader reader = new FileReader(filePath);
            SimpleLexer lexer = new SimpleLexer(reader);
            parser p = new parser(lexer);

            // 2. Parse the file to get the AST
            ProgramNode ast = (ProgramNode) p.parse().value;
            // System.out.println("--- Parsed AST ---");
            // System.out.println(ast.toString(0));
            
            // 3. Create and run the type checker
            TypeChecker typeChecker = new TypeChecker();
            typeChecker.typeCheckProgram(ast);

            // 4. If we reach here, no exception was thrown.
            System.out.println("Result: PASS - Program is well-typed.");

        } catch (Exception e) {
            // An exception means the type checker found an error.
            System.out.println("Result: FAIL - Type checker threw an exception.");
            System.out.println("Error Message: " + e.getMessage());
        }
        System.out.println("---------------------------------------\n");
    }
}
