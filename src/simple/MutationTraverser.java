package simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

public class MutationTraverser {
    private final Mutator mutator;

    public MutationTraverser(Mutator mutator) {
        this.mutator = mutator;
    }
    public MutationResult generateMutants(ProgramNode originalProgram) {
        List<ProgramNode> mutants = new ArrayList<>();
        
        for (int i = 0; i < originalProgram.fns.size(); i++) {
            FuncDef originalFunc = originalProgram.fns.get(i);
            List<AstNode> mutatedFuncs = traverse(originalFunc);

            for (AstNode mutatedFunc : mutatedFuncs) {
                List<FuncDef> newFns = new ArrayList<>(originalProgram.fns);
                newFns.set(i, (FuncDef) mutatedFunc); // Replace function with mutant
                mutants.add(new ProgramNode(newFns, originalProgram.globals, originalProgram.main));
            }
        }
        
        List<Stmt> allMainStmts = new ArrayList<>();
        allMainStmts.addAll(originalProgram.globals);
        allMainStmts.addAll(originalProgram.main);

        for (int i = 0; i < allMainStmts.size(); i++) {
            Stmt originalStmt = allMainStmts.get(i);
            List<AstNode> mutatedStmts = traverse(originalStmt);

            for (AstNode mutatedStmt : mutatedStmts) {
                List<VarDecl> newGlobals = new ArrayList<>(originalProgram.globals);
                List<Stmt> newMain = new ArrayList<>(originalProgram.main);
                
                if (i < originalProgram.globals.size()) {
                    newGlobals.set(i, (VarDecl) mutatedStmt);
                } else {
                    int mainIndex = i - originalProgram.globals.size();
                    newMain.set(mainIndex, (Stmt) mutatedStmt);
                }
                
                mutants.add(new ProgramNode(originalProgram.fns, newGlobals, newMain));
            }
        }
        
        return new MutationResult(originalProgram, mutants);
    }
    
    private List<AstNode> traverse(AstNode node) {
        if (node == null) {
            return Collections.emptyList();
        }

        List<AstNode> localMutants = new ArrayList<>();
        localMutants.addAll(mutator.apply(node));
        AstNode workingCopy = node.deepCopy(); 

        if (workingCopy instanceof FuncDef) {
            FuncDef func = (FuncDef) workingCopy;
            List<AstNode> bodyMutants = traverse(func.body);
            for (AstNode m : bodyMutants) {
                localMutants.add(new FuncDef(func.name, func.params, (Stmt) m));
            }
        }
        
        else if (workingCopy instanceof BlockStmt) {
            BlockStmt block = (BlockStmt) workingCopy;            
            for (int i = 0; i < block.declarations.size(); i++) {
                VarDecl originalDecl = block.declarations.get(i);
                List<AstNode> declMutants = traverse(originalDecl);
                for (AstNode m : declMutants) {
                    List<VarDecl> newDecls = new ArrayList<>(block.declarations);
                    newDecls.set(i, (VarDecl) m);
                    localMutants.add(new BlockStmt(newDecls, block.statements));
                }
            }
            for (int i = 0; i < block.statements.size(); i++) {
                Stmt originalStmt = block.statements.get(i);
                List<AstNode> stmtMutants = traverse(originalStmt);
                for (AstNode m : stmtMutants) {
                    List<Stmt> newStmts = new ArrayList<>(block.statements);
                    newStmts.set(i, (Stmt) m);
                    localMutants.add(new BlockStmt(block.declarations, newStmts));
                }
            }
        }
        
        else if (workingCopy instanceof AssignStmt) {
            AssignStmt assign = (AssignStmt) workingCopy;
            List<AstNode> exprMutants = traverse(assign.expr);
            for (AstNode m : exprMutants) {
                localMutants.add(new AssignStmt(assign.id, (Expr) m));
            }
        }
        
        else if (workingCopy instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) workingCopy;
            List<AstNode> condMutants = traverse(ifStmt.conditional);
            for (AstNode m : condMutants) {
                localMutants.add(new IfStmt((Expr) m, ifStmt.t, ifStmt.e));
            }
            List<AstNode> tMutants = traverse(ifStmt.t);
            for (AstNode m : tMutants) {
                localMutants.add(new IfStmt(ifStmt.conditional, (Stmt) m, ifStmt.e));
            }
            if (ifStmt.e != null) { // Else branch exists
                List<AstNode> eMutants = traverse(ifStmt.e);
                for (AstNode m : eMutants) {
                    localMutants.add(new IfStmt(ifStmt.conditional, ifStmt.t, (Stmt) m));
                }
            }
        }
        
        else if (workingCopy instanceof LoopStmt) {
            LoopStmt loop = (LoopStmt) workingCopy;
            List<AstNode> condMutants = traverse(loop.conditional);
            for (AstNode m : condMutants) {
                localMutants.add(new LoopStmt((Expr) m, loop.body));
            }
            List<AstNode> bodyMutants = traverse(loop.body);
            for (AstNode m : bodyMutants) {
                localMutants.add(new LoopStmt(loop.conditional, (Stmt) m));
            }
        }
        
        else if (workingCopy instanceof ReturnStmt) {
            ReturnStmt ret = (ReturnStmt) workingCopy;
            List<AstNode> exprMutants = traverse(ret.expr);
            for (AstNode m : exprMutants) {
                localMutants.add(new ReturnStmt((Expr) m));
            }
        }
        
        else if (workingCopy instanceof BinaryExpr) {
            BinaryExpr binary = (BinaryExpr) workingCopy;
            List<AstNode> leftMutants = traverse(binary.left);
            for (AstNode m : leftMutants) {
                localMutants.add(new BinaryExpr((Expr) m, binary.op, binary.right));
            }
            List<AstNode> rightMutants = traverse(binary.right);
            for (AstNode m : rightMutants) {
                localMutants.add(new BinaryExpr(binary.left, binary.op, (Expr) m));
            }
        }
        
        else if (workingCopy instanceof FuncCall) {
            FuncCall call = (FuncCall) workingCopy;
            for (int i = 0; i < call.args.size(); i++) {
                Expr originalArg = call.args.get(i);
                List<AstNode> argMutants = traverse(originalArg);
                for (AstNode m : argMutants) {
                    List<Expr> newArgs = new ArrayList<>(call.args);
                    newArgs.set(i, (Expr) m);
                    localMutants.add(new FuncCall(call.name, newArgs));
                }
            }
        }
        
        return localMutants;
    }
}
