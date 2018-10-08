package javabasiscode.Proxy.StaticProxy;

/**
 * 静态代理
 */
public class StaticProxyDemo {
    public static void main(String[] args) {
        RealSubject realSubject = new RealSubject(); //原对象
        Proxy proxy = new Proxy(realSubject); //代理对象

        proxy.request();

    }
}
//公共接口
interface Subject{
    void request();
}
//委托对象
class RealSubject implements Subject{
    @Override
    public void request() {
        System.out.println("real Subject request");
    }
}
//代理对象
class Proxy implements Subject{

    private Subject subject;

    public Proxy(Subject subject) {
        this.subject = subject;
    }

    @Override
    public void request() {
        System.out.println("PreProcess");
        subject.request();
        System.out.println("PostProcess");
    }
}
