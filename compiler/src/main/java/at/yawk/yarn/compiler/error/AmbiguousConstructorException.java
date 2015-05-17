package at.yawk.yarn.compiler.error;

/**
 * @author yawkat
 */
public class AmbiguousConstructorException extends YarnCompilerException {
    public AmbiguousConstructorException(String message) {
        super(message);
    }
}
