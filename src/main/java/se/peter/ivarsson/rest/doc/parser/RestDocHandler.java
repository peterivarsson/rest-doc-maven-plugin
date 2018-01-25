/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.parser;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import se.peter.ivarsson.rest.doc.sourceParser.JavaSourceParser;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class RestDocHandler {

    private static final Logger LOGGER = Logger.getLogger(RestDocHandler.class.getSimpleName());

    private URLClassLoader urlClassLoader;

    private final JavaSourceParser javaSourceParser = new JavaSourceParser();

    private final HashMap<String, String> javaDocComments = new HashMap<>();
    private final HashMap<String, String> enumTypes = new HashMap<>();
    private final HashMap<String, ResponseType> responseTypes = new HashMap<>();
    private final HashMap<String, PathInfo> classPaths = new HashMap<>();
    private final HashMap<String, String> constants = new HashMap<>();

    public static RestInfo restInfo = new RestInfo();

    /**
     * Used by the write documentation classes
     */
    public static Logger getLogger() {

        return LOGGER;
    }

    /**
     *
     */
    public RestDocHandler(final File classesDirectory, final File sourceDirectory, final File loggingDirectory) {

        addLoggingFileHandler(loggingDirectory);

        LOGGER.info("REST documentation STARTED analyzing");

        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();

        try {

            // Add the classesDirectory dir to the classpath
            urlClassLoader = new URLClassLoader(new URL[]{classesDirectory.toURI().toURL()}, currentThreadClassLoader);

        } catch (MalformedURLException mue) {

            LOGGER.severe("MalformedURLException: " + mue.getMessage());
        }

        try {

            Files.walk(Paths.get(sourceDirectory.toURI()))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {

                        LOGGER.info("Source path: " + path);

                        if (path.toString().endsWith(".java")) {

                            javaSourceParser.parseSourceFile(sourceDirectory, enumTypes, responseTypes, classPaths, constants, path, urlClassLoader);
                        }
                    });

            Files.walk(Paths.get(classesDirectory.toURI()))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {

                        LOGGER.info("Class path: " + path);

                        if (path.toString().endsWith(".class")) {

                            checkClassFilesForPathAnnotations(classesDirectory, path, sourceDirectory);
                        }
                    });

            restInfo.getClassInfo().stream()
                    .forEach(classinfo -> {

                        updatePaths(classinfo);
                        updateParameters(classinfo);
                    });

        } catch (IOException ioe) {

            LOGGER.severe("IOException reading war file: " + ioe.getMessage());

            ioe.printStackTrace();
        }

        LOGGER.info("REST documentation ENDED analyzing\n");

        LOGGER.info(restInfo.toString());
    }

    private void checkClassFilesForPathAnnotations(final File classesDirectory, final Path classNamePath, final File sourceDirectory) {

        LOGGER.info("Class file: " + classNamePath.getFileName().toString());

        ClassInfo classInfo = getFullClassNameFromClassesDir(classesDirectory, classNamePath);

        if (classInfo == null) {

            // Skipp this file
            return;
        }

        try {

            javaSourceParser.parseClassForJavaDocComments(sourceDirectory, javaDocComments, classInfo.getPackageAndClassName());

            Class clazz = urlClassLoader.loadClass(classInfo.getPackageAndClassName());

            Annotation[] annotations = clazz.getAnnotations();

            for (Annotation annotation : annotations) {

                if (annotation instanceof javax.ws.rs.Path) {

                    String pathValue = ((javax.ws.rs.Path) annotation).value();

                    // We found a class with Path annotation
                    addClassInfoToRestInfoList(classInfo, (javax.ws.rs.Path) annotation);
                }
            }

            checkClassMethodsForPathInformation(classInfo, clazz, javaDocComments);

        } catch (ClassNotFoundException cnfe) {

            LOGGER.severe("checkClassFilesForPathAnnotations, ClassNotFoundException: " + cnfe.getMessage());

        } catch (NoClassDefFoundError ncdfe) {

            LOGGER.severe("checkClassFilesForPathAnnotations, NoClassDefFoundError: " + ncdfe.getMessage());

        }
    }

    private ClassInfo getFullClassNameFromClassesDir(final File classesDirectory, final Path classNamePath) {

        String pathName;
        String className = "";
        int classIndex;

        StringBuilder packetAndclassName = new StringBuilder();

        boolean addDot = false;
        boolean addPackageName = false;

        int pathCount = classNamePath.getNameCount();

        for (int i = 0; i < pathCount; i++) {

            if (addDot == true) {

                packetAndclassName.append(".");
            }

            pathName = classNamePath.getName(i).toString();

            if (addPackageName == true) {

                classIndex = pathName.indexOf(".class");

                if (classIndex > 0) {

                    className = pathName.substring(0, classIndex);

                    packetAndclassName.append(className);

                } else {

                    packetAndclassName.append(pathName);
                    addDot = true;
                }
            } else if (pathName.equals(classesDirectory.getName())) {

                addPackageName = true;
            }
        }

        if (className.contains("$")) {

            // Probarbly an enum value
            return null;
        }

        ClassInfo classInfo = new ClassInfo();

        classInfo.setClassName(className);
        classInfo.setPackageAndClassName(packetAndclassName.toString());

        return classInfo;
    }

    private void addClassInfoToRestInfoList(final ClassInfo classInfo, final javax.ws.rs.Path annotation) {

        String pathValue = annotation.value();

        classInfo.setClassPath(pathValue);

        restInfo.getClassInfo().add(classInfo);
    }

    private void checkClassMethodsForPathInformation(final ClassInfo classInfo, final Class clazz, final HashMap<String, String> javaDocComments) {

        try {

            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {

                Annotation[] methodAnnotations = method.getAnnotations();

                for (Annotation annotation : methodAnnotations) {

                    if (annotation instanceof javax.ws.rs.Path) {

                        // We found a method with Path annotation
                        if (checkIfMethodsHasHttpRequestType(methodAnnotations)) {

                            // We found a method with a 'Http request type'
                            addMethodInfoToRestInfoList(classInfo, (javax.ws.rs.Path) annotation, method, javaDocComments);
                        }
                    }
                }
            }
        } catch (Exception cnfe) {

            LOGGER.info(cnfe.getMessage());
        }
    }

    // Only add methods that has 'Http request types'
    private boolean checkIfMethodsHasHttpRequestType(final Annotation[] methodAnnotations) {

        LOGGER.info("checkIfMethodsHasHttpRequestType()");

        for (Annotation annotation : methodAnnotations) {

            if (annotation instanceof javax.ws.rs.GET) {

                return true;

            } else if (annotation instanceof javax.ws.rs.POST) {

                return true;

            } else if (annotation instanceof javax.ws.rs.PUT) {

                return true;

            } else if (annotation instanceof javax.ws.rs.DELETE) {

                return true;
            }
        }

        return false;
    }

    private void addMethodInfoToRestInfoList(final ClassInfo classInfo, final javax.ws.rs.Path annotation, final Method method, final HashMap<String, String> javaDocComments) {

        if (classInfo.getClassPath() == null) {

            // Add to restInfoList
            classInfo.setClassPath("");

            restInfo.getClassInfo().add(classInfo);
        }

        List<MethodInfo> methodInfoList = classInfo.getMethodInfo();

        if (methodInfoList == null) {

            classInfo.setMethodInfo(new ArrayList<>());
        }

        StringBuilder restPath = new StringBuilder();

        if (!annotation.value().isEmpty() && (annotation.value().charAt(0) != '/')) {

            restPath.append('/');
        }

        if (annotation.value().endsWith("/")) {

            restPath.append(annotation.value().substring(0, annotation.value().length() - 1));

        } else {

            restPath.append(annotation.value());
        }

        ReturnInfo returnInfo = new ReturnInfo();
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setReturnInfo(returnInfo);

        methodInfo.setMethodName(method.getName());
        methodInfo.setMethodPath(restPath.toString());

        addMethodsPathMethod(methodInfo, returnInfo, method);
        addMethodReturnType(returnInfo, method, classInfo.getPackageAndClassName());
        addMethodParameters(methodInfo, method);

        String javaDocMethodComments = javaDocComments.get(method.getName());

        if (javaDocMethodComments != null) {

            methodInfo.setJavaDoc(javaDocMethodComments);
        }

        classInfo.getMethodInfo().add(methodInfo);
    }

    private void addMethodsPathMethod(final MethodInfo methodInfo, final ReturnInfo returnInfo, final Method method) {

        StringBuilder producesTypes = new StringBuilder();
        boolean firstProduceType = true;
        StringBuilder consumeTypes = new StringBuilder();
        boolean firstConsumeType = true;

        LOGGER.info("Method: " + method.toGenericString());

        Annotation[] methodAnnotations = method.getAnnotations();

        for (Annotation annotation : methodAnnotations) {

            LOGGER.info("Method Annotation: " + annotation.annotationType().toGenericString());

            if (annotation instanceof javax.ws.rs.GET) {

                methodInfo.setHttpRequestType("GET");

            } else if (annotation instanceof javax.ws.rs.POST) {

                methodInfo.setHttpRequestType("POST");

            } else if (annotation instanceof javax.ws.rs.PUT) {

                methodInfo.setHttpRequestType("PUT");

            } else if (annotation instanceof javax.ws.rs.DELETE) {

                methodInfo.setHttpRequestType("DELETE");

            } else if (annotation instanceof java.lang.Deprecated) {

                methodInfo.setDeprecated(true);

            } else if (annotation instanceof javax.ws.rs.Produces) {

                javax.ws.rs.Produces produces = (javax.ws.rs.Produces) annotation;

                for (String returnType : produces.value()) {

                    if (firstProduceType == true) {

                        firstProduceType = false;

                    } else {

                        producesTypes.append(", ");
                    }

                    producesTypes.append(returnType);
                }

                methodInfo.setProducesType(producesTypes.toString());

            } else if (annotation instanceof javax.ws.rs.Consumes) {

                javax.ws.rs.Consumes consumes = (javax.ws.rs.Consumes) annotation;

                for (String consumeType : consumes.value()) {

                    if (firstConsumeType == true) {

                        firstConsumeType = false;

                    } else {

                        consumeTypes.append(", ");
                    }

                    consumeTypes.append(consumeType);
                }

                methodInfo.setConsumeType(consumeTypes.toString());

            } else if (annotation instanceof se.peter.ivarsson.rest.doc.rest.type.DocReturnType) {

                se.peter.ivarsson.rest.doc.rest.type.DocReturnType returnType = (se.peter.ivarsson.rest.doc.rest.type.DocReturnType) annotation;

                addAnnotatedReturnType(returnInfo, returnType.key());
            }
        }
    }

    private void addMethodReturnType(final ReturnInfo returnInfo, final Method method, final String className) {

        String returnTypeName = null;
        String responseTypesKey = className + '-' + method.getName();

        if (responseTypes.containsKey(responseTypesKey)) {

            ResponseType responseType = responseTypes.get(responseTypesKey);

            returnTypeName = responseType.getReturnType();

            if (returnTypeName == null) {

                returnTypeName = method.getReturnType().getName();
            }

            returnInfo.setReturnClassName(returnTypeName);
            returnInfo.setReturnStatus(responseType.getReturnStatus());
            returnInfo.setReturnStatusCode(responseType.getReturnStatusCode());

        } else {

            returnTypeName = method.getReturnType().getName();

            returnInfo.setReturnClassName(returnTypeName);
        }

        if (isDomainData(returnTypeName)) {

            DataModelInfo dataModelInfo = addDomainDataInfo(returnTypeName);
        }
    }

    private void addAnnotatedReturnType(final ReturnInfo returnInfo, final String returnTypeClassName) {

        returnInfo.setAnnotatedReturnType(returnTypeClassName);

        addDomainDataInfo(returnTypeClassName);
    }

    private DataModelInfo addDomainDataInfo(String className) {

        if (className.equals("void")) {

            return null;  // Skipp this
        }

        int listStartIndex = className.indexOf("List<");

        if (listStartIndex != -1) {

            int listEndIndex = className.indexOf(">", listStartIndex);

            if (listEndIndex != -1) {

                className = className.substring(listStartIndex + 5, listEndIndex).trim();
            }
        }

        DataModelInfo domainData = restInfo.getDomainDataMap().get(className);

        if (domainData != null) {

            // This data already exists
            LOGGER.info(className + " already exists in domain data");
            return null;
        }

        LOGGER.info("Add " + className + " to domain data");

        HashSet<String> addDomainDataSet = new HashSet();

        DataModelInfo dataModelInfo = new DataModelInfo();

        try {

            Class clazz = urlClassLoader.loadClass(className);

            Method[] methods = clazz.getMethods();

            boolean enumFieldAdded = false;

            for (Method method : methods) {

                String fieldType = method.getReturnType().getName();

                if (fieldType.startsWith("[L")) {

                    continue;
                }

                FieldInfo fieldInfo = new FieldInfo();

                if (isGetter(method)) {

                    if (!fieldType.equals("java.lang.Class")) {

                        int startIndex = 0;

                        if (method.getName().startsWith("get")) {

                            startIndex = 3;

                        } else {

                            startIndex = 2;
                        }

                        char fieldName[] = method.getName().substring(startIndex).toCharArray();
                        fieldName[0] = Character.toLowerCase(fieldName[0]);

                        fieldInfo.setFieldName(new String(fieldName));
                        fieldInfo.setFieldType(fieldType);

                        for (Annotation annotation : method.getAnnotations()) {

                            String name = annotation.toString();

                            if (annotation instanceof se.peter.ivarsson.rest.doc.rest.type.DocListType) {

                                se.peter.ivarsson.rest.doc.rest.type.DocListType methodParam = (se.peter.ivarsson.rest.doc.rest.type.DocListType) annotation;

                                String annotationKey = methodParam.key();

                                fieldInfo.setFieldOfType(annotationKey);

                                // Add this annotation data to domain data list
                                addDomainDataSet.add(annotationKey);
                            }
                        }

                        if (fieldInfo.getFieldOfType().isEmpty()) {

                            if (fieldType.equals("java.util.List")
                                    || fieldType.endsWith("Set")) {

                                // "java.util.List<java.lang.String>"
                                // "java.util.???Set<java.lang.String>"
                                String genericReturnTypeName = method.getGenericReturnType().getTypeName();
                                int startListType = genericReturnTypeName.indexOf('<');
                                int endListType = genericReturnTypeName.indexOf('>');
                                String type = genericReturnTypeName.substring(startListType + 1, endListType);
                                fieldInfo.setFieldOfType(type);

                                if (isDomainData(type)) {

                                    addDomainDataSet.add(type);
                                }
                            } else {

                                if (isDomainData(fieldType)) {

                                    addDomainDataSet.add(fieldType);

                                    if (enumTypes.containsKey(fieldType)) {

                                        // Found enum in set
                                        int lastDotIndex = fieldType.lastIndexOf('.');

                                        if (lastDotIndex != -1) {

                                            fieldInfo.setFieldName(fieldType.substring(lastDotIndex + 1));
                                            fieldInfo.setFieldType("enum");
                                            fieldInfo.setFieldOfType(enumTypes.get(fieldType));
                                        }
                                    }
                                }
                            }
                        } else {

                            // ListOfType not empty
                            if (isDomainData(fieldInfo.getFieldOfType())) {

                                if (enumTypes.containsKey(fieldInfo.getFieldOfType())) {

                                    // Found enum in set
                                    int lastDotIndex = fieldInfo.getFieldOfType().lastIndexOf('.');

                                    if (lastDotIndex != -1) {

                                        fieldInfo.setFieldName(fieldInfo.getFieldOfType().substring(lastDotIndex + 1));
                                        fieldInfo.setFieldType("enum");
                                        fieldInfo.setFieldOfType(enumTypes.get(fieldInfo.getFieldOfType()));
                                    }
                                }
                            }
                        }

                        dataModelInfo.getFields().add(fieldInfo);
                    }
                } else {

                    // NO getter methods exists, check if enum is needed to be added
                    if (isDomainData(className) && !enumFieldAdded) {

                        if (enumTypes.containsKey(className)) {

                            // Found enum in set
                            int lastDotIndex = className.lastIndexOf('.');

                            if (lastDotIndex != -1) {

                                fieldInfo.setFieldName(className.substring(lastDotIndex + 1));
                                fieldInfo.setFieldType("enum");
                                fieldInfo.setFieldOfType(enumTypes.get(className));

                                dataModelInfo.getFields().add(fieldInfo);

                                enumFieldAdded = true;
                            }
                        }
                    }
                }
            }

            if (dataModelInfo.getFields().isEmpty()) {

                if (enumTypes.containsKey(className)) {

                    dataModelInfo.setInfo("enum " + enumTypes.get(className));

                } else {

                    dataModelInfo.setInfo("No fields found in this class");
                }
            }

            restInfo.getDomainDataMap().put(className, dataModelInfo);

            addDomainDataSet.stream()
                    .forEach(domaindata -> {

                        addDomainDataInfo(domaindata.toString());
                    });

        } catch (ClassNotFoundException cnfe) {

            LOGGER.severe("addDomainDataInfo, ClassNotFoundException: " + cnfe.getMessage());

            return null;

        } catch (NoClassDefFoundError ncdfe) {

            LOGGER.severe("addDomainDataInfo, NoClassDefFoundError: " + ncdfe.getMessage());
            dataModelInfo.setInfo("ERROR: Reflection failed to get methods info, NoClassDefFoundError: " + ncdfe.getMessage());
            restInfo.getDomainDataMap().put(className, dataModelInfo);
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.setFieldName("error");
            fieldInfo.setFieldType("error");
            dataModelInfo.getFields().add(fieldInfo);
        }

        return dataModelInfo;
    }

    private void addMethodParameters(final MethodInfo methodInfo, final Method method) {

        ParameterInfo parameterInfo;

        if (methodInfo.getParameterInfo() == null) {

            methodInfo.setParameterInfo(new ArrayList<>());
        }

        for (Parameter parameter : method.getParameters()) {

            Annotation[] annotations = parameter.getAnnotations();

            for (Annotation annotation : annotations) {

                if (annotation instanceof javax.ws.rs.PathParam) {

                    javax.ws.rs.PathParam pathParam = (javax.ws.rs.PathParam) annotation;

                    parameterInfo = new ParameterInfo();

                    parameterInfo.setParameterType("javax.ws.rs.PathParam");
                    parameterInfo.setParameterAnnotationName(pathParam.value());
                    parameterInfo.setParameterClassName(parameter.getParameterizedType().getTypeName());

                    methodInfo.getParameterInfo().add(parameterInfo);

                } else if (annotation instanceof javax.ws.rs.HeaderParam) {

                    javax.ws.rs.HeaderParam headerParam = (javax.ws.rs.HeaderParam) annotation;

                    parameterInfo = new ParameterInfo();

                    parameterInfo.setParameterType("javax.ws.rs.HeaderParam");
                    parameterInfo.setParameterAnnotationName(headerParam.value());
                    parameterInfo.setParameterClassName(parameter.getParameterizedType().getTypeName());

                    methodInfo.getParameterInfo().add(parameterInfo);

                } else if (annotation instanceof javax.ws.rs.QueryParam) {

                    javax.ws.rs.QueryParam queryParam = (javax.ws.rs.QueryParam) annotation;

                    parameterInfo = new ParameterInfo();

                    parameterInfo.setParameterType("javax.ws.rs.QueryParam");
                    parameterInfo.setParameterAnnotationName(queryParam.value());
                    parameterInfo.setParameterClassName(parameter.getParameterizedType().getTypeName());

                    methodInfo.getParameterInfo().add(parameterInfo);
                }
            }

            if (annotations.length == 0) {

                // This parameter has no annotation
                parameterInfo = new ParameterInfo();

                if (methodInfo.getConsumeType().isEmpty()) {

                    parameterInfo.setParameterType("-");

                } else {

                    parameterInfo.setParameterType(methodInfo.getConsumeType());
                }

                if (parameter.getName().startsWith("arg")) {

                    switch (parameter.getName().charAt(3)) {

                        case '0':
                            parameterInfo.setParameterAnnotationName("First argument");
                            break;

                        case '1':
                            parameterInfo.setParameterAnnotationName("Second argument");
                            break;

                        case '2':
                            parameterInfo.setParameterAnnotationName("Third argument");
                            break;

                        case '3':
                            parameterInfo.setParameterAnnotationName("Fourth argument");
                            break;

                        case '4':
                            parameterInfo.setParameterAnnotationName("Fifth argument");
                            break;

                        case '5':
                            parameterInfo.setParameterAnnotationName("Sixth argument");
                            break;

                        case '6':
                            parameterInfo.setParameterAnnotationName("Seventh argument");
                            break;

                        case '7':
                            parameterInfo.setParameterAnnotationName("Eighth argument");
                            break;

                        case '8':
                            parameterInfo.setParameterAnnotationName("Ninth argument");
                            break;

                        case '9':
                            parameterInfo.setParameterAnnotationName("Tenth argument");
                            break;

                        default:
                            parameterInfo.setParameterAnnotationName("-");
                            break;
                    }
                } else {

                    parameterInfo.setParameterAnnotationName(parameter.getName());
                }

                parameterInfo.setParameterClassName(parameter.getType().getName());

                methodInfo.getParameterInfo().add(parameterInfo);

                methodInfo.setRequestBodyName(parameterInfo.getParameterAnnotationName());
                methodInfo.setRequestBodyClassName(parameterInfo.getParameterClassName());

                addDomainDataInfo(parameter.getType().getName());

                LOGGER.info("Parameter without annotation: " + parameter.getName() + " Type: " + parameter.getType().getName());
            }
        }
    }

    private boolean isGetter(final Method method) {

        if (!(method.getName().startsWith("get")
                || method.getName().startsWith("is"))) {
            return false;
        }

        if (method.getParameterTypes().length != 0) {
            return false;
        }

        return !method.getReturnType().equals(void.class);
    }

    private boolean isDomainData(final String parameterName) {

        // Check for 'Java classes' or 'Primitive Data Types'
        if (parameterName.startsWith("java")) {

            // Is a Java class
            return false;

        } else if (parameterName.equals("byte")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("short")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("int")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("long")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("float")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("double")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("boolean")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("char")) {

            // Is a Primitive Data Type
            return false;
        }

        return true;
    }

    /*
     * Update class root path
     */
    private void updatePaths(ClassInfo classInfo) {

        if (classPaths.containsKey(classInfo.getPackageAndClassName())) {

            List<String> paths = new ArrayList<>();

            PathInfo pathInfo = classPaths.get(classInfo.getPackageAndClassName());

            // Add Class path 
            paths.add(pathInfo.getClassPath().replace("^/", ""));

            while (!pathInfo.getParentPath().isEmpty()) {

                pathInfo = classPaths.get(pathInfo.getParentPath());

                if (pathInfo == null) {

                    // Parent not found
                    break;
                }

                paths.add(pathInfo.getClassPath().replace("^/", ""));
            }

            String totalPath = "/";

            for (int index = paths.size() - 1; index >= 0; index--) {

                totalPath = totalPath + paths.get(index);
            }

            if (totalPath.endsWith("/")) {

                classInfo.setClassRootPath(totalPath.substring(0, totalPath.length() - 1));

            } else {

                classInfo.setClassRootPath(totalPath);
            }
        }
    }

    /*
     * Update parameters with parameters from class root path
     */
    private void updateParameters(ClassInfo classInfo) {

        final List<String> rootPathParameters = new ArrayList<>();

        final String classRootPath = classInfo.getClassRootPath();

        if (!classRootPath.isEmpty()) {

            int startParameterIndex = classInfo.getClassRootPath().indexOf('{');

            if (startParameterIndex != -1) {

                int endParameterIndex = classInfo.getClassRootPath().indexOf('}', startParameterIndex);

                if (endParameterIndex != -1) {

                    while ((startParameterIndex != -1) && (endParameterIndex != -1)) {

                        String parameter = classRootPath.substring(startParameterIndex + 1, endParameterIndex).trim();

                        rootPathParameters.add(parameter);

                        startParameterIndex = classInfo.getClassRootPath().indexOf('{', endParameterIndex);

                        if (startParameterIndex != -1) {

                            endParameterIndex = classInfo.getClassRootPath().indexOf('}', startParameterIndex);
                        }
                    }
                }
            }

            if (!rootPathParameters.isEmpty()) {

                List<ParameterInfo> rootParameterInfo = new ArrayList<>();

                rootPathParameters.stream()
                        .forEach(parameter -> {

                            ParameterInfo parameterInfo = new ParameterInfo();

                            parameterInfo.setParameterAnnotationName(parameter);
                            parameterInfo.setParameterClassName("java.lang.String");
                            parameterInfo.setParameterType("javax.ws.rs.PathParam");

                            rootParameterInfo.add(parameterInfo);
                        });

                classInfo.getMethodInfo().stream()
                        .forEach(methodInfo -> {

                            List<ParameterInfo> backupOldParameterInfo = methodInfo.getParameterInfo();

                            List<ParameterInfo> newParameterInfo = new ArrayList<>();

                            newParameterInfo.addAll(rootParameterInfo);
                            newParameterInfo.addAll(backupOldParameterInfo);

                            methodInfo.setParameterInfo(newParameterInfo);
                        });
            }
        }
    }

    private void addLoggingFileHandler(final File loggingDirectory) {

        try {

            String logFilePath = loggingDirectory.getAbsolutePath() + "/RestDoc.log";

            int maxSizeOfTheLogFile = 1_000_000;
            int maxNumberOfLogFiles = 10;

            FileHandler fileHandler = new FileHandler(logFilePath, maxSizeOfTheLogFile, maxNumberOfLogFiles);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);
            LOGGER.addHandler(fileHandler);

        } catch (IOException ioe) {

            System.out.println(ioe.getMessage());
        }
    }
}
