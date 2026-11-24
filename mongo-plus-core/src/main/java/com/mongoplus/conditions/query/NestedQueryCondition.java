package com.mongoplus.conditions.query;

import com.mongodb.BasicDBObject;
import com.mongoplus.bson.MongoPlusBasicDBObject;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.interfaces.query.QueryCondition;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.model.Order;
import com.mongoplus.model.Projection;
import com.mongoplus.support.SFunction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 嵌套文档条件
 * <p>由 {@link QueryCondition#nested(SFunction, Consumer)} 创建，在内部构建的条件字段会自动补上嵌套字段前缀，
 * 例如 {@code nested(TokenInfo::getSocialInfo, n -> n.eq(SocialInfo::getAgentId, "1"))}
 * 最终得到 {@code socialInfo.agentId = "1"}（字段名与 {@code @CollectionField} 注解保持一致）</p>
 * <p>所有比较、in、like、elemMatch 等条件方法都沿用外层接口的默认实现，因此字段前缀只在一处拼接，
 * 嵌套多层时前缀会继续叠加</p>
 *
 * @param <T> 当前嵌套层级的实体类型
 * @author yehuoshun
 */
public class NestedQueryCondition<T> implements QueryCondition<T, NestedQueryCondition<T>> {

    /**
     * 字段前缀，例如 social_info，多层嵌套时为 social_info.address
     */
    private final String prefix;

    /**
     * 条件最终落到的外层包装器
     */
    private final QueryCondition<?, ?> parent;

    public NestedQueryCondition(QueryCondition<?, ?> parent, String prefix) {
        this.parent = Objects.requireNonNull(parent, "parent");
        this.prefix = Objects.requireNonNull(prefix, "prefix");
    }

    @Override
    public NestedQueryCondition<T> addCondition(ConditionMetaObject conditionMetaObject) {
        parent.addCondition(conditionMetaObject);
        return this;
    }

    @Override
    public NestedQueryCondition<T> addCondition(List<Projection> projections) {
        List<Projection> nestedProjections = new ArrayList<>(projections.size());
        projections.forEach(projection -> nestedProjections.add(
                Projection.builder().column(prefix(projection.getColumn())).value(projection.getValue()).build()
        ));
        parent.addCondition(nestedProjections);
        return this;
    }

    @Override
    public NestedQueryCondition<T> addCondition(Order order) {
        Order nestedOrder = new Order();
        nestedOrder.setColumn(prefix(order.getColumn()));
        nestedOrder.setType(order.getType());
        parent.addCondition(nestedOrder);
        return this;
    }

    /**
     * 条件字段在这里统一补前缀，所有条件方法最终都会走到这个方法
     */
    @Override
    public ConditionMetaObject getBaseCondition(String condition, String column, Object value,
                                                Class<?> clazz, Field field, Object extraValue) {
        return QueryCondition.super.getBaseCondition(condition, prefix(column), value, clazz, field, extraValue);
    }

    /**
     * or、and、combine 传入的是独立的包装器，这里把里面已经构建好的条件统一补前缀，
     * 保证 {@code nested(..., n -> n.or(w -> w.eq(SocialInfo::getAgentId, "1")))}
     * 得到的是 {@code social_info.agentId} 而不是 {@code agentId}
     */
    @Override
    public ConditionMetaObject getBaseCondition(Wrapper<?> queryWrapper) {
        if (queryWrapper != null) {
            prefixConditions(queryWrapper.getConditionMetaObjects());
        }
        return ConditionMetaObject.builder()
                .condition(Thread.currentThread().getStackTrace()[2].getMethodName())
                .value(queryWrapper)
                .build();
    }

    /**
     * 嵌套内的字段前缀继续累加，支持多层嵌套
     */
    @Override
    public <R> String columnPrefix(SFunction<T, R> root) {
        return prefix(root.getFieldNameLine());
    }

    private String prefix(String column) {
        return prefix + "." + column;
    }

    private void prefixConditions(List<ConditionMetaObject> conditionMetaObjects) {
        if (conditionMetaObjects == null) {
            return;
        }
        for (ConditionMetaObject conditionMetaObject : conditionMetaObjects) {
            String column = conditionMetaObject.getColumn();
            if (column != null && !column.isEmpty()) {
                conditionMetaObject.setColumn(prefix(column));
            }
            Object value = conditionMetaObject.getValue();
            if (value instanceof List) {
                for (Object item : (List<?>) value) {
                    if (item instanceof ConditionMetaObject) {
                        prefixConditions(Collections.singletonList((ConditionMetaObject) item));
                    }
                }
            }
        }
    }

    @Override
    public NestedQueryCondition<T> custom(BasicDBObject basicDBObject) {
        return unsupportedCustom();
    }

    @Override
    public NestedQueryCondition<T> custom(org.bson.conversions.Bson bson) {
        return unsupportedCustom();
    }

    @Override
    public NestedQueryCondition<T> custom(MongoPlusBasicDBObject mongoPlusBasicDBObject) {
        return unsupportedCustom();
    }

    @Override
    public NestedQueryCondition<T> custom(SFunction<MongoPlusBasicDBObject, MongoPlusBasicDBObject> function) {
        return unsupportedCustom();
    }

    @Override
    public NestedQueryCondition<T> custom(List<BasicDBObject> basicDBObjectList) {
        return unsupportedCustom();
    }

    private NestedQueryCondition<T> unsupportedCustom() {
        throw new UnsupportedOperationException("嵌套文档条件不支持 custom，请改用 eq、in、elemMatch 等条件方法，或把 custom 放在外层包装器");
    }

}
