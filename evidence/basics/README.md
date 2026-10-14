# Java 基础题真实运行记录

这些 `.txt` 文件来自实际运行 stdout，不是预先编写的示例。环境：Zulu OpenJDK 17.0.20.1，Windows，UTF-8。

- `HelloWorld.txt`：题1程序输出；本机IDE安装和环境变量截图仍需用户完成。
- `ProductCalculation.txt`：题2。
- `Toolbox.txt`：题3；输入依次为1、86、2、5、3、4、3、13、1、-1、oops、4。包含正常流程与非法输入处理。
- `OopDemo-4.1.txt` 至 `OopDemo-4.5.txt`：分别独立进程运行各子题。
- `PaymentDemo.txt`：题5四种情景。
- `OrderDemo.txt`：题6原题数据。
- `FileDemo.txt`：题7真实13分片、一致性校验和自动删除。
- `TeaDemo.txt`：题8两名店员与两名顾客完整生产消费；线程先后顺序不固定。
- `BoundaryTests.txt`：187项边界检查通过。
- `jdk-version.txt`：真实Java版本输出。

运行文本可用于检查和复现。题目要求截图，提交前请在终端/IDE中实际运行并截图；这些记录不代表本机IDE操作截图。

已另提供 `display-*.html` 可切换浏览器展示页，以及 `../screenshots/basics/question-*.png` 共13张浏览器真实截图。它们逐字展示上述真实运行日志，每页标明“实际运行日志的浏览器展示，不是IDE截图”。题8与边界测试因日志较长仅展示首尾行，中间省略处明确标注，完整记录仍保留；题8展示5次额外真实复测的检查结果。浏览器截图不替代题1个人环境与IDE操作证据。
