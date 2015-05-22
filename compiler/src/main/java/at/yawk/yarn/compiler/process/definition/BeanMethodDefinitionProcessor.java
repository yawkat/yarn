package at.yawk.yarn.compiler.process.definition;

import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.BeanMethod;
import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.Util;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author yawkat
 */
public class BeanMethodDefinitionProcessor implements BeanDefinitionProcessor {
    @Override
    public void process(Compiler compiler, BeanDefinition definition) {
        TypeMirror type = definition.getType();
        if (type.getKind() != TypeKind.DECLARED) { return; }
        TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
        process(definition, element);
    }

    private void process(BeanDefinition definition, TypeElement element) {
        for (Element member : element.getEnclosedElements()) {
            if (member.getKind() != ElementKind.METHOD) { continue; }

            ExecutableElement method = (ExecutableElement) member;
            if (method.getSimpleName().contentEquals("<init>")) { continue; }

            Set<Modifier> modifiers = member.getModifiers();
            if (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.PRIVATE)) { continue; }

            BeanMethod bm = new BeanMethod();
            bm.setOwner(definition);
            bm.setName(method.getSimpleName().toString());
            bm.setReturnType(method.getReturnType());
            bm.setParameterTypes(
                    method.getParameters().stream()
                            .map(Element::asType)
                            .collect(Collectors.toList())
            );
            bm.setAnnotations(Util.getAnnotations(method));
            definition.getMethods().add(bm);
        }
    }
}
