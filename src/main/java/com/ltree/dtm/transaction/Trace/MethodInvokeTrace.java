package com.ltree.dtm.transaction.Trace;

import com.alibaba.dubbo.rpc.Invocation;
import com.ltree.dtm.transaction.DTMContext;

/**
 * 方法调用追踪类
 *
 * @author lemontree
 * @version 1.0, 2017/8/8
 * @since 1.0
 */
public class MethodInvokeTrace {


    public static void put(Invocation invocation) {
        DTMContext.get().put(invocation);
    }
}
