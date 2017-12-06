/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.javadoc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class JavaSourceParser {

    private static final Logger LOGGER = Logger.getLogger(JavaSourceParser.class.getSimpleName());

    private final HashMap<String, String> javaDocComments = new HashMap<>();
    private final HashMap<String, String> enumTypes = new HashMap<>();

    private int startIndexOffset = -1;
    private StringBuilder javaDocComment = null;
    private boolean readingComments = false;
    private boolean endReached = false;

    public HashMap<String, String> getEnumTypes() {

        return enumTypes;
    }

    public HashMap<String, String> parseJavaSource(File sourceDiretory, String className) {

        // Starting point
        startIndexOffset = -1;
        readingComments = false;
        endReached = false;

        javaDocComments.clear();
        enumTypes.clear();

        String javaFilePath = getSourceFileName(sourceDiretory, className);

        // Read file into stream
        try (Stream<String> stream = Files.lines(Paths.get(javaFilePath))) {

            stream.forEach(line -> {

                parseFileForComments(line, className);
            });

        } catch (IOException ioe) {

            LOGGER.severe(() -> "IOException: " + ioe.getMessage() + ", Can't find file: " + javaFilePath);
        }

        return javaDocComments;
    }

    private void parseFileForComments(String line, String className) {

        if (startIndexOffset < 0) {

            startIndexOffset = line.indexOf("/**");
        }

        if (endReached == false) {

            if ((startIndexOffset != -1) && (readingComments == false)) {

                javaDocComment = new StringBuilder(line);
                javaDocComment.append('\r');
                readingComments = true;

            } else {

                if (readingComments == true) {

                    javaDocComment.append(line.substring(startIndexOffset));
                    javaDocComment.append('\r');

                    int endIndexOffset = line.indexOf("*/");

                    if (endIndexOffset > 0) {

                        endReached = true;
                    }
                }
            }
        } else {

            // Search for public methods
            int publicMethodOffset = line.indexOf("public");

            if (publicMethodOffset != -1) {
                // Skip public
                findMethodNameAndAddToHashMap(line.substring(publicMethodOffset + 7), className);
            }
        }
    }

    private void findMethodNameAndAddToHashMap(String line, String className) {

        int endMethodNameOffset = line.indexOf('(');

        if (endMethodNameOffset != -1) {

            String methodString = line.substring(0, endMethodNameOffset).trim();

            int methodStartIndex = methodString.lastIndexOf(' ');

            if (methodStartIndex != -1) {

                String methodName = methodString.substring(methodStartIndex + 1);

                javaDocComments.put(methodName, javaDocComment.toString());
                startIndexOffset = -1;
                readingComments = false;
                endReached = false;
            }
        } else {

            int startEnumOffset = line.indexOf("enum");
            int endEnumOffset = line.indexOf('{');

            if ((startEnumOffset != -1) && (endEnumOffset != -1)) {

                // This is a public enum
                String enumType = line.substring(startEnumOffset + 5, endEnumOffset).trim();

                enumTypes.put(enumType, "enum");
            }

            // Skip this JavaDoc comment is not a part of method
            startIndexOffset = -1;
            readingComments = false;
            endReached = false;
        }
    }

    private String getSourceFileName(File sourceDiretory, String className) {

        return sourceDiretory.getAbsolutePath() + "/" + className.replaceAll("[.]", "/") + ".java";
    }
}
