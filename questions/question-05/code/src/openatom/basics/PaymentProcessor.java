package openatom.basics;
public class PaymentProcessor {
    public void pay(double amount, String transactionId) throws InsufficientFundsException {
        if (!Double.isFinite(amount)) throw new IllegalArgumentException("支付金额必须为有限数值");
        if (amount < 0) throw new IllegalArgumentException("支付金额不能为负数");
        if (amount > 10000) throw new InsufficientFundsException("余额不足", transactionId);
        if (amount == 500) throw new SystemMaintenanceException("网络连接超时", new NullPointerException("模拟底层连接对象为空"));
        System.out.println("交易[" + transactionId + "]支付成功，金额：[" + amount + "]");
    }
}
