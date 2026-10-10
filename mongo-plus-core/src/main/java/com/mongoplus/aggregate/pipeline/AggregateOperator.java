package com.mongoplus.aggregate.pipeline;

import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.enums.QueryOperatorEnum;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;

import static com.mongoplus.enums.CommonOperators.*;

/**
 * 聚合操作符
 *
 * @author anwen
 */
public class AggregateOperator {

    /**
     * 构造聚合相等表达式，两个操作数按声明顺序原样写入数组。
     * {@code "$field"} 表示字段引用，{@code "$$variable"} 表示变量引用；变量绑定由调用处的作用域决定。
     * 普通字符串、数值等 literal 由 BSON codec 编码，嵌套表达式可传入 Bson；不自动加前缀或转义 literal。
     *
     * @param left 左操作数
     * @param right 右操作数
     * @return 表示 {@code $eq} 聚合表达式的 Bson，服务端求值结果为布尔值
     * @mongoExpression $eq
     * @mongoExpressionShape ARRAY
     * @mongoParam left PIPELINE_EXPRESSION VALUE
     * @mongoParam right PIPELINE_EXPRESSION VALUE
     * @mongoComposition PIPELINE_EXPRESSION + PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION
     * @mongoCandidate operation=EXPRESSION_ARGUMENTS collectionCodec=org.bson.codecs.CollectionCodec resultJava=org.bson.Document
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java symbols=eq(Object,Object) mechanism=Document保存EQ的expression键；Arrays.asList按声明顺序保留两个operand及null，不提前计算。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java symbols=writeValue mechanism=非null元素按实际Java类型从registry取得codec；容器与每个叶子codec均需独立证明。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/AbstractCollectionCodec.java symbols=encode mechanism=按iteration顺序编码数组元素；null直接写BSON_NULL，不查询null的runtime类型。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/ValueCodecProvider.java symbols=addCodecs;get mechanism=Driver5.4默认值codec包含String、Integer、Long、Double、Boolean及Decimal128；不能由Object参数推断其他类型codec。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java symbols=encode;writeValue mechanism=Document按entry顺序写入expression键和值；非null值按runtime类型委托registry，嵌套Document使用DocumentCodec。
     * @mongoCandidateSource reference=https://www.mongodb.com/docs/manual/reference/operator/aggregation/eq/ symbols=$eq mechanism=聚合数组语法包含两个有序expression，服务器求值为布尔值。
     */
    public static Bson eq(Object left, Object right) {
        return new Document(QueryOperatorEnum.EQ.getOperatorValue(), Arrays.asList(left, right));
    }

    /**
     * 构造聚合不相等表达式，两个操作数按 left、right 顺序编码，不在 Java 中求值。
     * 字段引用使用 {@code "$field"}，变量引用使用 {@code "$$variable"}；
     * 普通字符串、null 和数值原样交给 codec，嵌套值须独立证明为聚合表达式。
     * Object 参数不保证任意 Java 类型存在 codec，也不证明服务端运行时类型合法。
     *
     * @param left 左操作数
     * @param right 右操作数
     * @return 聚合表达式 BSON 文档，不是完整 Stage
     * @mongoExpression $ne
     * @mongoExpressionShape ARRAY
     * @mongoParam left PIPELINE_EXPRESSION VALUE
     * @mongoParam right PIPELINE_EXPRESSION VALUE
     * @mongoComposition PIPELINE_EXPRESSION + PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION
     * @mongoCandidate operation=EXPRESSION_ARGUMENTS collectionCodec=org.bson.codecs.CollectionCodec resultJava=org.bson.Document
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java symbols=ne(Object,Object) mechanism=Document保存当前expression键；Arrays.asList按声明顺序保留两个operand及null，不提前计算。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java symbols=writeValue mechanism=非null元素按实际Java类型从registry取得codec；容器与每个叶子codec均需独立证明。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/AbstractCollectionCodec.java symbols=encode mechanism=按iteration顺序编码数组元素；null直接写BSON_NULL，不查询null的runtime类型。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/ValueCodecProvider.java symbols=addCodecs;get mechanism=Driver5.4默认值codec包含String、Integer、Long、Double、Boolean及Decimal128；不能由Object参数推断其他类型codec。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java symbols=encode;writeValue mechanism=Document按entry顺序写入expression键和值；非null值按runtime类型委托registry，嵌套Document使用DocumentCodec。
     * @mongoCandidateSource reference=https://www.mongodb.com/docs/manual/reference/operator/aggregation/ne/ symbols=$ne mechanism=聚合数组语法包含两个有序expression；服务器负责运算与运行时类型约束。
     */
    public static Bson ne(Object left, Object right) {
        return new Document("$ne", Arrays.asList(left, right));
    }

    /**
     * 构造聚合大于表达式，两个操作数按 left、right 顺序编码，不在 Java 中求值。
     * 字段引用使用 {@code "$field"}，变量引用使用 {@code "$$variable"}；
     * 普通字符串、null 和数值原样交给 codec，嵌套值须独立证明为聚合表达式。
     * Object 参数不保证任意 Java 类型存在 codec，也不证明服务端运行时类型合法。
     *
     * @param left 左操作数
     * @param right 右操作数
     * @return 聚合表达式 BSON 文档，不是完整 Stage
     * @mongoExpression $gt
     * @mongoExpressionShape ARRAY
     * @mongoParam left PIPELINE_EXPRESSION VALUE
     * @mongoParam right PIPELINE_EXPRESSION VALUE
     * @mongoComposition PIPELINE_EXPRESSION + PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION
     * @mongoCandidate operation=EXPRESSION_ARGUMENTS collectionCodec=org.bson.codecs.CollectionCodec resultJava=org.bson.Document
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java symbols=gt(Object,Object) mechanism=Document保存当前expression键；Arrays.asList按声明顺序保留两个operand及null，不提前计算。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java symbols=writeValue mechanism=非null元素按实际Java类型从registry取得codec；容器与每个叶子codec均需独立证明。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/AbstractCollectionCodec.java symbols=encode mechanism=按iteration顺序编码数组元素；null直接写BSON_NULL，不查询null的runtime类型。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/ValueCodecProvider.java symbols=addCodecs;get mechanism=Driver5.4默认值codec包含String、Integer、Long、Double、Boolean及Decimal128；不能由Object参数推断其他类型codec。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java symbols=encode;writeValue mechanism=Document按entry顺序写入expression键和值；非null值按runtime类型委托registry，嵌套Document使用DocumentCodec。
     * @mongoCandidateSource reference=https://www.mongodb.com/docs/manual/reference/operator/aggregation/gt/ symbols=$gt mechanism=聚合数组语法包含两个有序expression；服务器负责运算与运行时类型约束。
     */
    public static Bson gt(Object left, Object right) {
        return new Document("$gt", Arrays.asList(left, right));
    }

    /**
     * 构造聚合大于或等于表达式，两个操作数按 left、right 顺序编码，不在 Java 中求值。
     * 字段引用使用 {@code "$field"}，变量引用使用 {@code "$$variable"}；
     * 普通字符串、null 和数值原样交给 codec，嵌套值须独立证明为聚合表达式。
     * Object 参数不保证任意 Java 类型存在 codec，也不证明服务端运行时类型合法。
     *
     * @param left 左操作数
     * @param right 右操作数
     * @return 聚合表达式 BSON 文档，不是完整 Stage
     * @mongoExpression $gte
     * @mongoExpressionShape ARRAY
     * @mongoParam left PIPELINE_EXPRESSION VALUE
     * @mongoParam right PIPELINE_EXPRESSION VALUE
     * @mongoComposition PIPELINE_EXPRESSION + PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION
     * @mongoCandidate operation=EXPRESSION_ARGUMENTS collectionCodec=org.bson.codecs.CollectionCodec resultJava=org.bson.Document
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java symbols=gte(Object,Object) mechanism=Document保存当前expression键；Arrays.asList按声明顺序保留两个operand及null，不提前计算。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java symbols=writeValue mechanism=非null元素按实际Java类型从registry取得codec；容器与每个叶子codec均需独立证明。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/AbstractCollectionCodec.java symbols=encode mechanism=按iteration顺序编码数组元素；null直接写BSON_NULL，不查询null的runtime类型。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/ValueCodecProvider.java symbols=addCodecs;get mechanism=Driver5.4默认值codec包含String、Integer、Long、Double、Boolean及Decimal128；不能由Object参数推断其他类型codec。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java symbols=encode;writeValue mechanism=Document按entry顺序写入expression键和值；非null值按runtime类型委托registry，嵌套Document使用DocumentCodec。
     * @mongoCandidateSource reference=https://www.mongodb.com/docs/manual/reference/operator/aggregation/gte/ symbols=$gte mechanism=聚合数组语法包含两个有序expression；服务器负责运算与运行时类型约束。
     */
    public static Bson gte(Object left, Object right) {
        return new Document("$gte", Arrays.asList(left, right));
    }

    /**
     * 构造聚合小于表达式，两个操作数按 left、right 顺序编码，不在 Java 中求值。
     * 字段引用使用 {@code "$field"}，变量引用使用 {@code "$$variable"}；
     * 普通字符串、null 和数值原样交给 codec，嵌套值须独立证明为聚合表达式。
     * Object 参数不保证任意 Java 类型存在 codec，也不证明服务端运行时类型合法。
     *
     * @param left 左操作数
     * @param right 右操作数
     * @return 聚合表达式 BSON 文档，不是完整 Stage
     * @mongoExpression $lt
     * @mongoExpressionShape ARRAY
     * @mongoParam left PIPELINE_EXPRESSION VALUE
     * @mongoParam right PIPELINE_EXPRESSION VALUE
     * @mongoComposition PIPELINE_EXPRESSION + PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION
     * @mongoCandidate operation=EXPRESSION_ARGUMENTS collectionCodec=org.bson.codecs.CollectionCodec resultJava=org.bson.Document
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java symbols=lt(Object,Object) mechanism=Document保存当前expression键；Arrays.asList按声明顺序保留两个operand及null，不提前计算。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java symbols=writeValue mechanism=非null元素按实际Java类型从registry取得codec；容器与每个叶子codec均需独立证明。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/AbstractCollectionCodec.java symbols=encode mechanism=按iteration顺序编码数组元素；null直接写BSON_NULL，不查询null的runtime类型。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/ValueCodecProvider.java symbols=addCodecs;get mechanism=Driver5.4默认值codec包含String、Integer、Long、Double、Boolean及Decimal128；不能由Object参数推断其他类型codec。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java symbols=encode;writeValue mechanism=Document按entry顺序写入expression键和值；非null值按runtime类型委托registry，嵌套Document使用DocumentCodec。
     * @mongoCandidateSource reference=https://www.mongodb.com/docs/manual/reference/operator/aggregation/lt/ symbols=$lt mechanism=聚合数组语法包含两个有序expression；服务器负责运算与运行时类型约束。
     */
    public static Bson lt(Object left, Object right) {
        return new Document("$lt", Arrays.asList(left, right));
    }

    /**
     * 构造聚合小于或等于表达式，两个操作数按 left、right 顺序编码，不在 Java 中求值。
     * 字段引用使用 {@code "$field"}，变量引用使用 {@code "$$variable"}；
     * 普通字符串、null 和数值原样交给 codec，嵌套值须独立证明为聚合表达式。
     * Object 参数不保证任意 Java 类型存在 codec，也不证明服务端运行时类型合法。
     *
     * @param left 左操作数
     * @param right 右操作数
     * @return 聚合表达式 BSON 文档，不是完整 Stage
     * @mongoExpression $lte
     * @mongoExpressionShape ARRAY
     * @mongoParam left PIPELINE_EXPRESSION VALUE
     * @mongoParam right PIPELINE_EXPRESSION VALUE
     * @mongoComposition PIPELINE_EXPRESSION + PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION
     * @mongoCandidate operation=EXPRESSION_ARGUMENTS collectionCodec=org.bson.codecs.CollectionCodec resultJava=org.bson.Document
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java symbols=lte(Object,Object) mechanism=Document保存当前expression键；Arrays.asList按声明顺序保留两个operand及null，不提前计算。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java symbols=writeValue mechanism=非null元素按实际Java类型从registry取得codec；容器与每个叶子codec均需独立证明。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/AbstractCollectionCodec.java symbols=encode mechanism=按iteration顺序编码数组元素；null直接写BSON_NULL，不查询null的runtime类型。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/ValueCodecProvider.java symbols=addCodecs;get mechanism=Driver5.4默认值codec包含String、Integer、Long、Double、Boolean及Decimal128；不能由Object参数推断其他类型codec。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java symbols=encode;writeValue mechanism=Document按entry顺序写入expression键和值；非null值按runtime类型委托registry，嵌套Document使用DocumentCodec。
     * @mongoCandidateSource reference=https://www.mongodb.com/docs/manual/reference/operator/aggregation/lte/ symbols=$lte mechanism=聚合数组语法包含两个有序expression；服务器负责运算与运行时类型约束。
     */
    public static Bson lte(Object left, Object right) {
        return new Document("$lte", Arrays.asList(left, right));
    }

    /**
     * 构造聚合减法表达式，两个操作数按 left、right 顺序编码，不在 Java 中求值。
     * 字段引用使用 {@code "$field"}，变量引用使用 {@code "$$variable"}；
     * 普通字符串、null 和数值原样交给 codec，嵌套值须独立证明为聚合表达式。
     * Object 参数不保证任意 Java 类型存在 codec，也不证明服务端运行时类型合法。
     *
     * @param left 左操作数
     * @param right 右操作数
     * @return 聚合表达式 BSON 文档，不是完整 Stage
     * @mongoExpression $subtract
     * @mongoExpressionShape ARRAY
     * @mongoParam left PIPELINE_EXPRESSION VALUE
     * @mongoParam right PIPELINE_EXPRESSION VALUE
     * @mongoComposition PIPELINE_EXPRESSION + PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION
     * @mongoCandidate operation=EXPRESSION_ARGUMENTS collectionCodec=org.bson.codecs.CollectionCodec resultJava=org.bson.Document
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java symbols=subtract(Object,Object) mechanism=Document保存当前expression键；Arrays.asList按声明顺序保留两个operand及null，不提前计算。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java symbols=writeValue mechanism=非null元素按实际Java类型从registry取得codec；容器与每个叶子codec均需独立证明。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/AbstractCollectionCodec.java symbols=encode mechanism=按iteration顺序编码数组元素；null直接写BSON_NULL，不查询null的runtime类型。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/ValueCodecProvider.java symbols=addCodecs;get mechanism=Driver5.4默认值codec包含String、Integer、Long、Double、Boolean及Decimal128；不能由Object参数推断其他类型codec。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java symbols=encode;writeValue mechanism=Document按entry顺序写入expression键和值；非null值按runtime类型委托registry，嵌套Document使用DocumentCodec。
     * @mongoCandidateSource reference=https://www.mongodb.com/docs/manual/reference/operator/aggregation/subtract/ symbols=$subtract mechanism=聚合数组语法包含两个有序expression；服务器负责运算与运行时类型约束。
     */
    public static Bson subtract(Object left, Object right) {
        return new Document("$subtract", Arrays.asList(left, right));
    }

    /**
     * 构造聚合除法表达式，两个操作数按 left、right 顺序编码，不在 Java 中求值。
     * 字段引用使用 {@code "$field"}，变量引用使用 {@code "$$variable"}；
     * 普通字符串、null 和数值原样交给 codec，嵌套值须独立证明为聚合表达式。
     * Object 参数不保证任意 Java 类型存在 codec，也不证明服务端运行时类型合法。
     *
     * @param left 左操作数
     * @param right 右操作数
     * @return 聚合表达式 BSON 文档，不是完整 Stage
     * @mongoExpression $divide
     * @mongoExpressionShape ARRAY
     * @mongoParam left PIPELINE_EXPRESSION VALUE
     * @mongoParam right PIPELINE_EXPRESSION VALUE
     * @mongoComposition PIPELINE_EXPRESSION + PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION
     * @mongoCandidate operation=EXPRESSION_ARGUMENTS collectionCodec=org.bson.codecs.CollectionCodec resultJava=org.bson.Document
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java symbols=divide(Object,Object) mechanism=Document保存当前expression键；Arrays.asList按声明顺序保留两个operand及null，不提前计算。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java symbols=writeValue mechanism=非null元素按实际Java类型从registry取得codec；容器与每个叶子codec均需独立证明。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/AbstractCollectionCodec.java symbols=encode mechanism=按iteration顺序编码数组元素；null直接写BSON_NULL，不查询null的runtime类型。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/ValueCodecProvider.java symbols=addCodecs;get mechanism=Driver5.4默认值codec包含String、Integer、Long、Double、Boolean及Decimal128；不能由Object参数推断其他类型codec。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java symbols=encode;writeValue mechanism=Document按entry顺序写入expression键和值；非null值按runtime类型委托registry，嵌套Document使用DocumentCodec。
     * @mongoCandidateSource reference=https://www.mongodb.com/docs/manual/reference/operator/aggregation/divide/ symbols=$divide mechanism=聚合数组语法包含两个有序expression；服务器负责运算与运行时类型约束。
     */
    public static Bson divide(Object left, Object right) {
        return new Document("$divide", Arrays.asList(left, right));
    }

    /**
     * 构造聚合逻辑与表达式，保留零个、一个或多个操作数的数组结构。
     * 不在 Java 中计算真值；null 元素保留为 BSON null，null 参数数组拒绝。
     * 字段、变量、普通字符串和嵌套表达式沿用表达式参数契约，实际 codec 与变量作用域须独立证明。
     *
     * @param expressions 按顺序编码的表达式；可为空数组，不能为 null 数组
     * @return 聚合表达式 BSON 文档，不是完整 Stage
     * @mongoExpression $and
     * @mongoExpressionShape ARRAY
     * @mongoParam expressions PIPELINE_EXPRESSION ELEMENT
     * @mongoComposition PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION
     * @mongoCandidate operation=EXPRESSION_ARGUMENTS collectionCodec=org.bson.codecs.CollectionCodec resultJava=org.bson.Document
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java symbols=and(Object...) mechanism=非null的Object数组经Arrays.asList按序保存到Document；空数组仍编码为空BSON数组，null元素不删除。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java symbols=writeValue mechanism=非null元素按实际Java类型从registry取得codec；容器与每个叶子codec均需独立证明。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/AbstractCollectionCodec.java symbols=encode mechanism=按iteration顺序编码数组元素；null直接写BSON_NULL，不查询null的runtime类型。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/ValueCodecProvider.java symbols=addCodecs;get mechanism=Driver5.4默认值codec包含String、Integer、Long、Double、Boolean及Decimal128；不能由Object参数推断其他类型codec。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java symbols=encode;writeValue mechanism=Document按entry顺序写入expression键和值；非null值按runtime类型委托registry，嵌套Document使用DocumentCodec。
     * @mongoCandidateSource reference=https://www.mongodb.com/docs/manual/reference/operator/aggregation/and/ symbols=$and mechanism=聚合语法接收变长expression数组，包含零个或单个元素；服务器负责真值与错误传播。
     */
    public static Bson and(Object... expressions) {
        java.util.Objects.requireNonNull(expressions, "expressions");
        return new Document("$and", Arrays.asList(expressions));
    }

    /**
     * 构造聚合逻辑或表达式，保留零个、一个或多个操作数的数组结构。
     * 不在 Java 中计算真值；null 元素保留为 BSON null，null 参数数组拒绝。
     * 字段、变量、普通字符串和嵌套表达式沿用表达式参数契约，实际 codec 与变量作用域须独立证明。
     *
     * @param expressions 按顺序编码的表达式；可为空数组，不能为 null 数组
     * @return 聚合表达式 BSON 文档，不是完整 Stage
     * @mongoExpression $or
     * @mongoExpressionShape ARRAY
     * @mongoParam expressions PIPELINE_EXPRESSION ELEMENT
     * @mongoComposition PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION
     * @mongoCandidate operation=EXPRESSION_ARGUMENTS collectionCodec=org.bson.codecs.CollectionCodec resultJava=org.bson.Document
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java symbols=or(Object...) mechanism=非null的Object数组经Arrays.asList按序保存到Document；空数组仍编码为空BSON数组，null元素不删除。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java symbols=writeValue mechanism=非null元素按实际Java类型从registry取得codec；容器与每个叶子codec均需独立证明。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/AbstractCollectionCodec.java symbols=encode mechanism=按iteration顺序编码数组元素；null直接写BSON_NULL，不查询null的runtime类型。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/ValueCodecProvider.java symbols=addCodecs;get mechanism=Driver5.4默认值codec包含String、Integer、Long、Double、Boolean及Decimal128；不能由Object参数推断其他类型codec。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java symbols=encode;writeValue mechanism=Document按entry顺序写入expression键和值；非null值按runtime类型委托registry，嵌套Document使用DocumentCodec。
     * @mongoCandidateSource reference=https://www.mongodb.com/docs/manual/reference/operator/aggregation/or/ symbols=$or mechanism=聚合语法接收变长expression数组，包含零个或单个元素；服务器负责真值与错误传播。
     */
    public static Bson or(Object... expressions) {
        java.util.Objects.requireNonNull(expressions, "expressions");
        return new Document("$or", Arrays.asList(expressions));
    }

    /**
     * 构造聚合逻辑非表达式，单个输入始终保留一层操作数数组，不展开输入本身。
     * null 输入编码为单个 BSON null；不在 Java 中计算真值或验证服务端类型。
     * 嵌套表达式、变量作用域和实际 codec 须独立证明。
     *
     * @param expression 唯一操作数
     * @return 具有单元素数组的聚合表达式 BSON 文档，不是完整 Stage
     * @mongoExpression $not
     * @mongoExpressionShape ARRAY
     * @mongoParam expression PIPELINE_EXPRESSION VALUE
     * @mongoComposition PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION
     * @mongoCandidate operation=EXPRESSION_ARGUMENTS collectionCodec=org.bson.codecs.CollectionCodec resultJava=org.bson.Document
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java symbols=not(Object) mechanism=Document保存当前expression键；Collections.singletonList只包装一个operand，保留null或嵌套数组，不使用varargs展开。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java symbols=writeValue mechanism=非null元素按实际Java类型从registry取得codec；容器与每个叶子codec均需独立证明。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/AbstractCollectionCodec.java symbols=encode mechanism=按iteration顺序编码数组元素；null直接写BSON_NULL，不查询null的runtime类型。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/ValueCodecProvider.java symbols=addCodecs;get mechanism=Driver5.4默认值codec包含String、Integer、Long、Double、Boolean及Decimal128；不能由Object参数推断其他类型codec。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java symbols=encode;writeValue mechanism=Document按entry顺序写入expression键和值；非null值按runtime类型委托registry，嵌套Document使用DocumentCodec。
     * @mongoCandidateSource reference=https://www.mongodb.com/docs/manual/reference/operator/aggregation/not/ symbols=$not mechanism=聚合语法接收一个expression，并保留其外层单元素数组。
     */
    public static Bson not(Object expression) {
        return new Document("$not", java.util.Collections.singletonList(expression));
    }


    /**
     * $concatArrays阶段
     * @param list 多个数组
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     *
     * @mongoExpression $concatArrays
     * @mongoExpressionShape ARRAY
     * @mongoComposition PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION
     * @mongoParam list PIPELINE_EXPRESSION ELEMENT
     */
    public static Bson concatArrays(List<?>... list) {
        return new Document(CONCAT_ARRAYS.getOperator(), Arrays.asList(list));
    }

    /**
     * 拼接数组表达式，操作数可以是字段引用、独立证明的嵌套表达式或数组字面量。
     * 独立方法名避免与 concatArrays(List&lt;?&gt;...) 在空参、null 和 List 实参处产生重载歧义。
     * 不校验服务端数组类型；null 元素按 BSON null 编码，null varargs 容器抛出异常。
     *
     * @param operands 有序表达式操作数
     * @return runtime 为 Document 的聚合表达式
     * @mongoExpression $concatArrays
     * @mongoParam operands PIPELINE_EXPRESSION ELEMENT
     * @mongoExpressionShape ARRAY
     * @mongoComposition PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION
     * @mongoCandidate operation=EXPRESSION_ARGUMENTS collectionCodec=org.bson.codecs.CollectionCodec resultJava=org.bson.Document
     * @mongoCandidateSource path=mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java symbols=concatArraysExpressions mechanism=当前overload保留操作数runtime对象及声明顺序；只构造Document表达式，不在Java求值。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java symbols=encode;writeValue mechanism=Document按插入顺序写入键；null编码BSON_NULL；其余按实际runtime类型向registry查询codec。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/MongoClientSettings.java symbols=DEFAULT_CODEC_REGISTRY mechanism=只审计Driver5.4默认DocumentCodec和CollectionCodec及已知叶子codec；自定义registry或未知runtime对象需要独立严格编码证明。
     * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java symbols=encode;writeValue mechanism=按iteration顺序编码所有元素；容器和各叶子codec独立证明，不能仅凭Bson接口替代runtime对象。
     */
    public static Bson concatArraysExpressions(Object... operands) {
        return new Document(CONCAT_ARRAYS.getOperator(), Arrays.asList(operands));
    }

    /**
     * $concat操作符
     * @author anwen
     *
     * @mongoExpression $concat
     * @mongoParam expression PIPELINE_EXPRESSION ELEMENT
     */
    public static Bson concat(Object... expression) {
        return concat(Arrays.asList(expression));
    }

    /**
     * $concat操作符
     * @author anwen
     *
     * @mongoExpression $concat
     * @mongoParam expressions PIPELINE_EXPRESSION ELEMENT
     */
    public static Bson concat(List<?> expressions) {
        return new Document(CONCAT.getOperator(), expressions);
    }


    /**
     * $dateTrunc操作符
     *
     * @param field       字段
     *                    <br>
     * @param unit        单位可以是能被解析为下列值的表达式：year、quarter、week、month、day、hour、minute、second、millisecond，与binSize一起指定时间段
     *                    <br>
     * @param binSize     时间数值，以表达式形式指定，必须是非零正数。默认值为 1。
     *                    <br>
     * @param startOfWeek 指定周开始的天，只有当单位是周时可用，缺省为Sunday，startOfWeek可以是一个表达式，但必须能够被解析为：monday (或 mon)、tuesday (或 tue)、wednesday (或 wed)、thursday (或 thu)、friday (或 fri)、saturday (或 sat)、sunday (或 sun)
     *                    <br>
     * @param timezone    执行操作的时区，<tzExpression>必须是能被解析为奥尔森时区标识符格式的字符串或UTC偏移量，如果timezone不指定，返回值显示为UTC
     *
     * @mongoExpression $dateTrunc
     * @mongoParam unit PIPELINE_EXPRESSION VALUE
     * @mongoParam binSize PIPELINE_EXPRESSION VALUE
     * @mongoParam startOfWeek PIPELINE_EXPRESSION VALUE
     * @mongoParam timezone PIPELINE_EXPRESSION VALUE
     */
    public static Bson dateTrunc(SFunction<?, ?> field, String unit, Integer binSize, String startOfWeek, String timezone) {
        return dateTrunc(field.getFieldNameLineOption(), unit, binSize, startOfWeek, timezone);
    }

    /**
     * 构建日期截断表达式。
     *
     * @mongoExpression $dateTrunc
     * @mongoParam field PIPELINE_EXPRESSION VALUE
     * @mongoParam unit PIPELINE_EXPRESSION VALUE
     * @mongoParam binSize PIPELINE_EXPRESSION VALUE
     * @mongoParam startOfWeek PIPELINE_EXPRESSION VALUE
     * @mongoParam timezone PIPELINE_EXPRESSION VALUE
     */
    public static Bson dateTrunc(String field, String unit, Integer binSize, String startOfWeek, String timezone) {
        return new Document(DATE_TRUNC.getOperator(), new MongoPlusDocument() {{
            putIsNotNull("date", field);
            putIsNotNull("unit", unit);
            putIsNotNull("binSize", binSize);
            putIsNotNull("timezone", timezone);
            putIsNotNull("startOfWeek", startOfWeek);
        }});
    }

    /**
     * 构建日期截断表达式。
     *
     * @mongoExpression $dateTrunc
     * @mongoParam field PIPELINE_EXPRESSION VALUE
     * @mongoParam unit PIPELINE_EXPRESSION VALUE
     */
    public static Bson dateTrunc(String field, String unit) {
        return dateTrunc(field, unit, null, null, null);
    }

    /**
     * 构建日期截断表达式。
     *
     * @mongoExpression $dateTrunc
     * @mongoParam unit PIPELINE_EXPRESSION VALUE
     */
    public static Bson dateTrunc(SFunction<?, ?> field, String unit) {
        return dateTrunc(field, unit, null, null, null);
    }
}
