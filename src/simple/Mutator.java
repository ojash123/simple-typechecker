package simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface Mutator {
     List<AstNode> apply(AstNode astNode);
}

class MutationResult {
    final ProgramNode original;
    final List<ProgramNode> mutants;

    public MutationResult(ProgramNode original, List<ProgramNode> mutants) {
        this.original = original;
        this.mutants = mutants;
    }
}

class AORMutator implements Mutator {
    // All arithmetic operators for replacement
    private static final List<Operator> ARITHMETIC_OPS = Arrays.asList(
        Operator.ADD, Operator.SUB, Operator.MUL, Operator.DIV
    );
    private static final List<Operator> BOOL_OPS = Arrays.asList(
        Operator.AND, Operator.OR
    );

    @Override
    public List<AstNode> apply(AstNode astNode) {
        if (astNode instanceof BinaryExpr) {
            BinaryExpr expr = (BinaryExpr) astNode;
            if (ARITHMETIC_OPS.contains(expr.op)) {
                
                List<AstNode> mutants = new ArrayList<>();
                // Generate a new mutant for every boolean operator
                for (Operator newOp : BOOL_OPS) {
                    if (newOp != expr.op) {
                        // Create a NEW BinaryExpr node with the mutated operator
                        mutants.add(new BinaryExpr((Expr)(expr.left.deepCopy()), newOp, (Expr)expr.right.deepCopy()));
                    }
                }
                return mutants;
            }
        }
        return Collections.emptyList();
    }
    
}
// --- Concrete Mutation Operator 2: LCR (Literal Constant Replacement) ---

class LCRMutator implements Mutator {
    @Override
    public List<AstNode> apply(AstNode astNode) {
        List<AstNode> mutants = new ArrayList<>();
        
        if (astNode instanceof IntLiteral) {
            IntLiteral literal = (IntLiteral) astNode;
            
            // Mutation 2a: Value Replacement (e.g., 5 -> 0)
            if (literal.value != 0) {
                mutants.add(new IntLiteral(0));
            } else {
                mutants.add(new IntLiteral(1));
            }
            
            // Mutation 2b: Type Replacement (Int -> Bool)
            mutants.add(new BoolLiteral(true));
            mutants.add(new BoolLiteral(false));

        } else if (astNode instanceof BoolLiteral) {
            BoolLiteral literal = (BoolLiteral) astNode;
            
            // Mutation 2c: Value Replacement (e.g., true -> false)
            mutants.add(new BoolLiteral(!literal.value));
            
            // Mutation 2d: Type Replacement (Bool -> Int)
            mutants.add(new IntLiteral(0));
        }
        
        return mutants;
    }
}
// --- Concrete Mutation Operator 3: RTR (Return Type Replacement) ---

class RTRMutator implements Mutator {
    
    // Constant replacements that guarantee a type mismatch if the original type was correct.
    private static final IntLiteral INT_REPLACEMENT = new IntLiteral(0);
    private static final BoolLiteral BOOL_REPLACEMENT = new BoolLiteral(true);
    
    @Override
    public List<AstNode> apply(AstNode astNode) {
        if (astNode instanceof ReturnStmt) {            
            List<AstNode> mutants = new ArrayList<>();
            
            // Mutant 1: Force return to be an Integer literal (expecting failure if function expects bool/any other type)
            mutants.add(new ReturnStmt(INT_REPLACEMENT));

            // Mutant 2: Force return to be a Boolean literal (expecting failure if function expects int/any other type)
            mutants.add(new ReturnStmt(BOOL_REPLACEMENT));
            
            return mutants;
        }
        return Collections.emptyList();
    }
}