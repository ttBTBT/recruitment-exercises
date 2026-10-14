package openatom.basics;
public class PaymentDemo {
    public static void main(String[] args) {
        PaymentService service = new PaymentService();
        double[] amounts = {-1, 10001, 500, 99};
        for (int i = 0; i < amounts.length; i++) {
            System.out.println("=== 测试金额：" + amounts[i] + " ===");
            try { System.out.println("支付结果：" + service.pay(amounts[i], "TX-" + (i + 1))); }
            catch (SystemMaintenanceException exception) {
                System.out.println("调用方捕获：" + exception.getMessage() + "，原始原因：" + exception.getCause().getMessage());
            }
        }
    }
}
