package at.yawk.yarn.bytecode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

/**
 * @author yawkat
 */
class BEntity {
    final BytecodeContext context;

    BEntity(BytecodeContext context) {
        this.context = context;
    }

    static RuntimeException unsupported() {
        return new UnsupportedOperationException();
    }

    static Name name(String s) {
        return new Name() {
            @Override
            public int length() {
                return s.length();
            }

            @Override
            public char charAt(int index) {
                return s.charAt(index);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return s.subSequence(start, end);
            }

            @Override
            public boolean contentEquals(CharSequence cs) {
                return s.contentEquals(cs);
            }

            @Override
            public String toString() {
                return s;
            }
        };
    }

    static Set<Modifier> modifiers(int mask, Modifier... exclude) {
        Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        if (javassist.Modifier.isAbstract(mask)) { modifiers.add(Modifier.ABSTRACT); }
        if (javassist.Modifier.isFinal(mask)) { modifiers.add(Modifier.FINAL); }
        if (javassist.Modifier.isNative(mask)) { modifiers.add(Modifier.NATIVE); }
        if (javassist.Modifier.isPackage(mask)) { modifiers.add(Modifier.DEFAULT); }
        if (javassist.Modifier.isPrivate(mask)) { modifiers.add(Modifier.PRIVATE); }
        if (javassist.Modifier.isProtected(mask)) { modifiers.add(Modifier.PROTECTED); }
        if (javassist.Modifier.isPublic(mask)) { modifiers.add(Modifier.PUBLIC); }
        if (javassist.Modifier.isStatic(mask)) { modifiers.add(Modifier.STATIC); }
        if (javassist.Modifier.isStrict(mask)) { modifiers.add(Modifier.STRICTFP); }
        if (javassist.Modifier.isSynchronized(mask)) { modifiers.add(Modifier.SYNCHRONIZED); }
        if (javassist.Modifier.isTransient(mask)) { modifiers.add(Modifier.TRANSIENT); }
        if (javassist.Modifier.isVolatile(mask)) { modifiers.add(Modifier.VOLATILE); }
        for (Modifier modifier : exclude) {
            modifiers.remove(modifier);
        }
        return modifiers;
    }

    List<BAnnotationMirror> annotations(AnnotationsAttribute inv, AnnotationsAttribute vis) {
        List<BAnnotationMirror> annotations = new ArrayList<>();
        if (inv != null) {
            for (Annotation annotation : inv.getAnnotations()) {
                annotations.add(context.getAnnotationMirror(annotation));
            }
        }
        if (vis != null) {
            for (Annotation annotation : vis.getAnnotations()) {
                annotations.add(context.getAnnotationMirror(annotation));
            }
        }
        return annotations;
    }
}
