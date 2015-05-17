package at.yawk.yarn.compiler.process.definition;

import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.Util;
import java.lang.annotation.Annotation;

/**
 * @author yawkat
 */
public abstract class AnnotationBeanDefinitionProcessor<A extends Annotation> implements BeanDefinitionProcessor {
    private final Class<A> type;

    public AnnotationBeanDefinitionProcessor(Class<A> type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public AnnotationBeanDefinitionProcessor() {
        this.type = (Class<A>) Util.getGenericType(AnnotationBeanDefinitionProcessor.class, getClass(), 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(Compiler compiler, BeanDefinition definition) {
        definition.getAnnotations()
                .stream()
                .filter(type::isInstance)
                .forEach(annotation -> process(compiler, definition, (A) annotation));
    }

    protected abstract void process(Compiler compiler, BeanDefinition definition, A annotation);
}
