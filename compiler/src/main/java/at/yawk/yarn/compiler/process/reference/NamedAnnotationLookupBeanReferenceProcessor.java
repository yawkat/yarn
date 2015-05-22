package at.yawk.yarn.compiler.process.reference;

import at.yawk.yarn.compiler.BeanDefinition;
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
        reference.addFilter(provider -> {
            if (!(provider instanceof BeanDefinition)) { return false; }
            BeanDefinition def = (BeanDefinition) provider;
            return def.getName().isPresent() &&
                   def.getName().get().equals(name);
        });
    }
}
