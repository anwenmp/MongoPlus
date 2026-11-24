package com.mongoplus.conditions.interfaces.query;

import com.mongodb.BasicDBObject;
import com.mongoplus.bson.MongoPlusBasicDBObject;
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
     * <p>直接传入 json 字符串，内部按 {@link BasicDBObject#parse(String)} 解析成条件，
     * 适合把前端、配置或日志里已经拼好的查询条件原样带进来，
     * 加进来之后依旧可以继续排序、分页、查询总数</p>
     * <p>与链式条件合并时以 json 为准：同一个字段既在 json 里出现、又被 eq 等条件指定，最终取 json 里的值（custom 条件最后合并）</p>
     *
     * @param json json 格式的查询条件，例如 {@code {"age": {"$gt": 18}}}
     * @return {@link Children}
     */
    default Children custom(String json) {
        return custom(BasicDBObject.parse(json));
    }

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

}
