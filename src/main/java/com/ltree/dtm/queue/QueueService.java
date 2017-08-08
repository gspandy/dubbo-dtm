package com.ltree.dtm.queue;

/**
 * 队列接口
 *
 * @author lemontree
 * @version 1.0, 2017/8/8
 * @since 1.0
 */
public interface QueueService<E> {

    long size(String queueName);

    boolean add(String queueName, E value);

    E poll(String queueName);
}
