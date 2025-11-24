package simple;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MutationTesting {

    // Helper class to encapsulate the result of a single type check run
    private static class CheckResult {
        final boolean isWellTyped;
        final String errorMessage;

        public CheckResult(boolean isWellTyped, String errorMessage) {
            this.isWellTyped = isWellTyped;
            this.errorMessage = errorMessage;
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -cp \"bin:lib/java-cup-11b.jar\" simple.MutationTesting <path_to_test_file>");
            System.exit(1);
        }
        String filePath = args[0];
        runMutationSuite(filePath);
    }

    private static void runMutationSuite(String filePath) {
        ProgramNode originalAst = null;

        try (PrintWriter writer = new PrintWriter(new FileWriter("mutation_results.txt"))) {
            
            writer.println("--- MUTATION TESTING REPORT ---");
            writer.println("Input File: " + filePath);
            writer.println("Mutator Used: RTR (Return Type Replacement)\n");

            // 1. Parse the original program
            originalAst = parseProgram(filePath);
            
            writer.println("--- Original AST ---");
            writer.println(originalAst.toString());
            writer.println("--------------------\n");


            // 2. Check if the original program is well-typed (sanity check)
            CheckResult originalResult = runTypeCheck(originalAst, new TypeChecker());

            if (!originalResult.isWellTyped) {
                writer.println("CRITICAL FAILURE: Original program is ILL-TYPED.");
                writer.println("Error: " + originalResult.errorMessage);
                System.err.println("CRITICAL FAILURE: Original program is ILL-TYPED. Cannot proceed.");
                return;
            }
            
            writer.println("Original Program: PASS (Well-typed). Proceeding with mutation.\n");

            // 3. Generate mutants using the operator
            Mutator mutator = new RTRMutator();
            MutationTraverser traverser = new MutationTraverser(mutator);
            
            MutationResult result = traverser.generateMutants(originalAst);
            
            writer.printf("Total Mutants Generated: %d\n\n", result.mutants.size());

            int killedCount = 0;
            
            // 4. Test each mutant
            boolean printFlag = true;
            for (int i = 0; i < result.mutants.size(); i++) {
                ProgramNode mutant = result.mutants.get(i);
                
                // Must create a NEW TypeChecker instance for each mutant to reset the symbol table
                TypeChecker typeChecker = new TypeChecker();
                CheckResult mutantResult = runTypeCheck(mutant, typeChecker);
                
                String status = mutantResult.isWellTyped ? "SURVIVED" : "KILLED";
                
                writer.printf("Mutant %d: %s\n", i + 1, status);
                
                if (status.equals("KILLED")) {
                    killedCount++;
                    // Print the error message when the mutant is killed
                    writer.printf("  Reason: Type Checker threw error -> %s\n", mutantResult.errorMessage);
                    // To include the AST of one killed mutant:
                    if(printFlag){
                        writer.println("  Mutant AST:");
                        writer.println(mutant.toString());
                        printFlag = !printFlag;
                    }
                    
                } else {
                    writer.printf("  Reason: Passed type check (Survived).\n");
                }
            }
            
            writer.println("\n--- FINAL SUMMARY ---");
            writer.printf("Killed Mutants: %d\n", killedCount);
            writer.printf("Mutation Score: %.2f%%\n", 
                          result.mutants.isEmpty() ? 0.0 : ((double) killedCount / result.mutants.size()) * 100);

            System.out.println("Mutation testing complete. Results written to mutation_results.txt");


        } catch (Exception e) {
            System.err.println("An unexpected error occurred during setup or parsing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static ProgramNode parseProgram(String filePath) throws Exception {
        FileReader reader = new FileReader(filePath);
        SimpleLexer lexer = new SimpleLexer(reader);
        parser p = new parser(lexer);
        
        return (ProgramNode) p.parse().value;
    }

    /**
     * Runs the type checker on the given AST, capturing the error message if it fails.
     * @param ast The program AST.
     * @param checker The TypeChecker instance.
     * @return A CheckResult object containing the status and error message (if applicable).
     */
    private static CheckResult runTypeCheck(ProgramNode ast, TypeChecker checker) {
        try {
            checker.typeCheckProgram(ast);
            return new CheckResult(true, null);
        } catch (Exception e) {
            // Type checker failed, capture the error message
            return new CheckResult(false, e.getMessage());
        }
    }
}