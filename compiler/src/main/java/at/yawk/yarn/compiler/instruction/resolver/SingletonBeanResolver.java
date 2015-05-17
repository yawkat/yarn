package at.yawk.yarn.compiler.instruction.resolver;

import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.BeanReference;
import at.yawk.yarn.compiler.error.AmbiguousBeanException;
import at.yawk.yarn.compiler.error.NoSuchBeanException;
import at.yawk.yarn.compiler.instruction.StatementContext;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class SingletonBeanResolver implements BeanResolver {
    private final BeanReference reference;

    @Override
    public void write(StatementContext ctx) {
        List<BeanDefinition> candidates = ctx.getTree().findBeans(reference);
        if (candidates.isEmpty()) {
            // todo: better error message
            throw new NoSuchBeanException("No bean found for reference " + reference);
        }
        if (candidates.size() > 1) {
            throw new AmbiguousBeanException("Too many beans found for reference " + reference);
        }
        BeanDefinition bean = candidates.get(0);
        appendBeanField(ctx, bean);
    }

    static void appendBeanField(StatementContext ctx, BeanDefinition bean) {
        ctx.append("$L._$L", ctx.getYarnVariable(), bean.getId().toString());
    }
}
