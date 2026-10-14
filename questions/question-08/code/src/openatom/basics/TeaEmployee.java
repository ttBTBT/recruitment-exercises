package openatom.basics;
public class TeaEmployee implements Runnable {
    private final TeaCounter counter;
    private final String name;
    private int completed;
    public TeaEmployee(TeaCounter counter, String name) { this.counter = counter; this.name = name; }
    @Override public void run() {
        try {
            for (int i = 1; i <= 10; i++) {
                Thread.sleep(10);
                counter.put(name, name + "-奶茶" + i); completed++;
            }
            System.out.println(name + "完成10杯，下班");
        } catch (InterruptedException exception) { Thread.currentThread().interrupt(); }
    }
    public int getCompleted() { return completed; }
}
