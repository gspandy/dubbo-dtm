package com.ltree.dtm.transaction.rollback.async;

import com.ltree.dtm.boot.conf.DtmProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步执行抽像类
 *
 * @author lemontree
 * @version 1.0, 2017/8/8
 * @since 1.0
 */
@Component
public abstract class AbstractAsyncExecutor {

    @Resource
    private DtmProperties dtmProperties;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected ScheduledExecutorService scheduledExecutorService;

    @PostConstruct
    protected void init() {
        scheduledExecutorService = Executors.newScheduledThreadPool(getThreadPoolSize(), new QueueThreadFactory(threadPoolPrefix()));
        scheduledExecutorService.scheduleWithFixedDelay(new TimeListener(), 0L, 2L, TimeUnit.SECONDS);
    }

    //默认2个线程处理此队列
    protected int getThreadPoolSize() {
        return dtmProperties.getAsyncNum();
    }

    abstract String threadPoolPrefix();

    abstract Boolean isExecute();

    abstract void run();


    private class TimeListener implements Runnable{

        public void run() {
            try {
                if (isExecute()) {
                    AbstractAsyncExecutor.this.run();
                } else {
                    TimeUnit.MILLISECONDS.sleep(500L);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    private class QueueThreadFactory implements ThreadFactory {

        private final String namePrefix;

        private final ThreadGroup group;

        private final AtomicInteger poolCount = new  AtomicInteger(1);

        public QueueThreadFactory(String namePrefix){
            SecurityManager sm = System.getSecurityManager();
            group = sm == null ? Thread.currentThread().getThreadGroup() : sm.getThreadGroup();
            this.namePrefix = String.format("%s-asyn-pool-%s", namePrefix, poolCount.getAndIncrement());
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new Thread(group, runnable, namePrefix, 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }

            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }

            t.setUncaughtExceptionHandler((t1, e) -> logger.error("queueThread " + t1.getName() + " error !",e));

            return t;
        }

    }
}
