package com.ltree.dtm.transaction.rollback;

import com.alibaba.dubbo.rpc.Invocation;
import com.ltree.dtm.queue.QueueName;
import com.ltree.dtm.queue.QueueService;
import com.ltree.dtm.transaction.DTMInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反操作方法调用回滚接口实现
 *
 * @author lemontree
 * @version 1.0, 2017/8/8
 * @since 1.0
 */
@Component
public class InversionMethodRollback implements TransactionRollback, ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(InversionMethodRollback.class);

    private Map<String, Method> methodCache = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    @Resource
    private QueueService<DTMInvocation> queueService;


    @Override
    public void rollback(List<DTMInvocation> invocations) {
        Collections.reverse(invocations);
        invocations.forEach(this::delegation);
    }

    @Override
    public void invoke(DTMInvocation dtmInvocation) throws Exception {
        Invocation invocation = dtmInvocation.getInvocation();
        Object service = getBean(dtmInvocation.getClassName());
        methodCache.putIfAbsent(dtmInvocation.getClassName(), service.getClass()
                .getMethod(dtmInvocation.getRollbackMethod(), invocation.getParameterTypes()));
        Method methodObj = methodCache.get(service);
        methodObj.invoke(service, invocation.getArguments());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Object getBean(String className) {
        return applicationContext.getBean(className);
    }

    private void delegation(DTMInvocation invocation) {
        Object service = getBean(invocation.getClassName());

        if (invocation.isAsync()) {
            queueService.add(QueueName.ASYNC_QUEUE, invocation);

            return;
        }

        invoke(service, invocation);
    }

    private void invoke(Object service, DTMInvocation dtmInvocation) {
        Invocation invocation = dtmInvocation.getInvocation();

        try {
            methodCache.putIfAbsent(dtmInvocation.getClassName(), service.getClass()
                    .getMethod(dtmInvocation.getRollbackMethod(), invocation.getParameterTypes()));
            Method methodObj = methodCache.get(service);
            methodObj.invoke(service, invocation.getArguments());
        } catch (Exception e) {
            if (dtmInvocation.isFinish()) {
                LOGGER.error(String.format("rollback invocation:%s", dtmInvocation), e);
                return;
            }

            dtmInvocation.incrExecutions();
            delegation(dtmInvocation);
        }
    }
}
