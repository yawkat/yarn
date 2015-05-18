package at.yawk.yarn.bytecode;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import javax.lang.model.AnnotatedConstruct;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
abstract class BAnnotatedConstruct extends BEntity implements AnnotatedConstruct {
    BAnnotatedConstruct(BytecodeContext context) {
        super(context);
    }

    @Override
    public abstract List<BAnnotationMirror> getAnnotationMirrors();

    @SuppressWarnings("unchecked")
    @Override
    @SneakyThrows
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        for (BAnnotationMirror mirror : getAnnotationMirrors()) {
            if (mirror.annotation.getTypeName().equals(annotationType.getName())) {
                return BAnnotation.map(mirror.annotation, annotationType);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    @SneakyThrows
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        A single = getAnnotation(annotationType);
        if (single != null) {
            A[] array = (A[]) Array.newInstance(annotationType, 1);
            array[0] = single;
            return array;
        }
        Repeatable repeatable = annotationType.getAnnotation(Repeatable.class);
        if (repeatable != null) {
            Class<? extends Annotation> repeating = repeatable.value();
            Annotation repeatingFound = getAnnotation(repeating);
            if (repeatingFound != null) {
                Method valueMethod = repeating.getDeclaredMethod("value");
                valueMethod.setAccessible(true);
                return (A[]) valueMethod.invoke(repeatingFound);
            }
        }
        return (A[]) Array.newInstance(annotationType, 0);
    }
}
