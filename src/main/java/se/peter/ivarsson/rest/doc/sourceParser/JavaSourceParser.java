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
import se.peter.ivarsson.rest.doc.parser.ResponseType;
import sun.reflect.generics.tree.ReturnType;

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

    // Respose type parsing
    private final HashMap<String, String> importClasses = new HashMap<>();
    private final HashMap<String, String> variableTypes = new HashMap<>();
    private boolean pathAnnotationFound = false;
    private boolean publicReponseFound = false;
    private String responseMethodName = "";

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

    public void parseSourceFile(final File sourceDiretory, final HashMap<String, String> javaEnums, final HashMap<String, ResponseType> responseTypes, final Path sourceFilePath, final URLClassLoader urlClassLoader) {

        LOGGER.info(() -> "parseSourceFile(), Checking source file " + sourceFilePath + " for enums and response types");

        importClasses.clear();
        pathAnnotationFound = false;
        publicReponseFound = false;

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

        if (pathAnnotationFound == false) {

            // Search for enum in public methods
            int pathAnnotationOffset = line.indexOf("@Path");
            int pathParamAnnotationOffset = line.indexOf("PathParam");

            if ((pathAnnotationOffset != -1) && (pathParamAnnotationOffset == -1)) {

                pathAnnotationFound = true;
                publicReponseFound = false;
            }
            return; //  No path found yet
        }

        if (publicReponseFound == false) {

            int publicMethodOffset = line.indexOf("public ");
            int responseOffset = line.indexOf("Response ", publicMethodOffset);
            int methodEndOffset = line.indexOf('(', responseOffset);

            if ((publicMethodOffset != -1) && (responseOffset != -1) && (methodEndOffset != -1)) {

                publicReponseFound = true;
                responseMethodName = line.substring(responseOffset + 9, methodEndOffset).trim();
                variableTypes.clear();
            }
        } else {

            // Search for real 'Response.ok('
            int returnOffset = line.indexOf("return");
            int responseOffset = line.indexOf("Response", returnOffset);
            int responseOkOffset = line.indexOf(".ok", responseOffset);

            if ((returnOffset != -1) && (responseOffset != -1)) {

                String responseTypeKey = className + "-" + responseMethodName;

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

                                if (variableTypes.containsKey(variableName)) {

                                    ResponseType responseType = new ResponseType();
                                    responseType.setReturnStatus("OK");

                                    String variableType = variableTypes.get(variableName);

                                    int listIndex = variableType.indexOf("List<");

                                    if (listIndex == -1) {

                                        // No list
                                        if (importClasses.containsKey(variableTypes.get(variableName))) {

                                            responseType.setReturnType(importClasses.get(variableTypes.get(variableName)));

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
                pathAnnotationFound = false;
                publicReponseFound = false;

            } else {

                // Response.ok not found
                // Save all variables in the variableTypes HashList
                addVariableAndTypeToHashList(line);
            }
        }
    }

    private void findOutHttpStatusCode(final String line, final int responseOffset, final String responseTypeKey,
            final HashMap<String, ResponseType> responseTypes) {

        String status = "";

        int responseDotOffset = line.indexOf(".", responseOffset);
        int responseParenthesesStartOffset = line.indexOf("(", responseDotOffset);

        if ((responseDotOffset != -1) && (responseDotOffset != -1)) {

            ResponseType responseType = new ResponseType();

            String httpStatusMethod = line.substring(responseOffset + 9, responseParenthesesStartOffset);

            switch (httpStatusMethod) {

                case "accepted":
                    responseType.setReturnStatus("ACCEPTED");
                    break;

                case "noContent":
                    responseType.setReturnStatus("NO_CONTENT");
                    break;

                case "notAcceptable":
                    responseType.setReturnStatus("NOT_ACCEPTABLE");
                    break;

                case "notModified":
                    responseType.setReturnStatus("NOT_MODIFIED");
                    break;

                case "ok":
                    responseType.setReturnStatus("OK");
                    break;

                case "serverError":
                    responseType.setReturnStatus("INTERNAL_SERVER_ERROR");
                    break;

                case "status":
                    responseType.setReturnStatus(parseHttpStatusFromStatusMethod(line, responseParenthesesStartOffset));
                    break;

                default:
                    break;
            }

            responseTypes.put(responseTypeKey, responseType);
        }
    }

    private String parseHttpStatusFromStatusMethod(final String line, final int responseParenthesesStartOffset) {

        int statusStatusCodeBeginningOffset = line.indexOf("Response.Status.", responseParenthesesStartOffset);

        if (statusStatusCodeBeginningOffset != -1) {

            int responseParenthesesEndOffset = line.indexOf(")", statusStatusCodeBeginningOffset);

            if (responseParenthesesEndOffset != -1) {

                return  line.substring(statusStatusCodeBeginningOffset + 16, responseParenthesesEndOffset).trim();
            }
        }
        
        return null;
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

                    variableTypes.put(variableName, variableType);
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

            // This is a import row
            int lastDot = line.lastIndexOf('.');

            if (lastDot != -1) {

                int importEndOffset = line.indexOf(';', lastDot);

                if (importEndOffset != -1) {

                    importClasses.put(line.substring(lastDot + 1, importEndOffset), line.substring(importOffset + 6, importEndOffset).trim());
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
}
