package com.example.konyavic.compiler;

import com.example.konyavic.library.AbstractActorAdapter;
import com.example.konyavic.library.ActorClass;
import com.example.konyavic.library.ActorMethod;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;


@SupportedAnnotationTypes({
        "com.example.konyavic.library.ActorClass",
        "com.example.konyavic.library.ActorMethod"
})
public final class ActorProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("ActorProcessor#process");
        System.out.println("enter ActorProcessor");
        Map<Element, Collection<ExecutableElement>> actorMethodMap = new HashMap<>();

        Collection<? extends Element> actorElements = roundEnvironment.getElementsAnnotatedWith(ActorClass.class);
        for (Element e : actorElements) {
            System.out.println("# ActorClass:");
            System.out.println(e.getSimpleName());
            System.out.println(e.getKind());
            System.out.println(e.getEnclosedElements());
            System.out.println(e.getEnclosingElement());
        }

        Collection<? extends Element> actorMethodElements = roundEnvironment.getElementsAnnotatedWith(ActorMethod.class);
        for (Element e : actorMethodElements) {
            System.out.println("# ActorMethod:");
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
            generateActorAdapter(actor, actorMethodMap.get(actor));
        }

        return true;
    }

    void generateActorAdapter(Element actorClass, Collection<ExecutableElement> actorMethods) {
        System.out.println("build " + actorClass.getSimpleName());

        final String adaptorClassName = actorClass.getSimpleName() + "Adaptor";
        final String adaptorInterfaceName = actorClass.getSimpleName() + "Interface";
        final String adaptorPackageName = actorClass.getEnclosingElement().toString();

        LinkedList<MethodSpec> methodSpecList = new LinkedList<>();
        for (ExecutableElement method : actorMethods) {
            LinkedList<String> param = new LinkedList<>();
            for (VariableElement v : method.getParameters()) {
                param.addLast(v.getSimpleName().toString());
            }

            String returnStatement = method.getReturnType().getKind().equals(TypeKind.VOID)? "return" : "return null";
            MethodSpec methodSpec = MethodSpec.overriding(method)
                    .addCode(
                            "    mExecutorService.execute(new Runnable() {\n" +
                            "        public void run() {\n" +
                            "            getActor(" + actorClass.getSimpleName() + ".class)." + method.getSimpleName() + "(" + String.join(",", param) + ");\n" +
                            "        }\n" +
                            "    });\n" +
                            "    " + returnStatement + ";\n"
                    )
                    .build();
            methodSpecList.addLast(methodSpec);
        }

        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(TypeName.OBJECT, "actor", Modifier.FINAL).build())
                .addCode("super(actor);")
                .build();
        methodSpecList.addLast(constructorSpec);

        TypeSpec actorType = TypeSpec.classBuilder(adaptorClassName)
                .addModifiers(Modifier.FINAL)
                .superclass(ClassName.get(AbstractActorAdapter.class))
                .addSuperinterface(ClassName.bestGuess(adaptorInterfaceName))
                .addMethods(methodSpecList)
                .build();

        JavaFile javaFile = JavaFile.builder(adaptorPackageName, actorType).indent("    ").build();
        try {
            JavaFileObject sourceFile =
                    processingEnv.getFiler().
                            createSourceFile(adaptorPackageName + "." + adaptorClassName, actorClass);
            Writer writer = sourceFile.openWriter();
            writer.write(javaFile.toString());
            writer.close();

            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
