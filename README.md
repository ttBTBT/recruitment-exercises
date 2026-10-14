# 2026开放原子后端招新答案

本仓库在main按题整理代码、截图和运行记录，完整要求、解题过程、面试准备、验证情况及邮件提交说明集中在 [总说明.md](总说明.md)。姓名、学号、年级待本人填写。

| 题号 | 答题过程与源码讲解 | 全部源文件 |
| --- | --- | --- |
| 1 | [环境搭建与Java程序](questions/question-01/README.md) | [源码目录](questions/question-01/src) |
| 2 | [数据类型与运算符](questions/question-02/README.md) | [源码目录](questions/question-02/src) |
| 3 | [控制流工具箱](questions/question-03/README.md) | [源码目录](questions/question-03/src) |
| 4 | [面向对象与设计模式](questions/question-04/README.md) | [源码目录](questions/question-04/src) |
| 5 | [异常处理支付网关](questions/question-05/README.md) | [源码目录](questions/question-05/src) |
| 6 | [集合订单分析](questions/question-06/README.md) | [源码目录](questions/question-06/src) |
| 7 | [文件分片与合并](questions/question-07/README.md) | [源码目录](questions/question-07/src) |
| 8 | [线程同步奶茶店](questions/question-08/README.md) | [源码目录](questions/question-08/src) |
| 9 | [MySQL图书商城查询](questions/question-09/README.md) | [源码目录](questions/question-09/sql) |
| 10 | [Spring Boot用户认证](questions/question-10/README.md) | [共用源码](shared/web) |
| 11 | [线上活动报名系统](questions/question-11/README.md) | [共用源码](shared/web) |

基础题1—8的每题README现在直接展示全部Java文件，第9题展示全部SQL；第10—11题展示关键源码与所有源文件的直接链接。总说明内也加入源码与逐步推演，便于对着代码学习。

## 目录结构

```text
questions/
  question-01…08/   README、src、screenshots、evidence
  question-09/     README、sql、screenshots、evidence
  question-10…11/  题解与共用源码/证据链接
shared/web/        第10—11题唯一Spring Boot项目，含MySQL建表SQL
scripts/           Windows与Linux/macOS统一运行脚本
tests/basics/      第1—8题共用边界检查
evidence/          历史与最新验证记录、共用Web截图
总说明.md          完整解题过程、源码解读、面试与提交说明
compose.yml        共用Web项目的MySQL+Redis配置
```

源码只维护一份；每题README的代码展示与源文件对应。运行命令都从仓库根目录执行，脚本也支持从其他目录调用。

## 构建与运行

需要JDK17；Web还需Maven。第1—8题源码只在各题src维护；第9题SQL只在question-09/sql维护；第10—11题共用shared/web。

```powershell
./scripts/run-basics.ps1 -Main HelloWorld
./scripts/run-basics.ps1 -Main BoundaryTests
mvn -f shared/web/pom.xml verify
mvn -f shared/web/pom.xml spring-boot:run
```

Web启动后访问 http://localhost:8080 。默认H2+Caffeine是演示；MySQL+Redis/Docker步骤见 [web运行说明](shared/web/README.md)。第9题MySQL运行说明见 [第9题](questions/question-09/README.md)。

## 实际验证与待补

结构优化后重新通过187项基础检查、六组API测试与一组浏览器流程；运行入口和链接均已复验，见 [结构优化复验记录](evidence/structure-review-20261014/README.md)。


2026年10月14日：187项基础检查、六组API集成测试、第9题原生MySQL和14项真实HTTP检查通过。可选浏览器测试本次跳过，旧浏览器证据另保留。第1题个人环境/IDE截图、Web MySQL+Redis集成、Docker实际启动仍待补；邮件未发送，报名由本人完成。日志在 [最新复验报告](evidence/pre-submit-20261014/复验报告.md)。

基础截图是实际运行日志的浏览器展示，Web旧截图经真实Spring MockMvc处理；不冒充IDE或Docker证明。每题README注明截图来源。Codex协助实现、测试与整理，参考来源集中在总说明。
