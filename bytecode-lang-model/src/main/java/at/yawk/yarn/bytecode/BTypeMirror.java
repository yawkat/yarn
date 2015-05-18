package at.yawk.yarn.bytecode;

import javax.lang.model.type.TypeMirror;

/**
 * @author yawkat
 */
abstract class BTypeMirror extends BAnnotatedConstruct implements TypeMirror {
    BTypeMirror(BytecodeContext context) {
        super(context);
    }
}
