package JVM.Chapter8.$83;

/**
 * 重载方法匹配优先级 （字面量）
 */
public class Overload {

    /*public static void sayHello(Object arg) { //  由下向上执行，父类  6
        System.out.println("hello Object");
    }*/

    /*public static void sayHello(int arg) { //字符转为int   2
        System.out.println("hello int");
    }*/

    /*public static void sayHello(long arg) {  //先自动转为Int,再转为long 3
        System.out.println("hello long");
    }*/

    /*public static void sayHello(Character arg) {  //自动装箱 4
        System.out.println("hello Character");
    }*/

    /*public static void sayHello(char arg) { // 1
        System.out.println("hello char");
    }*/

    public static void sayHello(char... arg) {
        System.out.println("hello char ...");
    }

    /*public static void sayHello(Serializable arg) {  //Character实现的一个接口 5
        System.out.println("hello Serializable");
    }*/

    /*public static void sayHello(Comparable<Character> arg) {
        System.out.println("hello Serializable");
    }*/

    public static void main(String[] args) {
        sayHello('a');
    }
}
