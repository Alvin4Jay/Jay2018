package javabasiscode.MultiThread.thread3;

/**
 *   线程让步
 (01) wait()是让线程由“运行状态”进入到“等待(阻塞)状态”，而yield()是让线程由“运行状态”进入到“就绪状态”。
 (02) wait()是会线程释放它所持有对象的同步锁，而yield()方法不会释放锁。
 */
class ThreadC extends Thread{
    public ThreadC(String name){
        super(name);
    }
    public void run(){
        for(int i=0; i <10; i++){
            System.out.printf("%s [%d]:%d\n", this.getName(), this.getPriority(), i);
            // i整除4时，调用yield
            if (i%4 == 0)
                Thread.yield();  //让另一线程执行  让出CPU执行时间片
        }
    }
}

public class YieldTest{
    public static void main(String[] args){
        ThreadC t1 = new ThreadC("t1");
        ThreadC t2 = new ThreadC("t2");
        t1.start();
        t2.start();
    }
}