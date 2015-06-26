package at.yawk.yarn.bytecode;

import java.util.*;
import java.util.stream.Collectors;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
@EqualsAndHashCode(of = "clazz", callSuper = false)
class BTypeElement extends BElement implements TypeElement {
    final TypeElement outer;
    final CtClass clazz;

    BTypeElement(BytecodeContext context, TypeElement outer, CtClass clazz) {
        super(context);
        this.outer = outer;
        this.clazz = clazz;
    }

    @Override
    public NestingKind getNestingKind() {
        if (outer != null) {
            return NestingKind.MEMBER;
        }
        return NestingKind.TOP_LEVEL;
    }

    @Override
    public Element getEnclosingElement() {
        if (outer != null) { return outer; }
        return context.getPackage(clazz.getPackageName());
    }

    @Override
    public Name getQualifiedName() {
        return name(clazz.getName());
    }

    private SignatureAttribute signatureAttribute() {
        return (SignatureAttribute) clazz.getClassFile2().getAttribute(SignatureAttribute.tag);
    }

    @Override
    @SneakyThrows
    public TypeMirror getSuperclass() {
        SignatureAttribute attribute = signatureAttribute();
        if (attribute == null) {
            String superclass = clazz.getClassFile2().getSuperclass();
            return superclass == null ? null : context.getTypeMirror(BytecodeContext.parseType(superclass));
        }
        SignatureAttribute.ClassSignature classSignature =
                SignatureAttribute.toClassSignature(attribute.getSignature());
        SignatureAttribute.ClassType superClass = classSignature.getSuperClass();
        return superClass == null ? null : context.getTypeMirror(superClass);
    }

    @Override
    @SneakyThrows
    public List<? extends TypeMirror> getInterfaces() {
        SignatureAttribute attribute = signatureAttribute();
        if (attribute == null) {
            String[] interfaces = clazz.getClassFile2().getInterfaces();
            return Arrays.stream(interfaces)
                    .map(itf -> context.getTypeMirror(BytecodeContext.parseType(itf)))
                    .collect(Collectors.toList());
        }
        SignatureAttribute.ClassType[] interfaces =
                SignatureAttribute.toClassSignature(attribute.getSignature()).getInterfaces();
        return Arrays.stream(interfaces)
                .map(context::getTypeMirror)
                .collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public List<? extends TypeParameterElement> getTypeParameters() {
        SignatureAttribute attribute = signatureAttribute();
        if (attribute == null) {
            return Collections.emptyList();
        }
        SignatureAttribute.TypeParameter[] typeParameters =
                SignatureAttribute.toClassSignature(attribute.getSignature()).getParameters();
        return Arrays.stream(typeParameters)
                .map(p -> context.getTypeParameterElement(() -> this, p))
                .collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public TypeMirror asType() {
        SignatureAttribute attribute = signatureAttribute();
        SignatureAttribute.ClassType base = BytecodeContext.parseType(clazz.getName());
        if (attribute == null) {
            return context.getTypeMirror(base);
        }
        SignatureAttribute.ClassSignature cs =
                SignatureAttribute.toClassSignature(attribute.getSignature());
        if (base instanceof SignatureAttribute.NestedClassType) {
            return context.getTypeMirror(new SignatureAttribute.NestedClassType(
                    base.getDeclaringClass(),
                    base.getName(),
                    Arrays.stream(cs.getParameters())
                            .map(p -> new SignatureAttribute.TypeArgument(p.getClassBound()))
                            .toArray(SignatureAttribute.TypeArgument[]::new)
            ));
        } else {
            return context.getTypeMirror(new SignatureAttribute.ClassType(
                    base.getName(),
                    Arrays.stream(cs.getParameters())
                            .map(p -> new SignatureAttribute.TypeArgument(p.getClassBound()))
                            .toArray(SignatureAttribute.TypeArgument[]::new)
            ));
        }
    }

    @Override
    public ElementKind getKind() {
        if (clazz.isEnum()) { return ElementKind.ENUM; }
        if (clazz.isAnnotation()) { return ElementKind.ANNOTATION_TYPE; }
        if (clazz.isInterface()) { return ElementKind.INTERFACE; }
        return ElementKind.CLASS;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return modifiers(clazz.getModifiers(),
                         Modifier.VOLATILE,
                         Modifier.TRANSIENT,
                         Modifier.SYNCHRONIZED,
                         Modifier.NATIVE);
    }

    @Override
    public Name getSimpleName() {
        String simpleName = clazz.getSimpleName();
        return name(simpleName.substring(simpleName.lastIndexOf('$') + 1));
    }

    @Override
    @SneakyThrows
    public List<? extends Element> getEnclosedElements() {
        List<Element> members = new ArrayList<>();
        for (CtField field : clazz.getDeclaredFields()) {
            if (field.getAttribute(SyntheticAttribute.tag) == null) {
                members.add(context.getMember(field));
            }
        }
        for (CtMethod method : clazz.getDeclaredMethods()) {
            if (method.getAttribute(SyntheticAttribute.tag) == null) {
                members.add(context.getMember(method));
            }
        }
        InnerClassesAttribute attribute =
                (InnerClassesAttribute) clazz.getClassFile2().getAttribute(InnerClassesAttribute.tag);
        if (attribute != null) {
            for (int i = 0; i < attribute.tableLength(); i++) {
                String name = attribute.innerName(i);
                if (name == null) { continue; } // anonymous class
                String outer = attribute.outerClass(i);
                if (!clazz.getName().equals(outer)) { continue; } // further nested class
                CtClass raw = context.findRawType(clazz.getName() + '$' + name);
                members.add(new BTypeElement(context, this, raw));
            }
        }
        return members;
    }

    @Override
    public List<BAnnotationMirror> getAnnotationMirrors() {
        ClassFile cf = clazz.getClassFile2();
        AnnotationsAttribute inv = (AnnotationsAttribute)
                cf.getAttribute(AnnotationsAttribute.invisibleTag);
        AnnotationsAttribute vis = (AnnotationsAttribute)
                cf.getAttribute(AnnotationsAttribute.visibleTag);
        return annotations(inv, vis);
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitType(this, p);
    }

    @Override
    public String toString() {
        return clazz.getName();
    }
}
