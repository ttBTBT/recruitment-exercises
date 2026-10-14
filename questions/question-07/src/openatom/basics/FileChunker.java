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
