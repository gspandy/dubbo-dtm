package com.ltree.dtm.transaction.rollback.async;

import com.ltree.dtm.Constants;
import com.ltree.dtm.queue.QueueName;
import com.ltree.dtm.queue.QueueService;
import com.ltree.dtm.transaction.DTMInvocation;
import com.ltree.dtm.transaction.rollback.TransactionRollback;

import javax.annotation.Resource;

/**
 * 异步执行回滚执行类
 *
 * @author lemontree
 * @version 1.0, 2017/8/8
 * @since 1.0
 */
public class TransactionAsyncRollbackExecutor extends AbstractAsyncExecutor {
    @Resource
    private QueueService<DTMInvocation> queueService;

    @Resource
    private TransactionRollback transactionRollback;

    @Override
    String threadPoolPrefix() {
        return Constants.ASYNC_ROLLBACK_POOL_PREFIX;
    }

    @Override
    Boolean isExecute() {
        return queueService.size(QueueName.ASYNC_QUEUE) > 0;
    }

    @Override
    void run() {
        DTMInvocation invocation = queueService.poll(QueueName.ASYNC_QUEUE);
        try {
            transactionRollback.invoke(invocation);
        } catch (Exception e) {
            if (invocation.isFinish()) {
                logger.error(String.format("rollback invocation:%s", invocation), e);
                return;
            }

            invocation.incrExecutions();
            queueService.add(QueueName.ASYNC_QUEUE, invocation);
        }
    }
}
