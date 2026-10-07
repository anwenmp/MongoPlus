package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.Variable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.BsonDocument;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

/** 有序 document entries 使用真实 Driver 泛型构造器，验证精确 List 目标及 BSON 顺序。 */
public class LookupVariableEntriesTest {
    @Test
    public void singleEntry() {
        verify(Document.parse("{userId:'$userId'}"), Arrays.asList("userId"));
    }

    @Test
    public void multipleEntriesPreserveInputOrder() {
        verify(Document.parse("{userId:'$userId',orderId:'$_id'}"), Arrays.asList("userId", "orderId"));
    }

    private static void verify(Document entries, List<String> order) {
        List<Variable<String>> variables = new ArrayList<Variable<String>>(entries.size());
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            variables.add(new Variable<String>(entry.getKey(), (String) entry.getValue()));
        }
        Assert.assertEquals(order, variables.stream().map(Variable::getName).collect(Collectors.toList()));
        for (int i = 0; i < order.size(); i++) {
            Assert.assertEquals(entries.getString(order.get(i)), variables.get(i).getValue());
        }
        Aggregate<?> inner = new AggregateWrapper();
        inner.limit(1);
        Aggregate<?> outer = new AggregateWrapper();
        Assert.assertSame(outer, outer.lookup("users", variables, inner, "userInfo"));
        Assert.assertEquals(1, outer.getAggregateConditionList().size());
        Assert.assertEquals(1, inner.getAggregateConditionList().size());
        BsonDocument actual = outer.getAggregateCondition().toBsonDocument(BsonDocument.class,
                MongoClientSettings.getDefaultCodecRegistry());
        BsonDocument expected = BsonDocument.parse("{$lookup:{from:'users',let:" + entries.toJson()
                + ",pipeline:[{$limit:1}],as:'userInfo'}}");
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(order, new ArrayList<String>(actual.getDocument("$lookup").getDocument("let").keySet()));
    }
}
