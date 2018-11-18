# Java SPI扩展机制实现分析

​	`SPI`，即`Service Provider Interface` ，是`JDK`内置的服务发现机制。经常在实际工作中会用到`java.sql.Driver`接口，不同厂商可以根据该接口提供不同的实现，比如`mysql`和`postgresql`都有不同的实现提供给用户，而`Java`的`SPI`扩展机制则可以为某个接口寻找具体的服务实现。	

当服务的提供者提供了某个接口的实现后，需要在`ClassPath`下的`META-INF/services`目录下创建一个名为服务接口全限定名的配置文件，配置文件中的内容为这个接口具体实现类的全限定名。当其他程序需要这个服务时，在引用服务提供者的`jar`包后，通过查找`jar`包中`META-INF/services`的服务接口配置文件，该配置文件中有接口的具体实现类名，可以根据该类名进行加载、实例化服务提供者，因此能使用该服务。而定位、查找、实例化、缓存服务提供者实例的工作由`java.util.ServiceLoader`完成。

## SPI扩展机制举例说明

### SPI接口

```java
/** Log interface. */
public interface Log {
    /**
     * Log execute method.
     */
    void execute();
}
```

### 接口实现

实现一：

```java
/**  Log SPI implementation By Log4j. */
public class Log4j implements Log{
    @Override
    public void execute() {
        System.out.println("Log4j...");
    }
}
```

实现二：

```java
/** Log SPI implementation By Logback. */
public class Logback implements Log{
    @Override
    public void execute() {
        System.out.println("Logback...");
    }
}
```

### 增加META-INF配置文件

![](http://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/java%20spi-1.jpg?Expires=1542514901&OSSAccessKeyId=TMP.AQHDsaCjxl7Nr0Rrq4uOj2asJndt3ui3WhuxGyddyu-KzaN9UoWCJieLf_FeMC4CFQCJrdH6EIzDDiQqUuvU-zGiIInOLwIVAPyTslcDVPwU34M_6FUDx1uMdgoX&Signature=HZQpK%2BDh9HETOu1uspumF7GZtJo%3D)

```java
com.wacai.middleware.javaspi.Log4j
```

配置文件中指定实现类为`com.wacai.middleware.javaspi.Log4j`，则`SPI`加载机制只会加载这一个实现类。

```java
com.wacai.middleware.javaspi.Log4j
com.wacai.middleware.javaspi.Logback
```

如果配置了多个实现类，则`SPI`加载机制会全部加载实现类，程序需通过额外的机制来选择具体使用哪一个实现的服务。

### 测试类

```java
import java.util.Iterator;
import java.util.ServiceLoader;

/**  Log SPI Test */
public class Main {
    public static void main(String[] args) {
        // 创建ServiceLoader
        ServiceLoader<Log> loader = ServiceLoader.load(Log.class);
        // 获取迭代器
        Iterator<Log> iterator = loader.iterator();
        // hasNext()调用时读取配置文件
        while (iterator.hasNext()) {
            // next()调用时实例化 服务提供者 实现
            Log logImpl = iterator.next();
            logImpl.execute();
        }

    }
}
```

**说明：**

`ServiceLoader`在实例化之后，不会马上去加载配置文件并实例化服务提供者。配置文件的加载、解析是在获取迭代器`iterator`，并调用`iterator.hasNext()`的时候进行的；服务提供者类的实例化是在调用`iterator.next()`方法的时候进行的。因此，这是一种懒加载的服务定位、加载、实例化机制。

## 以测试类分析ServiceLoader的源码及实现原理

`ServiceLoader`中有6个`Field`，如下：

```java
// 接口文件所在的类路径目录
private static final String PREFIX = "META-INF/services/";

// 服务接口
private final Class<S> service;

// 定位、加载、实例化服务实现类的类加载器
private final ClassLoader loader;

// 访问控制上下文
private final AccessControlContext acc;

// 以初始化顺序缓存的服务提供者，key为服务提供者(实现类)的全限定名，value为实现类实例
private LinkedHashMap<String,S> providers = new LinkedHashMap<>();

// 当前的服务懒加载迭代器，真正完成迭代工作的迭代器. LazyIterator为ServiceLoader的内部类
private LazyIterator lookupIterator; 
```

### 创建ServiceLoader

```java
ServiceLoader<Log> loader = ServiceLoader.load(Log.class);
```

对应源码：

```java
// 使用线程上下文类加载器创建ServiceLoader
public static <S> ServiceLoader<S> load(Class<S> service) {
    // 线程上下文类加载器
	ClassLoader cl = Thread.currentThread().getContextClassLoader();
	return ServiceLoader.load(service, cl);
}
// 根据指定的服务接口Class对象和类加载器创建ServiceLoader
public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader) {
	return new ServiceLoader<>(service, loader);
}
// 私有构造器，内部调用
private ServiceLoader(Class<S> svc, ClassLoader cl) {
	service = Objects.requireNonNull(svc, "Service interface cannot be null");
	loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
	acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
    // 清空providers缓存
	reload();
}
// 重新加载，相当于重新创建ServiceLoader，用于新的服务提供者安装到运行中的JVM的情况。
public void reload() {
    // 清空providers缓存
	providers.clear();
    // 生产懒加载迭代器
	lookupIterator = new LazyIterator(service, loader);
}
```

以上步骤显示了`ServiceLoader`的实例化过程。

### 获取迭代器并进行迭代

```java
Iterator<Log> iterator = loader.iterator();
while (iterator.hasNext()) {
	Log logImpl = iterator.next();
	logImpl.execute();
}
```

`iterator`对应于源码中的部分：

```java
public Iterator<S> iterator() {
    return new Iterator<S>() {
		// 缓存中已知的服务提供者实例
        Iterator<Map.Entry<String,S>> knownProviders = providers.entrySet().iterator();
		// 是否还有服务提供者
        public boolean hasNext() {
            // 首先判断缓存中是否还有服务提供者
            if (knownProviders.hasNext())
                return true;
            // 如无，通过懒加载迭代器查找
            return lookupIterator.hasNext();
        }

        // 返回服务提供者实例
        public S next() {
            // 首先判断缓存中是否还有服务提供者，如有，直接返回缓存中的实例
            if (knownProviders.hasNext())
                return knownProviders.next().getValue();
            // 如无，通过懒加载迭代器查找
            return lookupIterator.next();
        }

        // 不支持remove操作
        public void remove() {
            throw new UnsupportedOperationException();
        }

    };
}
```

从服务提供者查找过程可知：

- `hasNext()`: 先从`providers`缓存中查找，如果有，直接返回`true`；如果无，通过`LazyIterator`进行查找。
- `next()`: 先从`providers`缓存中获取，如果有，直接返回服务提供者实例；如果无，通过`LazyIterator`获取。

### LazyIterator分析

`LazyIterator`的`Field`: 

```java
// 服务接口Class对象
Class<S> service; 
// 类加载器
ClassLoader loader;
// 通过类加载器获取到的所有配置文件的URL
Enumeration<URL> configs = null;
// 对应单个配置文件中的所有服务实现类的全限定名ArrayList集合的迭代器
Iterator<String> pending = null;
// 下一个被加载、实例化、缓存的服务提供者类全限定名
String nextName = null;
```

上述的`service`和`loader`已经在`ServiceLoader`实例化的时候赋值过。`configs、pending、nextName`是在调用`hasNext()、next()`时赋值的。

#### hasNext()方法

```java
// 是否还有服务提供者实现
public boolean hasNext() {
    if (acc == null) {
        return hasNextService();
    } else {
        PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>() {
            public Boolean run() { return hasNextService(); }
        };
        return AccessController.doPrivileged(action, acc);
    }
}
// 是否还有服务提供者实现
private boolean hasNextService() {
    // 当前存在还未被加载、实例化的服务提供者类，返回true
    if (nextName != null) {
        return true;
    }
    // 加载所有配置，只会加载一次，重新加载需调用reload()方法
    if (configs == null) {
        try {
            // fullName: META-INF/services/com.wacai.middleware.javaspi.Log
            String fullName = PREFIX + service.getName();
            // 调用类加载器加载配置资源
            if (loader == null)
                configs = ClassLoader.getSystemResources(fullName);
            else
                configs = loader.getResources(fullName);
        } catch (IOException x) {
            fail(service, "Error locating configuration files", x);
        }
    }
    // 第一次到这里，或解析的配置文件中无有效的服务提供者，或某个配置文件的所有服务提供者加载完了。
    while ((pending == null) || !pending.hasNext()) {
        if (!configs.hasMoreElements()) {
            // 无配置文件可解析，返回false
            return false;
        }
        pending = parse(service, configs.nextElement());
    }
    // 下一个被处理的服务提供者类
    nextName = pending.next();
    return true;
}
```

`hasNextService()`实现逻辑：

- 首先使用`ClassLoader`加载所有配置文件到`configs`，比如`META-INF/services/com.wacai.middleware.javaspi.Log文件`;
- 对每个配置文件进行解析，因为一行一个实现类名，因此将此配置文件中的所有实现类全限定类名保存到迭代器`pending`对应的`ArrayList`中。
- 最后返回`nextName`，即下一个被处理的实现类。

#### next()方法

```java
// 下一个服务提供者实例
public S next() {
    if (acc == null) {
        return nextService();
    } else {
        PrivilegedAction<S> action = new PrivilegedAction<S>() {
            public S run() { return nextService(); }
        };
        return AccessController.doPrivileged(action, acc);
    }
}
// 下一个服务提供者实例
private S nextService() {
    // 如果无，直接抛出异常
    if (!hasNextService())
        throw new NoSuchElementException();
	// 本次会处理的服务提供者全限定类名
    String cn = nextName; 
    // nextName置为null，因为本次已经处理了
    nextName = null;
    // cn对应的Class对象
    Class<?> c = null;
    try {
        // 加载cn，不初始化
        c = Class.forName(cn, false, loader);
    } catch (ClassNotFoundException x) {
        // 找不到该类，抛出异常
        fail(service,
             "Provider " + cn + " not found");
    }
    // 如果实现类不是服务接口的子类的话，抛出异常
    if (!service.isAssignableFrom(c)) {
        fail(service,
             "Provider " + cn  + " not a subtype");
    }
    try {
        // 实例化实现类，并转为接口类型.
        // 注：只会被实例化一次
        S p = service.cast(c.newInstance());
        // 放入缓存中(key=全限定实现类类名, value=类实例), 此处不需要判断缓存中是否已存在该实例，
        // 因为在配置文件解析的时候已经做了是否已经存在该实例的检查。
        providers.put(cn, p);
        return p;
    } catch (Throwable x) {
        fail(service,
             "Provider " + cn + " could not be instantiated",
             x);
    }
    throw new Error();          // This cannot happen
}
```

`nextService()`实现逻辑：

- 首先加载`nextName`对应的实现类，如`com.wacai.middleware.javaspi.Log4j`；
- 使用指定的类加载器创建实例，并转为服务接口类型；
- 将该实例缓存在`providers`缓存中，**供后续查找**，并返回转型后的实现类实例。

在`next()`方法返回之后，程序拿到服务提供者实现类实例，就可以完成后续的逻辑。

## SPI扩展机制的特点和缺点

### 特点

所有的配置文件只会加载一次，服务提供者也只会被实例化一次，重新加载配置文件可使用`reload`方法。

### 缺点

- 全部实例化：在使用`SPI`查找具体的某个实现的时候，需要遍历所有的实现，并实例化，然后在循环中才能找到需要的实现。因此需要把所有的实现都实例化了，即便不需要，也都给实例化了。
- 查找耗时：查找一个具体的实现需要遍历查找。

##Reference

- [JDK SPI的实现原理](http://www.cnblogs.com/java-zhao/p/7617143.html)
- [Java中SPI机制深入及源码解析](http://cxis.me/2017/04/17/Java%E4%B8%ADSPI%E6%9C%BA%E5%88%B6%E6%B7%B1%E5%85%A5%E5%8F%8A%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90/)
