package openatom.basics;
import java.util.List;
public class OrderDemo {
    public static void main(String[] args) {
        OrderAnalyzer analyzer = sample();
        System.out.println("清洗前订单数量：" + analyzer.size());
        analyzer.removeInvalidOrders();
        System.out.println("清洗后订单数量：" + analyzer.size());
        System.out.println("去重标签：" + analyzer.getAllUniqueTags());
        System.out.println("客户总消费：" + analyzer.getCustomerSpendMap());
        System.out.println("按金额降序、订单号升序："); analyzer.sortedByAmount().forEach(System.out::println);
        System.out.println("按客户姓名升序（返回新集合）："); analyzer.sortOrdersByCustomerName().forEach(System.out::println);
    }
    public static OrderAnalyzer sample() {
        OrderAnalyzer a = new OrderAnalyzer();
        a.add(new Order("O001", "Alice", 150, List.of("电子", "数码")));
        a.add(new Order("O002", "Bob", -50, List.of("服装")));
        a.add(new Order("O003", "Alice", 200, List.of("图书", "电子")));
        a.add(new Order("O004", "Charlie", 200, List.of("生鲜")));
        a.add(new Order("O005", "Bob", 300, List.of("服装", "户外")));
        return a;
    }
}
