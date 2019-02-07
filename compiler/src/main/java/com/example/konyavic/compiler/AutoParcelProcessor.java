package com.example.konyavic.compiler;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;

import com.example.konyavic.library.AutoParcel;

@SupportedAnnotationTypes("com.example.konyavic.library.AutoParcel")
public final class AutoParcelProcessor extends AbstractProcessor {

    @Override
    public boolean process(
            Set<? extends TypeElement> annotations,
            RoundEnvironment env) {
        System.out.println("enter AutoParcelProcessor");
        for (TypeElement t : annotations) {
            System.out.println(t);
        }

        Collection<? extends Element> annotatedElements =
                env.getElementsAnnotatedWith(AutoParcel.class);
        List<TypeElement> types =
                new ArrayList<TypeElement>(ElementFilter.typesIn(annotatedElements));

        for (TypeElement type : types) {
            processType(type);
        }

        // We are the only ones handling AutoParcel annotations
        return true;
    }

    private void processType(TypeElement type) {
        String className = "com.example.konyavic.testannotationprocessing."+type.getSimpleName()+"Generated";
        String source = "package com.example.konyavic.testannotationprocessing;\n" +
                "class " + type.getSimpleName() + "Generated" + " {\n" +
                "    public static String hello() {\n" +
                "        return \"hello from " + className + "\";\n" +
                "    }\n" +
                "}\n";
        writeSourceFile(className, source, type);
    }

    private void writeSourceFile(
            String className,
            String text,
            TypeElement originatingType) {

        try {
            JavaFileObject sourceFile =
                    processingEnv.getFiler().
                            createSourceFile(className, originatingType);

            Writer writer = sourceFile.openWriter();
            try {
                writer.write(text);
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            System.err.print(e.getLocalizedMessage());
        }
        System.out.println("generated: " + className);
    }
}