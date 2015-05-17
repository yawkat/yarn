package at.yawk.yarn.compiler.error;

/**
 * @author yawkat
 */
public class YarnCompilerException extends RuntimeException {
    public YarnCompilerException(String message) {
        super(message);
    }

    public YarnCompilerException(String message, Throwable cause) {
        super(message, cause);
    }
}
