package at.yawk.yarn.compiler.instruction;

import at.yawk.yarn.compiler.BeanDefinition;
import at.yawk.yarn.compiler.EntryPoint;
import at.yawk.yarn.compiler.instruction.setup.SetupInstruction;
import at.yawk.yarn.compiler.tree.BeanPool;
import at.yawk.yarn.compiler.tree.ContextBeanTree;
import com.squareup.javapoet.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanTreeWriter {
    private static final String YARN_FACTORY_NAME = "$YarnFactory";

    private final BeanPool tree;
    private final Path folder;

    private final Map<String, TypeSpec.Builder> factoryClasses = new HashMap<>();

    public static void write(BeanPool tree, Path outputFolder) throws IOException {
        new BeanTreeWriter(tree, outputFolder).write();
    }

    private void write(JavaFile.Builder builder) throws IOException {
        JavaFile file = builder.build();
        file.writeTo(folder);
    }

    private void write() throws IOException {
        for (Map.Entry<EntryPoint, ContextBeanTree> entry : tree.getEntryPoints().entrySet()) {
            EntryPoint entryPoint = entry.getKey();
            ContextBeanTree context = entry.getValue();
            for (BeanDefinition bean : context.getSortedBeans()) {
                writeFactory(entryPoint, context, bean);
            }
            writeGod(entryPoint, context);
        }
        for (Map.Entry<String, TypeSpec.Builder> entry : factoryClasses.entrySet()) {
            write(JavaFile.builder(entry.getKey(), entry.getValue().build()));
        }
    }

    private void writeFactory(EntryPoint entryPoint, ContextBeanTree context, BeanDefinition bean) {
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

        ClassName godClassName = getGodClassName(entryPoint);
        MethodSpec.Builder factoryMethodBuilder = MethodSpec.methodBuilder(bean.getId().toString())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(godClassName, "yarn")
                .returns(TypeName.get(bean.getType()));

        StatementBuilder builder = new StatementBuilder();
        StatementContext ctx = new StatementContext(builder, context, "yarn", bean.getType(), "instance");

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

    private void writeGod(EntryPoint entryPoint, ContextBeanTree context) throws IOException {
        ClassName className = getGodClassName(entryPoint);
        TypeSpec.Builder type = TypeSpec.classBuilder(className.simpleName())
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC);

        TypeName sup = TypeName.get(entryPoint.getDefinitionElement().asType());
        if (entryPoint.getDefinitionElement().getKind() == ElementKind.INTERFACE) {
            type.addSuperinterface(sup);
        } else {
            type.superclass(sup);
        }

        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (BeanDefinition bean : context.getSortedBeans()) {
            String id = bean.getId().toString();

            FieldSpec field = FieldSpec.builder(
                    TypeName.get(bean.getType()),
                    '_' + id,
                    Modifier.PUBLIC,
                    Modifier.FINAL
            ).build();

            type.addField(field);

            System.out.println("ref: " + ClassName.get(getAccessPackage(bean), YARN_FACTORY_NAME));
            constructor.addStatement(
                    "$N = $T.$L(this)",
                    field, ClassName.get(getAccessPackage(bean), YARN_FACTORY_NAME), id
            );
        }

        type.addMethod(constructor.build());

        entryPoint.getGetters().forEach((mn, ref) -> {
            MethodSpec.Builder builder = MethodSpec.methodBuilder(mn)
                    .addAnnotation(Override.class)
                    .returns(TypeName.get(ref.getType()))
                    .addModifiers(Modifier.PUBLIC);
            StatementBuilder statementBuilder = new StatementBuilder().append("return ");
            ref.getResolver().write(new StatementContext(statementBuilder, context, "this", null, null));
            statementBuilder.flush(builder);
            type.addMethod(builder.build());
        });

        write(JavaFile.builder(className.packageName(), type.build()).indent("  "));
    }

    private static String getAccessPackage(BeanDefinition bean) {
        Element element = bean.getAccessType().asElement();
        while (element.getKind() != ElementKind.PACKAGE) {
            element = element.getEnclosingElement();
        }
        return ((PackageElement) element).getQualifiedName().toString();
    }

    private static ClassName getGodClassName(EntryPoint entryPoint) {
        TypeElement definitionElement = entryPoint.getDefinitionElement();
        ClassName defName = ClassName.get(definitionElement);
        return ClassName.get(defName.packageName(), String.join("$", defName.simpleNames()) + "$YarnEntryPoint");
    }
}
