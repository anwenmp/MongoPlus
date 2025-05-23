package com.mongoplus.conditions.query;

import com.mongoplus.model.PageParam;
import com.mongoplus.model.PageResult;

import java.util.List;

/**
 * 查询方法定义
 * @author JiaChaoYang
*/
public interface ChainQuery<T> {

    /**
     * 获取列表 返回T类型的List
     * @return {@link List<T>}
     * @author JiaChaoYang
    */
    List<T> list();

    /**
     * 获取列表，返回R类型的List
     * @param rClazz 返回的类型
     * @return {@link List<R>}
     * @author anwen
     */
    <R> List<R> list(Class<R> rClazz);

    /**
     * 获取单个，返回T类型的对象
     * @return T
     * @author JiaChaoYang
    */
    T one();

    /**
     * 获取单个，返回R类型的对象
     * @param rClazz 返回类型
     * @return R
     * @author JiaChaoYang
     */
    <R> R one(Class<R> rClazz);

    /**
     * 分页
     * @param pageParam 分页参数对象
     * @return {@link PageResult<T>}
     * @author JiaChaoYang
    */
    PageResult<T> page(PageParam pageParam);

    /**
     * 分页
     * @param pageParam 分页参数对象
     * @param rClazz 返回类型
     * @return {@link PageResult<R>}
     * @author JiaChaoYang
     */
    <R> PageResult<R> page(PageParam pageParam,Class<R> rClazz);

    /**
     * 分页
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @return {@link PageResult<T>}
     * @author JiaChaoYang
    */
    PageResult<T> page(Integer pageNum, Integer pageSize);

    /**
     * 分页
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param rClazz 返回类型
     * @return {@link PageResult<R>}
     * @author JiaChaoYang
     */
    <R> PageResult<R> page(Integer pageNum, Integer pageSize, Class<R> rClazz);

    /**
     * 分页
     * @param pageParam 分页参数对象
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @return {@link PageResult<T>}
     * @author JiaChaoYang
    */
    PageResult<T> page(PageParam pageParam, Integer recentPageNum);

    /**
     * 分页
     * @param pageParam 分页参数对象
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @param rClazz 返回类型
     * @return {@link PageResult<R>}
     * @author JiaChaoYang
     */
    <R> PageResult<R> page(PageParam pageParam, Integer recentPageNum,Class<R> rClazz);

    /**
     * 分页
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @return {@link PageResult<T>}
     * @author JiaChaoYang
    */
    PageResult<T> page(Integer pageNum, Integer pageSize, Integer recentPageNum);

    /**
     * 分页
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @param rClazz 返回类型
     * @return {@link PageResult<R>}
     * @author JiaChaoYang
     */
    <R> PageResult<R> page(Integer pageNum, Integer pageSize, Integer recentPageNum,Class<R> rClazz);

    long count();
}
