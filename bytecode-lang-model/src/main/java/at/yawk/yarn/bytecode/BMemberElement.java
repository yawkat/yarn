package at.yawk.yarn.bytecode;

import java.util.List;
import java.util.Set;
import javassist.CtBehavior;
import javassist.CtConstructor;
import javassist.CtMember;
import javassist.CtMethod;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

/**
 * @author yawkat
 */
abstract class BMemberElement<M extends CtMember> extends BElement {
    final M member;

    BMemberElement(BytecodeContext context, M member) {
        super(context);
        this.member = member;
    }

    @Override
    public TypeMirror asType() {
        throw unsupported();
    }

    @Override
    public ElementKind getKind() {
        if (member instanceof CtConstructor) { return ElementKind.CONSTRUCTOR; }
        if (member instanceof CtMethod) { return ElementKind.METHOD; }
        return ElementKind.FIELD;
    }

    @Override
    public Set<Modifier> getModifiers() {
        if (member instanceof CtBehavior) {
            return modifiers(member.getModifiers(), Modifier.STRICTFP, Modifier.TRANSIENT, Modifier.VOLATILE);
        } else {
            return modifiers(member.getModifiers(),
                             Modifier.STRICTFP,
                             Modifier.ABSTRACT,
                             Modifier.NATIVE,
                             Modifier.SYNCHRONIZED);
        }
    }

    @Override
    public Name getSimpleName() {
        return name(member.getName());
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        throw unsupported();
    }
}
