package at.yawk.yarn.compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class EntryPoint {
    private TypeElement definitionElement;
    private Set<BeanDefinition> includedBeans = new HashSet<>();
    private Map<String, BeanReference> getters = new HashMap<>();
}
