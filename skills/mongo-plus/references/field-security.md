# 字段加密、脱敏、空值提示

注解的完整属性和默认值见[字段处理注解](../annotations/field-processing.md)。字段映射、TypeHandler与填充见[映射](mapping.md)，敏感词有[独立专题](sensitive-word.md)。

`@FieldEncrypt`通过默认写FieldHandler加密，通过FieldEncryptApply按findDecrypt处理回读。默认算法BASE64只是编码，不具有保密性；MD5、SM3是不可逆摘要，不应承诺能还原。内置Encryptor包括AES、RSA、PBE、SM2、SM4等具体实现，选择算法、密钥长度和异常处理要依据实现及项目安全要求，不能从类名推定满足合规密码方案。密钥从项目配置注入，Skill中的演示字符串不是可用生产密钥。

| 需求 | 优先API | 需要验证 |
|---|---|---|
| 存储敏感字段，读取恢复 | @FieldEncrypt + key / 自定义Encryptor | 真实库保存值、回读值、null、失败输入 |
| 展示手机或证件掩码 | @Desensitization(type=...) | 输出掩码与读前原值；避免将掩码当原值保存 |
| 自定义掩码 | DesensitizationHandler | startInclude/endExclude和Unicode、短字符串边界 |
| 按加密字段查询 | EncryptorConditionHandler及字段元数据 | String路径/lambda与不同算法生成的真实filter；范围/正则不当然可用 |
| 仅标可空性 | @NonNull / @Nullable | JSR-305工具支持；不会产生必填验证 |

实体save、实体update、TypeHandler与写链可以参与字段处理。wrapper-only更新、bulk的原始WriteModel及直接Driver命令不应被描述为自动获得相同加解密链。组合多个注解时跟到最终Converter，核对最新currentValue的传递和读取链顺序。

配置加密是另一个机制：Starter的`EncryptionPostProcessor`检测`${ENC:...}`，从`mpw.key`读取AES密钥并包装OriginTrackedMapPropertySource；不是字段加密注解的运行阶段。见[配置专题](configuration.md)。

依据：EncryptFieldHandler、FieldEncryptApply、EncryptorUtil、DesensitizationHandlerApply、EncryptionPostProcessor；源码定位在[公开类型矩阵](../audit/public-types.md)。运行状态独立列入[验收报告](../audit/coverage-report.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C064 字段加密与解密**：`Encryptor`, `EncryptorUtil`, `EncryptFieldHandler`, `FieldEncryptApply`。
- **C065 字段脱敏**：`DesensitizationHandler`, `DesensitizationHandlerApply`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
