package javabasiscode.Demo;

public class HelloB extends HelloA{
    public HelloB() {
        System.out.println("-----------HelloB 构造方法------------");
    }

    {
        System.out.println("I’m B class");
    }

    static{
        System.out.println("static B");
    }
    public static void main(String[] args){
        new HelloB();
    }
}
