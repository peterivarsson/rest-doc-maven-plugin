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
public class FieldInfo {

    String fieldName;
    String fieldType;
    String fieldOfType = "";   // List, Set, Map, Enum types

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldOfType() {
        return fieldOfType;
    }

    public void setFieldOfType(String fieldOfType) {
        this.fieldOfType = fieldOfType;
    }

    @Override
    public String toString() {
        return " FieldInfo{" + "\n         fieldName=" + fieldName + ",\n         fieldType=" + fieldType + ",\n         fieldOfType=" + fieldOfType + "\n      }";
    }
}
