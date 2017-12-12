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
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.stream.Stream;
import static se.peter.ivarsson.rest.doc.parser.RestDocHandler.restInfo;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class JavaSourceParser {

    private static final Logger LOGGER = Logger.getLogger(JavaSourceParser.class.getSimpleName());

    private int javaDocStartIndexOffset = -1;
    private StringBuilder javaDocComment = null;
    private boolean javaDocReadingComments = false;
    private boolean javaDocEndReached = false;

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

    public void parseSourceFileForEnums(final File sourceDiretory, final HashMap<String,String> javaEnums, final Path sourceFilePath, final URLClassLoader urlClassLoader) {

        LOGGER.info(() -> "parseSourceFileForEnums(), Checking source file " + sourceFilePath + " for enums");

        String enumListForClass = getEnumValuesFromClass(sourceDiretory, sourceFilePath, urlClassLoader);

        // Read file into stream
        try (Stream<String> stream = Files.lines(Paths.get(sourceFilePath.toString()))) {

            final Boolean[] isClass = new Boolean[1];
            isClass[0] = false;

            stream.forEach(line -> {

                if (line.indexOf(" class ") != -1) {

                    isClass[0] = true;
                }

                findEnumsInFile(line, javaEnums, sourceFilePath, isClass[0], enumListForClass);
            });

        } catch (IOException ioe) {

            LOGGER.severe(() -> "parseSourceFileForEnums(), IOException: " + ioe.getMessage() + ", Can't find file: " + sourceFilePath);
        }
    }

    private void findEnumsInFile(final String line, final HashMap<String, String> javaEnums, final Path sourceFilePath, final Boolean inClass, final String enumListForClass) {

        // Search for public methods
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

    public String getEnumValuesFromClass(final File sourceDiretory, final Path sourceFilePath, final URLClassLoader urlClassLoader) {

        LOGGER.info(() -> "getEnumValues(), Checking class " + sourceFilePath + " for enums");

        StringBuilder enumList = new StringBuilder();

        String className = null;

        try {

            className = getFullClassNameFromSourcesDir(sourceDiretory, sourceFilePath);

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

            LOGGER.severe("getEnumValues() in class " + className + ", ClassNotFoundException: " + cnfe.getMessage());
            return "";

        } catch (NoClassDefFoundError ncdfe) {

            LOGGER.severe("getEnumValues, NoClassDefFoundError: " + ncdfe.getMessage());
            return "";
        }
    
        return enumList.toString();
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
