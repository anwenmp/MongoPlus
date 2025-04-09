package com.mongoplus.aggregate.pipeline;

import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.aggregate.AggregateWrapper;
import com.mongoplus.aggregate.LambdaAggregateWrapper;
import com.mongoplus.support.SFunction;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * 用于 $facet 管道阶段的类，封装Facet，支持lambda
 * @author anwen
 */
public class Facet extends com.mongodb.client.model.Facet {
    
    /**
     * $facet阶段
     * @param name facet名称
     * @param pipeline facet管道
     * @author anwen
     */
    public <T> Facet(SFunction<T,?> name, List<? extends Bson> pipeline) {
        super(name.getFieldNameLine(), pipeline);
    }

    /**
     * $facet阶段
     * @param name facet名称
     * @param pipeline facet管道
     * @author anwen
     */
    public <T> Facet(SFunction<T,?> name, Bson... pipeline) {
        super(name.getFieldNameLine(), pipeline);
    }

    /**
     * $facet阶段
     * @param name facet名称
     * @param aggregateChainWrapper 管道Wrapper
     * @author anwen
     */
    public Facet(String name, Aggregate<?> aggregateChainWrapper){
        super(name, aggregateChainWrapper.getAggregateConditionList());
    }

    /**
     * $facet阶段
     * @param name facet名称
     * @param function 管道Wrapper
     * @author anwen
     */
    public Facet(String name, SFunction<LambdaAggregateWrapper<AggregateWrapper>,LambdaAggregateWrapper<AggregateWrapper>> function){
        super(name, function.apply(new AggregateWrapper()).getAggregateConditionList());
    }

    /**
     * $facet阶段
     * @param name facet名称
     * @param aggregateChainWrapper 管道Wrapper
     * @author anwen
     */
    public <T> Facet(SFunction<T,?> name, Aggregate<?> aggregateChainWrapper){
        super(name.getFieldNameLine(), aggregateChainWrapper.getAggregateConditionList());
    }

    /**
     * $facet阶段
     * @param name facet名称
     * @param function 管道Wrapper
     * @author anwen
     */
    public <T,R> Facet(SFunction<T,?> name, SFunction<LambdaAggregateWrapper<AggregateWrapper>,LambdaAggregateWrapper<AggregateWrapper>> function){
        super(name.getFieldNameLine(), function.apply(new AggregateWrapper()).getAggregateConditionList());
    }
    
}
