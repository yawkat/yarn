package at.yawk.yarn.bytecode;

import java.util.function.Supplier;
import javassist.*;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.NoType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BytecodeContext {
    final ClassPool classPool;

    public static BytecodeContext of(ClassPool classPool) {
        return new BytecodeContext(classPool);
    }

    public TypeElement findType(String name) {
        return findType(name, null);
    }

    TypeElement findType(String name, String scanPackage) {
        SignatureAttribute.ClassType type = parseType(name);
        return findType(type, scanPackage);
    }

    static SignatureAttribute.ClassType parseType(String name) {
        int ld = name.lastIndexOf('.');
        int ls = name.lastIndexOf('$');
        if (ls > ld) {
            return new SignatureAttribute.NestedClassType(
                    parseType(name.substring(0, ls)),
                    name.substring(ls + 1),
                    new SignatureAttribute.TypeArgument[0]
            );
        } else {
            return new SignatureAttribute.ClassType(name);
        }
    }

    public TypeElement findType(SignatureAttribute.Type type) {
        return findType(type, null);
    }

    TypeElement findType(SignatureAttribute.Type type, String scanPackage) {
        CtClass rawType = findRawType(type, scanPackage);

        TypeElement outer = null;
        if (type instanceof SignatureAttribute.ClassType) {
            SignatureAttribute.ClassType declaring = ((SignatureAttribute.ClassType) type).getDeclaringClass();
            if (declaring != null) {
                outer = findType(declaring, scanPackage);
            }
        }
        return new BTypeElement(this, outer, rawType);
    }

    CtClass findRawType(String name) {
        return findRawType(name, null);
    }

    @SneakyThrows
    private CtClass findRawType(String name, String scanPackage) {
        if (scanPackage != null && !name.contains(".")) {
            CtClass item = classPool.getOrNull(scanPackage + '.' + name);
            if (item != null) { return item; }
        }
        return classPool.get(name);
    }

    CtClass findRawType(SignatureAttribute.Type type) {
        return findRawType(type, null);
    }

    CtClass findRawType(SignatureAttribute.Type type, String scanPackage) {
        if (type instanceof SignatureAttribute.ClassType) {
            StringBuilder qnameBuilder = new StringBuilder();
            SignatureAttribute.ClassType ct = (SignatureAttribute.ClassType) type;
            while (ct != null) {
                qnameBuilder.insert(0, ct.getName());
                ct = ct.getDeclaringClass();
                if (ct != null) {
                    qnameBuilder.insert(0, '$');
                }
            }
            return findRawType(qnameBuilder.toString(), scanPackage);
        }

        if (type instanceof SignatureAttribute.BaseType) {
            switch (((SignatureAttribute.BaseType) type).getDescriptor()) {
            case 'Z':
                return CtClass.booleanType;
            case 'B':
                return CtClass.byteType;
            case 'S':
                return CtClass.shortType;
            case 'C':
                return CtClass.charType;
            case 'I':
                return CtClass.intType;
            case 'J':
                return CtClass.longType;
            case 'F':
                return CtClass.floatType;
            case 'D':
                return CtClass.doubleType;
            case 'V':
                return CtClass.voidType;
            default:
                throw new UnsupportedOperationException("Unsupported primitive type " + type);
            }
        }

        // assume array

        CtClass component = findRawType(((SignatureAttribute.ArrayType) type).getComponentType(), scanPackage);
        // don't do this at home
        return findRawType(component.getName() + "[]", scanPackage);
    }

    BSolidReferenceTypeMirror getTypeMirror(SignatureAttribute.Type type) {
        return new BSolidReferenceTypeMirror(this, type);
    }

    TypeParameterElement getTypeParameterElement(Supplier<Element> owner, SignatureAttribute.TypeParameter
            typeParameter) {
        return new BTypeParameterElement(this, owner, typeParameter);
    }

    BAnnotationMirror getAnnotationMirror(Annotation annotation) {
        return new BAnnotationMirror(this, annotation);
    }

    NoType noType() {
        return new BNoType(this);
    }

    BMemberElement<?> getMember(CtMember member) {
        if (member instanceof CtBehavior) {
            return new BExecutableElement(this, (CtBehavior) member);
        } else {
            return new BFieldElement(this, (CtField) member);
        }
    }

    BPackageElement getPackage(String name) {
        return new BPackageElement(this, name);
    }
}
