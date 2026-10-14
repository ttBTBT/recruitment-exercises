# 第10、11题：认证与活动报名系统

Java 17 / Spring Boot 3.5.7 / Spring Security / Spring Data JPA / MySQL 8.4 / Redis / 原生HTML。

## 运行

仅演示（内存数据库H2与Caffeine缓存，无需MySQL/Redis）：

```sh
mvn clean verify
mvn spring-boot:run
```

浏览器访问 http://localhost:8080 。演示数据库重启即清空。请在页面注册自己的测试账号；不预置密码。

完整要求环境：在上一级目录复制 `.env.example` 为 `.env`，更换四个口令，然后：

```sh
docker compose up --build -d
docker compose logs -f app
```

MySQL/Redis没有暴露宿主机端口；应用仅监听本地映射8080。数据库初始脚本仅在新volume第一次启动时执行。已有数据须自行进行非破坏性迁移，不要随意删除volume。

第11题要求的 `docker run` 等价演示方式（演示配置，不验证MySQL/Redis）：

```sh
docker build -t openatom-events:1.0 ./web
docker run --rm -p 127.0.0.1:8080:8080 -e SPRING_PROFILES_ACTIVE=demo openatom-events:1.0
```

### 本地连接MySQL/Redis

先创建数据库openatom并执行 `sql/web-schema.sql`，设置 `SPRING_PROFILES_ACTIVE=mysql`、`DB_URL`、`DB_USER`、`DB_PASSWORD`、`JWT_SECRET`（至少32字节）、`REDIS_HOST`、`REDIS_PASSWORD`，再启动jar。默认不自动创建/修改生产表。

## API

统一响应 `{code,message,data}`；成功code为0，错误code与HTTP状态一致。登录后的请求携带 `Authorization: Bearer <token>`。

| 方法 | 路径 | 权限与用途 |
| --- | --- | --- |
| POST | /api/auth/register | `{username,password}`注册；密码哈希存储 |
| POST | /api/auth/login | 用户名密码登录；有效Token可不传正文直接登录 |
| GET | /api/events | 游客分页，page从0开始，size为1-100，keyword最长120；status空、UPCOMING、ENDED |
| GET | /api/events/{id} | 游客查看详情（含取消状态） |
| POST | /api/events | 登录用户创建 |
| PUT | /api/events/{id} | 仅创建者修改，不能减少至已报名人数以下 |
| DELETE | /api/events/{id} | 仅创建者取消（软删除，保留报名记录） |
| POST | /api/events/{id}/registrations | 登录用户报名；已开始、已取消、满额、重复均409 |
| GET | /api/events/{id}/registrations/me | 查询本人报名状态 |

活动请求体示例：

```json
{"title":"Java学习交流","description":"练习并发报名","location":"二教105","eventTime":"2026-11-01T16:20:00+08:00","capacity":30}
```

用户名3-50位字母、数字、下划线，区分大小写；密码8-72字符且UTF-8编码不超过72字节；标题120、描述5000、地点200字，容量1-100000，活动时间必须在未来。数据库UTC存储，界面按浏览器本地时区显示。

## 设计与优化

- BCrypt单向哈希密码；JWT使用HS256签名、验证发行者与到期时间，七天到期；再次访问登录接口不滚动延长有效期。错误或过期Token返回401。Bearer放请求头，不放URL。JPA参数绑定和Criteria查询防SQL注入；搜索转义 `%`、`_`，按字面关键词匹配。
- 注册用户唯一索引避免并发重名；报名唯一索引避免重复；活动的 `PESSIMISTIC_WRITE` 行锁让多实例对同一活动的报名、修改、取消串行执行。报名记录与人数递增处于同一事务，回滚不占名额。
- Spring Cache缓存分页DTO，键包含页码、页大小、关键词、状态、30秒时间桶。列表内容变化时发布事件，在事务提交之后以 `@CacheEvict(allEntries=true)` 清除专用eventLists缓存；详情直接查库。任意创建/修改会使多个筛选页及页边界变化，因此仅清一个页不正确；同时清列表缓存不影响其他缓存名称。报名/取消也清缓存。
- Redis缓存TTL30秒，时钟导致活动状态切换的陈旧窗口最多30秒；并发在途旧查询重新写入缓存的竞态也可能产生短暂旧列表。缓存仅用于展示，报名资格与人数始终在事务中查数据库，缓存不能导致超卖。极高一致性系统可引入缓存版本/事务outbox，本作业保留清晰可解释的实现。
- 生产Redis使用JDK序列化仅存服务端自有DTO，Redis必须在受信私网并启用认证，禁止开放不可信写入；容器默认封闭端口。演示Caffeine限500项+30秒过期防止时间桶无限积累。
- 登录不建立服务端会话；退出通过清除浏览器Token实现。未实现服务器强制吊销Token、登录限流、邮件找回、TLS终止，公网部署应补齐。localStorage便于作业演示；生产可改为安全cookie并启用CSRF。

## 验证

`mvn test` 检查认证、哈希、过期/篡改、重复用户名、注入字符串、权限、CRUD、缓存清理、过滤、重复报名、容量缩减、8线程争抢3名额和输入错误。默认测试使用H2+Caffeine，不能代替MySQL+Redis容器验证。真实运行状态参见项目根目录 `evidence/验证报告.md`。
