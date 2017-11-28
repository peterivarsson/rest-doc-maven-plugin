/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class RestInfo {

    private List<ClassInfo> classInfo = new ArrayList<>();
    private HashMap<String, DataModelInfo> domainDataMap = new HashMap<>();

    public List<ClassInfo> getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(List<ClassInfo> classInfo) {
        this.classInfo = classInfo;
    }

    public HashMap<String, DataModelInfo> getDomainDataMap() {
        return domainDataMap;
    }

    public void setDomainDataMap(HashMap<String, DataModelInfo> domainDataMap) {
        this.domainDataMap = domainDataMap;
    }

    @Override
    public String toString() {
        return "RestInfo{" + "\n   classInfo=" + classInfo + ",\n   domainDataMap=" + domainDataMap + "\n}";
    }
}
