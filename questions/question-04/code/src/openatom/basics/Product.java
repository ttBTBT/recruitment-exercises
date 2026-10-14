package openatom.basics;

public class Product {
    private String id;
    private String name;
    private double price;
    private static int totalProductCount;

    public Product() { this("未编号", "未命名", 0); }
    public Product(String id, String name, double price) {
        setId(id); setName(name); setPrice(price);
        totalProductCount++;
    }
    public String getId() { return id; }
    public void setId(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("商品编号不能为空");
        this.id = id;
    }
    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("商品名称不能为空");
        this.name = name;
    }
    public double getPrice() { return price; }
    public void setPrice(double price) {
        if (!Double.isFinite(price) || price < 0) throw new IllegalArgumentException("价格必须为有限非负数");
        this.price = price;
    }
    public static int getTotalCount() { return totalProductCount; }
    public void printInfo() { System.out.println("商品信息：[" + id + "] " + name + ", 价格: " + price + "元"); }
}
