package openatom.basics;
import java.nio.file.*;
import java.util.*;

/** 不依赖第三方框架；断言失败时抛出异常，使进程退出为非零状态。 */
public class BoundaryTests {
    private static int passed;
    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
        passed++;
    }
    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("应拒绝非法输入"); }
        catch (IllegalArgumentException expected) { passed++; }
    }
    public static void main(String[] args) throws Exception {
        for (int score = 0; score <= 100; score++) check(Toolbox.gradeIf(score).equals(Toolbox.gradeSwitch(score)), "两种成绩分支一致");
        check(Toolbox.gradeIf(59).equals("E") && Toolbox.gradeIf(60).equals("D") && Toolbox.gradeIf(90).equals("A"), "成绩区间边界");
        rejects(() -> Toolbox.gradeIf(-1)); rejects(() -> Toolbox.gradeSwitch(101));
        Toolbox toolbox = new Toolbox();
        check(toolbox.calculateFactorial(0) == 1 && toolbox.calculateFactorial(12) == 479001600, "阶乘边界");
        rejects(() -> toolbox.calculateFactorial(13)); rejects(() -> toolbox.calculateFactorial(-1));
        check(Toolbox.pyramid(1).equals("*" + System.lineSeparator()), "单层金字塔");
        check(Toolbox.pyramid(3).equals("  *" + System.lineSeparator() + " * *" + System.lineSeparator() + "*****" + System.lineSeparator()), "空心金字塔");
        rejects(() -> Toolbox.pyramid(0));
        int before = Product.getTotalCount();
        new Product(); new Product("P", "测试", 1);
        check(Product.getTotalCount() == before + 2, "构造器只计数一次");
        rejects(() -> new Product("P", "测试", Double.NaN));
        check(new Truck("T").calculateCost(10) == 50 && new Drone("D").calculateCost(10) == 150, "多态计费");
        rejects(() -> new Truck("T").calculateCost(-1));
        StorageBox<Product> box = new StorageBox<>();
        check(box.retrieveItem() == null, "空泛型容器");
        Product p = new Product(); box.storeItem(p); check(box.retrieveItem() == p, "泛型安全取出");
        MyArrayList<Integer> list = new MyArrayList<>();
        for (int i = 0; i < 20; i++) list.add(i);
        check(list.size() == 20 && list.capacity() == 24, "连续扩容3→6→12→24");
        list.remove(0); check(list.get(0) == 1 && list.get(18) == 19, "首部删除移位");
        list.remove(18); check(list.size() == 18, "末尾删除");
        list.remove(-1); check(list.size() == 18 && list.get(-1) == null && list.get(18) == null, "非法索引无副作用");
        while (list.size() > 0) list.remove(0);
        check(list.get(0) == null, "空数组访问"); list.add(null); check(list.size() == 1 && list.get(0) == null, "允许null元素");
        check(LogisticsConfig.getInstance() == LogisticsConfig.getInstance(), "单例");
        check(VehicleFactory.createVehicle(null) == null && VehicleFactory.createVehicle("Boat") == null, "未知工厂类型");
        rejects(() -> new LogisticsOrder.Builder(""));
        LogisticsOrder.Builder builder = new LogisticsOrder.Builder("X");
        LogisticsOrder old = builder.build(); builder.sender("Alice");
        check(!old.toString().contains("Alice"), "Builder生成不可变快照");
        PaymentService payment = new PaymentService();
        check(payment.pay(0, "ZERO"), "零金额成功");
        check(payment.pay(10000, "LIMIT"), "余额上限成功");
        check(!payment.pay(10001, "OVER") && !payment.pay(500, "TIMEOUT"), "业务异常被处理");
        try { payment.pay(-1, "NEGATIVE"); throw new AssertionError("应重新抛出包装异常"); }
        catch (SystemMaintenanceException expected) { check(expected.getCause() instanceof IllegalArgumentException, "负数异常链"); }
        OrderAnalyzer analyzer = OrderDemo.sample(); analyzer.removeInvalidOrders();
        check(analyzer.size() == 4 && analyzer.getCustomerSpendMap().get("Alice") == 350.0, "清洗和分组");
        check(analyzer.getAllUniqueTags().size() == 6, "标签去重");
        check(analyzer.sortedByAmount().stream().map(Order::getOrderId).toList().equals(List.of("O005", "O003", "O004", "O001")), "金额降序和同金额订单号升序");
        analyzer.sortOrdersByCustomerName().clear(); check(analyzer.size() == 4, "排序返回独立集合");
        analyzer.add(new Order("ZERO", "Bob", 0, List.of())); analyzer.removeInvalidOrders(); check(analyzer.size() == 4, "零金额也清洗");
        Path directory = Files.createTempDirectory("openatom-boundary-");
        try {
            for (int length : new int[]{0, 1, 4, 5, 48}) {
                byte[] bytes = new byte[length]; new Random(length).nextBytes(bytes);
                Path source = directory.resolve("source" + length + ".bin"); Files.write(source, bytes);
                Path parts = directory.resolve("parts" + length);
                var chunks = FileChunker.split(source.toString(), parts.toString(), 4);
                check(chunks.size() == Math.max(1, (length + 3) / 4), "分片大小边界");
                for (Path part : chunks) check(Files.size(part) <= 4, "分片不超大小");
                Files.writeString(parts.resolve("unrelated.txt"), "保留");
                Path merged = directory.resolve("merged" + length + ".bin");
                FileChunker.merge(parts.toString(), source.getFileName().toString(), merged.toString());
                check(Files.mismatch(source, merged) == -1, "分片合并逐字节一致");
                check(Files.exists(parts.resolve("unrelated.txt")), "不删除无关文件");
                for (Path part : chunks) check(!Files.exists(part), "只删除已成功合并的分片");
            }
            Path source = directory.resolve("source48.bin"), parts = directory.resolve("failure-parts");
            var chunks = FileChunker.split(source.toString(), parts.toString(), 4);
            Files.delete(chunks.get(1));
            Path destination = directory.resolve("bad-merged.bin");
            try { FileChunker.merge(parts.toString(), source.getFileName().toString(), destination.toString()); throw new AssertionError("缺片应拒绝"); }
            catch (java.io.IOException expected) { check(!Files.exists(destination) && Files.exists(chunks.get(0)), "缺片失败保留分片"); }
            try { FileChunker.split(source.toString(), directory.resolve("invalid").toString(), 0); throw new AssertionError("无效大小应拒绝"); }
            catch (IllegalArgumentException expected) { passed++; }
        } finally {
            // 仅清理本测试自行创建的临时目录。
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
        TeaDemo.main(new String[0]);
        passed++;
        System.out.println("边界验证通过，共 " + passed + " 项检查。");
    }
}
