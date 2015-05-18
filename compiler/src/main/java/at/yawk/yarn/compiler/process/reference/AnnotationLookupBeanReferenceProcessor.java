package at.yawk.yarn.compiler.process.reference;

import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.LookupBeanReference;
import at.yawk.yarn.compiler.Util;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
public abstract class AnnotationLookupBeanReferenceProcessor<A extends Annotation> implements
        LookupBeanReferenceProcessor {
    private final Class<A> type;
    private final Class<?> repeatableType;

    @SuppressWarnings("unchecked")
    public AnnotationLookupBeanReferenceProcessor() {
        this.type = (Class<A>) Util.getGenericType(AnnotationLookupBeanReferenceProcessor.class, getClass(), 0);
        Repeatable repeatable = type.getAnnotation(Repeatable.class);
        repeatableType = repeatable == null ? null : repeatable.value();
    }

    @SuppressWarnings("unchecked")
    @Override
    @SneakyThrows
    public void process(Compiler compiler, LookupBeanReference reference) {
        for (Annotation annotation : reference.getAnnotations()) {
            if (type.isInstance(annotation)) {
                process(compiler, reference, (A) annotation);
            } else if (repeatableType != null && repeatableType.isInstance(annotation)) {
                for (A repeated : (A[]) repeatableType.getMethod("value").invoke(annotation)) {
                    process(compiler, reference, repeated);
                }
            }
        }
    }

    protected abstract void process(Compiler compiler, LookupBeanReference reference, A annotation);
}
