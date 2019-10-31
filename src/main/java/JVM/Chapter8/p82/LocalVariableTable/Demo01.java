package JVM.Chapter8.p82.LocalVariableTable;

/**
 * 局部变量表Slot复用对垃圾收集的影响
 */
public class Demo01 {
    public static void main(String[] args) {
        byte[] placeHolder = new byte[64*1024*1204];  //在作用域之中，不会回收
        System.gc();  //Full GC
        /*
        [GC (System.gc())  80393K->77904K(125952K), 0.0018542 secs]
        [Full GC (System.gc())  77904K->77816K(125952K), 0.0054276 secs]*/

    }
}
