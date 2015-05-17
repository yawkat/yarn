package at.yawk.yarn.compiler.instruction;

import at.yawk.yarn.Yarn;
import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.instruction.setup.SetupInstruction;
import at.yawk.yarn.compiler.tree.BeanTree;
import com.squareup.javapoet.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanTreeWriter {
    private static final String YARN_FACTORY_NAME = "_yarn_factory";
    private static final String YARN_PACKAGE = Yarn.class.getPackage().getName();
    private static final String YARN_CLASS_NAME = Yarn.class.getSimpleName();

    private final BeanTree tree;
    private final Path folder;

    private final Map<String, TypeSpec.Builder> factoryClasses = new HashMap<>();

    public static void write(BeanTree tree, Path outputFolder) throws IOException {
        new BeanTreeWriter(tree, outputFolder).write();
    }

    private void write(JavaFile.Builder builder) throws IOException {
        JavaFile file = builder.build();
        file.writeTo(folder);
    }

    private void write() throws IOException {
        writeFactories();
        writeGod();
    }

    private void writeFactories() throws IOException {
        for (BeanDefinition bean : tree.getSortedBeans()) {
            writeFactory(bean);
        }
        for (Map.Entry<String, TypeSpec.Builder> entry : factoryClasses.entrySet()) {
            write(JavaFile.builder(entry.getKey(), entry.getValue().build()));
        }
    }

    private void writeFactory(BeanDefinition bean) {
        String packageName = getAccessPackage(bean);

        TypeSpec.Builder factoryBuilder = factoryClasses.computeIfAbsent(
                packageName,
                p -> TypeSpec.classBuilder(YARN_FACTORY_NAME)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethod(
                                MethodSpec.constructorBuilder()
                                        .addModifiers(Modifier.PRIVATE)
                                        .build()
                        )
        );

        MethodSpec.Builder factoryMethodBuilder = MethodSpec.methodBuilder(bean.getId().toString())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Yarn.class, "yarn")
                .returns(TypeName.get(bean.getType()));

        StatementBuilder builder = new StatementBuilder();
        StatementContext ctx = new StatementContext(builder, tree, "yarn", bean.getType(), "instance");

        // constructor
        builder.append("$T instance = ", bean.getType());
        bean.getFactory().write(ctx);
        builder.flush(factoryMethodBuilder);

        for (SetupInstruction setupInstruction : bean.getSetupInstructions()) {
            setupInstruction.write(ctx);
            builder.flush(factoryMethodBuilder);
        }

        factoryMethodBuilder.addStatement("return instance");

        factoryBuilder.addMethod(factoryMethodBuilder.build());
    }

    private static String getAccessPackage(BeanDefinition bean) {
        String qname = ((TypeElement) bean.getAccessType().asElement()).getQualifiedName().toString();
        int classNameIndex = qname.lastIndexOf('.');
        return classNameIndex == -1 ? "" : qname.substring(0, classNameIndex);
    }

    private void writeGod() throws IOException {
        TypeSpec.Builder type = TypeSpec.classBuilder(YARN_CLASS_NAME)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC);

        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE);

        ParameterizedTypeName entryPointMapType = ParameterizedTypeName.get(HashMap.class, Class.class, Object.class);
        FieldSpec entryPointMap = FieldSpec.builder(
                entryPointMapType,
                "entryPoints",
                Modifier.PRIVATE, Modifier.FINAL
        ).initializer("new $T()", entryPointMapType).build();
        type.addField(entryPointMap);

        for (BeanDefinition bean : tree.getSortedBeans()) {
            String id = bean.getId().toString();

            FieldSpec field = FieldSpec.builder(
                    TypeName.get(bean.getType()),
                    '_' + id,
                    Modifier.PUBLIC,
                    Modifier.FINAL
            ).build();

            type.addField(field);

            constructor.addStatement(
                    "$N = $T.$L(this)",
                    field, ClassName.get(getAccessPackage(bean), YARN_FACTORY_NAME), id
            );

            if (bean.isEntryPoint()) {
                constructor.addStatement("$N.put($T.class, $N)", entryPointMap, bean.getType(), field);
            }
        }

        type.addMethod(constructor.build());
        type.addMethod(
                MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                        .returns(Yarn.class)
                        .addStatement("return new $L()", YARN_CLASS_NAME)
                        .build()
        );

        write(JavaFile.builder(YARN_PACKAGE, type.build()).indent("  "));
    }
}
