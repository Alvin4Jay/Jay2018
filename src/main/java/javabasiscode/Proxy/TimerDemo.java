package javabasiscode.Proxy;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jay on 2017/9/12
 */
public class TimerDemo {
    public static void main(String[] args) {
        Timer timer  = new Timer();
        Task task = new Task();
        timer.schedule(task, new Date(), 10*1000);

    }
}

class Task extends TimerTask{
    @Override
    public void run() {
        System.out.println("到点了");
    }
}
