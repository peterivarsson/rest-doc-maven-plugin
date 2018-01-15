/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.parser;

import java.util.List;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class ClassInfo {

    private String className;
    private String packageAndClassName;
    private String classRootPath = "";
    private String classPath = null;  // Need to be null
    private List<MethodInfo> methodInfo;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageAndClassName() {
        return packageAndClassName;
    }

    public void setPackageAndClassName(String packageAndClassName) {
        this.packageAndClassName = packageAndClassName;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public List<MethodInfo> getMethodInfo() {
        return methodInfo;
    }

    public void setMethodInfo(List<MethodInfo> methodInfo) {
        this.methodInfo = methodInfo;
    }

    public String getClassRootPath() {
        return classRootPath;
    }

    public void setClassRootPath(String classRootPath) {
        this.classRootPath = classRootPath;
    }

    @Override
    public String toString() {
        return "\n   ClassInfo{" + "\n      className=" + className + ",\n      packageAndClassName=" + packageAndClassName + ",\n      classRootPath=" + classRootPath + ",\n      classPath=" + classPath + ",\n      methodInfo=" + methodInfo + "\n   }";
    }
}
