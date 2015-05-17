package at.yawk.yarn.compiler.tree;

import at.yawk.yarn.compiler.*;
import at.yawk.yarn.compiler.error.CircularDependencyException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * @author yawkat
 */
public class BeanTree {
    @Getter(AccessLevel.PACKAGE)
    private final Set<BeanDefinition> beanDefinitions = new HashSet<>();
    @Getter
    private List<BeanDefinition> sortedBeans;

    public List<BeanDefinition> findBeans(BeanReference reference) {
        if (reference instanceof ExplicitBeanReference) {
            return Collections.singletonList(((ExplicitBeanReference) reference).getBean());
        } else if (reference instanceof LookupBeanReference) {
            return findBeans((LookupBeanReference) reference);
        } else {
            throw new AssertionError("Unsupported bean reference type " + reference.getClass());
        }
    }

    public List<BeanDefinition> findBeans(LookupBeanReference reference) {
        List<BeanDefinition> result = new ArrayList<>();

        outer:
        for (BeanDefinition bean : beanDefinitions) {
            if (!Util.inherits(reference.getType(), bean.getType())) {
                continue;
            }
            // check all criteria
            for (BeanFilter filter : reference.getFilters()) {
                if (!filter.accept(bean)) {
                    continue outer;
                }
            }
            result.add(bean);
        }

        return result;
    }

    void lookupReferences() {
        for (BeanDefinition bean : beanDefinitions) {
            Set<BeanReference> references = new HashSet<>();
            for (BeanReference dependency : bean.getDependencies()) {
                System.out.println("Hardening " + dependency + " on " + bean);
                if (dependency instanceof ExplicitBeanReference) {
                    references.add(dependency);
                } else if (dependency instanceof LookupBeanReference) {
                    // lookup references and replace with explicit ones
                    references.addAll(
                            findBeans((LookupBeanReference) dependency).stream()
                                    .map(match -> new ExplicitBeanReference(match, dependency.isSoft()))
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
                beanDefinitions,
                bd -> bd.getDependencies().stream()
                        .filter(d -> !d.isSoft())
                        .map(r -> ((ExplicitBeanReference) r).getBean()),
                bd -> bd.getDependencies().stream()
                        .filter(d -> d.isSoft())
                        .map(r -> ((ExplicitBeanReference) r).getBean())
        );

        /*
        Set<BeanDefinition> walked = new HashSet<>();
        //noinspection Convert2streamapi
        for (BeanDefinition bean : beanDefinitions) {
            sortFrom(walked, bean, new ArrayDeque<>());
        }
        */
    }

    private void sortFrom(Set<BeanDefinition> walked, BeanDefinition definition,
                          Deque<BeanDefinition> strongPath) {
        if (strongPath != null) {
            if (strongPath.contains(definition)) {
                StringBuilder builder = new StringBuilder();
                for (BeanDefinition item : strongPath) {
                    if (item.equals(definition)) { break; }
                    builder.append(item).append(" --> ");
                }
                builder.append(definition);
                throw new CircularDependencyException("Circular dependency: " + builder);
            } else {
                strongPath.addFirst(definition);
            }
        }
        // check if this bean was already walked
        if (!walked.add(definition)) {
            if (!sortedBeans.contains(definition)) {
                // circular dependency!
                if (strongPath == null) {
                    // soft
                    sortedBeans.add(definition);
                }
            }
            return;
        }

        // first walk strong, then soft dependencies
        for (boolean soft : new boolean[]{ false, true }) {
            definition.getDependencies()
                    .stream()
                    .filter(dependency -> dependency.isSoft() == soft)
                    .forEach(dependency -> {
                        assert dependency instanceof ExplicitBeanReference : "Called lookup?";
                        System.out.println("Walking into dep " + dependency + " of " + definition);
                        sortFrom(walked, ((ExplicitBeanReference) dependency).getBean(), soft ? null : strongPath);
                    });
        }
        sortedBeans.add(definition);

        if (strongPath != null) {
            strongPath.removeFirst();
        }
    }

    void assignIds() {
        Map<String, List<BeanDefinition>> beanIds = new HashMap<>();
        for (BeanDefinition bean : sortedBeans) {
            String preferredId = bean.getName().orElse(bean.getImplicitName());
            preferredId = Util.sanitizeJavaName(preferredId);
            List<BeanDefinition> beansWithSameId = beanIds.computeIfAbsent(preferredId, s -> new ArrayList<>());

            BeanId id;
            if (beansWithSameId.isEmpty()) {
                id = new BeanId(preferredId, false, 0);
            } else {
                if (beansWithSameId.size() == 1) {
                    beansWithSameId.get(0).setId(new BeanId(preferredId, false, 0));
                }
                id = new BeanId(preferredId, true, beansWithSameId.size());
            }
            bean.setId(id);
            beansWithSameId.add(bean);
        }
    }
}
