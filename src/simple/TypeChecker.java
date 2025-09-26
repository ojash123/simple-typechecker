package simple;

import java.util.Map;

public class TypeChecker {
    private TypeEnvironment typeEnv; //Symbol table
    private Map<AstNode, TypeExpr> exprTable; //type var table
    private static final TypeConst INT_TYPE = new TypeConst(Type.INTEGER);
    private static final TypeConst BOOL_TYPE = new TypeConst(Type.BOOLEAN);

}
