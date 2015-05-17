package at.yawk.yarn.compiler.instruction.setup;

import at.yawk.yarn.compiler.instruction.StatementContext;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class PostConstructSetupInstruction implements SetupInstruction {
    private final String methodName;

    @Override
    public void write(StatementContext ctx) {
        ctx.append("$L.$L()", ctx.getInstanceVariable(), methodName);
    }
}
