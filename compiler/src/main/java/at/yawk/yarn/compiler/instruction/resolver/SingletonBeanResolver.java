package at.yawk.yarn.compiler.instruction.resolver;

import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.BeanMethod;
import at.yawk.yarn.compiler.BeanProvider;
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
        List<BeanProvider> candidates = ctx.getTree().findBeans(reference);
        if (candidates.isEmpty()) {
            // todo: better error message
            throw new NoSuchBeanException("No bean found for reference " + reference);
        }
        if (candidates.size() > 1) {
            throw new AmbiguousBeanException("Too many beans found for reference " + reference);
        }
        BeanProvider bean = candidates.get(0);
        appendBeanField(ctx, bean);
    }

    static void appendBeanField(StatementContext ctx, BeanProvider bean) {
        if (bean instanceof BeanDefinition) {
            ctx.append("$L._$L", ctx.getYarnVariable(), ((BeanDefinition) bean).getId().toString());
        } else if (bean instanceof BeanMethod) {
            appendBeanField(ctx, ((BeanMethod) bean).getOwner());
            ctx.append("::$L", ((BeanMethod) bean).getName());
        } else {
            throw new AssertionError("Unsupported bean provider " + bean.getClass().getName());
        }
    }
}
