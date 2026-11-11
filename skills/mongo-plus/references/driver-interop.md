# Driver互操作与不存在的框架入口

当前公开Mapper/Repository/Service没有`distinct`或`replaceOne`专用方法，也没有独立`LambdaQueryWrapper`类。先核对依赖版本，不能将MyBatis-Plus或Driver的同名方法写成MongoPlus接口。

去重值可以优先用MongoPlus聚合group表达需求；确需Driver distinct的返回类型/选项时，通过MongoMapper.getCollection或MongoPlusClient取得集合。整文档替换可通过bulkWrite的ReplaceOneModel（仍是Driver model）或Driver replaceOne；不要用普通`$set`冒充替换，后者保留未提及字段。

```java
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

class DriverFallback {
    static List<String> distinct(MongoCollection<Document> collection) {
        return collection.distinct("category", String.class).into(new ArrayList<String>());
    }
    static void replace(MongoCollection<Document> collection, String id, Document replacement) {
        collection.replaceOne(new Document("_id",id),replacement,new ReplaceOptions().upsert(false));
    }
}
```

Driver绕过MongoPlus Mapper执行链时，租户、逻辑删除、动态集合、版本保护、字段转换与fill不能自动推定生效。需要session的调用必须显式使用同一client的ClientSession重载，并自行复现必要过滤。解释原因与边界后才能降级，而不是因为熟悉Driver就先绕过框架。

持续监听数据库所有来源写入、断点重放采用Driver Change Streams；MongoPlus Listener只观察本客户端命令。Change Streams需要部署支持、resume token持久化、幂等消费、快照衔接，外部MySQL等端到端一致性须另测。见[事件](events.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C017 distinct与replaceOne选型**：`@driver`。
- **C101 外部持续变更流边界**：`@driver`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
