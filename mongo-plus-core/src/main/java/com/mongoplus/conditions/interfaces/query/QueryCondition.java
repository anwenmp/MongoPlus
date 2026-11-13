package com.mongoplus.conditions.interfaces.query;

import com.mongodb.BasicDBObject;
import com.mongoplus.bson.MongoPlusBasicDBObject;
import com.mongoplus.conditions.query.NestedQueryCondition;
import com.mongoplus.conditions.interfaces.query.array.Array;
import com.mongoplus.conditions.interfaces.query.bitwise.Bitwise;
import com.mongoplus.conditions.interfaces.query.compare.Compare;
import com.mongoplus.conditions.interfaces.query.data.DataType;
import com.mongoplus.conditions.interfaces.query.geo.GeoSpatial;
import com.mongoplus.conditions.interfaces.query.logic.Logic;
import com.mongoplus.conditions.interfaces.query.other.Other;
import com.mongoplus.conditions.interfaces.query.text.Text;
import com.mongoplus.support.SFunction;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.function.Consumer;

/**
 * 查询条件顶级接口
 *
 * @author anwen
 */
public interface QueryCondition<T, Children> extends
        Array<T, Children>,
        Bitwise<T, Children>,
        Compare<T, Children>,
        DataType<T, Children>,
        GeoSpatial<T, Children>,
        Logic<T, Children>,
        Text<T, Children>,
        Other<T, Children> {

    /**
     * 自定义语句
     * @param basicDBObject bson对象
     * @return {@link Children}
     * @author anwen
     */
    Children custom(BasicDBObject basicDBObject);

    /**
     * 自定义语句
     * @param bson bson对象
     * @return {@link Children}
     * @author anwen
     */
    Children custom(Bson bson);

    /**
     * 自定义语句
     * @param mongoPlusBasicDBObject bson对象
     * @return {@link Children}
     * @author anwen
     */
    Children custom(MongoPlusBasicDBObject mongoPlusBasicDBObject);

    /**
     * 自定义语句
     * @param function bson对象
     * @return {@link Children}
     * @author anwen
     */
    Children custom(SFunction<MongoPlusBasicDBObject,MongoPlusBasicDBObject> function);

    /**
     * 自定义语句
     * @param basicDBObjectList bson对象
     * @return {@link Children}
     * @author anwen
     */
    Children custom(List<BasicDBObject> basicDBObjectList);

    /**
     * 嵌套文档条件
     * <p>第一个参数指定嵌套字段，后续条件都写在该嵌套文档内，字段会自动补上点路径前缀，
     * 不用再手写 {@code "social_info.agent_id"}：</p>
     * <pre>{@code
     * lambdaQuery().eq(TokenInfo::getTokenStatus, 1)
     *              .nested(TokenInfo::getSocialInfo, nested -> nested.eq(SocialInfo::getAgentId, agentId))
     *              .list();
     * // 生成 {"token_status": {"$eq": 1}, "social_info.agent_id": {"$eq": agentId}}
     * }</pre>
     * <p>嵌套里可以继续调 {@code nested} 拼多层路径；嵌套内不支持 custom，需要自定义 json 时放在外层包装器上</p>
     *
     * @param root 嵌套字段，例如 {@code TokenInfo::getSocialInfo}
     * @param consumer 嵌套文档内的条件
     * @return {@link Children}
     */
    default <R> Children nested(SFunction<T, R> root, Consumer<QueryCondition<R, NestedQueryCondition<R>>> consumer) {
        consumer.accept(new NestedQueryCondition<>(this, columnPrefix(root)));
        return typeThis();
    }

    /**
     * 嵌套条件的字段前缀，默认就是嵌套字段本身，多层嵌套时由嵌套条件继续累加
     *
     * @param root 嵌套字段
     * @return 字段前缀
     */
    default <R> String columnPrefix(SFunction<T, R> root) {
        return root.getFieldNameLine();
    }

}
