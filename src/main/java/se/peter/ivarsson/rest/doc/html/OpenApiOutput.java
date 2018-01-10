/*
 * Rest Documentation maven plugin.
 * 
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.html;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.logging.Logger;
import se.peter.ivarsson.rest.doc.parser.ClassInfo;
import se.peter.ivarsson.rest.doc.parser.DataModelInfo;
import se.peter.ivarsson.rest.doc.parser.FieldInfo;
import se.peter.ivarsson.rest.doc.parser.MethodInfo;
import se.peter.ivarsson.rest.doc.parser.RestDocHandler;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class OpenApiOutput {

    private static final Logger LOGGER = Logger.getLogger(OpenApiOutput.class.getSimpleName());

    private final HashMap<String, String> components = new HashMap<>();

    public void createOpenApiDocumantation(final File outputDirectory, final String projectTitle, final String openApiDocVersion, final String openApiLicenceName) {

        StringBuilder openApiBuffer = new StringBuilder("openapi: \"3.0.0\"\n");

        writeOpenApiInfo(openApiBuffer, projectTitle, openApiDocVersion, openApiLicenceName);

        writePaths(openApiBuffer);

        writeComponentsList(openApiBuffer);

        writeOpenApiToFile(outputDirectory, openApiBuffer, projectTitle);
    }

    private void writeOpenApiInfo(final StringBuilder openApiBuffer, final String projectTitle, final String openApiDocVersion, final String openApiLicenceName) {

        openApiBuffer.append("info:\n");
        openApiBuffer.append("  title: ");
        openApiBuffer.append(projectTitle);
        openApiBuffer.append("\n");
        openApiBuffer.append("  version: ");
        openApiBuffer.append(openApiDocVersion);
        openApiBuffer.append("\n");
        openApiBuffer.append("  license:\n");
        openApiBuffer.append("    name: ");
        openApiBuffer.append(openApiLicenceName);
        openApiBuffer.append("\n");
    }

    private void writePaths(final StringBuilder openApiBuffer) {

        openApiBuffer.append("paths:\n");

        RestDocHandler.restInfo.getClassInfo().stream()
                .filter(classInfo -> classInfo.getMethodInfo() != null)
                .forEach(classInfo -> {

                    addMethodPaths(openApiBuffer, classInfo);
                });
    }

    private void addMethodPaths(final StringBuilder openApiBuffer, final ClassInfo classinfo) {

        classinfo.getMethodInfo().stream()
                .forEach(methodInfo -> {

                    openApiBuffer.append("  ");

                    StringBuilder methodPath = new StringBuilder();

                    if (classinfo.getClassRootPath() != null) {

                        methodPath.append(classinfo.getClassRootPath());

                    } else {

                        if ((classinfo.getClassPath() != null) && !methodInfo.getRestPath().contains(classinfo.getClassPath())) {

                            methodPath.append(classinfo.getClassPath());
                        }
                    }

                    if (!methodInfo.getRestPath().isEmpty()) {

                        if (methodPath.length() > 0) {

                            if ((methodInfo.getRestPath().charAt(0) != '/') && (methodPath.charAt(methodPath.length() - 1) != '/')) {

                                openApiBuffer.append("/");
                            }

                            if ((methodInfo.getRestPath().charAt(0) == '/') && (methodPath.charAt(methodPath.length() - 1) == '/')) {

                                methodPath.append(methodInfo.getRestPath().substring(1));

                            }
                        } else {

                            methodPath.append(methodInfo.getRestPath());
                        }
                    }

                    if (methodPath.toString().endsWith("/")) {

                        openApiBuffer.append(methodPath.toString().substring(0, methodPath.toString().length() - 1));

                    } else {

                        openApiBuffer.append(methodPath);
                    }

                    openApiBuffer.append(":\n    ");
                    openApiBuffer.append(methodInfo.getHttpRequestType().toLowerCase());
                    openApiBuffer.append(":\n      ");
                    openApiBuffer.append("description: ");
                    openApiBuffer.append(onlyJavaDocComments(methodInfo.getJavaDoc()));
                    openApiBuffer.append("\n      parameters:");
                    methodInfo.getParameterInfo().stream()
                            .forEach(parameter -> {

                                switch (parameter.getParameterType()) {

                                    case "javax.ws.rs.PathParam":
                                        openApiBuffer.append("\n        name: ");
                                        openApiBuffer.append(parameter.getParameterAnnotationName());
                                        openApiBuffer.append("\n        in: ");
                                        openApiBuffer.append("path");
                                        break;

                                    case "javax.ws.rs.HeaderParam":
                                        openApiBuffer.append("\n        name: ");
                                        openApiBuffer.append(parameter.getParameterAnnotationName());
                                        openApiBuffer.append("\n        in: ");
                                        openApiBuffer.append("header");
                                        break;

                                    case "javax.ws.rs.QueryParam":
                                        openApiBuffer.append("\n        name: ");
                                        openApiBuffer.append(parameter.getParameterAnnotationName());
                                        openApiBuffer.append("\n        in: ");
                                        openApiBuffer.append("query");
                                        break;
                                }

                                String description = parameterCommentsFromJavaDoc(methodInfo.getJavaDoc().toLowerCase(), parameter.getParameterAnnotationName());

                                if (!description.isEmpty()) {

                                    openApiBuffer.append("\n        description: ");
                                    openApiBuffer.append(description);
                                }
                            });

                    if (!methodInfo.getRequestBodyClassName().isEmpty()) {

                        addRequestBodyInfo(openApiBuffer, methodInfo);
                    }

                    openApiBuffer.append("\n\n");
                });
    }

    private void addRequestBodyInfo(final StringBuilder openApiBuffer, final MethodInfo methodInfo) {

        openApiBuffer.append("\n      requestBody:");
        openApiBuffer.append("\n        description: ");
        openApiBuffer.append(methodInfo.getRequestBodyName());
        openApiBuffer.append("\n        content:\n          '");
        if (!methodInfo.getConsumeType().isEmpty()) {

            openApiBuffer.append(methodInfo.getConsumeType());
            openApiBuffer.append("':");

        } else {

            // No consume annotation set Json as default
            openApiBuffer.append("application/json':");
        }

        addOpenApiComponents(openApiBuffer, methodInfo);
    }

    private void addOpenApiComponents(final StringBuilder openApiBuffer, final MethodInfo methodInfo) {

        int lastDot = methodInfo.getRequestBodyClassName().lastIndexOf('.');

        if (lastDot > 0) {

            String componentName = methodInfo.getRequestBodyClassName().substring(lastDot + 1);

            openApiBuffer.append("\n             schema:");
            openApiBuffer.append("\n               $ref: '#/components/schemas/");
            openApiBuffer.append(componentName);
            openApiBuffer.append("'");

            addComponent(componentName, methodInfo.getRequestBodyClassName());
        }
    }

    private void addComponent(final String componentName, final String className) {

        StringBuilder component = new StringBuilder();
        
        component.append("    ");
        component.append(componentName);
        component.append(":");
        component.append("\n      properties:");
        
        DataModelInfo dataModelInfo = RestDocHandler.restInfo.getDomainDataMap().get(className);
        
        if(dataModelInfo != null) {
            
            dataModelInfo.getFields().stream()
                    .forEach(field -> {

                        component.append("\n        ");
                        component.append(field.getFieldName());
                        component.append(":");
                        component.append("\n          type: ");
                        String fieldType = mapFieldType(field.getFieldType());
                        component.append(fieldType);
                        if( fieldType.equals("array")) {

                            component.append("\n          items:");
                            component.append("\n            ");
                            component.append(mapFieldType(field.getListOfType()));
                        }
                    });

            components.put(componentName, component.toString());
        }
    }

    private void writeComponentsList(final StringBuilder openApiBuffer) {

        openApiBuffer.append("components:");
        openApiBuffer.append("\n  schemas:");

        components.forEach((key, value) -> {

            openApiBuffer.append("\n");
            openApiBuffer.append(value);
        });
    }

    private String mapFieldType(final String field) {

        switch (field) {

            case "int":
            case "java.lang.Integer":
                return "int32";

            case "long":
            case "java.lang.Long":
                return "int64";

            case "java.lang.String":
                return "string";

            case "boolean":
            case "java.lang.Boolean":
                return "boolean";

            case "java.lang.Double":
                return "double";
                
            case "java.lang.Float":
                return "float";

            case "java.util.Date":
                return "date";

            case "java.lang.Byte":
                return "byte";

            case "java.util.List":
                return "array";

            default:
                int lastDot = field.lastIndexOf('.');

                if (lastDot > 0) {

                    String componentName = field.substring(lastDot + 1);
                    
                    StringBuilder componentRef = new StringBuilder("$ref: '#/components/schemas/");

                    componentRef.append(componentName);
                    componentRef.append("'");

                    DataModelInfo restBodyDataModel = RestDocHandler.restInfo.getDomainDataMap().get(field);

                    addComponent(componentName, field);
                    
                    return componentRef.toString();
                    
                } else {
                    
                    return field;
                }
        }
    }

    private String onlyJavaDocComments(String javaDoc) {

        StringBuilder comment = new StringBuilder();

        boolean startOfCommentFound = false;
        boolean endOfCommentFound = false;

        int spacesFound = 0;

        byte[] javaDocArray = javaDoc.getBytes();
        char character = 0;

        for (int index = 0; (index < javaDocArray.length) && !endOfCommentFound; index++) {

            character = (char) javaDocArray[index];

            switch (character) {

                case '\r':
                case '\n':
                case '\f':
                case '@':
                    if (startOfCommentFound) {

                        // End comment capture
                        endOfCommentFound = true;
                    }
                    break;

                case '/':
                case '*':
                    if (startOfCommentFound) {

                        comment.append(character);
                    }
                    break;

                case '\t':
                case ' ':
                    if (startOfCommentFound) {

                        spacesFound++;

                        if (spacesFound == 1) {

                            comment.append(' ');
                        }
                    }
                    break;

                case ':':
                    if (startOfCommentFound) {

                        comment.append('.');
                    }
                    break;

                default:
                    // From '!' to '~' except charaters above
                    if ((character >= 0x21) && (character <= 0xFE)) {

                        comment.append(character);

                        startOfCommentFound = true;
                        spacesFound = 0;
                    }
                    break;
            }
        }

        return comment.toString();
    }

    private String parameterCommentsFromJavaDoc(final String javaDoc, String parameterName) {

        int startFirstAtIndex = javaDoc.indexOf("@param");

        while (startFirstAtIndex != -1) {

            int parameterNameIndex = javaDoc.indexOf(parameterName.toLowerCase(), startFirstAtIndex + 6);

            if (parameterNameIndex != -1) {

                int endDescriptionIndex = javaDoc.indexOf("*", parameterNameIndex);

                if (endDescriptionIndex != -1) {

                    int startDescription = parameterNameIndex + parameterName.length() + 1;

                    return javaDoc.substring(startDescription, endDescriptionIndex).replaceAll("\\*", "").trim();
                }
            }

            if (parameterNameIndex == -1) {

                startFirstAtIndex = javaDoc.indexOf("@param", startFirstAtIndex + 7);
            }
        }

        return "";
    }

    private void writeOpenApiToFile(final File outputDirectory, final StringBuilder openApiBuffer, final String projectTitle) {

        Path openApiPath = Paths.get(URI.create("file://" + outputDirectory.getAbsolutePath() + "/" + projectTitle.replaceAll("(\\s+|\"+)", "_") + ".yaml"));

        try {

            Files.write(openApiPath, openApiBuffer.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ioe) {

            LOGGER.severe("writeOpenApiToFile() Path ='" + openApiPath.toString() + "' IOException: " + ioe.getMessage());
        }
    }

}
