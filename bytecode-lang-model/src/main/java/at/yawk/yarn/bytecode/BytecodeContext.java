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
        return findType(new SignatureAttribute.ClassType(name));
    }

    public TypeElement findType(SignatureAttribute.Type type) {
        CtClass rawType = findRawType(type);
        TypeElement outer = null;
        if (type instanceof SignatureAttribute.ClassType) {
            String name = ((SignatureAttribute.ClassType) type).getName();
            int packageEnd = name.lastIndexOf('.');
            int outerEnd = name.lastIndexOf('$');
            if (outerEnd != -1 && outerEnd > packageEnd) {
                outer = findType(name.substring(0, outerEnd));
            }
        }
        return new BTypeElement(this, outer, rawType);
    }

    @SneakyThrows
    CtClass findRawType(String name) {
        return classPool.get(name);
    }

    @SneakyThrows
    CtClass findRawType(SignatureAttribute.Type type) {
        if (type instanceof SignatureAttribute.ClassType) {
            return findRawType(((SignatureAttribute.ClassType) type).getName());
        }
        // assume array

        CtClass component = findRawType(((SignatureAttribute.ArrayType) type).getComponentType());
        // don't do this at home
        return classPool.get(component.getName() + "[]");
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
