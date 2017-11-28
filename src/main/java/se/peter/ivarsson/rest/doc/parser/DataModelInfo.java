/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.parser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class DataModelInfo {

    private List<FieldInfo> fields = new ArrayList<>();

    public List<FieldInfo> getFields() {
        return fields;
    }

    public void setFields(List<FieldInfo> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return " DataModelInfo{" + "\n      fields=" + fields + "\n   }";
    }
}
