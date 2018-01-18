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
import se.peter.ivarsson.rest.doc.html.OpenApiOutput;
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
     * File path where to find the classes files ( the classes directory )
     *
     * @parameter property="classesDirectory"
     * @required
     * @readonly
     */
    private File classesDirectory;

    /**
     * File path where to find the source files ( the sources directory )
     *
     * @parameter property="sourcesDirectory"
     * @required
     * @readonly
     */
    private File sourcesDirectory;

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

    /**
     * Title of this project "Hello world"
     *
     * @parameter property="projectTitle"
     * @required
     * @readonly
     */
    private String projectTitle;

    /**
     * Output Type html or openapi
     *
     * @parameter property="outputType"
     * @required
     * @readonly
     */
    private String outputType;

    /**
     * Version of the created OpenApi documentation
     *
     * @parameter property="openApiDocVersion"
     * @required
     * @readonly
     */
    private String openApiDocVersion;

    /**
     * Licence information for the exposed API.
     *
     * @parameter property="openApiLicenceName"
     * @required
     * @readonly
     */
    private String openApiLicenceName;

    /**
     * Development server where you can test your application.
     *
     * @parameter property="openApiDevelopmentServerUrl"
     * @required
     * @readonly
     */
    private String openApiDevelopmentServerUrl;

    /**
     * Staging server where you can test your application.
     *
     * @parameter property="openApiStagingServerUrl"
     * @required
     * @readonly
     */
    private String openApiStagingServerUrl;

    /**
     * Production server where you can test your application.
     *
     * @parameter property="openApiProductionServerUrl"
     * @required
     * @readonly
     */
    private String openApiProductionServerUrl;

    @Override
    public void execute() throws MojoExecutionException {

        getLog().info("\nRESTDocMojo maven plugin STARTED executing\n");

        // avoid execution if classes directory does not exist
        if (!classesDirectory.exists() || !classesDirectory.isDirectory()) {

            String error = "Can't find classes directory: " + classesDirectory;
            throw new MojoExecutionException(error);
        }

        // avoid execution if sources directory does not exist
        if (!sourcesDirectory.exists() || !sourcesDirectory.isDirectory()) {

            String error = "Can't find sources directory: " + sourcesDirectory;
            throw new MojoExecutionException(error);
        }

        // Create output directory if directory does not exist
        if (!outputDirectory.exists()) {

            Path outputPath = Paths.get(outputDirectory.getAbsolutePath());

            try {

                Files.createDirectories(outputPath);

            } catch (IOException ioe) {

                String error = "Can't create output directory: " + outputDirectory;
                throw new MojoExecutionException(error);
            }
        }

        // Create log directory if directory does not exist
        if (!loggingDirectory.exists()) {

            Path logPath = Paths.get(loggingDirectory.getAbsolutePath());

            try {

                Files.createDirectories(logPath);

            } catch (IOException ioe) {

                String error = "Can't create log directory: " + outputDirectory;
                throw new MojoExecutionException(error);
            }
        }

        // Project title
        if ((projectTitle == null) || projectTitle.isEmpty()) {

            String error = "Missing project title";
            throw new MojoExecutionException(error);
        }

        // Output Type html or openapi
        if ((outputType == null) || outputType.isEmpty()) {

            String error = "Missing output type (html or openapi)";
            throw new MojoExecutionException(error);
        }

        new RestDocHandler(classesDirectory, sourcesDirectory, loggingDirectory);

        if (outputType.equals("html")) {

            getLog().info("\nRESTDocMojo maven plugin creates HTML output files\n");

            HtmlOutput htmlOutput = new HtmlOutput();

            htmlOutput.createHTMLDocumantation(outputDirectory, projectTitle);

        } else {

            if ((openApiDocVersion == null) || openApiDocVersion.isEmpty()) {

                String error = "Missing version of the created OpenApi documentation";
                throw new MojoExecutionException(error);
            }

            if ((openApiLicenceName == null) || openApiLicenceName.isEmpty()) {

                String error = "Missing licence information for the exposed API";
                throw new MojoExecutionException(error);
            }

            if (((openApiDevelopmentServerUrl == null) || openApiDevelopmentServerUrl.isEmpty())
                    && ((openApiStagingServerUrl == null) || openApiStagingServerUrl.isEmpty())
                    && ((openApiProductionServerUrl == null) || openApiProductionServerUrl.isEmpty())) {

                String error = "Missing server URL for testing this API (All 3 URLs is missing)";
                throw new MojoExecutionException(error);
            }

            getLog().info("\nRESTDocMojo maven plugin creates OpenApi output file\n");

            OpenApiOutput openApiOutput = new OpenApiOutput();

            openApiOutput.createOpenApiDocumantation(outputDirectory, projectTitle, openApiDocVersion, openApiLicenceName,
                    openApiDevelopmentServerUrl, openApiStagingServerUrl, openApiProductionServerUrl);
        }

        getLog().info("\nRESTDocMojo maven plugin FINISHED executing\n");
    }
}
