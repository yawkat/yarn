package at.yawk.yarn.compiler.instruction.setup;

import at.yawk.yarn.compiler.instruction.StatementContext;
import at.yawk.yarn.compiler.instruction.resolver.BeanResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class InjectMethodSetupInstruction implements SetupInstruction {
    private final String methodName;
    private final List<BeanResolver> resolvers;

    @Override
    public void write(StatementContext ctx) {
        ctx.append("$L.$L(", ctx.getInstanceVariable(), methodName);
        for (int i = 0; i < resolvers.size(); i++) {
            if (i != 0) { ctx.append(", "); }
            resolvers.get(i).write(ctx);
        }
        ctx.append(")");
    }
}
