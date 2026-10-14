package openatom.basics;
import java.util.List;
public final class Order implements Comparable<Order> {
    private final String orderId;
    private final String customerName;
    private final double amount;
    private final List<String> tags;
    public Order(String orderId, String customerName, double amount, List<String> tags) {
        if (orderId == null || customerName == null || !Double.isFinite(amount)) throw new IllegalArgumentException("订单字段无效");
        this.orderId = orderId; this.customerName = customerName; this.amount = amount; this.tags = List.copyOf(tags);
    }
    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public double getAmount() { return amount; }
    public List<String> getTags() { return tags; }
    @Override public int compareTo(Order other) {
        int byAmount = Double.compare(other.amount, amount);
        return byAmount != 0 ? byAmount : orderId.compareTo(other.orderId);
    }
    @Override public String toString() { return orderId + " / " + customerName + " / " + amount + " / " + tags; }
}
