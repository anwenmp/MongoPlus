package com.anwen.mongo.handlers.condition;

import com.anwen.mongo.bson.MongoPlusBasicDBObject;
import com.anwen.mongo.cache.codec.MapCodecCache;
import com.anwen.mongo.cache.global.HandlerCache;
import com.anwen.mongo.conditions.interfaces.condition.CompareCondition;
import com.anwen.mongo.conditions.query.QueryChainWrapper;
import com.anwen.mongo.domain.MongoPlusException;
import com.anwen.mongo.enums.CurrentDateType;
import com.anwen.mongo.enums.QueryOperatorEnum;
import com.anwen.mongo.enums.SpecialConditionEnum;
import com.anwen.mongo.enums.TypeEnum;
import com.anwen.mongo.model.BuildUpdate;
import com.anwen.mongo.model.MutablePair;
import com.anwen.mongo.toolkit.CollUtil;
import com.anwen.mongo.toolkit.Filters;
import com.mongodb.BasicDBObject;
import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 构建条件
 *
 * @author anwen
 * @date 2024/6/30 下午4:07
 */
public class BuildCondition extends AbstractCondition {

    private static Condition DEFAULT_BUSINESS_CONDITION;

    public static Condition condition() {
        return DEFAULT_BUSINESS_CONDITION;
    }

    public static void setCondition(Condition condition){
        DEFAULT_BUSINESS_CONDITION = condition;
    }

    static {
        DEFAULT_BUSINESS_CONDITION = new BuildCondition();
    }

    public BuildCondition() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public BasicDBObject queryCondition(CompareCondition compareCondition, MongoPlusBasicDBObject mongoPlusBasicDBObject) {
        HandlerCache.conditionHandlerList.forEach(conditionHandler -> conditionHandler.beforeQueryCondition(compareCondition,mongoPlusBasicDBObject));
        QueryOperatorEnum query = QueryOperatorEnum.getQueryOperator(compareCondition.getCondition());
        switch (Objects.requireNonNull(query)){
            case EQ:
                mongoPlusBasicDBObject.put(Filters.eq(compareCondition.getColumn(), compareCondition.getValue()));
                break;
            case NE:
                mongoPlusBasicDBObject.put(Filters.ne(compareCondition.getColumn(), compareCondition.getValue()));
                break;
            case GT:
                mongoPlusBasicDBObject.put(Filters.gt(compareCondition.getColumn(), compareCondition.getValue()));
                break;
            case LT:
                mongoPlusBasicDBObject.put(Filters.lt(compareCondition.getColumn(), compareCondition.getValue()));
                break;
            case GTE:
                mongoPlusBasicDBObject.put(Filters.gte(compareCondition.getColumn(), compareCondition.getValue()));
                break;
            case LTE:
                mongoPlusBasicDBObject.put(Filters.lte(compareCondition.getColumn(), compareCondition.getValue()));
                break;
            case REGEX:
            case LIKE:
                mongoPlusBasicDBObject.put(Filters.regex(compareCondition.getColumn(), (String) compareCondition.getValue()));
                break;
            case IN:
                mongoPlusBasicDBObject.put(Filters.in(compareCondition.getColumn(), (Collection<?>)compareCondition.getValue()));
                break;
            case NIN:
                mongoPlusBasicDBObject.put(Filters.nin(compareCondition.getColumn(), (Collection<?>)compareCondition.getValue()));
                break;
            case AND:
                List<Bson> andBsonList = new ArrayList<>();
                QueryChainWrapper<?, ?> andWrapper = (QueryChainWrapper<?, ?>) compareCondition.getValue();
                andWrapper.getCompareList().forEach(andCompareCondition -> andBsonList.add(queryCondition(andCompareCondition)));
                andBsonList.addAll(andWrapper.getBasicDBObjectList());
                mongoPlusBasicDBObject.put(Filters.and(andBsonList));
                break;
            case OR:
                List<Bson> orBsonList = new ArrayList<>();
                QueryChainWrapper<?, ?> orWrapper = (QueryChainWrapper<?, ?>) compareCondition.getValue();
                orWrapper.getCompareList().forEach(orCompareCondition -> orBsonList.add(queryCondition(orCompareCondition)));
                orBsonList.addAll(orWrapper.getBasicDBObjectList());
                mongoPlusBasicDBObject.put(Filters.or(orBsonList));
                break;
            case NOR:
                List<Bson> norBsonList = new ArrayList<>();
                QueryChainWrapper<?, ?> norWrapper = (QueryChainWrapper<?, ?>) compareCondition.getValue();
                norWrapper.getCompareList().forEach(norCompareCondition -> norBsonList.add(queryCondition(norCompareCondition)));
                norBsonList.addAll(norWrapper.getBasicDBObjectList());
                mongoPlusBasicDBObject.put(Filters.nor(norBsonList));
                break;
            case TYPE:
                Object typeValue = compareCondition.getValue();
                if (typeValue instanceof String){
                    mongoPlusBasicDBObject.put(Filters.type(compareCondition.getColumn(), (String) typeValue));
                    break;
                }
                if (typeValue instanceof TypeEnum){
                    typeValue = ((TypeEnum) typeValue).getTypeCode();
                }
                mongoPlusBasicDBObject.put(Filters.type(compareCondition.getColumn(), BsonType.findByValue((Integer) typeValue)));
                break;
            case EXISTS:
                mongoPlusBasicDBObject.put(Filters.exists(compareCondition.getColumn(), (Boolean) compareCondition.getValue()));
                break;
            case NOT:
            case EXPR:
                QueryChainWrapper<?, ?> exprWrapper = (QueryChainWrapper<?, ?>) compareCondition.getValue();
                BasicDBObject exprBasicDBObject = queryCondition(exprWrapper.getCompareList());
                List<BasicDBObject> exprBasicDBObjectList = exprWrapper.getBasicDBObjectList();
                exprBasicDBObjectList.forEach(basicDBObject -> exprBasicDBObject.putAll(basicDBObject.toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry())));
                java.util.Optional<String> exprOptional = exprBasicDBObject.keySet().stream().findFirst();
                if (exprOptional.isPresent()){
                    String exprKey = exprOptional.get();
                    mongoPlusBasicDBObject.put(Filters.expr(new BasicDBObject(exprKey, exprBasicDBObject.get(exprKey))));
                }
                break;
            case MOD:
                List<Long> modList = (List<Long>) compareCondition.getValue();
                if (modList.size() < 2){
                    throw new MongoPlusException("Mod requires modulus and remainder");
                }
                mongoPlusBasicDBObject.put(Filters.mod(compareCondition.getColumn(), modList.get(0),modList.get(1)));
                break;
            case ELEM_MATCH:
                QueryChainWrapper<?, ?> elemMatchWrapper = (QueryChainWrapper<?, ?>) compareCondition.getValue();
                BasicDBObject elemMatchBasicDBObject = queryCondition(elemMatchWrapper.getCompareList());
                Bson elemMatchBson = Filters.elemMatch(compareCondition.getColumn(), elemMatchBasicDBObject);
                if (CollUtil.isNotEmpty(elemMatchWrapper.getBasicDBObjectList())){
                    elemMatchWrapper.getBasicDBObjectList().forEach(bson -> elemMatchBson.toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry()).putAll(bson.toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry())));
                }
                mongoPlusBasicDBObject.put(elemMatchBson);
                break;
            case ALL:
                mongoPlusBasicDBObject.put(Filters.all(compareCondition.getColumn(), (Collection<?>)compareCondition.getValue()));
                break;
            case TEXT:
                mongoPlusBasicDBObject.put(Filters.text(compareCondition.getValue().toString()));
                break;
            case WHERE:
                mongoPlusBasicDBObject.put(Filters.where((String) compareCondition.getValue()));
                break;
            case SIZE:
                mongoPlusBasicDBObject.put(Filters.size(compareCondition.getColumn(), (Integer) compareCondition.getValue()));
                break;
            case BITS_ALL_CLEAR:
                mongoPlusBasicDBObject.put(Filters.bitsAllClear(compareCondition.getColumn(), (Integer) compareCondition.getValue()));
                break;
            case BITS_ALL_SET:
                mongoPlusBasicDBObject.put(Filters.bitsAllSet(compareCondition.getColumn(), (Integer) compareCondition.getValue()));
                break;
            case BITS_ANY_CLEAR:
                mongoPlusBasicDBObject.put(Filters.bitsAnyClear(compareCondition.getColumn(), (Integer) compareCondition.getValue()));
                break;
            case BITS_ANY_SET:
                mongoPlusBasicDBObject.put(Filters.bitsAnySet(compareCondition.getColumn(), (Integer) compareCondition.getValue()));
                break;
        }
        HandlerCache.conditionHandlerList.forEach(conditionHandler -> conditionHandler.afterQueryCondition(compareCondition,mongoPlusBasicDBObject));
        return mongoPlusBasicDBObject;
    }

    @Override
    public BasicDBObject buildUpdateCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate) {
        CompareCondition currentCompareCondition = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        updateBasicDBObject.put(currentCompareCondition.getColumn(),currentCompareCondition.getValue());
        return updateBasicDBObject;
    }

    @Override
    public BasicDBObject buildPushCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate) {
        CompareCondition currentCompareCondition = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        List<Object> valueList = compareConditionList.stream().filter(condition -> Objects.equals(condition.getColumn(), currentCompareCondition.getColumn())).map(CompareCondition::getValue).collect(Collectors.toList());
        updateBasicDBObject.put(currentCompareCondition.getColumn(),new BasicDBObject(SpecialConditionEnum.EACH.getCondition(),valueList));
        return updateBasicDBObject;
    }

    @Override
    public BasicDBObject buildCurrentDateCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate){
        CompareCondition currentCompareCondition = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        CurrentDateType currentDateType = currentCompareCondition.getValue(CurrentDateType.class);
        updateBasicDBObject.put(currentCompareCondition.getColumn(), new BasicDBObject(SpecialConditionEnum.TYPE.getCondition(),currentDateType.getType()));
        return updateBasicDBObject;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BasicDBObject buildRenameCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate) {
        CompareCondition currentCompareCondition = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        MutablePair<String,String> pairValue = currentCompareCondition.getValue(MutablePair.class);
        updateBasicDBObject.put(pairValue.getLeft(),pairValue.getRight());
        return updateBasicDBObject;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BasicDBObject buildUnsetCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate) {
        CompareCondition currentCompareCondition = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        List<String> pairValue = currentCompareCondition.getValue(List.class);
        pairValue.forEach(column -> updateBasicDBObject.put(column,""));
        return updateBasicDBObject;
    }

    @Override
    public BasicDBObject buildAddToSetCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate) {
        CompareCondition currentCompareCondition = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        updateBasicDBObject.put(currentCompareCondition.getColumn(),
                currentCompareCondition.getExtraValue(Boolean.class) ?
                        new BasicDBObject(SpecialConditionEnum.EACH.getCondition(),currentCompareCondition.getValue()) :
                        currentCompareCondition.getValue());
        return updateBasicDBObject;
    }

    @Override
    public BasicDBObject buildPullCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate) {
        CompareCondition currentCompareCondition = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        Object value = currentCompareCondition.getValue();
        if (currentCompareCondition.getExtraValue(Boolean.class)){
            QueryChainWrapper<?,?> wrapper = currentCompareCondition.getValue(QueryChainWrapper.class);
            BasicDBObject queriedCondition = queryCondition(wrapper.getCompareList());
            if (CollUtil.isNotEmpty(wrapper.getBasicDBObjectList())){
                wrapper.getBasicDBObjectList().forEach(basicDBObject -> basicDBObject.putAll(basicDBObject.toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry())));
                value = queriedCondition;
            }
        }
        updateBasicDBObject.put(currentCompareCondition.getColumn(),value);
        return updateBasicDBObject;
    }

}
