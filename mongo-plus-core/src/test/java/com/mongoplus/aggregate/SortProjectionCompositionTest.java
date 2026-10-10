package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongoplus.conditions.operation.ConditionOperators;
import com.mongoplus.aggregate.pipeline.Projections;
import com.mongoplus.aggregate.pipeline.Sorts;
import com.mongoplus.model.Order;
import com.mongoplus.model.Projection;
import com.mongoplus.support.SFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;

/** 真实公开 API 的 BSON 编码；模式合法性由 Index 消费测试验证，不冒充服务端执行。 */
public class SortProjectionCompositionTest {
    @Test
    public void mixedDirectionsRetainEveryKeyInBothReductionOverloads() {
        Bson[] parts = {Sorts.desc("createTime"), Sorts.asc("score"), Sorts.desc("id")};
        for (Bson body : Arrays.asList(Sorts.orderBy(parts), Sorts.orderBy(Arrays.asList(parts)))) {
            assertDocument(body, "{createTime:-1,score:1,id:-1}", "createTime", "score", "id");
            for (String key : encode(body).keySet()) { Assert.assertEquals(BsonType.INT32, encode(body).get(key).getBsonType()); }
        }
        Assert.assertEquals(encode(Sorts.orderBy(parts)), encode(Sorts.orderBy(
                new Order("createTime", -1), new Order("score", 1), new Order("id", -1))));
    }

    @Test
    public void sortBsonPassesBodyThroughAndDoesNotWrapIt() {
        Aggregate<?> aggregate = new AggregateWrapper();
        Bson body = Sorts.orderBy(Sorts.desc("createTime"), Sorts.asc("score"), Sorts.desc("id"));
        Assert.assertSame(aggregate, aggregate.sort(body));
        Assert.assertEquals(encode(body), encode(aggregate.getAggregateCondition()));
        Assert.assertFalse(encode(aggregate.getAggregateCondition()).containsKey("$sort"));
        // 保留旧透传行为；body 包装由独立的 sortSpecification 入口提供。
        Bson driverStage = com.mongodb.client.model.Aggregates.sort(body);
        assertDocument(driverStage, "{$sort:{createTime:-1,score:1,id:-1}}", "$sort");
    }

    @Test
    public void coreSameDirectionStagesRemainCompleteAndOrdered() {
        Aggregate<?> aggregate = new AggregateWrapper();
        aggregate.sortAsc(Arrays.asList("createTime", "score", "id"));
        aggregate.sortDesc("createTime", "score", "id");
        Assert.assertEquals(2, aggregate.getAggregateConditionList().size());
        assertDocument(encode(aggregate.getAggregateCondition(0)).getDocument("$sort"),
                "{createTime:1,score:1,id:1}", "createTime", "score", "id");
        assertDocument(encode(aggregate.getAggregateCondition(1)).getDocument("$sort"),
                "{createTime:-1,score:-1,id:-1}", "createTime", "score", "id");
    }

    @Test
    public void includeAndIdExceptionHaveExactInt32Flags() {
        for (Bson body : Arrays.asList(
                Projections.fields(Projections.include("name", "age"), Projections.excludeId()),
                Projections.fields(Arrays.asList(Projections.include(Arrays.asList("name", "age")), Projections.exclude("_id"))))) {
            assertProject(body, "{name:1,age:1,_id:0}", "name", "age", "_id");
            for (String key : encode(body).keySet()) { Assert.assertEquals(BsonType.INT32, encode(body).get(key).getBsonType()); }
        }
    }

    @Test
    public void excludeOnlyDoesNotIntroduceAnIdField() {
        assertProject(Projections.exclude("name", "phone"), "{name:0,phone:0}", "name", "phone");
        assertProject(Projections.exclude(Arrays.asList("name", "phone")), "{name:0,phone:0}", "name", "phone");
    }

    @Test
    public void computedCombinationPreservesNestedExpressionAndOperandOrder() {
        Bson[] expressions = {ConditionOperators.multiply("$price", "$quantity"),
                ConditionOperators.multiply(Arrays.asList("$price", "$quantity"))};
        for (Bson expression : expressions) {
            Bson body = Projections.fields(Projections.include("name"), Projections.computed("total", expression), Projections.excludeId());
            assertProject(body, "{name:1,total:{$multiply:['$price','$quantity']},_id:0}", "name", "total", "_id");
        }
    }

    @Test
    public void illegalMixedProjectionIsEncodedUnchangedByCore() {
        Bson body = Projections.fields(Projections.include("name"), Projections.exclude("phone"));
        assertProject(body, "{name:1,phone:0}", "name", "phone");
        // Core 只编码；下游必须消费模式证据拒绝，不能把 Core 未报错当 MongoDB 合法。
    }

    @Test
    public void numbersAndBooleansKeepTheirRuntimeBsonTypes() {
        assertDocument(Projections.computed("name", true), "{name:true}", "name");
        Assert.assertEquals(BsonType.BOOLEAN, encode(Projections.computed("name", true)).get("name").getBsonType());
        Assert.assertEquals(BsonType.INT64, encode(Projections.computed("name", 1L)).get("name").getBsonType());
        Assert.assertEquals(BsonType.DOUBLE, encode(Projections.computed("name", 0.5)).get("name").getBsonType());
        Bson expression = ConditionOperators.multiply(1, "$price");
        assertProject(Projections.computed("total", expression), "{total:{$multiply:[1,'$price']}}", "total");
        Assert.assertEquals(BsonType.INT32, encode(expression).getArray("$multiply").get(0).getBsonType());
    }

    @Test
    public void getterAndStringOverloadsHaveIndependentEncoding() {
        SFunction<Fields, String> name = Fields::getName;
        SFunction<Fields, String> phone = Fields::getPhone;
        assertDocument(Projections.include(name, phone), "{name:1,phone:1}", "name", "phone");
        assertDocument(Projections.includeLambda(Arrays.asList(name, phone)), "{name:1,phone:1}", "name", "phone");
        assertDocument(Projections.exclude(name, phone), "{name:0,phone:0}", "name", "phone");
        assertDocument(Projections.excludeLambda(Arrays.asList(name, phone)), "{name:0,phone:0}", "name", "phone");
        assertDocument(Sorts.asc(name, phone), "{name:1,phone:1}", "name", "phone");
        assertDocument(Sorts.ascLambda(Arrays.asList(name, phone)), "{name:1,phone:1}", "name", "phone");
        assertDocument(Sorts.desc(name, phone), "{name:-1,phone:-1}", "name", "phone");
        assertDocument(Sorts.descLambda(Arrays.asList(name, phone)), "{name:-1,phone:-1}", "name", "phone");
        assertDocument(Projections.computed(name, "$price"), "{name:'$price'}", "name");
    }

    @Test
    public void legacyProjectApisAppendIdSuppressionOnlyWhenFalse() {
        Aggregate<?> aggregate = new AggregateWrapper();
        aggregate.projectDisplay(false, "name", "age");
        assertDocument(encode(aggregate.getAggregateCondition()).getDocument("$project"), "{name:1,age:1,_id:0}", "name", "age", "_id");
        Aggregate<?> entries = new AggregateWrapper();
        entries.project(false, new Projection("name", 1), new Projection("total", ConditionOperators.multiply("$price", "$quantity")));
        assertDocument(encode(entries.getAggregateCondition()).getDocument("$project"),
                "{name:1,total:{$multiply:['$price','$quantity']},_id:0}", "name", "total", "_id");
        Aggregate<?> excluded = new AggregateWrapper();
        excluded.projectNone("name", "phone");
        assertDocument(encode(excluded.getAggregateCondition()).getDocument("$project"), "{name:0,phone:0}", "name", "phone");
    }

    @Test
    public void duplicateAndEmptyRuntimeReductionFactsRemainExplicit() {
        assertDocument(Sorts.orderBy(Sorts.asc("name", "phone"), Sorts.desc("name")), "{name:-1,phone:1}", "name", "phone");
        assertDocument(Projections.fields(Projections.include("name", "phone"), Projections.exclude("name")),
                "{phone:1,name:0}", "phone", "name");
        assertDocument(Sorts.orderBy(Collections.<Bson>emptyList()), "{}");
        assertDocument(Projections.fields(Collections.<Bson>emptyList()), "{}");
    }

    private static void assertProject(Bson body, String expected, String... keys) {
        Aggregate<?> aggregate = new AggregateWrapper();
        Assert.assertSame(aggregate, aggregate.project(body));
        aggregate.limit(2);
        Assert.assertEquals(2, aggregate.getAggregateConditionList().size());
        assertDocument(encode(aggregate.getAggregateCondition()).getDocument("$project"), expected, keys);
        Assert.assertEquals(BsonDocument.parse("{$limit:2}"), encode(aggregate.getAggregateCondition(1)));
    }

    private static void assertDocument(Bson bson, String expected, String... keys) {
        BsonDocument actual = encode(bson);
        Assert.assertEquals(BsonDocument.parse(expected), actual);
        Assert.assertEquals(Arrays.asList(keys), new ArrayList<>(actual.keySet()));
    }

    private static BsonDocument encode(Bson bson) {
        return bson.toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
    }

    public static class Fields {
        private String name;
        private String phone;
        public String getName() { return name; }
        public String getPhone() { return phone; }
    }
}
