# AQS源码分析1——独占锁的获取与释放

AQS(`AbstractQueuedSynchronizer`)是Java中众多锁以及并发工具的基础，其底层采用乐观锁，大量使用了CAS操作， 并且在冲突时，采用自旋方式重试，以实现轻量级和高效地获取锁。

AQS虽然被定义为抽象类，但事实上它并不包含任何抽象方法。这是因为AQS是被设计来支持多种用途的，如果定义抽象方法，则子类在继承时必须要覆写所有的抽象方法，这显然是不合理的。所以AQS将一些需要子类覆写的方法都设计成`protected`方法，将其默认实现为抛出`UnsupportedOperationException`异常。如果子类使用到这些方法，但是没有覆写，则会抛出异常；如果子类没有使用到这些方法，则不需要做任何操作。

AQS中实现了锁的获取框架，锁的实际获取逻辑交由子类去实现，就锁的获取操作而言，子类必须重写 `tryAcquire`方法。

## AQS核心实现

### 状态

在AQS中，状态是由`state`属性来表示的，是`volatile`类型的变量。

```java
private volatile int state; // The synchronization state.
```

该属性的值即表示了锁的状态，`state`为0表示锁没有被占用，`state`大于0表示当前已经有线程持有该锁，这里之所以说大于0而不说等于1是因为可能存在可重入的情况。你可以把`state`变量当做是当前持有该锁的线程数量。

对于独占锁，同一时刻，锁只能被一个线程所持有。通过`state`变量是否为0，可以分辨当前锁是否被占用，但光知道锁是不是被占用是不够的，我们并不知道占用锁的线程是哪一个。在AQS中，可通过`exclusiveOwnerThread`属性来确定：

```java
private transient Thread exclusiveOwnerThread; // 继承自AbstractOwnableSynchronizer
```

`exclusiveOwnerThread`属性的值即为当前持有锁的线程。

### 队列

AQS中，队列的实现是一个双向链表，被称为`sync queue`，它表示**所有等待锁的线程的集合**。在并发编程中使用队列，通常是**将当前线程包装成某种类型的数据结构扔到等待队列中**，先来看看队列中的每一个节点的结构：

```java
static final class Node {
    static final Node SHARED = new Node();
    static final Node EXCLUSIVE = null;

    static final int CANCELLED =  1;
    static final int SIGNAL    = -1;
    static final int CONDITION = -2;
    static final int PROPAGATE = -3;

    volatile int waitStatus;
    volatile Node prev;
    volatile Node next;
    volatile Thread thread;
    Node nextWaiter;

    final boolean isShared() {
        return nextWaiter == SHARED;
    }

    final Node predecessor() throws NullPointerException {
        Node p = prev;
        if (p == null)
            throw new NullPointerException();
        else
            return p;
    }

    Node() {    // Used to establish initial head or SHARED marker
    }

    Node(Thread thread, Node mode) {     // Used by addWaiter
        this.nextWaiter = mode;
        this.thread = thread;
    }

    Node(Thread thread, int waitStatus) { // Used by Condition
        this.waitStatus = waitStatus;
        this.thread = thread;
    }
}
```

这个结构的属性可以分为下面4类：

```java
// 节点所代表的线程
volatile Thread thread;

// 双向链表，每个节点需要保存自己的前驱节点和后继节点的引用
volatile Node prev;
volatile Node next;

// 线程所处的等待锁的状态，初始化时值为0
volatile int waitStatus;
static final int CANCELLED =  1;
static final int SIGNAL    = -1;
static final int CONDITION = -2;
static final int PROPAGATE = -3;

// 该属性用于condition条件队列或者共享锁
Node nextWaiter;
```

注意，在这个Node类中也有一个状态变量`waitStatus`，它表示了当前Node所代表的线程的等待锁的状态，在独占锁模式下，只需要关注`CANCELLED` `SIGNAL`两种状态即可。这里还有一个`nextWaiter`属性，它在独占锁模式下永远为`null`，仅仅起到一个标记作用，没有实际意义。

解释完队列中的节点，关于这个`sync queue`，既然是双向链表，操纵它自然只需要一个头结点和一个尾节点：

```java
// 头结点，不代表任何线程，是一个哑结点
private transient volatile Node head;

// 尾节点，每一个请求锁的线程会加到队尾
private transient volatile Node tail;
```

于是可以得到`sync queue`的结构

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/AQS-1.png)

在AQS中的队列是一个CLH队列，它的head节点永远是一个哑结点（dummy node), 它不代表任何线程（某些情况下可以看做是代表了当前持有锁的线程），**因此head所指向的Node的thread属性永远是null**。只有从次头节点往后的所有节点才代表了所有等待锁的线程。也就是说，在当前线程没有抢到锁被包装成Node扔到队列中时，**即使队列是空的，它也会排在第二个**，我们会在它的前面新建一个dummy节点(具体的代码在后面分析源码时再详细讲)。为了便于描述，下文中把除去head节点的队列称作是**等待队列**，在这个队列中的节点才代表了所有等待锁的线程：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/AQS-2.png)

在继续往下之前再对着上图总结一下Node节点各个参数的含义：

- `thread`：表示当前Node所代表的线程
- `waitStatus`：表示节点所处的等待状态，独占锁模式下关注`SIGNAL` `CANCELLED` 这两种状态，共享锁模式下关注`SIGNAL` `CANCELLED` `初始态(0)`三种状态。
- `prev` `next`：节点的前驱和后继
- `nextWaiter`：进作为标记，值永远为null，表示当前处于独占锁模式

### CAS操作

CAS操作大对数是用来改变状态state的，且一般在静态代码块中初始化需要CAS操作的属性的偏移量：

```java
private static final Unsafe unsafe = Unsafe.getUnsafe();
private static final long stateOffset;
private static final long headOffset;
private static final long tailOffset;
private static final long waitStatusOffset;
private static final long nextOffset;

static {
    try {
        stateOffset = unsafe.objectFieldOffset
            (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
        headOffset = unsafe.objectFieldOffset
            (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
        tailOffset = unsafe.objectFieldOffset
            (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
        waitStatusOffset = unsafe.objectFieldOffset
            (Node.class.getDeclaredField("waitStatus"));
        nextOffset = unsafe.objectFieldOffset
            (Node.class.getDeclaredField("next"));

    } catch (Exception ex) { throw new Error(ex); }
}
```

从这个静态代码块中也可以看出，CAS操作主要针对5个属性，包括AQS的3个属性`state`,`head`和`tail`, 以及Node对象的两个属性`waitStatus`,`next`。说明这5个属性基本是会被多个线程同时访问的。

定义完属性的偏移量之后，接下来就是CAS操作本身：

```java
protected final boolean compareAndSetState(int expect, int update) {
    return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}
private final boolean compareAndSetHead(Node update) {
    return unsafe.compareAndSwapObject(this, headOffset, null, update);
}
private final boolean compareAndSetTail(Node expect, Node update) {
    return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
}
private static final boolean compareAndSetWaitStatus(Node node, int expect,int update) {
    return unsafe.compareAndSwapInt(node, waitStatusOffset, expect, update);
}
private static final boolean compareAndSetNext(Node node, Node expect, Node update) {
    return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
}
```

最终CAS操作调用的是Unsafe类的`compareAndSwapXXX`方法。

## AQS核心属性

前面分析了AQS的状态，队列和CAS操作，对这个工具类有了初步的认识。接下来在进入源码分析之前，先来总结下AQS核心属性：

（1）锁相关的属性有两个：

```java
// 锁的状态
private volatile int state; 
// 当前持有锁的线程，注意这个属性是从AbstractOwnableSynchronizer继承而来
private transient Thread exclusiveOwnerThread; 
```

（2）`sync queue`相关的属性有两个：

```java
private transient volatile Node head; // 队头，为dummy node
private transient volatile Node tail; // 队尾，新入队的节点
```

（3）队列中的Node中需要关注的属性有三组：

```java
// 节点所代表的线程
volatile Thread thread;

// 双向链表，每个节点需要保存自己的前驱节点和后继节点的引用
volatile Node prev;
volatile Node next;

// 线程所处的等待锁的状态，初始化时，该值为0
volatile int waitStatus;
static final int CANCELLED =  1;
static final int SIGNAL    = -1;
```

## 独占锁的获取

前面已经提到, AQS大多数情况下都是通过继承来使用的, 子类通过覆写 `tryAcquire` 来实现自己的获取锁的逻辑，这里以ReentrantLock为例来说明锁的获取流程。值得注意的是, ReentrantLock有 `公平锁` 和 `非公平锁` 两种实现, 默认实现为非公平锁, 这体现在它的构造函数中。`FairSync` 继承自 `Sync`, 而`Sync`继承自 `AQS`, ReentrantLock获取锁的逻辑是直接调用了 `FairSync` 或者 `NonfairSync`的逻辑。这里以`FairSync`为例， 来逐行分析独占锁的获取:

```java
static final class FairSync extends Sync {
    private static final long serialVersionUID = -3000897897090466540L;
    //获取锁
    final void lock() {
        acquire(1);
    }
    ...
}
```

`lock`方法调用的 `acquire`方法来自父类AQS。下面首先给出完整的获取锁的流程图, 再逐行分析代码。

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/AQS-3.png)

### acquire

`acquire`定义在AQS类中，描述了获取锁的流程。

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

该方法中涉及了四个方法的调用:

**（1）tryAcquire(arg)**

该方法由继承AQS的子类实现, 为获取锁的具体逻辑。

**（2）addWaiter(Node mode)**

该方法由AQS实现, 负责在获取锁失败后调用, 将当前请求锁的线程包装成Node扔到sync queue中去，并返回这个Node。

**（3）acquireQueued(final Node node, int arg)**

该方法由AQS实现,这个方法比较复杂, 主要对上面刚加入队列的Node不断尝试以下两种操作之一:

- 在前驱节点就是head节点的时候,继续尝试获取锁
- 将当前线程挂起,使CPU不再调度它

**（4）selfInterrupt()**

该方法由AQS实现，用于中断当前线程。由于在整个抢锁过程中是不响应中断的，如果在抢锁的过程中发生了中断，为了记录下中断的状态，AQS的做法是简单的记录有没有有发生过中断，如果返回的时候发现曾经发生过中断，则在退出`acquire`方法之前，就调用`selfInterrupt()`自我中断一下，就好像将这个发生在抢锁过程中的中断“推迟”到抢锁结束以后再发生一样。

从上面的简单介绍中可以看出，除了获取锁的逻辑 `tryAcquire(arg)`由子类实现外, 其余方法均由AQS实现。

### tryAcquire

`tryAcquire` 获取锁的逻辑其实很简单——判断当前锁有没有被占用：

1. 如果锁没有被占用, 尝试以公平的方式获取锁
2. 如果锁已经被占用, 检查是不是锁重入

获取锁成功返回`true`, 失败则返回`false`。

```java
protected final boolean tryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    // 首先获取当前锁的状态
    int c = getState(); 
    
    // c=0 说明当前锁是avaiable的, 没有被任何线程占用, 可以尝试获取
    // 因为是实现公平锁, 所以在抢占之前首先看看队列中有没有排在自己前面的Node
    // 如果没有人在排队, 则通过CAS方式获取锁, 就可以直接退出了
    if (c == 0) {
        if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current); // 将当前线程设置为占用锁的线程
            return true;
        }
    }
    
    // 如果 c>0 说明锁已经被占用了
    // 对于可重入锁, 这个时候检查占用锁的线程是不是就是当前线程,是的话,说明已经拿到了锁, 直接重入就行
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    
    // 到这里说明有人占用了锁, 并且占用锁的不是当前线程, 则获取锁失败
    return false;
}
```

从这里可以看出，获取锁的过程主要就是一件事：**将state的状态通过CAS操作由0改写成1**。

由于是CAS操作，只有一个线程能执行成功。则执行成功的线程即获取了锁，在这之后，才有权利将`exclusiveOwnerThread`的值设成自己。另外对于可重入锁，如果当前线程已经是获取了锁的线程了，它还要注意增加锁的重入次数。

**注意**：这里修改state状态的操作，一个用了CAS方法`compareAndSetState`，一个用了普通的`setState`方法。这是因为用CAS操作时，当前线程还没有获得锁，所以可能存在多线程同时在竞争锁的情况；而调用setState方法时，是在当前线程已经是持有锁的情况下，因此对state的修改是安全的，只需要普通的方法就可以了。

### addWaiter

如果执行到此方法, 说明前面尝试获取锁的`tryAcquire`已经失败了, 既然获取锁已经失败了, 就要将当前线程包装成Node，加到等待锁的队列中去, 因为是FIFO队列, 所以自然是直接加在队尾。方法调用为：

```java
addWaiter(Node.EXCLUSIVE)
```

```java
private Node addWaiter(Node mode) {
    // 将当前线程包装成Node
    Node node = new Node(Thread.currentThread(), mode); 
    // 这里用注释的形式把Node的构造函数贴出来
    // 因为传入的mode值为Node.EXCLUSIVE，所以节点的nextWaiter属性被设为null
    /*
        static final Node EXCLUSIVE = null;
        
        Node(Thread thread, Node mode) {     // Used by addWaiter
            this.nextWaiter = mode;
            this.thread = thread;
        }
    */
    Node pred = tail;
    // 如果队列不为空, 则用CAS方式将当前节点设为尾节点
    if (pred != null) {
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    
    // 代码会执行到这里, 只有两种情况:
    //    1. 队列为空
    //    2. CAS失败
    // 注意, 这里是并发条件下, 所以什么都有可能发生, 尤其注意CAS失败后也会来到这里
    enq(node); //将节点插入队列
    return node;
}
```

可见，每一个处于独占锁模式下的节点，它的`nextWaiter`一定是null。在这个方法中，首先会尝试直接入队，但是因为目前是在并发条件下，所以有可能同一时刻，有多个线程都在尝试入队，导致`compareAndSetTail(pred, node)`操作失败——因为有可能其他线程已经成为了新的尾节点，导致尾节点不再是我们之前看到的那个`pred`了。如果入队失败了，接下来我们就需要调用`enq(node)`方法，在该方法中我们将通过`自旋+CAS`的方式，确保当前节点入队。

### enq

能执行到这个方法，说明当前线程获取锁已经失败了，我们已经把它包装成一个Node,准备把它扔到等待队列中去，但是在这一步又失败了。这个失败的原因可能是以下两种之一：

1. 等待队列现在是空的，没有线程在等待。
2. **其他线程在当前线程入队的过程中率先完成了入队，导致尾节点的值已经改变了，CAS操作失败。**

在该方法中使用了死循环, 即以自旋方式将节点插入队列，如果失败则不停的尝试, 直到成功为止, 另外, 该方法也负责在队列为空时初始化队列，这也说明，**队列是延时初始化的(lazily initialized)**：

```java
private Node enq(final Node node) {
    for (;;) {
        Node t = tail;
        // 如果是空队列, 首先进行初始化
        // 这里也可以看出, 队列不是在构造的时候初始化的, 而是延迟到需要用的时候再初始化, 以提升性能
        if (t == null) { 
            // 注意，初始化时使用new Node()方法新建了一个dummy节点
            if (compareAndSetHead(new Node()))
                tail = head; // 这里仅仅是将尾节点指向dummy节点，并没有返回
        } else {
        	// 到这里说明队列已经不是空的了, 这个时候再继续尝试将节点加到队尾
            node.prev = t;
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
}
```

这里尤其要注意的是，当队列为空时，初始化队列并没有使用当前传进来的节点，而是**新建了一个空节点**。在新建完空的头节点之后，**并没有立即返回**，而是将尾节点指向当前的头节点，然后进入下一轮循环。在下一轮循环中，尾节点已经不为null了，此时再将包装了当前线程的Node加到这个空节点后面。 这意味着在这个等待队列中，**头结点是一个“哑节点”，它不代表任何等待的线程。**(head节点不代表任何线程，它是一个空节点)

### 尾分叉

在继续往下之前，先分析`enq`方法中一个比较有趣的现象，暂且叫做尾分叉着重看下将当前节点设置成尾节点的操作：

```java
} else {
// 到这里说明队列已经不是空的了, 这个时候再继续尝试将节点加到队尾
    node.prev = t;
    if (compareAndSetTail(t, node)) {
        t.next = node;
        return t;
    }
}
```

将一个节点node添加到`sync queue`的末尾需要三步：

1. 设置node的前驱节点为当前的尾节点：`node.prev = t`
2. 修改`tail`属性，使它指向当前节点
3. 修改原来的尾节点，使它的next指向当前节点

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/AQS-4.png)

但是需要注意的，这里的三步并不是一个原子操作，第一步很容易成功；而第二步由于是一个CAS操作，在并发条件下有可能失败，第三步只有在第二步成功的条件下才执行。这里的CAS保证了同一时刻只有一个节点能成为尾节点，其他节点将失败，失败后将回到for循环中继续重试。

所以，当有大量的线程在同时入队的时候，同一时刻，只有一个线程能完整地完成这三步，**而其他线程只能完成第一步**，于是就出现了尾分叉：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/AQS-5.png)

注意，这里第三步是在第二步执行成功后才执行的，这就意味着，有可能即使已经完成了第二步，将新的节点设置成了尾节点，**此时原来旧的尾节点的next值可能还是null**(因为还没有来的及执行第三步)，所以如果此时有线程恰巧从头节点开始向后遍历整个链表，则它是遍历不到新加进来的尾节点的，但是这显然是不合理的，因为现在的tail已经指向了新的尾节点。 
另一方面，当完成了第二步之后，第一步一定是完成了的，所以如果我们从尾节点开始向前遍历，已经可以遍历到所有的节点。这也就是为什么在AQS相关的源码中，有时候常常会出现从尾节点开始逆向遍历链表——因为一个节点要能入队，则它的prev属性一定是有值的，但是它的next属性可能暂时还没有值。

至于那些“分叉”的入队失败的其他节点，在下一轮的循环中，它们的prev属性会重新指向新的尾节点，继续尝试新的CAS操作，最终，所有节点都会通过自旋不断的尝试入队，直到成功为止。

### addWaiter总结

至此，完成了`addWaiter(Node.EXCLUSIVE)`方法的完整的分析，该方法并不设计到任何关于锁的操作，它就是解决了并发条件下的节点入队问题。具体来说就是该方法保证了将当前线程包装成Node节点加入到等待队列的队尾，如果队列为空，则会新建一个哑节点作为头节点，再将当前节点接在头节点的后面。

`addWaiter(Node.EXCLUSIVE)`方法最终返回了代表了当前线程的Node节点，在返回的那一刻，这个节点必然是当时的`sync queue`的尾节点。

值得注意的是，`enq`方法也是有返回值（虽然这里我们并没有使用它的返回值），但是它返回的是node节点的前驱节点，这个返回值虽然在`addWaiter`方法中并没有使用，但是在其他地方会被用到。

再回到获取独占锁的逻辑中：

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

当`addWaiter(Node.EXCLUSIVE)`执行完毕后，节点现在已经被成功添加到`sync queue`中了，接下来将执行`acquireQueued`方法。

### acquireQueued

该方法是最复杂的一个方法，看代码之前首先简单的说明几点:

(1) 能执行到该方法, 说明`addWaiter` 方法已经成功将包装了当前Thread的节点添加到了等待队列的队尾 
(2) 该方法中将再次尝试去获取锁 
(3) 在再次尝试获取锁失败后, 判断是否需要把当前线程挂起

前面获取锁失败了, 这里还要再次尝试获取锁：

​	这里再次尝试获取锁是**基于一定的条件**的,即当前节点的前驱节点就是HEAD节点。head节点就是个哑节点，它不代表任何线程，或者代表了持有锁的线程，如果当前节点的前驱节点就是head节点，那就说明当前节点已经是排在整个等待队列最前面的了。

```java
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        // 自旋
        for (;;) {
            final Node p = node.predecessor();
            // 在当前节点的前驱就是HEAD节点时, 再次尝试获取锁
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            // 在获取锁失败后, 判断是否需要把当前线程挂起
            if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

首先看下面的再次获取锁的代码：

```java
final Node p = node.predecessor();
// 在当前节点的前驱就是HEAD节点时, 再次尝试获取锁
if (p == head && tryAcquire(arg)) {
    setHead(node);
    p.next = null; // help GC
    failed = false;
    return interrupted;
}
```

首先我们获取尾节点的前驱节点（因为上一步中返回的就是尾节点，并且这个节点就是代表了当前线程的Node）。如果前驱节点就是head节点，那说明当前线程已经排在了队列的最前面，所以这里我们再试着去获取锁。如果这一次获取成功了，即`tryAcquire`方法返回了true, 则我们将进入if代码块，调用`setHead`方法：	

```java
private void setHead(Node node) {
    head = node;
    node.thread = null;
    node.prev = null;
}
```

这个方法将head指向传进来的node，并且将node的thread和prev属性置为null。

可以看出，这个方法的本质是丢弃原来的head，将head指向已经获得了锁的node。但是接着又将该node的thread属性置为null了，**这某种意义上导致了这个新的head节点又成为了一个哑节点，它不代表任何线程**。这么做的原因是因为在`tryAcquire`调用成功后，`exclusiveOwnerThread`属性就已经记录了当前获取锁的线程了，此处没有必要再记录。**这某种程度上就是将当前线程从等待队列里面拿出来了，是一个变相的出队操作。**

还有另外一个特点是，这个`setHead`方法只是个普通方法，并没有像之前`enq`方法中那样采用`compareAndSetHead`方法，因为这里不会产生竞争。

在`enq`方法中，当我们设置头节点的时候，是新建一个哑节点并将它作为头节点，这个时候，可能多个线程都在执行这一步，因此我们需要通过CAS操作保证只有一个线程能成功。

在`acquireQueued`方法里，由于我们在调用到`setHead`的时，已经通过`tryAcquire`方法获得了锁，这意味着：

1. 此时没有其他线程在创建新的头节点——因为很明显此时队列并不是空的，不会执行到创建头节点的代码；
2. 此时能执行`setHead`的只有一个线程——因为要执行到`setHead`, 必然是`tryAcquire`已经返回了true, 而同一时刻，只有一个线程能获取到锁。

综上，在整个if语句内的代码即使不加锁，也是线程安全的，不需要采用CAS操作。

接下来再来看看另一种情况，即`p == head && tryAcquire(arg)`返回了false，此时需要判断是否需要将当前线程挂起：

### shouldParkAfterFailedAcquire

该方法用于决定在获取锁失败后，是否将线程挂起。决定的依据就是**前驱节点的**`waitStatus`值。

`waitStatus`有如下状态值：

```java
static final int CANCELLED =  1;
static final int SIGNAL    = -1;
static final int CONDITION = -2;
static final int PROPAGATE = -3;
```

一共有四种状态，在独占锁的获取操作中，只用到了其中的两个——`CANCELLED`和`SIGNAL`。 当然，前面在创建节点的时候并没有给`waitStatus`赋值，因此每一个节点最开始的时候`waitStatus`的值都被初始化为0，即不属于上面任何一种状态。

- `CANCELLED`: 表示Node所代表的当前线程已经取消了排队，即放弃获取锁了。

- `SIGNAL`: **不是表征当前节点的状态，而是当前节点的下一个节点的状态。**

  > 1.当一个节点的`waitStatus`被置为`SIGNAL`，就说明它的下一个节点（即它的后继节点）已经被挂起了（或者马上就要被挂起了），因此在当前节点释放了锁或者放弃获取锁时，如果它的`waitStatus`属性为`SIGNAL`，它还要完成一个额外的操作——唤醒它的后继节点。
  >
  > 2.`SIGNAL`这个状态的设置常常不是节点自己给自己设的，而是后继节点设置的。当决定要将一个线程挂起之前，首先要确保自己的前驱节点的waitStatus为`SIGNAL`，这就相当于给自己设一个闹钟再去睡，这个闹钟会在恰当的时候叫醒自己。

- `CONDITION`: 与`Condition`相关，该标识的结点处于**条件队列**中，结点的线程等待在`Condition`上，当其他线程调用了`Condition`的`signal()`方法后，`CONDITION`状态的结点将**从条件队列转移到同步队列中**，等待获取同步锁。

- `PROPAGATE`: 在共享模式中头结点有可能处于这种状态，表示锁的下一次获取可以无条件传播。

在理解了这些状态之后，看`shouldParkAfterFailedAcquire`的执行过程:

```java
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus; // 获得前驱节点的ws
    if (ws == Node.SIGNAL)
        // 前驱节点的状态已经是SIGNAL了，可以直接park
        return true;
    if (ws > 0) {
        // 当前节点的 ws > 0, 则为 Node.CANCELLED 说明前驱节点已经取消了等待锁(由于超时或者中断等原因)
        // 既然前驱节点不等了, 那就继续往前找, 直到找到一个还在等待锁的节点
        // 然后跨过这些不等待锁的节点, 直接排在等待锁的节点的后面(这里存在node节点的调整)
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        // 前驱节点的状态既不是SIGNAL，也不是CANCELLED
        // 用CAS设置前驱节点的ws为Node.SIGNAL，返回false，再次尝试获取锁
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}
```

可以看出`shouldParkAfterFailedAcquire`的执行逻辑如下：

- 如果为前驱节点的`waitStatus`值为 `Node.SIGNAL` 则直接返回 true；
- 如果为前驱节点的`waitStatus`值为 `Node.CANCELLED` (ws > 0), 则跳过那些节点, 重新寻找正常等待中的前驱节点，然后排在它后面，返回false；
- 其他情况, 将前驱节点的状态改为 `Node.SIGNAL`, 返回false。

这个函数只有在当前节点的前驱节点的`waitStatus`状态本身就是`SIGNAL`的时候才会返回true, 其他时候都会返回false。当`shouldParkAfterFailedAcquire`返回false后，会继续回到循环中再次尝试获取锁——这是因为此时前驱节点可能已经变了(`shouldParkAfterFailedAcquire`调整了node节点)。

当`shouldParkAfterFailedAcquire`返回true，即当前节点的前驱节点的`waitStatus`状态已经设为`SIGNAL`后，就可以安心的将当前线程挂起了，此时将调用`parkAndCheckInterrupt`：

### parkAndCheckInterrupt

到这个函数已经是最后一步了, 就是将线程挂起, 等待被唤醒。

```java
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this); // 线程被挂起，停在这里不再往下执行了
    return Thread.interrupted();
}
```

**注意**：`LockSupport.park(this)`执行完成后线程就被挂起了，除非其他线程`unpark`了当前线程，或者当前线程被中断了，否则代码是不会再往下执行的，后面的`Thread.interrupted()`也不会被执行。

### 独占锁获取的总结

1. AQS中用`state`属性表示锁，如果能成功将state属性通过CAS操作从0设置成1即获取了锁；
2. 获取了锁的线程才能将`exclusiveOwnerThread`设置成自己；
3. `addWaiter`负责将当前等待锁的线程包装成Node,并成功地添加到队列的末尾，这一点是由它调用的`enq`方法保证的，`enq`方法同时还负责在队列为空时初始化队列；
4. `acquireQueued`方法用于在Node成功入队后，继续尝试获取锁（取决于Node的前驱节点是不是head），或者将线程挂起；
5. `shouldParkAfterFailedAcquire`方法用于保证当前线程的前驱节点的`waitStatus`属性值为`SIGNAL`，从而保证了自己挂起后，前驱节点会负责在合适的时候唤醒自己；
6. `parkAndCheckInterrupt`方法用于挂起当前线程，并检查中断状态；
7. 如果最终成功获取了锁，线程会从`lock()`方法返回，继续往下执行；否则，线程会阻塞等待。

## 独占锁的释放

Java的内置锁在退出临界区之后是会自动释放锁的, 但是`ReentrantLock`这样的显式锁是需要自己显式的释放的, 所以在加锁之后需要在`finally`块中进行显式的锁释放:

```java
Lock lock = new ReentrantLock();
lock.lock();
try {
    // ...
} finally {
    lock.unlock();
}
```

下面以`ReentrantLock`的锁释放为例来分析独占锁的释放过程。由于锁的释放操作对于公平锁和非公平锁都是一样的, 所以, `unlock`的逻辑并没有放在 `FairSync` 或 `NonfairSync` 里面, 而是直接定义在 `ReentrantLock`类中:

```java
public void unlock() {
    sync.release(1);
}
```

### release

`release`方法定义在AQS类中，描述了释放锁的流程。

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```

可以看出, 相比于获取锁的`acquire`方法, 释放锁的过程要简单很多, 只涉及到两个子函数的调用:

- `tryRelease(arg)`——该方法由继承AQS的子类实现, 为释放锁的具体逻辑；
- `unparkSuccessor(h)`——唤醒后继线程。

下面分别分析这两个子函数的逻辑。

### tryRelease

`tryRelease`方法由`ReentrantLock`的静态类`Sync`实现。此外，相比获取锁的操作，这里并没有使用任何CAS操作，也是因为当前线程已经持有了锁，所以可以直接安全的操作，不会产生竞争。

```java
protected final boolean tryRelease(int releases) {
    
    // 首先将当前持有锁的线程个数减1(回溯到调用源头sync.release(1)可知, releases的值为1)
    // 这里的操作主要是针对可重入锁的情况下, c可能大于1
    int c = getState() - releases; 
    
    // 释放锁的线程当前必须是持有锁的线程
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    
    // 如果c为0了, 说明锁已经完全释放了
    boolean free = false;
    if (c == 0) {
        free = true;
        setExclusiveOwnerThread(null);
    }
    setState(c);
    return free;
}
```

### unparkSuccessor

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```

锁成功释放之后，接下来就是唤醒后继节点，这个方法同样定义在AQS中。值得注意的是，在成功释放锁之后(`tryRelease` 返回 `true`之后)，唤醒后继节点只是一个 "附加操作"，无论该操作结果怎样，最后 `release`操作都会返回 `true`。

接下来是`unparkSuccessor`的源码：

```java
private void unparkSuccessor(Node node) {
    int ws = node.waitStatus;
    
    // 如果head节点的ws<0, 则直接将它设为0
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);

    // 通常情况下, 要唤醒的节点就是自己的后继节点
    // 如果后继节点存在且也在等待锁, 那就直接唤醒它
    // 但是有可能存在 后继节点取消等待锁 的情况
    // 此时 从尾节点开始向前找起 , 直到找到距离head节点最近的ws<=0的节点
    Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t; // 注意! 这里找到了之并有return, 而是继续向前找
    }
    // 如果找到了还在等待锁的节点,则唤醒它
    if (s != null)
        LockSupport.unpark(s.thread);
}
```

上文分析了 `shouldParkAfterFailedAcquire` 方法执行的逻辑，提到了当前节点的前驱节点的 `waitStatus` 属性，该属性决定了是否要挂起当前线程，并且如果一个线程被挂起了，它的前驱节点的 `waitStatus`值必然是`Node.SIGNAL`。在唤醒后继节点的操作中， 也需要依赖于节点的`waitStatus`值。下面详细分析`unparkSuccessor`方法的逻辑：

1.首先, 传入该方法的参数node就是头节点head, 并且条件是`h != null && h.waitStatus != 0`。

- `h!=null` 条件必须满足；

- ` h.waitStatus != 0`的含义：

  > waitStatus为0的情况:
  >
  > - `shouldParkAfterFailedAcquire` 方法中将前驱节点的 `waitStatus`设为`Node.SIGNAL`；
  > - 新建一个节点的时候, 在`addWaiter`函数中, 当将一个新的节点添加进队列或者初始化空队列的时候，都会新建节点，而新建的节点的`waitStatus`在没有赋值的情况下都会初始化为0。
  >
  > 所以当一个head节点的`waitStatus`为0时，说明这个head节点后面没有在挂起等待中的后继节点了(如果有的话，head的ws就会被后继节点设为`Node.SIGNAL`了)，自然也就不要执行 `unparkSuccessor` 操作了。

2.从尾节点开始逆向查找, 而不是直接从head节点往后正向查找的原因：

- 从后往前找是基于`if (s == null || s.waitStatus > 0)`条件的，即后继节点不存在，或者后继节点取消了排队，这一条件大多数条件下是不满足的。因为虽然后继节点取消排队很正常，但是通过上面介绍的`shouldParkAfterFailedAcquire`方法可知，节点在挂起前，都会给自己找一个`waitStatus`状态为`SIGNAL`的前驱节点，而跳过那些已经`cancel`掉的节点。所以，这个从后往前找的目的其实是为了照顾刚刚加入到队列中的节点，这个与上面提到的**尾分叉**有关。

```java
private Node addWaiter(Node mode) {
    Node node = new Node(Thread.currentThread(), mode); //将当前线程包装成Node
    Node pred = tail;
    // 如果队列不为空, 则用CAS方式将当前节点设为尾节点
    if (pred != null) {
        node.prev = pred; //step 1, 设置前驱节点
        if (compareAndSetTail(pred, node)) { // step2, 将当前节点设置成新的尾节点
            pred.next = node; // step 3, 将前驱节点的next属性指向自己
            return node;
        }
    }
    enq(node); 
    return node;
}
```

仔细看上面这段代码, 可以发现**节点入队不是一个原子操作**, 虽然用了`compareAndSetTail`操作保证了当前节点被设置成尾节点，但是只能保证，此时step1和step2是执行完成的，有可能在step3还没有来的及执行到的时候，`unparkSuccessor`方法就开始执行了，此时`pred.next`的值还没有被设置成node，所以从前往后遍历的话是遍历不到尾节点的，但是因为尾节点此时已经设置完成，`node.prev = pred`操作也被执行过了，也就是说，如果从后往前遍历的话，新加的尾节点就可以遍历到了，并且可以通过它一直往前找。

所以之所以从后往前遍历是因为，处于多线程并发的条件下，如果一个节点的next属性为null, 并不能保证它就是尾节点（可能是因为新加的尾节点还没来得及执行`pred.next = node`）, 但是一个节点如果能入队, 则它的prev属性一定是有值的,所以反向查找一定是最精确的。

最后在调用了` LockSupport.unpark(s.thread)` 后，唤醒了线程。回到`park`的代码：

```java
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this); // 喏, 就是在这里被挂起了, 唤醒之后就能继续往下执行了
    return Thread.interrupted();
}
```

线程从`LockSupport.park(this);`唤醒后将接着往下执行。这里有两个线程：一个是正在释放锁的线程，并调用了`LockSupport.unpark(s.thread)` 唤醒了另外一个线程; 而这个`另外一个线程`，就是因为抢锁失败而被阻塞在`LockSupport.park(this)`处的线程。这个被阻塞的线程被唤醒后，将	调用 `Thread.interrupted()`并返回。

`Thread.interrupted()`这个函数将返回当前正在执行的线程的中断状态，并清除它。接着再返回到`parkAndCheckInterrupt`被调用的地方:

```java
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

返回到这个if语句

```
if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
    interrupted = true;
```

可见，如果`Thread.interrupted()`返回`true`，则 `parkAndCheckInterrupt()`就返回true, if条件成立，`interrupted`状态将设为`true`；如果`Thread.interrupted()`返回`false`, 则 `interrupted` 仍为`false`。

再接下来又回到了`for(;;) `死循环的开头，进行新一轮的抢锁。假设这次抢到了，将从 `return interrupted`处返回到`acquireQueued`的调用处:

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

如果`acquireQueued`的返回值为`true`，将执行 `selfInterrupt()`：

```java
static void selfInterrupt() {
    Thread.currentThread().interrupt();
}
```

而它的作用，就是中断当前线程。这么做的原因是：**并不知道线程被唤醒的原因。**具体来说，当从`LockSupport.park(this)`处被唤醒，并不知道是因为什么原因被唤醒，可能是因为别的线程释放了锁，调用了` LockSupport.unpark(s.thread)`，**也有可能是因为当前线程在等待中被中断了**，因此我们通过`Thread.interrupted()`方法检查了当前线程的中断标志，并将它记录下来，在最后返回`acquire`方法后，**如果发现当前线程曾经被中断过，那就把当前线程再中断一次。**

从上面的代码中知道，即使线程在等待资源的过程中被中断唤醒，它还是会不依不饶的再抢锁，直到它抢到锁为止。也就是说，**它是不响应这个中断的**，仅仅是记录下自己被人中断过。最后，当它抢到锁返回了，如果它发现自己曾经被中断过，它就再中断自己一次，将这个中断补上。

**注意**：中断对线程来说只是一个建议，一个线程被中断只是其中断状态被设为`true`, 线程可以选择忽略这个中断，中断一个线程并不会影响线程的执行。

最后再说明下，事实上在从`return interrupted;`处返回时并不是直接返回的，因为还有一个`finally`代码块：

```java
finally {
    if (failed)
        cancelAcquire(node);
}
```

它做了一些善后工作，但是条件是`failed`为`true`，而从前面的分析中知道，要从`for(;;)`中跳出来，只有一种可能，那就是当前线程已经拿到了锁，因为整个争锁过程我们都是不响应中断的，所以不可能有异常抛出，既然是拿到了锁，`failed`就一定是`false`，所以这个`finally`块在这里实际上并没有什么用，它是为**响应中断式的抢锁**所服务的。