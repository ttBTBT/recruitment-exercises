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
