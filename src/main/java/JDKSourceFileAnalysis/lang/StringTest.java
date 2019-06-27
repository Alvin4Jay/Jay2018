package JDKSourceFileAnalysis.lang;

/**
 * String Test
 *
 * @author xuanjian
 */
public class StringTest {

    private static void test01() {

        String str = "hello";
        String str2 = "hel" + "lo"; // 这个是在编译的时候确定的，会在字符串常量池中创建一个hello对象，然后把对象的引用给str。
        System.out.println(str == str2); // true
        System.out.println(str.equals(str2)); // 始终比较的是值是否相等返回 true

        String str3 = "hel" + new String("lo"); // 运行时创建，在堆内存
        System.out.println(str == str3); // false
        System.out.println(str.equals(str3)); // true

        String str4 = new String("hello"); // new String("llo");这个是在运行的时候确定，所以这个会在堆中的字符创非常量池中去创建一个hello，返回引用，此时str == str3返回的就是false。
        System.out.println(str == str4); // false。在堆中存放的对象，只要有new就会是新的创建对象
        System.out.println(str.equals(str4));//true

        String str5 = "hel" + new String("lo"); // 有new就是新的
        String str6 = "hel" + new String("lo"); // 虽然这两个都是在堆的非常量池中动态创建，但是都是动态的，new String()所以不是使用同一份。
        System.out.println(str5 == str6); // false
        System.out.println(str5.equals(str6)); // true

        String str7 = new String("hello"); // 有new就是新的
        String str8 = new String("hello"); // 虽然这两个都是在堆的非常量池中动态创建，但是都是动态的，new String()所以不是使用同一份。
        System.out.println(str7 == str8); // false
        System.out.println(str7.equals(str8)); // true
    }

    public static void main(String[] args) {
        test01();
    }

}
