# rest-doc-maven-plugin for JAX-RS documentation

## What does this Maven plugin do

It parses the classes and sources files for JAX-RS Annotation and JavaDoc comments.
It creates a index.html file in "output" directory.

## plugin configuration

You need to config all 4 directories in your pom.xml (classesDirectory, sourcesDirectory, outputDirectory and loggingDirectory)

Put below configurations in your pom.xml, and modify the paths for your needs.

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
    </configuration>

    <executions>
        <execution>
            <goals>
                <goal>restdoc</goal>
            </goals>
        </execution>
    </executions>
</plugin>
