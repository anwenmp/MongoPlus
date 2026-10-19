# MongoPlus API 源码索引

仅记录显式声明；类型名未做跨文件解析，不展开继承或 Lombok 成员。

全仓库扫描 622；成功 622；失败 0。本页 100 条完整源码记录（含内部实现）。

## 模块：mongo-plus-sensitive-word

### 包：com.mongoplus.annotation

#### annotation com.mongoplus.annotation.SensitiveWord

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/annotation/SensitiveWord.java:8-13

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 关键字注解
```

### 包：com.mongoplus.domain

#### class com.mongoplus.domain.SensitiveWordException

可见性：public；泛型：[]；extends：[MongoPlusException]；implements：[]

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/domain/SensitiveWordException.java:14-70

```text
* 敏感词异常类
```

##### field log

```java
private static final Log log
```

所属类型：com.mongoplus.domain.SensitiveWordException；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/domain/SensitiveWordException.java:16-16

##### field sensitiveWords

```java
private final Collection<String> sensitiveWords
```

所属类型：com.mongoplus.domain.SensitiveWordException；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/domain/SensitiveWordException.java:21-21

```text
* 敏感词
```

##### field fieldInformation

```java
private final FieldInformation fieldInformation
```

所属类型：com.mongoplus.domain.SensitiveWordException；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/domain/SensitiveWordException.java:23-23

##### field fieldName

```java
private final String fieldName
```

所属类型：com.mongoplus.domain.SensitiveWordException；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/domain/SensitiveWordException.java:28-28

```text
* 字段名
```

##### constructor SensitiveWordException

```java
public SensitiveWordException(@Nullable FieldInformation fieldInformation, @Nullable String fieldName, Collection<String> sensitiveWords)
```

所属类型：com.mongoplus.domain.SensitiveWordException；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/domain/SensitiveWordException.java:30-38

##### constructor SensitiveWordException

```java
public SensitiveWordException(@Nullable String fieldName, Collection<String> sensitiveWords)
```

所属类型：com.mongoplus.domain.SensitiveWordException；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/domain/SensitiveWordException.java:40-42

##### constructor SensitiveWordException

```java
public SensitiveWordException(Collection<String> sensitiveWords)
```

所属类型：com.mongoplus.domain.SensitiveWordException；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/domain/SensitiveWordException.java:44-46

##### method getSensitiveWords

```java
public Collection<String> getSensitiveWords()
```

所属类型：com.mongoplus.domain.SensitiveWordException；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/domain/SensitiveWordException.java:51-53

```text
* 返回敏感词
```

##### method getFieldName

```java
public String getFieldName()
```

所属类型：com.mongoplus.domain.SensitiveWordException；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/domain/SensitiveWordException.java:58-61

```java
@Nullable
```

```text
* 获取字段名
```

##### method getFieldInformation

```java
public FieldInformation getFieldInformation()
```

所属类型：com.mongoplus.domain.SensitiveWordException；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/domain/SensitiveWordException.java:66-69

```java
@Nullable
```

```text
* 获取字段信息
```

### 包：com.mongoplus.enums

#### enum com.mongoplus.enums.ResultHandler

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/enums/ResultHandler.java:6-17

```text
* 结果处理
```

#### enum com.mongoplus.enums.SensitiveType

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/enums/SensitiveType.java:8-22

```text
* 敏感词校验类型
 *
 * @author anwen
```

### 包：com.mongoplus.handler

#### interface com.mongoplus.handler.LoadExtraWord

可见性：public；泛型：[]；extends：[IWordDeny, IWordAllow]；implements：[]

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/handler/LoadExtraWord.java:13-24

```text
* 加载额外的敏感词
```

##### method allow

```java
default List<String> allow()
```

所属类型：com.mongoplus.handler.LoadExtraWord；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/handler/LoadExtraWord.java:15-18

```java
@Override
```

##### method deny

```java
default List<String> deny()
```

所属类型：com.mongoplus.handler.LoadExtraWord；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/handler/LoadExtraWord.java:20-23

```java
@Override
```

#### class com.mongoplus.handler.SensitiveWordFieldHandler

可见性：public；泛型：[]；extends：[]；implements：[FieldHandler]

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/handler/SensitiveWordFieldHandler.java:13-42

```text
* 关键字字段处理器
```

##### field sensitiveWordManager

```java
private final SensitiveWordManager sensitiveWordManager
```

所属类型：com.mongoplus.handler.SensitiveWordFieldHandler；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/handler/SensitiveWordFieldHandler.java:15-15

##### constructor SensitiveWordFieldHandler

```java
public SensitiveWordFieldHandler()
```

所属类型：com.mongoplus.handler.SensitiveWordFieldHandler；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/handler/SensitiveWordFieldHandler.java:17-22

##### method order

```java
public Integer order()
```

所属类型：com.mongoplus.handler.SensitiveWordFieldHandler；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/handler/SensitiveWordFieldHandler.java:24-27

```java
@Override
```

##### method handler

```java
public Object handler(FieldInformation fieldInformation)
```

所属类型：com.mongoplus.handler.SensitiveWordFieldHandler；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/handler/SensitiveWordFieldHandler.java:29-32

```java
@Override
```

##### method handler

```java
public Object handler(FieldInformation fieldInformation, Object currentValue)
```

所属类型：com.mongoplus.handler.SensitiveWordFieldHandler；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/handler/SensitiveWordFieldHandler.java:34-41

```java
@Override
```

### 包：com.mongoplus.interceptor

#### class com.mongoplus.interceptor.SensitiveWordInterceptor

可见性：public；泛型：[]；extends：[]；implements：[Interceptor]

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/interceptor/SensitiveWordInterceptor.java:21-89

```text
* 关键字拦截器
```

##### field sensitiveWordManager

```java
private final SensitiveWordManager sensitiveWordManager
```

所属类型：com.mongoplus.interceptor.SensitiveWordInterceptor；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/interceptor/SensitiveWordInterceptor.java:23-23

##### constructor SensitiveWordInterceptor

```java
public SensitiveWordInterceptor()
```

所属类型：com.mongoplus.interceptor.SensitiveWordInterceptor；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/interceptor/SensitiveWordInterceptor.java:25-30

##### method executeSave

```java
public List<Document> executeSave(List<Document> documentList, MongoCollection<Document> collection)
```

所属类型：com.mongoplus.interceptor.SensitiveWordInterceptor；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/interceptor/SensitiveWordInterceptor.java:32-39

```java
@Override
```

##### method executeSave

```java
public Document executeSave(Document document, MongoCollection<Document> collection)
```

所属类型：com.mongoplus.interceptor.SensitiveWordInterceptor；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/interceptor/SensitiveWordInterceptor.java:41-46

```java
@Override
```

##### method executeUpdate

```java
public List<MutablePair<Bson, Bson>> executeUpdate(List<MutablePair<Bson, Bson>> updatePairList, MongoCollection<Document> collection)
```

所属类型：com.mongoplus.interceptor.SensitiveWordInterceptor；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/interceptor/SensitiveWordInterceptor.java:48-58

```java
@Override
```

##### method executeUpdate

```java
public MutablePair<Bson, Bson> executeUpdate(MutablePair<Bson, Bson> updatePair, MongoCollection<Document> collection)
```

所属类型：com.mongoplus.interceptor.SensitiveWordInterceptor；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/interceptor/SensitiveWordInterceptor.java:60-67

```java
@Override
```

##### method executeBulkWrite

```java
public List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList, MongoCollection<Document> collection)
```

所属类型：com.mongoplus.interceptor.SensitiveWordInterceptor；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/interceptor/SensitiveWordInterceptor.java:69-88

```java
@Override
```

### 包：com.mongoplus.manager

#### class com.mongoplus.manager.SensitiveWordManager

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:26-213

```text
* @author anwen
```

##### field log

```java
private final Log log
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:28-28

##### field sensitiveWordProperty

```java
private final SensitiveWordProperty sensitiveWordProperty
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:33-33

```text
* 敏感词配置
```

##### field sensitiveWordBs

```java
private final SensitiveWordBs sensitiveWordBs
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:38-38

```text
* 敏感词引导类
```

##### constructor SensitiveWordManager

```java
public SensitiveWordManager(SensitiveWordProperty sensitiveWordProperty)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:40-42

##### constructor SensitiveWordManager

```java
public SensitiveWordManager(@Nullable SensitiveWordBs sensitiveWordBs, SensitiveWordProperty sensitiveWordProperty)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:44-52

##### method sensitiveWordBs

```java
public SensitiveWordBs sensitiveWordBs()
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:54-56

##### method getSensitiveType

```java
public SensitiveType getSensitiveType()
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:58-60

##### method addWordBlacklist

```java
public void addWordBlacklist(String... words)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:67-69

```text
* 添加敏感词
     * @param words 敏感词
     * @author anwen
```

##### method addWordBlacklist

```java
public void addWordBlacklist(Collection<String> words)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:76-78

```text
* 添加敏感词
     * @param words 敏感词
     * @author anwen
```

##### method addWordBlackList

```java
public void addWordBlackList(Path wordFile)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:85-92

```text
* 添加敏感词
     * @param wordFile 敏感词文件路径
     * @author anwen
```

##### method removeSensitiveWord

```java
public void removeSensitiveWord(String... words)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:99-101

```text
* 删除敏感词
     * @param words 敏感词
     * @author anwen
```

##### method removeSensitiveWord

```java
public void removeSensitiveWord(Collection<String> words)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:108-110

```text
* 删除敏感词
     * @param words 敏感词
     * @author anwen
```

##### method removeSensitiveWord

```java
public void removeSensitiveWord(Path wordFile)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:117-124

```text
* 删除敏感词
     * @param wordFile 敏感词文件路径
     * @author anwen
```

##### method addSensitiveWordAllow

```java
public void addSensitiveWordAllow(String... words)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:131-133

```text
* 添加敏感词白名单
     * @param words 敏感词
     * @author anwen
```

##### method addSensitiveWordAllow

```java
public void addSensitiveWordAllow(Collection<String> words)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:140-142

```text
* 添加敏感词白名单
     * @param words 敏感词
     * @author anwen
```

##### method addSensitiveWordAllow

```java
public void addSensitiveWordAllow(Path wordFile)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:149-156

```text
* 添加敏感词白名单
     * @param wordFile 敏感词文件路径
     * @author anwen
```

##### method removeSensitiveWordAllow

```java
public void removeSensitiveWordAllow(String... words)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:163-165

```text
* 删除敏感词白名单
     * @param words 敏感词
     * @author anwen
```

##### method removeSensitiveWordAllow

```java
public void removeSensitiveWordAllow(Collection<String> words)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:172-174

```text
* 删除敏感词白名单
     * @param words 敏感词
     * @author anwen
```

##### method removeSensitiveWordAllow

```java
public void removeSensitiveWordAllow(Path wordFile)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:181-188

```text
* 删除敏感词白名单
     * @param wordFile 敏感词文件路径
     * @author anwen
```

##### method handler

```java
public void handler(@Nullable String fieldName, String target)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:190-195

##### method handler

```java
public void handler(FieldInformation fieldInformation)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:197-203

##### method handler

```java
public void handler(String target)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:205-207

##### method replace

```java
public String replace(String word)
```

所属类型：com.mongoplus.manager.SensitiveWordManager；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:209-211

### 包：com.mongoplus.property

#### class com.mongoplus.property.SensitiveWordProperty

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:19-222

```text
* 敏感词属性
 *
 * @author anwen
```

##### field sensitiveType

```java
private SensitiveType sensitiveType
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:24-24

```text
* 敏感词校验类型,默认为局部
```

##### field ignoreCase

```java
private boolean ignoreCase
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:29-29

```text
* 是否忽略大小写
```

##### field ignoreWidth

```java
private boolean ignoreWidth
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:34-34

```text
* 是否忽略全角、半角
```

##### field ignoreNumStyle

```java
private boolean ignoreNumStyle
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:39-39

```text
* 是否忽略数字样式
```

##### field ignoreChineseStyle

```java
private boolean ignoreChineseStyle
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:44-44

```text
* 是否忽略中文样式
```

##### field ignoreEnglishStyle

```java
private boolean ignoreEnglishStyle
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:49-49

```text
* 是否忽略英文样式
```

##### field ignoreRepeat

```java
private boolean ignoreRepeat
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:54-54

```text
* 是否忽略重复
```

##### field ignoreChar

```java
private boolean ignoreChar
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:59-59

```text
* 是否忽略特殊字符,如'傻!@#$帽',将会忽略掉其中的特殊字符
```

##### field enableNumCheck

```java
private boolean enableNumCheck
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:65-65

```text
* 启用连续数字检测
```

##### field enableEmailCheck

```java
private boolean enableEmailCheck
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:71-71

```text
* 启用邮箱检测
     * 邮箱等个人信息
```

##### field enableUrlCheck

```java
private boolean enableUrlCheck
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:77-77

```text
* 启用 URL 检测
     * 用于过滤常见的网址信息
```

##### field enableIpv4Check

```java
private boolean enableIpv4Check
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:83-83

```text
* 是否启用 ipv4 校验
     * 避免用户通过 ip 绕过网址检测等
```

##### field loadExtraWord

```java
private LoadExtraWord loadExtraWord
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:86-86

##### method sensitiveWordBs

```java
public SensitiveWordBs sensitiveWordBs()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:88-104

##### method setSensitiveType

```java
public void setSensitiveType(SensitiveType sensitiveType)
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:110-112

```text
* 设置敏感词校验类型
     * @param sensitiveType 类型
```

##### method setDynamicLoadWord

```java
public void setDynamicLoadWord(LoadExtraWord loadExtraWord)
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:114-116

##### method initSensitiveType

```java
public void initSensitiveType()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:121-129

```text
* 初始化敏感词校验类型
```

##### method isIgnoreCase

```java
public boolean isIgnoreCase()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:131-133

##### method setIgnoreCase

```java
public void setIgnoreCase(boolean ignoreCase)
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:135-137

##### method isIgnoreWidth

```java
public boolean isIgnoreWidth()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:139-141

##### method isIgnoreChar

```java
public boolean isIgnoreChar()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:143-145

##### method setIgnoreChar

```java
public void setIgnoreChar(boolean ignoreChar)
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:147-149

##### method setIgnoreWidth

```java
public void setIgnoreWidth(boolean ignoreWidth)
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:151-153

##### method isIgnoreNumStyle

```java
public boolean isIgnoreNumStyle()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:155-157

##### method setIgnoreNumStyle

```java
public void setIgnoreNumStyle(boolean ignoreNumStyle)
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:159-161

##### method isIgnoreChineseStyle

```java
public boolean isIgnoreChineseStyle()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:163-165

##### method setIgnoreChineseStyle

```java
public void setIgnoreChineseStyle(boolean ignoreChineseStyle)
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:167-169

##### method isIgnoreEnglishStyle

```java
public boolean isIgnoreEnglishStyle()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:171-173

##### method setIgnoreEnglishStyle

```java
public void setIgnoreEnglishStyle(boolean ignoreEnglishStyle)
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:175-177

##### method isIgnoreRepeat

```java
public boolean isIgnoreRepeat()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:179-181

##### method setIgnoreRepeat

```java
public void setIgnoreRepeat(boolean ignoreRepeat)
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:183-185

##### method isEnableNumCheck

```java
public boolean isEnableNumCheck()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:187-189

##### method setEnableNumCheck

```java
public void setEnableNumCheck(boolean enableNumCheck)
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:191-193

##### method isEnableEmailCheck

```java
public boolean isEnableEmailCheck()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:195-197

##### method setEnableEmailCheck

```java
public void setEnableEmailCheck(boolean enableEmailCheck)
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:199-201

##### method isEnableUrlCheck

```java
public boolean isEnableUrlCheck()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:203-205

##### method setEnableUrlCheck

```java
public void setEnableUrlCheck(boolean enableUrlCheck)
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:207-209

##### method isEnableIpv4Check

```java
public boolean isEnableIpv4Check()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:211-213

##### method setEnableIpv4Check

```java
public void setEnableIpv4Check(boolean enableIpv4Check)
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:215-217

##### method getSensitiveType

```java
public SensitiveType getSensitiveType()
```

所属类型：com.mongoplus.property.SensitiveWordProperty；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:219-221

### 包：com.mongoplus.registry

#### class com.mongoplus.registry.HandlerRegistry

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/registry/HandlerRegistry.java:9-26

```text
* 处理器注册器
```

##### field registry

```java
private static final Map<Class<?>, Object> registry
```

所属类型：com.mongoplus.registry.HandlerRegistry；可见性：private

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/registry/HandlerRegistry.java:11-11

##### method get

```java
public static <T> T get(Class<T> clazz)
```

所属类型：com.mongoplus.registry.HandlerRegistry；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/registry/HandlerRegistry.java:17-20

```java
@SuppressWarnings("unchecked")
```

```text
* 获取实例
     * @param clazz class
```

##### method register

```java
public static <T> void register(Class<T> clazz, T instance)
```

所属类型：com.mongoplus.registry.HandlerRegistry；可见性：public

源码：mongo-plus-sensitive-word/src/main/java/com/mongoplus/registry/HandlerRegistry.java:22-24

