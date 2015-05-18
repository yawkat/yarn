package at.yawk.yarn.bytecode;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import javassist.bytecode.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class BAnnotation implements InvocationHandler {
    private final Factory factory;
    private final Annotation annotation;

    public static <A extends java.lang.annotation.Annotation> A map(Annotation annotation, Class<A> type) {
        return new Factory().map(annotation, type);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        assert args.length == 0;
        MemberValue value = annotation.getMemberValue(method.getName());
        return value == null ?
                method.getDefaultValue() :
                factory.map(value, method.getReturnType());
    }

    private static class Factory {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        @SneakyThrows
        private Object map(MemberValue value, Class<?> expectedValue) {
            if (value instanceof AnnotationMemberValue) {
                Annotation annotation1 = ((AnnotationMemberValue) value).getValue();
                Class c = cl.loadClass(annotation1.getTypeName());
                return map(annotation1, c);
            }
            if (value instanceof ArrayMemberValue) {
                MemberValue[] values = ((ArrayMemberValue) value).getValue();
                Class<?> componentType = expectedValue.getComponentType();
                return Arrays.stream(values)
                        .map(v -> map(v, componentType))
                        .toArray(i -> (Object[]) Array.newInstance(componentType, i));
            }
            if (value instanceof ClassMemberValue) {
                return cl.loadClass(((ClassMemberValue) value).getValue());
            }
            if (value instanceof BooleanMemberValue) { return ((BooleanMemberValue) value).getValue(); }
            if (value instanceof ByteMemberValue) { return ((ByteMemberValue) value).getValue(); }
            if (value instanceof ShortMemberValue) { return ((ShortMemberValue) value).getValue(); }
            if (value instanceof CharMemberValue) { return ((CharMemberValue) value).getValue(); }
            if (value instanceof IntegerMemberValue) { return ((IntegerMemberValue) value).getValue(); }
            if (value instanceof LongMemberValue) { return ((LongMemberValue) value).getValue(); }
            if (value instanceof FloatMemberValue) { return ((FloatMemberValue) value).getValue(); }
            if (value instanceof DoubleMemberValue) { return ((DoubleMemberValue) value).getValue(); }
            return ((StringMemberValue) value).getValue();
        }

        @SuppressWarnings("unchecked")
        <A extends java.lang.annotation.Annotation> A map(Annotation annotation, Class<A> type) {
            return (A) Proxy.newProxyInstance(cl, new Class[]{ type }, new BAnnotation(this, annotation));
        }
    }
}
