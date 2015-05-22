package at.yawk.yarn.compiler.process.entrypoint;

import at.yawk.yarn.ComponentScan;
import at.yawk.yarn.Include;
import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.EntryPoint;
import at.yawk.yarn.compiler.error.InvalidAnnotationException;
import at.yawk.yarn.compiler.tree.BeanPool;
import java.util.function.Supplier;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author yawkat
 */
public class ComponentScanner implements EntryPointProcessor {
    @Override
    public void process(Compiler compiler, EntryPoint entryPoint, BeanPool pool) {
        TypeElement definitionElement = entryPoint.getDefinitionElement();

        // ComponentScan
        for (ComponentScan scan : definitionElement.getAnnotationsByType(ComponentScan.class)) {
            String pkg = scan.value();
            boolean noClass = className(scan::packageClass).equals(ComponentScan.NoClass.class.getName());
            if (pkg.isEmpty()) {
                if (noClass) {
                    Element element = definitionElement;
                    while (element.getKind() != ElementKind.PACKAGE) {
                        element = element.getEnclosingElement();
                    }
                    pkg = ((PackageElement) element).getQualifiedName().toString();
                } else {
                    pkg = packageName(scan::packageClass);
                }
            } else {
                if (!noClass) {
                    throw new InvalidAnnotationException(
                            "Can only define ComponentScan.value or .packageClass, not both!"
                    );
                }
            }
            packageComponentScan(entryPoint, pool, pkg);
        }

        // Include
        for (Include include : definitionElement.getAnnotationsByType(Include.class)) {
            String toImport = className(include::value);
            BeanDefinition found = null;
            for (BeanDefinition definition : pool.getBeanDefinitions()) {
                if (definition.getType().getKind() == TypeKind.DECLARED &&
                    ((TypeElement) ((DeclaredType) definition.getType()).asElement())
                            .getQualifiedName().contentEquals(toImport)) {
                    found = definition;
                    break;
                }
            }
            if (found == null) {
                throw new InvalidAnnotationException(
                        "Cannot include " + toImport + ", not a component or not on classpath!"
                );
            }
            entryPoint.getIncludedBeans().add(found);
        }
    }

    private static String className(Supplier<Class<?>> cl) {
        try {
            return cl.get().getName();
        } catch (TypeNotPresentException e) {
            return e.typeName();
        }
    }

    private static String packageName(Supplier<Class<?>> cl) {
        try {
            Package pkg = cl.get().getPackage();
            return pkg == null ? "" : pkg.getName();
        } catch (TypeNotPresentException e) {
            int separator = e.typeName().lastIndexOf('.');
            return separator == -1 ? "" : e.typeName().substring(0, separator);
        }
    }

    private void packageComponentScan(EntryPoint entryPoint, BeanPool pool, String pkg) {
        pool.getBeanDefinitions().stream()
                .filter(BeanDefinition::isComponent)
                .forEach(definition -> {
                    TypeMirror type = definition.getType();
                    if (type.getKind() == TypeKind.DECLARED) {
                        String qualifiedName =
                                ((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName().toString();
                        if (pkg.isEmpty() || qualifiedName.startsWith(pkg + '.')) {
                            entryPoint.getIncludedBeans().add(definition);
                        }
                    }
                });
    }
}
