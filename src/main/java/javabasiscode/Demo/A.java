package javabasiscode.Demo;

public class A extends B{
    public void test(){
        B b = new B();
        String str = b.age;//错误！不同包下的子类不能通过实例出来的父类获取protected的变量
        String str2 = age;//正确，A类继承了B，直接拥有了该字段
        String str3 = b.birthday;//正确，birthday为public
    }
}
