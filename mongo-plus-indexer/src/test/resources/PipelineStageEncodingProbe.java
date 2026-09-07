import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.WindowOutputFields;
import com.mongoplus.aggregate.AggregateWrapper;
import com.mongoplus.aggregate.pipeline.Accumulators;
import com.mongoplus.aggregate.pipeline.Projections;
import com.mongoplus.aggregate.pipeline.UnwindOption;
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.conditions.operation.ConditionOperators;
import com.mongoplus.support.SFunction;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** 独立编译到 target 的 Core/Driver 编码回归，不为 Indexer 引入运行时依赖。 */
public final class PipelineStageEncodingProbe {
    @CollectionName("stored_orders")
    public static class Row {
        private String orders;
        private String code;
        public String getOrders() { return orders; }
        public String getCode() { return code; }
    }

    public static void main(String[] args) {
        pipeline("group + accumulators", new AggregateWrapper().group("$userId",
                        Accumulators.sum("total", "$amount"), Accumulators.avg("average", "$amount")),
                "{$group:{_id:'$userId',total:{$sum:'$amount'},average:{$avg:'$amount'}}}");
        pipeline("ifNull -> multiply -> project", new AggregateWrapper().project(Projections.computed("total",
                        ConditionOperators.multiply(ConditionOperators.ifNull("$price", 0), "$quantity"))),
                "{$project:{total:{$multiply:[{$ifNull:['$price',0]},'$quantity']}}}");
        pipeline("cond -> project", new AggregateWrapper().project(Projections.computed("label",
                        ConditionOperators.cond(new Document("$gte", Arrays.asList("$amount", 100)), "high", "low"))),
                "{$project:{label:{$cond:{if:{$gte:['$amount',100]},then:'high',else:'low'}}}}");
        pipeline("dateToString -> project", new AggregateWrapper().project(Projections.computed("day",
                        ConditionOperators.dateToString("%Y-%m-%d", "$createdAt", "UTC", "missing"))),
                "{$project:{day:{$dateToString:{date:'$createdAt',format:'%Y-%m-%d',timezone:'UTC',onNull:'missing'}}}}");
        pipeline("lookup -> unwind -> group -> sort", new AggregateWrapper()
                        .lookup("orders", "userId", "ownerId", "orders").unwind("$orders")
                        .group("$userId", Accumulators.sum("total", "$orders.amount")).sort("total", -1),
                "{$lookup:{from:'orders',localField:'userId',foreignField:'ownerId',as:'orders'}}",
                "{$unwind:'$orders'}", "{$group:{_id:'$userId',total:{$sum:'$orders.amount'}}}", "{$sort:{total:-1}}");

        SFunction<Row, String> orders = Row::getOrders;
        SFunction<Row, String> code = Row::getCode;
        // 核对输入表示和原样传递；非法服务端表示也不能被 Core 悄悄修复。
        for (String value : Arrays.asList("$orders", "orders", "$$orders")) {
            equal(new AggregateWrapper().unwind(value).getAggregateCondition(), new Document("$unwind", value));
            equal(new AggregateWrapper().unwind(value, new UnwindOption().includeArrayIndex("position"))
                    .getAggregateCondition(), new Document("$unwind", new Document("path", value).append("includeArrayIndex", "position")));
        }
        equal(new AggregateWrapper().unwind(orders).getAggregateCondition(), Document.parse("{$unwind:'$orders'}"));
        equal(new AggregateWrapper().unwind(orders, new UnwindOption().includeArrayIndex(code).preserveNullAndEmptyArrays(true))
                .getAggregateCondition(), Document.parse("{$unwind:{path:'$orders',includeArrayIndex:'code',preserveNullAndEmptyArrays:true}}"));
        equal(new AggregateWrapper().lookup(Row.class, orders, code, orders).getAggregateCondition(),
                Document.parse("{$lookup:{from:'stored_orders',localField:'orders',foreignField:'code',as:'orders'}}"));
        equal(new AggregateWrapper().graphLookup("orders", orders, code, orders, "result").getAggregateCondition(),
                Document.parse("{$graphLookup:{from:'orders',startWith:'$orders',connectFromField:'code',connectToField:'orders',as:'result'}}"));
        equal(new AggregateWrapper().addFields(orders, "$code").getAggregateCondition(), Document.parse("{$addFields:{orders:'$code'}}"));
        equal(new AggregateWrapper().addFields("literal", orders, code).getAggregateCondition(), Document.parse("{$addFields:{'orders.code':'literal'}}"));
        equal(new AggregateWrapper().set("literal", orders, code).getAggregateCondition(), Document.parse("{$set:{'orders.code':'literal'}}"));
        equal(new AggregateWrapper().bucket((Object) "$orders", Arrays.asList(0, 10)).getAggregateCondition(),
                Document.parse("{$bucket:{groupBy:'$orders',boundaries:[0,10]}}"));
        equal(new AggregateWrapper().bucket(orders, Arrays.asList("$a", "$z")).getAggregateCondition(),
                Document.parse("{$bucket:{groupBy:'$orders',boundaries:['$a','$z']}}"));
        equal(new AggregateWrapper().bucketAuto((Object) "$orders", 2).getAggregateCondition(),
                Document.parse("{$bucketAuto:{groupBy:'$orders',buckets:2}}"));
        equal(new AggregateWrapper().group(orders).getAggregateCondition(), Document.parse("{$group:{_id:'$orders'}}"));
        equal(new AggregateWrapper().group((SFunction<Row, ?>) null, Accumulators.sum("n", 1)).getAggregateCondition(),
                Document.parse("{$group:{_id:null,n:{$sum:1}}}"));
        equal(new AggregateWrapper().replaceRoot(orders).getAggregateCondition(), Document.parse("{$replaceRoot:{newRoot:'$orders'}}"));
        equal(new AggregateWrapper().replaceRoot(new Document("code", "$code")).getAggregateCondition(),
                Document.parse("{$replaceRoot:{newRoot:{code:'$code'}}}"));
        equal(new AggregateWrapper().replaceWith(new Document("code", "$code")).getAggregateCondition(),
                Document.parse("{$replaceWith:{code:'$code'}}"));
        equal(new AggregateWrapper().sortByCount(orders).getAggregateCondition(), Document.parse("{$sortByCount:'$orders'}"));
        equal(new AggregateWrapper().sortByCount("orders").getAggregateCondition(), Document.parse("{$sortByCount:'orders'}"));
        equal(new AggregateWrapper().projectDisplay(orders, code).getAggregateCondition(), Document.parse("{$project:{orders:1,code:1}}"));
        equal(new AggregateWrapper().unset(orders).getAggregateCondition(), Document.parse("{$unset:'orders'}"));
        equal(new AggregateWrapper().unset(orders, code).getAggregateCondition(), Document.parse("{$unset:['orders','code']}"));
        equal(new AggregateWrapper().count(code).getAggregateCondition(), Document.parse("{$count:'code'}"));
        AggregateWrapper child = new AggregateWrapper().limit(2);
        equal(new AggregateWrapper().facet("result", child).getAggregateCondition(), Document.parse("{$facet:{result:[{$limit:2}]}}"));
        equal(new AggregateWrapper().facet("result", child.getAggregateCondition()).getAggregateCondition(),
                Document.parse("{$facet:{result:[{$limit:2}]}}"));
        equal(new AggregateWrapper().unionWith(Row.class, child).getAggregateCondition(),
                Document.parse("{$unionWith:{coll:'stored_orders',pipeline:[{$limit:2}]}}"));
        equal(new AggregateWrapper().lookup("orders", child, "result").getAggregateCondition(),
                Document.parse("{$lookup:{from:'orders',pipeline:[{$limit:2}],as:'result'}}"));
        equal(new AggregateWrapper().out("archive", "orders").getAggregateCondition(), Document.parse("{$out:{db:'archive',coll:'orders'}}"));
        equal(new AggregateWrapper().merge(Row.class).getAggregateCondition(), Document.parse("{$merge:{into:'stored_orders'}}"));
        equal(new AggregateWrapper().setWindowFields("$code", new Document("orders", 1),
                WindowOutputFields.sum("total", "$orders", null)).getAggregateCondition(),
                Document.parse("{$setWindowFields:{partitionBy:'$code',sortBy:{orders:1},output:{total:{$sum:'$orders'}}}}"));
        // 证明 Bson body 不会被当作完整 Stage 自动去壳。
        equal(new AggregateWrapper().project(new Document("$project", new Document("code", 1))).getAggregateCondition(),
                Document.parse("{$project:{$project:{code:1}}}"));
        equal(new AggregateWrapper().setWindowFields("$code", new Document("$sort", new Document("orders", 1)),
                Collections.singletonList(WindowOutputFields.sum("total", "$orders", null))).getAggregateCondition(),
                Document.parse("{$setWindowFields:{partitionBy:'$code',sortBy:{$sort:{orders:1}},output:{total:{$sum:'$orders'}}}}"));
        System.out.println("Stage representation encoding PASS: String/getter/Class/pipeline/body/options; no database used");
    }

    private static void pipeline(String label, AggregateWrapper wrapper, String... expected) {
        List<Bson> stages = wrapper.getAggregateConditionList();
        if (stages.size() != expected.length) { throw new AssertionError(label + " Stage count"); }
        for (int i = 0; i < expected.length; i++) { equal(stages.get(i), BsonDocument.parse(expected[i])); }
        System.out.println(label + ": BSON PASS");
    }

    private static void equal(Bson actual, Bson expected) {
        BsonDocument left = actual.toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
        BsonDocument right = expected.toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
        if (!left.equals(right)) { throw new AssertionError("Actual " + left + "; expected " + right); }
    }
}
