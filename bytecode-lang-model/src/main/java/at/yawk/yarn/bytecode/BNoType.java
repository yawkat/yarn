package at.yawk.yarn.bytecode;

import java.util.Collections;
import java.util.List;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

/**
 * @author yawkat
 */
class BNoType extends BTypeMirror implements NoType {
    BNoType(BytecodeContext context) {
        super(context);
    }

    @Override
    public String toString() {
        return "<>";
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.NONE;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitNoType(this, p);
    }

    @Override
    public List<BAnnotationMirror> getAnnotationMirrors() {
        return Collections.emptyList();
    }
}
