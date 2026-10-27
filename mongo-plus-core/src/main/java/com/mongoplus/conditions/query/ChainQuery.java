package com.mongoplus.conditions.query;

import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongoplus.model.PageParam;
import com.mongoplus.model.PageResult;

import java.util.List;
import java.util.function.Consumer;

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
     * 按当前条件、排序和投影，以原子方式查找并物理删除一个文档。
     * 空条件匹配集合中的任意文档；未指定排序时不保证选中文档的顺序。
     * @return 删除前的文档，未匹配时返回 null
     */
    T findOneAndRemove();

    /**
     * 以原子方式查找并物理删除一个文档，映射为指定类型。
     * @param rClazz 返回类型，不影响查询集合的选择
     * @return 删除前的文档，未匹配时返回 null
     */
    <R> R findOneAndRemove(Class<R> rClazz);

    /**
     * 原子查找并物理删除一个文档，在 Wrapper 选项基础上进行配置。
     * @param optionsConfigurer 选项配置，后设置的投影和排序覆盖 Wrapper 配置；null 表示不追加配置
     * @return 删除前的文档，未匹配时返回 null
     */
    T findOneAndRemove(Consumer<FindOneAndDeleteOptions> optionsConfigurer);

    /**
     * 原子查找并物理删除一个文档，配置选项并映射为指定类型。
     * @param optionsConfigurer 选项配置，后设置的投影和排序覆盖 Wrapper 配置；null 表示不追加配置
     * @param rClazz 返回类型，不影响查询集合的选择
     * @return 删除前的文档，未匹配时返回 null
     */
    <R> R findOneAndRemove(Consumer<FindOneAndDeleteOptions> optionsConfigurer, Class<R> rClazz);

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
