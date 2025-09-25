package simple;

import java.util.List;
import java.util.stream.Collectors;

public class TypeEnvironment {

}
abstract class TypeExpr{}
class TypeConst extends TypeExpr{
    final Type type;
    
    public TypeConst(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
class TypeVar extends TypeExpr{
    private static int nextId = 0;
    final int id;
    TypeExpr instance = null;
    public TypeVar(){
        this.id = nextId++;
    }
    @Override
    public String toString() {
        if(instance != null)
            return instance.toString();
        return "t: " + String.valueOf(id);
    }
}
class FuncType extends TypeExpr{
    final TypeExpr returnType;
    final List<TypeExpr> paramTypes;
    public FuncType(TypeExpr returnType, List<TypeExpr> paramTypes) {
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }
    @Override
    public String toString() {
        // Use a stream to convert each parameter's TypeExpr to its string representation.
        String paramsStr = paramTypes.stream()
                                     .map(TypeExpr::toString) // Recursively calls toString on each type
                                     .collect(Collectors.joining(", "));

        // Combine the parameter string and the return type string into the final format.
        return String.format("(%s) -> %s", paramsStr, returnType.toString());
    }
    
}