# Proxy动态代理源码分析

Proxy类主要用于实现[动态代理机制](https://xuanjian1992.top/2019/03/17/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F%E6%80%BB%E7%BB%93/)，即代理类在程序运行时创建。下面先从一个实例出发，看看JDK如何实现动态代理。

## 一、实例

首先定义一个代理接口以及其实现类:

```java
public interface Subject { // 代理接口
    void request();
}

public class RealSubject implements Subject { // 实现
    @Override
    public void request() {
        System.out.println("real");
    }
}
```

其次，定义方法调用的处理Handler:

```java
public class ProxyHandler implements InvocationHandler {
    private Subject subject; // 持有被代理对象的引用

    public ProxyHandler(Subject subject) {
        this.subject = subject;
    }

  	// 方法调用， proxy为生成的代理类实例
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("before");
        Object res = method.invoke(subject, args);
        System.out.println("after");
        return res;
    }
}
// 调用处理器
public interface InvocationHandler {
		// 动态代理方法调用处理
  	// proxy:代理类实例，method调用方法，args调用参数
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable;
}
```

下面给出动态代理的效果:

```java
public class DynamicProxyDemo {
    public static void main(String[] args) {
        RealSubject realSubject = new RealSubject(); // 被代理实例
      	// 代理类实例
        Subject s = (Subject)  Proxy.newProxyInstance(realSubject.getClass().getClassLoader(),
                new Class[]{Subject.class}, new ProxyHandler(realSubject));
        s.request(); // 在代理类实例上调用request方法
        System.out.println(s.getClass().getName()); // 代理类的全限定类名
    }
}

// 输出:
before
real
after
com.sun.proxy.$Proxy0
```

根据测试结果可见，在被代理对象realSubject的request方法调用前后，执行了类似于AOP的代码。这是依赖于JDK的Proxy API实现的动态代理机制，在程序运行前并不存在代理类代码，而是在程序运行期间由Proxy API动态生成的。下面分析Proxy动态代理的实现机制。

## 二、Proxy类分析

### 1.重要属性

```java
/** parameter types of a proxy class constructor */ // 代理类构造器参数的类型
private static final Class<?>[] constructorParams =
    { InvocationHandler.class };

// a cache of proxy classes // 代理类的缓存 WeakCache<ClassLoader, 代理接口数组, 代理类>
private static final WeakCache<ClassLoader, Class<?>[], Class<?>>
    proxyClassCache = new WeakCache<>(new KeyFactory(), new ProxyClassFactory());

// the invocation handler for this proxy instance. 
// 代理类实例对应的调用处理器，在生成代理类实例的时候初始化
protected InvocationHandler h;
```

### 2.构造器

```java
private Proxy() { // 私有构造器，禁止外部初始化
}

protected Proxy(InvocationHandler h) { // 从动态代理类(Proxy子类)构造一个Proxy实例，由子类调用
    Objects.requireNonNull(h);
    this.h = h;
}
```

### 3.创建/获取代理类getProxyClass

```java
// loader: 类加载器，interfaces: 代理接口
public static Class<?> getProxyClass(ClassLoader loader, Class<?>... interfaces)
    throws IllegalArgumentException
{
    final Class<?>[] intfs = interfaces.clone(); // 代理接口数组浅拷贝
    final SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
        checkProxyAccess(Reflection.getCallerClass(), loader, intfs); // 检查创建代理类的权限
    }

    return getProxyClass0(loader, intfs); // 生成代理类或从缓存中获取
}
```

getProxyClass0实现:

```java
// loader: 类加载器，interfaces: 代理接口
private static Class<?> getProxyClass0(ClassLoader loader, Class<?>... interfaces) {
    if (interfaces.length > 65535) { // 代理接口数目不能超过65535
        throw new IllegalArgumentException("interface limit exceeded");
    }

  	// 由ClassLoader loader定义的并且实现了给定代理接口的代理类如果已存在，则直接返回缓存的代理类；
  	// 否则通过ProxyClassFactory生成代理类并缓存。
    return proxyClassCache.get(loader, interfaces);
}
```

如果根据给定的类加载器和代理接口数组，代理类已经缓存于proxyClassCache(**WeakCache，代理类缓存**)，则直接返回缓存的结果；否则需要使用ProxyClassFactory生成代理类并缓存。

```java
private static final WeakCache<ClassLoader, Class<?>[], Class<?>>
        proxyClassCache = new WeakCache<>(new KeyFactory(), new ProxyClassFactory());
```

下面先看WeakCache的结构，再分析其get(K key, P parameter)方法的实现。

#### (1) WeakCache类定义

```java
// K: ClassLoader, P: Class<?>[], V: Proxy子类，代理类
final class WeakCache<K, P, V> { // 用于缓存生成的代理类
		// 引用队列，用于K(ClassLoader)被GC回收时，保存CacheKey。
    private final ReferenceQueue<K> refQueue = new ReferenceQueue<>();
    // the key type is Object for supporting null key
  	// 缓存代理类<CacheKey(ClassLoader), <Key1、Key2、KeyX等实例, CacheValue(代理类)>>
    private final ConcurrentMap<Object, ConcurrentMap<Object, Supplier<V>>> map
        = new ConcurrentHashMap<>();
  	// <CacheValue(代理类), TRUE>
    private final ConcurrentMap<Supplier<V>, Boolean> reverseMap
        = new ConcurrentHashMap<>();
    private final BiFunction<K, P, ?> subKeyFactory; // Proxy.KeyFactory类实例，生成subKey
    private final BiFunction<K, P, V> valueFactory; // Proxy.ProxyClassFactory类实例，生成value

  	// 构造WeakCache实例
    // subKeyFactory：(key, parameter) -> sub-key，valueFactory：(key, parameter) -> value
    public WeakCache(BiFunction<K, P, ?> subKeyFactory, BiFunction<K, P, V> valueFactory) {
        this.subKeyFactory = Objects.requireNonNull(subKeyFactory);
        this.valueFactory = Objects.requireNonNull(valueFactory);
    }
 		// ...下面代码省略   
}
```

#### (2) WeakCache.get方法

Proxy.getProxyClass0方法中调用了这个方法，用于获取缓存的代理类，或者生成代理类并缓存。

```java
// K key: ClassLoader, P parameter: 代理接口数组 Class<?>[]，返回代理类，即Proxy子类
public V get(K key, P parameter) {
    Objects.requireNonNull(parameter); // parameter不能为null

    expungeStaleEntries(); // 删除遗留的无效entry

    Object cacheKey = CacheKey.valueOf(key, refQueue); // 生成CacheKey，弱引用key

    // lazily install the 2nd level valuesMap for the particular cacheKey
    ConcurrentMap<Object, Supplier<V>> valuesMap = map.get(cacheKey);
    if (valuesMap == null) {
        ConcurrentMap<Object, Supplier<V>> oldValuesMap
            = map.putIfAbsent(cacheKey, valuesMap = new ConcurrentHashMap<>());
        if (oldValuesMap != null) { // 本次putIfAbsent插入之前，其他线程已经插入了map
            valuesMap = oldValuesMap; // 使用已插入的map
        }
    }

    // create subKey and retrieve the possible Supplier<V> stored by that
    // subKey from valuesMap
  	// 创建subKey，并从valuesMap获取Supplier<V>
    Object subKey = Objects.requireNonNull(subKeyFactory.apply(key, parameter));
    Supplier<V> supplier = valuesMap.get(subKey);
    Factory factory = null; // 作用：创建代理类，并包装为CacheValue，缓存在valuesMap中

    while (true) {
	      // supplier可能是已缓存的CacheValue实例，或者是用于创建CacheValue的Factory
        if (supplier != null) { 
            // supplier might be a Factory or a CacheValue<V> instance
            V value = supplier.get(); // 获取value(获取代理类缓存或者创建代理类)
            if (value != null) { // 从CacheValue中获取代理类，或者Factory创建代理类成功，返回代理类
                return value;
            }
        }
      	// 到这里说明
      		1.缓存中无supplier. 或者
          2.由于CacheValue弱引用value，即代理类Class，当代理类被GC时，此时CacheValue无效，
            CacheValue.get()返回null. 或者
          3.Factory创建代理类失败(CacheValue)
        // else no supplier in cache
        // or a supplier that returned null (could be a cleared CacheValue
        // or a Factory that wasn't successful in installing the CacheValue)

        // lazily construct a Factory
        if (factory == null) { // 创建Factory实例
            factory = new Factory(key, parameter, subKey, valuesMap);
        }

        if (supplier == null) { // supplier为空，放入valuesMap
            supplier = valuesMap.putIfAbsent(subKey, factory);// null或已插入的supplier
            if (supplier == null) {
                // successfully installed Factory
                supplier = factory; // supplier成功插入valuesMap
            }
	          // 在当前线程将supplier插入valuesMap之前，已经有其他线程将supplier插入了valuesMap，
          	// 则使用已插入的supplier，重试
            // else retry with winning supplier 
        } else {
          	// supplier不为null，从上面代码supplier.get()看，是获取value失败了，
          	// 因此用新的factory替换原supplier
            if (valuesMap.replace(subKey, supplier, factory)) {
                // successfully replaced
                // cleared CacheEntry / unsuccessful Factory
                // with our Factory
                supplier = factory; // 替换无效的CacheValue或创建代理类失败的Factory为新factory
            } else {
                // retry with current supplier
                supplier = valuesMap.get(subKey); // 替换失败，则使用当前的supplier重试
            }
        }
    }
}
// 删除遗留的无效entry
private void expungeStaleEntries() {
    CacheKey<K> cacheKey;
 		// CacheKey弱引用Key(ClassLoader)，当Key被GC回收时，CacheKey会被放入引用队列
    while ((cacheKey = (CacheKey<K>)refQueue.poll()) != null) {
        cacheKey.expungeFrom(map, reverseMap); // 清理两个map
    }
}
```

`subKeyFactory.apply(key, parameter)`实际调用的是Proxy.KeyFactory.apply方法:

```java
// 根据ClassLoader，代理接口数组生成subKey
private static final class KeyFactory implements BiFunction<ClassLoader, Class<?>[], Object>
{
    @Override
    public Object apply(ClassLoader classLoader, Class<?>[] interfaces) {
    		// 根据代理接口数量来生成subKey，即Key1、Key2、KeyX的实例或key0
        switch (interfaces.length) {
            case 1: return new Key1(interfaces[0]); // the most frequent
            case 2: return new Key2(interfaces[0], interfaces[1]);
            case 0: return key0;
            default: return new KeyX(interfaces);
        }
    }
}
```

Proxy中的Key1、Key2、KeyX、key0定义如下：

```java
// 代理类如果没有实现接口，使用的key
private static final Object key0 = new Object();

// 代理类如果实现了1个接口，使用的key。Key1弱引用该接口
private static final class Key1 extends WeakReference<Class<?>> {
    private final int hash;

    Key1(Class<?> intf) {
        super(intf); // 弱引用
        this.hash = intf.hashCode(); // 接口hash
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        Class<?> intf;
        return this == obj ||
               obj != null &&
               obj.getClass() == Key1.class &&
               (intf = get()) != null &&
               intf == ((Key1) obj).get();
    }
}

// 代理类如果实现了2个接口，使用的key。Key2弱引用这两个接口
private static final class Key2 extends WeakReference<Class<?>> {
    private final int hash;
    private final WeakReference<Class<?>> ref2;

    Key2(Class<?> intf1, Class<?> intf2) {
        super(intf1); // 弱引用intf1
        hash = 31 * intf1.hashCode() + intf2.hashCode(); // 计算hash
        ref2 = new WeakReference<Class<?>>(intf2); // 弱引用intf2
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        Class<?> intf1, intf2;
        return this == obj ||
               obj != null &&
               obj.getClass() == Key2.class &&
               (intf1 = get()) != null &&
               intf1 == ((Key2) obj).get() &&
               (intf2 = ref2.get()) != null &&
               intf2 == ((Key2) obj).ref2.get();
    }
}

// 代理类如果实现了3个及以上的接口，使用的key。KeyX弱引用这些接口
private static final class KeyX {
    private final int hash;
    private final WeakReference<Class<?>>[] refs; // 弱引用

    @SuppressWarnings("unchecked")
    KeyX(Class<?>[] interfaces) {
        hash = Arrays.hashCode(interfaces); // 计算hash
        refs = (WeakReference<Class<?>>[])new WeakReference<?>[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            refs[i] = new WeakReference<>(interfaces[i]); // 弱引用
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj ||
               obj != null &&
               obj.getClass() == KeyX.class &&
               equals(refs, ((KeyX) obj).refs);
    }

    private static boolean equals(WeakReference<Class<?>>[] refs1,
                                  WeakReference<Class<?>>[] refs2) {
        if (refs1.length != refs2.length) { // length比较
            return false;
        }
        for (int i = 0; i < refs1.length; i++) {
            Class<?> intf = refs1[i].get();
            if (intf == null || intf != refs2[i].get()) { // 遍历依次比较
                return false;
            }
        }
        return true;
    }
}
```

#### (3) WeakCache.CacheKey类

CacheKey实例作为`private final ConcurrentMap<Object, ConcurrentMap<Object, Supplier<V>>> map = new ConcurrentHashMap<>();`的第一级Key。

```java
// CacheKey弱引用key，即ClassLoader
private static final class CacheKey<K> extends WeakReference<K> {

    // a replacement for null keys
    private static final Object NULL_KEY = new Object();
		// 生成CacheKey
    static <K> Object valueOf(K key, ReferenceQueue<K> refQueue) {
        return key == null
               // null key means we can't weakly reference it,
               // so we use a NULL_KEY singleton as cache key
               ? NULL_KEY
               // non-null key requires wrapping with a WeakReference
               : new CacheKey<>(key, refQueue); // 弱引用
    }

    private final int hash;

    private CacheKey(K key, ReferenceQueue<K> refQueue) {
        super(key, refQueue); // 弱引用key
        this.hash = System.identityHashCode(key);  // compare by identity == 比较
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        K key;
        return obj == this ||
               obj != null &&
               obj.getClass() == this.getClass() &&
               // cleared CacheKey is only equal to itself 
          		 // 如果CacheKey已经无效(key被GC回收)，则它只与自己相等。 == 比较
               (key = this.get()) != null &&
               // compare key by identity ==比较
               key == ((CacheKey<K>) obj).get();
    }

  	// private final ConcurrentMap<Object, ConcurrentMap<Object, Supplier<V>>> map = new ConcurrentHashMap<>();
    // private final ConcurrentMap<Supplier<V>, Boolean> reverseMap = new ConcurrentHashMap<>();
    void expungeFrom(ConcurrentMap<?, ? extends ConcurrentMap<?, ?>> map,
                     ConcurrentMap<?, Boolean> reverseMap) {
        // removing just by key is always safe here because after a CacheKey
        // is cleared and enqueue-ed it is only equal to itself
        // (see equals method)... == 比较，总是可以安全的移除valuesMap
        ConcurrentMap<?, ?> valuesMap = map.remove(this); // 删除valuesMap
        // remove also from reverseMap if needed
        if (valuesMap != null) {
            for (Object cacheValue : valuesMap.values()) {
                reverseMap.remove(cacheValue); // 清除reverseMap缓存
            }
        }
    }
}
```

#### (4)  WeakCache.CacheValue类

CacheValue实例作为`private final ConcurrentMap<Object, ConcurrentMap<Object, Supplier<V>>> map = new ConcurrentHashMap<>();`第二级key的value。

```java
private interface Value<V> extends Supplier<V> {}

// 弱引用value，即代理类Class
private static final class CacheValue<V> extends WeakReference<V> implements Value<V>
{
    private final int hash;

    CacheValue(V value) {
        super(value); // 弱引用
        this.hash = System.identityHashCode(value); // compare by identity == 比较
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        V value;
        return obj == this ||
               obj instanceof Value &&
               // cleared CacheValue is only equal to itself
               // 如果CacheValue已经无效(value被GC回收)，则它只与自己相等。 == 比较
               (value = get()) != null &&
               value == ((Value<?>) obj).get(); // compare by identity ==比较
    }
}
```

#### (5) WeakCache.Factory类(重要)

从WeakCache的get方法可以看出，在代理类缓存不存在的情况下，会使用Factory生成代理类并包装为CacheValue，最后缓存在valueMap中。

```java
// 生成代理类并包装为CacheValue，最后缓存在valueMap中
private final class Factory implements Supplier<V> {

    private final K key; // ClasLoader
    private final P parameter; // 代理接口数组
    private final Object subKey; // Key1、Key2、KeyX等实例
    private final ConcurrentMap<Object, Supplier<V>> valuesMap; // 根据subKey得到的valuesMap

    Factory(K key, P parameter, Object subKey,
            ConcurrentMap<Object, Supplier<V>> valuesMap) {
        this.key = key;
        this.parameter = parameter;
        this.subKey = subKey;
        this.valuesMap = valuesMap;
    }

    @Override
    public synchronized V get() { // serialize access 同步，串行获取
        // re-check 再次检查valuesMap中subKey对应的supplier是否已改变
        Supplier<V> supplier = valuesMap.get(subKey); 
        if (supplier != this) { // 已改变
            // something changed while we were waiting:
            // might be that we were replaced by a CacheValue
            // or were removed because of failure ->
            // return null to signal WeakCache.get() to retry
            // the loop 重试
          	// 因为get是同步方法，有可能是我们在等待的时候发生了如下事：
							1.其他线程生成了代理类，并包装为CacheValue，替换了当前的supplier。或者
              2.因为生成代理类失败，所以当前supplier被删除了。见下面的代码
            return null;
        }
        // else still us (supplier == this) // supplier未改变

        // create new value // 创建代理类
        V value = null;
        try {
          	// 使用Proxy.ProxyClassFactory类生成代理类。若失败，则抛出空指针异常，退出
            value = Objects.requireNonNull(valueFactory.apply(key, parameter));
        } finally {
            if (value == null) { // remove us on failure 生成失败，移除该<subKey, Factory>映射
                valuesMap.remove(subKey, this);
            }
        }
        // the only path to reach here is with non-null value
        assert value != null;

        // wrap value with CacheValue (WeakReference)
        CacheValue<V> cacheValue = new CacheValue<>(value); // 包装

        // try replacing us with CacheValue (this should always succeed)
        if (valuesMap.replace(subKey, this, cacheValue)) { // factory替换为CacheValue
            // put also in reverseMap
            reverseMap.put(cacheValue, Boolean.TRUE); // 更新reverseMap
        } else {
            throw new AssertionError("Should not reach here");
        }

        // successfully replaced us with new CacheValue -> return the value
        // wrapped by it
        return value; // 返回代理类
    }
}
```

这里`valueFactory.apply(key, parameter)`实际调用的是Proxy.ProxyClassFactory.apply方法。这个方法是生成代理类的关键所在。如下所示：

```java
private static final class ProxyClassFactory
    implements BiFunction<ClassLoader, Class<?>[], Class<?>>
{
    // prefix for all proxy class names 代理类名的前缀
    private static final String proxyClassNamePrefix = "$Proxy";

    // next number to use for generation of unique proxy class names 代理类名构成的序号
    private static final AtomicLong nextUniqueNumber = new AtomicLong();

    @Override
    public Class<?> apply(ClassLoader loader, Class<?>[] interfaces) { // 生成代理类
				// interfaceSet用于检查interfaces是否存在重复接口
        Map<Class<?>, Boolean> interfaceSet = new IdentityHashMap<>(interfaces.length);
        for (Class<?> intf : interfaces) { // 遍历
            /*
             * Verify that the class loader resolves the name of this
             * interface to the same Class object.
             */
            Class<?> interfaceClass = null; // 验证ClassLoader能加载该接口
            try {
                interfaceClass = Class.forName(intf.getName(), false, loader);
            } catch (ClassNotFoundException e) {
            }
            if (interfaceClass != intf) {
                throw new IllegalArgumentException(
                    intf + " is not visible from class loader");
            }
            /*
             * Verify that the Class object actually represents an
             * interface. 验证该类代表一个接口
             */
            if (!interfaceClass.isInterface()) {
                throw new IllegalArgumentException(
                    interfaceClass.getName() + " is not an interface");
            }
            /*
             * Verify that this interface is not a duplicate. 接口不能重复
             */
            if (interfaceSet.put(interfaceClass, Boolean.TRUE) != null) {
                throw new IllegalArgumentException(
                    "repeated interface: " + interfaceClass.getName());
            }
        }
				// 代理类定义的包名
        String proxyPkg = null;     // package to define proxy class in
        int accessFlags = Modifier.PUBLIC | Modifier.FINAL; // 访问标志 public final

        /*
         * Record the package of a non-public proxy interface so that the
         * proxy class will be defined in the same package.  Verify that
         * all non-public proxy interfaces are in the same package.
         */
        // 1.记录非public接口的包名，然后代理类定义在相同的包名下
        // 2.验证所有非public的接口，都必须在同一个包下，否则抛出异常
        for (Class<?> intf : interfaces) {
            int flags = intf.getModifiers(); // 获取接口的访问标志
            if (!Modifier.isPublic(flags)) { // 若非public
                accessFlags = Modifier.FINAL; // 访问标志改为final
                String name = intf.getName();
                int n = name.lastIndexOf('.');
                String pkg = ((n == -1) ? "" : name.substring(0, n + 1));
                if (proxyPkg == null) {
                    proxyPkg = pkg; // 包名更新为接口的包名
                } else if (!pkg.equals(proxyPkg)) { // 必须在同一个包下
                    throw new IllegalArgumentException(
                        "non-public interfaces from different packages");
                }
            }
        }

        if (proxyPkg == null) { // 若不存在非public的接口，则代理类定义在com.sun.proxy下
            // if no non-public proxy interfaces, use com.sun.proxy package
            proxyPkg = ReflectUtil.PROXY_PACKAGE + "."; 
        }

        /*
         * Choose a name for the proxy class to generate.
         */
        long num = nextUniqueNumber.getAndIncrement();
        // e.g. com.sun.proxy.$Proxy0
        String proxyName = proxyPkg + proxyClassNamePrefix + num; // 代理类全限定类名

        /*
         * Generate the specified proxy class.
         */
        byte[] proxyClassFile = ProxyGenerator.generateProxyClass(
            proxyName, interfaces, accessFlags); // 使用ProxyGenerator生产代理类的字节数组
        try {
        		// 在loader ClassLoader中定义该代理类
            return defineClass0(loader, proxyName,
                                proxyClassFile, 0, proxyClassFile.length);
        } catch (ClassFormatError e) {
            /*
             * A ClassFormatError here means that (barring bugs in the
             * proxy class generation code) there was some other
             * invalid aspect of the arguments supplied to the proxy
             * class creation (such as virtual machine limitations
             * exceeded).
             */
            throw new IllegalArgumentException(e.toString());
        }
    }
}
// Proxy的defineClass0方法
private static native Class<?> defineClass0(ClassLoader loader, String name,
                                                byte[] b, int off, int len);
```

### 4.生成代理类实例newProxyInstance

newProxyInstance方法的逻辑可分为两步：首先调用Proxy.getProxyClass0方法获取代理类，然后反射获取其构造器并生成代理类实例。

```java
// 生成代理类实例，loader: 类加载器；interfaces: 代理接口；h: 调用处理器
public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces,
                                      InvocationHandler h) throws IllegalArgumentException
{
    Objects.requireNonNull(h);

    final Class<?>[] intfs = interfaces.clone(); // 代理接口数组浅拷贝
    final SecurityManager sm = System.getSecurityManager();
    if (sm != null) { // 检查代理类创建权限
        checkProxyAccess(Reflection.getCallerClass(), loader, intfs);
    }

    /*
     * Look up or generate the designated proxy class.
     */
    Class<?> cl = getProxyClass0(loader, intfs); // 查找或创建代理类

    /*
     * Invoke its constructor with the designated invocation handler.
     */
    try {
        if (sm != null) {
            checkNewProxyPermission(Reflection.getCallerClass(), cl);
        }

        final Constructor<?> cons = cl.getConstructor(constructorParams); // 获取构造器
        final InvocationHandler ih = h;
        if (!Modifier.isPublic(cl.getModifiers())) { // 代理类非public
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    cons.setAccessible(true); // 设置访问权限
                    return null;
                }
            });
        }
        return cons.newInstance(new Object[]{h}); // 反射实例化代理类实例
    } catch (IllegalAccessException|InstantiationException e) {
        throw new InternalError(e.toString(), e);
    } catch (InvocationTargetException e) {
        Throwable t = e.getCause();
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else {
            throw new InternalError(t.toString(), t);
        }
    } catch (NoSuchMethodException e) {
        throw new InternalError(e.toString(), e);
    }
}
```

### 5.判断是否是代理类isProxyClass

判断一个类是否是动态代理类。

```java
public static boolean isProxyClass(Class<?> cl) {
  	// 首先是要继承自Proxy，其次，缓存中必须存在
    return Proxy.class.isAssignableFrom(cl) && proxyClassCache.containsValue(cl);
}
```

判断WeakCache缓存中是否存在该类的细节:

```java
// WeakCache.containsValue方法
public boolean containsValue(V value) { // 判断缓存中是否存在该类
    Objects.requireNonNull(value);

    expungeStaleEntries(); // 删除遗留的无效entry
  	// 使用LookupValue查找，避免创建CacheValue实例， == 比较
    return reverseMap.containsKey(new LookupValue<>(value));
}
// WeakCache.LookupValue类
private static final class LookupValue<V> implements Value<V> {
    private final V value;

    LookupValue(V value) {
        this.value = value;
    }

    @Override
    public V get() { // 返回value
        return value;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(value); // compare by identity == 比较
    } 

    @Override
    public boolean equals(Object obj) {
        return obj == this ||
               obj instanceof Value &&
               this.value == ((Value<?>) obj).get();  // compare by identity == 比较，重要
    }
}
```

### 6.获取InvocationHander实例getInvocationHandler

```java
public static InvocationHandler getInvocationHandler(Object proxy) // proxy代理类实例
    throws IllegalArgumentException
{
    /*
     * Verify that the object is actually a proxy instance.
     */
    if (!isProxyClass(proxy.getClass())) { // 判断是否是代理类
        throw new IllegalArgumentException("not a proxy instance");
    }

    final Proxy p = (Proxy) proxy;
    final InvocationHandler ih = p.h; // InvocationHandler
    if (System.getSecurityManager() != null) {
        Class<?> ihClass = ih.getClass();
        Class<?> caller = Reflection.getCallerClass();
        if (ReflectUtil.needsPackageAccessCheck(caller.getClassLoader(),
                                                ihClass.getClassLoader()))
        {
            ReflectUtil.checkPackageAccess(ihClass);
        }
    }

    return ih;
}
```

## 三、代理类生成的扩展知识点(细节)

### 1.ProxyGenerator生成代理类

ProxyClassFactory实际使用ProxyGenerator来生成代理类。

```java
// 是否保存生成的代理类到文件，系统属性-Djdk.proxy.ProxyGenerator.saveGeneratedFiles=true
private static final boolean saveGeneratedFiles =
        java.security.AccessController.doPrivileged(
            new GetBooleanAction(
                "jdk.proxy.ProxyGenerator.saveGeneratedFiles")).booleanValue();

// ProxyGenerator生成代理类的方法。name: 代理类名，interfaces：代理接口，accessFlags：访问标志
static byte[] generateProxyClass(final String name, Class<?>[] interfaces, int accessFlags)
{
    ProxyGenerator gen = new ProxyGenerator(name, interfaces, accessFlags);
    final byte[] classFile = gen.generateClassFile(); // 调用generateClassFile生成代理类

    if (saveGeneratedFiles) { // 保存代理类
        java.security.AccessController.doPrivileged(
        new java.security.PrivilegedAction<Void>() {
            public Void run() {
                try {
                    int i = name.lastIndexOf('.');
                    Path path;
                    if (i > 0) {
                        Path dir = Paths.get(name.substring(0, i).replace('.', File.separatorChar));
                        Files.createDirectories(dir); // 创建目录
                        path = dir.resolve(name.substring(i+1, name.length()) + ".class");
                    } else {
                        path = Paths.get(name + ".class");
                    }
                    Files.write(path, classFile); // 写到文件
                    return null;
                } catch (IOException e) {
                    throw new InternalError(
                        "I/O exception saving generated file: " + e);
                }
            }
        });
    }

    return classFile;
}
```

generateProxyClass方法调用了ProxyGenerator.generateClassFile方法，生成代理类：

```java
private byte[] generateClassFile() {

    /* ============================================================
     * Step 1: Assemble ProxyMethod objects for all methods to
     * generate proxy dispatching code for. 生成需要代理的所有方法的ProxyMethod对象
     */

    /*
     * Record that proxy methods are needed for the hashCode, equals,
     * and toString methods of java.lang.Object.  This is done before
     * the methods from the proxy interfaces so that the methods from
     * java.lang.Object take precedence over duplicate methods in the
     * proxy interfaces.
     */
  	// hashCode(), equals(), toString()方法的声明类是Object.class，且优先于代理接口中的这些方法定义
    addProxyMethod(hashCodeMethod, Object.class);
    addProxyMethod(equalsMethod, Object.class);
    addProxyMethod(toStringMethod, Object.class);

    /*
     * Now record all of the methods from the proxy interfaces, giving
     * earlier interfaces precedence over later ones with duplicate
     * methods.
     */
  	// 如果代理接口之中存在名称和参数签名相同的重复方法，则代理接口数组中排在前面的代理接口中的方法
  	// 定义优先于后面的接口，即方法的声明接口为前面的接口
    for (Class<?> intf : interfaces) {
        for (Method m : intf.getMethods()) {
            addProxyMethod(m, intf);
        }
    }

    /*
     * For each set of proxy methods with the same signature,
     * verify that the methods' return types are compatible.
     */
    for (List<ProxyMethod> sigmethods : proxyMethods.values()) {
        checkReturnTypes(sigmethods); // 检查返回类型的兼容性
    }

    /* ============================================================
     * Step 2: Assemble FieldInfo and MethodInfo structs for all of
     * fields and methods in the class we are generating. 生成FieldInfo、MethodInfo
     */
    try {
        methods.add(generateConstructor());

        for (List<ProxyMethod> sigmethods : proxyMethods.values()) {
            for (ProxyMethod pm : sigmethods) {

                // add static field for method's Method object
                fields.add(new FieldInfo(pm.methodFieldName,
                    "Ljava/lang/reflect/Method;",
                     ACC_PRIVATE | ACC_STATIC));

                // generate code for proxy method and add it
                methods.add(pm.generateMethod());
            }
        }

        methods.add(generateStaticInitializer());

    } catch (IOException e) {
        throw new InternalError("unexpected I/O Exception", e);
    }

    if (methods.size() > 65535) {
        throw new IllegalArgumentException("method limit exceeded");
    }
    if (fields.size() > 65535) {
        throw new IllegalArgumentException("field limit exceeded");
    }

    /* ============================================================
     * Step 3: Write the final class file. 生成类文件
     */

    /*
     * Make sure that constant pool indexes are reserved for the
     * following items before starting to write the final class file.
     */
    cp.getClass(dotToSlash(className));
    cp.getClass(superclassName);
    for (Class<?> intf: interfaces) {
        cp.getClass(dotToSlash(intf.getName()));
    }

    /*
     * Disallow new constant pool additions beyond this point, since
     * we are about to write the final constant pool table.
     */
    cp.setReadOnly();

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream(bout);

    try {
        /*
         * Write all the items of the "ClassFile" structure.
         * See JVMS section 4.1.
         */
                                    // u4 magic;
        dout.writeInt(0xCAFEBABE);
                                    // u2 minor_version;
        dout.writeShort(CLASSFILE_MINOR_VERSION);
                                    // u2 major_version;
        dout.writeShort(CLASSFILE_MAJOR_VERSION);

        cp.write(dout);             // (write constant pool)

                                    // u2 access_flags;
        dout.writeShort(accessFlags);
                                    // u2 this_class;
        dout.writeShort(cp.getClass(dotToSlash(className)));
                                    // u2 super_class;
        dout.writeShort(cp.getClass(superclassName));

                                    // u2 interfaces_count;
        dout.writeShort(interfaces.length);
                                    // u2 interfaces[interfaces_count];
        for (Class<?> intf : interfaces) {
            dout.writeShort(cp.getClass(
                dotToSlash(intf.getName())));
        }

                                    // u2 fields_count;
        dout.writeShort(fields.size());
                                    // field_info fields[fields_count];
        for (FieldInfo f : fields) {
            f.write(dout);
        }

                                    // u2 methods_count;
        dout.writeShort(methods.size());
                                    // method_info methods[methods_count];
        for (MethodInfo m : methods) {
            m.write(dout);
        }

                                     // u2 attributes_count;
        dout.writeShort(0); // (no ClassFile attributes for proxy classes)

    } catch (IOException e) {
        throw new InternalError("unexpected I/O Exception", e);
    }

    return bout.toByteArray(); // 返回字节数组
}
```

### 2.代理类文件

从上面的代码可以看出，设置`jdk.proxy.ProxyGenerator.saveGeneratedFiles=true`系统属性，即可生成代理类文件到本地。最后生成的文件如下所示：

```java
package com.sun.proxy; // 代理接口都为public，则包名为com.sun.proxy；否则，为非public代理接口的包名

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import javabasiscode.Proxy.DynamicProxy.Subject;

public final class $Proxy0 extends Proxy implements Subject { // 继承自Proxy，实现代理接口
    private static Method m1; // 代理的方法
    private static Method m2;
    private static Method m3;
    private static Method m0;

    public $Proxy0(InvocationHandler var1) throws  { // 唯一构造器，传入InvocationHandler
        super(var1);
    }

    public final boolean equals(Object var1) throws  { // 代理equals()
        try {
            return (Boolean)super.h.invoke(this, m1, new Object[]{var1});
        } catch (RuntimeException | Error var3) {
            throw var3;
        } catch (Throwable var4) {
            throw new UndeclaredThrowableException(var4);
        }
    }

    public final String toString() throws  { // 代理toString()
        try {
            return (String)super.h.invoke(this, m2, (Object[])null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    public final void request() throws  { // 代理接口的方法
        try {
            super.h.invoke(this, m3, (Object[])null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    public final int hashCode() throws  { // 代理hashCode()
        try {
            return (Integer)super.h.invoke(this, m0, (Object[])null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    static {
        try {
            m1 = Class.forName("java.lang.Object").getMethod("equals", Class.forName("java.lang.Object"));
            m2 = Class.forName("java.lang.Object").getMethod("toString");
            m3 = Class.forName("javabasiscode.Proxy.DynamicProxy.Subject").getMethod("request");
            m0 = Class.forName("java.lang.Object").getMethod("hashCode");
        } catch (NoSuchMethodException var2) {
            throw new NoSuchMethodError(var2.getMessage());
        } catch (ClassNotFoundException var3) {
            throw new NoClassDefFoundError(var3.getMessage());
        }
    }
}
```

## 参考文献

- [JDK Proxy](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Proxy.html)
- [JDK InvocationHandler](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/InvocationHandler.html)
- JDK WeakCache
- JDK ProxyGenerator