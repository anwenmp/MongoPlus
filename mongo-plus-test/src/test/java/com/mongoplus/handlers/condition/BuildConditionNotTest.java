package com.mongoplus.handlers.condition;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.UpdateWrapper;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BuildConditionNotTest {

    @Test
    public void notKeepsEveryConditionFromNestedWrapper() {
        BsonDocument condition = bson(new QueryWrapper<Object>()
                .not(wrapper -> wrapper.eq("age", 18).eq("enabled", true))
                .buildCondition()
                .getCondition());

        BsonArray conditions = condition.getArray("$nor");
        assertEquals(1, conditions.size());
        BsonDocument nestedCondition = conditions.get(0).asDocument();
        assertEquals(2, nestedCondition.size());
        assertEquals(18, nestedCondition.getDocument("age").getInt32("$eq").getValue());
        assertTrue(nestedCondition.getDocument("enabled").getBoolean("$eq").getValue());
    }

    @Test
    public void notKeepsSingleFieldBehavior() {
        BsonDocument condition = bson(new QueryWrapper<Object>()
                .not(wrapper -> wrapper.gt("age", 18))
                .buildCondition()
                .getCondition());

        assertEquals(18, condition.getDocument("age")
                .getDocument("$not").getInt32("$gt").getValue());
    }

    @Test
    public void exprRemainsOnItsExistingBranch() {
        BsonDocument condition = bson(new QueryWrapper<Object>()
                .expr(wrapper -> wrapper.eq("age", 18).eq("enabled", true))
                .buildCondition()
                .getCondition());

        BsonDocument expression = condition.getDocument("$expr");
        assertEquals(1, expression.size());
        assertTrue(expression.containsKey("age"));
    }

    @Test
    public void updateWrapperUsesCompleteNotFilter() {
        BsonDocument condition = bson(new UpdateWrapper<Object>()
                .not(wrapper -> wrapper.eq("age", 18).eq("enabled", true))
                .buildUpdateCondition()
                .getLeft());

        BsonArray conditions = condition.getArray("$nor");
        assertEquals(1, conditions.size());
        assertEquals(2, conditions.get(0).asDocument().size());
    }

    @Test
    public void falseConditionDoesNotInvokeNestedWrapper() {
        AtomicBoolean invoked = new AtomicBoolean();
        BasicDBObject condition = new QueryWrapper<Object>()
                .not(false, wrapper -> {
                    invoked.set(true);
                    return wrapper.eq("age", 18);
                })
                .buildCondition()
                .getCondition();

        assertFalse(invoked.get());
        assertTrue(condition.isEmpty());
    }

    @Test
    public void emptyNotDoesNotAddAFilter() {
        BasicDBObject condition = new QueryWrapper<Object>()
                .not(wrapper -> wrapper)
                .buildCondition()
                .getCondition();

        assertTrue(condition.isEmpty());
    }

    private static BsonDocument bson(BasicDBObject condition) {
        return BsonDocument.parse(condition.toJson());
    }
}
