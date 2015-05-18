package at.yawk.yarn.compiler.instruction.resolver;

import at.yawk.yarn.compiler.LookupBeanReference;
import javax.inject.Provider;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * todo: move this to compiler?
 *
 * @author yawkat
 */
public class BeanResolverFactory {
    private BeanResolverFactory() {}

    public static void assignBeanResolver(LookupBeanReference reference) {
        TypeMirror type = reference.getType();
        if (type.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) type;
            TypeElement typeElement = (TypeElement) declaredType.asElement();

            try {
                Class<?> c = Class.forName(typeElement.getQualifiedName().toString());

                // Provider
                if (c == Provider.class) {
                    reference.setType(declaredType.getTypeArguments().get(0));
                    assignBeanResolver(reference);
                    reference.setSoft(true);
                    reference.setResolver(new ProviderBeanResolver(reference.getResolver()));
                    return;
                }

                // Collections
                if (CollectionBeanResolver.SUPPORTED.contains(c)) {
                    reference.setType(declaredType.getTypeArguments().get(0));
                    reference.setResolver(new CollectionBeanResolver(c, reference));
                    return;
                }
            } catch (ClassNotFoundException ignored) {}
        }

        reference.setResolver(new SingletonBeanResolver(reference));
    }
}
