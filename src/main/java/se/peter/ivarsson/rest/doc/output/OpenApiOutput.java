/*
 * Rest Documentation maven plugin.
 * 
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.output;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import se.peter.ivarsson.rest.doc.mojo.ConfigParameters;
import se.peter.ivarsson.rest.doc.parser.ClassInfo;
import se.peter.ivarsson.rest.doc.parser.DataModelInfo;
import se.peter.ivarsson.rest.doc.parser.MethodInfo;
import se.peter.ivarsson.rest.doc.parser.OpenApiField;
import se.peter.ivarsson.rest.doc.parser.ParameterInfo;
import se.peter.ivarsson.rest.doc.parser.RestDocHandler;
import se.peter.ivarsson.rest.doc.utils.LoggingUtils;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class OpenApiOutput {

    private static final Logger LOGGER = Logger.getLogger(OpenApiOutput.class.getSimpleName());

    private static final String NO_COLLECTION = "";
    private static final String PATH_DELIMITER = "/";
    private static final String JAVA_LIST = "java.util.List";
    private static final String YAML_URL = "  - url: ";
    private static final String YAML_DESCRIPTION = "\n          description: ";
    private static final String YAML_PARAMETERS = "\n      parameters:";
    private static final String YAML_NEW_ROW = "\n            ";
    private static final String YAML_FORMAT = "\n            format: ";
    private static final String YAML_REFERENCE = "$ref:";
    private static final String YAML_ARRAY = "array";
    private static final String YAML_STRING = "string";

    private final HashSet<String> startAddedcomponents = new HashSet<>();
    private final HashMap<String, String> components = new HashMap<>();
    private final HashMap<String, String> methodPaths = new HashMap<>();

    public void createOpenApiDocumantation(final ConfigParameters configParameters) {

        LoggingUtils.addLoggingFileHandler(configParameters.getLoggingDirectory(), LOGGER);

        StringBuilder openApiBuffer = new StringBuilder("openapi: \"3.0.1\"\n");

        writeOpenApiInfo(openApiBuffer, configParameters);

        writePaths(openApiBuffer);

        writeComponentsList(openApiBuffer);

        writeOpenApiToFile(configParameters.getOutputDirectory(), openApiBuffer, configParameters.getProjectTitle());
    }

    private void writeOpenApiInfo(final StringBuilder openApiBuffer, final ConfigParameters configParameters) {

        openApiBuffer.append("info:\n");
        openApiBuffer.append("  title: ");
        openApiBuffer.append(configParameters.getProjectTitle());
        openApiBuffer.append("\n");
        openApiBuffer.append("  version: ");
        openApiBuffer.append(configParameters.getOpenApiDocVersion());
        openApiBuffer.append("\n");
        openApiBuffer.append("  license:\n");
        openApiBuffer.append("    name: ");
        openApiBuffer.append(configParameters.getOpenApiLicenceName());
        openApiBuffer.append("\nservers:\n");
        if (configParameters.getOpenApiDevelopmentServerUrl() != null) {

            openApiBuffer.append(YAML_URL);
            openApiBuffer.append(configParameters.getOpenApiDevelopmentServerUrl());
            openApiBuffer.append("\n    description: Development server\n");
        }
        if (configParameters.getOpenApiStagingServerUrl() != null) {

            openApiBuffer.append(YAML_URL);
            openApiBuffer.append(configParameters.getOpenApiStagingServerUrl());
            openApiBuffer.append("\n    description: Staging server\n");
        }
        if (configParameters.getOpenApiProductionServerUrl() != null) {

            openApiBuffer.append(YAML_URL);
            openApiBuffer.append(configParameters.getOpenApiProductionServerUrl());
            openApiBuffer.append("\n    description: Production server\n");
        }
    }

    private void writePaths(final StringBuilder openApiBuffer) {

        openApiBuffer.append("paths:\n");

        RestDocHandler.restInfo.getClassInfo().stream()
                .filter(classInfo -> classInfo.getMethodInfo() != null)
                .forEach(this::addMethodPaths);

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

                    LOGGER.info(() -> "ClassRootPath: " + classinfo.getClassRootPath());
                    LOGGER.info(() -> "ClassPath: " + classinfo.getClassPath());
                    LOGGER.info(() -> "methodInfo.getMethodPath: " + methodInfo.getMethodPath());

                    StringBuilder methodPath = new StringBuilder("  ");

                    methodPath.append(classinfo.getClassRootPath());
                    methodPath.append(classinfo.getClassPath());
                    methodPath.append(methodInfo.getMethodPath());

                    if (methodPath.toString().equals("  ")) {

                        methodPath.append("/:\n");

                    } else {

                        methodPath.append(":\n");
                    }

                    final StringBuilder pathContent = new StringBuilder();

                    String existingPaths = methodPaths.get(methodPath.toString());

                    if (existingPaths != null) {

                        pathContent.append(existingPaths);
                    }

                    pathContent.append("    ");
                    pathContent.append(methodInfo.getHttpRequestType().toLowerCase());
                    pathContent.append(":");
                    if (methodInfo.getJavaDoc() != null) {

                        pathContent.append("\n      description: ");
                        pathContent.append(onlyJavaDocComments(methodInfo.getJavaDoc()));
                    }
                    pathContent.append("\n      operationId: ");
                    pathContent.append(methodInfo.getMethodName());
                    pathContent.append("\n      responses: ");
                    pathContent.append("\n        '");
                    pathContent.append(methodInfo.getReturnInfo().getReturnStatusCode());
                    pathContent.append("':");
                    pathContent.append(YAML_DESCRIPTION);
                    pathContent.append(methodInfo.getReturnInfo().getReturnStatusAsText());

                    if (methodInfo.getReturnInfo().getReturnClassName().startsWith(JAVA_LIST)) {

                        addPathContent(pathContent, methodInfo);

                    } else {

                        if (!methodInfo.getReturnInfo().getReturnClassName().startsWith("java")) {

                            addPathContent(pathContent, methodInfo);

                        }
                    }

                    final Boolean[] validParameters = new Boolean[1];
                    validParameters[0] = false;

                    methodInfo.getParameterInfo().stream()
                            .forEach(parameter -> {

                                switch (parameter.getParameterType()) {

                                    case "javax.ws.rs.PathParam":
                                        if (!validParameters[0]) {

                                            pathContent.append(YAML_PARAMETERS);
                                            validParameters[0] = true;
                                        }
                                        addParameterYaml(pathContent, "path", parameter);
                                        break;

                                    case "javax.ws.rs.HeaderParam":
                                        if (!validParameters[0]) {

                                            pathContent.append(YAML_PARAMETERS);
                                            validParameters[0] = true;
                                        }
                                        addParameterYaml(pathContent, "header", parameter);
                                        break;

                                    case "javax.ws.rs.QueryParam":
                                        if (!validParameters[0]) {

                                            pathContent.append(YAML_PARAMETERS);
                                            validParameters[0] = true;
                                        }
                                        addParameterYaml(pathContent, "query", parameter);
                                        break;
                                        
                                    default:
                                        break;
                                }

                                String description = parameterCommentsFromJavaDoc(methodInfo.getJavaDoc(), parameter.getParameterAnnotationName());

                                if (!description.isEmpty()) {

                                    pathContent.append(YAML_DESCRIPTION);
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
        pathContent.append(YAML_NEW_ROW);
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
        type = mapFieldType(parameter.getParameterClassName(), null);
        pathContent.append(YAML_NEW_ROW);
        if (!type.getFieldType().startsWith(YAML_REFERENCE)) {

            pathContent.append("type: ");
        }
        pathContent.append(type.getFieldType());
        if (type.getFieldType().equals(YAML_ARRAY)) {

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

                pathContent.append(YAML_FORMAT);
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

        if (startAddedcomponents.contains(componentName)) {

            // This component already exist
            return;

        } else {

            startAddedcomponents.add(componentName);
        }

        StringBuilder component = new StringBuilder();

        component.append("    ");
        component.append(componentName);
        component.append(":");

        DataModelInfo dataModelInfo = RestDocHandler.restInfo.getDomainDataMap().get(className);

        if (!dataModelInfo.getFields().isEmpty()) {

            component.append("\n      properties:");

        } else {

            if (!dataModelInfo.getInfo().isEmpty()) {

                component.append("\n        description: ");
                component.append(dataModelInfo.getInfo());
            }
        }

        dataModelInfo.getFields().stream()
                .forEach(field -> {

                    component.append("\n        ");
                    component.append(field.getFieldName());
                    component.append(":");

                    if (field.getFieldType().equals("error")) {

                        component.append("\n          type: object");
                        component.append(YAML_DESCRIPTION);
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

                                    component.append(YAML_FORMAT);
                                    component.append(openApiField.getFieldFormat());
                                }

                                component.append(YAML_DESCRIPTION);
                                component.append(collectionType);

                            } else {

                                component.append("\n          ");

                                OpenApiField openApiField = mapFieldType(field.getFieldType(), field.getFieldOfType());

                                if (!openApiField.getFieldType().startsWith(YAML_REFERENCE)) {

                                    component.append("type: ");
                                }

                                component.append(openApiField.getFieldType());

                                if (openApiField.getFieldFormat() != null) {

                                    component.append("\n          format: ");
                                    component.append(openApiField.getFieldFormat());
                                }

                                if (openApiField.getDescription() != null) {

                                    component.append(YAML_DESCRIPTION);
                                    component.append(openApiField.getDescription());
                                }

                                if (openApiField.getFieldType().equals(YAML_ARRAY)) {

                                    component.append("\n          items:");

                                    OpenApiField openApiItemField = mapFieldType(field.getFieldOfType(), null);

                                    if (!openApiItemField.getFieldType().startsWith(YAML_REFERENCE)) {

                                        component.append("\n            type: ");
                                        component.append(openApiItemField.getFieldType());

                                        if (openApiItemField.getFieldFormat() != null) {

                                            component.append(YAML_FORMAT);
                                            component.append(openApiItemField.getFieldFormat());
                                        }
                                    } else {

                                        // $ref: reference
                                        component.append(YAML_NEW_ROW);
                                        component.append(openApiItemField.getFieldType());
                                    }
                                }
                            }
                        }
                    }
                });

        components.put(componentName, component.toString());
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
                field.setFieldType(YAML_STRING);
                return field;

            case "byte":
            case "java.lang.Byte":
                field.setFieldType(YAML_STRING);
                field.setFieldFormat("byte");
                return field;

            case "boolean":
            case "java.lang.Boolean":
                field.setFieldType("boolean");
                return field;

            case "java.util.Date":
                field.setFieldType(YAML_STRING);
                field.setFieldFormat("date");
                return field;

            case JAVA_LIST:
                field.setFieldType(YAML_ARRAY);
                return field;

            case "java.net.URI":
                field.setFieldType(YAML_STRING);
                field.setFieldFormat("uri");
                field.setDescription(javaField);
                return field;

            default:
                if (javaField.startsWith(JAVA_LIST)) {

                    field.setFieldType(YAML_ARRAY);
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

        if (javaDoc == null) {

            return "";
        }

        String javaDocLowerCase = javaDoc.toLowerCase();

        int startFirstAtIndex = javaDocLowerCase.indexOf("@param");

        while (startFirstAtIndex != -1) {

            int parameterNameIndex = javaDocLowerCase.indexOf(parameterName.toLowerCase(), startFirstAtIndex + 6);

            if (parameterNameIndex != -1) {

                int endDescriptionIndex = javaDocLowerCase.indexOf('*', parameterNameIndex);

                if (endDescriptionIndex != -1) {

                    int startDescription = parameterNameIndex + parameterName.length() + 1;

                    return javaDocLowerCase.substring(startDescription, endDescriptionIndex).replaceAll("\\*", "").trim();
                }
            }

            if (parameterNameIndex == -1) {

                startFirstAtIndex = javaDocLowerCase.indexOf("@param", startFirstAtIndex + 7);
            }
        }

        return "";
    }

    private void writeOpenApiToFile(final File outputDirectory, final StringBuilder openApiBuffer, final String projectTitle) {

        Path openApiPath = Paths.get(URI.create("file://" + outputDirectory.getAbsolutePath() + PATH_DELIMITER + projectTitle.replaceAll("(\\s+|\"+)", "_") + ".yaml"));

        try {

            Files.write(openApiPath, openApiBuffer.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ioe) {

            LOGGER.severe("writeOpenApiToFile() Path ='" + openApiPath.toString() + "' IOException: " + ioe.getMessage());
        }
    }

}
