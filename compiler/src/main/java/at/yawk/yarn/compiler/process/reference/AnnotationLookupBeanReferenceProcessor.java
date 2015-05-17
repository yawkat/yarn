package at.yawk.yarn.compiler.process.reference;

import at.yawk.yarn.compiler.LookupBeanReference;
import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.Util;
import java.lang.annotation.Annotation;

/**
 * @author yawkat
 */
public abstract class AnnotationLookupBeanReferenceProcessor<A extends Annotation> implements
        LookupBeanReferenceProcessor {
    private final Class<A> type;

    public AnnotationLookupBeanReferenceProcessor(Class<A> type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public AnnotationLookupBeanReferenceProcessor() {
        this.type = (Class<A>) Util.getGenericType(AnnotationLookupBeanReferenceProcessor.class, getClass(), 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(Compiler compiler, LookupBeanReference reference) {
        reference.getAnnotations()
                .stream()
                .filter(type::isInstance)
                .forEach(annotation -> process(compiler, reference, (A) annotation));
    }

    protected abstract void process(Compiler compiler, LookupBeanReference reference, A annotation);
}
