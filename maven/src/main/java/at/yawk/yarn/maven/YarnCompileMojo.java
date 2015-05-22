package at.yawk.yarn.maven;

import at.yawk.yarn.bytecode.BytecodeContext;
import at.yawk.yarn.compiler.instruction.BeanTreeWriter;
import at.yawk.yarn.compiler.tree.BeanPool;
import at.yawk.yarn.compiler.tree.BeanTreeCompiler;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.compiler.AbstractCompilerMojo;
import org.apache.maven.plugin.compiler.CompilationFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;

/**
 * @author yawkat
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class YarnCompileMojo extends AbstractCompilerMojo {
    @Parameter(defaultValue = "${project.build.directory}")
    String buildDirectory;

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    String outputDirectory;

    @Parameter(defaultValue = "${project.compileClasspathElements}")
    List<String> classpath;

    @Override
    public void execute() throws MojoExecutionException, CompilationFailureException {
        try {
            walkAndGenerate();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }

        super.execute();
    }

    private void walkAndGenerate() throws IOException, NotFoundException {
        BeanTreeCompiler compiler = new BeanTreeCompiler();

        // Collect all class names we know
        Set<String> classNames = new HashSet<>();
        for (String cp : getClasspathElements()) {
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
        for (String cp : getClasspathElements()) {
            pool.appendClassPath(cp);
        }

        // walk and scan top-level classes
        BytecodeContext context = BytecodeContext.of(pool);
        for (String className : classNames) {
            compiler.scan(context.findType(className));
        }

        // generate code
        BeanPool tree = compiler.finishTree();
        Path generationFolder = getSourceRoot();
        BeanTreeWriter.write(tree, generationFolder);
    }

    private Path getSourceRoot() {
        return Paths.get(buildDirectory, "generated-sources", "yarn");
    }

    private Path getClassRoot() {
        // todo use generated-classes and register with jar plugin
        return Paths.get(outputDirectory);
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

    /////// COMPILER ///////

    @Override
    protected SourceInclusionScanner getSourceInclusionScanner(int staleMillis) {
        return new SimpleSourceInclusionScanner(Collections.singleton("**/*.java"), Collections.emptySet());
    }

    @Override
    protected SourceInclusionScanner getSourceInclusionScanner(String inputFileEnding) {
        assert inputFileEnding.equals(".java");
        return getSourceInclusionScanner(0);
    }

    @Override
    protected List<String> getClasspathElements() {
        return classpath;
    }

    @Override
    protected List<String> getCompileSourceRoots() {
        return Collections.singletonList(getSourceRoot().toString());
    }

    @Override
    protected File getOutputDirectory() {
        return getClassRoot().toFile();
    }

    @Override
    protected String getSource() {
        return "1.8";
    }

    @Override
    protected String getTarget() {
        return "1.8";
    }

    @Override
    protected String getCompilerArgument() {
        return "";
    }

    @Override
    protected Map<String, String> getCompilerArguments() {
        return Collections.emptyMap();
    }

    @Override
    protected File getGeneratedSourcesDirectory() {
        return null;
    }
}
