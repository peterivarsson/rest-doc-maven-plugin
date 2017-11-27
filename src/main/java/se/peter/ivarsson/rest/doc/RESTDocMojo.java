/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

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
     * File path to Jar or War file
     *
     * @parameter property="analyzeJar"
     * @required
     * @readonly
     */
    private File analyzeJar;

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

        getLog().info("\nExecuting RESTDocMojo maven plugin\n");

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

        RestDocHandler restDocHandler = new RestDocHandler(classesDirectory, outputDirectory, getLog());

    }

}
