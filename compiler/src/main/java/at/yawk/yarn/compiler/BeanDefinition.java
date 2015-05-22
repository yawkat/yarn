package at.yawk.yarn.compiler;

import at.yawk.yarn.compiler.instruction.factory.BeanFactory;
import at.yawk.yarn.compiler.instruction.setup.SetupInstruction;
import java.lang.annotation.Annotation;
import java.util.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yawkat
 */
@Getter
@Setter
public class BeanDefinition implements BeanProvider {
    private BeanId id;

    /**
     * Type this definition must neighbour for package access.
     */
    private DeclaredType accessType;
    /**
     * Generic name for this bean. Copied to #name if this bean is annotated with @Named.
     */
    private String implicitName;
    private Optional<String> name = Optional.empty();
    private TypeMirror type;
    private List<Annotation> annotations;

    private BeanFactory factory;
    /**
     * Instructions to run on this bean after construction
     */
    private List<SetupInstruction> setupInstructions = new ArrayList<>();

    private Set<BeanReference> dependencies = new HashSet<>();

    private Set<BeanDefinition> providingDefinitions = new HashSet<>();

    private List<BeanMethod> methods = new ArrayList<>();

    private boolean component = false;

    @Override
    public String toString() {
        return String.format(
                "Bean{id=%s name=%s type=%s #deps=%d}",
                id, name.orElse(implicitName), type, dependencies.size()
        );
    }

    @Override
    public BeanDefinition getBaseDependency() {
        return this;
    }
}
