package at.yawk.yarn.compiler.instruction;

import at.yawk.yarn.compiler.tree.BeanTree;
import javax.lang.model.type.TypeMirror;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class StatementContext {
    private final StatementBuilder builder;
    @Getter private final BeanTree tree;
    @Getter private final String yarnVariable;
    @Getter private final TypeMirror instanceType;
    @Getter private final String instanceVariable;

    public StatementContext append(String format) {
        builder.append(format);
        return this;
    }

    public StatementContext append(String format, Object... args) {
        builder.append(format, args);
        return this;
    }
}
