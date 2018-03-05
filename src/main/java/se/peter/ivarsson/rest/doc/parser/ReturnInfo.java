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
    private String returnStatusAsText = "OK";
    private String returnStatusCode = "200";

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

    public String getReturnStatusAsText() {
        return returnStatusAsText;
    }

    public void setReturnStatusAsText(String returnStatusAsText) {
        this.returnStatusAsText = returnStatusAsText;
    }

    public String getReturnStatusCode() {
        return returnStatusCode;
    }

    public void setReturnStatusCode(String returnStatusCode) {
        this.returnStatusCode = returnStatusCode;
    }

    @Override
    public String toString() {
        return " ReturnInfo{" + "\n            returnClassName=" + returnClassName + ",\n            annotatedReturnType=" + annotatedReturnType + ",\n            returnStatusAsText=" + returnStatusAsText + ",\n            returnStatusCode=" + returnStatusCode + "\n         }";
    }
}
