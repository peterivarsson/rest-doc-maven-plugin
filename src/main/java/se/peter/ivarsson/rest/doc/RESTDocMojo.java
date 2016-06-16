/*
 * Copyright (C) 2016 Peter Ivarsson 
 *
 */

package se.peter.ivarsson.rest.doc;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;


/**
 * Maven goal which create REST documentetion.
 *
 * @author Peter Ivarsson     Peter.Ivarsson@cybercom.com
 * @goal   rest-doc
 * @phase  process-classes
 */
public class RESTDocMojo extends AbstractMojo {

    /**
     * File path where to find the classses files ( a src directory )
     * 
     * @parameter property="file.path.for.classes"
     * @required
     * @readonly
     */
    private File classesDirectory;

    /**
     * File path where to put the HTML output files
     * 
     * @parameter property="file.path.for.html.output.directory"
     * @required
     * @readonly
     */
    private File outputDirectory;


    @Override
    public void execute() throws MojoExecutionException {

        // avoid execution if output directory does not exist
        if( ! classesDirectory.exists() || ! classesDirectory.isDirectory() ) {
           
            getLog().info( "Can't find classes directory: " + classesDirectory );
            return;
        }

        // avoid execution if output directory does not exist
        if( ! outputDirectory.exists() || ! outputDirectory.isDirectory() ) {
           
            getLog().info( "Can't find output directory: " + outputDirectory );
            return;
        }

    }
   
}
