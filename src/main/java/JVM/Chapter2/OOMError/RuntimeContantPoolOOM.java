package JVM.Chapter2.OOMError;

/**
 * VM Args: -XX:PermSize=10M -XX:MaxPermSize=10M
 */
public class RuntimeContantPoolOOM {

    /*public static void main(String[] args) {
        // 使用List保持着常量池引用，避免Full GC回收常量池行为
        List<String> list  = new ArrayList<String>();

        // 10MB的PermSize在integer范围内足够产生OOM了
        int i = 0;
        while(true){
            list.add(String.valueOf(i++).intern());  //从常量池获取字符串引用
        }

    }*/

    public static void main(String[] args) {
        String str1 = new StringBuilder("计算机").append("软件").toString();
        System.out.println(str1.intern() == str1);


        String str2 = new StringBuilder("ja").append("va").toString();
        System.out.println(str2.intern() == str2);
    }

}
