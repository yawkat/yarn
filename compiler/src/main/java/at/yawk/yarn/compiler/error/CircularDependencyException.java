package at.yawk.yarn.compiler.error;

/**
 * @author yawkat
 */
public class CircularDependencyException extends YarnCompilerException {
    public CircularDependencyException(String message) {
        super(message);
    }
}
