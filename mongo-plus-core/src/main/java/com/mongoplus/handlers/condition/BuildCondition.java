package com.mongoplus.handlers.condition;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.geojson.Geometry;
import com.mongoplus.bson.MongoPlusBasicDBObject;
import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.conditions.AbstractChainWrapper;
import com.mongoplus.options.PushOptions;
import com.mongoplus.options.TextSearchOptions;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.model.Order;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.enums.*;
import com.mongoplus.model.BaseConditionResult;
import com.mongoplus.model.BuildUpdate;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.geo.GeoBox;
import com.mongoplus.model.geo.GeoCenter;
import com.mongoplus.model.geo.GeoNear;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.CollUtil;
import com.mongoplus.toolkit.Filters;
import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.mongoplus.enums.QueryOperatorEnum.*;
import static com.mongoplus.enums.QueryOperatorEnum.EQ;
import static com.mongoplus.enums.QueryOperatorEnum.REGEX;
import static com.mongoplus.enums.SpecialConditionEnum.*;


/**
 * 构建条件
 *
 * @author anwen
 */
public class BuildCondition extends AbstractCondition {

    private static Condition DEFAULT_BUSINESS_CONDITION;

    public static Condition condition() {
        return DEFAULT_BUSINESS_CONDITION;
    }

    public static void setCondition(Condition condition) {
        DEFAULT_BUSINESS_CONDITION = condition;
    }

    static {
        DEFAULT_BUSINESS_CONDITION = new BuildCondition();
    }

    public BuildCondition() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public BasicDBObject queryCondition(ConditionMetaObject conditionMetaObject,
                                        MongoPlusBasicDBObject mongoPlusBasicDBObject) {
        HandlerCache.conditionHandlerList.forEach(conditionHandler ->
                conditionHandler.beforeQueryCondition(conditionMetaObject, mongoPlusBasicDBObject));
        QueryOperatorEnum query = null;
        if (conditionMetaObject != null) {
            query = QueryOperatorEnum.getQueryOperator(conditionMetaObject.getCondition());
        }
        switch (Objects.requireNonNull(query)) {
            case EQ:
                mongoPlusBasicDBObject.put(
                        new Document(
                                conditionMetaObject.getColumn(),
                                new Document(EQ.getOperatorValue(), conditionMetaObject.getValue())
                        )
                );
                break;
            case NE:
                mongoPlusBasicDBObject.put(Filters.ne(conditionMetaObject.getColumn(), conditionMetaObject.getValue()));
                break;
            case GT:
                mongoPlusBasicDBObject.put(Filters.gt(conditionMetaObject.getColumn(), conditionMetaObject.getValue()));
                break;
            case LT:
                mongoPlusBasicDBObject.put(Filters.lt(conditionMetaObject.getColumn(), conditionMetaObject.getValue()));
                break;
            case GTE:
                mongoPlusBasicDBObject.put(Filters.gte(conditionMetaObject.getColumn(), conditionMetaObject.getValue()));
                break;
            case LTE:
                mongoPlusBasicDBObject.put(Filters.lte(conditionMetaObject.getColumn(), conditionMetaObject.getValue()));
                break;
            case REGEX:
            case LIKE:
                RegexOptions regexOptions = conditionMetaObject.getExtraValue(RegexOptions.class);
                Document likeDocument = new Document(conditionMetaObject.getColumn(),
                        new Document(REGEX.getOperatorValue(), conditionMetaObject.getValue().toString())
                                .append(CommonOperators.OPTIONS.getOperator(),
                                        regexOptions == null ? RegexOptions.CASE_INSENSITIVE.getFlag() : regexOptions.getFlag())
                );
                mongoPlusBasicDBObject.put(likeDocument);
                break;
            case IN:
                mongoPlusBasicDBObject.put(Filters.in(conditionMetaObject.getColumn(),
                        (Collection<?>) conditionMetaObject.getValue()));
                break;
            case NIN:
                mongoPlusBasicDBObject.put(Filters.nin(conditionMetaObject.getColumn(),
                        (Collection<?>) conditionMetaObject.getValue()));
                break;
            case AND:
                logic((QueryChainWrapper<?, ?>) conditionMetaObject.getValue(), mongoPlusBasicDBObject, Filters::and);
                break;
            case OR:
                logic((QueryChainWrapper<?, ?>) conditionMetaObject.getValue(), mongoPlusBasicDBObject, Filters::or);
                break;
            case NOR:
                logic((QueryChainWrapper<?, ?>) conditionMetaObject.getValue(), mongoPlusBasicDBObject, Filters::nor);
                break;
            case TYPE:
                Object typeValue = conditionMetaObject.getValue();
                if (typeValue instanceof String) {
                    mongoPlusBasicDBObject.put(Filters.type(conditionMetaObject.getColumn(), (String) typeValue));
                    break;
                }
                if (typeValue instanceof TypeEnum) {
                    typeValue = ((TypeEnum) typeValue).getTypeCode();
                }
                mongoPlusBasicDBObject.put(Filters.type(conditionMetaObject.getColumn(),
                        BsonType.findByValue((Integer) typeValue)));
                break;
            case EXISTS:
                mongoPlusBasicDBObject.put(Filters.exists(conditionMetaObject.getColumn(),
                        (Boolean) conditionMetaObject.getValue()));
                break;
            case NOT:
            case EXPR:
                QueryChainWrapper<?, ?> exprWrapper = (QueryChainWrapper<?, ?>) conditionMetaObject.getValue();
                BaseConditionResult baseConditionResult = exprWrapper.buildCondition();
                BasicDBObject exprBasicDBObject = baseConditionResult.getCondition();
                Optional<String> exprOptional = exprBasicDBObject.keySet().stream().findFirst();
                exprOptional.ifPresent(exprKey -> mongoPlusBasicDBObject.put(Filters.expr(
                        new BasicDBObject(exprKey, exprBasicDBObject.get(exprKey))
                )));
                break;
            case MOD:
                List<Long> modList = (List<Long>) conditionMetaObject.getValue();
                if (modList.size() < 2) {
                    throw new MongoPlusException("Mod requires modulus and remainder");
                }
                mongoPlusBasicDBObject.put(Filters.mod(conditionMetaObject.getColumn(), modList.get(0), modList.get(1)));
                break;
            case ELEM_MATCH:
                QueryChainWrapper<?, ?> elemMatchWrapper = (QueryChainWrapper<?, ?>) conditionMetaObject.getValue();
                BasicDBObject elemMatchBasicDBObject = queryCondition(elemMatchWrapper).getCondition();
                Bson elemMatchBson = Filters.elemMatch(conditionMetaObject.getColumn(), elemMatchBasicDBObject);
                if (CollUtil.isNotEmpty(elemMatchWrapper.getBasicDBObjectList())) {
                    elemMatchWrapper.getBasicDBObjectList().forEach(bson ->
                            elemMatchBson.toBsonDocument(
                                            BsonDocument.class,
                                            MapCodecCache.getDefaultCodecRegistry()).
                                    putAll(bson.toBsonDocument(
                                            BsonDocument.class,
                                            MapCodecCache.getDefaultCodecRegistry()
                                    )));
                }
                mongoPlusBasicDBObject.put(elemMatchBson);
                break;
            case ALL:
                mongoPlusBasicDBObject.put(Filters.all(conditionMetaObject.getColumn(),
                        (Collection<?>) conditionMetaObject.getValue()));
                break;
            case TEXT:
                Bson textBson;
                Object value = conditionMetaObject.getValue();
                TextSearchOptions textSearchOptions = conditionMetaObject.getExtraValue(TextSearchOptions.class);
                if (textSearchOptions != null) {
                    textBson = Filters.text(value.toString(), textSearchOptions.to());
                } else {
                    textBson = Filters.text(value.toString());
                }
                mongoPlusBasicDBObject.put(textBson);
                break;
            case WHERE:
                mongoPlusBasicDBObject.put(Filters.where((String) conditionMetaObject.getValue()));
                break;
            case SIZE:
                mongoPlusBasicDBObject.put(Filters.size(conditionMetaObject.getColumn(),
                        (Integer) conditionMetaObject.getValue()));
                break;
            case BITS_ALL_CLEAR:
                mongoPlusBasicDBObject.put(Filters.bitsAllClear(conditionMetaObject.getColumn(),
                        (Integer) conditionMetaObject.getValue()));
                break;
            case BITS_ALL_SET:
                mongoPlusBasicDBObject.put(Filters.bitsAllSet(conditionMetaObject.getColumn(),
                        (Integer) conditionMetaObject.getValue()));
                break;
            case BITS_ANY_CLEAR:
                mongoPlusBasicDBObject.put(Filters.bitsAnyClear(conditionMetaObject.getColumn(),
                        (Integer) conditionMetaObject.getValue()));
                break;
            case BITS_ANY_SET:
                mongoPlusBasicDBObject.put(Filters.bitsAnySet(conditionMetaObject.getColumn(),
                        (Integer) conditionMetaObject.getValue()));
                break;
            case GEO_INTERSECTS:
                Object geometry = conditionMetaObject.getValue();
                if (ClassTypeUtil.isTargetClass(Geometry.class,geometry.getClass())) {
                    mongoPlusBasicDBObject.put(
                            Filters.geoIntersects(conditionMetaObject.getColumn(),(Geometry) geometry)
                    );
                } else {
                    mongoPlusBasicDBObject.put(
                            Filters.geoIntersects(conditionMetaObject.getColumn(),(Bson) geometry)
                    );
                }
                break;
            case GEO_WITHIN:
                Object withinGeometry = conditionMetaObject.getValue();
                if (ClassTypeUtil.isTargetClass(Geometry.class,withinGeometry.getClass())) {
                    mongoPlusBasicDBObject.put(
                            Filters.geoWithin(conditionMetaObject.getColumn(),(Geometry) withinGeometry)
                    );
                } else {
                    mongoPlusBasicDBObject.put(
                            Filters.geoWithin(conditionMetaObject.getColumn(),(Bson) withinGeometry)
                    );
                }
                break;
            case NEAR:
                GeoNear geoNear = conditionMetaObject.getValue(GeoNear.class);
                mongoPlusBasicDBObject.put(geoNear.buildNear(conditionMetaObject.getColumn()));
                break;
            case NEAR_SPHERE:
                GeoNear geoNearSphere = conditionMetaObject.getValue(GeoNear.class);
                mongoPlusBasicDBObject.put(geoNearSphere.buildNearSphere(conditionMetaObject.getColumn()));
                break;
            case GEO_WITHIN_BOX:
                mongoPlusBasicDBObject.put(
                        conditionMetaObject.getValue(GeoBox.class).toBson(conditionMetaObject.getColumn())
                );
                break;
            case GEO_WITHIN_CENTER:
                GeoCenter geoCenter = conditionMetaObject.getValue(GeoCenter.class);
                mongoPlusBasicDBObject.put(
                        geoCenter.buildCenter(conditionMetaObject.getColumn())
                );
                break;
            case GEO_WITHIN_CENTER_SPHERE:
                GeoCenter geoCenterSphere = conditionMetaObject.getValue(GeoCenter.class);
                mongoPlusBasicDBObject.put(
                        geoCenterSphere.buildCenterSphere(conditionMetaObject.getColumn())
                );
                break;
            case GEO_WITHIN_POLYGON:
                mongoPlusBasicDBObject.put(
                        Filters.geoWithinPolygon(
                                conditionMetaObject.getColumn(),
                                (List<List<Double>>) conditionMetaObject.getValue()
                        )
                );
                break;
        }
        HandlerCache.conditionHandlerList.forEach(conditionHandler ->
                conditionHandler.afterQueryCondition(conditionMetaObject, mongoPlusBasicDBObject));
        return mongoPlusBasicDBObject;
    }

    @Override
    public BasicDBObject buildUpdateCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate) {
        ConditionMetaObject currentConditionMetaObject = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        updateBasicDBObject.put(currentConditionMetaObject.getColumn(), currentConditionMetaObject.getValue());
        return updateBasicDBObject;
    }

    @Override
    public BasicDBObject buildPushCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate) {
        ConditionMetaObject currentConditionMetaObject = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        Object value = currentConditionMetaObject.getValue();
        if (ClassTypeUtil.isTargetClass(Collection.class, value.getClass())) {
            PushOptions extraValue = currentConditionMetaObject.getExtraValue(PushOptions.class);
            if (Objects.isNull(extraValue)) {
                put(updateBasicDBObject, currentConditionMetaObject);
            } else {
                Bson pushOptions = buildPushOptions(currentConditionMetaObject.getValue(List.class), extraValue);
                updateBasicDBObject.put(currentConditionMetaObject.getColumn(), pushOptions);
            }
        } else {
            put(updateBasicDBObject, currentConditionMetaObject);
        }
        return updateBasicDBObject;
    }

    @Override
    public BasicDBObject buildCurrentDateCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate) {
        ConditionMetaObject currentConditionMetaObject = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        CurrentDateType currentDateType = currentConditionMetaObject.getValue(CurrentDateType.class);
        updateBasicDBObject.put(currentConditionMetaObject.getColumn(),
                new BasicDBObject(SpecialConditionEnum.TYPE.getCondition(), currentDateType.getType()));
        return updateBasicDBObject;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BasicDBObject buildRenameCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate) {
        ConditionMetaObject currentConditionMetaObject = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        MutablePair<String, String> pairValue = currentConditionMetaObject.getValue(MutablePair.class);
        updateBasicDBObject.put(pairValue.getLeft(), pairValue.getRight());
        return updateBasicDBObject;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BasicDBObject buildUnsetCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate) {
        ConditionMetaObject currentConditionMetaObject = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        List<String> pairValue = currentConditionMetaObject.getValue(List.class);
        pairValue.forEach(column -> updateBasicDBObject.put(column, ""));
        return updateBasicDBObject;
    }

    @Override
    public BasicDBObject buildAddToSetCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate) {
        ConditionMetaObject currentConditionMetaObject = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        updateBasicDBObject.put(currentConditionMetaObject.getColumn(),
                currentConditionMetaObject.getExtraValue(Boolean.class) ?
                        new BasicDBObject(SpecialConditionEnum.EACH.getCondition(), currentConditionMetaObject.getValue()) :
                        currentConditionMetaObject.getValue());
        return updateBasicDBObject;
    }

    @Override
    public BasicDBObject buildPullCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate) {
        ConditionMetaObject currentConditionMetaObject = buildUpdate.getCurrentCompareCondition();
        BasicDBObject updateBasicDBObject = buildUpdate.getUpdateBasicDBObject();
        if (currentConditionMetaObject.getExtraValue(Boolean.class)) {
            QueryChainWrapper<?, ?> wrapper = currentConditionMetaObject.getValue(QueryChainWrapper.class);
            BasicDBObject queriedCondition = queryCondition(wrapper).getCondition();
            if (CollUtil.isNotEmpty(wrapper.getBasicDBObjectList())) {
                wrapper.getBasicDBObjectList().forEach(basicDBObject -> queriedCondition.putAll(
                        basicDBObject.toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry())
                ));
            }
            updateBasicDBObject.putAll(queriedCondition.toBsonDocument(
                    BsonDocument.class,
                    MapCodecCache.getDefaultCodecRegistry()
            ));
        } else {
            updateBasicDBObject.put(currentConditionMetaObject.getColumn(), currentConditionMetaObject.getValue());
        }
        return updateBasicDBObject;
    }

    @Override
    public BaseConditionResult queryCondition(AbstractChainWrapper<?, ?> wrapper) {
        List<BasicDBObject> basicDBObjectList = wrapper.getBasicDBObjectList();
        List<Order> orderList = wrapper.getOrderList();
        BasicDBObject sortCond = new BasicDBObject();
        if (CollUtil.isNotEmpty(orderList)) {
            orderList.forEach(order -> sortCond.put(order.getColumn(), order.getType()));
        }
        BasicDBObject basicDBObject = queryCondition(wrapper.getConditionMetaObjects());
        if (CollUtil.isNotEmpty(basicDBObjectList)) {
            basicDBObjectList.forEach(basic -> basicDBObject.putAll(basic.toBsonDocument(
                    BsonDocument.class,
                    MapCodecCache.getDefaultCodecRegistry()
            )));
        }
        return new BaseConditionResult(
                basicDBObject, projectionCondition(wrapper.getProjectionList()),
                sortCond
        );
    }

    public void logic(QueryChainWrapper<?, ?> queryChainWrapper, MongoPlusBasicDBObject basicDBObject, Function<List<Bson>, Bson> function) {
        List<Bson> bsonList = new ArrayList<>();
        queryChainWrapper.getConditionMetaObjects().forEach(compareCondition -> {
            if (Objects.equals(COMBINE.getValue(), compareCondition.getCondition())) {
                bsonList.add(queryCondition(
                        ((QueryChainWrapper<?, ?>) compareCondition.getValue())
                                .getConditionMetaObjects()
                ));
            } else {
                bsonList.add(queryCondition(compareCondition));
            }
        });
        bsonList.addAll(queryChainWrapper.getBasicDBObjectList());
        basicDBObject.put(function.apply(bsonList));
    }

    protected Bson buildPushOptions(List<?> value,PushOptions options) {
        MongoPlusDocument document = new MongoPlusDocument();
        document.put(SpecialConditionEnum.EACH.getCondition(),value);
        document.putIsNotNull(POSITION.getCondition(),options.getPosition());
        document.putIsNotNull(SLICE.getCondition(),options.getSlice());
        document.putIsNotNull(SORT.getCondition(), options.getSort());
        document.putIsNotNull(SORT.getCondition(),options.getSortDocument());
        return document;
    }

    protected void put(BasicDBObject basicDBObject, ConditionMetaObject conditionMetaObject) {
        basicDBObject.put(conditionMetaObject.getColumn(), conditionMetaObject.getValue());
    }

    @SuppressWarnings("unchecked")
    public <T> void simpleUpdateLogic(List<Bson> bsonList, ConditionMetaObject conditionMetaObject, BiFunction<String,T,Bson> function) {
        bsonList.add(function.apply(conditionMetaObject.getColumn(), (T) conditionMetaObject.getValue()));
    }

    public void simpleUpdateLogic(List<Bson> bsonList, ConditionMetaObject conditionMetaObject, Function<String,Bson> function) {
        bsonList.add(function.apply(conditionMetaObject.getColumn()));
    }

}
