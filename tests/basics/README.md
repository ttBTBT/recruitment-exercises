# 跨题边界检查

BoundaryTests与第1—8题放在同一个Java包，由 [统一脚本](../../scripts/README.md)汇总源码后编译。各题仍可独立编译；测试不需要进入各题src目录。

在仓库根目录执行 ./scripts/run-basics.ps1 -Main BoundaryTests。187项检查涵盖分数、递归、动态数组、异常、集合、文件和线程。测试只处理自行创建的临时文件。
