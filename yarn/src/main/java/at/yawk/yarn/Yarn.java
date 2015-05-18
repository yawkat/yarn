package at.yawk.yarn;

/**
 * @author yawkat
 */
@SuppressWarnings({ "unused", "unchecked" })
public class Yarn {
    private Yarn() {}

    public static <E> E build(Class<E> entryPoint) {
        String className = entryPoint.getName();
        try {
            Class<?> entryPointClass = entryPoint.getClassLoader().loadClass(className + "$YarnEntryPoint");
            return (E) entryPointClass.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
