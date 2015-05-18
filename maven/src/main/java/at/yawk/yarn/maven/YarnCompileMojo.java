package at.yawk.yarn.maven;

import at.yawk.yarn.bytecode.BytecodeContext;
import at.yawk.yarn.compiler.instruction.BeanTreeWriter;
import at.yawk.yarn.compiler.tree.BeanTree;
import at.yawk.yarn.compiler.tree.BeanTreeCompiler;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * @author yawkat
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE)
public class YarnCompileMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}")
    MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            doExecute();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (NotFoundException | DependencyResolutionRequiredException e) {
            throw new RuntimeException(e);
        }
    }

    private void doExecute() throws IOException, NotFoundException, DependencyResolutionRequiredException {
        BeanTreeCompiler compiler = new BeanTreeCompiler();

        // Collect all class names we know
        List<String> classNames = new ArrayList<>();
        classNames.addAll(collectClassNames(Paths.get(project.getBuild().getOutputDirectory())));
        for (String cp : project.getCompileClasspathElements()) {
            Path location = Paths.get(cp);
            if (Files.isRegularFile(location)) {
                try (FileSystem fs = FileSystems.newFileSystem(location, null)) {
                    classNames.addAll(collectClassNames(fs.getRootDirectories().iterator().next()));
                }
            } else {
                classNames.addAll(collectClassNames(location));
            }
        }

        // Build javassist classpath
        ClassPool pool = new ClassPool();
        pool.appendSystemPath();
        for (String cp : project.getCompileClasspathElements()) {
            pool.appendClassPath(cp);
        }

        // walk and scan top-level classes
        BytecodeContext context = BytecodeContext.of(pool);
        for (String className : classNames) {
            compiler.scan(context.findType(className));
        }

        // generate code
        BeanTree tree = compiler.finishTree();
        BeanTreeWriter.write(tree, Paths.get("gen"));
    }

    @SuppressWarnings("Convert2streamapi")
    private static List<String> collectClassNames(Path directory) {
        List<String> names = new ArrayList<>();
        try {
            Files.list(directory).forEach(c -> {
                if (Files.isDirectory(c)) {
                    for (String name : collectClassNames(c)) {
                        names.add(c.getFileName() + "." + name);
                    }
                } else {
                    String name = c.getFileName().toString();
                    if (name.endsWith(".class") &&
                        // exclude nested classes
                        !name.contains("$")) {
                        names.add(name.substring(0, name.length() - 6));
                    }
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return names;
    }
}
