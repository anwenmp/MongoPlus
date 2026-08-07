package com.mongoplus.handlers.condition;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.UpdateWrapper;
import com.mongoplus.enums.RegexOptions;
import org.bson.BsonDocument;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BuildConditionRegexTest {

    /**
     * 验证 regex 显式传入的 RegexOptions 会进入最终 BSON，防止构建阶段再次硬编码为 i。
     */
    @Test
    public void regexUsesExplicitOption() {
        BasicDBObject condition = new QueryWrapper<Object>()
                .regex("name", "^mongo", RegexOptions.MULTILINE)
                .buildCondition()
                .getCondition();

        assertRegex(condition, "name", "^mongo", "m");
    }

    /**
     * 验证 like 的 Lambda 字段重载同样会传递 RegexOptions，覆盖字段解析与选项构建的组合路径。
     */
    @Test
    public void likeUsesExplicitOptionForLambdaColumn() {
        BasicDBObject condition = new QueryWrapper<User>()
                .like(User::getName, "mongo", RegexOptions.DOT_ALL)
                .buildCondition()
                .getCondition();

        assertRegex(condition, "name", "mongo", "s");
    }

    /**
     * 验证未显式指定选项时仍默认忽略大小写，确保修复不改变既有默认行为。
     */
    @Test
    public void regexKeepsCaseInsensitiveDefault() {
        BasicDBObject condition = new QueryWrapper<Object>()
                .regex("name", "mongo")
                .buildCondition()
                .getCondition();

        assertRegex(condition, "name", "mongo", "i");
    }

    /**
     * 验证显式传入 null 选项时继续回退到 i，保持修复前的兼容行为。
     */
    @Test
    public void nullOptionKeepsCaseInsensitiveCompatibility() {
        BasicDBObject condition = new QueryWrapper<Object>()
                .regex("name", "mongo", null)
                .buildCondition()
                .getCondition();

        assertRegex(condition, "name", "mongo", "i");
    }

    /**
     * 验证 UpdateWrapper 的查询过滤条件复用相同的 RegexOptions 构建逻辑。
     */
    @Test
    public void updateFilterUsesExplicitOption() {
        BasicDBObject condition = new UpdateWrapper<Object>()
                .regex("name", "mongo", RegexOptions.EXTENDED)
                .buildCondition()
                .getCondition();

        assertRegex(condition, "name", "mongo", "x");
    }

    /**
     * 验证 condition=false 时不会解析 Lambda 字段、校验 value 或添加查询条件。
     */
    @Test
    public void falseConditionDoesNotParseLambdaOrValidateValue() {
        BasicDBObject condition = new QueryWrapper<User>()
                .regex(false, user -> user, null, null)
                .buildCondition()
                .getCondition();

        assertTrue(condition.isEmpty());
    }

    /**
     * 固化 regex(null) 当前在 BSON 构建阶段抛出空指针异常的行为，避免误判为自动忽略条件。
     */
    @Test(expected = NullPointerException.class)
    public void regexNullStillFailsWhenBsonIsBuilt() {
        new QueryWrapper<Object>().regex("name", null).buildCondition();
    }

    /**
     * 固化 like(null) 当前在 BSON 构建阶段抛出空指针异常的行为，避免误判为自动忽略条件。
     */
    @Test(expected = NullPointerException.class)
    public void likeNullStillFailsWhenBsonIsBuilt() {
        new QueryWrapper<Object>().like("name", null).buildCondition();
    }

    private static void assertRegex(BasicDBObject condition, String field, String pattern, String options) {
        BsonDocument regex = (BsonDocument) condition.get(field);
        assertEquals(pattern, regex.getString("$regex").getValue());
        assertEquals(options, regex.getString("$options").getValue());
    }

    private static class User {
        private String name;

        public String getName() {
            return name;
        }
    }
}
