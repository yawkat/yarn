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
        implements ReferenceType, ArrayType, DeclaredType, PrimitiveType, NullType, TypeVariable {
    final SignatureAttribute.Type type;

    BSolidReferenceTypeMirror(BytecodeContext context, SignatureAttribute.Type type) {
        super(context);
        this.type = type;
    }

    @Override
    public TypeKind getKind() {
        if (type instanceof SignatureAttribute.BaseType) {
            switch (((SignatureAttribute.BaseType) type).getDescriptor()) {
            case 'Z':
                return TypeKind.BOOLEAN;
            case 'B':
                return TypeKind.BYTE;
            case 'S':
                return TypeKind.SHORT;
            case 'C':
                return TypeKind.CHAR;
            case 'I':
                return TypeKind.INT;
            case 'J':
                return TypeKind.LONG;
            case 'F':
                return TypeKind.FLOAT;
            case 'D':
                return TypeKind.DOUBLE;
            case 'V':
                return TypeKind.VOID;
            }
        }
        if (type instanceof SignatureAttribute.ArrayType) {
            return TypeKind.ARRAY;
        }
        if (type instanceof SignatureAttribute.TypeVariable) {
            return TypeKind.TYPEVAR;
        }
        return TypeKind.DECLARED;
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
    public TypeMirror getUpperBound() {
        throw unsupported();
    }

    @Override
    public TypeMirror getLowerBound() {
        throw unsupported();
    }

    @Override
    public TypeMirror getEnclosingType() {
        throw unsupported();
    }

    @Override
    public List<? extends TypeMirror> getTypeArguments() {
        if (type instanceof SignatureAttribute.ClassType) {
            SignatureAttribute.ClassType classType = (SignatureAttribute.ClassType) this.type;
            SignatureAttribute.TypeArgument[] arguments = classType.getTypeArguments();
            if (arguments != null) {
                return Arrays.stream(arguments)
                        .map(arg -> context.getTypeMirror(arg.getType()))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BSolidReferenceTypeMirror &&
               typesEqual(this.type, ((BSolidReferenceTypeMirror) obj).type);
    }

    private static boolean typesEqual(SignatureAttribute.Type a, SignatureAttribute.Type b) {
        if (a.getClass() != b.getClass()) { return false; }
        if (a.getClass() == SignatureAttribute.BaseType.class) {
            return ((SignatureAttribute.BaseType) a).getDescriptor() ==
                   ((SignatureAttribute.BaseType) b).getDescriptor();
        }
        if (a.getClass() == SignatureAttribute.ArrayType.class) {
            return typesEqual(((SignatureAttribute.ArrayType) a).getComponentType(),
                              ((SignatureAttribute.ArrayType) b).getComponentType());
        }
        if (a.getClass() == SignatureAttribute.NestedClassType.class) {
            return typesEqual(((SignatureAttribute.NestedClassType) a).getDeclaringClass(),
                              ((SignatureAttribute.NestedClassType) b).getDeclaringClass()) &&
                   ((SignatureAttribute.NestedClassType) a).getName()
                           .equals(((SignatureAttribute.NestedClassType) b).getName());
        }
        if (a.getClass() == SignatureAttribute.ClassType.class) {
            return ((SignatureAttribute.ClassType) a).getName()
                    .equals(((SignatureAttribute.ClassType) b).getName());
        }
        return ((SignatureAttribute.TypeVariable) a).getName()
                .equals(((SignatureAttribute.TypeVariable) b).getName());
    }

    @Override
    public int hashCode() {
        return this.type.toString().hashCode();
    }
}
