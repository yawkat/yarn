package at.yawk.yarn.compiler.tree;

import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.BeanId;
import at.yawk.yarn.compiler.EntryPoint;
import at.yawk.yarn.compiler.Util;
import java.util.*;
import lombok.Getter;

/**
 * @author yawkat
 */
public class BeanPool {
    @Getter private final Set<BeanDefinition> beanDefinitions = new HashSet<>();

    @Getter private final Map<EntryPoint, ContextBeanTree> entryPoints = new HashMap<>();

    void assignIds() {
        Map<String, List<BeanDefinition>> beanIds = new HashMap<>();
        for (BeanDefinition bean : beanDefinitions) {
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
