package at.yawk.yarn.compiler.process.reference;

import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.LookupBeanReference;
import javax.inject.Named;

/**
 * @author yawkat
 */
public class NamedAnnotationLookupBeanReferenceProcessor extends AnnotationLookupBeanReferenceProcessor<Named> {
    @Override
    protected void process(Compiler compiler, LookupBeanReference reference, Named annotation) {
        String name;
        if (annotation.value().isEmpty()) {
            name = reference.getName().get();
        } else {
            name = annotation.value();
        }
        reference.addFilter(definition -> definition.getName().isPresent() &&
                                          definition.getName().get().equals(name));
    }
}
