package at.yawk.yarn.compiler.instruction.factory;

import at.yawk.yarn.compiler.instruction.StatementContext;
import at.yawk.yarn.compiler.instruction.resolver.BeanResolver;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class MethodBeanFactory implements BeanFactory {
    private final BeanResolver resolver;
    private final String methodName;

    @Override
    public void write(StatementContext ctx) {
        resolver.write(ctx);
        ctx.append(".$L()", methodName);
    }
}
