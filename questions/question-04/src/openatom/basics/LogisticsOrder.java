package openatom.basics;
public final class LogisticsOrder {
    private final String orderId;
    private final String sender;
    private final String recipient;
    private final boolean fragile;
    private final double insuredAmount;
    private LogisticsOrder(Builder builder) {
        orderId = builder.orderId; sender = builder.sender; recipient = builder.recipient;
        fragile = builder.fragile; insuredAmount = builder.insuredAmount;
    }
    public static class Builder {
        private final String orderId;
        private String sender = "未填写";
        private String recipient = "未填写";
        private boolean fragile;
        private double insuredAmount;
        public Builder(String orderId) {
            if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("订单号必填");
            this.orderId = orderId;
        }
        public Builder sender(String sender) { this.sender = sender; return this; }
        public Builder recipient(String recipient) { this.recipient = recipient; return this; }
        public Builder fragile(boolean fragile) { this.fragile = fragile; return this; }
        public Builder insuredAmount(double amount) {
            if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("保价金额必须为有限非负数");
            insuredAmount = amount; return this;
        }
        public LogisticsOrder build() { return new LogisticsOrder(this); }
    }
    @Override public String toString() {
        return "订单号[" + orderId + "], 寄件人[" + sender + "], 收件人[" + recipient
                + "], 易碎品[" + fragile + "], 保价[" + insuredAmount + "元]";
    }
}
