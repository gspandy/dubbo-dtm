package com.ltree.dtm.dubbo.rpc.filter;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.*;
import com.ltree.dtm.Constants;
import com.ltree.dtm.transaction.Trace.MethodInvokeTrace;

/**
 * 事务相关信息拦截处理过滤器，此过滤器主要用于获取调用方法的相关信息，如名称、方法、参数等,且指针对consumer
 *
 * @author lemontree
 * @version 1.0, 2017/8/8
 * @since 1.0
 */
@Activate(group = Constants.CONSUMER)
public class TransactionFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result result = invoker.invoke(invocation);
        // 如果是injvm协议，则使用不用理用直接使用jdbc事务回滚
        if (StringUtils.isEquals(invoker.getUrl().getProtocol(), Constants.LOCAL_PROTOCOL)) {
            return result;
        }

        MethodInvokeTrace.put(invocation);

        return result;
    }
}
