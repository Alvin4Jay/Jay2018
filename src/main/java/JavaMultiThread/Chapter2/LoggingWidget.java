package JavaMultiThread.Chapter2;

/**
 * 重入
 */
public class LoggingWidget extends Widget{

    @Override
    public synchronized void doSomething() { //请求锁
        System.out.println(toString() + " calling doSomething"); //子类对象
        super.doSomething(); //同一线程请求锁，可重入
    }

    public static void main(String[] args) {

        LoggingWidget widget = new LoggingWidget(); //子类对象
        System.out.println(widget.toString());
        widget.doSomething();

    }
}
