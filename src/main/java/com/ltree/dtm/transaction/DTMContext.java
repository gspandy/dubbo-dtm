package com.ltree.dtm.transaction;

import com.alibaba.dubbo.rpc.Invocation;
import com.ltree.dtm.boot.conf.DtmProperties;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * DTM上下文
 *
 * @author lemontree
 * @version 1.0, 2017/8/8
 * @since 1.0
 */
public class DTMContext {
    private static final ThreadLocal<DTMContext> LOCAL = new InheritableThreadLocal<DTMContext>() {
        @Override
        protected DTMContext initialValue() {
            return new DTMContext();
        }
    };

    private static Map<String, Map<String, DtmProperties.BeanMethodInfo>> relation;

    private List<DTMInvocation> invocations = new ArrayList<>();

    private Boolean isBegin = Boolean.FALSE;

    private DTMContext() {

    }

    public static void setProperties(DtmProperties properties) {
        if (Objects.isNull(DTMContext.relation)) {
            relation = new HashMap<>();
            properties.getMapRelation().forEach((k, v) -> {
                relation.put(k, v.stream().collect(Collectors.
                        toMap(DtmProperties.BeanMethodInfo::getMethod, Function.identity())));
            });
        }
    }

    public static DTMContext get() {
        return LOCAL.get();
    }

    public void begin() {
        if (isBegin) {
            throw new RuntimeException("不能嵌套开启回滚事务");
        }

        this.isBegin = Boolean.TRUE;
    }

    public void put(Invocation invocation) {
        String className = invocation.getInvoker().getInterface().getName();
        Map<String, DtmProperties.BeanMethodInfo> methodInfo = relation.get(className);
        DtmProperties.BeanMethodInfo methodInfoBean = methodInfo.get(invocation.getMethodName());
        // 开启事务、配制了相应方法的回滚方法
        if (isBegin && Objects.nonNull(methodInfo) && Objects.nonNull(methodInfoBean)) {
            invocations.add(new DTMInvocation(
                    className,
                    methodInfoBean.getMethod(),
                    methodInfoBean.getRollbackMethod(),
                    methodInfoBean.getRetries(),
                    methodInfoBean.isAsync(),
                    invocation
            ));
        }
    }

    public List<DTMInvocation> getInvocations() {
        return new ArrayList<>(invocations);
    }
}
