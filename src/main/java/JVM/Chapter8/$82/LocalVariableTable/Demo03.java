package JVM.Chapter8.$82.LocalVariableTable;

/**
 * 局部变量表Slot复用对垃圾收集的影响
 */
public class Demo03 {
    public static void main(String[] args) {
        {
            byte[] placeHolder = new byte[64 * 1024 * 1204]; //局部变量表slot引用被替换
        }
        int a = 0;
        System.gc();  //Full GC
        /*
        [GC (System.gc())  80393K->77936K(125952K), 0.0012178 secs]
        [Full GC (System.gc())  77936K->760K(125952K), 0.0048631 secs]*/

    }
}
