package at.yawk.yarn.compiler.instruction.setup;

import at.yawk.yarn.compiler.instruction.StatementContext;
import at.yawk.yarn.compiler.instruction.resolver.BeanResolver;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class InjectFieldSetupInstruction implements SetupInstruction {
    private final String fieldName;
    private final BeanResolver resolver;

    @Override
    public void write(StatementContext ctx) {
        ctx.append("$L.$L = ", ctx.getInstanceVariable(), fieldName);
        resolver.write(ctx);
    }
}
