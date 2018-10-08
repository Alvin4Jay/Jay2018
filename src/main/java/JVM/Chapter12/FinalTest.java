package JVM.Chapter12;

/**
 *  final的可见性，无需同步，即可被其他线程正确访问
 */
public class FinalTest {
    private static final int i;

    public final int j;

    static {
        i = 0;
    }

    {
        j = 0;
    }

    public static void main(String[] args) {



    }
}
