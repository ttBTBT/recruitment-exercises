package openatom.basics;
public class PaymentService {
    public boolean pay(double amount, String transactionId) {
        try {
            new PaymentProcessor().pay(amount, transactionId);
            return true;
        } catch (InsufficientFundsException exception) {
            System.out.println("余额不足，流水号为[" + exception.getTransactionId() + "]");
            return false;
        } catch (IllegalArgumentException exception) {
            throw new SystemMaintenanceException("支付请求处理失败", exception);
        } catch (Exception exception) {
            System.out.println("发生未知异常:" + exception.getMessage());
            System.out.println("底层原因：" + (exception.getCause() == null ? "无" : exception.getCause().getClass().getSimpleName()));
            return false;
        } finally {
            System.out.println("执行资源清理任务...");
        }
    }
}
