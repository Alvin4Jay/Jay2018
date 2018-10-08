package javabasiscode; /**
 * Created by Jay on 2017/9/15
 */
public class ThreadLocalTest {
    ThreadLocal<String> threadLocal = new ThreadLocal<String>(){
        @Override
        protected String initialValue() {
            return "HaHa";
        }
    };

    public static void main(String[] args) {
        ThreadLocalTest test = new ThreadLocalTest();
        System.out.println(test.threadLocal.get());
    }


}
