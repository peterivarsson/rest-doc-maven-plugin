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
public class MethodInfo {

    private String methodName;
    private String restPath;
    private String httpRequestType;
    private String produceType = "";
    private String consumeType = "";
    private ReturnInfo returnInfo;
    private List<ParameterInfo> parameterInfo;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getRestPath() {
        return restPath;
    }

    public void setRestPath(String restPath) {
        this.restPath = restPath;
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

    public String getProducesType() {
        return produceType;
    }

    public void setProducesType(String producesType) {
        this.produceType = producesType;
    }

    public ReturnInfo getReturnInfo() {
        return returnInfo;
    }

    public void setReturnInfo(ReturnInfo returnInfo) {
        this.returnInfo = returnInfo;
    }

    @Override
    public String toString() {
        return "MethodInfo{" + "methodName=" + methodName + ", restPath=" + restPath + ", httpRequestType=" + httpRequestType + ", produceType=" + produceType + ", consumeType=" + consumeType + ", returnInfo=" + returnInfo + ", parameterInfo=" + parameterInfo + '}';
    }
}
