/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.html;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import se.peter.ivarsson.rest.doc.parser.ClassInfo;
import se.peter.ivarsson.rest.doc.parser.FieldInfo;

import se.peter.ivarsson.rest.doc.parser.MethodInfo;
import se.peter.ivarsson.rest.doc.parser.ParameterInfo;
import se.peter.ivarsson.rest.doc.parser.RestDocHandler;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class HtmlOutput {

    private static final Logger LOGGER = Logger.getLogger(HtmlOutput.class.getSimpleName());

    private static int methodNumber = -1;

    public void createHTMLDocumantation(final File outputDirectory) {

        writeIndexHtmlFile(outputDirectory);

        writeProgrammersInfoHtmlFile(outputDirectory);

        writeResouresDocumentationToFiles(outputDirectory);

        writeDomainDataToFiles(outputDirectory);
    }

    private void writeIndexHtmlFile(final File outputDirectory) {

        LOGGER.info("writeIndexHtmlFile() wrinte index.html");

        Path indexFilePath = Paths.get(URI.create("file://" + outputDirectory.getAbsolutePath() + "/index.html"));

        final StringBuffer htmlBuffer = htmlHeader();

        htmlBodyHeader(htmlBuffer, "REST api");

        htmlRestResourcesList(htmlBuffer);

        htmlGoToProgrammersInfo(htmlBuffer);

        htmlFooter(htmlBuffer);

        writeHtmlToFile(indexFilePath, htmlBuffer);
    }

    private void htmlRestResourcesList(final StringBuffer htmlBuffer) {

        htmlBuffer.append("\r\r\t<ul>");

        LOGGER.info("htmlRestResourcesList()  restInfo.ClassInfo size = "
                + RestDocHandler.restInfo.getClassInfo().size());

        RestDocHandler.restInfo.getClassInfo().stream()
                .filter(classInfo -> classInfo.getMethodInfo() != null)
                .sorted(new Comparator<ClassInfo>() {

                    @Override
                    public int compare(ClassInfo o1, ClassInfo o2) {

                        return o1.getClassName().compareTo(o2.getClassName());
                    }
                })
                .forEach((res) -> {

                    htmlBuffer.append("\r\t\t<li><a href=./");
                    htmlBuffer.append(res.getPackageAndClassName());
                    htmlBuffer.append(".html>");
                    htmlBuffer.append(res.getClassName());
                    htmlBuffer.append("</a></li>\r\t\t<BR>");
                });

        htmlBuffer.append("\r\t</ul>");
    }

    private void writeProgrammersInfoHtmlFile(final File outputDirectory) {

        LOGGER.info("writeProgrammersInfoHtmlFile() wrinte index.html");

        Path programmersInfoFilePath = Paths.get(URI.create("file://" + outputDirectory.getAbsolutePath() + "/programmersinfo.html"));

        final StringBuffer htmlBuffer = htmlHeader();

        htmlBodyHeader(htmlBuffer, "Programmers Info");

        htmlGoHome(htmlBuffer);

        // DocReturnType
        htmlBuffer.append("\r\r\t\t<table>");

        htmlBuffer.append("\r\t\t\t<tr><td>Annotation</td><td>Comment</td></tr>");
        htmlBuffer.append("\r\t\t\t<tr><td>DocReturnType</td><td>If you wan't to set an other return type than the default return type. <BR>Se example below</td></tr>");
        htmlBuffer.append("\r\t\t\t<tr><td colspan=2></td></tr>");
        htmlBuffer.append("\r\t\t\t<tr><td colspan=2>");

        htmlBuffer.append("@POST<BR>@Produces( { MediaType.APPLICATION_JSON } )<BR>@Path( PATH_VALIDATE )<BR>"
                + "<b>@DocReturnType( key = \"se.cybercom.rest.doc.PaymentValidation\" )</b><BR>"
                + "public Response validatePayment( ) {<BR><BR>"
                + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;PaymentValidation paymentValidation = new PaymentValidation();<BR><BR>"
                + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return Response.ok( paymentValidation ).build();<BR>}");

        htmlBuffer.append("</td></tr>");

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

        htmlBuffer.append("</td></tr>");
        htmlBuffer.append("\r\t\t</table><BR>");

        htmlFooter(htmlBuffer);

        writeHtmlToFile(programmersInfoFilePath, htmlBuffer);
    }

    private void writeResouresDocumentationToFiles(final File outputDirectory) {

        LOGGER.info("writeResouresDocumentationToFiles() restInfo.ClassInfo size = "
                + RestDocHandler.restInfo.getClassInfo().size());

        RestDocHandler.restInfo.getClassInfo().stream().forEach(classInfo -> {

            writeResouresDocumentationToFile(outputDirectory, classInfo);
        });
    }

    private void writeResouresDocumentationToFile(final File outputDirectory, final ClassInfo classInfo) {

        LOGGER.info("writeResouresDocumentationToFile( " + classInfo.getClassName() + " )");

        Path programmersInfoFilePath = Paths.get(URI.create("file://" + outputDirectory.getAbsolutePath() + "/" + classInfo.getPackageAndClassName() + ".html"));

        final StringBuffer htmlBuffer = htmlHeader();

        htmlBodyHeader(htmlBuffer, classInfo.getClassName());

        htmlGoHome(htmlBuffer);

        htmlRestResourceDetail(htmlBuffer, classInfo.getClassName());

        htmlFooter(htmlBuffer);

        writeHtmlToFile(programmersInfoFilePath, htmlBuffer);
    }

    private void htmlRestResourceDetail(final StringBuffer htmlBuffer, final String resourceType) {

        List<MethodInfo> methodInfoList = new ArrayList<>();

        LOGGER.info("htmlRestResourceDetail() resourceType = " + resourceType);

        RestDocHandler.restInfo.getClassInfo().stream().forEach((res) -> {

            if (res.getClassName().equals(resourceType)) {

                if (res.getMethodInfo() == null) {

                    // No info in structure
                    return;

                } else {

                    methodInfoList.addAll(res.getMethodInfo());
                }
            }
        });

        methodNumber = 1;

        htmlBuffer.append("\r\t\t<ul>");

        methodInfoList.stream().forEach((method) -> {

            htmlBuffer.append("\r\t\t\t<li><a href=\"#method");
            htmlBuffer.append(methodNumber);
            htmlBuffer.append("\">");
            htmlBuffer.append(method.getHttpRequestType());
            htmlBuffer.append(" ");
            if (method.getRestPath().equals("")) {

                htmlBuffer.append("\"\"");

            } else {

                htmlBuffer.append(method.getRestPath());
            }
            htmlBuffer.append("</a></li><BR>");

            methodNumber++;
        });

        htmlBuffer.append("\r\t\t</ul>");

        methodNumber = 1;

        methodInfoList.stream().forEach((method) -> {

            htmlMethodDetail(htmlBuffer, method, methodNumber);

            methodNumber++;
        });
    }

    private void htmlMethodDetail(final StringBuffer htmlBuffer, final MethodInfo methodInfo, final int methodNumber) {

        htmlBuffer.append("\r\r\r\t\t<p><a name=\"method");
        htmlBuffer.append(methodNumber);
        htmlBuffer.append("\"><h3>");
        htmlBuffer.append(methodInfo.getHttpRequestType());
        if (methodInfo.getRestPath().equals("")) {

            htmlBuffer.append(" \"\"");

        } else {

            htmlBuffer.append(" ");
            htmlBuffer.append(methodInfo.getRestPath());
        }
        if (methodInfo.isDeprecated()) {

            htmlBuffer.append(" - Deprecated");
        }
        htmlBuffer.append("</h3></a></p>");

        htmlBuffer.append("\r\r\t\t<table>");

        if (methodInfo.getJavaDoc() != null) {

            htmlBuffer.append("\r\t\t\t<tr><td colspan=\"3\">");
            htmlBuffer.append(replaceCarriageReturnWithHtmlBreak(methodInfo.getJavaDoc()));
            htmlBuffer.append("</td></tr>");
        }

        htmlBuffer.append("\r\t\t\t<tr><td>Class</td><td>Name</td><td>Parameter type</td></tr>");

        List<ParameterInfo> parameterInfoList = methodInfo.getParameterInfo();

        parameterInfoList.stream().forEach((param) -> {

            htmlBuffer.append("\r\t\t\t<tr><td>");
            htmlBuffer.append(param.getParameterAnnotationName());
            htmlBuffer.append("</td><td>");

            if (isDomainData(param.getParameterClassName())) {

                // Domain data
                htmlBuffer.append("\r\t\t<a href=./");
                htmlBuffer.append(param.getParameterClassName());
                htmlBuffer.append(".html>");
                htmlBuffer.append(param.getParameterClassName());
                htmlBuffer.append("</a>");

            } else {

                htmlBuffer.append(param.getParameterClassName());
            }
            htmlBuffer.append("</td><td>");

            switch (param.getParameterType()) {

                case "javax.ws.rs.PathParam":
                    htmlBuffer.append("Path parameter");
                    break;

                case "javax.ws.rs.HeaderParam":
                    htmlBuffer.append("Header parameter");
                    break;

                default:
                    htmlBuffer.append(param.getParameterType());
                    break;
            }
            htmlBuffer.append("</td></tr>");
        });

        htmlBuffer.append("\r\t\t</table><BR>");

        htmlBuffer.append("\r\r\t\tResponse Body");

        htmlBuffer.append("<BR><BR>\r\r\t\t<table>\r\t\t\t<tr><td>Element</td><td>Media Type</td></tr>");

        htmlBuffer.append("\r\t\t\t<tr><td>");

        if (((methodInfo.getReturnInfo().getAnnotatedReturnType() == null)
                || methodInfo.getReturnInfo().getAnnotatedReturnType().isEmpty())
                && (isDomainData(methodInfo.getReturnInfo().getReturnClassName()) == false)) {

            htmlBuffer.append(methodInfo.getReturnInfo().getReturnClassName());

        } else {

            if (isDomainData(methodInfo.getReturnInfo().getReturnClassName())) {

                // Domain data
                htmlBuffer.append("\r\t\t<a href=./");
                htmlBuffer.append(methodInfo.getReturnInfo().getReturnClassName());
                htmlBuffer.append(".html>");
                htmlBuffer.append(methodInfo.getReturnInfo().getReturnClassName());
                htmlBuffer.append("</a>");

            } else {

                // Annotated ReturnType Domain data
                htmlBuffer.append("\r\t\t<a href=./");
                htmlBuffer.append(methodInfo.getReturnInfo().getAnnotatedReturnType());
                htmlBuffer.append(".html>");
                htmlBuffer.append(methodInfo.getReturnInfo().getAnnotatedReturnType());
                htmlBuffer.append("</a>");
            }
        }

        htmlBuffer.append("</td><td>");

        htmlBuffer.append(methodInfo.getProducesType());

        htmlBuffer.append("</td>\r\t\t\t</tr>\r\t\t</table><BR>");
    }

    private void writeDomainDataToFiles(final File outputDirectory) {

        LOGGER.info("writeDomainDataToFiles() restInfo.ClassInfo size = " + RestDocHandler.restInfo.getClassInfo().size());

        RestDocHandler.restInfo.getDomainDataMap().entrySet().stream()
                .filter(Objects::nonNull).forEach(domainData -> {

            writeDomainDataToFile(outputDirectory, domainData.getKey());
        });
    }

    private void writeDomainDataToFile(final File outputDirectory, final String domainDataType) {

        LOGGER.info("writeDomainDataToFile( " + domainDataType + " )");

        Path programmersInfoFilePath = Paths.get(URI.create("file://" + outputDirectory.getAbsolutePath() + "/" + domainDataType + ".html"));

        final StringBuffer htmlBuffer = htmlHeader();

        htmlBodyHeader(htmlBuffer, domainDataType);

        htmlGoHome(htmlBuffer);

        htmlRestResourceDomainData(htmlBuffer, domainDataType);

        htmlFooter(htmlBuffer);

        writeHtmlToFile(programmersInfoFilePath, htmlBuffer);
    }

    private void htmlRestResourceDomainData(final StringBuffer htmlBuffer, final String domainDataType) {

        LOGGER.info("htmlRestResourceDomainData() domainDataType = " + domainDataType);

        HashMap domainData = RestDocHandler.restInfo.getDomainDataMap();

        if (domainData.get(domainDataType) == null) {

            LOGGER.severe("htmlRestResourceDomainData() domainDataType = " + domainDataType + "Not found in DomainData map");
            return;
        }

        final List<FieldInfo> fields = RestDocHandler.restInfo.getDomainDataMap().get(domainDataType).getFields();

        htmlBuffer.append("\r\r\t\t<table>");

        if( RestDocHandler.restInfo.getDomainDataMap().get(domainDataType).getInfo() != null ) {
            
            htmlBuffer.append("\r\t\t\t<tr><td colspan=3>" + RestDocHandler.restInfo.getDomainDataMap().get(domainDataType).getInfo() + "</td></tr>");
            htmlBuffer.append("\r\t\t\t<tr><td colspan=3></td></tr>");
        }
                
        htmlBuffer.append("\r\t\t\t<tr><td>Field name</td><td>Field type</td><td>Type in list</td></tr>");

        fields.stream().forEach((field) -> {

            htmlBuffer.append("\r\t\t\t<tr><td>");

            htmlBuffer.append(field.getFieldName());

            htmlBuffer.append("</td><td>");
            htmlBuffer.append(field.getFieldType());
            htmlBuffer.append("</td><td>");

            if (!field.getListOfType().isEmpty()) {

                if ((field.getListOfType().contains(".") == true)
                        && (field.getListOfType().startsWith("java.") == false)) {

                    // Domain data
                    htmlBuffer.append("\r\t\t<a href=./");
                    htmlBuffer.append(field.getListOfType());
                    htmlBuffer.append(".html>");
                    htmlBuffer.append(field.getListOfType());
                    htmlBuffer.append("</a>");

                } else {

                    // This is a Primitive Data Type
                    htmlBuffer.append(field.getListOfType());
                }
            } else {

                // This is not a list
                htmlBuffer.append("-");
            }

            htmlBuffer.append("</td></tr>");
        });

        htmlBuffer.append("\r\t\t</table><BR>");
    }

    private StringBuffer htmlHeader() {

        final StringBuffer htmlBuffer = new StringBuffer(4096);

        htmlBuffer.append("<html lang=\"en\">\r\t<head>");
        htmlBuffer.append("\r\r\t<style>\r\t\ttable, th, td {\r\t\t\tborder: 1px solid;\r\t\t\tborder-collapse: collapse;\r\t\t\tborder-color: #D6D6C2;\r\t\t}\r\t\tth, td {\r\t\t\tpadding: 8px;\r\t\t}\r\t</style>");
        htmlBuffer.append("\r\r\t\t<title>REST documentation</title>\r\t</head>\r\r\t<body>");

        return htmlBuffer;
    }

    private void htmlBodyHeader(final StringBuffer htmlBuffer, String headerText) {

        htmlBuffer.append("\r\t\t<a name=\"top\"><h1>");
        htmlBuffer.append(headerText);
        htmlBuffer.append("</h1></a>");
    }

    private void htmlGoToProgrammersInfo(final StringBuffer htmlBuffer) {

        htmlBuffer.append("\r\r\t<ul style=\"list-style-type: none\">");
        htmlBuffer.append("\r\t\t<li><a href=./programmersinfo.html><h4>Programmers Information</h4></a></li>");
        htmlBuffer.append("\r\t</ul>");
    }

    private void htmlFooter(final StringBuffer htmlBuffer) {

        htmlBuffer.append("\r\r\t<table style=\"border: none;\">");
        htmlBuffer.append("\r\r\t\t<tr>");
        htmlBuffer.append("\r\t\t\t<td style=\"border: none;\"><a href=\"#top\"><h4>To top</h4></a></td><td style=\"border: none;\"><a href=./index.html><h4>Home</h4></a></td>");
        htmlBuffer.append("\r\r\t\t</tr>");
        htmlBuffer.append("\r\r\t\t<tr></tr>");
        htmlBuffer.append("\r\r\t\t<tr>");
        htmlBuffer.append("\r\t\t\t<td style=\"border: none;\"><h4>&copy; Copyright Peter Ivarsson</h4></td>");
        htmlBuffer.append("\r\r\t\t</tr>");
        htmlBuffer.append("\r\r\t</table>");
        htmlBuffer.append("\r\t</body>\r</html>");
    }

    private void htmlGoHome(final StringBuffer htmlBuffer) {

        htmlBuffer.append("\r\r\t<table style=\"border: none;\">");
        htmlBuffer.append("\r\r\t\t<tr>");
        htmlBuffer.append("\r\t\t\t<td style=\"border: none;\"><a href=./index.html><h4>Home</h4></a></td>");
        htmlBuffer.append("\r\r\t\t<tr>");
        htmlBuffer.append("\r\r\t<table>");
    }

    private void writeHtmlToFile(final Path indexFilePath, final StringBuffer htmlBuffer) {

        try {

            Files.write(indexFilePath, htmlBuffer.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ioe) {

            LOGGER.severe("writeHtmlToFile() Path ='" + indexFilePath.toString() + "' IOException: " + ioe.getMessage());
        }
    }

    private boolean isDomainData(final String parameterName) {

        // Check for 'Java classes' or 'Primitive Data Types'
        if (parameterName.startsWith("java")) {

            // Is a Java class
            return false;

        } else if (parameterName.equals("byte")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("short")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("int")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("long")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("float")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("double")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("boolean")) {

            // Is a Primitive Data Type
            return false;

        } else if (parameterName.equals("char")) {

            // Is a Primitive Data Type
            return false;
        }

        return true;
    }

    String replaceCarriageReturnWithHtmlBreak(String javaDocComments) {

        return javaDocComments.replaceAll("\r", "\r<BR>");
    }
}