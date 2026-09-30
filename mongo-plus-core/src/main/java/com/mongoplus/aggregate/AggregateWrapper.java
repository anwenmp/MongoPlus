package com.mongoplus.aggregate;

/**
 * 便捷的创建管道构造器
 * @author anwen
 */
public class AggregateWrapper extends LambdaAggregateWrapper<AggregateWrapper> {

    /**
     * 创建拥有独立空管道的 receiver；各次构造不共享 Stage 列表。
     * @mongoPipelineFactory receiver=NEW initial=EMPTY ownership=INDEPENDENT
     */
    public AggregateWrapper() {
    }
}
