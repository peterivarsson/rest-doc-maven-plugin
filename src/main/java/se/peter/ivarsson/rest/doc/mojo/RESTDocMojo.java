/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.mojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import se.peter.ivarsson.rest.doc.html.HtmlOutput;
import se.peter.ivarsson.rest.doc.parser.RestDocHandler;

/**
 * Maven goal which create REST documentetion.
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 * @goal restdoc
 * @aggregator
 */
public class RESTDocMojo extends AbstractMojo {

    /**
     * File path where to find the classses files ( a src directory )
     *
     * @parameter property="classesDirectory"
     * @required
     * @readonly
     */
    private File classesDirectory;

    /**
     * File path where to put the HTML output files
     *
     * @parameter property="outputDirectory"
     * @required
     * @readonly
     */
    private File outputDirectory;

    /**
     * File path where to put the logging files
     *
     * @parameter property="loggingDirectory"
     * @required
     * @readonly
     */
    private File loggingDirectory;

    @Override
    public void execute() throws MojoExecutionException {

        getLog().info("\nRESTDocMojo maven plugin STARTED executing\n");

        // avoid execution if classes directory does not exist
        if (!classesDirectory.exists() || !classesDirectory.isDirectory()) {

            String error = "Can't find classes directory: " + classesDirectory;
            throw new MojoExecutionException(error);
        }

        // Create output directory if directory does not exist
        if (!outputDirectory.exists()) {

            Path outputPath = Paths.get(outputDirectory.getAbsolutePath());
            
            try {

                Files.createDirectories(outputPath);
                
            } catch(IOException ioe) {
                
                String error = "Can't create output directory: " + outputDirectory;
                throw new MojoExecutionException(error);
            }
        }

        // Create log directory if directory does not exist
        if (!loggingDirectory.exists()) {

            Path logPath = Paths.get(loggingDirectory.getAbsolutePath());
            
            try {

                Files.createDirectories(logPath);
                
            } catch(IOException ioe) {
                
                String error = "Can't create log directory: " + outputDirectory;
                throw new MojoExecutionException(error);
            }
        }

        new RestDocHandler(classesDirectory, loggingDirectory);

        getLog().info("\nRESTDocMojo maven plugin creates HTML output files\n");

        HtmlOutput htmlOutput = new HtmlOutput();

        htmlOutput.createHTMLDocumantation(outputDirectory);

        getLog().info("\nRESTDocMojo maven plugin FINISHED executing\n");
    }

}
