package at.yawk.yarn.bytecode;

import java.util.Map;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

/**
 * @author yawkat
 */
class BAnnotationMirror extends BEntity implements AnnotationMirror {
    final Annotation annotation;

    BAnnotationMirror(BytecodeContext context, Annotation annotation) {
        super(context);
        this.annotation = annotation;
    }

    @Override
    public DeclaredType getAnnotationType() {
        return context.getTypeMirror(new SignatureAttribute.ClassType(annotation.getTypeName()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
        throw unsupported();
    }

    @Override
    public String toString() {
        return annotation.toString();
    }
}
