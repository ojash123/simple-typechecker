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
