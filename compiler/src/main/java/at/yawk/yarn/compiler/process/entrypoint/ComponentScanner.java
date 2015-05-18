package at.yawk.yarn.compiler.process.entrypoint;

import at.yawk.yarn.ComponentScan;
import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.EntryPoint;
import at.yawk.yarn.compiler.error.InvalidAnnotationException;
import at.yawk.yarn.compiler.tree.BeanPool;
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
            if (pkg.isEmpty()) {
                if (scan.packageClass() != ComponentScan.NoClass.class) {
                    pkg = scan.packageClass().getPackage().getName();
                } else {
                    Element element = definitionElement;
                    while (element.getKind() != ElementKind.PACKAGE) {
                        element = element.getEnclosingElement();
                    }
                    pkg = ((PackageElement) element).getQualifiedName().toString();
                }
            } else {
                if (scan.packageClass() != ComponentScan.NoClass.class) {
                    throw new InvalidAnnotationException(
                            "Can only define ComponentScan.value or .packageClass, not both!"
                    );
                }
            }
            packageComponentScan(entryPoint, pool, pkg);
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
