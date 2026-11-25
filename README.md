<p align="center">
  <img src="logo.png" width="200" alt="MongoPlus Logo" />
</p>

<h1 align="center">MongoPlus</h1>
<p align="center"><strong>使用 MyBatis-Plus 的方式，优雅地操作 MongoDB</strong></p>
<p align="center">简单易用 · 低侵入 · 轻量高效 · 灵活扩展</p>

<p align="center">
  <a href="https://gitee.com/anwena/mongo-plus/blob/master/LICENSE">
    <img src="https://img.shields.io/badge/License-View-blue.svg" alt="License" />
  </a>
  <a href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
    <img src="https://img.shields.io/badge/JDK-8-green.svg" alt="JDK 8" />
  </a>
  <a href="https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html">
    <img src="https://img.shields.io/badge/JDK-17-green.svg" alt="JDK 17" />
  </a>
  <a href="https://gitee.com/aizuda/mongo-plus/stargazers">
    <img src="https://gitee.com/aizuda/mongo-plus/badge/star.svg?theme=gvp" alt="Gitee Stars" />
  </a>
  <a href="https://gitee.com/aizuda/mongo-plus/members">
    <img src="https://gitee.com/aizuda/mongo-plus/badge/fork.svg?theme=gvp" alt="Gitee Forks" />
  </a>
  <a href="https://gitcode.com/aizuda/mongo-plus/stargazers">
    <img src="https://gitcode.com/aizuda/mongo-plus/star/badge.svg" alt="GitCode Stars" />
  </a>
</p>
<p align="center">
  <a href="https://mongoplus.com/">官方网站</a> ·
  <a href="https://mongoplus.com/">开发文档</a> ·
  <a href="https://gitee.com/aizuda/mongo-plus/issues">问题反馈</a>
</p>

## 📖 项目介绍

**MongoPlus** 是一款面向 Java 开发者的轻量级 MongoDB ODM（对象文档映射）框架，借鉴 MyBatis-Plus 的设计理念，致力于简化 MongoDB 数据访问，提供更加简单、高效、优雅的开发体验。

通过通用 CRUD、Lambda 条件构造器、链式调用以及丰富的扩展能力，开发者无需编写大量重复的原生 MongoDB 操作代码，即可轻松完成常见的数据访问及复杂查询操作，将更多精力专注于业务本身。

## ✨ 核心特性

| 特性 | 说明 |
| :--- | :--- |
| 低侵入性 | 只做增强，不做改变，可与现有 MongoDB 开发框架结合使用，降低接入成本。 |
| 轻量高效 | 自动提供基础 CRUD 能力，减少重复代码与额外开销。 |
| 通用 CRUD | 提供通用 Service，简化文档的新增、查询、更新与删除操作。 |
| Lambda 条件构造器 | 支持通过 Lambda 表达式构建查询条件，减少字段名硬编码及拼写错误。 |
| 链式调用 | 支持流畅、直观的链式 API，让复杂条件的构建更加清晰。 |
| 主键策略 | 支持 5 种主键生成策略，内置 Sequence 分布式唯一 ID 生成器。 |
| 无实体类操作 | 支持不依赖实体类的数据操作，满足灵活的数据访问需求。 |
| 动态数据源 | 支持多数据源场景下的数据访问与动态切换。 |
| 逻辑删除 | 通过逻辑删除机制满足业务数据保留需求。 |
| 自动填充 | 支持公共字段自动填充，减少重复赋值操作。 |
| 操作安全 | 支持防止全集合更新与删除，降低误操作风险。 |

## 🚀 快速开始

MongoPlus 延续了 MyBatis-Plus 简单易用的开发理念，通过少量配置即可快速接入项目，无需为常见的数据访问操作编写大量重复代码。

无论是基础 CRUD、链式条件查询，还是复杂的数据访问场景，MongoPlus 都致力于让 MongoDB 的操作更加自然、便捷。

关于 Maven 依赖、项目配置、API 使用及进阶功能，请参阅官方文档：

**[https://mongoplus.com/](https://mongoplus.com/)**

## 🛡️ 安全检测

MongoPlus 已接入墨菲安全，持续关注项目依赖及软件供应链安全。

[![Security Status](https://www.murphysec.com/platform3/v31/badge/1810204695353073664.svg)](https://www.murphysec.com/console/report/1810204585500057600/1810204695353073664)

点击上方徽章查看安全检测报告。

## 💬 交流与反馈

如果你在使用 MongoPlus 的过程中遇到问题、有新的功能建议，或希望参与项目交流，欢迎提交 Issue 或加入 MongoPlus 开发者交流群。

- 微信：`anwen_529`
- 添加好友时请备注：`MongoPlus`

<img src="wx.png" width="180" alt="MongoPlus 微信联系方式" />

## 🤝 参与贡献

MongoPlus 的成长离不开每一位开源贡献者。无论是功能开发、问题修复、文档完善，还是使用建议，我们都欢迎你的参与。

1. Star 并 Fork 本仓库。
2. 创建新的功能分支，例如 `feat/xxx`。
3. 完成代码修改，并进行必要的测试。
4. 提交代码并发起 Pull Request。

## 📄 开源协议

有关项目的使用、修改及分发许可，请参阅 [LICENSE](https://gitee.com/anwena/mongo-plus/blob/master/LICENSE) 文件。

## ⭐ 支持项目

如果 MongoPlus 对你的开发有所帮助，欢迎为项目点亮 Star。你的支持是项目持续维护与迭代的动力！

<p align="center">
  <strong>让 MongoDB 操作更简单，让 Java 开发更优雅。</strong>
</p>
