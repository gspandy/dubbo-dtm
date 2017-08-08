package com.ltree.dtm.transaction;

import com.alibaba.dubbo.rpc.Invocation;

import java.io.Serializable;

/**
 * DTMInvocation
 *
 * @author lemontree
 * @version 1.0, 2017/8/8
 * @since 1.0
 */
public class DTMInvocation implements Serializable{

    /**
     * 调用接口名称
     */
    private String className;

    /**
     * 调用原方法
     */
    private String method;

    /**
     * 对应回滚执行方法
     */
    private String rollbackMethod;

    /**
     * 失败尝试重试次数
     */
    private Integer retries = 0;

    /**
     * 已失败尝试重试次数
     */
    private Integer executions = 0;

    /**
     * 是否异步
     */
    private Boolean async = Boolean.FALSE;

    /**
     * dubbo invocation
     */
    private Invocation invocation;

    public DTMInvocation(String className, String method, String rollbackMethod, Integer retries,
                         Boolean async, Invocation invocation) {
        this.className = className;
        this.method = method;
        this.rollbackMethod = rollbackMethod;
        this.retries = retries;
        this.async = async;
        this.invocation = invocation;
    }


    public void incrExecutions() {
        this.executions++;
    }

    public boolean isFinish () {
        return this.executions >= this.retries;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRollbackMethod() {
        return rollbackMethod;
    }

    public void setRollbackMethod(String rollbackMethod) {
        this.rollbackMethod = rollbackMethod;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getExecutions() {
        return executions;
    }

    public Boolean isAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public Invocation getInvocation() {
        return invocation;
    }

    public void setInvocation(Invocation invocation) {
        this.invocation = invocation;
    }

    @Override
    public String toString() {
        return "DTMInvocation{" +
                "className='" + className + '\'' +
                ", method='" + method + '\'' +
                ", rollbackMethod='" + rollbackMethod + '\'' +
                ", retries=" + retries +
                ", executions=" + executions +
                ", async=" + async +
                ", invocation=" + invocation +
                '}';
    }
}
