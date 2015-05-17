package at.yawk.yarn.compiler;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class BeanId {
    private final String name;
    private final boolean useIndex;
    private final int index;

    @Override
    public String toString() {
        return useIndex ? name + index : name;
    }
}
