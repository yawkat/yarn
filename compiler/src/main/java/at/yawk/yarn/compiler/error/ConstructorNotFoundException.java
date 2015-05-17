package at.yawk.yarn.compiler.error;

/**
 * @author yawkat
 */
public class ConstructorNotFoundException extends YarnCompilerException {
    public ConstructorNotFoundException(String message) {
        super(message);
    }
}
