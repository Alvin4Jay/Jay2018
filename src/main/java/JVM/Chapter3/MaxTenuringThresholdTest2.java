package JVM.Chapter3;

/**
 *  长期存活对象将进入老年代
 *  VM参数：-verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=1 / 15
 *  -XX:+PrintTenuringDistribution （每次发生Minor GC时， 输出Survivor区对象的年龄分布）
 */
public class MaxTenuringThresholdTest2 {
    private static final int _1MB = 1024 * 1024;

    @SuppressWarnings("unused")
    public static void testTenuringThreshold(){
        byte[] allocation1,allocation2,allocation3;
        allocation1 = new byte[_1MB/4];

        // 什么时候进入老年代决定于XX:MaxTenuringThreshold设置
        allocation2 = new byte[4*_1MB];
        allocation3 = new byte[4*_1MB];
        allocation3 = null;
        allocation3 = new byte[4*_1MB];
    }

    public static void main(String[] args) {
        testTenuringThreshold();
    }
}

