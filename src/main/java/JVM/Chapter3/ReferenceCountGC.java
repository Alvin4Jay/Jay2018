package JVM.Chapter3;

/**
 * testGC() 方法执行之后，obJA/objB会不会被GC ? 会，因为JVM用的不是引用计数算法
 * 引用计数算法不会通知GC回收内存
 */
public class ReferenceCountGC {

    public Object instance = null;

    private static final int _1MB = 1024*1024;

    /**
     * 这个成员属性的唯一意义就是占点内存，以便在能在GC日志中看清楚是否有回收过
     */
    private byte[] bigSize = new byte[2*_1MB];


    public static void testGC(){
        ReferenceCountGC objA = new ReferenceCountGC();
        ReferenceCountGC objB = new ReferenceCountGC();
        objA.instance = objB; //相互循环引用
        objB.instance = objA;

        objA = null;  //两个对象不会在访问
        objB = null;

        System.gc();//实际上会被回收因为HotSpot用的不是引用计数算法；引用计数算法不会通知GC回收内存
    }

    public static void main(String[] args) {
        ReferenceCountGC.testGC();
    }

}
