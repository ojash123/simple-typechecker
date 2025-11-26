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
    private static final List<Operator> ARITHMETIC_OPS = Arrays.asList(
            Operator.ADD, Operator.SUB, Operator.MUL, Operator.DIV);
    private static final List<Operator> BOOL_OPS = Arrays.asList(
            Operator.AND, Operator.OR);

    @Override
    public List<AstNode> apply(AstNode astNode) {
        if (astNode instanceof BinaryExpr) {
            BinaryExpr expr = (BinaryExpr) astNode;
            if (ARITHMETIC_OPS.contains(expr.op)) {
                List<AstNode> mutants = new ArrayList<>();
                for (Operator newOp : BOOL_OPS) {
                    if (newOp != expr.op) {
                        mutants.add(new BinaryExpr((Expr) (expr.left.deepCopy()), newOp, (Expr) expr.right.deepCopy()));
                    }
                }
                return mutants;
            }
        }
        return Collections.emptyList();
    }

}

class LCRMutator implements Mutator {
    @Override
    public List<AstNode> apply(AstNode astNode) {
        List<AstNode> mutants = new ArrayList<>();

        if (astNode instanceof IntLiteral) {
            IntLiteral literal = (IntLiteral) astNode;
            if (literal.value != 0) {
                mutants.add(new IntLiteral(0));
            } else {
                mutants.add(new IntLiteral(1));
            }
            mutants.add(new BoolLiteral(true));
            mutants.add(new BoolLiteral(false));
        } else if (astNode instanceof BoolLiteral) {
            BoolLiteral literal = (BoolLiteral) astNode;
            mutants.add(new BoolLiteral(!literal.value));
            mutants.add(new IntLiteral(0));
        }

        return mutants;
    }
}

class RTRMutator implements Mutator {
    private static final IntLiteral INT_REPLACEMENT = new IntLiteral(0);
    private static final BoolLiteral BOOL_REPLACEMENT = new BoolLiteral(true);

    @Override
    public List<AstNode> apply(AstNode astNode) {
        if (astNode instanceof ReturnStmt) {
            List<AstNode> mutants = new ArrayList<>();
            mutants.add(new ReturnStmt(INT_REPLACEMENT));
            mutants.add(new ReturnStmt(BOOL_REPLACEMENT));
            return mutants;
        }
        return Collections.emptyList();
    }
}