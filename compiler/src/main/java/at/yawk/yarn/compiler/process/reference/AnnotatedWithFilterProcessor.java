package at.yawk.yarn.compiler.process.reference;

import at.yawk.yarn.AnnotatedWith;
import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.LookupBeanReference;
import java.lang.annotation.Annotation;

/**
 * @author yawkat
 */
public class AnnotatedWithFilterProcessor extends AnnotationLookupBeanReferenceProcessor<AnnotatedWith> {
    @Override
    protected void process(Compiler compiler, LookupBeanReference reference, AnnotatedWith annotation) {
        String qname = getQName(annotation);
        reference.addFilter(definition -> {
            for (Annotation a : definition.getAnnotations()) {
                try {
                    if (a.annotationType().getName().equals(qname)) {
                        return true;
                    }
                } catch (TypeNotPresentException e) {
                    if (e.typeName().equals(qname)) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private String getQName(AnnotatedWith annotation) {
        try {
            return annotation.value().getName();
        } catch (TypeNotPresentException e) {
            return e.typeName();
        }
    }
}
