# 备份恢复与GridFS

`BackupManager`导出/导入集合数据，与数据库运维备份方案的故障恢复保证不同。需要跨集合一致性、持续增量或生产恢复目标时先核对该实现是否满足需求。

| 需求 | API | 前置条件与边界 |
|---|---|---|
| 导出选定集合 | new BackupManager(path,names,client).export() | 路径可写；names非空；默认每批1000；返回集合到导出路径映射 |
| 按实体选集合 | setCollectionNames(List<Class<?>>) | 通过实体元数据取物理名，当前行为是追加 |
| 按名称选集合 | setCollectionNamesStr(List<String>) | 当前行为是追加，不清空原列表 |
| 切导出源/批量大小 | setDataSourceName / setLimit | 数据源需注册；核对排序和并发写下分页一致性 |
| 恢复 | imports(String path) | 当前只有文件路径入口，读取本框架导出的ZIP；无InputStream重载 |
| 导出回调 | BackupListener.export | 备份生命周期回调，不是数据库变更流 |
| 大文件上传 | GridFSBucketManager.create(database,bucket).upload(...) | 依赖ComponentRegistry中MongoPlusClient；返回ObjectId |
| 下载/查文件 | getGridFSBucket()后使用Driver | 框架未提供完整下载包装，说明Driver边界与关闭责任 |

```java
import com.mongoplus.grid.GridFSBucketManager;
import org.bson.types.ObjectId;

class StoreAttachment {
    static ObjectId upload(String database, byte[] bytes) {
        return GridFSBucketManager.create(database,"attachments").upload(bytes,"note.txt");
    }
}
```

export()为每个非空集合生成一个ZIP路径，Map值可传给imports(String path)。imports从每个entry首条information读取database与collection_name；目标数据源实际取BackupManager当前dataSourceName，读取的information.data_source没有用于目标选择。每次imports的首个entry会直接collection.drop()再写入，后续entry继续追加；它不是无破坏合并恢复，也不自动重建索引。只在已批准的恢复目标执行，先核对元信息中的namespace和Manager目标数据源。导出与恢复的实际往返、类型保真、空批次及多entry行为本次为NOT_RUN，不将源码推导写成运行保证。

GridFS的InputStream上传重载会关闭输入流；byte[]重载使用try-with-resources。当前文件路径上传重载向下传filename的实现应实际检查，不从参数名承诺最终文件名，路径重载契约标`UNVERIFIED`。备份不承诺多集合快照、全局事务或恢复自动回滚。运行测试仅使用临时目录和自建测试集合，检验BSON类型、null、索引/元信息是否恢复及错误后残留。

具体imports、构造器与imports重载见[管理API](api/management.md)，源码位置见[公开类型矩阵](../audit/public-types.md)。本Skill任务本身不执行备份导入，不生成发布ZIP。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C092 备份恢复与导出监听**：`BackupManager`, `BackupListener`。
- **C093 GridFS上传与Driver下载**：`GridFSBucketManager`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
