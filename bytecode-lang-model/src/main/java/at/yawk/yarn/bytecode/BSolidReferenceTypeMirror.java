package at.yawk.yarn.bytecode;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javassist.bytecode.SignatureAttribute;
import javax.lang.model.element.Element;
import javax.lang.model.type.*;

/**
 * @author yawkat
 */
class BSolidReferenceTypeMirror extends BTypeMirror
        implements ReferenceType, ArrayType, DeclaredType, PrimitiveType, NullType {
    final SignatureAttribute.Type type;

    BSolidReferenceTypeMirror(BytecodeContext context, SignatureAttribute.Type type) {
        super(context);
        this.type = type;
    }

    @Override
    public TypeKind getKind() {
        switch (type.jvmTypeName()) {
        case "boolean":
            return TypeKind.BOOLEAN;
        case "byte":
            return TypeKind.BYTE;
        case "short":
            return TypeKind.SHORT;
        case "char":
            return TypeKind.CHAR;
        case "int":
            return TypeKind.INT;
        case "long":
            return TypeKind.LONG;
        case "float":
            return TypeKind.FLOAT;
        case "double":
            return TypeKind.DOUBLE;
        case "null":
            return TypeKind.NULL;
        }
        if (type instanceof SignatureAttribute.ArrayType) {
            return TypeKind.ARRAY;
        } else {
            return TypeKind.DECLARED;
        }
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        switch (getKind()) {
        case NULL:
            return v.visitNull(this, p);
        case ARRAY:
            return v.visitArray(this, p);
        case DECLARED:
            return v.visitDeclared(this, p);
        default: // primitives
            return v.visitPrimitive(this, p);
        }
    }

    @Override
    public List<BAnnotationMirror> getAnnotationMirrors() {
        throw unsupported();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        throw unsupported();
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        throw unsupported();
    }

    @Override
    public TypeMirror getComponentType() {
        return context.getTypeMirror(((SignatureAttribute.ArrayType) type).getComponentType());
    }

    @Override
    public Element asElement() {
        return context.findType(type);
    }

    @Override
    public TypeMirror getEnclosingType() {
        throw unsupported();
    }

    @Override
    public List<? extends TypeMirror> getTypeArguments() {
        if (type instanceof SignatureAttribute.ClassType) {
            SignatureAttribute.TypeArgument[] arguments =
                    ((SignatureAttribute.ClassType) type).getTypeArguments();
            if (arguments != null) {
                return Arrays.stream(arguments)
                        .map(arg -> context.getTypeMirror(type))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
