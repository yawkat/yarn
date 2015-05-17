package at.yawk.yarn.compiler.instruction.resolver;

import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.BeanReference;
import at.yawk.yarn.compiler.instruction.StatementContext;
import java.util.*;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class CollectionBeanResolver implements BeanResolver {
    static final Set<Class<?>> SUPPORTED = new HashSet<>(Arrays.asList(
            Iterable.class, List.class, Collection.class, Set.class));

    private final Class<?> type;
    private final BeanReference reference;

    @Override
    public void write(StatementContext ctx) {
        String suffix;
        if (type.isAssignableFrom(List.class)) {
            ctx.append("$T.asList(", Arrays.class);
            suffix = ")";
        } else {
            assert type == Set.class;
            ctx.append("new $T($T.asList(", HashSet.class, Arrays.class);
            suffix = "))";
        }

        List<BeanDefinition> candidates = ctx.getTree().findBeans(reference);
        for (int i = 0; i < candidates.size(); i++) {
            if (i != 0) { ctx.append(", "); }
            SingletonBeanResolver.appendBeanField(ctx, candidates.get(i));
        }
        ctx.append(suffix);
    }
}
