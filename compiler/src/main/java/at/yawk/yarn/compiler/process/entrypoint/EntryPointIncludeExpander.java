package at.yawk.yarn.compiler.process.entrypoint;

import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.EntryPoint;
import at.yawk.yarn.compiler.tree.BeanPool;
import java.util.ArrayList;

/**
 * @author yawkat
 */
public class EntryPointIncludeExpander implements EntryPointProcessor {
    @Override
    public void process(Compiler compiler, EntryPoint entryPoint, BeanPool tree) {
        for (BeanDefinition definition : new ArrayList<>(entryPoint.getIncludedBeans())) {
            expand(entryPoint, definition);
        }
    }

    private void expand(EntryPoint entryPoint, BeanDefinition definition) {
        for (BeanDefinition provided : definition.getProvidingDefinitions()) {
            entryPoint.getIncludedBeans().add(provided);
            expand(entryPoint, provided);
        }
    }
}
