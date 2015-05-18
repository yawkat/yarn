package at.yawk.yarn.compiler;

import java.util.HashSet;
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
}
