package openatom.basics;
public class TeaCustomer implements Runnable {
    private final TeaCounter counter;
    private final String name;
    private int completed;
    public TeaCustomer(TeaCounter counter, String name) { this.counter = counter; this.name = name; }
    @Override public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                counter.take(name); completed++;
                Thread.sleep(20);
            }
            System.out.println(name + "喝完10杯，满足");
        } catch (InterruptedException exception) { Thread.currentThread().interrupt(); }
    }
    public int getCompleted() { return completed; }
}
