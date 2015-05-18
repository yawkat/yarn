package at.yawk.yarn.bytecode;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;

/**
 * @author yawkat
 */
class BParameterElement extends BElement implements VariableElement {
    private final SignatureAttribute.Type type;
    private final Annotation[] visible;
    private final Annotation[] invisible;
    private final Supplier<String> name;

    BParameterElement(BytecodeContext context, SignatureAttribute.Type type, Annotation[] visible,
                      Annotation[] invisible, Supplier<String> name) {
        super(context);
        this.type = type;
        this.visible = visible;
        this.invisible = invisible;
        this.name = name;
    }

    @Override
    public TypeMirror asType() {
        return context.getTypeMirror(type);
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.PARAMETER;
    }

    @Override
    public Set<Modifier> getModifiers() {
        throw unsupported();
    }

    @Override
    public Name getSimpleName() {
        return name(name.get());
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        throw unsupported();
    }

    @Override
    public List<BAnnotationMirror> getAnnotationMirrors() {
        return Stream.concat(Stream.of(invisible), Stream.of(visible))
                .map(context::getAnnotationMirror)
                .collect(Collectors.toList());
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitVariable(this, p);
    }

    @Override
    public Object getConstantValue() {
        throw unsupported();
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
