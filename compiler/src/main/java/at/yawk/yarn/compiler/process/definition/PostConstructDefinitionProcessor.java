package at.yawk.yarn.compiler.process.definition;

import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.error.UnsupportedMemberException;
import at.yawk.yarn.compiler.instruction.setup.PostConstructSetupInstruction;
import javax.annotation.PostConstruct;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

/**
 * @author yawkat
 */
public class PostConstructDefinitionProcessor implements BeanDefinitionProcessor {
    @Override
    public void process(at.yawk.yarn.compiler.Compiler compiler, BeanDefinition definition) {
        if (!(definition.getType() instanceof DeclaredType)) { return; }
        Element element = ((DeclaredType) definition.getType()).asElement();
        for (Element member : element.getEnclosedElements()) {
            // check for @PostConstruct
            if (member.getAnnotation(PostConstruct.class) == null) { continue; }

            if (member instanceof ExecutableElement) {
                definition.getSetupInstructions().add(new PostConstructSetupInstruction(
                        member.getSimpleName().toString()
                ));
            } else {
                throw new UnsupportedMemberException("Unsupported member to inject " + member);
            }
        }
    }
}
