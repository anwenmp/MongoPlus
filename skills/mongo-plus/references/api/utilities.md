# 当前公开API签名：utilities

这些是本次AST提取的声明签名，用于import、泛型和重载选择；不是各方法已经运行的证明。不含方法体，不替代[业务专题](../verification.md)中的真实命令验证。

## com.mongoplus.enums.AggregateEnum · mongo-plus-annotation

`import com.mongoplus.enums.AggregateEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/AggregateEnum.java#L8)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getValue();
枚举项: ADD_FIELDS, SET, BUCKET, BUCKET_AUTO, MATCH, PROJECT, SORT, LOOKUP, FACET, GRAPH_LOOKUP, GROUP, UNION_WITH, UNWIND, MERGE, REPLACE_ROOT, MERGE_OBJECTS, EACH, POSITION, SLICE
```

## com.mongoplus.enums.AggregateOptionsEnum · mongo-plus-annotation

`import com.mongoplus.enums.AggregateOptionsEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/AggregateOptionsEnum.java#L8)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getOptions();
public static AggregateOptionsEnum getByOptions(String options);
枚举项: ALLOW_DISK_USE, BATCH_SIZE, COLLATION, MAX_TIME_MS, MAX_AWAIT_TIME_MS, BYPASS_DOCUMENT_VALIDATION, COMMENT, COMMENT_STR, HINT, HINT_STR, LET
```

## com.mongoplus.enums.AlgorithmEnum · mongo-plus-annotation

`import com.mongoplus.enums.AlgorithmEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/AlgorithmEnum.java#L8)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getAlgorithm();
枚举项: MD5_32, MD5_16, BASE64, AES, RSA, SM2, SM3, SM4, PBEWithMD5AndDES, PBEWithMD5AndTripleDES, PBEWithSHA1AndDESede, PBEWithSHA1AndRC2_40
```

## com.mongoplus.enums.Analyzer · mongo-plus-annotation

`import com.mongoplus.enums.Analyzer;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/Analyzer.java#L9)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getAnalyzer();
枚举项: DEFAULT, STANDARD, SIMPLE, WHITESPACE, KEYWORD, ARABIC, ARMENIAN, BASQUE, BENGALI, BRAZILIAN, BULGARIAN, CATALAN, CHINESE, CJK, CZECH, DANISH, DUTCH, ENGLISH, FINNISH, FRENCH, GALICIAN, GERMAN, GREEK, HINDI, HUNGARIAN, INDONESIAN, IRISH, ITALIAN, JAPANESE, KOREAN, KUROMOJI, LATVIAN, LITHUANIAN, MORFOLOGIK, NORI, NORWEGIAN, PERSIAN, POLISH, PORTUGUESE, ROMANIAN, RUSSIAN, SMARTCN, SORANI, SPANISH, SWEDISH, THAI, TURKISH, UKRAINIAN
```

## com.mongoplus.enums.BannerType · mongo-plus-annotation

`import com.mongoplus.enums.BannerType;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/BannerType.java#L3)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
枚举项: DEFAULT, IKUN
```

## com.mongoplus.enums.CollectionNameConvertEnum · mongo-plus-annotation

`import com.mongoplus.enums.CollectionNameConvertEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/CollectionNameConvertEnum.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
枚举项: ALL_CHAR_LOWERCASE, FIRST_CHAR_LOWERCASE, CLASS_NAME, CAMEL_TO_UNDERLINE
```

## com.mongoplus.enums.CommandEnum · mongo-plus-annotation

`import com.mongoplus.enums.CommandEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommandEnum.java#L8)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getCommand();
枚举项: FIND, INSERT, DELETE, UPDATE, AGGREGATE, COUNT, CREATE_INDEXES
```

## com.mongoplus.enums.CommandOperate · mongo-plus-annotation

`import com.mongoplus.enums.CommandOperate;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommandOperate.java#L10)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import java.util.Arrays;
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getOperate();
public static CommandOperate getCommandOperate(String operate);
枚举项: FIND, AGGREGATE
```

## com.mongoplus.enums.CommonOperators · mongo-plus-annotation

`import com.mongoplus.enums.CommonOperators;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommonOperators.java#L7)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getOperator();
枚举项: OPTIONS, ABS, TO_DATE, DATE_TO_STRING, MULTIPLY, COND, DATE_FROM_STRING, TO_BOOL, TO_DECIMAL, TO_DOUBLE, TO_HASHED_INDEX_KEY, TO_INT, TO_LONG, TO_OBJECT_ID, TO_STRING, SUBSTR_BYTES, IF_NULL, SUM, ADD, CONCAT_ARRAYS, CONCAT, DATE_TRUNC
```

## com.mongoplus.enums.CurrentDateType · mongo-plus-annotation

`import com.mongoplus.enums.CurrentDateType;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/CurrentDateType.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getType();
枚举项: DATE, TIMESTAMP
```

## com.mongoplus.enums.DesensitizationTypeEnum · mongo-plus-annotation

`import com.mongoplus.enums.DesensitizationTypeEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/DesensitizationTypeEnum.java#L7)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
枚举项: CUSTOM, USER_ID, CHINESE_NAME, ID_CARD, FIXED_PHONE, MOBILE_PHONE, ADDRESS, EMAIL, PASSWORD, CAR_LICENSE, BANK_CARD, IPV4, IPV6, FIRST_MASK, CLEAR_TO_NULL, CLEAR_TO_EMPTY
```

## com.mongoplus.enums.ExecuteMethodEnum · mongo-plus-annotation

`import com.mongoplus.enums.ExecuteMethodEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/ExecuteMethodEnum.java#L10)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import java.util.Arrays;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getMethod();
public static ExecuteMethodEnum getMethod(String method);
枚举项: SAVE, SAVE_ONE, REMOVE, REMOVE_ONE, UPDATE, UPDATE_ONE, QUERY, AGGREGATE, COUNT, ESTIMATED_DOCUMENT_COUNT, BULK_WRITE, FIND_ONE_AND_REMOVE
```

## com.mongoplus.enums.FieldFill · mongo-plus-annotation

`import com.mongoplus.enums.FieldFill;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/FieldFill.java#L7)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
枚举项: DEFAULT, INSERT, UPDATE, INSERT_UPDATE
```

## com.mongoplus.enums.GeoType · mongo-plus-annotation

`import com.mongoplus.enums.GeoType;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/GeoType.java#L7)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getType();
枚举项: SPHERE, TWO_D
```

## com.mongoplus.enums.IdTypeEnum · mongo-plus-annotation

`import com.mongoplus.enums.IdTypeEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/IdTypeEnum.java#L8)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public int getKey();
枚举项: OBJECT_ID, ASSIGN_UUID, ASSIGN_ULID, ASSIGN_ID, AUTO
```

## com.mongoplus.enums.IndexDirection · mongo-plus-annotation

`import com.mongoplus.enums.IndexDirection;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/IndexDirection.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public Integer getValue();
枚举项: ASC, DESC
```

## com.mongoplus.enums.IndexType · mongo-plus-annotation

`import com.mongoplus.enums.IndexType;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/IndexType.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
枚举项: INDEX, COMPOUND_INDEX, HASH_INDEX, TEXT_INDEX, GEO_INDEX
```

## com.mongoplus.enums.LogicDataType · mongo-plus-annotation

`import com.mongoplus.enums.LogicDataType;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/LogicDataType.java#L7)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
枚举项: DEFAULT, STRING, INTEGER, LONG, BOOLEAN
```

## com.mongoplus.enums.MultipleWrite · mongo-plus-annotation

`import com.mongoplus.enums.MultipleWrite;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/MultipleWrite.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
枚举项: SAVE, UPDATE, REMOVE, BULK_WRITE
```

## com.mongoplus.enums.OrderEnum · mongo-plus-annotation

`import com.mongoplus.enums.OrderEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/OrderEnum.java#L7)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public Integer getValue();
枚举项: ASC, DESC
```

## com.mongoplus.enums.PopType · mongo-plus-annotation

`import com.mongoplus.enums.PopType;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/PopType.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public Integer getValue();
枚举项: FIRST, LAST
```

## com.mongoplus.enums.ProjectionEnum · mongo-plus-annotation

`import com.mongoplus.enums.ProjectionEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/ProjectionEnum.java#L8)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public Integer getValue();
枚举项: NONE, DISPLAY
```

## com.mongoplus.enums.QueryOperatorEnum · mongo-plus-annotation

`import com.mongoplus.enums.QueryOperatorEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/QueryOperatorEnum.java#L7)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getValue();
public String getOperatorValue();
public static boolean isQueryOperator(String value);
public static QueryOperatorEnum getQueryOperator(String value);
枚举项: COMBINE, MOD, LT, ELEM_MATCH, TYPE, NOR, NIN, NOT, AND, GTE, EXPR, LTE, ALL, OR, IN, LIKE, EQ, GT, REGEX, NE, TEXT, EXISTS, SET, INC, PUSH, WHERE, SIZE, BITS_ALL_CLEAR, BITS_ALL_SET, BITS_ANY_CLEAR, BITS_ANY_SET, GEO_INTERSECTS, GEO_WITHIN, NEAR, NEAR_SPHERE, GEO_WITHIN_BOX, GEO_WITHIN_CENTER, GEO_WITHIN_CENTER_SPHERE, GEO_WITHIN_POLYGON
```

## com.mongoplus.enums.ReadConcernEnum · mongo-plus-annotation

`import com.mongoplus.enums.ReadConcernEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/ReadConcernEnum.java#L7)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
枚举项: DEFAULT, LOCAL, MAJORITY, LINEARIZABLE, AVAILABLE, SNAPSHOT
```

## com.mongoplus.enums.ReadPreferenceEnum · mongo-plus-annotation

`import com.mongoplus.enums.ReadPreferenceEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/ReadPreferenceEnum.java#L8)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
枚举项: PRIMARY, PRIMARY_PREFERRED, SECONDARY, SECONDARY_PREFERRED, NEAREST
```

## com.mongoplus.enums.RegexOptions · mongo-plus-annotation

`import com.mongoplus.enums.RegexOptions;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/RegexOptions.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getFlag();
枚举项: CASE_INSENSITIVE, MULTILINE, EXTENDED, DOT_ALL, UNICODE
```

## com.mongoplus.enums.SearchFieldType · mongo-plus-annotation

`import com.mongoplus.enums.SearchFieldType;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/SearchFieldType.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getValue();
枚举项: STRING, AUTOCOMPLETE, TOKEN, BOOLEAN, DATE, NUMBER, OBJECT_ID, UUID, DOCUMENT, EMBEDDED_DOCUMENTS, GEO, VECTOR, DATE_FACET, NUMBER_FACET, STRING_FACET, KNN_VECTOR
```

## com.mongoplus.enums.SearchIndexingMethod · mongo-plus-annotation

`import com.mongoplus.enums.SearchIndexingMethod;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/SearchIndexingMethod.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getValue();
枚举项: NONE, HNSW, FLAT
```

## com.mongoplus.enums.SearchIndexOptions · mongo-plus-annotation

`import com.mongoplus.enums.SearchIndexOptions;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/SearchIndexOptions.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getValue();
枚举项: DOCS, FREQS, POSITIONS, OFFSETS
```

## com.mongoplus.enums.SearchNormalizer · mongo-plus-annotation

`import com.mongoplus.enums.SearchNormalizer;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/SearchNormalizer.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getValue();
枚举项: NONE, LOWERCASE
```

## com.mongoplus.enums.SearchNorms · mongo-plus-annotation

`import com.mongoplus.enums.SearchNorms;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/SearchNorms.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getValue();
枚举项: INCLUDE, OMIT
```

## com.mongoplus.enums.SearchQuantization · mongo-plus-annotation

`import com.mongoplus.enums.SearchQuantization;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/SearchQuantization.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getValue();
枚举项: EMPTY, NONE, SCALAR, BINARY
```

## com.mongoplus.enums.SearchRepresentation · mongo-plus-annotation

`import com.mongoplus.enums.SearchRepresentation;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/SearchRepresentation.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getValue();
枚举项: DOUBLE, INT64
```

## com.mongoplus.enums.SearchSimilarity · mongo-plus-annotation

`import com.mongoplus.enums.SearchSimilarity;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/SearchSimilarity.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getValue();
枚举项: NONE, BM25, BOOLEAN, STABLE_TFL
```

## com.mongoplus.enums.SearchSortNoData · mongo-plus-annotation

`import com.mongoplus.enums.SearchSortNoData;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/SearchSortNoData.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getValue();
枚举项: DEFAULT, LOWEST, HIGHEST
```

## com.mongoplus.enums.SearchTokenization · mongo-plus-annotation

`import com.mongoplus.enums.SearchTokenization;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/SearchTokenization.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getValue();
枚举项: NONE, EDGE_GRAM, RIGHT_EDGE_GRAM, N_GRAM
```

## com.mongoplus.enums.SearchVectorSimilarity · mongo-plus-annotation

`import com.mongoplus.enums.SearchVectorSimilarity;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/SearchVectorSimilarity.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getValue();
枚举项: DEFAULT, EUCLIDEAN, COSINE, DOT_PRODUCT
```

## com.mongoplus.enums.SpecialConditionEnum · mongo-plus-annotation

`import com.mongoplus.enums.SpecialConditionEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/SpecialConditionEnum.java#L3)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getCondition();
public String getSubCondition();
枚举项: OR, AND, NOR, SET, INC, PUSH, EACH, POSITION, SLICE, SORT, TYPE, IN, EQ, ELEM_MATCH, REGEX, TEXT, SEARCH
```

## com.mongoplus.enums.TextLanguages · mongo-plus-annotation

`import com.mongoplus.enums.TextLanguages;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/TextLanguages.java#L7)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getLanguage();
枚举项: NONE, DANISH, DUTCH, ENGLISH, FINNISH, FRENCH, GERMAN, HUNGARIAN, ITALIAN, NORWEGIAN, PORTUGUESE, ROMANIAN, RUSSIAN, SPANISH, SWEDISH, TURKISH
```

## com.mongoplus.enums.TypeEnum · mongo-plus-annotation

`import com.mongoplus.enums.TypeEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/TypeEnum.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public int getTypeCode();
枚举项: END_OF_DOCUMENT, DOUBLE, STRING, OBJECT, ARRAY, BINARY_DATA, UNDEFINED, OBJECT_ID, BOOLEAN, DATE, NULL, REGEX, JAVASCRIPT_CODE, SYMBOL, JAVASCRIPT_CODE_WITH_SCOPE, INT32, TIMESTAMP, INT64, DECIMAL128, MIN_KEY, MAX_KEY
```

## com.mongoplus.enums.UpdateConditionEnum · mongo-plus-annotation

`import com.mongoplus.enums.UpdateConditionEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/UpdateConditionEnum.java#L7)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getCondition();
public String getSubCondition();
枚举项: SET, PUSH, INC, CURRENT_DATE, MIN, MAX, MUL, RENAME, UNSET, ADD_TO_SET, POP, PULL, PULL_ALL, SET_ON_INSERT
```

## com.mongoplus.enums.VectorFieldType · mongo-plus-annotation

`import com.mongoplus.enums.VectorFieldType;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/VectorFieldType.java#L4)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
枚举项: VECTOR, FILTER
```

## com.mongoplus.enums.WriteConcernEnum · mongo-plus-annotation

`import com.mongoplus.enums.WriteConcernEnum;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/enums/WriteConcernEnum.java#L7)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
枚举项: ACKNOWLEDGED, UNACKNOWLEDGED, MAJORITY, W1, W2, W3, JOURNALED
```

## com.mongoplus.bson.EmptyDocument · mongo-plus-core

`import com.mongoplus.bson.EmptyDocument;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/bson/EmptyDocument.java#L16)。类型：CLASS；类型参数 ``；extends `Document`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.Document;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Document append(String key, Object value);
@Override public Object put(String key, Object value);
@Override public Object remove(Object key);
@Override public void putAll(Map<? extends String, ?> map);
@Override public void replaceAll(BiFunction<? super String, ? super Object, ?> function);
@Override public boolean remove(Object key, Object value);
@Override public boolean replace(String key, Object oldValue, Object newValue);
@Override public Object replace(String key, Object value);
@Override public Set<Entry<String, Object>> entrySet();
@Override public Collection<Object> values();
@Override public Set<String> keySet();
@Override public void clear();
```

## com.mongoplus.bson.ImmutableDocument · mongo-plus-core

`import com.mongoplus.bson.ImmutableDocument;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/bson/ImmutableDocument.java#L15)。类型：CLASS；类型参数 ``；extends `MongoPlusDocument`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.domain.MongoPlusUnsupportedException;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public ImmutableDocument(Document document);
@Override public <T, R> void put(SFunction<T, R> key, Object value);
@Override public <T, R> void putIsNotNull(String key, Object value);
@Override public <T, V> void put(SFunction<T, ?> key, SFunction<V, ?> value);
@Override public <T, V> void putOption(SFunction<T, ?> key, SFunction<V, ?> value);
@Override public <T, R> void append(SFunction<T, R> key, Object value);
@Override public <T, R> Object remove(SFunction<T, R> key);
@Override public <T, R> Object remove(SFunction<T, R> key, Object value);
@Override public Object put(String key, Object value);
@Override public Object remove(Object key);
@Override public void putAll(Map<? extends String, ?> map);
@Override public void clear();
@Override public Document append(String key, Object value);
@Override public Object putIfAbsent(String key, Object value);
@Override public boolean remove(Object key, Object value);
@Override public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction);
@Override public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction);
@Override public boolean replace(String key, Object oldValue, Object newValue);
@Override public Object replace(String key, Object value);
@Override public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction);
@Override public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction);
```

## com.mongoplus.bson.MongoPlusBasicDBObject · mongo-plus-core

`import com.mongoplus.bson.MongoPlusBasicDBObject;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/bson/MongoPlusBasicDBObject.java#L13)。类型：CLASS；类型参数 ``；extends `BasicDBObject`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.support.SFunction;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public <T, R> void put(SFunction<T, R> key, BasicDBObject value);
public <T, R> void append(SFunction<T, R> key, BasicDBObject value);
public <T, R> void get(SFunction<T, R> key);
public <T, R> void putIsNotNull(String key, Object value);
public <T, V> void put(SFunction<T, ?> key, SFunction<V, ?> value);
public <T, V> void putOption(SFunction<T, ?> key, SFunction<V, ?> value);
public <T, R> boolean containsKey(SFunction<T, R> key);
public <T, R> Object remove(SFunction<T, R> key);
public void put(String key, BasicDBObject value);
public void put(Bson bson);
```

## com.mongoplus.bson.MongoPlusDocument · mongo-plus-core

`import com.mongoplus.bson.MongoPlusDocument;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/bson/MongoPlusDocument.java#L15)。类型：CLASS；类型参数 ``；extends `Document`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Map;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public MongoPlusDocument();
public <T, R> MongoPlusDocument(SFunction<T, R> key, Object value);
public MongoPlusDocument(String key, Object value);
public MongoPlusDocument(Map<String, ?> map);
public <T, R> void put(SFunction<T, R> key, Object value);
public <T, R> void putIsNotNull(String key, Object value);
public void putIsNotNullByEnum(String key, Enum<?> value);
public <T, V> void put(SFunction<T, ?> key, SFunction<V, ?> value);
public <T, V> void putOption(SFunction<T, ?> key, SFunction<V, ?> value);
public <T, R> void append(SFunction<T, R> key, Object value);
public <T, R> Object get(SFunction<T, R> key);
public <T, R, D> D get(SFunction<T, R> key, Class<D> clazz);
public <T, R> Object remove(SFunction<T, R> key);
public <T, R> Object remove(SFunction<T, R> key, Object value);
public <T, R> boolean containsKey(SFunction<T, R> key);
public ObjectId getId();
```

## com.mongoplus.bson.OverridableUuidRepresentationCodecProvider · mongo-plus-core

`import com.mongoplus.bson.OverridableUuidRepresentationCodecProvider;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/bson/OverridableUuidRepresentationCodecProvider.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `CodecProvider`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.UuidRepresentation;
import org.bson.codecs.Codec;
import org.bson.codecs.OverridableUuidRepresentationCodec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import static org.bson.assertions.Assertions.notNull;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public OverridableUuidRepresentationCodecProvider(final CodecProvider wrapped, final UuidRepresentation uuidRepresentation);
@Override @SuppressWarnings({"unchecked"}) public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry);
@Override public boolean equals(final Object o);
@Override public int hashCode();
@Override public String toString();
```

## com.mongoplus.model.AggregateBasicDBObject · mongo-plus-core

`import com.mongoplus.model.AggregateBasicDBObject;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/AggregateBasicDBObject.java#L11)。类型：CLASS；类型参数 ``；extends `BasicDBObject`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import java.util.Map;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public Integer getOrder();
public void setOrder(Integer order);
public AggregateBasicDBObject(int size, Integer order);
public AggregateBasicDBObject(String key, Object value, Integer order);
public AggregateBasicDBObject(Map map, Integer order);
```

## com.mongoplus.model.aggregate.Field · mongo-plus-core

`import com.mongoplus.model.aggregate.Field;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/aggregate/Field.java#L10)。类型：CLASS；类型参数 `TExpression`；extends `com.mongodb.client.model.Field<TExpression>`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static <T, R, TExpression> Field<TExpression> of(final SFunction<T, R> name, final TExpression value);
public static <TExpression> Field<TExpression> of(final String name, final TExpression value);
public static <T, R, TExpression> Field<TExpression> of(final Boolean isField, final SFunction<T, R> name, final TExpression value);
public static <T, R, TExpression> Field<TExpression> ofNotField(final SFunction<T, R> name, final TExpression value);
public Field(final Boolean isField, String name, final TExpression value);
public Field(String name, final TExpression value);
```

## com.mongoplus.model.AutoFillMetaObject · mongo-plus-core

`import com.mongoplus.model.AutoFillMetaObject;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/AutoFillMetaObject.java#L18)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public AutoFillMetaObject();
public AutoFillMetaObject(MongoPlusDocument document);
public AutoFillMetaObject(MongoPlusDocument document, TypeInformation typeInformation);
public AutoFillMetaObject(MongoPlusDocument document, ConcurrentSkipListMap<String, Object> autoFillDocument);
public ConcurrentSkipListMap<String, Object> getAllFillField();
public MongoPlusDocument getDocument();
public void getAllFillFieldAndClear(Document document);
public boolean isEmpty();
public void skipCurrentWriteBack();
public <T, R> void fillValue(SFunction<T, R> column, R value);
public <T, R> void forceFillValue(SFunction<T, R> column, R value);
public void fillValue(String column, Object value);
public void forceFillValue(String column, Object value);
public <T, R> boolean metaObjectExist(SFunction<T, R> column);
public boolean metaObjectExist(String column);
public <T, R> Object getMetaObjectValue(SFunction<T, R> column);
public Object getMetaObjectValue(String column);
public TypeInformation getTargetObject();
public void setTargetObject(TypeInformation targetObject);
```

## com.mongoplus.model.BaseConditionResult · mongo-plus-core

`import com.mongoplus.model.BaseConditionResult;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/BaseConditionResult.java#L10)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public BaseConditionResult(BasicDBObject condition, BasicDBObject projection, BasicDBObject sort);
public BaseConditionResult();
public BasicDBObject getCondition();
public void setCondition(BasicDBObject condition);
public BasicDBObject getProjection();
public void setProjection(BasicDBObject projection);
public BasicDBObject getSort();
public void setSort(BasicDBObject sort);
@Override public String toString();
```

## com.mongoplus.model.BaseModelID · mongo-plus-core

`import com.mongoplus.model.BaseModelID;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/BaseModelID.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.enums.IdTypeEnum;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public BaseModelID();
public String getId();
public void setId(String id);
public boolean equals(Object o);
public int hashCode();
public String toString();
```

## com.mongoplus.model.BaseProperty · mongo-plus-core

`import com.mongoplus.model.BaseProperty;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/BaseProperty.java#L6)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getHost();
public String getPort();
public String getDatabase();
public String getUsername();
public String getPassword();
public String getAuthenticationDatabase();
public Integer getMinPoolSize();
public Integer getMaxPoolSize();
public Integer getWaitQueueTimeoutMS();
public Integer getServerSelectionTimeoutMS();
public Integer getLocalThresholdMS();
public Integer getHeartbeatFrequencyMS();
public String getReplicaSet();
public Boolean getSsl();
public String getClientKeyStore();
public void setClientKeyStore(String clientKeyStore);
public String getJks();
public void setJks(String jks);
public String getKeyPassword();
public void setKeyPassword(String keyPassword);
public boolean isInvalidHostNameAllowed();
public void setInvalidHostNameAllowed(boolean invalidHostNameAllowed);
public Integer getPosition();
public void setPosition(Integer position);
public Boolean getTls();
public Boolean getTlsInsecure();
public Boolean getTlsAllowInvalidHostnames();
public Integer getConnectTimeoutMS();
public Integer getSocketTimeoutMS();
public Integer getMaxIdleTimeMS();
public Integer getMaxLifeTimeMS();
public Boolean getJournal();
public String getW();
public Integer getWtimeoutMS();
public String getReadPreference();
public String getReadPreferenceTags();
public Integer getMaxStalenessSeconds();
public String getAuthMechanism();
public String getAuthSource();
public String getAuthMechanismProperties();
public String getAppName();
public String getCompressors();
public Integer getZlibCompressionLevel();
public Boolean getRetryWrites();
public Boolean getRetryReads();
public String getUuidRepresentation();
public Boolean getDirectConnection();
public Integer getMaxConnecting();
public String getSrvServiceName();
public void setHost(String host);
public void setPort(String port);
public void setDatabase(String database);
public void setUsername(String username);
public void setPassword(String password);
public void setAuthenticationDatabase(String authenticationDatabase);
public void setMinPoolSize(Integer minPoolSize);
public void setMaxPoolSize(Integer maxPoolSize);
public void setWaitQueueTimeoutMS(Integer waitQueueTimeoutMS);
public void setServerSelectionTimeoutMS(Integer serverSelectionTimeoutMS);
public void setLocalThresholdMS(Integer localThresholdMS);
public void setHeartbeatFrequencyMS(Integer heartbeatFrequencyMS);
public void setReplicaSet(String replicaSet);
public void setSsl(Boolean ssl);
public void setTls(Boolean tls);
public void setTlsInsecure(Boolean tlsInsecure);
public void setTlsAllowInvalidHostnames(Boolean tlsAllowInvalidHostnames);
public void setConnectTimeoutMS(Integer connectTimeoutMS);
public void setSocketTimeoutMS(Integer socketTimeoutMS);
public void setMaxIdleTimeMS(Integer maxIdleTimeMS);
public void setMaxLifeTimeMS(Integer maxLifeTimeMS);
public void setJournal(Boolean journal);
public void setW(String w);
public void setWtimeoutMS(Integer wtimeoutMS);
public void setReadPreference(String readPreference);
public void setReadPreferenceTags(String readPreferenceTags);
public void setMaxStalenessSeconds(Integer maxStalenessSeconds);
public void setAuthMechanism(String authMechanism);
public void setAuthSource(String authSource);
public void setAuthMechanismProperties(String authMechanismProperties);
public void setAppName(String appName);
public void setCompressors(String compressors);
public void setZlibCompressionLevel(Integer zlibCompressionLevel);
public void setRetryWrites(Boolean retryWrites);
public void setRetryReads(Boolean retryReads);
public void setUuidRepresentation(String uuidRepresentation);
public void setDirectConnection(Boolean directConnection);
public void setMaxConnecting(Integer maxConnecting);
public void setSrvServiceName(String srvServiceName);
public String getUrl();
public void setUrl(String url);
public boolean isSrv();
public void setSrv(boolean srv);
public boolean equals(Object o);
public int hashCode();
public String toString();
public BaseProperty(String host, String port, String database, String username, String password, String authenticationDatabase, Integer minPoolSize, Integer maxPoolSize, Integer waitQueueTimeoutMS, Integer serverSelectionTimeoutMS, Integer localThresholdMS, Integer heartbeatFrequencyMS, String replicaSet, Boolean ssl, Boolean tls, Boolean tlsInsecure, Boolean tlsAllowInvalidHostnames, Integer connectTimeoutMS, Integer socketTimeoutMS, Integer maxIdleTimeMS, Integer maxLifeTimeMS, Boolean journal, String w, Integer wtimeoutMS, String readPreference, String readPreferenceTags, Integer maxStalenessSeconds, String authMechanism, String authSource, String authMechanismProperties, String appName, String compressors, Integer zlibCompressionLevel, Boolean retryWrites, Boolean retryReads, String uuidRepresentation, Boolean directConnection, Integer maxConnecting, String srvServiceName);
public BaseProperty();
```

## com.mongoplus.model.BuildUpdate · mongo-plus-core

`import com.mongoplus.model.BuildUpdate;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/BuildUpdate.java#L9)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public BuildUpdate(ConditionMetaObject currentConditionMetaObject, BasicDBObject updateBasicDBObject);
public ConditionMetaObject getCurrentCompareCondition();
public void setCurrentCompareCondition(ConditionMetaObject currentConditionMetaObject);
public BasicDBObject getUpdateBasicDBObject();
public void setUpdateBasicDBObject(BasicDBObject updateBasicDBObject);
```

## com.mongoplus.model.command.BaseCommand · mongo-plus-core

`import com.mongoplus.model.command.BaseCommand;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/command/BaseCommand.java#L7)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getCommandName();
public void setCommandName(String commandName);
public BaseCommand(String commandName);
public BaseCommand();
```

## com.mongoplus.model.command.CommandFailed · mongo-plus-core

`import com.mongoplus.model.command.CommandFailed;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/command/CommandFailed.java#L9)。类型：CLASS；类型参数 ``；extends `BaseCommand`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.event.CommandFailedEvent;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public Throwable getThrowable();
public void setThrowable(Throwable throwable);
public CommandFailedEvent getCommandFailedEvent();
public void setCommandFailedEvent(CommandFailedEvent commandFailedEvent);
public CommandFailed(String commandName, Throwable throwable, CommandFailedEvent commandFailedEvent);
```

## com.mongoplus.model.command.CommandStarted · mongo-plus-core

`import com.mongoplus.model.command.CommandStarted;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/command/CommandStarted.java#L10)。类型：CLASS；类型参数 ``；extends `BaseCommand`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.event.CommandStartedEvent;
import org.bson.BsonDocument;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public BsonDocument getCommandDocument();
public void setCommandDocument(BsonDocument commandDocument);
public String getCommand();
public void setCommand(String command);
public CommandStartedEvent getCommandStartedEvent();
public void setCommandStartedEvent(CommandStartedEvent commandStartedEvent);
public CommandStarted(String commandName, BsonDocument commandDocument, String command, CommandStartedEvent commandStartedEvent);
public CommandStarted();
```

## com.mongoplus.model.command.CommandSucceeded · mongo-plus-core

`import com.mongoplus.model.command.CommandSucceeded;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/command/CommandSucceeded.java#L10)。类型：CLASS；类型参数 ``；extends `BaseCommand`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.event.CommandSucceededEvent;
import org.bson.BsonDocument;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public BsonDocument getResponse();
public void setResponse(BsonDocument response);
public CommandSucceededEvent getCommandSucceededEvent();
public void setCommandSucceededEvent(CommandSucceededEvent commandSucceededEvent);
public CommandSucceeded(String commandName, BsonDocument response, CommandSucceededEvent commandSucceededEvent);
public CommandSucceeded();
```

## com.mongoplus.model.command.ParseCommand · mongo-plus-core

`import com.mongoplus.model.command.ParseCommand;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/command/ParseCommand.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.conversions.Bson;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getOperate();
public void setOperate(String operate);
public String getCollection();
public void setCollection(String collection);
public Object getCommand();
public Bson getBsonCommand();
@SuppressWarnings("unchecked") public List<Bson> getBsonListCommand();
public void setCommand(Object command);
public String getUnresolvedCommand();
public void setUnresolvedCommand(String unresolvedCommand);
public String getOriginal();
public void setOriginal(String original);
@Override public String toString();
```

## com.mongoplus.model.ConditionModel · mongo-plus-core

`import com.mongoplus.model.ConditionModel;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/ConditionModel.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public ConditionModel();
public List<ConditionMetaObject> getConditionMetaObjects();
public void setConditionMetaObjects(List<ConditionMetaObject> conditionMetaObjects);
public List<Order> getOrderList();
public void setOrderList(List<Order> orderList);
public List<Projection> getProjectionList();
public void setProjectionList(List<Projection> projectionList);
public List<BasicDBObject> getBasicDBObjectList();
public void setBasicDBObjectList(List<BasicDBObject> basicDBObjectList);
```

## com.mongoplus.model.ExtraField · mongo-plus-core

`import com.mongoplus.model.ExtraField;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/ExtraField.java#L3)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public ExtraField(String name, Object value, Class<?> type);
public String getName();
public void setName(String name);
public Object getValue();
public void setValue(Object value);
public Class<?> getType();
public void setType(Class<?> type);
@Override public String toString();
```

## com.mongoplus.model.geo.Coordinate · mongo-plus-core

`import com.mongoplus.model.geo.Coordinate;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/geo/Coordinate.java#L3)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public Coordinate(Double x, Double y);
public Double getX();
public Double getY();
```

## com.mongoplus.model.geo.GeoBox · mongo-plus-core

`import com.mongoplus.model.geo.GeoBox;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/geo/GeoBox.java#L6)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.toolkit.Filters;
import org.bson.conversions.Bson;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public GeoBox(double lowerLeftX, double lowerLeftY, double upperRightX, double upperRightY);
public Bson toBson(String fieldName);
public double getLowerLeftX();
public double getLowerLeftY();
public double getUpperRightX();
public double getUpperRightY();
@Override public String toString();
```

## com.mongoplus.model.geo.GeoCenter · mongo-plus-core

`import com.mongoplus.model.geo.GeoCenter;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/geo/GeoCenter.java#L6)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.toolkit.Filters;
import org.bson.conversions.Bson;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public GeoCenter(Double x, Double y, Double radius);
public Bson buildCenter(String fieldName);
public Bson buildCenterSphere(String fieldName);
public Double getX();
public Double getY();
public Double getRadius();
```

## com.mongoplus.model.geo.GeoNear · mongo-plus-core

`import com.mongoplus.model.geo.GeoNear;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/geo/GeoNear.java#L8)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.Point;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.Filters;
import org.bson.conversions.Bson;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public GeoNear(Object geometry, Double maxDistance, Double minDistance);
public Bson buildNear(String fieldName);
public Bson buildNearSphere(String fieldName);
public Object getGeometry();
public Double getMaxDistance();
public Double getMinDistance();
```

## com.mongoplus.model.Indexes · mongo-plus-core

`import com.mongoplus.model.Indexes;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/Indexes.java#L22)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.support.SFunction;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import java.util.List;
import java.util.stream.Collectors;
import static com.mongodb.assertions.Assertions.notNull;
import static java.util.Arrays.asList;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static Bson ascending(final String... fieldNames);
@SafeVarargs public static <T> Bson ascendingFunc(final SFunction<T, Object>... fieldNames);
public static Bson ascending(final List<String> fieldNames);
public static <T> Bson ascendingFunc(final List<SFunction<T, Object>> fieldNames);
public static Bson descending(final String... fieldNames);
@SafeVarargs public static <T> Bson descendingFunc(final SFunction<T, Object>... fieldNames);
public static Bson descending(final List<String> fieldNames);
public static <T> Bson descendingFunc(final List<SFunction<T, Object>> fieldNames);
public static Bson geo2dsphere(final String... fieldNames);
public static <T> Bson geo2dsphereFunc(final SFunction<T, Object>... fieldNames);
public static Bson geo2dsphere(final List<String> fieldNames);
public static <T> Bson geo2dsphereFunc(final List<SFunction<T, Object>> fieldNames);
public static Bson geo2d(final String fieldName);
public static <T> Bson geo2d(final SFunction<T, Object> fieldName);
@Deprecated public static Bson geoHaystack(final String fieldName, final Bson additional);
@Deprecated public static <T> Bson geoHaystack(final SFunction<T, Object> fieldName, final Bson additional);
public static Bson text(final String fieldName);
public static <T> Bson text(final SFunction<T, Object> fieldName);
public static Bson text();
public static Bson hashed(final String fieldName);
public static <T> Bson hashed(final SFunction<T, Object> fieldName);
public static Bson compoundIndex(final Bson... indexes);
public static Bson compoundIndex(final List<? extends Bson> indexes);
```

## com.mongoplus.model.IndexMetaObject · mongo-plus-core

`import com.mongoplus.model.IndexMetaObject;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/IndexMetaObject.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.SearchIndexModel;
import com.mongoplus.enums.IndexType;
import com.mongoplus.mapping.TypeInformation;
import java.util.List;
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public IndexMetaObject();
public IndexMetaObject(TypeInformation typeInformation, String dataSource, List<IndexModel> indexModels, IndexType indexType, List<SearchIndexModel> searchIndexModels);
public TypeInformation getTypeInformation();
public void setTypeInformation(TypeInformation typeInformation);
public String getDataSource();
public void setDataSource(String dataSource);
public List<IndexModel> getIndexModels();
public void setIndexModels(List<IndexModel> indexModels);
public IndexType getIndexType();
public void setIndexType(IndexType indexType);
public List<SearchIndexModel> getSearchIndexModels();
public void setSearchIndexModels(List<SearchIndexModel> searchIndexModels);
@Override public boolean equals(Object o);
@Override public int hashCode();
@Override public String toString();
```

## com.mongoplus.model.LogicDeleteResult · mongo-plus-core

`import com.mongoplus.model.LogicDeleteResult;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/LogicDeleteResult.java#L15)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.enums.LogicDataType;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import org.bson.*;
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public LogicDataType getLogicDataType();
public void setLogicDataType(LogicDataType logicDataType);
public String getColumn();
public void setColumn(String column);
public Object getLogicDeleteValue();
public void setLogicDeleteValue(String logicDeleteValue);
public Object getLogicNotDeleteValue();
public BsonValue getLogicDeleteBsonValue();
public BsonValue getLogicNotDeleteBsonValue();
public BsonValue getLogicBsonValue(Object value);
public void setLogicNotDeleteValue(String logicNotDeleteValue);
```

## com.mongoplus.model.LogicProperty · mongo-plus-core

`import com.mongoplus.model.LogicProperty;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/LogicProperty.java#L8)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.enums.LogicDataType;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public LogicDataType getLogicDataType();
public void setLogicDataType(LogicDataType logicDataType);
public void setAutoFill(Boolean autoFill);
public Boolean getAutoFill();
public Boolean getOpen();
public void setOpen(Boolean open);
public String getLogicDeleteField();
public void setLogicDeleteField(String logicDeleteField);
public String getLogicDeleteValue();
public void setLogicDeleteValue(String logicDeleteValue);
public String getLogicNotDeleteValue();
public void setLogicNotDeleteValue(String logicNotDeleteValue);
```

## com.mongoplus.model.MutablePair · mongo-plus-core

`import com.mongoplus.model.MutablePair;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/MutablePair.java#L10)。类型：CLASS；类型参数 `L, R`；extends `Pair<L, R>`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static <L, R> MutablePair<L, R> of(final L left, final R right);
public static <T, R> MutablePair<String, R> of(final SFunction<T, Object> left, final R right);
public MutablePair();
public MutablePair(final L left, final R right);
@SuppressWarnings("unchecked") public MutablePair(final SFunction<?, Object> left, final R right);
@Override public L getLeft();
public void setLeft(final L left);
@Override public R getRight();
public void setRight(final R right);
@Override public R setValue(final R value);
```

## com.mongoplus.model.OperationResult · mongo-plus-core

`import com.mongoplus.model.OperationResult;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/OperationResult.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `Serializable`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.ID;
import java.io.Serializable;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getId();
public void setId(String id);
public String getOperation();
public void setOperation(String operation);
public boolean isRecordStatus();
public void setRecordStatus(boolean recordStatus);
public String getDatasourceName();
public void setDatasourceName(String datasourceName);
public String getDatabaseName();
public void setDatabaseName(String databaseName);
public String getCollectionName();
public void setCollectionName(String collectionName);
public String getChangedData();
public void setChangedData(String changedData);
public long getCost();
public void setCost(long cost);
@Override public String toString();
```

## com.mongoplus.model.Order · mongo-plus-core

`import com.mongoplus.model.Order;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/Order.java#L8)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public Integer getType();
public String getColumn();
public void setType(Integer type);
public void setColumn(String column);
public boolean equals(Object o);
public int hashCode();
public String toString();
public Order(Integer type, String column);
public Order(String column, Integer type);
public Order();
```

## com.mongoplus.model.PageParam · mongo-plus-core

`import com.mongoplus.model.PageParam;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/PageParam.java#L7)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public Integer getPageNum();
public Integer getPageSize();
public void setPageNum(Integer pageNum);
public void setPageSize(Integer pageSize);
public boolean equals(Object o);
public int hashCode();
public String toString();
public PageParam(Integer pageNum, Integer pageSize);
public PageParam();
```

## com.mongoplus.model.PageResult · mongo-plus-core

`import com.mongoplus.model.PageResult;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/PageResult.java#L12)。类型：CLASS；类型参数 `T`；extends `null`；implements `Serializable`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public long getPageNum();
public void setPageNum(long pageNum);
public long getPageSize();
public void setPageSize(long pageSize);
public long getTotalSize();
public void setTotalSize(long totalSize);
public long getTotalPages();
public void setTotalPages(long totalPages);
public List<T> getContentData();
public void setContentData(List<T> contentData);
public boolean isFirstPage();
public void setFirstPage(boolean firstPage);
public boolean isLastPage();
public void setLastPage(boolean lastPage);
public boolean isHasPreviousPage();
public void setHasPreviousPage(boolean hasPreviousPage);
public boolean isHasNextPage();
public void setHasNextPage(boolean hasNextPage);
@Override public boolean equals(Object object);
@Override public int hashCode();
@Override public String toString();
public PageResult(long pageNum, long pageSize, long totalSize, long totalPages, List<T> contentData);
public PageResult();
```

## com.mongoplus.model.Pair · mongo-plus-core

`import com.mongoplus.model.Pair;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/Pair.java#L15)。类型：CLASS；类型参数 `L, R`；extends `null`；implements `Map.Entry<L, R>, Comparable<Pair<L, R>>, Serializable`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.toolkit.builder.CompareToBuilder;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static <L, R> Pair<L, R> of(final L left, final R right);
public abstract L getLeft();
public abstract R getRight();
@Override public final L getKey();
@Override public R getValue();
@Override public int compareTo(final Pair<L, R> other);
@Override public boolean equals(final Object obj);
@Override public int hashCode();
@Override public String toString();
public String toString(final String format);
```

## com.mongoplus.model.Projection · mongo-plus-core

`import com.mongoplus.model.Projection;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/Projection.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.support.SFunction;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static <T> Projection chain(SFunction<T, Object> column, Boolean value);
public static <T> Projection chain(SFunction<T, Object> column, Integer value);
public static <T> Projection chain(SFunction<T, Object> column, Object value);
public static <T> Projection chain(String column, Boolean value);
public static <T> Projection chain(String column, Integer value);
public static <T> Projection chain(String column, Object value);
public static Projection display(String column);
public static <T> Projection display(SFunction<T, ?> column);
public static Projection none(String column);
public static <T> Projection none(SFunction<T, ?> column);
public static ProjectionBuilder builder();
public static ProjectionBuilder builder(List<Projection> projectionList);
public String getColumn();
public Object getValue();
public void setColumn(String column);
public void setValue(Object value);
public Projection(String column, Object value);
public Projection();
public boolean equals(Object o);
public int hashCode();
public String toString();
```

## com.mongoplus.model.Projection.ProjectionBuilder · mongo-plus-core

`import com.mongoplus.model.Projection.ProjectionBuilder;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/Projection.java#L89)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.support.SFunction;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public ProjectionBuilder column(String column);
public <T> ProjectionBuilder column(SFunction<T, ?> column);
public ProjectionBuilder value(Object value);
public ProjectionBuilder display(String... column);
public ProjectionBuilder none(String... column);
@SafeVarargs public final <T> ProjectionBuilder display(SFunction<T, ?>... column);
@SafeVarargs public final <T> ProjectionBuilder none(SFunction<T, ?>... column);
public ProjectionBuilder projection(String column, Object value);
public <T> ProjectionBuilder projection(SFunction<T, ?> column, Object value);
public Projection build();
public List<Projection> buildList();
public String toString();
```

## com.mongoplus.model.QueryParam · mongo-plus-core

`import com.mongoplus.model.QueryParam;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/QueryParam.java#L13)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import org.bson.conversions.Bson;
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public QueryParam(Bson query, BasicDBObject projection, BasicDBObject sort);
public Bson getQuery();
public void setQuery(Bson query);
public BasicDBObject getProjection();
public void setProjection(BasicDBObject projection);
public BasicDBObject getSort();
public void setSort(BasicDBObject sort);
@Override public boolean equals(Object o);
@Override public int hashCode();
@Override public String toString();
```

## com.mongoplus.model.Retry · mongo-plus-core

`import com.mongoplus.model.Retry;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/Retry.java#L13)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.interceptor.Invocation;
import java.util.function.BiFunction;
import java.util.function.Consumer;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static RetryBuilder builder();
public Integer getMaxRetryNum();
public Long getRetryInterval();
public Boolean getProcessIntercept();
public Integer getAutoVersionNum();
public Boolean getAsyncRetry();
public Consumer<UpdateRetryResult> getHitRetry();
public Consumer<UpdateRetryResult> getOnSuccess();
public Consumer<UpdateRetryResult> getOnFailure();
public BiFunction<UpdateRetryResult, Invocation, Object> getFallback();
public void setMaxRetryNum(final Integer maxRetryNum);
public void setRetryInterval(final Long retryInterval);
public void setProcessIntercept(final Boolean processIntercept);
public void setAutoVersionNum(final Integer autoVersionNum);
public void setAsyncRetry(final Boolean asyncRetry);
public void setHitRetry(final Consumer<UpdateRetryResult> hitRetry);
public void setOnSuccess(final Consumer<UpdateRetryResult> onSuccess);
public void setOnFailure(final Consumer<UpdateRetryResult> onFailure);
public void setFallback(final BiFunction<UpdateRetryResult, Invocation, Object> fallback);
public boolean equals(final Object o);
public int hashCode();
public String toString();
```

## com.mongoplus.model.Retry.RetryBuilder · mongo-plus-core

`import com.mongoplus.model.Retry.RetryBuilder;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/Retry.java#L86)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.interceptor.Invocation;
import java.util.function.BiFunction;
import java.util.function.Consumer;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public RetryBuilder maxRetryNum(final Integer maxRetryNum);
public RetryBuilder retryInterval(final Long retryInterval);
public RetryBuilder processIntercept(final Boolean processIntercept);
public RetryBuilder asyncRetry(final Boolean asyncRetry);
public RetryBuilder autoVersionNum(final Integer autoVersionNum);
public RetryBuilder hitRetry(final Consumer<UpdateRetryResult> hitRetry);
public RetryBuilder onSuccess(final Consumer<UpdateRetryResult> onSuccess);
public RetryBuilder onFailure(final Consumer<UpdateRetryResult> onFailure);
public RetryBuilder fallback(final BiFunction<UpdateRetryResult, Invocation, Object> fallback);
public Retry build();
public String toString();
```

## com.mongoplus.model.SlaveDataSource · mongo-plus-core

`import com.mongoplus.model.SlaveDataSource;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/SlaveDataSource.java#L7)。类型：CLASS；类型参数 ``；extends `BaseProperty`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public boolean equals(Object o);
public int hashCode();
public String getSlaveName();
public void setSlaveName(String slaveName);
public String toString();
public SlaveDataSource(String slaveName);
public SlaveDataSource();
```

## com.mongoplus.model.UpdateRetryResult · mongo-plus-core

`import com.mongoplus.model.UpdateRetryResult;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/UpdateRetryResult.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public UpdateRetryResult(Object result, Integer currentRetryNum, Object[] args, Retry retry);
public Object getResult();
public Integer getCurrentRetryNum();
public Retry getRetry();
public Object[] getArgs();
public boolean isUpdateResult();
public boolean isBulkWriteResult();
public UpdateResult getUpdateResult();
public BulkWriteResult getBulkWriteResult();
public long getModifiedCount();
@Override public String toString();
```

## com.mongoplus.strategy.conversion.impl.bson.BsonBooleanConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.bson.BsonBooleanConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/bson/BsonBooleanConversionStrategy.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BsonBoolean>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonBoolean;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BsonBoolean convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.bson.BsonDateTimeConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.bson.BsonDateTimeConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/bson/BsonDateTimeConversionStrategy.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BsonDateTime>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonDateTime;
import java.util.Date;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BsonDateTime convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.bson.BsonDoubleConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.bson.BsonDoubleConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/bson/BsonDoubleConversionStrategy.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BsonDouble>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonDouble;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BsonDouble convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.bson.BsonInt32ConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.bson.BsonInt32ConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/bson/BsonInt32ConversionStrategy.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BsonInt32>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonInt32;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BsonInt32 convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.bson.BsonInt64ConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.bson.BsonInt64ConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/bson/BsonInt64ConversionStrategy.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BsonInt64>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonInt64;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BsonInt64 convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.bson.BsonStringConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.bson.BsonStringConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/bson/BsonStringConversionStrategy.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BsonString>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonString;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BsonString convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.indexer.model.MongoPlusApiIndex · mongo-plus-indexer

`import com.mongoplus.indexer.model.MongoPlusApiIndex;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-indexer/src/main/java/com/mongoplus/indexer/model/MongoPlusApiIndex.java#L9)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public MongoPlusApiIndex(String mongoPlusVersion);
public Map<String, Object> asMap();
@SuppressWarnings("unchecked") public List<Object> list(String name);
@SuppressWarnings("unchecked") public Map<String, Object> object(String name);
public List<Object> getMethodFamilies();
```
