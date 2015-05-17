package at.yawk.yarn.compiler.process.definition;

import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.Compiler;
import java.util.Optional;
import javax.inject.Named;

/**
 * @author yawkat
 */
public class NamedAnnotationBeanDefinitionProcessor extends AnnotationBeanDefinitionProcessor<Named> {
    @Override
    protected void process(Compiler compiler, BeanDefinition definition, Named annotation) {
        if (annotation.value().isEmpty()) {
            definition.setName(Optional.of(definition.getImplicitName()));
        } else {
            definition.setName(Optional.of(annotation.value()));
        }
    }
}
