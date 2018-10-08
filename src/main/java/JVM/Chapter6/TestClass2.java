package JVM.Chapter6;

/*模拟异常*/
public class TestClass2 {
    public int inc() {
        int x;

        try {
            x = 1;
            return x;
        } catch (Exception e) {
            x = 2;
            return x;
        } finally {
            x = 3;
        }
    }
}
