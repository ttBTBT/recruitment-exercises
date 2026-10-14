# 仓库结构优化复验

2026年10月14日。旧提交历史保留，此次主要移动和去重文件。

- 第1—8题：每题src保存唯一源码；第9题：question-09/sql保存唯一SQL。
- 第10—11题：共用shared/web中的源码、配置、测试和建表SQL。
- scripts统一运行入口，tests/basics保存跨题测试；重复Web截图和验证记录改为链接。
- 移动前逐份核对重复源码内容相同；业务实现没有重写。浏览器测试和截图输出改为按新模块位置定位。
- PowerShell脚本187项检查通过，按题4运行动态数组演示通过，从仓库外调用题1通过。
- Bash脚本在Git Bash中编译并运行HelloWorld通过；未在原生Linux/macOS另行执行。
- 新shared/web模块Maven verify成功，六组API测试通过；可选浏览器流程另行启用后1组通过。
- 文档相对链接、源码摘录、Compose构建与SQL挂载路径检查通过。Docker仍未实际启动。

日志分别为basics.log、web-build.log、browser.log。默认API验证使用H2+Caffeine；浏览器测试通过stdio连接真实Spring MockMvc。本次没有新增Web MySQL+Redis或Docker运行证明，原验收缺项仍需补齐。
