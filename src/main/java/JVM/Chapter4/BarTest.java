package JVM.Chapter4;

/**
 *  HSDIS JIT生成代码反汇编
 */
public class BarTest {
    int a = 1;
    static int b = 2;

    public int sum(int c) {
        return a + b + c;
    }

    public static void main(String[] args) {
        new BarTest().sum(3);
    }
}


