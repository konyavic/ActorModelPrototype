package com.example.konyavic.compiler;

import com.example.konyavic.library.Actor;
import com.example.konyavic.library.ActorMethod;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;


@SupportedAnnotationTypes({
        "com.example.konyavic.library.Actor",
        "com.example.konyavic.library.ActorMethod"
})
public final class ActorProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("enter ActorProcessor");
        Map<Element, Collection<ExecutableElement>> actorMethodMap = new HashMap<>();

        Collection<? extends Element> actorElements = roundEnvironment.getElementsAnnotatedWith(Actor.class);
        for (Element e : actorElements) {
            System.out.println(e.getSimpleName());
            System.out.println(e.getKind());
            System.out.println(e.getEnclosedElements());
            System.out.println(e.getEnclosingElement());
        }

        Collection<? extends Element> actorMethodElements = roundEnvironment.getElementsAnnotatedWith(ActorMethod.class);
        for (Element e : actorMethodElements) {
            System.out.println(e.getSimpleName());
            System.out.println(e.getKind());
            System.out.println(e.getEnclosedElements());
            System.out.println(e.getEnclosingElement());

            Element parent = e.getEnclosingElement();
            Collection<ExecutableElement> c;
            if (actorMethodMap.containsKey(parent)) {
                c = actorMethodMap.get(parent);
            } else {
                c = new LinkedList<>();

            }
            c.add((ExecutableElement) e);
            actorMethodMap.put(parent, c);
        }

        for (Element actor : actorMethodMap.keySet()) {
            try {
                buildActor(actor, actorMethodMap.get(actor));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    void buildActor(Element actor, Collection<ExecutableElement> methods) throws ClassNotFoundException {
        System.out.println("build " + actor.getAnnotation(Actor.class).name());
        for (Element method : methods) {
            System.out.println("    method: " + method);
        }

        final String actorClassName = actor.getAnnotation(Actor.class).name();
        final String actorInterfaceName = actor.getAnnotation(Actor.class).implementing();
        final String packageName = actor.getEnclosingElement().toString();

        List<MethodSpec> methodSpecList = new LinkedList<>();
        for (ExecutableElement method : methods) {
            System.out.println(method.getReturnType());
            List<String> param = new LinkedList<>();
            for (VariableElement v : method.getParameters()) {
                System.out.println(v.getSimpleName());
                System.out.println(v.getModifiers());
                System.out.println(v.getClass());

                ((LinkedList<String>) param).addLast(v.getSimpleName().toString());
            }

            MethodSpec methodSpec = MethodSpec.overriding(method)
                    .addCode("new Thread(new Runnable() { public void run() {\n" +
                            "mImpl." + method.getSimpleName() + "(" + String.join(",", param) + ");\n" +
                            "} }, \"another-thread\").start();\n")
                    .build();
            ((LinkedList<MethodSpec>) methodSpecList).addLast(methodSpec);
        }

        TypeSpec actorType = TypeSpec.classBuilder(actorClassName)
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(ClassName.bestGuess(actorInterfaceName))
                .addField(FieldSpec.builder(ClassName.bestGuess(actor.getSimpleName().toString()), "mImpl", Modifier.FINAL, Modifier.PRIVATE).build())
                .addMethods(methodSpecList)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder(ClassName.bestGuess(actor.getSimpleName().toString()), "impl").build())
                        .addCode(
                        "mImpl = impl;\n"
                        ).build())
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, actorType).build();
        try {
            JavaFileObject sourceFile =
                    processingEnv.getFiler().
                            createSourceFile(packageName + "." + actorClassName, actor);
            Writer writer = sourceFile.openWriter();
            writer.write(javaFile.toString());
            writer.close();

            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
