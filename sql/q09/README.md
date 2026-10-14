# 第9题：数据库基础

需要 MySQL 8.0.16 或更新版本，使用空的练习数据库。在 MySQL 客户端执行：

```sql
CREATE DATABASE openatom_q09 CHARACTER SET utf8mb4;
USE openatom_q09;
SOURCE sql/q09/01-schema.sql;
SOURCE sql/q09/02-seed.sql;
SOURCE sql/q09/03-queries.sql;
```

`SOURCE` 路径相对于启动客户端的目录；也可以在数据库工具中逐份打开并运行文件。基础三张表沿用题目定义，新增明细表保存历史单价。DDL 不删除既有表；重复运行会提示表或主键已经存在，请使用新的练习数据库。

第一份查询的预期结果（预期值，不冒充实际运行）：

| username | total_tech_books | total_spent |
| --- | ---: | ---: |
| Alice | 3 | 270.00 |
| Bob | 3 | 210.00 |

Diana 只买了2本，应被过滤；Charlie 没有买 Tech 图书。Alice 跨两个订单买了3本，Bob 在同一明细里买了3本，因此不能使用 COUNT(*) 或 COUNT(DISTINCT book_id)。历史购买价格与当前目录价格不同，花费必须采用 buy_price。

第二份查询应返回 History / World History / 90.00，Literature / Poems / 50.00，以及 Tech 下 Database Design 和 Distributed Systems 两本120.00元的书。

单独执行 `04-constraint-check.sql`，quantity=0 的 INSERT 应被拒绝；也可把0改成-1验证负数。NOT NULL 另行拒绝空数量。检查错误后执行 `SELECT COUNT(*) FROM order_items;` 应仍为7。保留这次真实的错误截图。MySQL 8.0.16以前的版本会解析但不执行部分 CHECK 定义，不适合作为此题验证环境。参见 [MySQL CHECK 约束文档](https://dev.mysql.com/doc/refman/8.4/en/create-table-check-constraints.html)。

可在事务内修改 books 当前价格，再运行第一份查询，确认历史花费不变，最后 ROLLBACK；这验证了 buy_price 的意义。订单总额与明细金额一致，但本题未实现库存扣减或订单写入事务服务。
