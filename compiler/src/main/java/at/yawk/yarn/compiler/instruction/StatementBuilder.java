package at.yawk.yarn.compiler.instruction;

import com.squareup.javapoet.MethodSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yawkat
 */
public class StatementBuilder {
    private final StringBuilder format = new StringBuilder();
    private final List<Object> arguments = new ArrayList<>();

    public StatementBuilder append(String format) {
        this.format.append(format);
        return this;
    }

    public StatementBuilder append(String format, Object... args) {
        this.arguments.addAll(Arrays.asList(args));
        return append(format);
    }

    void flush(MethodSpec.Builder builder) {
        builder.addStatement(format.toString(), arguments.toArray());
        format.setLength(0);
        arguments.clear();
    }
}
