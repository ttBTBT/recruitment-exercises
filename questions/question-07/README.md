# 第7题：文件分片与合并

题目要求、完整解题过程与面试追问见 [总说明](../../总说明.md)。本目录src为唯一源码，screenshots为截图，evidence为本题记录。


## 源码目录与文件

[打开源码目录](src)；下面的链接直接打开源文件，不是截图。

- [FileChunker.java](src/openatom/basics/FileChunker.java)
- [FileDemo.java](src/openatom/basics/FileDemo.java)

## 答题过程与代码讲解

**第1步：分片按容量读，不把文件一次读进内存。** 先读取一个字节判断是否还有下一片，再以remaining=chunkSize-1控制本片余量。每次读取上限为buffer长度与remaining的较小者，实际返回count是多少就写多少。

**第2步：合并只匹配自己的分片。** 正则对原文件名做Pattern.quote，仅接受该文件的.part整数编号。TreeMap按数字排，part2会在part10前；逐个检查编号从1连续。

**第3步：先保住数据，再清理。** 先写临时文件，完整写出并移动成最终目标后才删已匹配分片。不覆盖旧目标，无关文件不删除。没有总片数清单仍无法发现最后一片丢失，这是实现边界。

**第4步：验证不只看文件大小。** 13片实际覆盖数字排序陷阱；合并后逐字节与SHA-256一致，再确认分片删除。两个文件一样大并不能说明内容一致。

## 本题完整Java源代码

以下逐文件展示实际源码，与源码目录保持一致。每个文件应保持独立，不能把所有public类粘贴到同一个.java里。

### [FileChunker.java](src/openatom/basics/FileChunker.java)

```java
package openatom.basics;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class FileChunker {
    public static List<Path> split(String sourceFilePath, String outputDirPath, long chunkSize) throws IOException {
        if (chunkSize <= 0) throw new IllegalArgumentException("chunkSize必须大于0");
        Path source = Path.of(sourceFilePath).toAbsolutePath().normalize();
        if (!Files.isRegularFile(source)) throw new IOException("源文件不存在或不是普通文件");
        Path directory = Path.of(outputDirPath).toAbsolutePath().normalize();
        Files.createDirectories(directory);
        String filename = source.getFileName().toString();
        // 不覆盖已有分片，避免旧分片混入导致合并结果错误。
        if (!findParts(directory, filename).isEmpty()) throw new IOException("输出目录已存在同名分片，请使用新目录");
        List<Path> parts = new ArrayList<>();
        byte[] buffer = new byte[8192];
        try (InputStream input = new BufferedInputStream(Files.newInputStream(source))) {
            int first = input.read();
            int index = 1;
            do {
                Path part = directory.resolve(filename + ".part" + index++);
                parts.add(part);
                try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(part, StandardOpenOption.CREATE_NEW))) {
                    if (first != -1) {
                        output.write(first);
                        long remaining = chunkSize - 1;
                        while (remaining > 0) {
                            int count = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                            if (count == -1) break;
                            output.write(buffer, 0, count);
                            remaining -= count;
                        }
                    }
                }
                first = input.read();
            } while (first != -1);
        } catch (IOException exception) {
            for (Path part : parts) Files.deleteIfExists(part);
            throw exception;
        }
        return parts;
    }

    private static SortedMap<Integer, Path> findParts(Path directory, String filename) throws IOException {
        Pattern pattern = Pattern.compile(Pattern.quote(filename) + "\\.part([1-9][0-9]*)");
        SortedMap<Integer, Path> parts = new TreeMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                Matcher matcher = pattern.matcher(path.getFileName().toString());
                if (matcher.matches() && Files.isRegularFile(path)) {
                    int index;
                    try { index = Integer.parseInt(matcher.group(1)); }
                    catch (NumberFormatException exception) { throw new IOException("分片编号过大", exception); }
                    parts.put(index, path);
                }
            }
        }
        return parts;
    }

    public static void merge(String targetDirPath, String originalFileName, String mergedFilePath) throws IOException {
        if (originalFileName == null || originalFileName.isBlank() || !Path.of(originalFileName).getFileName().toString().equals(originalFileName)) {
            throw new IllegalArgumentException("originalFileName必须为文件名，不能包含目录");
        }
        Path directory = Path.of(targetDirPath).toAbsolutePath().normalize();
        SortedMap<Integer, Path> parts = findParts(directory, originalFileName);
        if (parts.isEmpty()) throw new IOException("没有找到分片");
        int expected = 1;
        for (int index : parts.keySet()) if (index != expected++) throw new IOException("缺失分片part" + (expected - 1));
        Path destination = Path.of(mergedFilePath).toAbsolutePath().normalize();
        if (Files.exists(destination)) throw new IOException("合并目标已存在，不允许覆盖");
        if (destination.getParent() != null) Files.createDirectories(destination.getParent());
        Path temporary = Files.createTempFile(destination.getParent(), "merge-", ".tmp");
        try {
            byte[] buffer = new byte[8192];
            try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(temporary))) {
                for (Path part : parts.values()) {
                    try (InputStream input = new BufferedInputStream(Files.newInputStream(part))) {
                        int count;
                        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                    }
                }
            }
            Files.move(temporary, destination);
            // 仅成功写出最终文件后删除本次精确匹配到的分片，保留其他文件。
            for (Path part : parts.values()) Files.delete(part);
        } finally { Files.deleteIfExists(temporary); }
    }
}
```

### [FileDemo.java](src/openatom/basics/FileDemo.java)

```java
package openatom.basics;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
public class FileDemo {
    public static void main(String[] args) throws Exception {
        Path base = Files.createTempDirectory("openatom-file-demo-");
        byte[] data = new byte[25000]; new Random(2026).nextBytes(data);
        Path source = base.resolve("myWords.bin"); Files.write(source, data);
        Path parts = base.resolve("parts");
        List<Path> chunks = FileChunker.split(source.toString(), parts.toString(), 2048);
        System.out.println("测试目录：" + base);
        System.out.println("分片数：" + chunks.size() + "（验证part2在part10之前）");
        Path merged = base.resolve("merged.bin");
        FileChunker.merge(parts.toString(), source.getFileName().toString(), merged.toString());
        System.out.println("逐字节一致：" + (Files.mismatch(source, merged) == -1));
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        System.out.println("源文件SHA-256：" + HexFormat.of().formatHex(digest.digest(Files.readAllBytes(source))));
        System.out.println("合并SHA-256：" + HexFormat.of().formatHex(digest.digest(Files.readAllBytes(merged))));
        try (var stream = Files.list(parts)) { System.out.println("合并后分片已删除：" + (stream.count() == 0)); }
        // 示例测试文件保留在输出的临时目录，便于人工检查。
    }
}
```

## 运行

需要JDK17。在本题目录执行：

```powershell
../../scripts/run-basics.ps1 -Main FileDemo
```

Linux/macOS可在本题目录执行：

```sh
mkdir -p build/classes
javac -encoding UTF-8 --release 17 -d build/classes src/openatom/basics/*.java
java -Dfile.encoding=UTF-8 -cp build/classes openatom.basics.FileDemo
```

## 证据来源与完成状态

截图是Java实际运行日志的浏览器展示，不是IDE截图；187项基础检查已复验。

![已保存运行证据，来源如上](screenshots/question-07.png)

