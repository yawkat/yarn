package at.yawk.yarn.compiler.instruction.resolver;

import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.instruction.StatementContext;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class ExactBeanResolver implements BeanResolver {
    private final BeanDefinition definition;

    @Override
    public void write(StatementContext ctx) {
        SingletonBeanResolver.appendBeanField(ctx, definition);
    }
}
