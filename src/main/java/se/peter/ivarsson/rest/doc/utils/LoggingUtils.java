/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2018 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class LoggingUtils {

    private static FileHandler fileHandler;

    private LoggingUtils() {

        throw new IllegalStateException("LoggingUtils class");
    }

    public static void addLoggingFileHandler(final File loggingDirectory, Logger logger) {

        try {

            if (fileHandler == null) {

                String logFilePath = loggingDirectory.getAbsolutePath() + "/RestDoc.log";

                fileHandler = new FileHandler(logFilePath, false);
                SimpleFormatter simpleFormatter = new SimpleFormatter();
                fileHandler.setFormatter(simpleFormatter);
            }

            logger.addHandler(fileHandler);

        } catch (IOException ioe) {

            logger.severe(() -> "IOException: " + ioe.getMessage());
        }
    }
}
