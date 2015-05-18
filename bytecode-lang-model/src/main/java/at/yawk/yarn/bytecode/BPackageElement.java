package at.yawk.yarn.bytecode;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;

/**
 * @author yawkat
 */
class BPackageElement extends BElement implements PackageElement {
    private final String name;

    BPackageElement(BytecodeContext context, String name) {
        super(context);
        this.name = name;
    }

    @Override
    public TypeMirror asType() {
        throw unsupported();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.PACKAGE;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public Name getSimpleName() {
        return name(name.substring(name.lastIndexOf('.') + 1));
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
        return v.visitPackage(this, p);
    }

    @Override
    public Name getQualifiedName() {
        return name(name);
    }

    @Override
    public boolean isUnnamed() {
        return name.isEmpty();
    }
}
