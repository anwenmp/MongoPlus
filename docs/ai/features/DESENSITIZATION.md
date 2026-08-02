# 返回值脱敏

> 审计日期：2026-08-02。脱敏是 Document→实体字段的读取处理，不是写入保护或 JSON 序列化器。映射链见 [ENTITY_MAPPING.md](../architecture/ENTITY_MAPPING.md)。

## 入口与支持范围

`@Desensitization` 配置 `type/startInclude/endExclude/desensitizationHandler`。core 的静态 `HandlerCache` 无条件注册 `DesensitizationHandlerApply`（order 1），没有开关或独立 Boot 3/Boot 4/Solon 配置。自定义 class 只有可赋给 `DesensitizationHandler` 时才通过反射使用；否则静默回退内置处理，没有容器注入契约。

当前枚举实际有 16 种：`CUSTOM`、`USER_ID`、`CHINESE_NAME`、`ID_CARD`、`FIXED_PHONE`、`MOBILE_PHONE`、`ADDRESS`、`EMAIL`、`PASSWORD`、`CAR_LICENSE`、`BANK_CARD`、`IPV4`、`IPV6`、`FIRST_MASK`、`CLEAR_TO_NULL`、`CLEAR_TO_EMPTY`。`CUSTOM` 按 `[startInclude,endExclude)` 隐藏；不能沿用“5 种”等宣传数字。

## 读取顺序和赋值

```text
raw Document field
  → TypeHandler.getResult（先保存）
  → 解密 ReadHandler(order 0)
  → 脱敏 ReadHandler(order 1)
  → DBRef ReadHandler（默认最大 order）
  → TypeHandler 结果非 null：最终赋 TypeHandler 结果
     否则转换 Handler 后的值并赋目标字段
```

所以先解密再脱敏，脱敏在 DBRef 前。ReadHandler 每步后检查 `discontinue`；`CLEAR_TO_NULL` 令当前字段直接停止赋值，而不是显式 set null。TypeHandler 返回非 null 时脱敏仍执行，但最终值仍是 TypeHandler 结果。解密失败会吞异常并留下密文，随后可能掩码密文，不能据此判断解密成功。脱敏+DBRef 可能把 DBRef 先字符串化，随后 DBRef 强转失败。

转换器在 raw value 为 null 时进入 Handler 前就跳过字段；空白值由 `DesensitizedUtil` 统一返回空串。非 String 通过 `String.valueOf` 并返回 String，数字/日期目标能否再转换需测试。集合、数组、Map 字段整体字符串化，不逐元素处理；嵌套实体自己的字段注解会在默认递归映射进入该对象时生效。

短值不是统一策略：`ID_CARD` 在保留位数之和大于长度时返回空；`FIXED_PHONE`、`MOBILE_PHONE`、`ADDRESS` 最终依赖 `StringUtils.hide` 的边界裁剪；`EMAIL` 在 `@` 位置小于等于 1 时原样返回；`CAR_LICENSE` 仅长度 7/8 才掩码、其他长度原样返回；`BANK_CARD` 清除空白后长度小于 9 原样返回；`IPV4/IPV6` 无格式校验，只保留首个分隔符前文本并拼固定掩码；`USER_ID` 恒返回字符串 `"0"`；`CLEAR_TO_EMPTY` 返回空串；`CLEAR_TO_NULL` 返回 null 并触发 ReadHandler `discontinue`，使当前字段保持对象构造默认值而不是执行一次显式赋 null。其余精确下标边界仍应以运行测试固定。

## 返回目标和副作用

| 目标 | 是否执行 |
|---|---|
| 实体 / DTO | 目标字段有注解时执行 |
| 普通、分页、聚合实体/DTO | 最终走同一 converter 时执行 |
| `Document.class` | 不执行，原 Document 不修改 |
| `Class<Map>` / `TypeReference<Map>` | 不按字段注解执行；当前 Mapper Map 映射另有递归缺陷 |
| `UnClassCollection` | 无实体元数据，Map/Document 路径不执行 |

脱敏只改 ReadHandler 的局部 `obj`，随后写新目标对象，不修改输入 Document。但目标对象不是只读对象；框架没有防回写标记或只读保护，调用者再次 save 时会把当前掩码/空值当普通实体值参与写映射。因此“污染数据库”是已确认的行为风险，是否在具体业务中实际发生仍需端到端运行证据。缓存、日志、JSON 输出不属于处理范围。敏感词没有读取 Handler，脱敏前后都不自动检查返回值。

## 验证与关键源码

覆盖 16 种类型、null/空/短值、数字、集合/数组/Map/嵌套、自定义 Handler、解密/TypeHandler/DBRef 组合、普通/分页/聚合 DTO、Document、读后再保存和并发。自定义 Handler 线程安全由实现者负责。

关键源码：`Desensitization.java`、`DesensitizationTypeEnum.java`；`DesensitizationHandlerApply`、`DesensitizationHandler`、`DesensitizedUtil`、`AbstractMongoConverter`、`HandlerCache`。敏感词写入行为见 [SENSITIVE_WORD.md](SENSITIVE_WORD.md)。
