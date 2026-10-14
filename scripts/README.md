# 统一运行入口

从仓库根目录执行，脚本自动定位源码，不依赖当前工作目录。需要JDK17，优先使用JAVA_HOME，也可给PowerShell脚本传JavaHome。

```powershell
./scripts/run-basics.ps1 -Question 1
./scripts/run-basics.ps1 -Question 4 -ProgramArgs 4.4
./scripts/run-basics.ps1 -Main BoundaryTests
```

题号1—8分别对应HelloWorld、ProductCalculation、Toolbox、OopDemo、PaymentDemo、OrderDemo、FileDemo、TeaDemo。Main参数兼容按类名运行；交互题3需要终端输入。脚本编译当前源码，再运行，不显示预存输出。

Windows也可用根目录run-basics.cmd HelloWorld。编译文件只进入被Git忽略的build目录。跨题边界测试位于 [tests/basics](../tests/basics/)，完整讲解见 [总说明](../总说明.md)。

Linux/macOS：

```sh
bash scripts/run-basics.sh HelloWorld
bash scripts/run-basics.sh OopDemo 4.4
bash scripts/run-basics.sh BoundaryTests
```

Web使用共享项目：mvn -f shared/web/pom.xml verify。MySQL、Redis、Docker说明见 [Web运行说明](../shared/web/README.md)。
