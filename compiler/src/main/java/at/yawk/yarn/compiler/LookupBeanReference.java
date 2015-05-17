package at.yawk.yarn.compiler;

import at.yawk.yarn.compiler.instruction.resolver.BeanResolver;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.lang.model.type.TypeMirror;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class LookupBeanReference implements BeanReference {
    private TypeMirror type;
    private List<Annotation> annotations;
    private Optional<String> name = Optional.empty();
    private List<BeanFilter> filters = new ArrayList<>();
    private boolean soft = false;
    private BeanResolver resolver;

    public void addFilter(BeanFilter filter) {
        filters.add(filter);
    }

    @Override
    public String toString() {
        return String.format(
                "%%{type=%s name=%s fil=%s res=%s}s=%s",
                type, name, filters, resolver, soft
        );
    }
}
