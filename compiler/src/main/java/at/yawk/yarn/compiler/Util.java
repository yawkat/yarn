package at.yawk.yarn.compiler;

import at.yawk.yarn.compiler.error.InvalidAccessException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractElementVisitor8;

/**
 * @author yawkat
 */
public class Util {
    public static <E> Iterable<E> filterByType(Iterable<?> iterable, Class<E> type) {
        return () -> new Iterator<E>() {
            private final Iterator<?> itr = iterable.iterator();
            private E peekedItem = null;

            @SuppressWarnings("unchecked")
            private void peek() {
                while (itr.hasNext()) {
                    Object next = itr.next();
                    if (type.isInstance(next)) {
                        peekedItem = (E) next;
                        break;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                if (peekedItem == null) {
                    peek();
                }
                return peekedItem != null;
            }

            @Override
            public E next() {
                if (peekedItem == null) {
                    peek();
                    if (peekedItem == null) {
                        throw new NoSuchElementException();
                    }
                }
                E next = this.peekedItem;
                peekedItem = null;
                return next;
            }
        };
    }

    private static final List<Modifier> ACCESS_LADDER =
            Arrays.asList(Modifier.PUBLIC, Modifier.PROTECTED, Modifier.DEFAULT, Modifier.PRIVATE);

    public static void checkAccess(Element element, Modifier expected) {
        int expectedIndex = ACCESS_LADDER.indexOf(expected);
        assert expectedIndex != -1 : expected + " is not an access modifier";
        for (Modifier modifier : element.getModifiers()) {
            int found = ACCESS_LADDER.indexOf(modifier);
            if (found != -1) {
                if (found > expectedIndex) {
                    // invisible, throw
                    invalidAccess(element, expected, modifier);
                } else {
                    // as visible or more visible than necessary
                    return;
                }
            }
        }
        // check if DEFAULT is allowed, throw otherwise
        if (ACCESS_LADDER.indexOf(Modifier.DEFAULT) > expectedIndex) {
            invalidAccess(element, expected, Modifier.DEFAULT);
        }
    }

    public static void checkAccess(Element element) {
        checkAccess(element, Modifier.DEFAULT);
    }

    private static void invalidAccess(Element element, Modifier expect, Modifier found) {
        throw new InvalidAccessException(
                "Invalid access on " + element + ": Expected at least " + expect + " but was " + found);
    }

    public static <T> Type getGenericType(Class<T> definition, Class<? extends T> implementation, int index) {
        // only works on direct subclasses because I'm lazy
        Type superType = implementation.getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) superType;
        assert parameterizedType.getRawType() == definition;
        return parameterizedType.getActualTypeArguments()[index];
    }

    public static String decapitalize(String s) {
        char[] chars = s.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    public static List<Annotation> getAnnotations(Element element) {
        List<Annotation> annotations = new ArrayList<>();
        Set<String> resolvedClasses = new HashSet<>();
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            try {
                String qname = ((TypeElement) mirror.getAnnotationType().asElement()).getQualifiedName().toString();
                if (resolvedClasses.add(qname)) {
                    Class annotationClass = Class.forName(qname);
                    annotations.add(element.getAnnotation(annotationClass));
                }
            } catch (Exception ignored) {}
        }
        return annotations;
    }

    public static boolean inherits(TypeMirror parent, TypeMirror child) {
        return parent.equals(child) ||
               (
                       child.getKind() == TypeKind.DECLARED &&
                       parent.getKind() == TypeKind.DECLARED &&
                       inherits((TypeElement) ((DeclaredType) parent).asElement(), child)
               );
    }

    private static boolean inherits(TypeElement parent, TypeMirror child) {
        return child.getKind() == TypeKind.DECLARED &&
               inherits(parent, (TypeElement) ((DeclaredType) child).asElement());
    }

    public static boolean inherits(TypeElement parent, TypeElement child) {
        return child.accept(InheritanceElementVisitor.instance, parent);
    }

    private static class InheritanceElementVisitor extends AbstractElementVisitor8<Boolean, TypeElement> {
        static final InheritanceElementVisitor instance = new InheritanceElementVisitor();

        @Override
        public Boolean visitPackage(PackageElement e, TypeElement element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Boolean visitType(TypeElement e, TypeElement parent) {
            if (parent.equals(e)) {
                return true;
            }
            TypeMirror superclass = e.getSuperclass();
            if (superclass != null && inherits(parent, superclass)) {
                return true;
            }
            for (TypeMirror itf : e.getInterfaces()) {
                if (inherits(parent, itf)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Boolean visitVariable(VariableElement e, TypeElement parent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Boolean visitExecutable(ExecutableElement e, TypeElement parent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Boolean visitTypeParameter(TypeParameterElement e, TypeElement parent) {
            throw new UnsupportedOperationException();
        }
    }

    public static String sanitizeJavaName(String name) {
        StringBuilder result = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (result.length() == 0) {
                if (Character.isJavaIdentifierStart(c)) {
                    result.append(c);
                }
            } else {
                if (Character.isJavaIdentifierPart(c)) {
                    result.append(c);
                }
            }
        }

        // make sure the identifier
        // - isn't empty
        // - doesn't end with a digit
        if (result.length() == 0 ||
            Character.isDigit(result.charAt(result.length() - 1))) {
            result.append('_');
        }
        return result.toString();
    }
}
