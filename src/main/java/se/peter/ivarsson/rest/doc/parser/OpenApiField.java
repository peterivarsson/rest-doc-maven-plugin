/*
 * Rest Documentation maven plugin.
 * 
 * Copyright (C) 2018 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.parser;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class OpenApiField {
   
    String fieldType;
    String fieldFormat;

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldFormat() {
        return fieldFormat;
    }

    public void setFieldFormat(String fieldFormat) {
        this.fieldFormat = fieldFormat;
    }

    @Override
    public String toString() {
        return "OpenApiField{" + "fieldType=" + fieldType + ", fieldFormat=" + fieldFormat + '}';
    }
}
