package simple;

import java.util.Map;

public class TypeChecker {
    private TypeEnvironment typeEnv; //Symbol table
    private Map<AstNode, TypeExpr> exprTable; //type var table
    private static final TypeConst INT_TYPE = new TypeConst(Type.INTEGER);
    private static final TypeConst BOOL_TYPE = new TypeConst(Type.BOOLEAN);

    void unify(TypeExpr t1, TypeExpr t2) throws Exception{
        TypeExpr rep1 = t1.find(), rep2 = t2.find();
        if(rep1 == rep2){
            //same type
            return;
        }
        if(rep1 instanceof TypeVar){
            ((TypeVar)rep1).instance = rep2;
            return;
        }
        if(rep2 instanceof TypeVar){
            ((TypeVar)rep2).instance = rep1;
            return;
        }
        if(rep1 instanceof TypeConst && rep2 instanceof TypeConst){
            if(!rep1.equals(rep2) ){
                throw new Exception("Type Mismatch: " + rep1.toString() + " " + rep2.toString());
            }
            return;
        }
        if (rep1 instanceof FuncType && rep2 instanceof FuncType) {
            FuncType f1 = (FuncType) rep1;
            FuncType f2 = (FuncType) rep2;

            // Check for the same number of parameters.
            if (f1.paramTypes.size() != f2.paramTypes.size()) {
                throw new Exception("Function arity mismatch: Cannot unify " + f1 + " with " + f2);
            }
            //unify return types
            unify(f1.returnType, f2.returnType);

            //unify each parameter type.
            for (int i = 0; i < f1.paramTypes.size(); i++) {
                unify(f1.paramTypes.get(i), f2.paramTypes.get(i));
            }
            return;
        }
        throw new Exception("Type Mismatch: " + rep1.toString() + " " + rep2.toString());

    }

}
