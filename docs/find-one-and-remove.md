# 原子查找并删除

查询链的 `findOneAndRemove()` 对应 MongoDB `findOneAndDelete`：原子查找并物理删除一个匹配文档，返回删除前的数据，未命中返回 `null`。

```java
User removed = userService.lambdaQuery()
        .eq(User::getStatus, "expired")
        .orderByAsc(User::getCreatedAt)
        .findOneAndRemove();
```

与 `one`、`list` 一样，可以投影字段并指定返回类型；查询集合始终由原实体类型确定：

```java
UserView removed = userService.lambdaQuery()
        .eq(User::getStatus, "expired")
        .orderByAsc(User::getCreatedAt)
        .projectDisplay(User::getName)
        .findOneAndRemove(UserView.class);
```

### 配置完整驱动选项

通过回调配置 `FindOneAndDeleteOptions` 的全部选项，回调参数之后可追加返回类型：

```java
UserView removed = userService.lambdaQuery()
        .eq(User::getStatus, "expired")
        .orderByAsc(User::getCreatedAt)
        .projectDisplay(User::getName)
        .findOneAndRemove(options -> options
                .maxTime(3, TimeUnit.SECONDS)
                .hintString("status_created_at_index")
                .sort(new Document("_id", 1))
                .projection(new Document("name", 1)), UserView.class);
```

先应用 Lambda 的投影和排序，再执行回调。回调未设置的选项保留原值；显式设置的
`projection/sort` 完全覆盖 Lambda 配置，而不是合并。显式设置为 `null` 可以清除对应配置。
每次执行都会创建新的选项对象，回调配置不会影响 Wrapper 的后续复用；回调抛错时不会执行删除。
不需要指定返回类型时，使用 `findOneAndRemove(options -> ...)`。

### 行为说明

- 多条匹配时按排序选择一条；需要稳定选择时，排序中包含唯一字段。
- 未指定排序时不保证选中顺序；空条件会匹配任意文档。
- 投影仅控制返回字段，删除的仍是完整文档。
- 复用现有删除条件拦截器，保留租户、逻辑未删除条件以及动态集合处理。
- 此方法执行物理删除，不转换为逻辑删除更新；使用独立执行类型 `FIND_ONE_AND_REMOVE`，自定义按操作类型筛选的扩展需显式支持该类型。
- 已有的数据变更记录、异步多写和分片扩展不会自动覆盖此新操作。
- 当前事务上下文存在 session 时，驱动调用携带该 session。查询链状态不会自动清空。

参考：[MongoDB Java 驱动查找并删除](https://www.mongodb.com/zh-cn/docs/drivers/java/sync/current/crud/delete/#find-and-delete-a-document)。
