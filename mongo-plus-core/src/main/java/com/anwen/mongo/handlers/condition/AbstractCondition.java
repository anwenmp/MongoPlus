package com.anwen.mongo.handlers.condition;

import com.anwen.mongo.bson.MongoPlusBasicDBObject;
import com.anwen.mongo.cache.global.HandlerCache;
import com.anwen.mongo.conditions.interfaces.condition.CompareCondition;
import com.anwen.mongo.conditions.update.UpdateChainWrapper;
import com.anwen.mongo.enums.UpdateConditionEnum;
import com.anwen.mongo.model.BuildUpdate;
import com.anwen.mongo.model.MutablePair;
import com.anwen.mongo.toolkit.CollUtil;
import com.mongodb.BasicDBObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 抽象的条件处理器
 *
 * @author anwen
 * @date 2024/6/30 下午3:58
 */
public abstract class AbstractCondition implements Condition,UpdateCondition {

    @Override
    public BasicDBObject queryCondition(List<CompareCondition> compareConditionList) {
        MongoPlusBasicDBObject mongoPlusBasicDBObject = new MongoPlusBasicDBObject();
        if (CollUtil.isNotEmpty(compareConditionList)) {
            compareConditionList.forEach(compareCondition -> queryCondition(compareCondition,mongoPlusBasicDBObject));
        }
        return mongoPlusBasicDBObject;
    }

    @Override
    public BasicDBObject queryCondition(CompareCondition compareCondition){
        return queryCondition(compareCondition,new MongoPlusBasicDBObject());
    }

    @Override
    public MutablePair<BasicDBObject, BasicDBObject> updateCondition(UpdateChainWrapper<?, ?> updateChainWrapper) {
        List<CompareCondition> updateCompareList = updateChainWrapper.getUpdateCompareList();

        Map<UpdateConditionEnum, List<CompareCondition>> conditionMap = Arrays.stream(UpdateConditionEnum.values())
                .collect(Collectors.toMap(Function.identity(),
                        conditionEnum -> updateCompareList.stream()
                                .filter(compareCondition -> Objects.equals(compareCondition.getCondition(),
                                        conditionEnum.getSubCondition()))
                                .collect(Collectors.toList())));

        BasicDBObject updateBasicDBObject = new BasicDBObject();
        conditionMap.forEach((conditionEnum, compareConditionList) -> {
            if (CollUtil.isNotEmpty(compareConditionList)) {
                updateBasicDBObject.append(conditionEnum.getCondition(),updateValue(conditionEnum,compareConditionList));
            }
        });

        return new MutablePair<>(updateChainWrapper.buildCondition().getCondition(), updateBasicDBObject);
    }
    
    /**
     * 具体的抽象更新构建方法
     * @param updateConditionEnum 操作枚举
     * @param compareConditionList 条件集合
     * @return {@link BasicDBObject}
     * @author anwen
     * @date 2024/8/1 下午5:35
     */
    public BasicDBObject updateValue(UpdateConditionEnum updateConditionEnum,List<CompareCondition> compareConditionList){
        final AtomicReference<List<CompareCondition>> finalCompareConditionList = new AtomicReference<>(compareConditionList);
        BiFunction<AbstractCondition,BuildUpdate,BasicDBObject> updateValueFunc =  (condition,buildUpdate) -> {
            List<CompareCondition> currentCompareConditionList = finalCompareConditionList.get();
            switch (updateConditionEnum){
                case SET:
                case INC:
                case MIN:
                case MAX:
                case MUL:
                case POP:
                case PULL_ALL:
                    return condition.buildUpdateCondition(currentCompareConditionList,buildUpdate);
                case PUSH:
                    finalCompareConditionList.set(currentCompareConditionList.stream().distinct().collect(Collectors.toList()));
                    currentCompareConditionList = finalCompareConditionList.get();
                    return condition.buildPushCondition(currentCompareConditionList,buildUpdate);
                case CURRENT_DATE:
                    return condition.buildCurrentDateCondition(currentCompareConditionList,buildUpdate);
                case RENAME:
                    return condition.buildRenameCondition(currentCompareConditionList,buildUpdate);
                case UNSET:
                    return condition.buildUnsetCondition(currentCompareConditionList,buildUpdate);
                case ADD_TO_SET:
                    return condition.buildAddToSetCondition(currentCompareConditionList,buildUpdate);
                case PULL:
                    return condition.buildPullCondition(currentCompareConditionList,buildUpdate);
            }
            return null;
        };
        BasicDBObject updateBasicDBObject = new BasicDBObject();
        finalCompareConditionList.get().forEach(compareCondition -> {
            HandlerCache.conditionHandlerList.forEach(conditionHandler -> conditionHandler.beforeUpdateCondition(compareCondition,updateBasicDBObject));
            updateValueFunc.apply(this,new BuildUpdate(compareCondition,updateBasicDBObject));
            HandlerCache.conditionHandlerList.forEach(conditionHandler -> conditionHandler.afterUpdateCondition(compareCondition,updateBasicDBObject));
        });
        return updateBasicDBObject;
    }

    /**
     * 具体的抽象条件构建方法
     * @param compareCondition 条件
     * @param mongoPlusBasicDBObject BasicDBObject
     * @return {@link BasicDBObject}
     * @author anwen
     * @date 2024/8/1 下午2:27
     */
    public abstract BasicDBObject queryCondition(CompareCondition compareCondition,MongoPlusBasicDBObject mongoPlusBasicDBObject);

}
