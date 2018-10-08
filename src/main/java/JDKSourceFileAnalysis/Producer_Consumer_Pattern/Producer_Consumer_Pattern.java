package JDKSourceFileAnalysis.Producer_Consumer_Pattern;

/**
 * wait() notifyAll()实现生产者消费者模式
 */
public class Producer_Consumer_Pattern {
    public static void main(String[] args) {
        Depot depot = new Depot(100);

        Producer producer = new Producer(depot);

        Consumer consumer = new Consumer(depot);

        producer.produce(60);
        producer.produce(120);
        consumer.consume(90);
        consumer.consume(150);
        producer.produce(110);
    }

}

class Producer{
    private Depot depot;

    public Producer(Depot depot) {
        this.depot = depot;
    }

    public void produce(final int val){ //每次开一个线程去生产
        new Thread(){
            @Override
            public void run() {
                try {
                    depot.produce(val);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}

class Consumer{
    private Depot depot;

    public Consumer(Depot depot) {
        this.depot = depot;
    }

    public void consume(final int val){ //每次开一个线程取消费
        new Thread(){
            @Override
            public void run() {
                try {
                    depot.consume(val);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}

class Depot{
    private int capacity; //仓库容量
    private int size;    //目前存放的个数

    public Depot(int capacity) {
        this.capacity = capacity;
        this.size = 0;
    }

    public synchronized void produce(int val) throws InterruptedException {
        int left = val;
        while(left > 0){ //还有left没生产
            while(this.size == capacity) //仓库满了
                this.wait(); //当前生产者等待

            int inc = ((size + left) < capacity) ? left : (capacity-size); //可以生产多少
            size += inc;
            left -= inc;

            System.out.printf("%s produce(%3d) --> left = %3d, inc = %3d, size = %3d\n",
                    Thread.currentThread().getName(), val, left, inc, size);

            this.notifyAll(); //消费者可以消费了
        }
    }

    public synchronized void consume(int val) throws InterruptedException {
        int left = val;
        while(left > 0){ //还有left还没消费
            while(size == 0) //仓库消费完了
                this.wait(); //消费者线程等待

            int des = (size>left)? left : size ; //可以消费多少个
            size -= des;
            left -= des;

            System.out.printf("%s consume(%3d) <-- left = %3d, dec = %3d, size = %3d\n",
                     Thread.currentThread().getName(), val, left, des, size);

            this.notifyAll(); //通知生产者可以生产了
        }
    }

    @Override
    public String toString() {
        return "Depot{" +
                "capacity=" + capacity +
                ", size=" + size +
                '}';
    }
}
