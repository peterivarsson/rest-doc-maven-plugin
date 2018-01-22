# rest-doc-maven-plugin for JAX-RS documentation

## What does this Maven plugin do

It parses the classes and sources files for JAX-RS Annotations and JavaDoc comments.
It creates a index.html or a yaml file in "output" directory.

## Plugin configuration

You need to config all 4 directories in your pom.xml (classesDirectory, sourcesDirectory, outputDirectory and loggingDirectory).
Also projectTitle is mandatory.
If output is set to openApi additional config parameters is used, see below.

Put below configurations in your pom.xml, and modify the paths for your needs.

```
<plugin>
    <groupId>rest.doc.plugin</groupId>
    <artifactId>rest-doc-maven-plugin</artifactId>
    <version>${project.version}</version>
    <configuration>
        <!-- Specify the RESTDoc parameters -->
        <classesDirectory>/Users/piv/Documents/NetBeansProjects/rest-doc-test-mobilebackend-api/target/classes</classesDirectory>
        <sourcesDirectory>/Users/piv/Documents/NetBeansProjects/rest-doc-test-mobilebackend-api/src/main/java</sourcesDirectory>
        <outputDirectory>/Users/piv/Documents/NetBeansProjects/rest-doc-output-directory</outputDirectory>
        <loggingDirectory>/Users/piv/Documents/NetBeansProjects/rest-doc-logging-directory</loggingDirectory>
        <projectTitle>Mobile backend</projectTitle>
        <!-- outputType html or openapi -->
        <outputType>html</outputType>
        <!-- Mandatory openapi configuration parameters, if openapi output is chosen above -->
           <openApiDocVersion>1.0.0</openApiDocVersion>
           <openApiLicenceName>Apache 2.0</openApiLicenceName>
           <!-- openapi at least one of the servers below is needed to be defined -->
           <openApiDevelopmentServerUrl>https://test.cybercom.com/services</openApiDevelopmentServerUrl>
           <openApiStagingServerUrl>https://staging.cybercom.com/services</openApiStagingServerUrl>
           <openApiProductionServerUrl>https://production.cybercom.com/services</openApiProductionServerUrl>
    </configuration>

    <executions>
        <execution>
            <goals>
                <goal>restdoc</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```