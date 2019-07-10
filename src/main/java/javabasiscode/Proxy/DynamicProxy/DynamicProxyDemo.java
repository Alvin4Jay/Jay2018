package javabasiscode.Proxy.DynamicProxy;

import java.lang.reflect.Proxy;

/**
 * Created by Jay on 2017/9/12
 */
public class DynamicProxyDemo {
    public static void main(String[] args) {
        RealSubject realSubject = new RealSubject();
        Subject s = (Subject) Proxy.newProxyInstance(realSubject.getClass().getClassLoader(),
                new Class[]{Subject.class}, new ProxyHandler(realSubject));
        s.request();
        System.out.println(s.getClass().getName());
    }
}
