package io.buck.parser;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by buck on 2017/12/18
 */

public class MessageParserProcessor extends AbstractProcessor {

    private Map<String,ClassItem> classItemMap = new HashMap<>();
    private Set<String> messages = new HashSet<>();


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(Message.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        classItemMap.clear();
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(Message.class);
        for (Element element : annotatedElements) {
            if (!isElementValid(element)) return false;
            Message message = element.getAnnotation(Message.class);
            messages.add(message.value());
            ExecutableElement methodElement = (ExecutableElement) element;
            TypeElement classElement = (TypeElement) methodElement.getEnclosingElement();
            String methodName = methodElement.getSimpleName().toString();
            String classSimpleName = classElement.getSimpleName().toString();
            String classFullName = classElement.getQualifiedName().toString();
            ClassItem classItem = classItemMap.get(classFullName);
            if (classItem == null) {
                classItem = new ClassItem(classFullName, classSimpleName);
                classItemMap.put(classFullName, classItem);
            }
            MethodItem methodItem = new MethodItem(message.value(), methodName);
            classItem.addMethodItem(methodItem);
        }

        if (classItemMap.keySet().size() == 0) return false;

        StringBuilder sb = new StringBuilder();
        sb.append("package io.buck.parser;\n");
        sb.append("\n");
        for (String s : classItemMap.keySet()) {
            sb.append("import " + s + ";\n");
        }
        sb.append("\n");
        sb.append("public final class MessageParserProxy{\n");
        sb.append("\n");
        sb.append("    public static void parse(String msg, byte[] bytes){\n");
        sb.append("        switch(msg){\n");
        for (ClassItem classItem : classItemMap.values()) {
            sb.append("            // " + classItem.classSimpleName + "\n");
            for (MethodItem methodItem : classItem.methodItems) {
                sb.append("            case \"" + methodItem.message +"\":\n");
                sb.append("                "+classItem.classSimpleName+"."+methodItem.methodName+"(bytes);\n");
                sb.append("                break;\n");
            }
        }
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("}\n");


        try {
            JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile("MessageParserProxy");
            Writer writer = javaFileObject.openWriter();
            writer.write(sb.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            error(null,e.getMessage());
            return false;
        }
        return true;
    }

    private boolean isElementValid(Element element) {
        if (element.getKind() != ElementKind.METHOD) {
            error(element,"< @Message > must declared on method!");
            return false;
        }
        if (!element.getModifiers().contains(Modifier.STATIC)) {
            error(element,"< %s() > method must be static!",element.getSimpleName());
            return false;
        }
        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            error(element, "< %s() > method must be public!", element.getSimpleName());
            return false;
        }
        ExecutableElement methodElement = (ExecutableElement)element;
        List<? extends VariableElement> parameters = methodElement.getParameters();
        if (parameters.size() != 1) {
            error(element, "< %s() > method can only be one parameter!", element.getSimpleName());
            return false;
        } else {
            VariableElement variableElement = parameters.get(0);

            if (!variableElement.asType().toString().equals("byte[]")) {
                error(variableElement,"parameter must be one < byte[] >");
                return false;
            }
        }
        Message message = element.getAnnotation(Message.class);
        if (messages.contains(message.value())) {
            error(element,"< %s > message already exit!",message.value());
            return false;
        }
        return true;

    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
