/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.mojo;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import se.peter.ivarsson.rest.doc.html.HtmlOutput;
import se.peter.ivarsson.rest.doc.parser.RestDocHandler;
import se.peter.ivarsson.rest.doc.parser.RestInfo;

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

    @Override
    public void execute() throws MojoExecutionException {

        getLog().info("\nRESTDocMojo maven plugin STARTED executing\n");

        // avoid execution if output directory does not exist
        if (!classesDirectory.exists() || !classesDirectory.isDirectory()) {

            String error = "Can't find classes directory: " + classesDirectory;
            throw new MojoExecutionException(error);
        }

        // avoid execution if output directory does not exist
        if (!outputDirectory.exists() || !outputDirectory.isDirectory()) {

            String error = "Can't find output directory: " + outputDirectory;
            throw new MojoExecutionException(error);
        }

        RestDocHandler restDocHandler = new RestDocHandler(classesDirectory, outputDirectory);

        getLog().info("\nRESTDocMojo maven plugin creates HTML output files\n");
        
        HtmlOutput htmlOutput = new HtmlOutput();
        
        htmlOutput.createHTMLDocumantation(outputDirectory);

        getLog().info("\nRESTDocMojo maven plugin FINISHED executing\n");
    }

}
