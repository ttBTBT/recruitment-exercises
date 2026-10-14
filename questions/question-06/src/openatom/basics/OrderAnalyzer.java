package openatom.basics;
import java.util.*;
public class OrderAnalyzer {
    private final List<Order> orders = new ArrayList<>();
    public void add(Order order) { orders.add(Objects.requireNonNull(order, "订单不能为空")); }
    public void removeInvalidOrders() { orders.removeIf(order -> order.getAmount() <= 0); }
    public Set<String> getAllUniqueTags() {
        Set<String> tags = new LinkedHashSet<>();
        for (Order order : orders) tags.addAll(order.getTags());
        return tags;
    }
    public Map<String, Double> getCustomerSpendMap() {
        Map<String, Double> spending = new TreeMap<>();
        for (Order order : orders) spending.merge(order.getCustomerName(), order.getAmount(), Double::sum);
        return spending;
    }
    public List<Order> sortOrdersByCustomerName() {
        List<Order> sorted = new ArrayList<>(orders);
        sorted.sort(Comparator.comparing(Order::getCustomerName).thenComparing(Order::getOrderId));
        return sorted;
    }
    public List<Order> sortedByAmount() { List<Order> result = new ArrayList<>(orders); Collections.sort(result); return result; }
    public int size() { return orders.size(); }
}
