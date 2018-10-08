##`AtomicLong/AtomicInteger原子类解析`

1.构造器和函数API

```
// 构造函数
AtomicLong()
// 创建值为initialValue的AtomicLong对象
AtomicLong(long initialValue)
// 以原子方式设置当前值为newValue。
final void set(long newValue) 
// 获取当前值
final long get() 
// 以原子方式将当前值减 1，并返回减1后的值。等价于“--num”
final long decrementAndGet() 
// 以原子方式将当前值减 1，并返回减1前的值。等价于“num--”
final long getAndDecrement() 
// 以原子方式将当前值加 1，并返回加1后的值。等价于“++num”
final long incrementAndGet() 
// 以原子方式将当前值加 1，并返回加1前的值。等价于“num++”
final long getAndIncrement()    
// 以原子方式将delta与当前值相加，并返回相加后的值。
final long addAndGet(long delta) 
// 以原子方式将delta添加到当前值，并返回相加前的值。
final long getAndAdd(long delta) 
// 如果当前值 == expect，则以原子方式将该值设置为update。成功返回true，否则返回false，并且不修改原值。
final boolean compareAndSet(long expect, long update)
// 以原子方式设置当前值为newValue，并返回旧值。
final long getAndSet(long newValue)
// 返回当前值对应的int值
int intValue() 
// 获取当前值对应的long值
long longValue()    
// 以 float 形式返回当前值
float floatValue()    
// 以 double 形式返回当前值
double doubleValue()    
// 最后设置为给定值。延时设置变量值，这个等价于set()方法，但是由于字段是volatile类型的，
因此次字段的修改会比普通字段（非volatile字段）有稍微的性能延时（尽管可以忽略），所以如果
不是想立即读取设置的新值，允许在“后台”修改值，那么此方法就很有用。如果还是难以理解，这里
就类似于启动一个后台线程如执行修改新值的任务，原线程就不等待修改结果立即返回（这种解释其实
是不正确的，但是可以这么理解）。
final void lazySet(long newValue)
// 如果当前值 == 预期值，则以原子方式将该设置为给定的更新值。JSR规范中说：以原子方式读取和
有条件地写入变量但不 创建任何 happen-before 排序，因此不提供与除 weakCompareAndSet 目标
外任何变量以前或后续读取或写入操作有关的任何保证。大意就是说调用weakCompareAndSet时并不能
保证不存在happen-before的发生（也就是可能存在指令重排序导致此操作失败）。但是从Java源码来看
，其实此方法并没有实现JSR规范的要求，最后效果和compareAndSet是等效的，都调用了unsafe.compareAndSwapInt()
完成操作。
final boolean weakCompareAndSet(long expect, long update)
```


2.乐观锁和悲观锁
- `synchronized`是独占锁，会导致需要该锁的其他线程挂起，等待持有锁的线程释放锁，
是一种悲观锁，造成CPU吞吐率下降。
- 乐观锁的思路是每次不加锁，并且假设没有冲突去完成某项操作，如果因为冲突失
败了就重试(循环)，直到完成。(`AtomicLong`的CAS就是乐观锁实现，`Compare And Swap`)

3.`volatile+CAS`
> CAS有3个操作数，内存值V(`volatile`)、旧的预期值A、更新值B。当且仅当内存值等于旧的预期值时，将内存值
更新为更新值B.

4.`volatile`无法实现原子性，只能实现内存可见性
> - 对`volatile`变量执行读操作时，都要强制的先从主内存读取最新的变量值到工作内存，然后再读工作内存
中所存储的变量副本
> - 对`volatile`变量执行写操作时，又会强制的将工作内存中的刚刚改变的值写到主内存中去.因此，每个线程拿
到的`volatile`变量值都是最新的。

    private volatile int count = 0;

    //无法保证原子性，执行引擎读取工作内存变量副本，执行字节码的时候（这个过程）并非原子操作

    假设现在有两条线程分别对count执行加1操作，那么期待的结果最后count==2，但是看下边的分析：
    假设有如下流程：
    1）线程a获取了count==0；
    2）线程b获取了count==0；
    3）线程b对count+1，之后写入主内存count==1； 
    4）线程a对count+1，之后写入主内存count==1；
    结果count==1而非count==2，原因就是线程a获取count后，volatile不能实现原子性，这个时候b也能去操作count。

5.方法实现介绍`incrementAndGet()`
```
public final int incrementAndGet() {
    for (;;) {  //失败就重试
        int current = get();  //内存值
        int next = current + 1;  //更新值
        if (compareAndSet(current, next)) //预期值，更新值
            return next;
    }
}
```