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
import java.util.logging.Logger;
import se.peter.ivarsson.rest.doc.parser.ClassInfo;
import se.peter.ivarsson.rest.doc.parser.RestDocHandler;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class OpenApiOutput {

    private static final Logger LOGGER = Logger.getLogger(OpenApiOutput.class.getSimpleName());

    public void createOpenApiDocumantation(final File outputDirectory, final String projectTitle, final String openApiDocVersion, final String openApiLicenceName) {

        StringBuffer openApiBuffer = new StringBuffer("openapi: \"3.0.0\"\n");

        writeOpenApiInfo(openApiBuffer, projectTitle, openApiDocVersion, openApiLicenceName);

        writePaths(openApiBuffer);

        writeOpenApiToFile(outputDirectory, openApiBuffer, projectTitle);
    }

    private void writeOpenApiInfo(final StringBuffer openApiBuffer, final String projectTitle, final String openApiDocVersion, final String openApiLicenceName) {

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

    private void writePaths(final StringBuffer openApiBuffer) {

        openApiBuffer.append("paths:\n");

        RestDocHandler.restInfo.getClassInfo().stream()
                .filter(classInfo -> classInfo.getMethodInfo() != null)
                .forEach(classInfo -> {

                    addMethodPaths(openApiBuffer, classInfo);
                });
    }

    private void addMethodPaths(final StringBuffer openApiBuffer, final ClassInfo classinfo) {

        classinfo.getMethodInfo().stream()
                .forEach(methodInfo -> {

                    openApiBuffer.append("  ");

                    StringBuffer methodPath = new StringBuffer();

                    if (classinfo.getClassRootPath() != null) {

                        methodPath.append(classinfo.getClassRootPath());

                    } else {

                        if ((classinfo.getClassPath() != null) && !methodInfo.getRestPath().contains(classinfo.getClassPath())) {

                            methodPath.append(classinfo.getClassPath());
                        }
                    }

                    methodPath.append(methodInfo.getRestPath());

                    if (methodPath.charAt(0) != '/') {

                        openApiBuffer.append("/");
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

                                openApiBuffer.append("\n        name: ");
                                openApiBuffer.append(parameter.getParameterAnnotationName());
                                openApiBuffer.append("\n        in: ");
                                switch (parameter.getParameterType()) {

                                    case "javax.ws.rs.PathParam":
                                        openApiBuffer.append("path");
                                        break;

                                    case "javax.ws.rs.HeaderParam":
                                        openApiBuffer.append("header");
                                        break;

                                    case "javax.ws.rs.QueryParam":
                                        openApiBuffer.append("query");
                                        break;

                                    default:
                                        openApiBuffer.append("-");
                                        break;
                                }

                                String description = parameterCommentsFromJavaDoc(methodInfo.getJavaDoc().toLowerCase(), parameter.getParameterAnnotationName());

                                if (!description.isEmpty()) {

                                    openApiBuffer.append("\n        description: ");
                                    openApiBuffer.append(description);
                                }
                            });

                    openApiBuffer.append("\n\n");
                });
    }

    private String onlyJavaDocComments(String javaDoc) {

        int atIndex = javaDoc.indexOf("@");

        if (atIndex != -1) {

            return javaDoc.substring(3, atIndex).replace("/", "").replace("*", "").trim();
        }

        return "";
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

    private void writeOpenApiToFile(final File outputDirectory, final StringBuffer openApiBuffer, final String projectTitle) {

        Path openApiPath = Paths.get(URI.create("file://" + outputDirectory.getAbsolutePath() + "/" + projectTitle.replaceAll("(\\s+|\"+)", "_") + ".yaml"));

        try {

            Files.write(openApiPath, openApiBuffer.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ioe) {

            LOGGER.severe("writeOpenApiToFile() Path ='" + openApiPath.toString() + "' IOException: " + ioe.getMessage());
        }
    }

}
