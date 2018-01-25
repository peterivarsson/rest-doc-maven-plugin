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
import se.peter.ivarsson.rest.doc.parser.ClassInfo;
import se.peter.ivarsson.rest.doc.parser.DataModelInfo;
import se.peter.ivarsson.rest.doc.parser.MethodInfo;
import se.peter.ivarsson.rest.doc.parser.OpenApiField;
import se.peter.ivarsson.rest.doc.parser.ParameterInfo;
import se.peter.ivarsson.rest.doc.parser.RestDocHandler;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class OpenApiOutput {

    final String NO_COLLECTION = "";

    private final HashMap<String, String> components = new HashMap<>();
    private final HashMap<String, String> methodPaths = new HashMap<>();

    public void createOpenApiDocumantation(final File outputDirectory, final String projectTitle, final String openApiDocVersion,
            final String openApiLicenceName, final String openApiDevelopmentServerUrl,
            final String openApiStagingServerUrl, final String openApiProductionServerUrl) {

        StringBuilder openApiBuffer = new StringBuilder("openapi: \"3.0.1\"\n");

        writeOpenApiInfo(openApiBuffer, projectTitle, openApiDocVersion, openApiLicenceName,
                openApiDevelopmentServerUrl, openApiStagingServerUrl, openApiProductionServerUrl);

        writePaths(openApiBuffer);

        writeComponentsList(openApiBuffer);

        writeOpenApiToFile(outputDirectory, openApiBuffer, projectTitle);
    }

    private void writeOpenApiInfo(final StringBuilder openApiBuffer, final String projectTitle, final String openApiDocVersion,
            final String openApiLicenceName, final String openApiDevelopmentServerUrl,
            final String openApiStagingServerUrl, final String openApiProductionServerUrl) {

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
        openApiBuffer.append("\nservers:\n");
        if (openApiDevelopmentServerUrl != null) {

            openApiBuffer.append("  - url: ");
            openApiBuffer.append(openApiDevelopmentServerUrl);
            openApiBuffer.append("\n    description: Development server\n");
        }
        if (openApiStagingServerUrl != null) {

            openApiBuffer.append("  - url: ");
            openApiBuffer.append(openApiStagingServerUrl);
            openApiBuffer.append("\n    description: Staging server\n");
        }
        if (openApiProductionServerUrl != null) {

            openApiBuffer.append("  - url: ");
            openApiBuffer.append(openApiProductionServerUrl);
            openApiBuffer.append("\n    description: Production server\n");
        }
    }

    private void writePaths(final StringBuilder openApiBuffer) {

        openApiBuffer.append("paths:\n");

        RestDocHandler.restInfo.getClassInfo().stream()
                .filter(classInfo -> classInfo.getMethodInfo() != null)
                .forEach(classInfo -> {

                    addMethodPaths(classInfo);
                });

        methodPaths.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .forEach(entrySet -> {

                    openApiBuffer.append(entrySet.getKey());
                    openApiBuffer.append(entrySet.getValue());
                });

    }

    private void addMethodPaths(final ClassInfo classinfo) {

        classinfo.getMethodInfo().stream()
                .forEach(methodInfo -> {

                    RestDocHandler.getLogger().fine(() -> "ClassRootPath: " + classinfo.getClassRootPath());
                    RestDocHandler.getLogger().fine(() -> "ClassPath: " + classinfo.getClassPath());
                    RestDocHandler.getLogger().fine(() -> "methodInfo.getMethodPath: " + methodInfo.getMethodPath());

                    StringBuilder methodPath = new StringBuilder("  ");

                    methodPath.append(classinfo.getClassRootPath());
                    methodPath.append(classinfo.getClassPath());
                    methodPath.append(methodInfo.getMethodPath());
                    methodPath.append(":\n");

                    final StringBuilder pathContent = new StringBuilder();

                    String existingPaths = methodPaths.get(methodPath.toString());

                    if (existingPaths != null) {

                        pathContent.append(existingPaths);
                    }

                    pathContent.append("    ");
                    pathContent.append(methodInfo.getHttpRequestType().toLowerCase());
                    pathContent.append(":\n      description: ");
                    pathContent.append(onlyJavaDocComments(methodInfo.getJavaDoc()));
                    pathContent.append("\n      operationId: ");
                    pathContent.append(methodInfo.getMethodName());
                    pathContent.append("\n      responses: ");
                    pathContent.append("\n        '");
                    pathContent.append(methodInfo.getReturnInfo().getReturnStatusCode());
                    pathContent.append("':");
                    pathContent.append("\n          description: ");
                    pathContent.append(methodInfo.getReturnInfo().getReturnStatus());

                    if (methodInfo.getReturnInfo().getReturnClassName().startsWith("java.util.List")) {

                        addPathContent(pathContent, methodInfo);

                    } else {

                        if (!methodInfo.getReturnInfo().getReturnClassName().startsWith("java")) {

                            addPathContent(pathContent, methodInfo);

                        }
                    }

                    pathContent.append("\n      parameters:");
                    methodInfo.getParameterInfo().stream()
                            .forEach(parameter -> {

                                String type = null;

                                switch (parameter.getParameterType()) {

                                    case "javax.ws.rs.PathParam":
                                        addParameterYaml(pathContent, "path", parameter);
                                        break;

                                    case "javax.ws.rs.HeaderParam":
                                        addParameterYaml(pathContent, "header", parameter);
                                        break;

                                    case "javax.ws.rs.QueryParam":
                                        addParameterYaml(pathContent, "query", parameter);
                                        break;
                                }

                                String description = parameterCommentsFromJavaDoc(methodInfo.getJavaDoc().toLowerCase(), parameter.getParameterAnnotationName());

                                if (!description.isEmpty()) {

                                    pathContent.append("\n          description: ");
                                    pathContent.append(description);
                                }
                            });

                    if (!methodInfo.getRequestBodyClassName().isEmpty()) {

                        addRequestBodyInfo(pathContent, methodInfo);
                    }

                    pathContent.append("\n");

                    methodPaths.put(methodPath.toString(), pathContent.toString());
                });
    }

    private void addPathContent(final StringBuilder pathContent, MethodInfo methodInfo) {

        pathContent.append("\n          content: ");
        pathContent.append("\n            ");
        if (methodInfo.getProduceType().isEmpty()) {

            // Default to 'application/json'
            pathContent.append("application/json");

        } else {

            pathContent.append(methodInfo.getProduceType());
        }
        pathContent.append(":\n              schema:");
        pathContent.append("\n                ");
        OpenApiField openApiField = mapFieldType(methodInfo.getReturnInfo().getReturnClassName(), null);
        pathContent.append(openApiField.getFieldType());
    }

    private void addParameterYaml(final StringBuilder pathContent, final String inWhere, final ParameterInfo parameter) {

        OpenApiField type;

        pathContent.append("\n        - in: ");
        pathContent.append(inWhere);
        pathContent.append("\n          name: ");
        pathContent.append(parameter.getParameterAnnotationName());
        if (inWhere.equals("path")) {

            pathContent.append("\n          required: true");

        }
        pathContent.append("\n          schema:");
        pathContent.append("\n            type: ");
        type = mapFieldType(parameter.getParameterClassName(), null);
        pathContent.append(type.getFieldType());
        if (type.equals("array")) {

            pathContent.append("\n            items:");
            pathContent.append("\n              type: ");
            type = mapFieldType(getListType(parameter.getParameterClassName()), null);
            pathContent.append(type.getFieldType());
            if (type.getFieldFormat() != null) {

                pathContent.append("\n              format: ");
                pathContent.append(type.getFieldFormat());
            }
        } else {

            if (type.getFieldFormat() != null) {

                pathContent.append("\n            format: ");
                pathContent.append(type.getFieldFormat());
            }
        }
    }

    private void addRequestBodyInfo(final StringBuilder openApiBuffer, final MethodInfo methodInfo) {

        openApiBuffer.append("\n      requestBody:");
        openApiBuffer.append("\n        description: ");
        openApiBuffer.append(methodInfo.getRequestBodyName());
        openApiBuffer.append(", Class = ");
        openApiBuffer.append(methodInfo.getRequestBodyClassName());
        openApiBuffer.append("\n        content:\n          '");
        if (!methodInfo.getConsumeType().isEmpty()) {

            openApiBuffer.append(methodInfo.getConsumeType());
            openApiBuffer.append("':");

        } else {

            // No consume annotation set Json as default
            openApiBuffer.append("application/json':");
        }

        addOpenApiComponentReference(openApiBuffer, methodInfo);
    }

    private void addOpenApiComponentReference(final StringBuilder openApiBuffer, final MethodInfo methodInfo) {

        int lastDot = methodInfo.getRequestBodyClassName().lastIndexOf('.');

        if (lastDot > 0) {

            String componentName = methodInfo.getRequestBodyClassName().substring(lastDot + 1);

            openApiBuffer.append("\n             schema:");
            openApiBuffer.append("\n               $ref: '#/components/schemas/");
            openApiBuffer.append(componentName);
            openApiBuffer.append("'");

            addComponent(componentName, methodInfo.getRequestBodyClassName(), NO_COLLECTION);
        }
    }

    private void addComponent(final String componentName, final String className, final String collectionType) {

        if (className.startsWith("java")) {

            // Don't add component
            return;
        }

        if (components.containsKey(componentName)) {

            // This component already exist
            return;
        }

        StringBuilder component = new StringBuilder();

        component.append("    ");
        component.append(componentName);
        component.append(":");
        component.append("\n      properties:");

        DataModelInfo dataModelInfo = RestDocHandler.restInfo.getDomainDataMap().get(className);

        if (dataModelInfo != null) {

            dataModelInfo.getFields().stream()
                    .forEach(field -> {

                        component.append("\n        ");
                        component.append(field.getFieldName());
                        component.append(":");

                        if (field.getFieldType().equals("error")) {

                            component.append("\n          type: object");
                            component.append("\n          description: ");
                            component.append(dataModelInfo.getInfo().replaceAll(":", ""));

                        } else {

                            if (field.getFieldType().equals("enum")) {

                                component.append("\n          type: string");
                                component.append("\n          enum: [");
                                component.append(field.getFieldOfType());
                                component.append("]");

                            } else {

                                // The ordert is important, needs to be after error and enum
                                if (componentName.startsWith("Set-")) {

                                    component.append("\n          type: array");
                                    component.append("\n          items: ");

                                    OpenApiField openApiField = mapFieldType(field.getFieldType(), field.getFieldOfType());

                                    component.append("\n            type: ");
                                    component.append(openApiField.getFieldType());

                                    if (openApiField.getFieldFormat() != null) {

                                        component.append("\n            format: ");
                                        component.append(openApiField.getFieldFormat());
                                    }

                                    component.append("\n          description: ");
                                    component.append(collectionType);

                                } else {

                                    component.append("\n          ");

                                    OpenApiField openApiField = mapFieldType(field.getFieldType(), field.getFieldOfType());

                                    if (!openApiField.getFieldType().startsWith("$ref:")) {

                                        component.append("type: ");
                                    }

                                    component.append(openApiField.getFieldType());

                                    if (openApiField.getFieldFormat() != null) {

                                        component.append("\n          format: ");
                                        component.append(openApiField.getFieldFormat());
                                    }

                                    if (openApiField.getDescription() != null) {

                                        component.append("\n          description: ");
                                        component.append(openApiField.getDescription());
                                    }

                                    if (openApiField.getFieldType().equals("array")) {

                                        component.append("\n          items:");

                                        OpenApiField openApiItemField = mapFieldType(field.getFieldOfType(), null);

                                        if (!openApiItemField.getFieldType().startsWith("$ref:")) {

                                            component.append("\n            type: ");
                                            component.append(openApiItemField.getFieldType());

                                            if (openApiItemField.getFieldFormat() != null) {

                                                component.append("\n            format: ");
                                                component.append(openApiItemField.getFieldFormat());
                                            }
                                        } else {

                                            // $ref: reference
                                            component.append("\n            ");
                                            component.append(openApiItemField.getFieldType());
                                        }
                                    }
                                }
                            }
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

    private OpenApiField mapFieldType(final String javaField, final String extendedFieldType) {

        OpenApiField field = new OpenApiField();

        switch (javaField) {

            case "int":
            case "java.lang.Integer":
                field.setFieldType("integer");
                field.setFieldFormat("int32");
                return field;

            case "long":
            case "java.lang.Long":
                field.setFieldType("integer");
                field.setFieldFormat("int64");
                return field;

            case "float":
            case "java.lang.Float":
                field.setFieldType("number");
                field.setFieldFormat("float");
                return field;

            case "double":
            case "java.lang.Double":
                field.setFieldType("number");
                field.setFieldFormat("double");
                return field;

            case "java.lang.String":
                field.setFieldType("string");
                return field;

            case "byte":
            case "java.lang.Byte":
                field.setFieldType("string");
                field.setFieldFormat("byte");
                return field;

            case "boolean":
            case "java.lang.Boolean":
                field.setFieldType("boolean");
                return field;

            case "java.util.Date":
                field.setFieldType("string");
                field.setFieldFormat("date");
                return field;

            case "java.util.List":
                field.setFieldType("array");
                return field;

            case "java.net.URI":
                field.setFieldType("string");
                field.setFieldFormat("uri");
                field.setDescription(javaField);
                return field;

            default:
                if (javaField.startsWith("java.util.List")) {

                    field.setFieldType("array");
                    return field;

                } else if (javaField.endsWith("Map")) {

                    field.setFieldType("object");
                    field.setDescription(javaField);
                    return field;

                } else {

                    int lastDot = javaField.lastIndexOf('.');

                    if (lastDot > 0) {

                        String componentName;

                        int lessThanIndex = -1;
                        int greaterThanIndex = javaField.lastIndexOf('>');

                        if (greaterThanIndex == -1) {

                            componentName = javaField.substring(lastDot + 1);

                            if (componentName.equals("Set")) {

                                int extendedLastDot = extendedFieldType.lastIndexOf('.');

                                componentName = componentName + "-" + extendedFieldType.substring(extendedLastDot + 1);

                                addComponent(componentName, extendedFieldType, javaField);
                            }
                        } else {

                            componentName = javaField.substring(lastDot + 1, greaterThanIndex);
                            lessThanIndex = javaField.lastIndexOf('<');
                        }

                        StringBuilder componentRef = new StringBuilder("$ref: '#/components/schemas/");

                        componentRef.append(componentName);
                        componentRef.append("'");

                        if (lessThanIndex == -1) {

                            addComponent(componentName, javaField, NO_COLLECTION);

                        } else {

                            addComponent(componentName, javaField.substring(lessThanIndex + 1, greaterThanIndex), NO_COLLECTION);
                        }

                        field.setFieldType(componentRef.toString());

                        if (javaField.endsWith("Set")) {

                            field.setDescription(javaField);
                        }

                        return field;

                    } else {

                        return field;
                    }
                }
        }
    }

    private String getListType(final String className) {

        int startIndex = className.indexOf('<');
        int endIndex = className.indexOf('>');

        if ((startIndex > 0) && (endIndex > startIndex)) {

            return className.substring(startIndex + 1, endIndex).trim();
        }

        return "";
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

            RestDocHandler.getLogger().severe("writeOpenApiToFile() Path ='" + openApiPath.toString() + "' IOException: " + ioe.getMessage());
        }
    }

}
