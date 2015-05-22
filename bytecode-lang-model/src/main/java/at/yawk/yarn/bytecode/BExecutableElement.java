package at.yawk.yarn.bytecode;

import java.util.ArrayList;
import java.util.List;
import javassist.CtBehavior;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
class BExecutableElement extends BMemberElement<CtBehavior> implements ExecutableElement {
    BExecutableElement(BytecodeContext context, CtBehavior member) {
        super(context, member);
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
        throw unsupported();
    }

    @Override
    public TypeMirror getReturnType() {
        if (member instanceof CtConstructor) {
            return context.noType();
        } else {
            SignatureAttribute.Type returnType = signature().getReturnType();
            return context.getTypeMirror(returnType);
        }
    }

    @SneakyThrows
    private SignatureAttribute.MethodSignature signature() {
        CtMethod method = (CtMethod) this.member;

        SignatureAttribute attribute =
                (SignatureAttribute) member.getMethodInfo2().getAttribute(SignatureAttribute.tag);
        String signature = attribute == null ?
                method.getMethodInfo().getDescriptor() :
                attribute.getSignature();
        return SignatureAttribute.toMethodSignature(signature);
    }

    @Override
    public List<? extends VariableElement> getParameters() {
        SignatureAttribute.Type[] parameterTypes = signature().getParameterTypes();
        ParameterAnnotationsAttribute vis = (ParameterAnnotationsAttribute) member.getMethodInfo2()
                .getAttribute(ParameterAnnotationsAttribute.visibleTag);
        ParameterAnnotationsAttribute inv = (ParameterAnnotationsAttribute) member.getMethodInfo2()
                .getAttribute(ParameterAnnotationsAttribute.invisibleTag);

        List<BParameterElement> parameters = new ArrayList<>(parameterTypes.length);
        for (int i = 0; i < parameterTypes.length; i++) {
            final int finalI = i;
            parameters.add(new BParameterElement(
                    context,
                    parameterTypes[i],
                    getAnnotationsAt(vis, i), getAnnotationsAt(inv, i),
                    () -> {
                        LocalVariableAttribute attribute = (LocalVariableAttribute) member.getMethodInfo2()
                                .getAttribute(LocalVariableAttribute.tag);
                        if (attribute != null) {
                            int localVarIndex;
                            if (Modifier.isStatic(member.getModifiers())) {
                                localVarIndex = finalI;
                            } else {
                                localVarIndex = finalI + 1;
                            }
                            return attribute.variableName(localVarIndex);
                        } else {
                            return "arg" + finalI;
                        }
                    }
            ));
        }
        return parameters;
    }

    private Annotation[] getAnnotationsAt(ParameterAnnotationsAttribute attr, int i) {
        if (attr == null) { return new Annotation[0]; }
        return attr.getAnnotations()[i];
    }

    @Override
    public TypeMirror getReceiverType() {
        throw unsupported();
    }

    @Override
    public boolean isVarArgs() {
        return (member.getModifiers() & Modifier.VARARGS) != 0;
    }

    @Override
    public boolean isDefault() {
        return !javassist.Modifier.isAbstract(member.getModifiers());
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes() {
        throw unsupported();
    }

    @Override
    public AnnotationValue getDefaultValue() {
        throw unsupported();
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitExecutable(this, p);
    }

    @Override
    public List<BAnnotationMirror> getAnnotationMirrors() {
        MethodInfo mi = member.getMethodInfo2();
        AnnotationsAttribute inv = (AnnotationsAttribute)
                mi.getAttribute(AnnotationsAttribute.invisibleTag);
        AnnotationsAttribute vis = (AnnotationsAttribute)
                mi.getAttribute(AnnotationsAttribute.visibleTag);
        return annotations(inv, vis);
    }

    @Override
    public String toString() {
        return member.getName() + member.getMethodInfo2().getDescriptor();
    }
}
