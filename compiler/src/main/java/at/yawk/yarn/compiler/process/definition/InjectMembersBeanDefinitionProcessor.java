package at.yawk.yarn.compiler.process.definition;

import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.LookupBeanReference;
import at.yawk.yarn.compiler.Util;
import at.yawk.yarn.compiler.error.StaticMemberException;
import at.yawk.yarn.compiler.error.UnsupportedMemberException;
import at.yawk.yarn.compiler.instruction.resolver.BeanResolver;
import at.yawk.yarn.compiler.instruction.setup.InjectFieldSetupInstruction;
import at.yawk.yarn.compiler.instruction.setup.InjectMethodSetupInstruction;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

/**
 * @author yawkat
 */
public class InjectMembersBeanDefinitionProcessor implements BeanDefinitionProcessor {
    @Override
    public void process(Compiler compiler, BeanDefinition definition) {
        if (!(definition.getType() instanceof DeclaredType)) { return; }
        Element element = ((DeclaredType) definition.getType()).asElement();
        for (Element member : element.getEnclosedElements()) {
            // check for @Inject
            if (member.getAnnotation(Inject.class) == null) { continue; }
            if (member.getModifiers().contains(Modifier.STATIC)) {
                throw new StaticMemberException("Cannot inject static member " + member);
            }
            if (member instanceof VariableElement) {
                LookupBeanReference reference = new LookupBeanReference();
                reference.setType(member.asType());
                reference.setAnnotations(Util.getAnnotations(element));
                compiler.processLookupBeanReference(reference);

                definition.getDependencies().add(reference);

                definition.getSetupInstructions().add(new InjectFieldSetupInstruction(
                        member.getSimpleName().toString(),
                        reference.getResolver()
                ));

            } else if (member instanceof ExecutableElement) {
                List<BeanResolver> resolvers = new ArrayList<>();
                for (VariableElement parameter : ((ExecutableElement) member).getParameters()) {
                    LookupBeanReference reference = new LookupBeanReference();
                    reference.setType(parameter.asType());
                    reference.setAnnotations(Util.getAnnotations(element));

                    compiler.processLookupBeanReference(reference);

                    resolvers.add(reference.getResolver());
                    definition.getDependencies().add(reference);
                }

                definition.getSetupInstructions().add(new InjectMethodSetupInstruction(
                        member.getSimpleName().toString(),
                        resolvers
                ));

            } else {
                throw new UnsupportedMemberException("Unsupported member to inject " + member);
            }
        }
    }
}
