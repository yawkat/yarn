package at.yawk.yarn.compiler.tree;

import at.yawk.yarn.compiler.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class ContextBeanTree {
    @Setter(AccessLevel.PACKAGE)
    private Set<BeanDefinition> includedBeans;
    @Getter private List<BeanDefinition> sortedBeans;

    public List<BeanProvider> findBeans(BeanReference reference) {
        if (reference instanceof ExplicitBeanReference) {
            return Collections.singletonList(((ExplicitBeanReference) reference).getBean());
        } else if (reference instanceof LookupBeanReference) {
            return findBeans((LookupBeanReference) reference);
        } else {
            throw new AssertionError("Unsupported bean reference type " + reference.getClass());
        }
    }

    @SuppressWarnings("Convert2streamapi")
    public List<BeanProvider> findBeans(LookupBeanReference reference) {
        List<BeanProvider> result = new ArrayList<>();

        for (BeanDefinition bean : includedBeans) {
            if (matchesFilters(reference, bean)) {
                result.add(bean);
            }
            for (BeanMethod beanMethod : bean.getMethods()) {
                if (matchesFilters(reference, beanMethod)) {
                    result.add(beanMethod);
                }
            }
        }

        return result;
    }

    private static boolean matchesFilters(LookupBeanReference reference, BeanProvider bean) {
        for (BeanFilter filter : reference.getFilters()) {
            if (!filter.accept(bean)) {
                return false;
            }
        }
        return true;
    }

    void lookupReferences() {
        for (BeanDefinition bean : includedBeans) {
            Set<BeanReference> references = new HashSet<>();
            for (BeanReference dependency : bean.getDependencies()) {
                if (dependency instanceof ExplicitBeanReference) {
                    references.add(dependency);
                } else if (dependency instanceof LookupBeanReference) {
                    // lookup references and replace with explicit ones
                    references.addAll(
                            findBeans((LookupBeanReference) dependency).stream()
                                    .map(match -> new ExplicitBeanReference(
                                            match.getBaseDependency(), dependency.isSoft()))
                                    .collect(Collectors.toList())
                    );
                } else {
                    throw new AssertionError("Unsupported bean reference type " + dependency.getClass());
                }
            }
            bean.setDependencies(references);
        }
    }

    /**
     * Build the sortedBeans list. The algorithm tries to load dependencies before the dependents on a best-effort
     * basis. If there is a circular dependency that cannot be avoided by disregarding a soft dependency, a
     * CircularDependencyException is thrown. If a soft dependency is present which can be removed to fix the
     * dependency graph it will be ignored.
     */
    @SuppressWarnings("Convert2MethodRef")
    void sort() {
        sortedBeans = DependencySorter.sort(
                includedBeans,
                bd -> bd.getDependencies().stream()
                        .filter(d -> !d.isSoft())
                        .map(r -> ((ExplicitBeanReference) r).getBean()),
                bd -> bd.getDependencies().stream()
                        .filter(d -> d.isSoft())
                        .map(r -> ((ExplicitBeanReference) r).getBean())
        );
    }
}
