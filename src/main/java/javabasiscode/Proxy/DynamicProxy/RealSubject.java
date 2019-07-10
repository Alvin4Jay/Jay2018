package javabasiscode.Proxy.DynamicProxy;

public class RealSubject implements Subject {
    @Override
    public void request() {
        System.out.println("real");
    }
}