package JavaMultiThread.JavaConcurrencyInPractice.Chapter8;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 线程饥饿死锁
 */
public class ThreadDeadlock {
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public class LoadFileTask implements Callable<String>{

        private final String name;

        public LoadFileTask(String name) {
            this.name = name;
        }

        @Override
        public String call() throws Exception {
            return "";
        }
    }

    public class RenderPageTask implements Callable<String>{
        @Override
        public String call() throws Exception {
            Future<String> header, footer;

            header = executorService.submit(new LoadFileTask("header.html"));
            footer = executorService.submit(new LoadFileTask("footer.html"));

            String page = renderBody();

            return header.get() + page + footer.get(); //在单线程内执行发生死锁
        }

        public String renderBody(){
            return "";
        }
    }

}
