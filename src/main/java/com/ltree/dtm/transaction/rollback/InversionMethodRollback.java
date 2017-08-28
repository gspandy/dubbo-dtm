package com.ltree.dtm.transaction.rollback;

import com.alibaba.dubbo.rpc.Invocation;
import com.ltree.dtm.Constants;
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
import java.util.*;
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

    private final Map<String, Class<?>> classCache = new HashMap<>();

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
        String key = dtmInvocation.getClassName().concat(Constants.SEPARATOR).concat(dtmInvocation.getMethod());
        methodCache.putIfAbsent(key, service.getClass().getMethod(dtmInvocation.getRollbackMethod(),
                invocation.getParameterTypes()));
        Method methodObj = methodCache.get(key);
        methodObj.invoke(service, invocation.getArguments());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Object getBean(String className) {
        try {
            Class clazz = classCache.get(className);
            if (Objects.isNull(clazz)) {
                synchronized (classCache) {
                    if (!classCache.containsKey(className)) {
                        classCache.put(className, Class.forName(className));
                    }

                    clazz = classCache.get(className);
                }
            }

            return applicationContext.getBean(clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void delegation(DTMInvocation invocation) {
        if (invocation.isAsync()) {
            queueService.add(QueueName.ASYNC_QUEUE, invocation);

            return;
        }

        try {
            invoke(invocation);
        } catch (Exception e) {
            if (invocation.isFinish()) {
                LOGGER.error(String.format("rollback invocation:%s", invocation), e);
                return;
            }

            invocation.incrExecutions();
            delegation(invocation);
        }
    }
}
