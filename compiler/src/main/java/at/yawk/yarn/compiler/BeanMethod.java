package at.yawk.yarn.compiler;

import java.lang.annotation.Annotation;
import java.util.List;
import javax.lang.model.type.TypeMirror;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class BeanMethod implements BeanProvider {
    private BeanProvider owner;
    private String name;

    private TypeMirror returnType;
    private List<TypeMirror> parameterTypes;
    private List<Annotation> annotations;

    @Override
    public BeanDefinition getBaseDependency() {
        return owner.getBaseDependency();
    }
}
