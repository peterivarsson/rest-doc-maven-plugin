/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.output;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import se.peter.ivarsson.rest.doc.mojo.ConfigParameters;
import se.peter.ivarsson.rest.doc.parser.ClassInfo;
import se.peter.ivarsson.rest.doc.parser.FieldInfo;
import se.peter.ivarsson.rest.doc.parser.MehodInfoComparator;

import se.peter.ivarsson.rest.doc.parser.MethodInfo;
import se.peter.ivarsson.rest.doc.parser.ParameterInfo;
import se.peter.ivarsson.rest.doc.parser.RestDocHandler;
import se.peter.ivarsson.rest.doc.utils.LoggingUtils;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class HtmlOutput {

    private static final String FILE_TYPE = "file://";
    private static final String PATH_DELIMITER = "/";
    private static final String HTML_END_TAG = ".html>";
    private static final String HTML_END_TABLE_ONE = "\r\t\t</table><BR>";
    private static final String HTML_END_TABLE_TWO = "\r\r\t\t<table>";
    private static final String HTML_END_TABLE_ROW = "</td></tr>";
    private static final String HTML_END_TABLE_ROW_NEW_TABLE_DATA = "\r\t\t\t<tr><td>";
    private static final String HTML_NEW_TABLE_DATA = "</td><td>";
    private static final String HTML_NEW_ROW = "\r\r\t\t<tr>";
    private static final String HTML_NEW_ROW_HREF = "\r\t\t<a href=./";

    private static final Logger LOGGER = Logger.getLogger(HtmlOutput.class.getSimpleName());

    public void createHTMLDocumantation(final ConfigParameters configParameters) {

        LoggingUtils.addLoggingFileHandler(configParameters.getLoggingDirectory(), LOGGER);

        writeIndexHtmlFile(configParameters.getOutputDirectory(), configParameters.getProjectTitle());

        writeProgrammersInfoHtmlFile(configParameters.getOutputDirectory());

        writeResouresDocumentationToFiles(configParameters.getOutputDirectory());

        writeDomainDataToFiles(configParameters.getOutputDirectory());
    }

    private void writeIndexHtmlFile(final File outputDirectory, final String projectTitle) {

        LOGGER.info("writeIndexHtmlFile() write index.html");

        Path indexFilePath = Paths.get(URI.create(FILE_TYPE + outputDirectory.getAbsolutePath() + "/index.html"));

        final StringBuilder htmlBuffer = htmlHeader();

        htmlBodyHeader(htmlBuffer, "REST api for project: \"" + projectTitle + "\"");

        htmlRestResourcesList(htmlBuffer);

        htmlGoToProgrammersInfo(htmlBuffer);

        htmlFooter(htmlBuffer);

        writeHtmlToFile(indexFilePath, htmlBuffer);
    }

    private void htmlRestResourcesList(final StringBuilder htmlBuffer) {

        htmlBuffer.append("\r\r\t<ul>");

        LOGGER.info(() -> "htmlRestResourcesList()  restInfo.ClassInfo size = "
                + RestDocHandler.restInfo.getClassInfo().size());

        RestDocHandler.restInfo.getClassInfo().stream()
                .filter(classInfo -> classInfo.getMethodInfo() != null)
                .sorted((o1, o2) -> o1.getClassName().compareTo(o2.getClassName()))
                .forEach(res -> {

                    htmlBuffer.append("\r\t\t<li><a href=./");
                    htmlBuffer.append(res.getPackageAndClassName());
                    htmlBuffer.append(HTML_END_TAG);
                    htmlBuffer.append(res.getClassName());
                    htmlBuffer.append("</a> ");
                    htmlBuffer.append(res.getClassRootPath());
                    htmlBuffer.append(res.getClassPath());
                    htmlBuffer.append("</li>\r\t\t<BR>");
                });

        htmlBuffer.append("\r\t</ul>");
    }

    private void writeProgrammersInfoHtmlFile(final File outputDirectory) {

        LOGGER.info("writeProgrammersInfoHtmlFile() wrinte index.html");

        Path programmersInfoFilePath = Paths.get(URI.create(FILE_TYPE + outputDirectory.getAbsolutePath() + "/programmersinfo.html"));

        final StringBuilder htmlBuffer = htmlHeader();

        htmlBodyHeader(htmlBuffer, "Programmers Info");

        htmlGoHome(htmlBuffer);

        // DocReturnType
        htmlBuffer.append(HTML_END_TABLE_TWO);

        htmlBuffer.append("\r\t\t\t<tr><td>Annotation</td><td>Comment</td></tr>");
        htmlBuffer.append("\r\t\t\t<tr><td>DocReturnType</td><td>If you wan't to set an other return type than the default return type. <BR>Se example below</td></tr>");
        htmlBuffer.append("\r\t\t\t<tr><td colspan=2></td></tr>");
        htmlBuffer.append("\r\t\t\t<tr><td colspan=2>");

        htmlBuffer.append("@POST<BR>@Produces( { MediaType.APPLICATION_JSON } )<BR>@Path( PATH_VALIDATE )<BR>"
                + "<b>@DocReturnType( key = \"se.cybercom.rest.doc.PaymentValidation\" )</b><BR>"
                + "public Response validatePayment( ) {<BR><BR>"
                + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;PaymentValidation paymentValidation = new PaymentValidation();<BR><BR>"
                + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return Response.ok( paymentValidation ).build();<BR>}");

        htmlBuffer.append(HTML_END_TABLE_ROW);

        // DocListType
        htmlBuffer.append("\r\t\t\t<tr><td colspan=2><BR><BR></td></tr>");

        htmlBuffer.append("\r\t\t\t<tr><td>Annotation</td><td>Comment</td></tr>");
        htmlBuffer.append("\r\t\t\t<tr><td>DocListType</td><td>If the method returns a list of some kind ( java.util.List ),<BR>"
                + "and you wan't specify what kind of list it is, use this annotation. <BR>Se example below</td></tr>");
        htmlBuffer.append("\r\t\t\t<tr><td colspan=2></td></tr>");
        htmlBuffer.append("\r\t\t\t<tr><td colspan=2>");

        htmlBuffer.append(":<BR>private List&lt;Movie&gt; movies;<BR>"
                + "private List&lt;String&gt; movieVersions;<BR>"
                + ":<BR>/**<BR> * @return A list of movies.<BR> */<BR>"
                + "<b>@DocListType( key = \"se.cybercom.rest.doc.domain.Movie\" )</b><BR>"
                + "public List<Movie> getMovies() {<BR><BR>"
                + "&nbsp;&nbsp;&nbsp;&nbsp;return movies;<BR>}<BR>:<BR><BR>"
                + ":<BR><b>@DocListType( key = \"String\" )</b><BR>"
                + "public List<String> getMovieVersions() {<BR><BR>"
                + "&nbsp;&nbsp;&nbsp;&nbsp;return movieVersions;<BR>}<BR>:");

        htmlBuffer.append(HTML_END_TABLE_ROW);
        htmlBuffer.append(HTML_END_TABLE_ONE);

        htmlFooter(htmlBuffer);

        writeHtmlToFile(programmersInfoFilePath, htmlBuffer);
    }

    private void writeResouresDocumentationToFiles(final File outputDirectory) {

        LOGGER.info(() -> "writeResouresDocumentationToFiles() restInfo.ClassInfo size = "
                + RestDocHandler.restInfo.getClassInfo().size());

        RestDocHandler.restInfo.getClassInfo().stream().forEach(classInfo -> writeResouresDocumentationToFile(outputDirectory, classInfo));
    }

    private void writeResouresDocumentationToFile(final File outputDirectory, final ClassInfo classInfo) {

        LOGGER.info(() -> "writeResouresDocumentationToFile( " + classInfo.getClassName() + " )");

        Path programmersInfoFilePath = Paths.get(URI.create(FILE_TYPE + outputDirectory.getAbsolutePath() + PATH_DELIMITER + classInfo.getPackageAndClassName() + ".html"));

        final StringBuilder htmlBuffer = htmlHeader();

        htmlBodyHeader(htmlBuffer, classInfo.getClassName());

        htmlGoHome(htmlBuffer);

        htmlRestResourceDetail(htmlBuffer, classInfo.getClassName());

        htmlFooter(htmlBuffer);

        writeHtmlToFile(programmersInfoFilePath, htmlBuffer);
    }

    private void htmlRestResourceDetail(final StringBuilder htmlBuffer, final String resourceType) {

        List<MethodInfo> methodInfoList = new ArrayList<>();

        LOGGER.info(() -> "htmlRestResourceDetail() resourceType = " + resourceType);

        RestDocHandler.restInfo.getClassInfo().stream()
                .forEach(res -> {

                    if (res.getClassName().equals(resourceType)) {

                        if (res.getMethodInfo() == null) {

                            // No info in structure
                            return;

                        } else {

                            methodInfoList.addAll(res.getMethodInfo());
                        }
                    }
                });

        methodInfoList.sort(new MehodInfoComparator());

        final Integer[] methodNumber = new Integer[1];
        methodNumber[0] = 1;

        htmlBuffer.append("\r\t\t<ul>");

        methodInfoList.stream().forEach(method -> {

            htmlBuffer.append("\r\t\t\t<li><a href=\"#method");
            htmlBuffer.append(methodNumber[0]);
            htmlBuffer.append("\">");
            htmlBuffer.append(method.getHttpRequestType());
            htmlBuffer.append(" ");
            htmlBuffer.append(method.getMethodPath());
            htmlBuffer.append("</a></li><BR>");

            methodNumber[0]++;
        });

        htmlBuffer.append("\r\t\t</ul>");

        methodNumber[0] = 1;

        methodInfoList.stream().forEach(method -> {

            htmlMethodDetail(htmlBuffer, method, methodNumber[0]);

            methodNumber[0]++;
        });
    }

    private void htmlMethodDetail(final StringBuilder htmlBuffer, final MethodInfo methodInfo, final int methodNumber) {

        htmlBuffer.append("\r\r\r\t\t<p><a name=\"method");
        htmlBuffer.append(methodNumber);
        htmlBuffer.append("\"><h3>");
        htmlBuffer.append(methodInfo.getHttpRequestType());
        if (methodInfo.getMethodPath().equals("")) {

            htmlBuffer.append(" \"\"");

        } else {

            htmlBuffer.append(" ");
            htmlBuffer.append(methodInfo.getMethodPath());
        }
        if (methodInfo.isDeprecated()) {

            htmlBuffer.append(" - Deprecated");
        }
        htmlBuffer.append("</h3></a></p>");

        htmlBuffer.append(HTML_END_TABLE_TWO);

        if (methodInfo.getJavaDoc() != null) {

            htmlBuffer.append("\r\t\t\t<tr><td colspan=\"3\">");
            htmlBuffer.append(replaceCarriageReturnWithHtmlBreak(methodInfo.getJavaDoc()));
            htmlBuffer.append(HTML_END_TABLE_ROW);
        }

        htmlBuffer.append("\r\t\t\t<tr><td>Name</td><td>Class</td><td>Parameter type</td></tr>");

        List<ParameterInfo> parameterInfoList = methodInfo.getParameterInfo();

        parameterInfoList.stream().forEach(param -> {

            htmlBuffer.append(HTML_END_TABLE_ROW_NEW_TABLE_DATA);
            htmlBuffer.append(param.getParameterAnnotationName());
            htmlBuffer.append(HTML_NEW_TABLE_DATA);

            if (isDomainData(param.getParameterClassName())) {

                // Domain data
                htmlBuffer.append(HTML_NEW_ROW_HREF);
                htmlBuffer.append(param.getParameterClassName());
                htmlBuffer.append(HTML_END_TAG);
                htmlBuffer.append(param.getParameterClassName());
                htmlBuffer.append("</a>");

            } else {

                htmlBuffer.append(replaceLessThanAndGreaterThan(param.getParameterClassName()));
            }
            htmlBuffer.append(HTML_NEW_TABLE_DATA);

            switch (param.getParameterType()) {

                case "javax.ws.rs.PathParam":
                    htmlBuffer.append("Path parameter");
                    break;

                case "javax.ws.rs.HeaderParam":
                    htmlBuffer.append("Header parameter");
                    break;

                case "javax.ws.rs.QueryParam":
                    htmlBuffer.append("Query parameter");
                    break;

                default:
                    htmlBuffer.append(param.getParameterType());
                    break;
            }
            htmlBuffer.append(HTML_END_TABLE_ROW);
        });

        htmlBuffer.append(HTML_END_TABLE_ONE);

        htmlBuffer.append("\r\r\t\tJAX-RS Response");

        htmlBuffer.append("<BR><BR>\r\r\t\t<table>\r\t\t\t<tr><td>Element</td><td>Media Type</td><td>Default Status Code</td></tr>");

        htmlBuffer.append(HTML_END_TABLE_ROW_NEW_TABLE_DATA);

        if (((methodInfo.getReturnInfo().getAnnotatedReturnType() == null)
                || methodInfo.getReturnInfo().getAnnotatedReturnType().isEmpty())
                && !isDomainData(methodInfo.getReturnInfo().getReturnClassName())) {

            htmlBuffer.append(methodInfo.getReturnInfo().getReturnClassName());

        } else {

            if (isDomainData(methodInfo.getReturnInfo().getReturnClassName())) {

                // Domain data
                String returnClassName = methodInfo.getReturnInfo().getReturnClassName();
                int listIndex = returnClassName.indexOf("List<");

                if (listIndex == -1) {

                    // Not a List<>
                    htmlBuffer.append(HTML_NEW_ROW_HREF);
                    htmlBuffer.append(methodInfo.getReturnInfo().getReturnClassName());
                    htmlBuffer.append(HTML_END_TAG);
                    htmlBuffer.append(methodInfo.getReturnInfo().getReturnClassName());
                    htmlBuffer.append("</a>");

                } else {

                    // Is a List<>
                    String listType = returnClassName.substring(listIndex + 5, returnClassName.length() - 1);

                    htmlBuffer.append("\r\t\tList&lt;<a href=./");
                    htmlBuffer.append(listType);
                    htmlBuffer.append(HTML_END_TAG);
                    htmlBuffer.append(listType);
                    htmlBuffer.append("</a>&gt;");
                }
            } else {

                // Annotated ReturnType Domain data
                htmlBuffer.append(HTML_NEW_ROW_HREF);
                htmlBuffer.append(methodInfo.getReturnInfo().getAnnotatedReturnType());
                htmlBuffer.append(HTML_END_TAG);
                htmlBuffer.append(methodInfo.getReturnInfo().getAnnotatedReturnType());
                htmlBuffer.append("</a>");
            }
        }

        htmlBuffer.append(HTML_NEW_TABLE_DATA);

        htmlBuffer.append(methodInfo.getProduceType());

        htmlBuffer.append(HTML_NEW_TABLE_DATA);

        if (methodInfo.getReturnInfo().getReturnStatusAsText() == null) {

            htmlBuffer.append('-');

        } else {

            htmlBuffer.append(methodInfo.getReturnInfo().getReturnStatusAsText());
        }

        htmlBuffer.append("</td>\r\t\t\t</tr>\r\t\t</table><BR>");
    }

    private String replaceLessThanAndGreaterThan(final String inString) {

        return inString.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    private void writeDomainDataToFiles(final File outputDirectory) {

        LOGGER.info(() -> "writeDomainDataToFiles() restInfo.ClassInfo size = " + RestDocHandler.restInfo.getClassInfo().size());

        RestDocHandler.restInfo.getDomainDataMap().entrySet().stream()
                .filter(Objects::nonNull)
                .forEach(domainData -> writeDomainDataToFile(outputDirectory, domainData.getKey()));
    }

    private void writeDomainDataToFile(final File outputDirectory, final String domainDataType) {

        LOGGER.info(() -> "writeDomainDataToFile( " + domainDataType + " )");

        Path programmersInfoFilePath = Paths.get(URI.create(FILE_TYPE + outputDirectory.getAbsolutePath() + PATH_DELIMITER + domainDataType + ".html"));

        final StringBuilder htmlBuffer = htmlHeader();

        htmlBodyHeader(htmlBuffer, domainDataType);

        htmlGoHome(htmlBuffer);

        htmlRestResourceDomainData(htmlBuffer, domainDataType);

        htmlFooter(htmlBuffer);

        writeHtmlToFile(programmersInfoFilePath, htmlBuffer);
    }

    private void htmlRestResourceDomainData(final StringBuilder htmlBuffer, final String domainDataType) {

        LOGGER.info(() -> "htmlRestResourceDomainData() domainDataType = " + domainDataType);

        Map domainData = RestDocHandler.restInfo.getDomainDataMap();

        if (domainData.get(domainDataType) == null) {

            LOGGER.severe(() -> "htmlRestResourceDomainData() domainDataType = " + domainDataType + "Not found in DomainData map");
            return;
        }

        final List<FieldInfo> fields = RestDocHandler.restInfo.getDomainDataMap().get(domainDataType).getFields();

        htmlBuffer.append(HTML_END_TABLE_TWO);

        if (RestDocHandler.restInfo.getDomainDataMap().get(domainDataType).getInfo() != null) {

            htmlBuffer.append("\r\t\t\t<tr><td colspan=3>" + RestDocHandler.restInfo.getDomainDataMap().get(domainDataType).getInfo() + HTML_END_TABLE_ROW);
            htmlBuffer.append("\r\t\t\t<tr><td colspan=3></td></tr>");
        }

        htmlBuffer.append("\r\t\t\t<tr><td>Field name</td><td>Field type</td><td>Type in list</td></tr>");

        fields.stream().forEach(field -> {

            htmlBuffer.append(HTML_END_TABLE_ROW_NEW_TABLE_DATA);

            htmlBuffer.append(field.getFieldName());

            htmlBuffer.append(HTML_NEW_TABLE_DATA);
            htmlBuffer.append(field.getFieldType());
            htmlBuffer.append(HTML_NEW_TABLE_DATA);

            if (!field.getFieldOfType().isEmpty()) {

                if (field.getFieldOfType().contains(".")
                        && !field.getFieldOfType().startsWith("java.")) {

                    // Domain data
                    htmlBuffer.append(HTML_NEW_ROW_HREF);
                    htmlBuffer.append(field.getFieldOfType());
                    htmlBuffer.append(HTML_END_TAG);
                    htmlBuffer.append(field.getFieldOfType());
                    htmlBuffer.append("</a>");

                } else {

                    // This is a Primitive Data Type
                    htmlBuffer.append(field.getFieldOfType());
                }
            } else {

                // This is not a list
                htmlBuffer.append("-");
            }

            htmlBuffer.append(HTML_END_TABLE_ROW);
        });

        htmlBuffer.append(HTML_END_TABLE_ONE);
    }

    private StringBuilder htmlHeader() {

        final StringBuilder htmlBuffer = new StringBuilder(4096);

        htmlBuffer.append("<html lang=\"en\">\r\t<head>");
        htmlBuffer.append("\r\r\t<style>\r\t\ttable, th, td {\r\t\t\tborder: 1px solid;\r\t\t\tborder-collapse: collapse;\r\t\t\tborder-color: #D6D6C2;\r\t\t}\r\t\tth, td {\r\t\t\tpadding: 8px;\r\t\t}\r\t</style>");
        htmlBuffer.append("\r\r\t\t<title>REST documentation</title>\r\t</head>\r\r\t<body>");

        return htmlBuffer;
    }

    private void htmlBodyHeader(final StringBuilder htmlBuffer, String headerText) {

        htmlBuffer.append("\r\t\t<a name=\"top\"><h1>");
        htmlBuffer.append(headerText);
        htmlBuffer.append("</h1></a>");
    }

    private void htmlGoToProgrammersInfo(final StringBuilder htmlBuffer) {

        htmlBuffer.append("\r\r\t<ul style=\"list-style-type: none\">");
        htmlBuffer.append("\r\t\t<li><a href=./programmersinfo.html><h4>Programmers Information</h4></a></li>");
        htmlBuffer.append("\r\t</ul>");
    }

    private void htmlFooter(final StringBuilder htmlBuffer) {

        htmlBuffer.append("\r\r\t<table style=\"border: none;\">");
        htmlBuffer.append(HTML_NEW_ROW);
        htmlBuffer.append("\r\t\t\t<td style=\"border: none;\"><a href=\"#top\"><h4>To top</h4></a></td><td style=\"border: none;\"><a href=./index.html><h4>Home</h4></a></td>");
        htmlBuffer.append("\r\r\t\t</tr>");
        htmlBuffer.append("\r\r\t\t<tr></tr>");
        htmlBuffer.append(HTML_NEW_ROW);
        htmlBuffer.append("\r\t\t\t<td style=\"border: none;\"><h4>&copy; Copyright Peter Ivarsson</h4></td>");
        htmlBuffer.append("\r\r\t\t</tr>");
        htmlBuffer.append("\r\r\t</table>");
        htmlBuffer.append("\r\t</body>\r</html>");
    }

    private void htmlGoHome(final StringBuilder htmlBuffer) {

        htmlBuffer.append("\r\r\t<table style=\"border: none;\">");
        htmlBuffer.append(HTML_NEW_ROW);
        htmlBuffer.append("\r\t\t\t<td style=\"border: none;\"><a href=./index.html><h4>Home</h4></a></td>");
        htmlBuffer.append(HTML_NEW_ROW);
        htmlBuffer.append("\r\r\t<table>");
    }

    private void writeHtmlToFile(final Path indexFilePath, final StringBuilder htmlBuffer) {

        try {

            Files.write(indexFilePath, htmlBuffer.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ioe) {

            LOGGER.severe("writeHtmlToFile() Path ='" + indexFilePath.toString() + "' IOException: " + ioe.getMessage());
        }
    }

    private boolean isDomainData(final String parameterName) {

        // Check for 'Java classes' or 'Primitive Data Types'
        switch (parameterName) {

            case "byte":
            case "short":
            case "int":
            case "long":
            case "float":
            case "double":
            case "boolean":
            case "char":
                // Is a Primitive Data Type
                return false;

            default:
                return !parameterName.startsWith("java");
        }
    }

    String replaceCarriageReturnWithHtmlBreak(String javaDocComments) {

        return javaDocComments.replaceAll("\r", "\r<BR>");
    }
}
