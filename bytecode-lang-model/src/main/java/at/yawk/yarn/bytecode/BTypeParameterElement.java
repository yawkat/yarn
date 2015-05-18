package at.yawk.yarn.bytecode;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javassist.bytecode.SignatureAttribute;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;

/**
 * @author yawkat
 */
class BTypeParameterElement extends BElement implements TypeParameterElement {
    private final Supplier<Element> owner;
    private final SignatureAttribute.TypeParameter typeParameter;

    BTypeParameterElement(BytecodeContext context, Supplier<Element> owner,
                          SignatureAttribute.TypeParameter typeParameter) {
        super(context);
        this.owner = owner;
        this.typeParameter = typeParameter;
    }

    @Override
    public Element getGenericElement() {
        return owner.get();
    }

    @Override
    public List<? extends TypeMirror> getBounds() {
        return Stream.concat(
                Stream.of(typeParameter.getClassBound()),
                Stream.of(typeParameter.getClassBound())
        ).map(context::getTypeMirror).collect(Collectors.toList());
    }

    @Override
    public TypeMirror asType() {
        throw unsupported();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.TYPE_PARAMETER;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public Name getSimpleName() {
        return name(typeParameter.getName());
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        throw unsupported();
    }

    @Override
    public List<BAnnotationMirror> getAnnotationMirrors() {
        return Collections.emptyList();
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitTypeParameter(this, p);
    }

    @Override
    public String toString() {
        return typeParameter.toString();
    }
}
