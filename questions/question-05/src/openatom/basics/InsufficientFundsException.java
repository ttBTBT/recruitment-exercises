package openatom.basics;
public class InsufficientFundsException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String transactionId;
    public InsufficientFundsException(String message, String transactionId) { super(message); this.transactionId = transactionId; }
    public String getTransactionId() { return transactionId; }
}
