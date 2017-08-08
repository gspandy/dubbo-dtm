package com.ltree.dtm.transaction.support;

import com.ltree.dtm.boot.conf.DtmProperties;
import com.ltree.dtm.transaction.DTMContext;
import com.ltree.dtm.transaction.DTMInvocation;
import com.ltree.dtm.transaction.rollback.TransactionRollback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Thread local事务实现
 *
 * @author lemontree
 * @version 1.0, 2017/8/8
 * @since 1.0
 */
@Component
public class ThreadLocalTransactionManager implements TransactionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLocalTransactionManager.class);

    @Resource
    private DtmProperties properties;

    @Resource
    private TransactionRollback ITransactionRollback;

    @PostConstruct
    public void init() {
        Assert.isNull(properties.getMapRelation(), "DTM事务管理器还未配制不能直接使用");
        DTMContext.setProperties(properties);
        LOGGER.debug("properties:{}", properties);
    }

    @Override
    public void begin() {
        DTMContext.get().begin();
    }

    @Override
    public void rollback() {
        List<DTMInvocation> invocations = DTMContext.get().getInvocations();
        ITransactionRollback.rollback(invocations);
    }
}
