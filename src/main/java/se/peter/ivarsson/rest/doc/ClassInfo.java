/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc;

import java.util.List;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class ClassInfo {

    private String className;
    private String packageAndClassName;
    private String classRootPath;
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

    public String getClassRootPath() {
        return classRootPath;
    }

    public void setClassRootPath(String classRootPath) {
        this.classRootPath = classRootPath;
    }

    public List<MethodInfo> getMethodInfo() {
        return methodInfo;
    }

    public void setMethodInfo(List<MethodInfo> methodInfo) {
        this.methodInfo = methodInfo;
    }

    @Override
    public String toString() {
        return "ClassInfo{" + "className=" + className + ", packageAndClassName=" + packageAndClassName + ", classRootPath=" + classRootPath + ", methodInfo=" + methodInfo + '}';
    }
}
