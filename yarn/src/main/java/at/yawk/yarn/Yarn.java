package at.yawk.yarn;

/**
 * Main yarn accessor class. Replaced at compile time.
 *
 * @author yawkat
 */
@SuppressWarnings("unused")
public class Yarn {
    private Yarn() {}

    public static Yarn build() {
        throw new UnsupportedOperationException();
    }

    public <C> C getComponent(Class<C> type) {
        throw new UnsupportedOperationException();
    }
}
