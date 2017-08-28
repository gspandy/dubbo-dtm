dubbo事务处理组件DTM
===

## DTM (dubbo transaction Manager) 说明

DTM的出现主要解决dubbo分布式应该用事务回滚问题，此框架使用时需要遵循的一些规范：

- dubbo接口需要提供回滚的反操作方法
- 反操作方便的参数与要与调用方法参数一致
- 有值传递关系的调用依赖本组件暂时不能够提供支持回滚


## 原理介绍
  实现Dubbo调用的回滚操作其主要还是靠接口自身的反操作方法，组件只是实现`RPC`调用的`透明`参数记录和异常时的回滚反操作方法的自动调用。实现透明记录有很多方法，其中如果想做订制化扩展可添加dubbo xml的schema定义，同时自定义NamespaceHandler解析规则或是自定义annotation也可达到目的，但这对dubbo自身的侵入性太大，所以组件中采用自定义RPC filter的策略来获取相应的方法参数。
  
  而事务的开启是在`DTMContext`中做一个boolean类型的标记，DTMContext中除全局`static`值外都是线程安全的。所以当filter中加入追踪调用方法相关信息时，首先判断是否开启了事务和是否配制了此方法相关的反操作方法，否则认为此次调用无需被跟综记录下来，方法不需要相应回滚操作。
  
  当方法执行异常且catch中调用了回滚操作时，组件这时根据追踪记录的调用RPC栈优先回滚最近调用的RPC请求反操作方法直到全部调用结束。回滚时根据yml中配制方法回滚信息来相应决定是异步执行还是同步调用。如果同步调用，假如配制了异常出错重试次数是直接连续重试n次后结束。异步则是失败后放后队列前面再次等待回滚操作，直到回滚次数操过了配制值才中止重试。
  
## 使用方法说明

> yml配制：
>> async-num： 异步回滚线程池的数量，默认cpu核数
   map-relation： 调用方法与反操作方法配制关系
>>> method：调用方法 
    rollback-method：反操作方法 
    retries：回滚方法失败尝试重试次数 
    async：是否异步回滚

``` yml
dubbo.dtm:
  async-num: 5
  map-relation:
    # 这里接口全名称需要把"."替换成"-"
    com-xxx-rpc-xxxService:
        - method: add
          rollback-method: del
          retries: 1
          async: false

    com-xxx-rpc-xxxSXXService:
        - method: add
          rollback-method: del
          retries: 1
          async: false
```


``` java
@Resource
TransactionManager transactionManager;

try {
    // 开启一个事务
    transactionManager.begin();
    xxx1RPCService.save(vo);
    xxx2RPCService.update(vo);
    ....
    ....
} catch(Exception e) {
    // 异常后如果已调用过需要回滚的PC方法，则会自动做反方法透明调用和重试
    transactionManager.rollback();
}

``` 

## 注意点
- DTM一切事务相关操作都是基于反方法调用实现，所以RPC接口包一定要提供反操作方法才能实现事务的回滚
- RPC接口调用方法和反操作方法的参数一致
- Dubbo中如果开启了`injvm`协议，则DTM不进行调用追踪记录使用JDBC的本地事务即可回滚数据