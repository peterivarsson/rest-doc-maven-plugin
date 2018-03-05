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
public class MethodInfo {

    private String methodName;
    private boolean deprecated = false;
    private String methodPath;
    private String httpRequestType;
    private String produceType = "";
    private String consumeType = "";
    private String requestBodyName = "";
    private String requestBodyClassName = "";
    private ReturnInfo returnInfo;
    private List<ParameterInfo> parameterInfo;
    private String javaDoc;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodPath() {
        return methodPath;
    }

    public void setMethodPath(String methodPath) {
        this.methodPath = methodPath;
    }

    public String getHttpRequestType() {
        return httpRequestType;
    }

    public void setHttpRequestType(String httpRequestType) {
        this.httpRequestType = httpRequestType;
    }

    public String getProduceType() {
        return produceType;
    }

    public void setProduceType(String produceType) {
        this.produceType = produceType;
    }

    public List<ParameterInfo> getParameterInfo() {
        return parameterInfo;
    }

    public void setParameterInfo(List<ParameterInfo> parameterInfo) {
        this.parameterInfo = parameterInfo;
    }

    public String getConsumeType() {
        return consumeType;
    }

    public void setConsumeType(String consumeType) {
        this.consumeType = consumeType;
    }

    public ReturnInfo getReturnInfo() {
        return returnInfo;
    }

    public void setReturnInfo(ReturnInfo returnInfo) {
        this.returnInfo = returnInfo;
    }

    public String getJavaDoc() {
        return javaDoc;
    }

    public void setJavaDoc(String javaDoc) {
        this.javaDoc = javaDoc;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getRequestBodyName() {
        return requestBodyName;
    }

    public void setRequestBodyName(String requestBodyName) {
        this.requestBodyName = requestBodyName;
    }

    public String getRequestBodyClassName() {
        return requestBodyClassName;
    }

    public void setRequestBodyClassName(String requestBodyClassName) {
        this.requestBodyClassName = requestBodyClassName;
    }

    @Override
    public String toString() {
        return " MethodInfo{" + "\n         methodName=" + methodName + "\n         deprecated=" + deprecated + ",\n         methodPath=" + methodPath + ",\n         httpRequestType=" + httpRequestType + ",\n         produceType=" + produceType + ",\n         consumeType=" + consumeType + ",\n         requestBodyName=" + requestBodyName + ",\n         requestBodyClassName=" + requestBodyClassName + ",\n         returnInfo=" + returnInfo + ",\n         parameterInfo=" + parameterInfo + "\n         javaDoc=" + javaDoc + "\n      }";
    }
}
