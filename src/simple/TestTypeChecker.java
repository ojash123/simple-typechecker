package simple;

import java.io.FileReader;

public class TestTypeChecker {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -cp \"bin:lib/java-cup-11b.jar\" <path_to_test_file>");
            System.exit(1);
        }
        String filePath = args[0];
        runTest(filePath);
    }

    private static void runTest(String filePath) {
        System.out.println("--- Running Test Case: " + filePath + " ---");
        try {
            FileReader reader = new FileReader(filePath);
            SimpleLexer lexer = new SimpleLexer(reader);
            parser p = new parser(lexer);

            ProgramNode ast = (ProgramNode) p.parse().value;
            // System.out.println("--- Parsed AST ---");
            // System.out.println(ast.toString(0));
            
            TypeChecker typeChecker = new TypeChecker();
            typeChecker.typeCheckProgram(ast);

            System.out.println("Result: PASS - Program is well-typed.");

        } catch (Exception e) {
            System.out.println("Result: FAIL - Type checker threw an exception.");
            System.out.println("Error Message: " + e.getMessage());
        }
        System.out.println("---------------------------------------\n");
    }
}
