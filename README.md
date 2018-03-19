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
    <groupId>se.peter.ivarsson.rest.doc</groupId>
    <artifactId>rest-doc-maven-plugin</artifactId>
    <version>1.0</version>
    <configuration>
        <!-- Specify the RESTDoc parameters -->
           <classesDirectory>${project.build.outputDirectory}</classesDirectory>
           <sourcesDirectory>${project.build.directory}/../src/main/java</sourcesDirectory>
           <outputDirectory>${project.build.directory}</outputDirectory>
           <loggingDirectory>${project.build.directory}</loggingDirectory>
           <projectTitle>${project.name}</projectTitle>
           <!-- outputType html or openapi -->
           <outputType>openapi</outputType>
           <!-- Mandatory openapi configuration parameters, if openapi output is chosen above -->
           <openApiDocVersion>${project.version}</openApiDocVersion>
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

## Show a yaml file

You can use swagger-ui to show the content in the yaml file created with this maven plugin.

## 'Rest Doc' Annotation types

The 'Rest Doc' recognizes 2 types of special annotations DocReturnType and DocListType.
These annotations needs rarely to be used.

| Annotation    | Comment                                                                                 |
|---------------|-----------------------------------------------------------------------------------------|
| DocReturnType | If you wan't to set an other return type than the default return type. Se example below |                                                      |

```
@POST
@Produces( { MediaType.APPLICATION_JSON } )
@Path( PATH_VALIDATE )
@DocReturnType( key = "se.cybercom.rest.doc.PaymentValidation" )
public Response validatePayment( ) {

   PaymentValidation paymentValidation = new PaymentValidation();

   return Response.ok( paymentValidation ).build();
}
```

| Annotation  | Comment                                                                                 |
|-------------|-----------------------------------------------------------------------------------------|
| DocListType | If the method returns a list of some kind ( java.util.List ), and you wan't specify what kind of list it is, use this annotation. Se example below |

```
:
private List<Movie> movies;
private List<String> movieVersions;
:
/**
* @return A list of movies.
*/
@DocListType( key = "se.cybercom.rest.doc.domain.Movie" )
public List getMovies() {

    return movies;
}
:

:
@DocListType( key = "String" )
public List getMovieVersions() {

    return movieVersions;
}
:
```

To use these annotations add the following dependencies into the pom file.

```
<dependency>
    <groupId>se.peter.ivarsson.rest.doc</groupId>
    <artifactId>rest-doc-types</artifactId>
    <version>1.0</version>
</dependency>
```

