package at.yawk.yarn.bytecode;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import javassist.bytecode.annotation.*;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class BAnnotation implements InvocationHandler {
    private final Class<? extends java.lang.annotation.Annotation> type;
    private final Factory factory;
    private final Annotation annotation;

    public static <A extends java.lang.annotation.Annotation> A map(Annotation annotation, Class<A> type) {
        return new Factory().map(annotation, type);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        assert args.length == 0;
        MemberValue value = annotation.getMemberValue(method.getName());
        return value == null ?
                method.getDefaultValue() :
                factory.map(value, method.getReturnType());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(type.getSimpleName()).append('{');
        for (int i = 0; i < type.getMethods().length; i++) {
            if (i != 0) { builder.append(", "); }
            Method method = type.getMethods()[i];
            builder.append(method.getName()).append('=');
            Object val = invoke(null, method, new Object[0]);
            if (val.getClass().isArray()) {
                builder.append(Arrays.toString((Object[]) val));
            } else if (val instanceof String) {
                builder.append('"').append(val).append('"');
            } else if (val instanceof Class<?>) {
                builder.append(((Class) val).getName());
            } else {
                builder.append(val);
            }
        }
        return builder.append('}').toString();
    }

    private static class Factory {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        private Object map(MemberValue value, Class<?> expectedValue) {
            if (value instanceof AnnotationMemberValue) {
                Annotation a = ((AnnotationMemberValue) value).getValue();
                Class c = doLoadClass(a.getTypeName());
                return map(a, c);
            }
            if (value instanceof ArrayMemberValue) {
                MemberValue[] values = ((ArrayMemberValue) value).getValue();
                Class<?> componentType = expectedValue.getComponentType();
                return Arrays.stream(values)
                        .map(v -> map(v, componentType))
                        .toArray(i -> (Object[]) Array.newInstance(componentType, i));
            }
            if (value instanceof ClassMemberValue) {
                return doLoadClass(((ClassMemberValue) value).getValue());
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

        private Class<?> doLoadClass(String n) {
            try {
                return cl.loadClass(n);
            } catch (ClassNotFoundException e) {
                throw new TypeNotPresentException(n, e);
            }
        }

        @SuppressWarnings("unchecked")
        <A extends java.lang.annotation.Annotation> A map(Annotation annotation, Class<A> type) {
            return (A) Proxy.newProxyInstance(cl, new Class[]{ type }, new BAnnotation(type, this, annotation));
        }
    }
}
