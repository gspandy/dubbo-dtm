package com.ltree.dtm.transaction.rollback;

import com.alibaba.dubbo.rpc.Invocation;
import com.ltree.dtm.transaction.DTMInvocation;

import java.util.List;

/**
 * 回滚接口
 *
 * @author lemontree
 * @version 1.0, 2017/8/8
 * @since 1.0
 */
public interface TransactionRollback {

    void rollback(List<DTMInvocation> invocations);

    void invoke(DTMInvocation dtmInvocation) throws Exception;
}
