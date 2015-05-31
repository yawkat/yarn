package at.yawk.yarn.compiler;

import java.lang.annotation.Annotation;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class MissingAnnotation implements Annotation {
    private final AnnotationMirror mirror;

    @Override
    public Class<? extends Annotation> annotationType() {
        throw new TypeNotPresentException(
                ((TypeElement) mirror.getAnnotationType().asElement()).getQualifiedName().toString(), null);
    }
}
