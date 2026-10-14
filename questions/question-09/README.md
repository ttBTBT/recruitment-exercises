# 第9题：MySQL图书商城查询

题目要求、完整解题过程与面试追问见 [总说明](../../总说明.md)。本目录code为本题可运行源码，screenshots为截图，evidence为原始记录。

## 运行

需要MySQL8.0.16或更新版本；新建练习数据库，在本题目录启动mysql客户端后执行（不要对已有业务库操作）：

```sql
CREATE DATABASE openatom_q09 CHARACTER SET utf8mb4;
USE openatom_q09;
SOURCE code/01-schema.sql;
SOURCE code/02-seed.sql;
SOURCE code/03-queries.sql;
SOURCE code/04-constraint-check.sql;
```

最后一份quantity=0应被拒绝。已真实执行结果见evidence/mysql-q09.txt：Alice3本270元、Bob3本210元，最高价保留Tech两本并列120元。

## 证据来源与完成状态

第9题已原生MySQL复验通过，日志位于evidence。截图为这份实际日志的页面展示，不是数据库工具或终端截图。此日志不代表Web项目数据库集成完成。

![原生MySQL实际日志的页面展示](screenshots/mysql-q09-log.png)

