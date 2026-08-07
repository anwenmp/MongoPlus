package com.mongoplus.business;

import org.bson.Document;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * 验证乐观锁对更新 BSON 的改写。
 */
public class OptimisticLockerInterceptorTest {

    @Test
    public void updateShouldPreserveUserIncrementWhenAddingVersionIncrement() {
        OptimisticLockerInterceptor interceptor = new OptimisticLockerInterceptor();
        Document filter = new Document("_id", 1);
        Document update = new Document("$set", new Document("version", 2).append("name", "MongoPlus"))
                .append("$inc", new Document("count", 3))
                .append("$unset", new Document("obsolete", 1))
                .append("$push", new Document("history", "updated"));

        interceptor.updateParamHandler("version", filter, update, false);

        Document increments = update.get("$inc", Document.class);
        assertEquals(Integer.valueOf(3), increments.getInteger("count"));
        assertEquals(Integer.valueOf(1), increments.getInteger("version"));
        assertEquals(Integer.valueOf(2), filter.getInteger("version"));
        assertEquals("MongoPlus", update.get("$set", Document.class).getString("name"));
        assertFalse(update.get("$set", Document.class).containsKey("version"));
        assertEquals(Integer.valueOf(1), update.get("$unset", Document.class).getInteger("obsolete"));
        assertEquals("updated", update.get("$push", Document.class).getString("history"));
    }

    /**
     * 验证不存在用户 $inc 时创建仅含版本字段的 $inc。
     */
    @Test
    public void updateShouldCreateVersionIncrementWhenUserIncrementIsAbsent() {
        OptimisticLockerInterceptor interceptor = new OptimisticLockerInterceptor();
        Document filter = new Document();
        Document update = new Document("$set", new Document("version", 2));

        interceptor.updateParamHandler("version", filter, update, false);

        assertEquals(Integer.valueOf(1), update.get("$inc", Document.class).getInteger("version"));
    }

    /**
     * 验证乐观锁自增覆盖用户指定的版本自增，但保留其余自增字段。
     */
    @Test
    public void updateShouldOverrideUserVersionIncrementButPreserveOtherIncrements() {
        OptimisticLockerInterceptor interceptor = new OptimisticLockerInterceptor();
        interceptor.setAutoInc(2);
        Document filter = new Document();
        Document update = new Document("$set", new Document("version", 5))
                .append("$inc", new Document("version", 100).append("count", 3));

        interceptor.updateParamHandler("version", filter, update, false);

        Document increments = update.get("$inc", Document.class);
        assertEquals(Integer.valueOf(2), increments.getInteger("version"));
        assertEquals(Integer.valueOf(3), increments.getInteger("count"));
    }

    /**
     * 验证缺少 $set.version 时保持现有跳过语义。
     */
    @Test
    public void updateShouldNotChangeBsonWhenOriginalVersionIsAbsent() {
        OptimisticLockerInterceptor interceptor = new OptimisticLockerInterceptor();
        Document filter = new Document("_id", 1);
        Document update = new Document("$set", new Document("name", "MongoPlus"))
                .append("$inc", new Document("count", 3));

        interceptor.updateParamHandler("version", filter, update, false);

        assertFalse(filter.containsKey("version"));
        assertFalse(update.get("$inc", Document.class).containsKey("version"));
        assertEquals(Integer.valueOf(3), update.get("$inc", Document.class).getInteger("count"));
    }
}
