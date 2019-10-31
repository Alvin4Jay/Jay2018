package JVM.Chapter8.p83;

/**
 *  静态方法解析
 */
public class StaticResolution {
    public static void sayHello(){
        System.out.println("hello world");
    }

    public static void main(String[] args) {
        StaticResolution.sayHello();
    }
}
