package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.Accumulators;
import com.mongoplus.aggregate.pipeline.Facet;
import com.mongoplus.aggregate.pipeline.UnwindOption;
import com.mongoplus.model.Projection;
import com.mongoplus.model.aggregate.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

/** 逐一执行 47 个已声明 effect 的真实 overload；这里只验证追加及 BSON，不证明 Resolver 参数准入。 */
public class CommonStageEffectTest {
    private static final String INCLUDED = "{$project:{value:1}}";
    private static final String EXCLUDED = "{$project:{value:0}}";
    private static final String INCLUDED_NO_ID = "{$project:{value:1,_id:0}}";
    private static final String EXCLUDED_NO_ID = "{$project:{value:0,_id:0}}";
    private static final String GROUP = "{$group:{_id:'$value'}}";
    private static final String GROUP_SUM = "{$group:{_id:'$value',total:{$sum:1}}}";
    private static final String UNWIND = "{$unwind:'$value'}";
    private static final String UNWIND_OPTIONS =
            "{$unwind:{path:'$value',preserveNullAndEmptyArrays:true,includeArrayIndex:'idx'}}";
    private static final String ADD_FIELDS = "{$addFields:{value:'constant'}}";
    private static final String SET = "{$set:{value:'constant'}}";

    @Test
    public void everyAuditedOverloadAppendsOnceToSameReceiver() {
        List<StageCase> cases = cases();
        Assert.assertEquals(47, cases.size());
        for (StageCase stage : cases) {
            Aggregate<?> receiver = new AggregateWrapper();
            Aggregate<?> sibling = new AggregateWrapper();
            receiver.limit(1);
            Assert.assertSame(stage.signature, receiver, stage.call.apply(receiver));
            Assert.assertEquals(stage.signature, 2, receiver.getAggregateConditionList().size());
            Assert.assertEquals(stage.signature, BsonDocument.parse("{$limit:1}"), bson(receiver, 0));
            Assert.assertEquals(stage.signature, stage.expected, bson(receiver, 1));
            Assert.assertTrue(stage.signature, sibling.getAggregateConditionList().isEmpty());
        }
    }

    @Test
    public void facetPreservesEveryEffectAndIndependentBranchOrder() {
        for (StageCase stage : cases()) {
            Aggregate<?> inner = inner(stage);
            Aggregate<?> sibling = new AggregateWrapper();
            sibling.count("otherCount");
            Aggregate<?> outer = new AggregateWrapper();
            Assert.assertSame(outer, outer.facet(new Facet("rows", inner), new Facet("other", sibling)));
            BsonDocument body = new BsonDocument("rows", pipeline(stage))
                    .append("other", new BsonArray(Collections.singletonList(BsonDocument.parse("{$count:'otherCount'}"))));
            Assert.assertEquals(stage.signature, new BsonDocument("$facet", body), single(outer));
            Assert.assertEquals(Arrays.asList("rows", "other"),
                    Arrays.asList(single(outer).getDocument("$facet").keySet().toArray()));
            Assert.assertEquals(3, inner.getAggregateConditionList().size());
            Assert.assertEquals(1, sibling.getAggregateConditionList().size());
            Assert.assertNotSame(inner.getAggregateConditionList(), sibling.getAggregateConditionList());
        }
    }

    @Test
    public void lookupPreservesEveryEffectInCallOrder() {
        for (StageCase stage : cases()) {
            Aggregate<?> inner = inner(stage);
            Aggregate<?> outer = new AggregateWrapper();
            Assert.assertSame(outer, outer.lookup("orders", inner, "rows"));
            BsonDocument body = new BsonDocument("from", new BsonString("orders"))
                    .append("pipeline", pipeline(stage)).append("as", new BsonString("rows"));
            Assert.assertEquals(stage.signature, new BsonDocument("$lookup", body), single(outer));
            Assert.assertEquals(3, inner.getAggregateConditionList().size());
            Assert.assertNotSame(inner.getAggregateConditionList(), outer.getAggregateConditionList());
        }
    }

    @Test
    public void unionWithPreservesEveryEffectInCallOrder() {
        for (StageCase stage : cases()) {
            Aggregate<?> inner = inner(stage);
            Aggregate<?> outer = new AggregateWrapper();
            Assert.assertSame(outer, outer.unionWith("history", inner));
            BsonDocument body = new BsonDocument("coll", new BsonString("history")).append("pipeline", pipeline(stage));
            Assert.assertEquals(stage.signature, new BsonDocument("$unionWith", body), single(outer));
            Assert.assertEquals(3, inner.getAggregateConditionList().size());
            Assert.assertNotSame(inner.getAggregateConditionList(), outer.getAggregateConditionList());
        }
    }

    @Test
    public void failedLongSkipDoesNotAppendOrChangePreviousStage() {
        Aggregate<?> receiver = new AggregateWrapper();
        receiver.count("before");
        try {
            receiver.skip((long) Integer.MAX_VALUE + 1);
            Assert.fail("Math.toIntExact 必须保留现有溢出异常");
        } catch (ArithmeticException expected) {
            Assert.assertEquals(BsonDocument.parse("{$count:'before'}"), single(receiver));
        }
    }

    private static Aggregate<?> inner(StageCase stage) {
        Aggregate<?> receiver = new AggregateWrapper();
        receiver.limit(1);
        Assert.assertSame(receiver, stage.call.apply(receiver));
        receiver.limit(2);
        return receiver;
    }

    private static BsonArray pipeline(StageCase stage) {
        return new BsonArray(Arrays.asList(BsonDocument.parse("{$limit:1}"), stage.expected,
                BsonDocument.parse("{$limit:2}")));
    }

    private static BsonDocument single(Aggregate<?> receiver) {
        Assert.assertEquals(1, receiver.getAggregateConditionList().size());
        return bson(receiver, 0);
    }

    private static BsonDocument bson(Aggregate<?> receiver, int offset) {
        return receiver.getAggregateConditionList().get(offset)
                .toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
    }

    private static UnwindOption options() {
        return new UnwindOption().preserveNullAndEmptyArrays(true).includeArrayIndex("idx");
    }

    private static List<StageCase> cases() {
        return Arrays.asList(
                stage("project(Bson bson)", a -> a.project(new Document("value", 1)), INCLUDED),
                stage("projectDisplay(SFunction<T, R>... column)", a -> a.projectDisplay(Input::getValue), INCLUDED),
                stage("projectDisplay(String... column)", a -> a.projectDisplay("value"), INCLUDED),
                stage("projectNone(SFunction<T, R>... column)", a -> a.projectNone(Input::getValue), EXCLUDED),
                stage("projectNone(String... column)", a -> a.projectNone("value"), EXCLUDED),
                stage("project(boolean displayId, Projection... projection)",
                        a -> a.project(false, new Projection("value", 1)), INCLUDED_NO_ID),
                stage("project(Projection... projection)", a -> a.project(new Projection("value", 1)), INCLUDED),
                stage("project(boolean displayId, Collection<? extends Projection> projection)",
                        a -> a.project(false, Collections.singletonList(new Projection("value", 1))), INCLUDED_NO_ID),
                stage("project(Collection<? extends Projection> projection)",
                        a -> a.project(Collections.singletonList(new Projection("value", 1))), INCLUDED),
                stage("projectDisplay(boolean displayId, SFunction<T, R>... column)",
                        a -> a.projectDisplay(false, Input::getValue), INCLUDED_NO_ID),
                stage("projectDisplay(boolean displayId, String... column)",
                        a -> a.projectDisplay(false, "value"), INCLUDED_NO_ID),
                stage("projectNone(boolean displayId, SFunction<T, R>... column)",
                        a -> a.projectNone(false, Input::getValue), EXCLUDED_NO_ID),
                stage("projectNone(boolean displayId, String... column)",
                        a -> a.projectNone(false, "value"), EXCLUDED_NO_ID),
                stage("group(String _id)", a -> a.group("$value"), GROUP),
                stage("group(SFunction<T, ?> _id)", a -> a.group(Input::getValue), GROUP),
                stage("group(TExpression id, BsonField... fieldAccumulators)",
                        a -> a.group("$value", Accumulators.sum("total", 1)), GROUP_SUM),
                stage("group(SFunction<T, ?> id, BsonField... fieldAccumulators)",
                        a -> a.group(Input::getValue, Accumulators.sum("total", 1)), GROUP_SUM),
                stage("group(TExpression id, List<BsonField> fieldAccumulators)",
                        a -> a.group("$value", Collections.singletonList(Accumulators.sum("total", 1))), GROUP_SUM),
                stage("group(SFunction<T, ?> id, List<BsonField> fieldAccumulators)",
                        a -> a.group(Input::getValue, Collections.singletonList(Accumulators.sum("total", 1))), GROUP_SUM),
                stage("count()", Aggregate::count, "{$count:'count'}"),
                stage("count(String field)", a -> a.count("value"), "{$count:'value'}"),
                stage("count(SFunction<T, ?> field)", a -> a.count(Input::getValue), "{$count:'value'}"),
                stage("unwind(String fieldName)", a -> a.unwind("$value"), UNWIND),
                stage("unwind(SFunction<T, ?> fieldName)", a -> a.unwind(Input::getValue), UNWIND),
                stage("unwind(String fieldName, UnwindOption unwindOption)",
                        a -> a.unwind("$value", options()), UNWIND_OPTIONS),
                stage("unwind(SFunction<T, ?> fieldName, UnwindOption unwindOption)",
                        a -> a.unwind(Input::getValue, options()), UNWIND_OPTIONS),
                stage("addFields(String field, String value)", a -> a.addFields("value", "constant"), ADD_FIELDS),
                stage("addFields(SFunction<T, ?> field, String value)",
                        a -> a.addFields(Input::getValue, "constant"), ADD_FIELDS),
                stage("addFields(String value, SFunction<T, ?>... field)",
                        a -> a.addFields("constant", Input::getValue), ADD_FIELDS),
                stage("addFields(Field<?>... fields)", a -> a.addFields(Field.of("value", "constant")), ADD_FIELDS),
                stage("addFields(List<Field<?>> fields)",
                        a -> a.addFields(Collections.<Field<?>>singletonList(Field.of("value", "constant"))), ADD_FIELDS),
                stage("addFields(Bson bson)", a -> a.addFields(new Document("value", "constant")), ADD_FIELDS),
                stage("set(String field, String value)", a -> a.set("value", "constant"), SET),
                stage("set(SFunction<T, ?> field, String value)", a -> a.set(Input::getValue, "constant"), SET),
                stage("set(String value, SFunction<T, ?>... field)", a -> a.set("constant", Input::getValue), SET),
                stage("set(Field<?>... fields)", a -> a.set(Field.of("value", "constant")), SET),
                stage("set(List<Field<?>> fields)",
                        a -> a.set(Collections.<Field<?>>singletonList(Field.of("value", "constant"))), SET),
                stage("set(Bson bson)", a -> a.set(new Document("value", "constant")), SET),
                stage("skip(long skip)", a -> a.skip(2L), "{$skip:2}"),
                stage("skip(int skip)", a -> a.skip(2), "{$skip:2}"),
                stage("sample(Number size)", a -> a.sample(2), "{$sample:{size:2}}"),
                stage("replaceRoot(TExpression fieldName)", a -> a.replaceRoot("$value"),
                        "{$replaceRoot:{newRoot:'$value'}}"),
                stage("replaceRoot(Document value)", a -> a.replaceRoot(new Document("value", 1)),
                        "{$replaceRoot:{newRoot:{value:1}}}"),
                stage("replaceRoot(SFunction<T, ?> fieldName)", a -> a.replaceRoot(Input::getValue),
                        "{$replaceRoot:{newRoot:'$value'}}"),
                stage("replaceWith(TExpression fieldName)", a -> a.replaceWith("$value"), "{$replaceWith:'$value'}"),
                stage("replaceWith(Document value)", a -> a.replaceWith(new Document("value", 1)),
                        "{$replaceWith:{value:1}}"),
                stage("replaceWith(SFunction<T, ?> fieldName)", a -> a.replaceWith(Input::getValue),
                        "{$replaceWith:'$value'}"));
    }

    private static StageCase stage(String signature, Function<Aggregate<?>, Object> call, String expected) {
        return new StageCase(signature, call, BsonDocument.parse(expected));
    }

    private static final class StageCase {
        private final String signature;
        private final Function<Aggregate<?>, Object> call;
        private final BsonDocument expected;

        private StageCase(String signature, Function<Aggregate<?>, Object> call, BsonDocument expected) {
            this.signature = signature;
            this.call = call;
            this.expected = expected;
        }
    }

    /** 实际方法引用经过 Core 的 Lambda 字段解析，避免夹具直接伪造解析结果。 */
    public static class Input {
        private String value;

        public String getValue() { return value; }
    }
}
