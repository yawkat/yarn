package at.yawk.yarn.bytecode;

import javax.lang.model.element.Element;

/**
 * @author yawkat
 */
abstract class BElement extends BAnnotatedConstruct implements Element {
    BElement(BytecodeContext context) {
        super(context);
    }

    @Override
    public Element getEnclosingElement() {
        throw unsupported();
    }
}
