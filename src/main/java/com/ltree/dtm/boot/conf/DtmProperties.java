package com.ltree.dtm.boot.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 事务管理配制类
 *
 * @author lemontree
 * @version 1.0, 2017/8/8
 * @since 1.0
 */
@Component
@ConfigurationProperties(prefix = "dubbo.dtm")
public class DtmProperties {
    /**
     * 映射关系，key为接口class全名
     */
    private Map<String, List<BeanMethodInfo>> mapRelation;

    private Integer asyncNum = Runtime.getRuntime().availableProcessors();

    public Map<String, List<BeanMethodInfo>> getMapRelation() {
        return mapRelation;
    }

    public void setMapRelation(Map<String, List<BeanMethodInfo>> mapRelation) {
        this.mapRelation = mapRelation;
    }

    public Integer getAsyncNum() {
        return asyncNum;
    }

    public void setAsyncNum(Integer asyncNum) {
        this.asyncNum = asyncNum;
    }

    public static class BeanMethodInfo {

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
         * 是否异步
         */
        private Boolean async = Boolean.FALSE;

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

        public Boolean isAsync() {
            return async;
        }

        public void setAsync(Boolean async) {
            this.async = async;
        }
    }
}
