package JVM.Chapter3;

/**
 *  长期存活对象将进入老年代
 *  VM参数：-verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=1 / 15
 *  -XX:+PrintTenuringDistribution （每次发生Minor GC时， 输出Survivor区对象的年龄分布）
 */
public class MaxTenuringThresholdTest {
    private static final int _1MB = 1024 * 1024;

    @SuppressWarnings("unused")
    public static void testTenuringThreshold(){
        byte[] allocation1,allocation2,allocation3;
        allocation1 = new byte[_1MB/4]; //256k

        // 什么时候进入老年代决定于XX:MaxTenuringThreshold设置
        allocation2 = new byte[4*_1MB];

        // Eden区放不下了，发起第一次GC，allocation1年龄+1，allocation2因为无法放入
        // Survivor区通过分配担保机制提前进入老年代，allocation3进入新生代Eden区
        allocation3 = new byte[4*_1MB];
        allocation3 = null;

        //发起第二次GC，allocation3被回收，allocation1年龄过大进入老年代，
        // allocation4进入Eden区
        allocation3 = new byte[4*_1MB];
    }

    public static void main(String[] args) {
        testTenuringThreshold();
    }
}

//MaxTenuringThreshold=1
/*

[GC (Allocation Failure) [DefNew
Desired survivor size 524288 bytes, new threshold 1 (max 1)
- age   1:    1019936 bytes,    1019936 total
: 6732K->996K(9216K), 0.0034787 secs] 6732K->5092K(19456K), 0.0035116 secs] [Times: user=0.00 sys=0.02, real=0.00 secs]
[GC (Allocation Failure) [DefNew
Desired survivor size 524288 bytes, new threshold 1 (max 1)
: 5092K->0K(9216K), 0.0012200 secs] 9188K->5080K(19456K), 0.0012538 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
Heap
 def new generation   total 9216K, used 4178K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
  eden space 8192K,  51% used [0x00000000fec00000, 0x00000000ff014930, 0x00000000ff400000)
  from space 1024K,   0% used [0x00000000ff400000, 0x00000000ff400000, 0x00000000ff500000)
  to   space 1024K,   0% used [0x00000000ff500000, 0x00000000ff500000, 0x00000000ff600000)
 tenured generation   total 10240K, used 5080K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
   the space 10240K,  49% used [0x00000000ff600000, 0x00000000ffaf61f0, 0x00000000ffaf6200, 0x0000000100000000)
 Metaspace       used 3501K, capacity 4500K, committed 4864K, reserved 1056768K
  class space    used 389K, capacity 392K, committed 512K, reserved 1048576K
* */

