/*
 * Rest Documentation maven plugin.
 * 
 * Copyright (C) 2018 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.mojo;

import java.io.File;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class ConfigParameters {
    
    private File outputDirectory;
    private File loggingDirectory;
    private String projectTitle;
    private String openApiDocVersion;
    private String openApiLicenceName;
    private String openApiDevelopmentServerUrl;
    private String openApiStagingServerUrl;
    private String openApiProductionServerUrl;

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public File getLoggingDirectory() {
        return loggingDirectory;
    }

    public void setLoggingDirectory(File loggingDirectory) {
        this.loggingDirectory = loggingDirectory;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getOpenApiDocVersion() {
        return openApiDocVersion;
    }

    public void setOpenApiDocVersion(String openApiDocVersion) {
        this.openApiDocVersion = openApiDocVersion;
    }

    public String getOpenApiLicenceName() {
        return openApiLicenceName;
    }

    public void setOpenApiLicenceName(String openApiLicenceName) {
        this.openApiLicenceName = openApiLicenceName;
    }

    public String getOpenApiDevelopmentServerUrl() {
        return openApiDevelopmentServerUrl;
    }

    public void setOpenApiDevelopmentServerUrl(String openApiDevelopmentServerUrl) {
        this.openApiDevelopmentServerUrl = openApiDevelopmentServerUrl;
    }

    public String getOpenApiStagingServerUrl() {
        return openApiStagingServerUrl;
    }

    public void setOpenApiStagingServerUrl(String openApiStagingServerUrl) {
        this.openApiStagingServerUrl = openApiStagingServerUrl;
    }

    public String getOpenApiProductionServerUrl() {
        return openApiProductionServerUrl;
    }

    public void setOpenApiProductionServerUrl(String openApiProductionServerUrl) {
        this.openApiProductionServerUrl = openApiProductionServerUrl;
    }

    @Override
    public String toString() {
        return "configParameters{" + "outputDirectory=" + outputDirectory + ", loggingDirectory=" + loggingDirectory + ", projectTitle=" + projectTitle + ", openApiDocVersion=" + openApiDocVersion + ", openApiLicenceName=" + openApiLicenceName + ", openApiDevelopmentServerUrl=" + openApiDevelopmentServerUrl + ", openApiStagingServerUrl=" + openApiStagingServerUrl + ", openApiProductionServerUrl=" + openApiProductionServerUrl + '}';
    }
}
