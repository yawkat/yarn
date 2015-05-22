package at.yawk.yarn.compiler.process.entrypoint;

import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.EntryPoint;
import at.yawk.yarn.compiler.LookupBeanReference;
import at.yawk.yarn.compiler.Util;
import at.yawk.yarn.compiler.tree.BeanPool;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author yawkat
 */
public class EntryPointGetterBuilder implements EntryPointProcessor {
    @Override
    public void process(Compiler compiler, EntryPoint entryPoint, BeanPool tree) {
        process(compiler, entryPoint, entryPoint.getDefinitionElement());
    }

    private void process(Compiler compiler, EntryPoint entryPoint, TypeElement element) {
        for (Element enclosed : element.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.METHOD &&
                enclosed.getModifiers().contains(Modifier.ABSTRACT)) {

                String name = enclosed.getSimpleName().toString();

                // this was already added in a subclass
                if (entryPoint.getGetters().containsKey(name)) { continue; }

                LookupBeanReference reference = new LookupBeanReference();
                reference.setType(((ExecutableElement) enclosed).getReturnType());
                reference.setAnnotations(Util.getAnnotations(enclosed));
                compiler.processLookupBeanReference(reference);

                entryPoint.getGetters().put(name, reference);
            }
        }

        // super
        TypeMirror superclass = element.getSuperclass();
        if (superclass != null) {
            if (superclass.getKind() == TypeKind.DECLARED) {
                process(compiler, entryPoint, (TypeElement) ((DeclaredType) superclass).asElement());
            }
        }

        // interfaces
        for (TypeMirror itf : element.getInterfaces()) {
            process(compiler, entryPoint, (TypeElement) ((DeclaredType) itf).asElement());
        }
    }
}
