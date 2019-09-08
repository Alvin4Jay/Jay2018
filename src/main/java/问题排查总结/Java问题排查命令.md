## 1. 查看线程堆栈状态

```shell
jstack -l <pid> |  grep java.lang.Thread.State | awk '{print $2$3$4$5}' | sort | uniq -c
```

输出：

```shell
23 BLOCKED(onobjectmonitor)
28 RUNNABLE
 2 TIMED_WAITING(onobjectmonitor)
10 TIMED_WAITING(parking)
 5 TIMED_WAITING(sleeping)
 2 WAITING(onobjectmonitor)
15 WAITING(parking)
```

## 2.查看线程状态为TIMED_WAIT的线程

```shell
jstack <pid> |   grep 'TIMED_WAIT' -B 1 | grep 'nid'
```

输出：

```java
"Keep-Alive-Timer" #8234 daemon prio=8 os_prio=0 tid=0x00007f3798004800 nid=0x266b waiting on condition [0x00007f38962da000]
"ninja-metric-reporter" #111 daemon prio=5 os_prio=0 tid=0x00007f38efb25000 nid=0xf1 waiting on condition [0x00007f37d41e2000]
"http-nio-8080-AsyncTimeout" #107 daemon prio=5 os_prio=0 tid=0x00007f38ec3eb800 nid=0xef waiting on condition [0x00007f37d43e4000]
"cacheAllAppNames-update-thread" #92 daemon prio=5 os_prio=0 tid=0x00007f37b928b800 nid=0xe0 waiting on condition [0x00007f37d5af3000]
...
```

