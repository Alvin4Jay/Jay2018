package javabasiscode.Proxy.DynamicProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Jay on 2017/9/12
 */
public class DynamicProxyDemo {
    public static void main(String[] args) {
        RealSubject realSubject = new RealSubject();
        Subject s  = (Subject) Proxy.newProxyInstance(realSubject.getClass().getClassLoader(),
                new Class[]{Subject.class}, new ProxyHandler(realSubject));
        s.request();
        System.out.println(s.getClass().getName());
    }
}

interface Subject{
    void request();
}

class RealSubject implements Subject{
    @Override
    public void request() {
        System.out.println("real");
    }
}

class ProxyHandler implements InvocationHandler{
    private Subject subject;

    public ProxyHandler(Subject subject) {
        this.subject = subject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("before");
        Object res =  method.invoke(subject, args);
        System.out.println("after");
        return res;
    }
}
