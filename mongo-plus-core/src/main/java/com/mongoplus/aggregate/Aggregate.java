package com.mongoplus.aggregate;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoNamespace;
import com.mongodb.client.model.*;
import com.mongodb.client.model.Facet;
import com.mongodb.client.model.Variable;
import com.mongodb.client.model.densify.DensifyOptions;
import com.mongodb.client.model.densify.DensifyRange;
import com.mongodb.client.model.fill.FillOptions;
import com.mongodb.client.model.fill.FillOutputField;
import com.mongoplus.aggregate.pipeline.Project;
import com.mongoplus.aggregate.pipeline.UnwindOption;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.model.aggregate.Field;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Collection;
import java.util.List;

public interface Aggregate<Children> extends Project<Children> {

    /**
     * 返回当前 receiver 的完整有序管道，列表是可变状态的直接视图。
     * @return {@link List<Bson>}
     * @author anwen
     * @mongoPipelineRepresentation source=RECEIVER semanticType=PIPELINE order=CALL_ORDER access=LIVE_VIEW
     */
    List<Bson> getAggregateConditionList();

    /**
     * 获取管道
     * <p>这里获取的是第0个，应对构建需要用到一个管道的情况</p>
     * @author anwen
     */
    default Bson getAggregateCondition(){
        return getAggregateCondition(0);
    }

    /**
     * 获取指定下标的管道
     * @author anwen
     */
    default Bson getAggregateCondition(int index){
        return getAggregateConditionList().get(index);
    }

    /**
     * 是否跳过获取结果
     * @author anwen
     */
    boolean isSkip();

    /**
     * 获取聚合管道选项
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject getAggregateOptions();

    /* $addFields阶段 start */

    /**
     * 向嵌入文档或文档中加入一个新字段
     * $addFields: {"specs": "unleaded"}
     * $addFields: {"specs.fuel_type": "unleaded"}
     * @param field 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $addFields
     * @mongoParam field OUTPUT_FIELD_NAME VALUE
     * @mongoParam value PIPELINE_EXPRESSION VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children addFields(final String field,final String value);

    /**
     * 向嵌套文档或文档中加入一个新字段
     * $addFields: {"specs": "unleaded"}
     * $addFields: {"specs.fuel_type": "unleaded"}
     * @param field 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $addFields
     * @mongoParam field OUTPUT_FIELD_NAME VALUE
     * @mongoParam value PIPELINE_EXPRESSION VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T> Children addFields(final SFunction<T,?> field,final String value);

    /**
     * 向嵌套文档或文档中加入一个新字段
     * $addFields: {"specs": "unleaded"}
     * $addFields: {"specs.fuel_type": "unleaded"}
     * @param value 值
     * @param field 字段，嵌套文档请按照顺序传入，中间会自动拼接.
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $addFields
     * @mongoParam value PIPELINE_EXPRESSION VALUE
     * @mongoParam field OUTPUT_FIELD_PATH_SEGMENT ELEMENT
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    @SuppressWarnings("unchecked")
    <T> Children addFields(final String value,final SFunction<T,?>... field);

    /**
     * $addFields阶段，指定现有字段，覆盖原字段
     * @param field 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    <T> Children addFields(final SFunction<T,?> field,final Object value);

    /**
     * $addFields阶段，向数组中添加元素
     * @param field 数组字段
     * @param value 需要向数组中添加的值
     * @return {@link Children}
     * @author anwen
     */
    <T> Children addFields(final SFunction<T,?> field, final Collection<?> value);

    /**
     * $addFields阶段，向数组中添加元素
     * @param field 数组
     * @param value 需要向数组中添加的值
     * @return {@link Children}
     * @author anwen
     */
    Children addFields(final String field, final Collection<?> value);

    /**
     * $addFields阶段
     * @param fields 多个Field
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $addFields
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children addFields(final Field<?>... fields);

    /**
     * $addFields阶段
     * @param fields 多个Field
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $addFields
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children addFields(final List<Field<?>> fields);

    /**
     * $addFields阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson，或使用${@link Field}
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $addFields
     * @mongoParam bson STAGE_BODY_DOCUMENT VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children addFields(final Bson bson);

    /* $addFields阶段 end */

    /* =============================================================================== */

    /* $set阶段 start */

    /**
     * 向嵌入文档或文档中加入一个新字段
     * $set: {"specs": "unleaded"}
     * $set: {"specs.fuel_type": "unleaded"}
     * @param field 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $set
     * @mongoParam field OUTPUT_FIELD_NAME VALUE
     * @mongoParam value PIPELINE_EXPRESSION VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children set(final String field,final String value);

    /**
     * 向嵌套文档或文档中加入一个新字段
     * $set: {"specs": "unleaded"}
     * $set: {"specs.fuel_type": "unleaded"}
     * @param field 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $set
     * @mongoParam field OUTPUT_FIELD_NAME VALUE
     * @mongoParam value PIPELINE_EXPRESSION VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T> Children set(final SFunction<T,?> field,final String value);

    /**
     * 向嵌套文档或文档中加入一个新字段
     * $set: {"specs": "unleaded"}
     * $set: {"specs.fuel_type": "unleaded"}
     * @param value 值
     * @param field 字段，嵌套文档请按照顺序传入，中间会自动拼接.
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $set
     * @mongoParam value PIPELINE_EXPRESSION VALUE
     * @mongoParam field OUTPUT_FIELD_PATH_SEGMENT ELEMENT
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    @SuppressWarnings("unchecked")
    <T> Children set(final String value,final SFunction<T,?>... field);

    /**
     * $set阶段，指定现有字段，覆盖原字段
     * @param field 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    <T> Children set(final SFunction<T,?> field,final Object value);

    /**
     * $set阶段，向数组中添加元素
     * @param field 数组字段
     * @param value 需要向数组中添加的值
     * @return {@link Children}
     * @author anwen
     */
    <T> Children set(final SFunction<T,?> field, final Collection<?> value);

    /**
     * $set阶段，向数组中添加元素
     * @param field 数组
     * @param value 需要向数组中添加的值
     * @return {@link Children}
     * @author anwen
     */
    Children set(final String field, final Collection<?> value);

    /**
     * $set阶段
     * @param fields 多个Field
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $set
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children set(final Field<?>... fields);

    /**
     * $set阶段
     * @param fields 多个Field
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $set
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children set(final List<Field<?>> fields);

    /**
     * $set阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson，或使用${@link Field}
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $set
     * @mongoParam bson STAGE_BODY_DOCUMENT VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children set(final Bson bson);

    /* $set阶段 end */

    /* =============================================================================== */

    /* $bucket阶段 start */

    /**
     * $bucket阶段
     * @param groupBy 分组字段
     * @param boundaries 桶边界
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $bucket
     * @mongoParam groupBy FIELD_REFERENCE VALUE
     * @mongoParam boundaries BUCKET_BOUNDARY ELEMENT
     */
    <Boundary,T> Children bucket(final SFunction<T,?> groupBy,final List<Boundary> boundaries);

    /**
     * $bucket阶段
     * @param groupBy 分组字段
     * @param boundaries 桶边界
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $bucket
     * @mongoParam groupBy PIPELINE_EXPRESSION VALUE
     * @mongoParam boundaries BUCKET_BOUNDARY ELEMENT
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <Boundary> Children bucket(final Object groupBy,final List<Boundary> boundaries);

    /**
     * $bucket阶段
     * @param groupBy 分组字段
     * @param boundaries 桶边界
     * @param options 可选值，其中包含default和output
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $bucket
     * @mongoParam groupBy FIELD_REFERENCE VALUE
     * @mongoParam boundaries BUCKET_BOUNDARY ELEMENT
     */
    <Boundary,T> Children bucket(final SFunction<T,?> groupBy, final List<Boundary> boundaries, BucketOptions options);

    /**
     * $bucket阶段
     * @param groupBy 分组字段
     * @param boundaries 桶边界
     * @param options 可选值，其中包含default和output
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $bucket
     * @mongoParam groupBy PIPELINE_EXPRESSION VALUE
     * @mongoParam boundaries BUCKET_BOUNDARY ELEMENT
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <Boundary> Children bucket(final Object groupBy, final List<Boundary> boundaries, BucketOptions options);

    /**
     * $bucket阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $bucket
     * @mongoParam bson STAGE_BODY_DOCUMENT VALUE
     */
    Children bucket(final Bson bson);

    /* $bucket阶段 end */

    /* =============================================================================== */

    /* $bucketAuto阶段 start */

    /**
     * $bucketAuto阶段
     * @param groupBy 分组字段
     * @param buckets 桶的数量
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $bucketAuto
     * @mongoParam groupBy FIELD_REFERENCE VALUE
     */
    <T> Children bucketAuto(final SFunction<T,?> groupBy,final Integer buckets);

    /**
     * $bucketAuto阶段
     * @param groupBy 分组字段
     * @param buckets 桶的数量
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $bucketAuto
     * @mongoParam groupBy PIPELINE_EXPRESSION VALUE
     */
    Children bucketAuto(final Object groupBy,final Integer buckets);

    /**
     * $bucketAuto阶段
     * @param groupBy 分组字段
     * @param buckets 桶的数量
     * @param options 可选值，其中包含output和granularity
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $bucketAuto
     * @mongoParam groupBy FIELD_REFERENCE VALUE
     */
    <T> Children bucketAuto(final SFunction<T,?> groupBy, final Integer buckets, BucketAutoOptions options);

    /**
     * $bucketAuto阶段
     * @param groupBy 分组字段
     * @param buckets 桶的数量
     * @param options 可选值，其中包含output和granularity
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $bucketAuto
     * @mongoParam groupBy PIPELINE_EXPRESSION VALUE
     */
    Children bucketAuto(final Object groupBy, final Integer buckets, BucketAutoOptions options);

    /**
     * $bucketAuto阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $bucketAuto
     * @mongoParam bson STAGE_BODY_DOCUMENT VALUE
     */
    Children bucketAuto(final Bson bson);

    /* $bucketAuto阶段 end */

    /* =============================================================================== */

    /* $count阶段 start */

    /**
     * $count阶段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $count
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children count();

    /**
     * $count阶段
     * @param field 输出字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $count
     * @mongoParam field OUTPUT_FIELD_NAME VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children count(final String field);

    /**
     * $count阶段
     * @param field 输出字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $count
     * @mongoParam field OUTPUT_FIELD_NAME VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T> Children count(final SFunction<T,?> field);

    /* $count阶段 end */

    /* =============================================================================== */

    /* $match阶段 start */

    /**
     * $match阶段
     * @param queryWrapper 实体对象封装操作类 {@link com.mongoplus.conditions.query.QueryWrapper}
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $match
     */
    Children match(final Wrapper<?> queryWrapper);

    /**
     * $match阶段
     * @param function 函数
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $match
     */
    Children match(final SFunction<QueryWrapper<?>, QueryWrapper<?>> function);

    /**
     * $match阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @author anwen
     *
     * @mongoStage $match
     * @mongoParam bson STAGE_BODY_DOCUMENT VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children match(final Bson bson);

    /* $match阶段 end */

    /* =============================================================================== */

    /* $project阶段 start */

    /**
     * $project阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * 如：使用{@link com.mongoplus.aggregate.pipeline.Projections}进行构建，基于MongoDB驱动提供，进行封装，支持lambda形式
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $project
     * @mongoParam bson STAGE_BODY_DOCUMENT VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoDocumentInput parameter=bson semantic=STAGE_BODY_DOCUMENT encoding=WRAP_DECLARED_STAGE
     * @mongoDocumentSource mongoDocumentInput path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=project(Bson);custom mechanism=BasicDBObject($project,bson)包装一次并追加一个Stage。
     * @mongoDocumentPolicy input=FLAT_DOCUMENT numeric=ZERO_NONZERO_FLAGS boolean=FALSE_TRUE_FLAGS exceptionField=_id mixed=REJECT_NON_EXCEPTION_EXCLUSION empty=REJECT expressions=INDEPENDENT_EVIDENCE
     * @mongoDocumentSource mongoDocumentPolicy reference=https://raw.githubusercontent.com/mongodb/mongo/v8.0/src/mongo/db/query/projection_parser.cpp symbols=isInclusionOrExclusionType;parseInclusion;parseExclusion;parseLiteral;parseAndAnalyze mechanism=顶层数字与Bool按零或非零判包含/排除，_id标志不决定其他字段模式；普通排除不能混入include/computed。
     * @mongoCandidate operation=STAGE_DOCUMENT_INPUT
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=project;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     */
    Children project(final Bson bson);

    /* $project阶段 end */

    /* =============================================================================== */

    /* $sort阶段 start */

    /**
     * $sort阶段
     * @param field 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoParam value INTEGER_VALUE VALUE
     * @mongoCandidate operation=FIELD_STAGE key=field value=value allowedInt32=-1,1
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo/v8.0/src/mongo/db/query/sort_pattern.cpp symbols=SortPattern::SortPattern mechanism=普通数值方向只接受1或-1；Core虽可编码其他Integer，合法候选输入域不能因此扩张。
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=sort;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=sort;SimplePipelineStage.toBsonDocument mechanism=SimplePipelineStage仅以声明的Stage键包裹原body，保留BSON类型、值和字段顺序。
     */
    Children sort(final String field, final Integer value);

    /**
     * $sort阶段
     * @param field 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME VALUE
     */
    <T> Children sort(final SFunction<T,?> field,final Integer value);

    /**
     * $sort阶段，按照指定字段升序排序
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME VALUE
     */
    <T> Children sortAsc(final SFunction<T,?> field);

    /**
     * $sort阶段，按照指定字段升序排序
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoCandidate operation=FIELD_STAGE key=field value=int32:1
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=sortAsc;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=sort;SimplePipelineStage.toBsonDocument mechanism=SimplePipelineStage仅以声明的Stage键包裹原body，保留BSON类型、值和字段顺序。
     */
    Children sortAsc(final String field);

    /**
     * $sort阶段，按照指定字段升序排序
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME ELEMENT
     */
    @SuppressWarnings("unchecked")
    <T> Children sortAsc(final SFunction<T, ?>... field);

    /**
     * $sort阶段，按照指定字段升序排序
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME ELEMENT
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoCandidate operation=FIELD_STAGE key=field value=int32:1
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=sortAsc;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=sort;SimplePipelineStage.toBsonDocument mechanism=SimplePipelineStage仅以声明的Stage键包裹原body，保留BSON类型、值和字段顺序。
     */
    Children sortAsc(final String... field);

    /**
     * $sort阶段，按照指定字段升序排序
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME ELEMENT
     */
    <T> Children sortAscLambda(final List<SFunction<T,?>> field);

    /**
     * $sort阶段，按照指定字段升序排序
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME ELEMENT
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoCandidate operation=FIELD_STAGE key=field value=int32:1
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=sortAsc;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=sort;SimplePipelineStage.toBsonDocument mechanism=SimplePipelineStage仅以声明的Stage键包裹原body，保留BSON类型、值和字段顺序。
     */
    Children sortAsc(final List<String> field);

    /**
     * $sort阶段，按照指定字段降序排序
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME VALUE
     */
    <T> Children sortDesc(final SFunction<T,?> field);

    /**
     * $sort阶段，按照指定字段降序排序
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoCandidate operation=FIELD_STAGE key=field value=int32:-1
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=sortDesc;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=sort;SimplePipelineStage.toBsonDocument mechanism=SimplePipelineStage仅以声明的Stage键包裹原body，保留BSON类型、值和字段顺序。
     */
    Children sortDesc(final String field);

    /**
     * $sort阶段，按照指定字段降序排序
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME ELEMENT
     */
    @SuppressWarnings("unchecked")
    <T> Children sortDesc(final SFunction<T, ?>... field);

    /**
     * $sort阶段，按照指定字段降序排序
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME ELEMENT
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoCandidate operation=FIELD_STAGE key=field value=int32:-1
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=sortDesc;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=sort;SimplePipelineStage.toBsonDocument mechanism=SimplePipelineStage仅以声明的Stage键包裹原body，保留BSON类型、值和字段顺序。
     */
    Children sortDesc(final String... field);

    /**
     * $sort阶段，按照指定字段降序排序
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME ELEMENT
     */
    <T> Children sortDescLambda(final List<SFunction<T,?>> field);

    /**
     * $sort阶段，按照指定字段降序排序
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sort
     * @mongoParam field FIELD_NAME ELEMENT
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoCandidate operation=FIELD_STAGE key=field value=int32:-1
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=sortDesc;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=sort;SimplePipelineStage.toBsonDocument mechanism=SimplePipelineStage仅以声明的Stage键包裹原body，保留BSON类型、值和字段顺序。
     */
    Children sortDesc(final List<String> field);

    /**
     * 为给定字段上的文本分数元投影创建排序规范
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     */
    Children metaTextScore(final String field);

    /**
     * 为给定字段上的文本分数元投影创建排序规范
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     */
    <T> Children metaTextScore(final SFunction<T,?> field);

    /**
     * $sort阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     * @mongoParam bson PIPELINE_STAGE_DOCUMENT VALUE
     * @mongoDocumentInput parameter=bson semantic=PIPELINE_STAGE_DOCUMENT encoding=UNCHANGED
     * @mongoDocumentSource mongoDocumentInput path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=sort(Bson);custom mechanism=直接追加完整Stage，无body包装；Sorts.orderBy返回body不能直接绑定到此入口。
     */
    Children sort(final Bson bson);

    /**
     * 将排序规范 body 包装为一个完整的 $sort Stage，并追加到当前 receiver。
     * 可用 {@link com.mongoplus.aggregate.pipeline.Sorts#orderBy(Bson...)} 组合不同方向；
     * 保留 body 的字段顺序及 BSON 值类型，入参不应包含 $sort 外层。
     * 本方法与 {@link #sort(Bson)} 的完整 Stage 透传入口具有不同的输入语义。
     *
     * @param specification 非 null 的排序规范 body
     * @return 当前 receiver
     * @throws NullPointerException specification 为 null 时抛出，此时不追加 Stage
     * @mongoStage $sort
     * @mongoParam specification SORT_SPECIFICATION VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoDocumentInput parameter=specification semantic=SORT_SPECIFICATION encoding=WRAP_DECLARED_STAGE
     * @mongoDocumentSource mongoDocumentInput path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java symbols=sortSpecification;custom mechanism=默认方法将排序body交Driver包装一次，再通过custom追加到当前receiver；LambdaAggregateWrapper继承此实现。
     * @mongoDocumentSource mongoDocumentInput reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=sort;SimplePipelineStage.toBsonDocument mechanism=SimplePipelineStage用声明的Stage键包装原body的toBsonDocument结果，不重排字段或转换BSON值类型。
     * @mongoCandidate operation=STAGE_DOCUMENT_INPUT
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java symbols=sortSpecification;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=sort;SimplePipelineStage.toBsonDocument mechanism=SimplePipelineStage仅以声明的Stage键包裹原body，保留BSON类型、值和字段顺序。
     */
    default Children sortSpecification(final Bson specification) {
        java.util.Objects.requireNonNull(specification, "specification");
        return custom(Aggregates.sort(specification));
    }

    /* $sort阶段 end */

    /* =============================================================================== */

    /* $sortByCount阶段 start */

    /**
     * $sortByCount阶段
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sortByCount
     * @mongoParam field PIPELINE_EXPRESSION VALUE
     * @mongoStageValue field encoding=UNCHANGED prefix=$ minimumLength=2
     * @mongoStageValueSource field path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=sortByCount(String);custom(Bson) mechanism=String 表达式原样传给 Aggregates.sortByCount，不自动添加或移除美元前缀；正常返回追加一次。
     * @mongoStageValueSource field artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=Aggregates.sortByCount;SortByCountStage.toBsonDocument mechanism=BuildersHelper.encodeValue 将 String 原值编码为整个 Stage body，不接受任意表达式对象代替 String。
     * @mongoStageValueSource field reference=https://raw.githubusercontent.com/mongodb/mongo/v8.0/src/mongo/db/pipeline/document_source_sort_by_count.cpp symbols=DocumentSourceSortByCount.createFromBson mechanism=字符串必须以美元符号开头且至少有一个后续字符；Core 能编码普通字符串，但服务端拒绝；变量引用另需正式作用域绑定 evidence。
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children sortByCount(final String field);

    /**
     * $sortByCount阶段
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sortByCount
     * @mongoParam field FIELD_REFERENCE VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T> Children sortByCount(final SFunction<T,?> field);

    /* $sortByCount阶段 end */

    /* =============================================================================== */

    /* $skip阶段 start */

    /**
     * $skip阶段，跳过指定数量的文档。
     * <p>long 经 Math.toIntExact 委托 int overload，仍编码为 BSON Int32；超出 Int32 范围时抛出
     * ArithmeticException 且不追加 Stage。合法绑定范围为 0..2147483647，负值虽能编码但服务端拒绝。</p>
     * @param skip 跳过的文档数量，不是页码
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $skip
     * @mongoParam skip INTEGER_VALUE VALUE
     * @mongoStageValue skip encoding=INT32_EXACT minimum=0 maximum=2147483647
     * @mongoStageValueSource skip path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=skip(long);skip(int);custom(Bson) mechanism=Math.toIntExact 先精确转为 int，再委托 int overload；不会输出 BSON Int64；越界抛异常且不追加。
     * @mongoStageValueSource skip artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=Aggregates.skip(int) mechanism=构造 BsonDocument($skip,new BsonInt32(skip))，没有非负运行时校验；合法绑定整数不得截断或舍入。
     * @mongoStageValueSource skip reference=https://raw.githubusercontent.com/mongodb/mongo/v8.0/src/mongo/db/pipeline/document_source_skip.cpp symbols=DocumentSourceSkip.createFromBson;DocumentSourceSkip.create mechanism=MongoDB 接受非负 64 位整数，包括零；Core 的实际范围与服务端合法范围取交集为 0..2147483647。
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoCandidate operation=STAGE_VALUE
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=skip;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=skip mechanism=Aggregates.skip直接构造Int32的Stage值，无额外转换。
     */
    Children skip(final long skip);

    /**
     * $skip阶段，跳过指定数量的文档。
     * <p>int 原样编码为 BSON Int32。合法绑定范围为 0..2147483647；Core 不拒绝负数，服务端拒绝。</p>
     * @param skip 跳过的文档数量，不是页码
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $skip
     * @mongoParam skip INTEGER_VALUE VALUE
     * @mongoStageValue skip encoding=INT32_EXACT minimum=0 maximum=2147483647
     * @mongoStageValueSource skip path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=skip(int);custom(Bson) mechanism=int 原样进入 Aggregates.skip，正常返回向当前 receiver 追加一次；Core 不校验负值。
     * @mongoStageValueSource skip artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=Aggregates.skip(int) mechanism=构造 BsonDocument($skip,new BsonInt32(skip))，不会输出 BSON Int64；合法绑定整数不得截断或舍入。
     * @mongoStageValueSource skip reference=https://raw.githubusercontent.com/mongodb/mongo/v8.0/src/mongo/db/pipeline/document_source_skip.cpp symbols=DocumentSourceSkip.createFromBson;DocumentSourceSkip.create mechanism=MongoDB 接受非负 64 位整数，包括零；Core 的实际范围与服务端合法范围取交集为 0..2147483647。
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoCandidate operation=STAGE_VALUE
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=skip;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=skip mechanism=Aggregates.skip直接构造Int32的Stage值，无额外转换。
     */
    Children skip(final int skip);

    /* $skip阶段 end */

    /* =============================================================================== */

    /* $limit阶段 start */

    /**
     * $limit阶段 每页显示行数
     * @param limit 每页显示行数
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $limit
     */
    Children limit(final long limit);

    /**
     * $limit阶段 每页显示行数
     * @param limit 每页显示行数
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $limit
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children limit(final int limit);

    /* $limit阶段 end */

    /* =============================================================================== */

    /* $lookup阶段 start */

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    Children lookup(final String from,final String localField,final String foreignField,final String as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T> Children lookup(final String from,final String localField,final String foreignField,final SFunction<T,?> as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    Children lookup(final Class<?> from,final String localField,final String foreignField,final String as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T> Children lookup(final Class<?> from,final String localField,final String foreignField,final SFunction<T,?> as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T,R> Children lookup(final String from,final SFunction<T,?> localField,final SFunction<R,?> foreignField,
                          final String as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T,R,A> Children lookup(final String from,final SFunction<T,?> localField,final SFunction<R,?> foreignField,
                          final SFunction<A,?> as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T,R> Children lookup(final Class<?> from,final SFunction<T,?> localField,final SFunction<R,?> foreignField,
                          final String as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T,R,A> Children lookup(final Class<?> from,final SFunction<T,?> localField,final SFunction<R,?> foreignField,
                          final SFunction<A,?> as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T> Children lookup(final String from,final SFunction<T,?> localField,final String foreignField,final String as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T,A> Children lookup(final String from,final SFunction<T,?> localField,final String foreignField,
                        final SFunction<A,?> as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T> Children lookup(final Class<?> from,final SFunction<T,?> localField,final String foreignField,final String as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T,A> Children lookup(final Class<?> from,final SFunction<T,?> localField,final String foreignField,
                        final SFunction<A,?> as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T> Children lookup(final String from,final String localField,final SFunction<T,?> foreignField,final String as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T,A> Children lookup(final String from,final String localField,final SFunction<T,?> foreignField,
                        final SFunction<A,?> as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T> Children lookup(final Class<?> from,final String localField,final SFunction<T,?> foreignField,final String as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param localField 当前集合用于关联的字段
     * @param foreignField 指定目标集合用于关联的字段
     * @param as 输出结果中保存关联值的字段名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam localField LOCAL_FIELD_NAME VALUE
     * @mongoParam foreignField FOREIGN_FIELD_NAME VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T,A> Children lookup(final Class<?> from,final String localField,final SFunction<T,?> foreignField,
                        final SFunction<A,?> as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param letList 在管道字段阶段使用的变量
     * @param aggregate 在连接集合上运行的管道
     * @param as 输出结果中保存关联值的字段名
     * @return Children
     * @author JiaChaoYang
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam aggregate PIPELINE VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     * @mongoParam letList VARIABLE_DEFINITION ELEMENT
     * @mongoEntryConstruction letList constructor=com.mongodb.client.model.Variable artifact=org.mongodb:mongodb-driver-core:5.4.0 key=0 value=1 keySemantic=VARIABLE_NAME valueSemantic=PIPELINE_EXPRESSION result=VARIABLE_DEFINITION
     * @mongoEntryConstructionSource letList artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=com.mongodb.client.model.Variable#Variable(String,TExpression);getName;getValue mechanism=公开构造器原样保存名称和表达式值；StringCodec保留字段引用字符串。
     * @mongoTypedContainer letList input=DOCUMENT_ENTRIES order=INPUT target=java.util.List
     * @mongoVariableScope declarations=letList body=aggregate parent=ENCLOSING initializer=PARENT inheritance=LEXICAL shadowing=NEAREST exit=RESTORE_PARENT
     * @mongoVariableScopeSource reference=https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/ symbols=let;pipeline mechanism=let声明仅在目标pipeline及继承该环境的嵌套pipeline可见；退出body不向outer或sibling导出声明。
     * @mongoVariableScopeSource reference=https://github.com/mongodb/mongo/blob/r8.0.0/src/mongo/db/pipeline/document_source_lookup.cpp symbols=DocumentSourceLookUp;Expression::parseOperand;VariablesParseState::defineVariable;Variables::copyToExpCtx mechanism=initializer使用expCtx的父variablesParseState；新变量写入独立_variablesParseState并复制到foreign body环境。
     * @mongoVariableScopeSource reference=https://github.com/mongodb/mongo/blob/r8.0.0/src/mongo/db/pipeline/variables.cpp symbols=VariablesParseState::defineVariable;VariablesParseState::getVariable mechanism=同名新声明替换当前独立parseState中的名称到ID映射；引用取得最近环境中的声明ID；找不到已声明用户或system变量时报undefined-variable。
     * @mongoObjectField letList field=let
     * @mongoObjectFieldSource letList path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=lookup(String,List,Aggregate,String) mechanism=letList原样交给Driver；Aggregate提取既有有序管道列表。
     * @mongoObjectFieldSource letList artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=Aggregates.LookupStage.toBsonDocument;Variable.getName;Variable.getValue mechanism=按List顺序遍历变量，以名称writeName并encodeValue写入let子document。
     * @mongoObjectField from field=from
     * @mongoObjectField aggregate field=pipeline
     * @mongoObjectField as field=as
     * @mongoObjectFieldSource from path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=lookup(String,List,Aggregate,String) mechanism=from原样委托Aggregates.lookup。
     * @mongoObjectFieldSource from artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=Aggregates.LookupStage.toBsonDocument mechanism=writeString将from原样写入lookup对象的from字段。
     * @mongoObjectFieldSource aggregate path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=lookup(String,List,Aggregate,String) mechanism=getAggregateConditionList提供完整有序PIPELINE。
     * @mongoObjectFieldSource aggregate artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=Aggregates.LookupStage.toBsonDocument mechanism=逐stage编码到pipeline数组，保持输入顺序。
     * @mongoObjectFieldSource as path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=lookup(String,List,Aggregate,String) mechanism=as原样委托Aggregates.lookup。
     * @mongoObjectFieldSource as artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=Aggregates.LookupStage.toBsonDocument mechanism=writeString将as原样写入lookup对象的as字段。
     * @mongoPipelineInput aggregate extractor=com.mongoplus.aggregate.Aggregate#getAggregateConditionList()
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <TExpression> Children lookup(final String from, final List<Variable<TExpression>> letList,
                                  final Aggregate<?> aggregate, final String as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param letList 在管道字段阶段使用的变量
     * @param aggregate 在连接集合上运行的管道
     * @param as 输出结果中保存关联值的字段名
     * @return Children
     * @author JiaChaoYang
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam aggregate PIPELINE VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <TExpression,T> Children lookup(final String from, final List<Variable<TExpression>> letList,
                                  final Aggregate<?> aggregate, final SFunction<T,?> as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param letList 在管道字段阶段使用的变量
     * @param aggregate 在连接集合上运行的管道
     * @param as 输出结果中保存关联值的字段名
     * @return Children
     * @author JiaChaoYang
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam aggregate PIPELINE VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <TExpression> Children lookup(final Class<?> from, final List<Variable<TExpression>> letList,
                                  final Aggregate<?> aggregate, final String as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param letList 在管道字段阶段使用的变量
     * @param aggregate 在连接集合上运行的管道
     * @param as 输出结果中保存关联值的字段名
     * @return Children
     * @author JiaChaoYang
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam aggregate PIPELINE VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <TExpression,T> Children lookup(final Class<?> from, final List<Variable<TExpression>> letList,
                                  final Aggregate<?> aggregate, final SFunction<T,?> as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param aggregate 在连接集合上运行的管道
     * @param as 输出结果中保存关联值的字段名
     * @return Children
     * @author JiaChaoYang
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam aggregate PIPELINE VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     * @mongoObjectField from field=from
     * @mongoObjectFieldSource from path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=lookup(String,Aggregate,String) mechanism=from 原样传给 Driver Aggregates.lookup。
     * @mongoObjectFieldSource from artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=com.mongodb.client.model.Aggregates.lookup;LookupStage.toBsonDocument mechanism=Driver 将 from 字符串原样写入 from 字段。
     * @mongoObjectField aggregate field=pipeline
     * @mongoObjectFieldSource aggregate path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=lookup(String,Aggregate,String);getAggregateConditionList mechanism=Aggregate 取 receiver 的完整 Stage 列表后传给 Driver。
     * @mongoObjectFieldSource aggregate artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=com.mongodb.client.model.Aggregates.lookup;LookupStage.toBsonDocument mechanism=Driver 将完整 Stage 列表按输入顺序编码为 pipeline 数组。
     * @mongoVariableEnvironment aggregate source=ENCLOSING inheritance=LEXICAL exit=RESTORE_PARENT
     * @mongoVariableScopeSource reference=https://github.com/mongodb/mongo/blob/r8.0.0/src/mongo/db/pipeline/document_source_lookup.cpp symbols=DocumentSourceLookUp;Variables::copyToExpCtx mechanism=无本地let声明时仍复制父variables及parseState到独立foreign pipeline环境；不向父环境导出声明。
     * @mongoObjectField as field=as
     * @mongoObjectFieldSource as path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=lookup(String,Aggregate,String) mechanism=as 输出字段名原样传给 Driver Aggregates.lookup。
     * @mongoObjectFieldSource as artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=com.mongodb.client.model.Aggregates.lookup;LookupStage.toBsonDocument mechanism=Driver 将 as 字符串原样写入 as 字段。
     * @mongoPipelineInput aggregate extractor=com.mongoplus.aggregate.Aggregate#getAggregateConditionList()
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children lookup(final String from, final Aggregate<?> aggregate, final String as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param aggregate 在连接集合上运行的管道
     * @param as 输出结果中保存关联值的字段名
     * @return Children
     * @author JiaChaoYang
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam aggregate PIPELINE VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T> Children lookup(final String from, final Aggregate<?> aggregate, final SFunction<T,?> as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param aggregate 在连接集合上运行的管道
     * @param as 输出结果中保存关联值的字段名
     * @return Children
     * @author JiaChaoYang
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam aggregate PIPELINE VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    Children lookup(final Class<?> from, final Aggregate<?> aggregate, final String as);

    /**
     * $lookup阶段
     * @param from 目标集合名称
     * @param aggregate 在连接集合上运行的管道
     * @param as 输出结果中保存关联值的字段名
     * @return Children
     * @author JiaChaoYang
     *
     * @mongoStage $lookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam aggregate PIPELINE VALUE
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     */
    <T> Children lookup(final Class<?> from, final Aggregate<?> aggregate, final SFunction<T,?> as);

    /**
     * $lookup阶段,如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children lookup(final Bson bson);

    /* $lookup阶段 end */

    /* =============================================================================== */

    /* $facet阶段 start */

    /**
     * $facet阶段
     * @param name facet名称
     * @param pipeline facet管道
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $facet
     * @mongoParam name OUTPUT_FIELD_NAME VALUE
     * @mongoParam pipeline PIPELINE_STAGE_DOCUMENT ELEMENT
     */
    Children facet(final String name, final Bson... pipeline);

    /**
     * $facet阶段
     * @param name facet名称
     * @param pipeline facet管道
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $facet
     * @mongoParam name OUTPUT_FIELD_NAME VALUE
     * @mongoParam pipeline PIPELINE VALUE
     */
    Children facet(final String name, final List<? extends Bson> pipeline);

    /**
     * $facet阶段
     * @param name facet名称
     * @param aggregate facet管道
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $facet
     * @mongoParam name OUTPUT_FIELD_NAME VALUE
     * @mongoParam aggregate PIPELINE VALUE
     */
    Children facet(final String name, final Aggregate<?> aggregate);

    /**
     * $facet阶段
     * @param facets facets，可以使用{@link com.mongoplus.aggregate.pipeline.Facet}进行构建
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $facet
     * @mongoParam facets NAMED_PIPELINE ELEMENT
     * @mongoPipelineContainer facets operation=NAMED_PIPELINES result=STAGE_BODY_DOCUMENT order=INPUT invocation=SINGLE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children facet(final Facet... facets);

    /**
     * $facet阶段
     * @param facets facets，可以使用{@link com.mongoplus.aggregate.pipeline.Facet}进行构建
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $facet
     * @mongoParam facets NAMED_PIPELINE ELEMENT
     * @mongoPipelineContainer facets operation=NAMED_PIPELINES result=STAGE_BODY_DOCUMENT order=INPUT invocation=SINGLE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children facet(final List<Facet> facets);

    /**
     * $facet阶段,如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children facet(final Bson bson);

    /* $facet阶段 end */

    /* =============================================================================== */

    /* $graphLookup阶段 start */

    /**
     * $graphLookup阶段
     * @param from 要查询的集合
     * @param startWith 启动图形查找的表达式
     * @param connectFromField 来自字段
     * @param connectToField 目标字段
     * @param as 输出文档中的字段名称
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $graphLookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam startWith PIPELINE_EXPRESSION VALUE
     * @mongoParam connectFromField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_FROM_FIELD_NAME
     * @mongoParam connectToField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_TO_FIELD_NAME
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children graphLookup(final String from, final Object startWith, final String connectFromField,
                         final String connectToField, final String as);

    /**
     * $graphLookup阶段
     * @param from 要查询的集合
     * @param startWith 启动图形查找的表达式
     * @param connectFromField 来自字段
     * @param connectToField 目标字段
     * @param as 输出文档中的字段名称
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $graphLookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam startWith FIELD_REFERENCE VALUE
     * @mongoParam connectFromField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_FROM_FIELD_NAME
     * @mongoParam connectToField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_TO_FIELD_NAME
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T,R,U> Children graphLookup(final String from, final SFunction<T,?> startWith, final SFunction<R,?> connectFromField,
                         final SFunction<U,?> connectToField, final String as);

    /**
     * $graphLookup阶段
     * @param from 要查询的集合
     * @param startWith 启动图形查找的表达式
     * @param connectFromField 来自字段
     * @param connectToField 目标字段
     * @param as 输出文档中的字段名称
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $graphLookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam startWith PIPELINE_EXPRESSION VALUE
     * @mongoParam connectFromField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_FROM_FIELD_NAME
     * @mongoParam connectToField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_TO_FIELD_NAME
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T,R> Children graphLookup(final String from, final Object startWith, final SFunction<T,?> connectFromField,
                               final SFunction<R,?> connectToField, final String as);

    /**
     * $graphLookup阶段
     * @param from 要查询的集合
     * @param startWith 启动图形查找的表达式
     * @param connectFromField 来自字段
     * @param connectToField 目标字段
     * @param as 输出文档中的字段名称
     * @param options 查找选项:
     * <div>
     *      <p>maxDepth（指定最大递归深度的非负整）</p>
     *      <p>depthField（要添加到搜索路径中每个遍历文档的字段的名称）</p>
     *      <p>restrictSearchWithMatch（指定递归搜索的附加条件的文档，不能是表达式，比如不能是{@code { lastName: { $ne: "$lastName" } }}）</p>
     * </div>
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $graphLookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam startWith PIPELINE_EXPRESSION VALUE
     * @mongoParam connectFromField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_FROM_FIELD_NAME
     * @mongoParam connectToField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_TO_FIELD_NAME
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children graphLookup(final String from, final Object startWith, final String connectFromField,
                         final String connectToField, final String as, final GraphLookupOptions options);

    /**
     * $graphLookup阶段
     * @param from 要查询的集合
     * @param startWith 启动图形查找的表达式
     * @param connectFromField 来自字段
     * @param connectToField 目标字段
     * @param as 输出文档中的字段名称
     * @param options 查找选项:
     * <div>
     *      <p>maxDepth（指定最大递归深度的非负整）</p>
     *      <p>depthField（要添加到搜索路径中每个遍历文档的字段的名称）</p>
     *      <p>restrictSearchWithMatch（指定递归搜索的附加条件的文档，不能是表达式，比如不能是{@code { lastName: { $ne: "$lastName" } }}）</p>
     * </div>
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $graphLookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam startWith FIELD_REFERENCE VALUE
     * @mongoParam connectFromField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_FROM_FIELD_NAME
     * @mongoParam connectToField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_TO_FIELD_NAME
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T,R,U> Children graphLookup(final String from, final SFunction<T,?> startWith, final SFunction<R,?> connectFromField,
                               final SFunction<U,?> connectToField, final String as, final GraphLookupOptions options);

    /**
     * $graphLookup阶段
     * @param from 要查询的集合
     * @param startWith 启动图形查找的表达式
     * @param connectFromField 来自字段
     * @param connectToField 目标字段
     * @param as 输出文档中的字段名称
     * @param options 查找选项:
     * <div>
     *      <p>maxDepth（指定最大递归深度的非负整）</p>
     *      <p>depthField（要添加到搜索路径中每个遍历文档的字段的名称）</p>
     *      <p>restrictSearchWithMatch（指定递归搜索的附加条件的文档，不能是表达式，比如不能是{@code { lastName: { $ne: "$lastName" } }}）</p>
     * </div>
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $graphLookup
     * @mongoParam from COLLECTION_NAME VALUE
     * @mongoParam startWith PIPELINE_EXPRESSION VALUE
     * @mongoParam connectFromField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_FROM_FIELD_NAME
     * @mongoParam connectToField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_TO_FIELD_NAME
     * @mongoParam as OUTPUT_FIELD_NAME VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T,R> Children graphLookup(final String from, final Object startWith, final SFunction<T,?> connectFromField,
                               final SFunction<R,?> connectToField, final String as, final GraphLookupOptions options);

    /**
     * $graphLookup阶段,如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children graphLookup(final Bson bson);

    /* $graphLookup阶段 end */

    /* =============================================================================== */

    /* $group阶段 start */

    /**
     * $group阶段，只有一个字段参数的情况，如{@code $group : { _id : "$item" }}
     * @param _id group的_id表达式
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $group
     * @mongoParam _id PIPELINE_EXPRESSION VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoCandidate operation=PREFIXED_ENTRIES_STAGE key=_id value=_id entries=EMPTY
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=group;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=group;GroupStage.toBsonDocument;BuildersHelper.encodeValue mechanism=GroupStage先写_id，再按输入顺序写BsonField名称及值；BuildersHelper保留实际codec类型。
     */
    Children group(final String _id);

    /**
     * $group阶段，只有一个字段参数的情况，如{@code $group : { _id : "$item" }}
     * @param _id group的_id表达式
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $group
     * @mongoParam _id FIELD_REFERENCE VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T> Children group(final SFunction<T,?> _id);

    /**
     * $group阶段
     * @param id group的_id表达式，可以为null
     * @param fieldAccumulators 零个或多个字段累加器对，使用{@link com.mongoplus.aggregate.pipeline.Accumulators}构建
     * @return {@link Bson}
     * @author anwen
     *
     * @mongoStage $group
     * @mongoParam id PIPELINE_EXPRESSION VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoCandidate operation=PREFIXED_ENTRIES_STAGE key=_id value=id entries=fieldAccumulators
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=group;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=group;GroupStage.toBsonDocument;BuildersHelper.encodeValue mechanism=GroupStage先写_id，再按输入顺序写BsonField名称及值；BuildersHelper保留实际codec类型。
     */
    <TExpression> Children group(@Nullable final TExpression id, final BsonField... fieldAccumulators);

    /**
     * $group阶段
     * @param id group的_id表达式，可以为null
     * @param fieldAccumulators 零个或多个字段累加器对，使用{@link com.mongoplus.aggregate.pipeline.Accumulators}构建
     * @return {@link Bson}
     * @author anwen
     *
     * @mongoStage $group
     * @mongoParam id FIELD_REFERENCE VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T,TExpression> Children group(@Nullable final SFunction<T,?> id, final BsonField... fieldAccumulators);

    /**
     * $group阶段
     * @param id group的_id表达式，可以为null
     * @param fieldAccumulators 零个或多个字段累加器对，使用{@link com.mongoplus.aggregate.pipeline.Accumulators}构建
     * @return {@link Bson}
     * @author anwen
     *
     * @mongoStage $group
     * @mongoParam id PIPELINE_EXPRESSION VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoCandidate operation=PREFIXED_ENTRIES_STAGE key=_id value=id entries=fieldAccumulators
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=group;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java symbols=group;GroupStage.toBsonDocument;BuildersHelper.encodeValue mechanism=GroupStage先写_id，再按输入顺序写BsonField名称及值；BuildersHelper保留实际codec类型。
     */
    <TExpression> Children group(@Nullable final TExpression id, final List<BsonField> fieldAccumulators);

    /**
     * $group阶段
     * @param id group的_id表达式，可以为null
     * @param fieldAccumulators 零个或多个字段累加器对，使用{@link com.mongoplus.aggregate.pipeline.Accumulators}构建
     * @return {@link Bson}
     * @author anwen
     *
     * @mongoStage $group
     * @mongoParam id FIELD_REFERENCE VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T,TExpression> Children group(@Nullable final SFunction<T,?> id, final List<BsonField> fieldAccumulators);

    /**
     * $group阶段,如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children group(final Bson bson);

    /* $group阶段 end */

    /* =============================================================================== */

    /* $unionWith阶段 start */

    /**
     * $unionWith阶段
     * <p>要包含指定集合中的所有文档而不进行任何处理，您可以使用简化的形式</p>
     * @param collectionName 集合名
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unionWith
     * @mongoParam collectionName COLLECTION_NAME VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children unionWith(final String collectionName);

    /**
     * $unionWith阶段
     * <p>要包含指定集合中的所有文档而不进行任何处理，您可以使用简化的形式</p>
     * @param collection 集合类
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unionWith
     * @mongoParam collection COLLECTION_NAME VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children unionWith(final Class<?> collection);

    /**
     * $unionWith阶段
     * @param collectionName 要执行合并的同一数据库中的集合的名称
     * @param aggregate 应用于输入文档的聚合管道
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unionWith
     * @mongoParam collectionName COLLECTION_NAME VALUE
     * @mongoParam aggregate PIPELINE VALUE
     * @mongoObjectField collectionName field=coll
     * @mongoObjectFieldSource collectionName path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=unionWith(String,Aggregate);unionWith(String,List) mechanism=collectionName 原样委托给 Driver Aggregates.unionWith。
     * @mongoObjectFieldSource collectionName artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=com.mongodb.client.model.Aggregates.unionWith;UnionWithStage.toBsonDocument mechanism=Driver 将 collection 字符串写入 coll 字段。
     * @mongoObjectField aggregate field=pipeline
     * @mongoObjectFieldSource aggregate path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=unionWith(String,Aggregate);getAggregateConditionList();unionWith(String,List) mechanism=Aggregate receiver 的完整有序列表由 getAggregateConditionList 提取并传给 Driver。
     * @mongoObjectFieldSource aggregate artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=com.mongodb.client.model.Aggregates.unionWith;UnionWithStage.toBsonDocument mechanism=Driver 按输入顺序将完整 Stage 编码到 pipeline 数组，不额外包装元素。
     * @mongoPipelineInput aggregate extractor=com.mongoplus.aggregate.Aggregate#getAggregateConditionList()
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children unionWith(final String collectionName,final Aggregate<?> aggregate);

    /**
     * $unionWith阶段
     * @param collectionName 要执行合并的同一数据库中的集合的名称
     * @param aggregate 应用于输入文档的聚合管道
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unionWith
     * @mongoParam collectionName COLLECTION_NAME VALUE
     * @mongoParam aggregate PIPELINE VALUE
     * @mongoObjectField collectionName field=coll
     * @mongoObjectFieldSource collectionName path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=unionWith(String,List) mechanism=collectionName 原样传给 Driver Aggregates.unionWith。
     * @mongoObjectFieldSource collectionName artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=com.mongodb.client.model.Aggregates.unionWith;UnionWithStage.toBsonDocument mechanism=Driver 将 collection 字符串写入 coll 字段。
     * @mongoObjectField aggregate field=pipeline
     * @mongoObjectFieldSource aggregate path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=unionWith(String,List) mechanism=已有完整 Bson Stage 列表原样传给 Driver，不声明 Stage 到 Bson 的构造能力。
     * @mongoObjectFieldSource aggregate artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=com.mongodb.client.model.Aggregates.unionWith;UnionWithStage.toBsonDocument mechanism=Driver 按输入顺序将完整 Stage 编码到 pipeline 数组，不额外包装元素。
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children unionWith(final String collectionName,final List<? extends Bson> aggregate);

    /**
     * $unionWith阶段
     * @param collection 集合名称，取用类名，如有@CollectionName注解，则取用注解值
     * @param aggregate 应用于输入文档的聚合管道
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unionWith
     * @mongoParam collection COLLECTION_NAME VALUE
     * @mongoParam aggregate PIPELINE VALUE
     * @mongoObjectField collection field=coll
     * @mongoObjectFieldSource collection path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=unionWith(Class,Aggregate);unionWith(String,Aggregate) mechanism=Class 经 AnnotationOperate.getCollectionName 转成集合名再委托 String overload；需要 Java Class 值。
     * @mongoObjectFieldSource collection artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=com.mongodb.client.model.Aggregates.unionWith;UnionWithStage.toBsonDocument mechanism=Driver 将解析后的 collection 字符串写入 coll 字段。
     * @mongoObjectField aggregate field=pipeline
     * @mongoObjectFieldSource aggregate path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=unionWith(Class,Aggregate);unionWith(String,Aggregate);getAggregateConditionList() mechanism=委托 String overload 后提取 Aggregate receiver 的完整有序列表。
     * @mongoObjectFieldSource aggregate artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=com.mongodb.client.model.Aggregates.unionWith;UnionWithStage.toBsonDocument mechanism=Driver 按输入顺序将完整 Stage 编码到 pipeline 数组，不额外包装元素。
     * @mongoPipelineInput aggregate extractor=com.mongoplus.aggregate.Aggregate#getAggregateConditionList()
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children unionWith(final Class<?> collection,final Aggregate<?> aggregate);

    /**
     * $unionWith阶段
     * @param collection 集合名称，取用类名，如有@CollectionName注解，则取用注解值
     * @param aggregate 应用于输入文档的聚合管道
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unionWith
     * @mongoParam collection COLLECTION_NAME VALUE
     * @mongoParam aggregate PIPELINE VALUE
     * @mongoObjectField collection field=coll
     * @mongoObjectFieldSource collection path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=unionWith(Class,List);unionWith(String,List) mechanism=Class 经 AnnotationOperate.getCollectionName 转成集合名再委托 String overload；需要 Java Class 值。
     * @mongoObjectFieldSource collection artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=com.mongodb.client.model.Aggregates.unionWith;UnionWithStage.toBsonDocument mechanism=Driver 将解析后的 collection 字符串写入 coll 字段。
     * @mongoObjectField aggregate field=pipeline
     * @mongoObjectFieldSource aggregate path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=unionWith(Class,List);unionWith(String,List) mechanism=已有完整 Bson Stage 列表委托给 String overload，不声明 Stage 到 Bson 的构造能力。
     * @mongoObjectFieldSource aggregate artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=com.mongodb.client.model.Aggregates.unionWith;UnionWithStage.toBsonDocument mechanism=Driver 按输入顺序将完整 Stage 编码到 pipeline 数组，不额外包装元素。
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children unionWith(final Class<?> collection,final List<? extends Bson> aggregate);

    /**
     * $unionWith阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children unionWith(final Bson bson);

    /* $unionWith阶段 end */

    /* =============================================================================== */

    /* $unwind阶段 start */

    /**
     * $unwind阶段
     * @param fieldName 该字段名称必须以'$'符号为前缀
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unwind
     * @mongoParam fieldName FIELD_REFERENCE VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children unwind(final String fieldName);

    /**
     * $unwind阶段
     * @param fieldName 字段名称
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unwind
     * @mongoParam fieldName FIELD_REFERENCE VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T> Children unwind(final SFunction<T,?> fieldName);

    /**
     * $unwind阶段
     * @param fieldName 该字段名称必须以'$'符号为前缀
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unwind
     * @mongoParam fieldName FIELD_REFERENCE VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children unwind(final String fieldName, final UnwindOption unwindOption);

    /**
     * $unwind阶段
     * @param fieldName 字段名称
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unwind
     * @mongoParam fieldName FIELD_REFERENCE VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T> Children unwind(final SFunction<T,?> fieldName,final UnwindOption unwindOption);

    /**
     * $unwind阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children unwind(final Bson bson);

    /* $unwind阶段 end */

    /* =============================================================================== */

    /* $out阶段 start */

    /**
     * $out阶段
     * @param collectionName 集合名称
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $out
     * @mongoParam collectionName COLLECTION_NAME VALUE
     */
    Children out(final String collectionName);

    /**
     * $out阶段
     * @param collection 集合名称
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $out
     * @mongoParam collection COLLECTION_NAME VALUE
     */
    Children out(final Class<?> collection);

    /**
     * $out
     * @param databaseName 数据库名称
     * @param collectionName 集合名称
     * @return {@link Bson}
     * @author anwen
     *
     * @mongoStage $out
     * @mongoParam databaseName DATABASE_NAME VALUE
     * @mongoParam collectionName COLLECTION_NAME VALUE
     */
    Children out(final String databaseName, final String collectionName);

    /**
     * $out阶段,如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children out(final Bson bson);

    /* $out阶段 end */

    /* =============================================================================== */

    /* $merge阶段 start */

    /**
     * $merge阶段
     * @param collectionName 要合并的集合的名称
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $merge
     * @mongoParam collectionName COLLECTION_NAME VALUE
     */
    Children merge(final String collectionName);

    /**
     * $merge阶段
     * @param collection 集合
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $merge
     * @mongoParam collection COLLECTION_NAME VALUE
     */
    Children merge(final Class<?> collection);

    /**
     * $merge阶段
     * @param namespace 要合并到的命名空间
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $merge
     */
    Children merge(final MongoNamespace namespace);

    /**
     * $merge阶段
     * @param collectionName 要合并的集合的名称
     * @param options 合并选项
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $merge
     * @mongoParam collectionName COLLECTION_NAME VALUE
     */
    Children merge(final String collectionName, final MergeOptions options);

    /**
     * $merge阶段
     * @param collection 要合并的集合
     * @param options 合并选项
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $merge
     * @mongoParam collection COLLECTION_NAME VALUE
     */
    Children merge(final Class<?> collection, final MergeOptions options);

    /**
     * $merge阶段
     * @param namespace 要合并到的命名空间
     * @param options 合并选项
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $merge
     */
    Children merge(final MongoNamespace namespace, final MergeOptions options);

    /**
     * $merge阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children merge(final Bson bson);

    /* $out阶段 end */

    /* =============================================================================== */

    /* replaceRoot阶段 */

    /**
     * $replaceRoot阶段
     * @param fieldName 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $replaceRoot
     * @mongoParam fieldName PIPELINE_EXPRESSION VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <TExpression> Children replaceRoot(final TExpression fieldName);

    /**
     * $replaceRoot阶段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $replaceRoot
     * @mongoParam value PIPELINE_EXPRESSION VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children replaceRoot(final Document value);

    /**
     * $replaceRoot阶段
     * @param fieldName 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $replaceRoot
     * @mongoParam fieldName FIELD_REFERENCE VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T> Children replaceRoot(final SFunction<T,?> fieldName);

    /**
     * $replaceRoot阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children replaceRoot(final Bson bson);

    /* $replaceRoot阶段 end */

    /* =============================================================================== */

    /* $replaceWith阶段 start*/

    /**
     * $replaceWith阶段
     * <p>候选等价证据仅覆盖来源和完整子树已证明的精确 runtime Document 表达式。
     * 必须另行证明实际 Java overload 与泛型绑定、每个 Codec、BSON 类型及顺序和 receiver effect；
     * 普通 String、任意 Bson、未知 Object、Document 子类及 null 不自动属于此等价输入域。</p>
     * @param fieldName 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $replaceWith
     * @mongoParam fieldName PIPELINE_EXPRESSION VALUE
     * @mongoComposition PIPELINE_EXPRESSION -> PIPELINE_STAGE_DOCUMENT
     * @mongoCandidate operation=STAGE_EXPRESSION_DOCUMENT runtimeJava=org.bson.Document runtimeCodec=org.bson.codecs.DocumentCodec bsonDocumentCodec=org.bson.codecs.BsonDocumentCodec
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=replaceWith(TExpression);replaceWith(Bson);custom(Bson) mechanism=原表达式经 Aggregates.replaceWith 包装后由 Bson overload 追加一次，返回当前 typedThis；只在独立证明的 Document 表达式输入域建立等价。
     * @mongoCandidateSource reference=urn:maven:org.mongodb:mongodb-driver-core:5.4.0 symbols=Aggregates.replaceWith;ReplaceStage.toBsonDocument;BuildersHelper.encodeValue mechanism=Driver 的 ReplaceStage(value,true) 写入唯一 $replaceWith 字段；精确 runtime Document 经 Bson 分支、DocumentCodec 和 registry.get(BsonDocument.class) 的 BsonDocumentCodec 编码，保留 BSON 类型、值及所有文档和数组顺序。
     * @mongoCandidateSource reference=urn:maven:org.mongodb:bson:5.4.0 symbols=Document.toBsonDocument;DocumentCodec.encode;BsonDocumentCodec.encode mechanism=Document 从 registry 获取 DocumentCodec，按 entry 迭代顺序保留子值的 runtime 编码；中间 BsonDocumentCodec 及所有叶子和容器 Codec 必须独立确认。
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <TExpression> Children replaceWith(final TExpression fieldName);

    /**
     * $replaceWith阶段
     * <p>仅在精确 runtime Document、独立表达式/Codec/Java 绑定证据和相同 receiver effect
     * 均闭合时，与泛型包装路线构成条件等价；Document 可赋值给 Bson 不授予 Bson 透传路线等价性。</p>
     * @param value 值
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $replaceWith
     * @mongoParam value PIPELINE_EXPRESSION VALUE
     * @mongoComposition PIPELINE_EXPRESSION -> PIPELINE_STAGE_DOCUMENT
     * @mongoCandidate operation=STAGE_EXPRESSION_DOCUMENT runtimeJava=org.bson.Document runtimeCodec=org.bson.codecs.DocumentCodec bsonDocumentCodec=org.bson.codecs.BsonDocumentCodec
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=replaceWith(Document);replaceWith(Bson);custom(Bson) mechanism=Document 原值经 Aggregates.replaceWith 包装后由 Bson overload 追加一次，返回当前 typedThis；与泛型路线比较时仍要求两条完整调用树独立闭合。
     * @mongoCandidateSource reference=urn:maven:org.mongodb:mongodb-driver-core:5.4.0 symbols=Aggregates.replaceWith;ReplaceStage.toBsonDocument;BuildersHelper.encodeValue mechanism=Driver 的 ReplaceStage(value,true) 写入唯一 $replaceWith 字段；精确 runtime Document 经 Bson 分支、DocumentCodec 和 registry.get(BsonDocument.class) 的 BsonDocumentCodec 编码，保留 BSON 类型、值及所有文档和数组顺序。
     * @mongoCandidateSource reference=urn:maven:org.mongodb:bson:5.4.0 symbols=Document.toBsonDocument;DocumentCodec.encode;BsonDocumentCodec.encode mechanism=Document 从 registry 获取 DocumentCodec，按 entry 迭代顺序保留子值的 runtime 编码；中间 BsonDocumentCodec 及所有叶子和容器 Codec 必须独立确认。
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children replaceWith(final Document value);

    /**
     * $replaceWith阶段
     * @param fieldName 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $replaceWith
     * @mongoParam fieldName FIELD_REFERENCE VALUE
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T> Children replaceWith(final SFunction<T,?> fieldName);

    /**
     * $replaceWith阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children replaceWith(final Bson bson);

    /* $replaceWith阶段 end */

    /* =============================================================================== */

    /* $sample阶段 start */

    /**
     * $sample阶段
     * <p>Index 的等价绑定仅覆盖 1..2147483647 的精确整数，使用 Int32 编码。
     * 实现调用 size.intValue()，运行时不检查截断或溢出；小数、超范围、非精确转换及自定义 Number
     * 不属于该绑定承诺，BSON Int64/Double/Decimal128 的类型等价性也未声明。</p>
     * @param size 指定数量
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $sample
     * @mongoParam size INTEGER_VALUE VALUE
     * @mongoObjectField size field=size encoding=INT32_EXACT minimum=1 maximum=2147483647
     * @mongoObjectFieldSource size path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=sample(Number);sample(Bson);custom(Bson) mechanism=sample(Number) 经 size.intValue() 调用 Aggregates.sample(int)，完整 Stage 原样加入管道；仅精确 Int32 整数避免截断和溢出。
     * @mongoObjectFieldSource size artifact=org.mongodb:mongodb-driver-core:5.4.0 symbols=com.mongodb.client.model.Aggregates.sample(int) mechanism=Driver 构造 $sample 对象，其 size 字段为 BsonInt32(size)。
     * @mongoObjectFieldSource size path=https://www.mongodb.com/docs/manual/reference/operator/aggregation/sample/ symbols=$sample.size mechanism=MongoDB 要求 size 为大于等于 1 的整数；上界来自此 Java API 的 Int32 编码边界。
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    Children sample(final Number size);

    /**
     * $sample阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children sample(final Bson bson);

    /* $sample阶段 end */

    /* =============================================================================== */

    /* $setWindowFields阶段 start */

    /**
     * $setWindowFields阶段，可以使用{@link WindowOutputFields}构建WindowOutputField
     * 创建一个 {@code $setWindowFields} 管道阶段，允许使用窗口运算符.
     * 此阶段对输入文档进行分区，类似于 {@link #group(Object, List) $group} 管道阶段,可选择对它们进行排序，
     * 通过计算每个指定的 {@linkplain Window windows} 上的窗口函数来计算文档中的字段函数，并输出文档。
     * 与{@code $group}管道阶段的重要区别在于，属于同一分区或窗口的文档不会折叠成单个文档.
     *
     * @param partitionBy 可选的数据分区，如 {@link #group(Object, List)} 中的 {@code id} 指定.
     *                    如果{@code null}，则所有文档属于同一分区.
     * @param sortBy 排序依据的字段。语法与 {@link #sort(Bson)} 中的 {@code sort} 相同（请参阅 {@link com.mongoplus.aggregate.pipeline.Sorts}）.
     *               某些函数需要排序，某些窗口可能需要排序（有关更多详细信息，请参阅{@link Windows}）.
     *               排序仅用于计算窗口函数，并不保证输出文档的排序.
     * @param output {@linkplain WindowOutputField 窗口计算}.
     * @param moreOutput 更多{@linkplain WindowOutputField 窗口计算}.
     * @param <TExpression> {@code partitionBy} 表达式类型.
     * @return {@code $setWindowFields} 管道阶段.
     * @author anwen
     *
     * @mongoStage $setWindowFields
     * @mongoParam partitionBy PIPELINE_EXPRESSION VALUE
     * @mongoParam sortBy SORT_SPECIFICATION VALUE
     */
    <TExpression> Children setWindowFields(@Nullable final TExpression partitionBy, @Nullable final Bson sortBy,
                                                     final WindowOutputField output, final WindowOutputField... moreOutput);

    /**
     * $setWindowFields阶段，可以使用{@link WindowOutputFields}构建WindowOutputField
     * 创建一个 {@code $setWindowFields} 管道阶段，允许使用窗口运算符.
     * 此阶段对输入文档进行分区，类似于 {@link #group(Object, List) $group} 管道阶段,
     * 可选择对它们进行排序, 通过计算每个指定的 {@linkplain Window windows} 上的窗口函数来计算文档中的字段功能,
     * 并输出文档。与 {@code $group} 管道阶段的重要区别在于,属于同一分区或窗口的文档不会折叠成一个文档.
     *
     * @param partitionBy 数据的可选分区指定为 {@link #group(Object, List)} 中的 {@code id}。如果{@code null}，则所有文档属于同一个分区.
     * @param sortBy 排序依据的字段。语法与 {@link #sort(Bson)} 中的 {@code sort} 相同（请参阅 {@link com.mongoplus.aggregate.pipeline.Sorts}）.
     *               某些函数需要排序，某些窗口可能需要排序（有关更多详细信息，请参阅{@link Windows}）.
     *               排序仅用于计算窗口函数，并不保证输出文档的排序.
     * @param output {@linkplain WindowOutputField 窗口计算}的列表.
     * 指定空列表不是错误，但生成的阶段不会执行任何有用的操作.
     * @param <TExpression> {@code partitionBy} 表达式类型.
     * @return {@code $setWindowFields} 管道阶段.
     * @author anwen
     *
     * @mongoStage $setWindowFields
     * @mongoParam partitionBy PIPELINE_EXPRESSION VALUE
     * @mongoParam sortBy SORT_SPECIFICATION VALUE
     */
    <TExpression> Children setWindowFields(@Nullable final TExpression partitionBy, @Nullable final Bson sortBy,
                                                     final Iterable<? extends WindowOutputField> output);

    /**
     * $setWindowFields阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children setWindowFields(Bson bson);

    /* $setWindowFields阶段 end */

    /* =============================================================================== */

    /* $densify阶段 start */

    /**
     * $densify阶段
     * @param field 字段
     * @param range 范围 指定如何密集化数据的对象
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $densify
     * @mongoParam field FIELD_NAME VALUE
     */
    Children densify(final String field, final DensifyRange range);

    /**
     * $densify阶段
     * @param field 字段
     * @param range 范围 指定如何密集化数据的对象
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $densify
     * @mongoParam field FIELD_NAME VALUE
     */
    <T> Children densify(final SFunction<T,?> field, final DensifyRange range);

    /**
     * $desify阶段
     * @param field 字段
     * @param range 范围 制定如何密集化数据的 对象
     * @param options 表示聚合管道的$densify管道阶段的可选字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $densify
     * @mongoParam field FIELD_NAME VALUE
     */
    Children densify(final String field, final DensifyRange range, final DensifyOptions options);

    /**
     * $desify阶段
     * @param field 字段
     * @param range 范围 制定如何密集化数据的 对象
     * @param options 表示聚合管道的$densify管道阶段的可选字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $densify
     * @mongoParam field FIELD_NAME VALUE
     */
    <T> Children densify(final SFunction<T,?> field, final DensifyRange range, final DensifyOptions options);

    /**
     * $desify阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children densify(final Bson bson);

    /* $densify阶段 end */

    /* =============================================================================== */

    /* $fill阶段 start */

    /**
     * $fill阶段
     * @param options 填充选项
     * @param output {@link FillOutputField}，可以使用{@link com.mongoplus.aggregate.pipeline.FillField}
     * @param moreOutput {@link FillOutputField}，可以使用{@link com.mongoplus.aggregate.pipeline.FillField}
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $fill
     */
    Children fill(final FillOptions options, final FillOutputField output, final FillOutputField... moreOutput);

    /**
     * $fill阶段
     * @param options 填充选项
     * @param output {@link FillOutputField}，可以使用{@link com.mongoplus.aggregate.pipeline.FillField}
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $fill
     */
    Children fill(final FillOptions options, final Iterable<? extends FillOutputField> output);

    /**
     * $fill阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children fill(final Bson bson);

    /* $fill阶段 end */

    /* =============================================================================== */

    /* $unset阶段 start */

    /**
     * $unset阶段
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unset
     * @mongoParam field FIELD_NAME ELEMENT
     * @mongoStageValue field encoding=SINGLETON_SCALAR_ELSE_ARRAY minimumSize=1 duplicates=REJECT singleton=VALUE_TO_ELEMENT
     * @mongoStageValueSource field path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=unset(String...);unset(List);custom(Bson) mechanism=varargs 转 List；一个 String 输出 BsonString，零个或多个输出 BsonArray；保留顺序和重复字段，不增删美元前缀；标量字段需提升为单元素容器。
     * @mongoStageValueSource field reference=https://raw.githubusercontent.com/mongodb/mongo/v8.0/src/mongo/db/pipeline/document_source_project.cpp symbols=DocumentSourceProject.createFromBson;buildExclusionProjectionSpecification mechanism=服务端只接受字符串或非空字符串数组，Core 能编码空数组但该输入不允许合法绑定。
     * @mongoStageValueSource field reference=https://raw.githubusercontent.com/mongodb/mongo/v8.0/src/mongo/db/query/projection_parser.cpp symbols=addNodeAtPathHelper mechanism=重复字段及父子路径冲突触发服务端 path collision；绑定不能去重修复，字段路径仍需服务端校验。
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoCandidate operation=STAGE_VALUE
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=unset;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     */
    Children unset(final String... field);

    /**
     * $unset阶段
     * @param field 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unset
     * @mongoParam field FIELD_NAME ELEMENT
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    @SuppressWarnings("unchecked")
    <T> Children unset(final SFunction<T,?>... field);

    /**
     * $unset阶段
     * @param fields 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unset
     * @mongoParam fields FIELD_NAME ELEMENT
     * @mongoStageValue fields encoding=SINGLETON_SCALAR_ELSE_ARRAY minimumSize=1 duplicates=REJECT singleton=VALUE_TO_ELEMENT
     * @mongoStageValueSource fields path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=unset(List);unset(Bson);custom(Bson) mechanism=真实元素类型为 String；一个元素输出 BsonString，零个或多个输出 BsonArray；保留顺序和重复字段；数组直接收集为 List，标量字段需提升为单元素 List。
     * @mongoStageValueSource fields reference=https://raw.githubusercontent.com/mongodb/mongo/v8.0/src/mongo/db/pipeline/document_source_project.cpp symbols=DocumentSourceProject.createFromBson;buildExclusionProjectionSpecification mechanism=服务端只接受字符串或非空字符串数组，Core 能编码空数组但该输入不允许合法绑定。
     * @mongoStageValueSource fields reference=https://raw.githubusercontent.com/mongodb/mongo/v8.0/src/mongo/db/query/projection_parser.cpp symbols=addNodeAtPathHelper mechanism=重复字段及父子路径冲突触发服务端 path collision；绑定不能去重修复，字段路径仍需服务端校验。
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     * @mongoCandidate operation=STAGE_VALUE
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java symbols=unset;custom mechanism=当前输入快照及codec下构造一次，保留参数值与顺序；公开返回receiver且只追加一个Stage。
     */
    Children unset(final List<String> fields);

    /**
     * $unset阶段
     * @param fields 字段
     * @return {@link Children}
     * @author anwen
     *
     * @mongoStage $unset
     * @mongoParam fields FIELD_NAME ELEMENT
     * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
     */
    <T> Children unsetLambda(final List<SFunction<T,?>> fields);

    /**
     * $unset阶段，如果MongoPlus封装的条件未满足该阶段的需求，请自行构建Bson
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children unset(final Bson bson);

    /**
     * 如果缺少管道，请使用该方法构建
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children custom(final Bson bson);

}
