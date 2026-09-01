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
