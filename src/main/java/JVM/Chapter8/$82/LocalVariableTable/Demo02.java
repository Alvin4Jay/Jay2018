package JVM.Chapter8.$82.LocalVariableTable;

/**
 * 局部变量表Slot复用对垃圾收集的影响
 */
public class Demo02 {
    public static void main(String[] args) {
        {
            byte[] placeHolder = new byte[64 * 1024 * 1204]; //slot保留着对数组对象的引用
        }
        System.gc();  //Full GC
        /*
        [GC (System.gc())  80393K->77904K(125952K), 0.0014547 secs]
        [Full GC (System.gc())  77904K->77816K(125952K), 0.0064516 secs]*/

    }
}
