package JDKSourceFileAnalysis.JUCCollection.ConcurrentHashMap.bit;

/**
 * ConcurrentHashMap 部分字段测试
 *
 * @author <a href=mailto:xuweijay@gmail.com>xuanjian</a>
 */
public class BitTest {

    /**
     * The number of bits used for generation stamp in sizeCtl.
     * Must be at least 6 for 32bit arrays.
     */
    private static int RESIZE_STAMP_BITS = 16;

    /**
     * The maximum number of threads that can help resize.
     * Must fit in 32 - RESIZE_STAMP_BITS bits. 最大扩容线程数
     */
    private static final int MAX_RESIZERS = (1 << (32 - RESIZE_STAMP_BITS)) - 1;

    /**
     * The bit shift for recording size stamp in sizeCtl.
     */
    private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;

    static final int resizeStamp(int n) {
        return Integer.numberOfLeadingZeros(n) | (1 << (RESIZE_STAMP_BITS - 1));
    }

    public static void main(String[] args) {
        int n = 16; // 数组长度
        int rs = resizeStamp(n); // 扩容标识符

//		if (sc < 0) {
//			if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
//					sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
//					transferIndex <= 0)
//				break;
//			if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
//				transfer(tab, nt);
//		}
//		else if (U.compareAndSwapInt(this, SIZECTL, sc,
//				(rs << RESIZE_STAMP_SHIFT) + 2))
//			transfer(tab, null);


        System.out.println("rs: " + rs + "\t\t\tbinary: " + Integer.toBinaryString(rs));
        int sc = (rs << RESIZE_STAMP_SHIFT) + 2; // 扩容时的sizeCtl
        System.out.println("sc: " + sc + "\t\tbinary: " + Integer.toBinaryString(sc));
        sc = sc + 1; // 扩容线程+1
        System.out.println("sc: " + sc + "\t\tbinary: " + Integer.toBinaryString(sc));
        sc = sc + 1; // 扩容线程+1
        System.out.println("sc: " + sc + "\t\tbinary: " + Integer.toBinaryString(sc));
        sc = sc - 1; // 扩容线程-1
        System.out.println("sc: " + sc + "\t\tbinary: " + Integer.toBinaryString(sc));

        if ((sc >>> RESIZE_STAMP_SHIFT) != rs) {
            return;
        }

        // 最大扩容线程数 1111111111111111
        System.out.println("MAX_RESIZERS: " + Integer.toBinaryString(MAX_RESIZERS));
    }
}
