# 题 1–8：Java 基础程序

需要 JDK 17 或更高版本。无第三方依赖；所有类位于 `openatom.basics` 包，每个类独立文件。

## 环境与运行

题 1 的环境安装步骤需要在自己的电脑完成并真实截图：安装 JDK 17+、安装 IntelliJ IDEA Community 或其他 Java IDE；把 `JAVA_HOME` 设为 JDK 安装根目录，把 `%JAVA_HOME%\bin` 加入用户 Path。重新打开终端后检查 `java -version`、`javac -version`、`$env:JAVA_HOME`。本项目脚本的 `-JavaHome` 参数可直接指向独立 JDK，不会修改系统设置。

在本目录的 PowerShell 中执行：

```powershell
./run.ps1 -Main HelloWorld
./run.ps1 -Main ProductCalculation
./run.ps1 -Main Toolbox
./run.ps1 -Main OopDemo -ProgramArgs 4.1
./run.ps1 -Main OopDemo -ProgramArgs 4.2
./run.ps1 -Main OopDemo -ProgramArgs 4.3
./run.ps1 -Main OopDemo -ProgramArgs 4.4
./run.ps1 -Main OopDemo -ProgramArgs 4.5
./run.ps1 -Main PaymentDemo
./run.ps1 -Main OrderDemo
./run.ps1 -Main FileDemo
./run.ps1 -Main TeaDemo
./run.ps1 -Main BoundaryTests
```

可加 `-JavaHome 'C:\实际的JDK路径'`。IDE 中打开 `src` 为源代码目录，选择相应类的 `main` 运行即可。`OopDemo` 不传参数会演示全部五个子题；分题截图请分别传对应题号，使商品计数从新的进程开始。

Linux/macOS 通用编译运行方式：

```sh
mkdir -p build/classes
javac -encoding UTF-8 --release 17 -d build/classes src/openatom/basics/*.java
java -Dfile.encoding=UTF-8 -cp build/classes openatom.basics.BoundaryTests
```

## 各题说明

| 题号 | 入口 | 实现与边界 |
|---|---|---|
| 1 | HelloWorld | 经典 main、多文件 GreetingService.sayHello(String)，输出题目要求的 Hello world 和 Hello! |
| 2 | ProductCalculation | String/double/int/boolean、库存、总价和包邮判断；XOR 原地交换两个独立整数变量，避免加减法交换溢出 |
| 3 | Toolbox | while(true) 菜单、输入4 break；if-else 与传统 switch；嵌套 for 空心金字塔；独立递归 int calculateFactorial(int) |
| 4.1 | OopDemo 4.1 | private 字段与 Getter/Setter、无参和全参构造器、构造链只计数一次、类名调用静态方法 |
| 4.2 | OopDemo 4.2 | GPSLocatable、抽象 TransportVehicle、Truck/Drone 继承与接口实现、父类引用展示计费多态 |
| 4.3 | OopDemo 4.3 | StorageBox<T> 精确方法签名；取出 Product 与 TransportVehicle 无向下转型；空箱返回 null |
| 4.4 | OopDemo 4.4 | Object[] 初始容量3、手工复制倍增、非法查询打印并返回null、for移位删除并清空旧尾部；5单号实战 |
| 4.5 | OopDemo 4.5 | 饿汉单例、静态简单工厂、静态内部 Builder 链式选填；订单构建后为不可变快照 |
| 5 | PaymentDemo | 受检异常附带流水号、非受检异常附带 cause；多重catch按要求包装/处理；finally资源清理；4情景完整测试 |
| 6 | OrderDemo | List清洗、Set去重、Map分组；Comparable金额降序/编号升序；客户排序返回新的List；包含原题5条数据 |
| 7 | FileDemo | 缓冲字节流分片；数字编号排序（13片，实际覆盖part2/part10排序）；成功合并后删除匹配分片；逐字节和SHA-256验证 |
| 8 | TeaDemo | 2店员、2顾客每人10杯；容量5；synchronized+while+wait+notifyAll；sleep模拟；join后核验每人数目、总数及容量 |

成绩采用 A≥90、B≥80、C≥70、D≥60、E<60。题目未明确60以下字母，本文采用向下顺延 E，并保持两种实现一致。分数限制0–100；金字塔限制1–100防止控制台刷屏；int 阶乘限制0–12（13!超过int最大值），0!=1。

定位字符串为题目演示数据，不代表真实 GPS。题 4.2 因父类未声明定位接口，示例将实际实现接口的两种车辆转换为 GPSLocatable 后调用；4.3 泛型取出不需要任何转换。

题 7 不覆盖已有分片或合并目标；空文件生成一个零字节分片；缺失中间编号时拒绝合并；合并失败时保留分片。生产场景还应增加分片总数和校验和清单，否则仅靠连续编号无法发现“最后一个分片丢失”的情况。示例真实源文件和结果保留于打印出的系统临时目录供检查，不污染源码目录。

题 8 使用 while 而不是 if 等待，处理虚假唤醒及多消费者竞争；notifyAll 唤醒所有角色重新判断条件。线程输出顺序由调度决定，不要求每次一致。示例出现中断时恢复中断标志；主线程设置10秒完成上限，异常时中断并回收所有线程。

`BoundaryTests` 涵盖全部101个合法分数及非法输入、阶乘上限、单层/空心金字塔、构造计数、价格校验、泛型空箱、多轮扩容/删除/null/越界、Builder快照、支付阈值和异常链、订单排序平局/0金额/新集合、空文件/1字节/整块/末块/12片/缺片/无关文件保留、完整并发生产消费。测试只清理它自行创建的临时测试目录。

## 已执行验证

使用 Zulu OpenJDK 17.0.20.1 真实编译并运行所有入口，`BoundaryTests` 共187项检查通过。原始运行文本保存在 `../evidence/basics/`；题3记录的输入依次为 `1,86,2,5,3,4,3,13,1,-1,oops,4`，展示正常交互、阶乘溢出拒绝、非法分数与非法菜单。题7实际生成13片、逐字节一致且SHA-256一致、分片自动删除；题8每人10杯，总生产与消费均20，吧台最终0，观察最大5。脚本把 Java 临时目录设为本目录 `build/temp`，便于沙箱运行和检查文件。

## 提交截图

每题运行相应入口，把终端显示的代码运行结果真实截图后附上。题1另外需要本机版本与IDE截图；题3依次选择1输入86、2输入5、3输入4、4退出；题4各子题分别运行截图；题7显示一致性与分片删除；题8显示各员工下班、各顾客满足及最后验证。截图必须来自实际运行，本项目不伪造IDE安装或个人环境完成情况。
