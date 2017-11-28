/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.parser;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class ParameterInfo {

    private String parameterAnnotationName;
    private String parameterClassName;
    private String parameterType;

    public String getParameterAnnotationName() {
        return parameterAnnotationName;
    }

    public void setParameterAnnotationName(String parameterAnnotationName) {
        this.parameterAnnotationName = parameterAnnotationName;
    }

    public String getParameterClassName() {
        return parameterClassName;
    }

    public void setParameterClassName(String parameterClassName) {
        this.parameterClassName = parameterClassName;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    @Override
    public String toString() {
        return " ParameterInfo{" + "\n            parameterAnnotationName=" + parameterAnnotationName + ",\n            parameterClassName=" + parameterClassName + ",\n            parameterType=" + parameterType + "\n         }";
    }
}
