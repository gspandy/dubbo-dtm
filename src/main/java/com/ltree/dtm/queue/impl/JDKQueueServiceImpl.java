package com.ltree.dtm.queue.impl;


import com.ltree.dtm.queue.QueueService;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * JDK队列接口实现
 *
 * @author lemontree
 * @version 1.0, 2017/8/8
 * @since 1.0
 */
@Component
public class JDKQueueServiceImpl<E> implements QueueService<E> {
    private static final ConcurrentHashMap<String, BlockingQueue> queues = new ConcurrentHashMap();

    @Override
    public long size(String queueName) {
        BlockingQueue queue = queues.get(queueName);
        if (queue == null) {
            return 0;
        }

        return queue.size();
    }

    @Override
    public boolean add(String queueName, E value) {
        queues.putIfAbsent(queueName, new LinkedBlockingQueue());
        BlockingQueue blockingQueue = queues.get(queueName);

        return blockingQueue.add(value);
    }

    @Override
    public E poll(String queueName) {
        queues.putIfAbsent(queueName, new LinkedBlockingQueue());
        BlockingQueue blockingQueue = queues.get(queueName);

        return (E) blockingQueue.poll();
    }
}
