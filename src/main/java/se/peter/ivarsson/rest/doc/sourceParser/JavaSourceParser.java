/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.sourceParser;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;
import se.peter.ivarsson.rest.doc.parser.PathInfo;
import se.peter.ivarsson.rest.doc.parser.ResponseType;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class JavaSourceParser {

    private static final Logger LOGGER = Logger.getLogger(JavaSourceParser.class.getSimpleName());

    // JavaDoc parsing
    private int javaDocStartIndexOffset = -1;
    private StringBuilder javaDocComment = null;
    private boolean javaDocReadingComments = false;
    private boolean javaDocEndReached = false;

    // Used in 'Respose type' and 'Class path'
    private final HashMap<String, String> importClasses = new HashMap<>();

    // Respose type parsing
    private final HashMap<String, String> responseTypeVariableTypes = new HashMap<>();
    private boolean responseTypeAnnotationFound = false;
    private boolean responseTypePublicReponseFound = false;
    private String responseTypeResponseMethodName = "";

    // Class path parsing
    private boolean classPathAnnotationFound = false;
    private String classPathTemporary = "";
    private String classPath = "";

    // Constants parsing
    private boolean isClass = false;

    public void parseClassForJavaDocComments(final File sourceDiretory, final HashMap<String, String> javaDocComments, final String className) {

        // Starting point
        javaDocStartIndexOffset = -1;
        javaDocReadingComments = false;
        javaDocEndReached = false;

        javaDocComments.clear();

        String javaFilePath = getSourceFileNameFromClassName(sourceDiretory, className);

        // Read file into stream
        try (Stream<String> stream = Files.lines(Paths.get(javaFilePath))) {

            stream.forEach(line -> {

                parseFileForComments(line, javaDocComments, className);
            });

        } catch (IOException ioe) {

            LOGGER.severe(() -> "parseClassForJavaDocComments(), IOException: " + ioe.getMessage() + ", Can't find file: " + javaFilePath);
        }
    }

    private void parseFileForComments(final String line, final HashMap<String, String> javaDocComments, final String className) {

        if (javaDocStartIndexOffset < 0) {

            javaDocStartIndexOffset = line.indexOf("/**");
        }

        if (javaDocEndReached == false) {

            if ((javaDocStartIndexOffset != -1) && (javaDocReadingComments == false)) {

                javaDocComment = new StringBuilder(line);
                javaDocComment.append('\r');
                javaDocReadingComments = true;

            } else {

                if (javaDocReadingComments == true) {

                    javaDocComment.append(line.substring(javaDocStartIndexOffset));
                    javaDocComment.append('\r');

                    int endIndexOffset = line.indexOf("*/");

                    if (endIndexOffset > 0) {

                        javaDocEndReached = true;
                    }
                }
            }
        } else {

            // Search for public methods
            int publicMethodOffset = line.indexOf("public");

            if (publicMethodOffset != -1) {
                // Skip public
                findMethodNameAndAddToHashMap(line.substring(publicMethodOffset + 7), javaDocComments, className);
            }
        }
    }

    private void findMethodNameAndAddToHashMap(final String line, final HashMap<String, String> javaDocComments, final String className) {

        int endMethodNameOffset = line.indexOf('(');

        if (endMethodNameOffset != -1) {

            String methodString = line.substring(0, endMethodNameOffset).trim();

            int methodStartIndex = methodString.lastIndexOf(' ');

            if (methodStartIndex != -1) {

                String methodName = methodString.substring(methodStartIndex + 1);

                javaDocComments.put(methodName, javaDocComment.toString());
                javaDocStartIndexOffset = -1;
                javaDocReadingComments = false;
                javaDocEndReached = false;
            }
        } else {

            // Skip this JavaDoc comment is not a part of method
            javaDocStartIndexOffset = -1;
            javaDocReadingComments = false;
            javaDocEndReached = false;
        }
    }

    public void parseSourceFile(final File sourceDiretory, final HashMap<String, String> javaEnums,
            final HashMap<String, ResponseType> responseTypes, final HashMap<String, PathInfo> classPaths,
            final HashMap<String, String> constants, final Path sourceFilePath, final URLClassLoader urlClassLoader) {

        LOGGER.info(() -> "parseSourceFile(), Checking source file " + sourceFilePath + " for enums and response types");

        importClasses.clear();
        responseTypeAnnotationFound = false;
        responseTypePublicReponseFound = false;

        classPathAnnotationFound = false;
        classPathTemporary = "";
        classPath = "";

        isClass = false;

        String className = getFullClassNameFromSourcesDir(sourceDiretory, sourceFilePath);

        String enumListForClass = getEnumValuesFromClass(className, urlClassLoader);

        // Read file into stream
        try (Stream<String> stream = Files.lines(Paths.get(sourceFilePath.toString()))) {

            final Boolean[] isClass = new Boolean[1];
            isClass[0] = false;

            stream.forEach(line -> {

                if (line.indexOf(" class ") != -1) {

                    isClass[0] = true;
                }

                addImportStatementToMap(line);

                findEnumsInFile(line, javaEnums, sourceFilePath, isClass[0], enumListForClass);

                findResponseOkType(line, responseTypes, className);

                findClassPaths(line, classPaths, className);

                findConstants(line, constants, className);
            });

        } catch (IOException ioe) {

            LOGGER.severe(() -> "parseSourceFile(), IOException: " + ioe.getMessage() + ", Can't find file: " + sourceFilePath);
        }
    }

    private void findEnumsInFile(final String line, final HashMap<String, String> javaEnums, final Path sourceFilePath,
            final Boolean inClass, final String enumListForClass) {

        // Search for enum in public methods
        int publicMethodOffset = line.indexOf("public ");
        int startEnumOffset = line.indexOf(" enum ");
        int endEnumOffset = line.indexOf('{');

        if ((publicMethodOffset != -1) && (startEnumOffset != -1) && (endEnumOffset != -1)) {

            // This is a public enum
            String enumType = line.substring(startEnumOffset + 5, endEnumOffset).trim();

            String sourceFile = sourceFilePath.toString();

            int startSourcesFolderIndex = sourceFile.indexOf("/java/") + 6;
            int endSourcesFolderIndex = sourceFile.indexOf(enumType, startSourcesFolderIndex);

            if ((startSourcesFolderIndex != -1) && (endSourcesFolderIndex != -1)) {

                String enumTypeWithPath = null;

                if (inClass) {

                    // Enum inside a class
                    int endJavaSuffixIndex = sourceFile.indexOf(".java", startSourcesFolderIndex);

                    enumTypeWithPath = sourceFile.substring(startSourcesFolderIndex, endJavaSuffixIndex).replace('/', '.') + '$' + enumType;

                } else {

                    // This is a public enum
                    enumTypeWithPath = sourceFile.substring(startSourcesFolderIndex, endSourcesFolderIndex).replace('/', '.') + enumType;
                }

                javaEnums.put(enumTypeWithPath, enumListForClass);

                LOGGER.info(() -> "findEnumsInFile(), Enum " + enumType + " found in file " + sourceFilePath);
            }
        }
    }

    private void findResponseOkType(final String line, final HashMap<String, ResponseType> responseTypes, final String className) {

        if (responseTypeAnnotationFound == false) {

            // Search for enum in public methods
            if (isPathAnnotation(line) != -1) {

                responseTypeAnnotationFound = true;
                responseTypePublicReponseFound = false;
            }
            return; //  No path found yet
        }

        if (responseTypePublicReponseFound == false) {

            String methodName;

            if ((methodName = isResponseMethod(line)) != null) {

                responseTypePublicReponseFound = true;
                responseTypeResponseMethodName = methodName;
                responseTypeVariableTypes.clear();
            }
        } else {

            // Search for real 'Response.ok('
            int returnOffset = line.indexOf("return");
            int responseOffset = line.indexOf("Response", returnOffset);
            int responseOkOffset = line.indexOf(".ok", responseOffset);

            if ((returnOffset != -1) && (responseOffset != -1)) {

                String responseTypeKey = className + "-" + responseTypeResponseMethodName;

                if (responseOkOffset != -1) {

                    // Http status OK
                    int responseOkStartOffset = line.indexOf("(", responseOkOffset);

                    if (responseOkStartOffset != -1) {

                        // Get Response OK variable name
                        int responseOkEndOffset = line.indexOf(")", responseOkStartOffset);

                        if (responseOkEndOffset != -1) {

                            int newOffset = line.indexOf("new ");

                            if (newOffset == -1) {

                                // We don't want any new objects
                                String variableName = line.substring(responseOkStartOffset + 1, responseOkEndOffset).trim();

                                if (responseTypeVariableTypes.containsKey(variableName)) {

                                    ResponseType responseType = new ResponseType();
                                    responseType.setReturnStatus("OK");
                                    responseType.setReturnStatusCode("200");

                                    String variableType = responseTypeVariableTypes.get(variableName);

                                    int listIndex = variableType.indexOf("List<");

                                    if (listIndex == -1) {

                                        // No list
                                        if (importClasses.containsKey(responseTypeVariableTypes.get(variableName))) {

                                            responseType.setReturnType(importClasses.get(responseTypeVariableTypes.get(variableName)));

                                            responseTypes.put(responseTypeKey, responseType);
                                        }
                                    } else {

                                        String listType = variableType.substring(listIndex + 5, variableType.length() - 1);

                                        if (importClasses.containsKey(listType)) {

                                            responseType.setReturnType("List<" + importClasses.get(listType) + ">");

                                            responseTypes.put(responseTypeKey, responseType);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {

                    // Http status thas is NOT OK
                    findOutHttpStatusCode(line, responseOffset, responseTypeKey, responseTypes);
                }

                // We have found an return Response Type
                responseTypeAnnotationFound = false;
                responseTypePublicReponseFound = false;

            } else {

                // Response.ok not found
                // Save all variables in the variableTypes HashList
                addVariableAndTypeToHashList(line);
            }
        }
    }

    private void findClassPaths(final String line, final HashMap<String, PathInfo> classPaths, final String className) {

        if (classPathAnnotationFound == false) {

            int pathOffset = isPathAnnotation(line);

            // Search for enum in public methods
            if (pathOffset != -1) {

                parsePathAnnotation(line, pathOffset);

                classPathAnnotationFound = true;
            }
            return; //  No path found yet
        }

        if (isClass(line)) {

            if (!classPathTemporary.isEmpty()) {

                classPath = classPathTemporary + '/';

            } else {

                classPath = "";
            }

            classPathAnnotationFound = false;
            classPathTemporary = "";
            return;
        }

        if (classPathAnnotationFound == false) {

            if (isPathAnnotation(line) != -1) {

                classPathAnnotationFound = true;
                return;
            }
        } else {

            if (isResponseMethod(line) != null) {

                classPathAnnotationFound = false;
                classPathTemporary = "";
                return;

            } else {

                String methodReturnType;

                if ((methodReturnType = isPublicMethod(line)) != null) {

                    PathInfo pathInfo = new PathInfo();
                    pathInfo.setParentPath(className);

                    if (importClasses.containsKey(methodReturnType)) {

                        methodReturnType = importClasses.get(methodReturnType);

                        if (importClasses.containsKey(classPathTemporary)) {

                            String[] pathList = importClasses.get(classPathTemporary).split("\\s+");

                            if (pathList.length > 0) {

                                if (!classPathTemporary.isEmpty()) {

                                    pathInfo.setClassPath(classPath + importClasses.get(classPathTemporary) + '/');

                                } else {

                                    pathInfo.setClassPath(classPath);
                                }

                                classPaths.put(methodReturnType, pathInfo);
                            }

                        } else {

                            if (!classPathTemporary.isEmpty()) {

                                pathInfo.setClassPath(classPath + classPathTemporary + '/');

                            } else {

                                pathInfo.setClassPath(classPath);
                            }

                            classPaths.put(methodReturnType, pathInfo);
                        }
                    }

                    classPathAnnotationFound = false;
                    classPathTemporary = "";
                }
            }
        }
    }

    private void parsePathAnnotation(final String line, final int pathOffset) {

        int quoteStartOffset = line.indexOf('\"', pathOffset);
        int quoteEndOffset = line.indexOf('\"', quoteStartOffset + 1);
        int parentheseEndOffset = line.indexOf(')', quoteEndOffset + 1);

        if ((quoteStartOffset != -1) && (quoteEndOffset != -1) && ((parentheseEndOffset - quoteEndOffset) < 4)) {

            classPathTemporary = line.substring(quoteStartOffset + 1, quoteEndOffset);

        } else {

            int parentheseStartOffset = line.indexOf('(');

            // Probably a define
            String pathContent = line.substring(parentheseStartOffset + 1, parentheseEndOffset);

            if (pathContent.contains("+")) {

                pathContent = pathContent.replaceAll("(\\s+|\"+)", "");

                String[] pathList = pathContent.split("[+]");

                if (pathList.length > 0) {

                    String classPathTemporary = "";
                    int index = 0;

                    while (index < pathList.length) {

                        if (pathList[index].equals("/") && (index == 0)) {

                            index++;
                            continue;
                        }

                        if (importClasses.containsKey(pathList[index])) {

                            classPathTemporary = classPathTemporary + importClasses.get(pathList[index]);

                        } else {

                            classPathTemporary = classPathTemporary + pathList[index];
                        }
                        index++;
                    }
                }
            } else {

                if (importClasses.containsKey(pathContent)) {

                    classPathTemporary = classPathTemporary + importClasses.get(pathContent);

                } else {

                    classPathTemporary = classPathTemporary + pathContent;
                }
            }
        }
    }

    private void findConstants(final String line, final HashMap<String, String> constants, final String className) {

        if (isClass == false) {

            if (isClass(line)) {

                isClass = true;
            }
            return;
        }

        // Find constants
        //     static final String PATH_CLEAR_CACHE_RESOURCE = "/clearcache";
        int privateOffset = line.indexOf("private");
        int staticOffset = line.indexOf("static");
        int finalOffset = line.indexOf("final", staticOffset + 1);
        int equalOffset = line.indexOf('=', finalOffset + 1);

        if ((staticOffset != -1) && (finalOffset != -1) && (equalOffset != -1) && (privateOffset == -1)) {

            String constant = line.substring(finalOffset + 6);

            String[] constantList = constant.split("\\s+");

            if (constantList.length > 3) {

                if (constantList[constantList.length - 4].equals("String") && constantList[constantList.length - 2].equals("=")) {

                    String value = constantList[constantList.length - 1];

                    int firstQuoteOffset = value.indexOf('\"');
                    int secondQuoteOffset = value.indexOf('\"', firstQuoteOffset + 1);

                    if ((firstQuoteOffset != -1) && (secondQuoteOffset != -1)) {

                        constants.put(className + '.' + constantList[constantList.length - 3], value.substring(firstQuoteOffset + 1, secondQuoteOffset));

                    } else {

                        int semiColonOffset = value.indexOf(';', firstQuoteOffset + 1);

                        if (semiColonOffset != -1) {

                            constants.put(className + '.' + constantList[constantList.length - 3], value.substring(0, semiColonOffset));
                        }
                    }
                }
            }
        }
    }

    private void findOutHttpStatusCode(final String line, final int responseOffset, final String responseTypeKey,
            final HashMap<String, ResponseType> responseTypes) {

        int responseDotOffset = line.indexOf(".", responseOffset);
        int responseParenthesesStartOffset = line.indexOf("(", responseDotOffset);

        if ((responseDotOffset != -1) && (responseDotOffset != -1)) {

            ResponseType responseType = new ResponseType();

            String httpStatusMethod = line.substring(responseOffset + 9, responseParenthesesStartOffset);

            switch (httpStatusMethod) {

                case "accepted":
                    responseType.setReturnStatus("ACCEPTED");
                    responseType.setReturnStatusCode("202");
                    break;

                case "noContent":
                    responseType.setReturnStatus("NO_CONTENT");
                    responseType.setReturnStatusCode("204");
                    break;

                case "notAcceptable":
                    responseType.setReturnStatus("NOT_ACCEPTABLE");
                    responseType.setReturnStatusCode("406");
                    break;

                case "notModified":
                    responseType.setReturnStatus("NOT_MODIFIED");
                    responseType.setReturnStatusCode("304");
                    break;

                case "ok":
                    responseType.setReturnStatus("OK");
                    responseType.setReturnStatusCode("200");
                    break;

                case "serverError":
                    responseType.setReturnStatus("INTERNAL_SERVER_ERROR");
                    responseType.setReturnStatusCode("500");
                    break;

                case "status":
                    parseHttpStatusFromStatusMethod(line, responseParenthesesStartOffset, responseType);
                    break;

                default:
                    responseType.setReturnStatus("OK");
                    responseType.setReturnStatusCode("200");
                    break;
            }

            responseTypes.put(responseTypeKey, responseType);
        }
    }

    private void parseHttpStatusFromStatusMethod(final String line, final int responseParenthesesStartOffset, final ResponseType responseType) {

        int statusStatusCodeBeginningOffset = line.indexOf("Response.Status.", responseParenthesesStartOffset);

        if (statusStatusCodeBeginningOffset != -1) {

            int responseParenthesesEndOffset = line.indexOf(")", statusStatusCodeBeginningOffset);

            if (responseParenthesesEndOffset != -1) {

                String status = line.substring(statusStatusCodeBeginningOffset + 16, responseParenthesesEndOffset).trim();

                responseType.setReturnStatus(status);
                responseType.setReturnStatusCode(getStatusCodeFromStatusName(status));
            }
        }
    }

    private String getStatusCodeFromStatusName(final String statusName) {

        switch (statusName.toUpperCase()) {

            case "ACCEPTED":
                return "202";

            case "BAD_GATEWAY":
                return "502";

            case "BAD_REQUEST":
                return "400";

            case "CONFLICT":
                return "409";

            case "CREATED":
                return "201";

            case "EXPECTATION_FAILED":
                return "417";

            case "FORBIDDEN":
                return "403";

            case "FOUND":
                return "302";

            case "GATEWAY_TIMEOUT":
                return "504";

            case "GONE":
                return "410";

            case "HTTP_VERSION_NOT_SUPPORTED":
                return "505";

            case "INTERNAL_SERVER_ERROR":
                return "500";

            case "LENGTH_REQUIRED":
                return "411";

            case "METHOD_NOT_ALLOWED":
                return "405";

            case "MOVED_PERMANENTLY":
                return "301";

            case "NO_CONTENT":
                return "204";

            case "NOT_ACCEPTABLE":
                return "406";

            case "NOT_FOUND":
                return "404";

            case "NOT_IMPLEMENTED":
                return "501";

            case "NOT_MODIFIED":
                return "304";

            case "OK":
                return "200";

            case "PARTIAL_CONTENT":
                return "206";

            case "PAYMENT_REQUIRED":
                return "402";

            case "PRECONDITION_FAILED":
                return "412";

            case "PROXY_AUTHENTICATION_REQUIRED":
                return "407";

            case "REQUEST_ENTITY_TOO_LARGE":
                return "413";

            case "REQUEST_TIMEOUT":
                return "408";

            case "REQUEST_URI_TOO_LONG":
                return "414";

            case "REQUESTED_RANGE_NOT_SATISFIABLE":
                return "416";

            case "RESET_CONTENT":
                return "205";

            case "SEE_OTHER":
                return "303";

            case "SERVICE_UNAVAILABLE":
                return "503";

            case "TEMPORARY_REDIRECT":
                return "307";

            case "UNAUTHORIZED":
                return "401";

            case "UNSUPPORTED_MEDIA_TYPE":
                return "415";

            case "USE_PROXY":
                return "305";

            default:
                return "200";
        }
    }

    private void addVariableAndTypeToHashList(final String line) {

        int equalsOffset = line.indexOf("=");
        int equalityOperatorsOffset = line.indexOf("==");

        if ((equalsOffset != -1) && (equalityOperatorsOffset == -1)) {

            String[] variableList = line.substring(0, equalsOffset).split("\\s+");

            if (variableList.length >= 2) {

                String variableName = variableList[variableList.length - 1];
                String variableType = variableList[variableList.length - 2];

                if (!variableType.isEmpty()) {

                    responseTypeVariableTypes.put(variableName, variableType);
                }
            }
        }
    }

    private String getEnumValuesFromClass(final String className, final URLClassLoader urlClassLoader) {

        LOGGER.info(() -> "getEnumValuesFromClass(), Checking class " + className + " for enums");

        StringBuilder enumList = new StringBuilder();

        try {

            Class clazz = urlClassLoader.loadClass(className);

            if (clazz.isEnum()) {

                Object[] enums = clazz.getEnumConstants();

                for (int i = 0; i < enums.length; i++) {

                    enumList.append(enums[i].toString());
                    enumList.append(", ");
                }

                if (enums.length > 0) {

                    int lastIndex = enumList.lastIndexOf(", ");

                    enumList.delete(lastIndex, lastIndex + 2);
                }
            }
        } catch (ClassNotFoundException cnfe) {

            LOGGER.severe("getEnumValuesFromClass() in class " + className + ", ClassNotFoundException: " + cnfe.getMessage());
            return "";

        } catch (NoClassDefFoundError ncdfe) {

            LOGGER.severe("getEnumValuesFromClass() in class " + className + ", NoClassDefFoundError: " + ncdfe.getMessage());
            return "";
        }

        return enumList.toString();
    }

    private void addImportStatementToMap(final String line) {

        int importOffset = line.indexOf("import");

        if (importOffset != -1) {

            String[] importList = line.split("\\s+");

            if (importList.length > 0) {

                String lastPartOfRow = importList[importList.length - 1];

                // This is a import row
                int lastDot = lastPartOfRow.lastIndexOf('.');

                if (lastDot != -1) {

                    int importEndOffset = lastPartOfRow.indexOf(';', lastDot);

                    if (importEndOffset != -1) {

                        importClasses.put(lastPartOfRow.substring(lastDot + 1, importEndOffset), lastPartOfRow.substring(0, importEndOffset).trim());
                    }
                }
            }
        }
    }

    private String getSourceFileNameFromClassName(File sourceDiretory, String className) {

        return sourceDiretory.getAbsolutePath() + "/" + className.replaceAll("[.]", "/") + ".java";
    }

    private String getFullClassNameFromSourcesDir(final File sourceDiretory, final Path sourceNamePath) {

        String pathName;
        String className = "";
        int classIndex;

        StringBuilder packetAndclassName = new StringBuilder();

        boolean addDot = false;
        boolean addPackageName = false;

        int pathCount = sourceNamePath.getNameCount();

        for (int i = 0; i < pathCount; i++) {

            if (addDot == true) {

                packetAndclassName.append(".");
            }

            pathName = sourceNamePath.getName(i).toString();

            if (addPackageName == true) {

                classIndex = pathName.indexOf(".java");

                if (classIndex > 0) {

                    className = pathName.substring(0, classIndex);

                    packetAndclassName.append(className);

                } else {

                    packetAndclassName.append(pathName);
                    addDot = true;
                }
            } else if (pathName.equals(sourceDiretory.getName())) {

                addPackageName = true;
            }
        }

        return packetAndclassName.toString();
    }

    int isPathAnnotation(final String line) {

        int pathAnnotationOffset = line.indexOf("@Path");

        if (pathAnnotationOffset != -1) {

            int pathParamAnnotationOffset = line.indexOf("@PathParam", pathAnnotationOffset);

            if (pathParamAnnotationOffset == -1) {

                return pathAnnotationOffset;
            }
        }

        return -1;
    }

    String isResponseMethod(final String line) {

        int publicMethodOffset = line.indexOf("public ");

        if (publicMethodOffset != -1) {

            int responseOffset = line.indexOf("Response ", publicMethodOffset);
            int methodEndOffset = line.indexOf('(', responseOffset);

            if ((responseOffset != -1) && (methodEndOffset != -1)) {

                return line.substring(responseOffset + 9, methodEndOffset).trim();
            }
        }

        return null;
    }

    String isPublicMethod(final String line) {

        int publicMethodOffset = line.indexOf("public ");

        if (publicMethodOffset != -1) {

            int methodEndOffset = line.indexOf('(', publicMethodOffset);

            if (methodEndOffset != -1) {

                String[] pathList = line.substring(publicMethodOffset, methodEndOffset).split("\\s+");

                if (pathList.length > 1) {

                    return pathList[pathList.length - 2];
                }
            }
        }

        return null;
    }

    boolean isClass(final String line) {

        int classOffset = line.indexOf(" class ");
        int classStartOffset = line.indexOf('{', classOffset);

        if ((classOffset != -1) && (classStartOffset != -1)) {

            return true;
        }

        return false;
    }
}
