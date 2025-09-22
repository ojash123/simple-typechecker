package simple;

import java.util.List;
//import java.util.stream.Collectors;

// --- Helper for indentation ---
class Indent {
    public static String get(int level) {
        return "  ".repeat(level);
    }
}

interface AstNode {
    String toString(int indent);

    @Override
    String toString();
}

abstract class Stmt implements AstNode {
     @Override
    public final String toString() {
        return toString(0);
    }
}

abstract class Expr implements AstNode {
     @Override
    public final String toString() {
        return toString(0);
    }
}

enum Type {
    INTEGER, BOOLEAN
}

enum Operator {
    ADD, SUB, MUL, DIV, EQ, LT, GT, AND, OR
}

class ProgramNode implements AstNode {
    final List<FuncDef> fns;
    final List<VarDecl> globals;
    final List<Stmt> main;

    public ProgramNode(List<FuncDef> fns, List<VarDecl> globals, List<Stmt> main) {
        this.fns = fns;
        this.globals = globals;
        this.main = main;
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(Indent.get(indent)).append("ProgramNode:\n");
        sb.append("Definitions: \n");
        for (FuncDef fn : fns) {
            sb.append(fn.toString(indent));
        }
        sb.append("Globals: \n");
        for (VarDecl global : globals) {
            sb.append(global.toString(indent));
        }
        sb.append("Main: \n");
        for (Stmt stmt : main) {
            sb.append(stmt.toString(indent));
        }
        return sb.toString();
    }
    
    @Override
    public final String toString() {
        return toString(0);
    }
}

class VarDecl extends Stmt {
    final String name;
    final Type type;

    public VarDecl(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString(int indent) {
        return String.format("%sVarDecl: %s %s ", Indent.get(indent), type, name);
    }
}

class FuncDef implements AstNode {
    final String name;
    final List<VarDecl> params;
    final Stmt body;

    public FuncDef(String name, List<VarDecl> params, Stmt body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(Indent.get(indent)).append("Func: ").append(name).append(" ");
        sb.append(Indent.get(indent)).append("Params: ");
        for (VarDecl param : params) {
            sb.append(param.toString(indent));
        }
        sb.append(Indent.get(indent)).append("\nBody:\n");
        sb.append(body.toString(indent + 1));
        return sb.toString();
    }
    
    @Override
    public final String toString() {
        return toString(0);
    }
}

class BlockStmt extends Stmt {
    final List<VarDecl> declarations;
    final List<Stmt> statements;

    public BlockStmt(List<VarDecl> declarations, List<Stmt> statements) {
        this.declarations = declarations;
        this.statements = statements;
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(Indent.get(indent)).append("BlockStmt:\n");
        for (VarDecl decl : declarations) {
            sb.append(decl.toString(indent + 1));
        }
        sb.append(Indent.get(indent)).append("Statements\n");
        for (Stmt stmt : statements) {
            sb.append(stmt.toString(indent + 1));
        }
        return sb.toString();
    }
}

class AssignStmt extends Stmt {
    final String id;
    final Expr expr;

    public AssignStmt(String id, Expr expr) {
        this.id = id;
        this.expr = expr;
    }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(Indent.get(indent)).append(id).append(" := ");
        sb.append(expr.toString(indent)).append("\n");
        return sb.toString();
    }
}

class IfStmt extends Stmt {
    final Expr conditional;
    final Stmt t;
    final Stmt e;

    public IfStmt(Expr conditional, Stmt t, Stmt e) {
        this.conditional = conditional;
        this.t = t;
        this.e = e;
    }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(Indent.get(indent)).append("If (");
        sb.append(conditional.toString(indent)).append(")");
        sb.append("\n" + Indent.get(indent)).append("Then:\n");
        sb.append(t.toString(indent + 1));
        if (e != null) {
            sb.append("\n" + Indent.get(indent)).append("Else:\n");
            sb.append(e.toString(indent + 1));
        }
        sb.append("\n");
        return sb.toString();
    }
}

class LoopStmt extends Stmt {
    final Expr conditional;
    final Stmt body;

    public LoopStmt(Expr conditional, Stmt body) {
        this.conditional = conditional;
        this.body = body;
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(Indent.get(indent)).append("Loop:\n");
        sb.append(Indent.get(indent)).append("while : ");
        sb.append(conditional.toString(0)).append("\n");
        sb.append(Indent.get(indent + 1)).append("Body: \n");
        sb.append(body.toString(indent + 1)).append("\n");
        return sb.toString();
    }
}

class ReturnStmt extends Stmt {
    final Expr expr;

    public ReturnStmt(Expr expr) {
        this.expr = expr;
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(Indent.get(indent)).append("ReturnStmt: ");
        sb.append(expr.toString(0)).append("\n");
        return sb.toString();
    }
}

class BinaryExpr extends Expr {
    final Expr left;
    final Operator op;
    final Expr right;

    public BinaryExpr(Expr left, Operator op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(left.toString(indent));
        sb.append(" ").append(op).append(" ");
        sb.append(right.toString(0));
        return sb.toString();
    }
}

class IdExpr extends Expr {
    final String name;

    public IdExpr(String name) {
        this.name = name;
    }

    @Override
    public String toString(int indent) {
        return String.format("Id: %s ", name);
    }
}

class IntLiteral extends Expr {
    final int value;

    public IntLiteral(int value) {
        this.value = value;
    }

    @Override
    public String toString(int indent) {
        return String.format("IntLiteral: %d ", value);
    }
}

class BoolLiteral extends Expr {
    final boolean value;

    public BoolLiteral(boolean value) {
        this.value = value;
    }

    @Override
    public String toString(int indent) {
        return String.format("BoolLiteral: %b ", value);
    }
}

class FuncCall extends Expr {
    final String name;
    final List<Expr> args;

    public FuncCall(String name, List<Expr> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(Indent.get(indent)).append("FuncCall: ").append(name).append(" ");
        sb.append("Args: (");
        for (Expr arg : args) {
            sb.append(arg.toString(0));
        }
        sb.append(")");
        return sb.toString();
    }
}
