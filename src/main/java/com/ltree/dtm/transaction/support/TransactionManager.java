package com.ltree.dtm.transaction.support;

import javax.transaction.InvalidTransactionException;

/**
 * 事务管理器接口
 *
 * @author lemontree
 * @version 1.0, 2017/8/8
 * @since 1.0
 */
public interface TransactionManager {

    void begin();

    void rollback();
}
