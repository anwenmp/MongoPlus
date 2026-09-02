package com.mongoplus.conditions.interfaces.query.logic;

import com.mongoplus.conditions.interfaces.query.logic.operations.And;
import com.mongoplus.conditions.interfaces.query.logic.operations.Nor;
import com.mongoplus.conditions.interfaces.query.logic.operations.Not;
import com.mongoplus.conditions.interfaces.query.logic.operations.Or;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;

/**
 * 逻辑条件
 *
 * @author anwen
 */
public interface Logic<T, Children> extends
        And<T, Children>,
        Nor<T, Children>,
        Not<T, Children>,
        Or<T, Children> {

    /**
     * 合并
     * <p>combine中的条件，将会存在同一个对象中，常用于or,and等逻辑操作符中</p>
     * <p>如构建or条件：{@code or(wrapper -> wrapper.eq(User::getUserName,"张三").like(User::getUserName,"1"))}</p>
     * <p>该操作将会构建语句为：{@code {or:[{userName:{"eq":"张三"}},{userName:{"like":"1"}}]}}</p>
     * <p>如使用{@link #combine}构建：{@code or(wrapper ->
     *     wrapper.custom(customWrapper ->
     *     customWrapper.eq(User::getUserName,"张三").like(User::getUserName,"1")))}</p>
     * <p>则对应语句为：{@code {or:[{userName:{"eq":"张三","like":"1"}}]}}</p>
     * <p style='color: red'>在使用{@link #combine}方法时，请保证{@code combine}中条件的字段名一致</p>
     * @mongoComposition LOGICAL_SAME_FIELD_CONDITIONS
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 需要合并的链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    default Children combine(boolean condition, SFunction<QueryWrapper<T>, QueryWrapper<T>> function) {
        return condition ? combine(function) : typeThis();
    }

    /**
     * 合并
     * <p>combine中的条件，将会存在同一个对象中，常用于or,and等逻辑操作符中</p>
     * <p>如构建or条件：{@code or(wrapper -> wrapper.eq(User::getUserName,"张三").like(User::getUserName,"1"))}</p>
     * <p>该操作将会构建语句为：{@code {or:[{userName:{"eq":"张三"}},{userName:{"like":"1"}}]}}</p>
     * <p>如使用{@link #combine}构建：{@code or(wrapper ->
     *     wrapper.custom(customWrapper ->
     *     customWrapper.eq(User::getUserName,"张三").like(User::getUserName,"1")))}</p>
     * <p>则对应语句为：{@code {or:[{userName:{"eq":"张三","like":"1"}}]}}</p>
     * <p style='color: red'>在使用{@link #combine}方法时，请保证{@code combine}中条件的字段名一致</p>
     * @mongoComposition LOGICAL_SAME_FIELD_CONDITIONS
     * @param function 需要合并的链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    default Children combine(SFunction<QueryWrapper<T>, QueryWrapper<T>> function) {
        return combine(function.apply(new QueryWrapper<>()));
    }

    /**
     * 合并
     * <p>combine中的条件，将会存在同一个对象中，常用于or,and等逻辑操作符中</p>
     * <p>如构建or条件：{@code or(new QueryWrapper<User>().eq(User::getUserName,"张三").like(User::getUserName,"1"))}</p>
     * <p>该操作将会构建语句为：{@code {or:[{userName:{"eq":"张三"}},{userName:{"like":"1"}}]}}</p>
     * <p>如使用{@link #combine}构建：{@code or(new QueryWrapper<User>()
     *     .custom(customWrapper ->
     *     customWrapper.eq(User::getUserName,"张三").like(User::getUserName,"1")))}</p>
     * <p>则对应语句为：{@code {or:[{userName:{"eq":"张三","like":"1"}}]}}</p>
     * <p style='color: red'>在使用{@link #combine}方法时，请保证{@code combine}中条件的字段名一致</p>
     * @mongoComposition LOGICAL_SAME_FIELD_CONDITIONS
     * @param queryWrapper 实体对象封装操作类 {@link com.mongoplus.conditions.query.QueryWrapper}
     * @return {@link Children}
     * @author anwen
     */
    default Children combine(Wrapper<?> queryWrapper) {
        return addCondition(getBaseCondition(queryWrapper));
    }

    /**
     * 合并
     * <p>combine中的条件，将会存在同一个对象中，常用于or,and等逻辑操作符中</p>
     * <p>如构建or条件：{@code or(new QueryWrapper<User>().eq(User::getUserName,"张三").like(User::getUserName,"1"))}</p>
     * <p>该操作将会构建语句为：{@code {or:[{userName:{"eq":"张三"}},{userName:{"like":"1"}}]}}</p>
     * <p>如使用{@link #combine}构建：{@code or(new QueryWrapper<User>()
     *     .custom(customWrapper ->
     *     customWrapper.eq(User::getUserName,"张三").like(User::getUserName,"1")))}</p>
     * <p>则对应语句为：{@code {or:[{userName:{"eq":"张三","like":"1"}}]}}</p>
     * <p style='color: red'>在使用{@link #combine}方法时，请保证{@code combine}中条件的字段名一致</p>
     * @mongoComposition LOGICAL_SAME_FIELD_CONDITIONS
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryWrapper 实体对象封装操作类 {@link com.mongoplus.conditions.query.QueryWrapper}
     * @return {@link Children}
     * @author anwen
     */
    default Children combine(boolean condition,Wrapper<?> queryWrapper) {
        return condition ? combine(queryWrapper) : typeThis();
    }

}
