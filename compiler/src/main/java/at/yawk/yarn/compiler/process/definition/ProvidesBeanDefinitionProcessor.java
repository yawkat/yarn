package at.yawk.yarn.compiler.process.definition;

import at.yawk.yarn.Provides;
import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.ExplicitBeanReference;
import at.yawk.yarn.compiler.Util;
import at.yawk.yarn.compiler.instruction.factory.MethodBeanFactory;
import at.yawk.yarn.compiler.instruction.resolver.SingletonBeanResolver;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;

/**
 * @author yawkat
 */
public class ProvidesBeanDefinitionProcessor implements BeanDefinitionProcessor {
    @Override
    public void process(Compiler compiler, BeanDefinition definition) {
        if (definition.getType().getKind() == TypeKind.DECLARED) {
            for (Element member : ((DeclaredType) definition.getType()).asElement().getEnclosedElements()) {
                if (member.getAnnotation(Provides.class) == null) { continue; }
                if (member.getKind() == ElementKind.METHOD) {
                    ExecutableElement method = (ExecutableElement) member;
                    ExplicitBeanReference reference = new ExplicitBeanReference(definition, false);

                    BeanDefinition provided = new BeanDefinition();
                    provided.setAccessType((DeclaredType) definition.getType());
                    provided.setImplicitName(method.getSimpleName().toString());
                    provided.setType(method.getReturnType());
                    provided.setAnnotations(Util.getAnnotations(method));
                    provided.setFactory(new MethodBeanFactory(
                            new SingletonBeanResolver(reference), member.getSimpleName().toString()));
                    provided.getDependencies().add(reference);

                    definition.getProvidingDefinitions().add(provided);
                } else {
                    throw new UnsupportedOperationException("Cannot provide bean from member " + member);
                }
            }
        }
    }
}
