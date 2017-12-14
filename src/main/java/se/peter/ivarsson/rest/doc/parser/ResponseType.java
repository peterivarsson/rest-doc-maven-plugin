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
public class ResponseType {

    private String returnType;
    private String returnStatus;

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    @Override
    public String toString() {
        return "ResponseType{" + "returnType=" + returnType + ", returnStatus=" + returnStatus + '}';
    }
}
