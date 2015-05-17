package at.yawk.yarn.compiler.tree;

import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.LookupBeanReference;
import at.yawk.yarn.compiler.Util;
import at.yawk.yarn.compiler.error.AmbiguousConstructorException;
import at.yawk.yarn.compiler.error.ConstructorNotFoundException;
import at.yawk.yarn.compiler.instruction.factory.ConstructorBeanFactory;
import at.yawk.yarn.compiler.instruction.resolver.BeanResolver;
import at.yawk.yarn.compiler.instruction.resolver.BeanResolverFactory;
import at.yawk.yarn.compiler.process.definition.*;
import at.yawk.yarn.compiler.process.reference.LookupBeanReferenceProcessor;
import at.yawk.yarn.compiler.process.reference.NamedAnnotationLookupBeanReferenceProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

/**
 * @author yawkat
 */
public class BeanTreeCompiler implements at.yawk.yarn.compiler.Compiler {
    private final BeanTree tree = new BeanTree();

    public void scanAndAddEntryPoint(TypeElement entryPoint) {
        ExecutableElement noArgsConstructor = null;
        ExecutableElement injectConstructor = null;
        for (Element element : entryPoint.getEnclosedElements()) {
            if (element instanceof ExecutableElement &&
                element.getSimpleName().contentEquals("<init>")) {
                if (element.getAnnotation(Inject.class) != null) {
                    if (injectConstructor != null) {
                        throw new AmbiguousConstructorException(
                                "Found more than one constructor annotated with @Inject on class " +
                                entryPoint.getQualifiedName()
                        );
                    }
                    injectConstructor = (ExecutableElement) element;
                }
                if (((ExecutableElement) element).getParameters().isEmpty()) {
                    assert noArgsConstructor == null : "More than one <init>()V ?";
                    noArgsConstructor = (ExecutableElement) element;
                }
            }
        }

        if (noArgsConstructor == null && injectConstructor == null) {
            throw new ConstructorNotFoundException(
                    "Found no usable constructor on " + entryPoint.getQualifiedName() +
                    ". Either add an empty constructor or annotated one with @Inject."
            );
        }

        ExecutableElement constructorToUse =
                injectConstructor == null ? noArgsConstructor : injectConstructor;

        Util.checkAccess(constructorToUse);

        BeanDefinition definition = new BeanDefinition();
        definition.setAccessType((DeclaredType) entryPoint.asType());
        definition.setImplicitName(Util.decapitalize(entryPoint.getSimpleName().toString()));
        definition.setType(entryPoint.asType());
        definition.setAnnotations(Util.getAnnotations(entryPoint));

        List<BeanResolver> arguments = new ArrayList<>();
        for (VariableElement parameter : constructorToUse.getParameters()) {
            LookupBeanReference reference = new LookupBeanReference();
            reference.setType(parameter.asType());
            reference.setAnnotations(Util.getAnnotations(parameter));
            processLookupBeanReference(reference);

            arguments.add(reference.getResolver());
            definition.getDependencies().add(reference);
        }

        definition.setFactory(new ConstructorBeanFactory(arguments));
        definition.setEntryPoint(true);

        register(definition);
    }

    private void register(BeanDefinition definition) {
        processBeanDefinition(definition);
        tree.getBeanDefinitions().add(definition);

        definition.getProvidingDefinitions().forEach(this::register);
    }

    public BeanTree finishTree() {
        tree.lookupReferences();
        tree.sort();
        tree.assignIds();
        return tree;
    }

    ///// PROCESSORS /////

    private final List<LookupBeanReferenceProcessor> referenceProcessors = Arrays.asList(
            new NamedAnnotationLookupBeanReferenceProcessor()
    );

    @Override
    public void processLookupBeanReference(LookupBeanReference reference) {
        BeanResolverFactory.assignBeanResolver(reference);
        for (LookupBeanReferenceProcessor processor : referenceProcessors) {
            processor.process(this, reference);
        }
    }

    private final List<BeanDefinitionProcessor> definitionProcessors = Arrays.asList(
            new NamedAnnotationBeanDefinitionProcessor(),
            new InjectMembersBeanDefinitionProcessor(),
            new PostConstructDefinitionProcessor(),
            new ProvidesBeanDefinitionProcessor()
    );

    void processBeanDefinition(BeanDefinition definition) {
        for (BeanDefinitionProcessor processor : definitionProcessors) {
            processor.process(this, definition);
        }
    }
}