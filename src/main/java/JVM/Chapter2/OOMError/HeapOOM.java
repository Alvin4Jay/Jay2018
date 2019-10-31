package JVM.Chapter2.OOMError;

import java.util.ArrayList;
import java.util.List;

/**
 * Java堆OOM
 *  VM Args: -Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError
 */
public class HeapOOM {
    // 一个对象16B=对象头12(8+4)+对齐填充4
    static class OOMObject{

    }

    public static void main(String[] args) {
        List<OOMObject> list = new ArrayList<OOMObject>();
        while(true){
            list.add(new OOMObject());
        }
    }
}
