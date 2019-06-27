package JDKSourceFileAnalysis.lang;

/**
 * String Test
 *
 * @author xuanjian
 */
public class StringTest2 {

    private static void test01() {

//        String str1 = new String("A" + "B"); // 会创建多少个对象? 常量池1，堆内存1
//
//        String str2 = new String("ABC") + "ABC"; // 会创建多少个对象? 常量池1，堆内存3

        String s = new String("str") + new String("ing"); // 总共6个对象

    }

    public static void main(String[] args) {
        test01();
    }

}
