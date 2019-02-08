package com.example.konyavic.compiler;

import com.example.konyavic.library.AbstractActorAdapter;
import com.example.konyavic.library.ActorClass;
import com.example.konyavic.library.ActorMethod;
import com.example.konyavic.library.StageActor;
import com.example.konyavic.library.StageActorRef;
import com.example.konyavic.library.StageClass;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
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
        "com.example.konyavic.library.ActorMethod",
        "com.example.konyavic.library.StageActorRef",
        "com.example.konyavic.library.StageClass",
        "com.example.konyavic.library.StageActor"
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

        Map<Element, Collection<Element>> stageActorMap = new HashMap<>();
        Map<Element, Collection<Element>> stageActorRefMap = new HashMap<>();
        Map<Element, Collection<Element>> stageActorSyncedRefMap = new HashMap<>();

        Collection<? extends Element> stageClassElements = roundEnvironment.getElementsAnnotatedWith(StageClass.class);
        Collection<? extends Element> stageActorElements = roundEnvironment.getElementsAnnotatedWith(StageActor.class);
        Collection<? extends Element> stageActorRefElements = roundEnvironment.getElementsAnnotatedWith(StageActorRef.class);

        for (Element e : stageActorElements) {
            System.out.println(e.getSimpleName());
            Element parent = e.getEnclosingElement();
            Collection<Element> c;
            if (stageActorMap.containsKey(parent)) {
                c = stageActorMap.get(parent);
            } else {
                c = new LinkedList<>();
            }
            c.add(e);
            stageActorMap.put(parent, c);
        }

        for (Element e : stageActorRefElements) {
            Element parent = e.getEnclosingElement();
            if (e.getAnnotation(StageActorRef.class).sync()) {
                Collection<Element> c;
                if (stageActorSyncedRefMap.containsKey(parent)) {
                    c = stageActorSyncedRefMap.get(parent);
                } else {
                    c = new LinkedList<>();
                }
                c.add(e);
                stageActorSyncedRefMap.put(parent, c);
            } else {
                Collection<Element> c;
                if (stageActorRefMap.containsKey(parent)) {
                    c = stageActorRefMap.get(parent);
                } else {
                    c = new LinkedList<>();
                }
                c.add(e);
                stageActorRefMap.put(parent, c);
            }
        }

        for (Element stage : stageActorMap.keySet()) {
            System.out.println(stageActorRefMap.toString());
            System.out.println(stageActorSyncedRefMap.toString());
            generateStageScenario(stage, stageActorMap.get(stage), stageActorRefMap, stageActorSyncedRefMap);
        }


        return true;
    }

    private void generateActorAdapter(Element actorClass, Collection<ExecutableElement> actorMethods) {
        String actorClassName = actorClass.getSimpleName().toString();
        System.out.println("build " + actorClassName);

        final String adaptorClassName = actorClassName + "Adaptor";
        final String adaptorInterfaceName = actorClassName.substring(0, actorClassName.length()-5) + "Character";
        final String adaptorPackageName = actorClass.getEnclosingElement().toString();

        LinkedList<MethodSpec> methodSpecList = new LinkedList<>();
        LinkedList<MethodSpec> syncedStubMethodSpecList = new LinkedList<>();
        for (ExecutableElement method : actorMethods) {
            LinkedList<String> param = new LinkedList<>();
            for (VariableElement v : method.getParameters()) {
                param.addLast(v.getSimpleName().toString());

            }

            boolean isVoid = method.getReturnType().getKind().equals(TypeKind.VOID);

            // make async method stubs
            String returnStatement = isVoid ? "return" : "return null";
            MethodSpec methodSpec = MethodSpec.overriding(method)
                    .addCode("mExecutorService.execute(new Runnable() {\n" +
                            "    public void run() {\n" +
                            "        getActor()." + method.getSimpleName() + "(" + String.join(",", param) + ");\n" +
                            "    }\n" +
                            "});\n" +
                            "" + returnStatement + ";\n"
                    )
                    .build();
            methodSpecList.addLast(methodSpec);

            // make sync method stubs
            String futureReturnType = isVoid ? "Void" : method.getReturnType().toString();
            String methodCallString = "actor." + method.getSimpleName() + "(" + String.join(",", param) + ")";
            String methodBody = isVoid ? methodCallString + ";\n            return null": "return " + methodCallString;
            MethodSpec syncedMethodSpec = MethodSpec.overriding(method)
                    .addCode("try {\n" +
                            "    " + (isVoid ? "" : "return") + " syncFromAnotherActor(new SyncedCall<" + actorClassName + ", " + futureReturnType + ">() {\n" +
                            "        @Override\n" +
                            "        public " + futureReturnType + " run(" + actorClassName + " actor) {\n" +
                            "            " + methodBody + ";\n" +
                            "        }\n" +
                            "    }).get();\n" +
                            "} catch (Exception e) {\n" +
                            "    e.printStackTrace();\n" +
                            "} finally {\n" +
                            (isVoid ? "    return;\n" : "    return null;\n") +
                            "}\n"
                    )
                    .build();
            syncedStubMethodSpecList.addLast(syncedMethodSpec);
        }

        // make constructor
        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(ClassName.get(actorClass.asType()), "actor", Modifier.FINAL).build())
                .addCode("super(actor);\n")
                .build();
        methodSpecList.addLast(constructorSpec);

        // make inner class
        String stubName = adaptorClassName + "SyncedStub";
        TypeSpec actorStubType = TypeSpec.classBuilder(stubName)
                .addModifiers(Modifier.FINAL, Modifier.PRIVATE)
                .addSuperinterface(ClassName.bestGuess(adaptorInterfaceName))
                .addMethods(syncedStubMethodSpecList)
                .build();

        // make synchronous stub
        MethodSpec syncedStubSpec = MethodSpec.methodBuilder("getSyncedStub")
                .addCode("return new " + stubName + "();\n")
                .returns(ClassName.bestGuess(adaptorInterfaceName))
                .build();
        methodSpecList.addLast(syncedStubSpec);

        // make class
        TypeSpec actorType = TypeSpec.classBuilder(adaptorClassName)
                .addModifiers(Modifier.FINAL)
                .superclass(ClassName.bestGuess("AbstractActorAdapter<" + actorClassName + ">"))
                .addSuperinterface(ClassName.bestGuess(adaptorInterfaceName))
                .addMethods(methodSpecList)
                .addType(actorStubType)
                .addField(AbstractActorAdapter.class, "dummy", Modifier.PRIVATE)
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

    void generateStageScenario(Element stageClass,
                               Collection<Element> stageActors,
                               Map<Element, Collection<Element>> stageActorRefMap,
                               Map<Element, Collection<Element>> stageActorSyncedRefMap) {
        final String stagePackageName = stageClass.getEnclosingElement().toString();

        LinkedList<FieldSpec> fieldSpecLinkedList = new LinkedList<>();
        String initializingMemberStatement = "";
        String initializingRefStatement = "";
        for (Element e : stageActors) {
            String interfaceClassName = e.getAnnotation(StageActor.class).playing();
            String fieldName = "m" + ClassName.bestGuess(interfaceClassName).simpleName();
            String paramName = e.toString();
            ClassName adapterClass = ClassName.bestGuess(e.asType() + "Adaptor");
            FieldSpec fieldSpec = FieldSpec.builder(ClassName.bestGuess(interfaceClassName),
                    fieldName,
                    Modifier.PUBLIC,
                    Modifier.FINAL)
                    .build();
            fieldSpecLinkedList.addLast(fieldSpec);

            String syncedFieldName = "mSynced" + ClassName.bestGuess(interfaceClassName).simpleName();
            FieldSpec syncedFieldSpec = FieldSpec.builder(ClassName.bestGuess(interfaceClassName),
                    syncedFieldName,
                    Modifier.PUBLIC,
                    Modifier.FINAL)
                    .build();
            fieldSpecLinkedList.addLast(syncedFieldSpec);

            initializingMemberStatement += fieldName + " = new " + adapterClass.simpleName() + "(stage." + paramName + ");\n";
            initializingMemberStatement += syncedFieldName + " = ((" + adapterClass.simpleName() + ")" + fieldName + ").getSyncedStub();\n";

            String className = ClassName.bestGuess(e.asType().toString()).simpleName();
            for (Element enclosingActor : stageActorRefMap.keySet()) {
                if (enclosingActor.getSimpleName().toString().equals(className)) {
                    Collection<Element> refs = stageActorRefMap.get(enclosingActor);
                    for (Element r : refs) {
                        String interfaceName = ClassName.bestGuess(r.asType().toString().split("<")[1].split(">")[0]).simpleName();
                        initializingRefStatement += "stage." + paramName + "." + r.getSimpleName() + " = new WeakReference<" + interfaceName + ">(m" + interfaceName + ");\n";
                    }
                }
            }

            for (Element enclosingActor : stageActorSyncedRefMap.keySet()) {
                if (enclosingActor.getSimpleName().toString().equals(className)) {
                    Collection<Element> refs = stageActorSyncedRefMap.get(enclosingActor);
                    for (Element r : refs) {
                        String interfaceName = ClassName.bestGuess(r.asType().toString().split("<")[1].split(">")[0]).simpleName();
                        initializingRefStatement += "stage." + paramName + "." + r.getSimpleName() + " = new WeakReference<" + interfaceName + ">(mSynced" + interfaceName + ");\n";
                    }
                }
            }
        }

        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                .addParameter(ClassName.bestGuess(stageClass.getSimpleName().toString()), "stage", Modifier.FINAL)
                .addCode(initializingMemberStatement)
                .addCode(initializingRefStatement)
                .build();

        String scenarioClassName = stageClass.getSimpleName().toString() + "Scenario";
        TypeSpec scenarioType = TypeSpec.classBuilder(scenarioClassName)
                .addFields(fieldSpecLinkedList)
                .addField(WeakReference.class, "dummy")
                .addMethod(constructorSpec)
                .build();

        JavaFile javaFile = JavaFile.builder(stagePackageName, scenarioType).indent("    ").build();
        try {
            JavaFileObject sourceFile =
                    processingEnv.getFiler().
                            createSourceFile(stagePackageName + "." + scenarioClassName, stageClass);
            Writer writer = sourceFile.openWriter();
            writer.write(javaFile.toString());
            writer.close();

            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
