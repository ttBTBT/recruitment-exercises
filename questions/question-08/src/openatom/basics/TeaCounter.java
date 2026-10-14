package openatom.basics;
import java.util.ArrayDeque;
import java.util.Queue;
public class TeaCounter {
    private final Queue<String> teas = new ArrayDeque<>();
    private final int capacity = 5;
    private int produced;
    private int consumed;
    private int maxObserved;
    public synchronized void put(String employee, String tea) throws InterruptedException {
        while (teas.size() == capacity) wait();
        teas.add(tea); produced++; maxObserved = Math.max(maxObserved, teas.size());
        System.out.println("[" + employee + "]制作了 [" + tea + "]，吧台当前数量：" + teas.size());
        notifyAll();
    }
    public synchronized String take(String customer) throws InterruptedException {
        while (teas.isEmpty()) wait();
        String tea = teas.remove(); consumed++;
        System.out.println("[" + customer + "]取走了[" + tea + "]，吧台剩余数量：" + teas.size());
        notifyAll();
        return tea;
    }
    public synchronized int size() { return teas.size(); }
    public synchronized int getProduced() { return produced; }
    public synchronized int getConsumed() { return consumed; }
    public synchronized int getMaxObserved() { return maxObserved; }
}
