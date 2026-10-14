# 2026开放原子后端招新答案

本仓库在main按题整理代码、截图和运行记录，完整要求、解题过程、面试准备、验证情况及邮件提交说明集中在 [总说明.md](总说明.md)。姓名、学号、年级待本人填写。

| 题号 | 答题过程与源码讲解 | 全部源文件 |
| --- | --- | --- |
| 1 | [环境搭建与Java程序](questions/question-01/README.md) | [code目录](questions/question-01/code/) |
| 2 | [数据类型与运算符](questions/question-02/README.md) | [code目录](questions/question-02/code/) |
| 3 | [控制流工具箱](questions/question-03/README.md) | [code目录](questions/question-03/code/) |
| 4 | [面向对象与设计模式](questions/question-04/README.md) | [code目录](questions/question-04/code/) |
| 5 | [异常处理支付网关](questions/question-05/README.md) | [code目录](questions/question-05/code/) |
| 6 | [集合订单分析](questions/question-06/README.md) | [code目录](questions/question-06/code/) |
| 7 | [文件分片与合并](questions/question-07/README.md) | [code目录](questions/question-07/code/) |
| 8 | [线程同步奶茶店](questions/question-08/README.md) | [code目录](questions/question-08/code/) |
| 9 | [MySQL图书商城查询](questions/question-09/README.md) | [code目录](questions/question-09/code/) |
| 10 | [Spring Boot用户认证](questions/question-10/README.md) | [code目录](questions/question-10/code/) |
| 11 | [线上活动报名系统](questions/question-11/README.md) | [code目录](questions/question-11/code/) |

基础题1—8的每题README现在直接展示全部Java文件，第9题展示全部SQL；第10—11题展示关键源码与所有源文件的直接链接。总说明内也加入源码与逐步推演，便于对着代码学习。

## 构建与运行

需要JDK17；Web还需Maven。根目录basics/web用于整体构建，各题code用于单题查看与运行。

```powershell
./basics/run.ps1 -Main HelloWorld
./basics/run.ps1 -Main BoundaryTests
mvn -f web/pom.xml verify
mvn -f web/pom.xml spring-boot:run
```

Web启动后访问 http://localhost:8080 。默认H2+Caffeine是演示；MySQL+Redis/Docker步骤见 [web运行说明](web/README.md)。第9题MySQL运行说明见 [第9题](questions/question-09/README.md)。

## 实际验证与待补

2026年10月14日：187项基础检查、六组API集成测试、第9题原生MySQL和14项真实HTTP检查通过。可选浏览器测试本次跳过，旧浏览器证据另保留。第1题个人环境/IDE截图、Web MySQL+Redis集成、Docker实际启动仍待补；邮件未发送，报名由本人完成。日志在 [最新复验报告](evidence/pre-submit-20261014/复验报告.md)。

基础截图是实际运行日志的浏览器展示，Web旧截图经真实Spring MockMvc处理；不冒充IDE或Docker证明。每题README注明截图来源。Codex协助实现、测试与整理，参考来源集中在总说明。
