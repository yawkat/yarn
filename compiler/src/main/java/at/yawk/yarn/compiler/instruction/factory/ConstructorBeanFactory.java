package at.yawk.yarn.compiler.instruction.factory;

import at.yawk.yarn.compiler.instruction.StatementContext;
import at.yawk.yarn.compiler.instruction.resolver.BeanResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class ConstructorBeanFactory implements BeanFactory {
    private final List<BeanResolver> resolvers;

    @Override
    public void write(StatementContext ctx) {
        ctx.append("new $T(", ctx.getInstanceType());
        for (int i = 0; i < resolvers.size(); i++) {
            if (i != 0) { ctx.append(", "); }
            resolvers.get(i).write(ctx);
        }
        ctx.append(")");
    }
}
