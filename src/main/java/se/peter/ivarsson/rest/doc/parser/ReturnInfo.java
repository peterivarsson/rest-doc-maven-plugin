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
public class ReturnInfo {

    private String returnClassName;
    private String annotatedReturnType;
    private String returnStatus;

    public String getReturnClassName() {
        return returnClassName;
    }

    public void setReturnClassName(String returnClassName) {
        this.returnClassName = returnClassName;
    }

    public String getAnnotatedReturnType() {
        return annotatedReturnType;
    }

    public void setAnnotatedReturnType(String annotatedReturnType) {
        this.annotatedReturnType = annotatedReturnType;
    }

    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    @Override
    public String toString() {
        return " ReturnInfo{" + "\n            returnClassName=" + returnClassName + ",\n            annotatedReturnType=" + annotatedReturnType + ",\n            returnStatus=" + returnStatus + "\n         }";
    }
}
