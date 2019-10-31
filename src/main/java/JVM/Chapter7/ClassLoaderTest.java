package JVM.Chapter7;

import java.io.IOException;
import java.io.InputStream;

/**
 *  不同的类加载器对instanceof关键字运算结果的影响
 */
public class ClassLoaderTest {
    public static void main(String[] args) throws Exception {

        ClassLoader myLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                try {
                    String fileName = name.substring(name.lastIndexOf(".") + 1) + ".class";
                    InputStream is = getClass().getResourceAsStream(fileName); //当前目录载入文件
                    if (is == null) {
                        return super.loadClass(name);
                    }
                    byte[] b = new byte[is.available()]; //字节数组
                    is.read(b); //读入
                    return defineClass(name, b, 0, b.length); //构造Class对象
                } catch (IOException e) {
                    throw new ClassNotFoundException(name);
                }
            }
        };

        Object obj = myLoader.loadClass("JVM.Chapter7.ClassLoaderTest").newInstance();

        System.out.println(obj.getClass()); //自定义类加载器加载
        System.out.println(obj instanceof ClassLoaderTest); //系统类加载器加载
    }
}
