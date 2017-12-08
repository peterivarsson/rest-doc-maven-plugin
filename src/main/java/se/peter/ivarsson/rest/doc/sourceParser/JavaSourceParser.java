/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.sourceParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.stream.Stream;

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

    public void parseClassForJavaDocComments(File sourceDiretory, HashMap<String, String> javaDocComments, String className) {

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

    private void parseFileForComments(String line, HashMap<String, String> javaDocComments, String className) {

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

    private void findMethodNameAndAddToHashMap(String line, HashMap<String, String> javaDocComments, String className) {

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

    public void parseSourceFileForEnums(File sourceDiretory, HashSet<String> javaEnums, Path sourceFilePath) {

        LOGGER.info(() -> "parseSourceFileForEnums(), Checking source file " + sourceFilePath + " for enums");

        // Read file into stream
        try (Stream<String> stream = Files.lines(Paths.get(sourceFilePath.toString()))) {

            final Boolean[] isClass = new Boolean[1];
            isClass[0] = false;
        
            stream.forEach(line -> {
                
                if(line.indexOf(" class ") != -1) {
                    
                    isClass[0] = true;
                }

                findEnumsInFile(line, javaEnums, sourceFilePath, isClass[0]);
            });

        } catch (IOException ioe) {

            LOGGER.severe(() -> "parseSourceFileForEnums(), IOException: " + ioe.getMessage() + ", Can't find file: " + sourceFilePath);
        }
    }

    private void findEnumsInFile(String line, HashSet<String> javaEnums, Path sourceFilePath, Boolean inClass) {

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
                    
                    if(inClass) {
                        
                        // Enum inside a class
                        int endJavaSuffixIndex = sourceFile.indexOf(".java", startSourcesFolderIndex);
                
                        enumTypeWithPath = sourceFile.substring(startSourcesFolderIndex, endJavaSuffixIndex).replace('/', '.') + '$' + enumType;
                        
                    } else {
                    
                        // This is a public enum
                        enumTypeWithPath = sourceFile.substring(startSourcesFolderIndex, endSourcesFolderIndex).replace('/', '.') + enumType;
                    }
                    
                    javaEnums.add(enumTypeWithPath);

                    LOGGER.info(() -> "findEnumsInFile(), Enum " + enumType + " found in file " + sourceFilePath);
                }
            }
    }

    private String getSourceFileNameFromClassName(File sourceDiretory, String className) {

        return sourceDiretory.getAbsolutePath() + "/" + className.replaceAll("[.]", "/") + ".java";
    }
}
