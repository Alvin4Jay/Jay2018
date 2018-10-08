##JDK自带java进程分析的小工具

1. `jps`—— 用于查看有权访问的hotspot虚拟机的进程
    ```
    jps 默认列出VM标示符号LVMID和主类名
    jps -q 仅列出VM标识符号LVMID，不显示jar,class, main参数等信息
    jps -m 输出虚拟机进程启动时传递给主类main()主函数的参数
    jps -l 输出应用程序主类完整package名称或jar完整名称
    jps -v 列出jvm参数
    ```
2. `jstat`——监视虚拟机各种运行参数，本地或远程虚拟机进程中的类装载、内存、垃圾回收等
    ```
    jstat -gc + pid号
    jstat -gc 2764 250 20 //每250ms查询一次进程2764的垃圾收集情况，一共查询20次
          -class  监控类装载信息
          -gc  垃圾收集信息
          -compiler 输出JIT编译器编译过的方法、耗时等信息
    ```
3. `jinfo`——查看Java配置信息
    ```
    jinfo -flag 参数项 进程id  //虚拟机进程该参数项的值
    jinfo -sysprops 进程id  //查看虚拟机进程的System.getProperties()的内容    
    ```
4. `jmap`——Java内存映像工具,生成堆转储快照heapdump
    ```
    jmap -dump 进程id  //生成heapdump文件
    jmap -heap 进程id  //显示java堆详细信息
    jmap -histo 进程id //显示堆中对象统计信息
    ```
5. `jhat`——分析堆转储快照heapdump
    ```
    jhat eclipse.bin  //分析dump文件 eclipse.bin
    ```
6. `jstack`——Java堆栈跟踪工具，生成虚拟机当前时刻是的线程快照threaddump
    ```
    jstack -l 进程id  //显示堆栈、锁信息
    
    ```
7. `java -jar` `java -cp`
    
    ```
    java -jar xxx.jar  执行该命令时，会用到目录META-INF\MANIFEST.MF文件，在该文件中，有一个叫Main－Class的参数，它说明了java -jar命令执行的类。
    
    java -cp .;c:\classes01\myClass.jar;c:\classes02\*.jar  packname.mainclassname   -cp指定类运行所需要的jar包，使用全路径。后跟主类名
    
    用maven导出的包中，如果没有在pom文件中将依赖包打进去，是没有依赖包。
    1.打包时指定了主类，可以直接用java -jar xxx.jar。
    2.打包时没有指定主类，可以用java -cp xxx.jar 主类名称（绝对路径）。
    3.要引用其他的jar包，可以用java -classpath $CLASSPATH:xxxx.jar 主类名称（绝对路径）。其中 -classpath 指定需要引入的类。
    ```
    
8. `java` `javac` `javadoc` `javap`
    
    ```
    javac 编译类  javac xxx.java
    java 运行类  java xxx
    javadoc [option] [packagenames] [sourcefiles] , option有-public -private 等
    javap -verbose 查看字节码文件信息
    
    ```
    
9. `jcmd` 与`jmap`类似，但更加强大

    ```
    jcmd -l 列出所有的虚拟机
    jcmd pid help //列出该虚拟机支持的命令
    jcmd pid VM.uptime //虚拟机启动时间
    jcmd pid Thread.print  //打印线程栈信息
    jcmd pid GC.class_histogram  //类统计信息
    ```
    
8. `jconsole` 分析性能

    ```
    概述: Displays overview information about the Java VM and monitored values.
    内存: 显示内存使用信息
    线程: 显示线程使用信息
    类: 显示类装载信息
    *VM摘要:显示java VM信息
    MBeans: 显示 MBeans.
    ```
    



