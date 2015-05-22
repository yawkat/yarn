package at.yawk.yarn.bytecode;

import java.util.List;
import javassist.CtField;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.SignatureAttribute;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
class BFieldElement extends BMemberElement<CtField> implements VariableElement {
    BFieldElement(BytecodeContext context, CtField member) {
        super(context, member);
    }

    @Override
    public Object getConstantValue() {
        return member.getConstantValue();
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitVariable(this, p);
    }

    @Override
    public List<BAnnotationMirror> getAnnotationMirrors() {
        FieldInfo fi = member.getFieldInfo2();
        AnnotationsAttribute inv = (AnnotationsAttribute)
                fi.getAttribute(AnnotationsAttribute.invisibleTag);
        AnnotationsAttribute vis = (AnnotationsAttribute)
                fi.getAttribute(AnnotationsAttribute.visibleTag);
        return annotations(inv, vis);
    }

    @Override
    public String toString() {
        return member.getName();
    }

    @Override
    @SneakyThrows
    public TypeMirror asType() {
        SignatureAttribute attribute =
                (SignatureAttribute) member.getFieldInfo2().getAttribute(SignatureAttribute.tag);
        String signature = attribute == null ?
                member.getFieldInfo().getDescriptor() :
                attribute.getSignature();
        return context.getTypeMirror(SignatureAttribute.toFieldSignature(signature));
    }
}
