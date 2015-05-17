package at.yawk.yarn.compiler.instruction.resolver;

import at.yawk.yarn.compiler.instruction.StatementContext;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class ProviderBeanResolver implements BeanResolver {
    private final BeanResolver resolver;

    @Override
    public void write(StatementContext ctx) {
        // lazy is just a lambda
        ctx.append("() -> ");
        resolver.write(ctx);
    }
}
