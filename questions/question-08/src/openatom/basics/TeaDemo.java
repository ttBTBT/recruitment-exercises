package openatom.basics;
public class TeaDemo {
    public static void main(String[] args) throws InterruptedException {
        TeaCounter counter = new TeaCounter();
        TeaEmployee first = new TeaEmployee(counter, "店员1"), second = new TeaEmployee(counter, "店员2");
        TeaCustomer alice = new TeaCustomer(counter, "顾客1"), bob = new TeaCustomer(counter, "顾客2");
        Thread[] workers = {new Thread(first), new Thread(second), new Thread(alice), new Thread(bob)};
        for (Thread worker : workers) worker.start();
        try {
            long deadline = System.currentTimeMillis() + 10000;
            for (Thread worker : workers) worker.join(Math.max(1, deadline - System.currentTimeMillis()));
            for (Thread worker : workers) if (worker.isAlive()) throw new IllegalStateException("线程未按时完成");
            if (first.getCompleted() != 10 || second.getCompleted() != 10 || alice.getCompleted() != 10 || bob.getCompleted() != 10
                    || counter.size() != 0 || counter.getProduced() != 20 || counter.getConsumed() != 20 || counter.getMaxObserved() > 5) {
                throw new IllegalStateException("生产消费数量不符合要求");
            }
            System.out.println("验证通过：每人10杯，共生产20杯、消费20杯；最终吧台0杯，历史最大数量=" + counter.getMaxObserved());
        } finally {
            for (Thread worker : workers) if (worker.isAlive()) worker.interrupt();
            for (Thread worker : workers) worker.join();
        }
    }
}
