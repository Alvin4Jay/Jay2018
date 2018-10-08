package JVM.Chapter3;

/**
 *大对象直接进入老年代
 * VM 参数: -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8
 *  -XX:PretenureSizeThreshold=3145728  以字节为单位， 大对象大小限制3M
 */
public class BigObjectIntoOldGen {
    private static final int _1MB = 1024 * 1024;

    public static void testPretenureSizeThreshold(){
        byte[] allocation;
        allocation = new byte[4*_1MB];  //直接分配在老年代 Old Gen
    }

    public static void main(String[] args) {
        testPretenureSizeThreshold();
    }
}
