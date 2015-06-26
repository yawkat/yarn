package at.yawk.yarn.compiler.process.reference;

import at.yawk.yarn.compiler.*;
import at.yawk.yarn.compiler.Compiler;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author yawkat
 */
public class TypeFilterProcessor implements LookupBeanReferenceProcessor {
    @Override
    public void process(Compiler compiler, LookupBeanReference reference) {
        TypeMirror type = reference.getType();
        ExecutableElement functional;
        if (type.getKind() == TypeKind.DECLARED) {
            Optional<ExecutableElement> found =
                    findFunctionalInterfaceMethod((TypeElement) ((DeclaredType) type).asElement());
            functional = found == null ? null : found.orElse(null);
        } else {
            functional = null;
        }
        reference.addFilter(provider -> {
            if (provider instanceof BeanDefinition) {
                return Util.inherits(type, ((BeanDefinition) provider).getType());
            }
            if (provider instanceof BeanMethod) {
                if (functional != null) {
                    BeanMethod method = (BeanMethod) provider;
                    List<TypeMirror> present = method.getParameterTypes();
                    List<? extends VariableElement> expected = functional.getParameters();
                    if (Util.inherits(functional.getReturnType(), method.getReturnType()) &&
                        present.size() == expected.size()) {
                        for (int i = 0; i < present.size(); i++) {
                            if (!Util.inherits(present.get(i), expected.get(i).asType())) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
            return false;
        });
    }

    /**
     * @return filled: only abstract interface method <br>
     * empty: no abstract interface method found <br>
     * null: too many interface methods found or not an interface
     */
    private static Optional<ExecutableElement> findFunctionalInterfaceMethod(TypeElement element) {
        if (element.getKind() != ElementKind.INTERFACE) { return null; }
        ExecutableElement match = null;
        for (Element enclosed : Util.getEnclosedElementsWithParents(element)) {
            if (enclosed.getKind() != ElementKind.METHOD) { continue; }
            Set<Modifier> modifiers = enclosed.getModifiers();
            if (!modifiers.contains(Modifier.ABSTRACT)) { continue; }
            if (match != null) {
                // too many found
                return null;
            }
            match = (ExecutableElement) enclosed;
        }
        for (TypeMirror itf : element.getInterfaces()) {
            Optional<ExecutableElement> superMatch = findFunctionalInterfaceMethod(
                    (TypeElement) ((DeclaredType) itf).asElement());
            if (superMatch == null) {
                // fail-fast in super
                return null;
            }
            if (superMatch.isPresent()) {
                // multiple matches, not functional
                // todo: overridden now-default methods?
                if (match != null) { return null; }

                match = superMatch.get();
            }
        }
        return Optional.ofNullable(match);
    }
}
