# GitHub参考调研

调研时间：2026-10-04。以下是已通过网页读取核实的官方/原项目资料，并非本次招新的标准答案。此次按题目重新编写代码，没有复制这些项目的实现文件。

| 来源 | 适用题目 | 参考思路 |
| --- | --- | --- |
| [spring-projects/spring-petclinic](https://github.com/spring-projects/spring-petclinic) | 10、11 | Spring Boot应用组织、持久化配置与测试。仓库提供H2及MySQL等数据库配置；本题需要另做用户认证和活动领域，不能直接替换题目 |
| [spring-projects/spring-security-samples](https://github.com/spring-projects/spring-security-samples) | 10、11 | 官方安全示例目录包含JWT登录、资源服务器和用户名密码认证。借鉴认证与业务授权分工；7天有效期和活动所有者权限按题目自定义 |
| [iluwatar/java-design-patterns](https://github.com/iluwatar/java-design-patterns) | 4.5 | 学习Singleton、Factory、Builder解决的问题，随后用题目指定类名、属性和行为实现 |

PetClinic标明 [Apache License 2.0](https://github.com/spring-projects/spring-petclinic/blob/main/LICENSE.txt)。Java Design Patterns主体使用 [MIT许可证](https://github.com/iluwatar/java-design-patterns/blob/master/LICENSE.md)，其许可文件另提示ZK相关模块有不同许可。Spring Security Samples的 [JWT登录配置文件版权头](https://github.com/spring-projects/spring-security-samples/blob/main/servlet/spring-boot/java/jwt/login/src/main/java/example/RestConfig.java)标明Apache License 2.0，后续引用其他文件仍应逐一确认声明。本成果只参考其样例目录和安全设计，不打包或复制其代码。依赖库许可与示例参考许可是不同事项。

选择这些项目的原因是来源明确、与题目所用技术一致，可以帮助理解结构；没有寻找一套更名后即可提交的同题答案。源码中若后续加入直接复制的片段，应补充对应文件、版本、来源和许可要求，不能仅保留此表。

本成果的针对性优化包括：历史价格用buy_price聚合；最高价保留并列；密码哈希与参数化持久化；7天Token验证；活动所有者权限；报名事务防超卖；列表缓存写后失效。这些是按业务要求设计的实现方向，并不表示每项环境集成已实测，实际验证以运行记录为准。
