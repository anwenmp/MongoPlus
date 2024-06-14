package com.anwen.mongo.conditions.aggregate.aggregate;

import com.anwen.mongo.conditions.BuildCondition;
import com.anwen.mongo.conditions.interfaces.aggregate.aggregate.Aggregate;
import com.anwen.mongo.conditions.interfaces.aggregate.pipeline.project.Projection;
import com.anwen.mongo.conditions.query.QueryChainWrapper;
import com.anwen.mongo.constant.AggregationOperators;
import com.anwen.mongo.constant.SqlOperationConstant;
import com.anwen.mongo.enums.OrderEnum;
import com.anwen.mongo.enums.ProjectionEnum;
import com.anwen.mongo.model.aggregate.Field;
import com.anwen.mongo.support.SFunction;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.*;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class AggregateChainWrapper<Children> implements Aggregate<Children> {

    /**
     * 定义管道条件集合，需要通过调用顺序控制管道的顺序
     */
    private final List<Bson> aggregateConditionList = new ArrayList<>();

    @Override
    public List<Bson> getAggregateConditionList() {
        return aggregateConditionList;
    }

    /**
     * 定义自己
     * @date 2024/6/7 下午3:56
     */
    protected final Children typedThis = (Children) this;

    @Override
    public Children addFields(String field, String value) {
        return addFields(Field.of(field,value));
    }

    @Override
    public <T> Children addFields(SFunction<T, ?> field, String value) {
        return addFields(field.getFieldNameLine(),value);
    }

    @Override
    public <T> Children addFields(String value, SFunction<T, ?>... field) {
        StringBuilder sb = new StringBuilder();
        for (SFunction<T,?> sFunction : field) {
            sb.append(sFunction.getFieldNameLine()).append(".");
        }
        sb.deleteCharAt(sb.length()-1);
        return addFields(sb.toString(),value);
    }

    @Override
    public <T> Children addFields(SFunction<T,?> field, Object value) {
        return addFields(Field.of(field,value));
    }

    @Override
    public <T> Children addFields(SFunction<T, ?> field, Collection<?> value) {
        return addFields(field.getFieldNameLine(),value);
    }

    @Override
    public Children addFields(String field, Collection<?> value) {
        return addFields(Field.of(field, new BasicDBObject(AggregationOperators.CONCAT_ARRAYS, new ArrayList<Bson>() {{
            add(new BasicDBObject(AggregationOperators.FIELD + field, value));
        }})));
    }

    @Override
    public Children addFields(Field<?>... fields) {
        return addFields(new ArrayList<>(Arrays.asList(fields)));
    }

    @Override
    public Children addFields(List<Field<?>> fields) {
        return addFields(Aggregates.addFields(new ArrayList<>(fields)));
    }

    @Override
    public Children addFields(Bson bson) {
        return custom(bson);
    }

    @Override
    public Children set(String field, String value) {
        return set(Field.of(field,value));
    }

    @Override
    public <T> Children set(SFunction<T,?> field, String value) {
        return set(field.getFieldNameLine(),value);
    }

    @Override
    public <T> Children set(String value, SFunction<T,?>... field) {
        StringBuilder sb = new StringBuilder();
        for (SFunction<T,?> sFunction : field) {
            sb.append(sFunction.getFieldNameLine()).append(".");
        }
        sb.deleteCharAt(sb.length()-1);
        return set(sb.toString(),value);
    }

    @Override
    public <T> Children set(SFunction<T,?> field, Object value) {
        return set(Field.of(field,value));
    }

    @Override
    public <T> Children set(SFunction<T,?> field, Collection<?> value) {
        return set(field.getFieldNameLine(),value);
    }

    @Override
    public Children set(String field, Collection<?> value) {
        return set(Field.of(field, new BasicDBObject(AggregationOperators.CONCAT_ARRAYS, new ArrayList<Bson>() {{
            add(new BasicDBObject(AggregationOperators.FIELD + field, value));
        }})));
    }

    @Override
    public Children set(Field<?>... fields) {
        return set(new ArrayList<>(Arrays.asList(fields)));
    }

    @Override
    public Children set(List<Field<?>> fields) {
        return set(Aggregates.set(new ArrayList<>(fields)));
    }

    @Override
    public Children set(Bson bson) {
        return custom(bson);
    }

    @Override
    public <Boundary,T> Children bucket(SFunction<T,?> groupBy, List<Boundary> boundaries) {
        return bucket(groupBy.getFieldNameLineOption(),boundaries);
    }

    @Override
    public <Boundary> Children bucket(Object groupBy, List<Boundary> boundaries) {
        return bucket(Aggregates.bucket(groupBy,boundaries));
    }

    @Override
    public <Boundary,T> Children bucket(SFunction<T,?> groupBy, List<Boundary> boundaries, BucketOptions options) {
        return bucket(groupBy.getFieldNameLineOption(),boundaries,options);
    }

    @Override
    public <Boundary> Children bucket(Object groupBy, List<Boundary> boundaries, BucketOptions options) {
        return bucket(Aggregates.bucket(groupBy,boundaries,options));
    }

    @Override
    public Children bucket(Bson bson) {
        return custom(bson);
    }

    @Override
    public <T> Children bucketAuto(SFunction<T,?> groupBy, Integer buckets) {
        return bucketAuto(groupBy.getFieldNameLineOption(),buckets);
    }

    @Override
    public Children bucketAuto(Object groupBy, Integer buckets) {
        return bucketAuto(Aggregates.bucketAuto(groupBy,buckets));
    }

    @Override
    public <T> Children bucketAuto(SFunction<T,?> groupBy, Integer buckets, BucketAutoOptions options) {
        return bucketAuto(groupBy.getFieldNameLineOption(),buckets,options);
    }

    @Override
    public Children bucketAuto(Object groupBy, Integer buckets, BucketAutoOptions options) {
        return bucketAuto(Aggregates.bucketAuto(groupBy,buckets,options));
    }

    @Override
    public Children bucketAuto(Bson bson) {
        return custom(bson);
    }

    @Override
    public Children count() {
        return custom(Aggregates.count());
    }

    @Override
    public Children count(String field) {
        return custom(Aggregates.count(field));
    }

    @Override
    public <T> Children count(SFunction<T, ?> field) {
        return count(field.getFieldNameLine());
    }

    @Override
    public Children match(QueryChainWrapper<?, ?> queryChainWrapper) {
        return match(BuildCondition.buildQueryCondition(queryChainWrapper.getCompareList()));
    }

    @Override
    public Children match(Bson bson) {
        return custom(bson);
    }

    @Override
    public <T, R> Children projectDisplay(SFunction<T, R>... column) {
        return buildProject(column,ProjectionEnum.DISPLAY.getValue());
    }

    @Override
    public Children projectDisplay(String... column) {
        return buildProject(column,ProjectionEnum.DISPLAY.getValue());
    }

    @Override
    public <T, R> Children projectNone(SFunction<T, R>... column) {
        return buildProject(column,ProjectionEnum.NONE.getValue());
    }

    @Override
    public Children projectNone(String... column) {
        return buildProject(column,ProjectionEnum.NONE.getValue());
    }

    @Override
    public Children project(boolean displayId, Projection... projection) {
        return buildProject(displayId, Arrays.stream(projection).collect(Collectors.toList()));
    }

    @Override
    public <T, R> Children projectDisplay(boolean displayId, SFunction<T, R>... column) {
        return buildProject(displayId,column,ProjectionEnum.DISPLAY.getValue());
    }

    @Override
    public Children projectDisplay(boolean displayId, String... column) {
        return buildProject(displayId,column,ProjectionEnum.DISPLAY.getValue());
    }

    @Override
    public <T, R> Children projectNone(boolean displayId, SFunction<T, R>... column) {
        return buildProject(displayId,column,ProjectionEnum.NONE.getValue());
    }

    @Override
    public Children projectNone(boolean displayId, String... column) {
        return buildProject(displayId,column,ProjectionEnum.NONE.getValue());
    }

    @Override
    public Children project(Bson bson) {
        return custom(bson);
    }

    @Override
    public Children sort(String field, Integer value) {
        return sort(orderBy(field,value));
    }

    @Override
    public <T> Children sort(SFunction<T, ?> field, Integer value) {
        return sort(field.getFieldNameLine(),value);
    }

    @Override
    public <T> Children sortAsc(SFunction<T, ?> field) {
        return sortAsc(field.getFieldNameLine());
    }

    @Override
    public Children sortAsc(String field) {
        return sort(field,OrderEnum.ASC.getValue());
    }

    @Override
    public <T> Children sortAsc(SFunction<T, ?>... field) {
        return sortAscLambda(Arrays.stream(field).collect(Collectors.toList()));
    }

    @Override
    public Children sortAsc(String... field) {
        return sortAsc(Arrays.asList(field));
    }

    @Override
    public <T> Children sortAscLambda(List<SFunction<T, ?>> field) {
        return sortAsc(field.stream().map(SFunction::getFieldNameLine).collect(Collectors.toList()));
    }

    @Override
    public Children sortAsc(List<String> field) {
        return sort(orderBy(field,OrderEnum.ASC.getValue()));
    }

    @Override
    public <T> Children sortDesc(SFunction<T, ?> field) {
        return sortDesc(field.getFieldNameLine());
    }

    @Override
    public Children sortDesc(String field) {
        return sort(field,OrderEnum.DESC.getValue());
    }

    @Override
    public <T> Children sortDesc(SFunction<T, ?>... field) {
        return sortDescLambda(Arrays.stream(field).collect(Collectors.toList()));
    }

    @Override
    public Children sortDesc(String... field) {
        return sortDesc(Arrays.asList(field));
    }

    @Override
    public <T> Children sortDescLambda(List<SFunction<T, ?>> field) {
        return sortDesc(field.stream().map(SFunction::getFieldNameLine).collect(Collectors.toList()));
    }

    @Override
    public Children sortDesc(List<String> field) {
        return sort(orderBy(field,OrderEnum.DESC.getValue()));
    }

    @Override
    public Children metaTextScore(String field) {
        return sort(Sorts.metaTextScore(field));
    }

    @Override
    public <T> Children metaTextScore(SFunction<T, ?> field) {
        return metaTextScore(field.getFieldNameLine());
    }

    @Override
    public Children sort(Bson bson) {
        return custom(bson);
    }

    @Override
    public Children sortByCount(String field) {
        return custom(Aggregates.sortByCount(field));
    }

    @Override
    public <T> Children sortByCount(SFunction<T, ?> field) {
        return sortByCount(field.getFieldNameLineOption());
    }

    @Override
    public Children skip(long skip) {
        return skip(Math.toIntExact(skip));
    }

    @Override
    public Children skip(int skip) {
        return custom(Aggregates.skip(skip));
    }

    @Override
    public Children limit(long limit) {
        return limit(Math.toIntExact(limit));
    }

    @Override
    public Children limit(int limit) {
        return custom(Aggregates.limit(limit));
    }

    @Override
    public Children lookup(String from, String localField, String foreignField, String as) {
        return lookup(Aggregates.lookup(from, localField, foreignField, as));
    }

    @Override
    public <T, R> Children lookup(String from, SFunction<T, ?> localField, SFunction<R, ?> foreignField, String as) {
        return lookup(from, localField.getFieldNameLine(),foreignField.getFieldNameLine(),as);
    }

    @Override
    public <T> Children lookup(String from, SFunction<T, ?> localField, String foreignField, String as) {
        return lookup(from, localField.getFieldNameLine(),foreignField,as);
    }

    @Override
    public <T> Children lookup(String from, String localField, SFunction<T, ?> foreignField, String as) {
        return lookup(from, localField,foreignField.getFieldNameLine(),as);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public <TExpression> Children lookup(String from, List<Variable<TExpression>> letList, AggregateChainWrapper<?> aggregate, String as) {
        return custom(Aggregates.lookup(from,(List) letList,aggregate.getAggregateConditionList(),as));
    }

    @Override
    public Children lookup(String from, AggregateChainWrapper<?> aggregate, String as) {
        return custom(Aggregates.lookup(from,aggregate.getAggregateConditionList(),as));
    }

    @Override
    public Children lookup(Bson bson) {
        return custom(bson);
    }

    @Override
    public Children facet(String name, Bson... pipeline) {
        return facet(name,Arrays.asList(pipeline));
    }

    @Override
    public Children facet(String name, List<? extends Bson> pipeline) {
        return facet(new Facet(name,pipeline));
    }

    @Override
    public Children facet(String name, Aggregate<?> aggregate) {
        return facet(name,aggregate.getAggregateConditionList());
    }

    @Override
    public Children facet(Facet... facets) {
        return facet(Aggregates.facet(facets));
    }

    @Override
    public Children facet(List<Facet> facets) {
        return facet(Aggregates.facet(facets));
    }

    @Override
    public Children facet(Bson bson) {
        return custom(bson);
    }

    @Override
    public Children graphLookup(String from, Object startWith, String connectFromField, String connectToField, String as) {
        return graphLookup(Aggregates.graphLookup(from,startWith,connectFromField,connectToField,as));
    }

    @Override
    public <T,R,U> Children graphLookup(String from, SFunction<T, ?> startWith, SFunction<R, ?> connectFromField, SFunction<U, ?> connectToField, String as) {
        return graphLookup(from,startWith.getFieldNameLineOption(),connectFromField.getFieldNameLine(),connectToField.getFieldNameLine(),as);
    }

    @Override
    public <T, R> Children graphLookup(String from, Object startWith, SFunction<T, ?> connectFromField, SFunction<R, ?> connectToField, String as) {
        return graphLookup(from,startWith,connectFromField.getFieldNameLine(),connectToField.getFieldNameLine(),as);
    }

    @Override
    public Children graphLookup(String from, Object startWith, String connectFromField, String connectToField, String as, GraphLookupOptions options) {
        return graphLookup(Aggregates.graphLookup(from,startWith,connectFromField,connectToField,as,options));
    }

    @Override
    public <T, R, U> Children graphLookup(String from, SFunction<T, ?> startWith, SFunction<R, ?> connectFromField, SFunction<U, ?> connectToField, String as, GraphLookupOptions options) {
        return graphLookup(from,startWith.getFieldNameLineOption(),connectFromField.getFieldNameLine(),connectToField.getFieldNameLine(),as,options);
    }

    @Override
    public <T, R> Children graphLookup(String from, Object startWith, SFunction<T, ?> connectFromField, SFunction<R,?> connectToField, String as, GraphLookupOptions options) {
        return graphLookup(from,startWith,connectFromField.getFieldNameLine(),connectToField.getFieldNameLine(),as,options);
    }

    @Override
    public Children graphLookup(Bson bson) {
        return custom(bson);
    }

    @Override
    public Children group(String _id) {
        return null;
    }

    @Override
    public <T> Children group(SFunction<T, ?> _id) {
        return null;
    }

    @Override
    public <TExpression> Children group(TExpression id, BsonField... fieldAccumulators) {
        return null;
    }

    @Override
    public <TExpression> Children group(TExpression id, List<BsonField> fieldAccumulators) {
        return null;
    }

    @Override
    public Children group(Bson bson) {
        return null;
    }

    @Override
    public Children custom(Bson bson) {
        this.aggregateConditionList.add(bson);
        return typedThis;
    }

    private Children buildProject(boolean displayId,List<Projection> projectionList){
        if (!displayId){
            projectionList.add(Projection.builder().column(SqlOperationConstant._ID).value(ProjectionEnum.NONE.getValue()).build());
        }
        return buildProject(projectionList);
    }

    private Children buildProject(boolean displayId,String[] column,Integer value){
        return buildProject(displayId,Arrays.stream(column).map(c -> Projection.builder().column(c).value(value).build()).collect(Collectors.toList()));
    }

    private Children buildProject(boolean displayId,SFunction<?,?>[] column,Integer value){
        return buildProject(displayId,Arrays.stream(column).map(SFunction::getFieldNameLine).toArray(String[]::new),value);
    }

    private Children buildProject(String[] column,Integer value){
        List<Projection> projectionList = new ArrayList<>();
        for (String c : column) {
            projectionList.add(new Projection(c,value));
        }
        return buildProject(projectionList);
    }

    private Children buildProject(SFunction<?,?>[] column,Integer value){
        return buildProject(Arrays.stream(column).map(SFunction::getFieldNameLine).toArray(String[]::new),value);
    }

    private Children buildProject(List<Projection> projectionList){
        return project(Aggregates.project(BuildCondition.buildProjection(projectionList)));
    }

    private Bson orderBy(final String fieldName, final Integer value){
        return orderBy(Collections.singletonList(fieldName),value);
    }

    private Bson orderBy(final List<String> fieldNames, final Integer value) {
        BsonDocument document = new BsonDocument();
        fieldNames.forEach(fieldName -> document.append(fieldName, new BsonInt32(value)));
        return document;
    }

}
