# Java SPI扩展机制实现分析

​	`SPI`，即`Service Provider Interface` ，是`JDK`内置的服务发现机制。经常在实际工作中会用到`java.sql.Driver`接口，不同厂商可以根据该接口提供不同的实现，比如`mysql`和`postgresql`都有不同的实现提供给用户，而`Java`的`SPI`扩展机制则可以为某个接口寻找具体的服务实现。	

当服务的提供者提供了某个接口的实现后，需要在`ClassPath`下的`META-INF/services`目录下创建一个名为服务接口全限定名的配置文件，配置文件中的内容为这个接口具体实现类的全限定名。当其他程序需要这个服务时，在引用服务提供者的`jar`包后，通过查找`jar`包中`META-INF/services`的服务接口配置文件，该配置文件中有接口的具体实现类名，可以根据该类名进行加载、实例化服务提供者，因此能使用该服务。而定位、查找、实例化、缓存服务提供者实例的工作由`java.util.ServiceLoader`完成。

## SPI扩展机制举例说明

### SPI接口

```java
/**
 * Log interface.
 *
 * @author xuanjian.xuwj
 */
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
/**
 * Log SPI implementation By Log4j.
 *
 * @author xuanjian.xuwj
 */
public class Log4j implements Log{
    @Override
    public void execute() {
        System.out.println("Log4j...");
    }
}
```

实现二：

```java
/**
 * Log SPI implementation By Logback.
 *
 * @author xuanjian.xuwj
 */
public class Logback implements Log{
    @Override
    public void execute() {
        System.out.println("Logback...");
    }
}
```

### 增加META-INF配置文件

![](./pic/java spi-1.jpg)

```java
com.wacai.middleware.javaspi.Log4j
com.wacai.middleware.javaspi.Logback
```

