package openatom.basics;

public class ProductCalculation {
    public static void main(String[] args) {
        String name = "机械键盘";
        double price = 150.0;
        int stock = 20;
        boolean promotional = false;
        int quantity = 2;
        double total = price * quantity;
        boolean freeShipping = total >= 100 || promotional;
        System.out.println("商品：" + name + "，单价：" + price + "元，库存：" + stock
                + "，促销中：" + promotional + "，购买" + quantity + "件，总价为：" + total + "元");
        System.out.println("是否享受包邮：" + freeShipping);
        int a = 10, b = 20;
        System.out.println("交换前: a=" + a + ", b=" + b);
        // 两个独立变量可用 XOR 交换，不存在加减法交换的整数溢出问题。
        a ^= b;
        b ^= a;
        a ^= b;
        System.out.println("交换后: a=" + a + ", b=" + b);
    }
}
