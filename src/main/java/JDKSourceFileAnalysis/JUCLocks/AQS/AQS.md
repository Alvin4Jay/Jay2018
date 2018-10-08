 AbstractQueuedSynchronizer(AQS) 
----
##一、AQS中的锁类型
1. AQS中包含**独占锁和共享锁**两种。
	（1）独占锁————锁在一个时间点只能被一个线程持有。根据锁的获取机制，独占锁可以分为公平锁与非公平锁。公平锁，
	是按照通过CLH等待线程按照先来先得(FIFO)的规则，公平的获取锁；而非公平锁，则当线程要获取锁时，它会无视CLH
	等待队列而直接获取锁。独占锁的典型实例子是ReentrantLock，此外，ReentrantReadWriteLock.WriteLock也是独占锁。
	
	（2）共享锁————能被多个线程同时拥有，能被共享的锁。JUC包中的ReentrantReadWriteLock.ReadLock，CyclicBarrier，
	 CountDownLatch和Semaphore都是共享锁。
	
2. CLH队列 -- Craig, Landin, and Hagersten lock queue。
	 CLH队列是AQS中“等待锁”的线程队列。在多线程中，为了保护竞争资源不被多个线程同时操作而起来错误，我们常常需要通过锁来
	 保护这些资源。在独占锁中，竞争资源在一个时间点只能被一个线程锁访问；而其它线程则需要等待。**CLH就是管理这些“等待锁”的线程的队列。**

3. AQS基于一个先进先出(CLH FIFO)队列，可以用来创建锁（如`ReentrantLock`）和其他同步工具。AQS利用了字段`state`来表示其内部状态。
使用的方法是继承，子类（Sync）通过继承AQS并实现一些方法来管理自身状态，如`acquire()`和`release()`来操纵，而`acquire()`、`release()`
这些方法则需要通过`AQS`已经实现的原子操作`setState()、getState()、compareAndSetState()`对`state`进行操作。

	子类推荐定义为自定义同步器(ReentrantLock)的内部类(Sync)，同步器自己定义了一些`tryAcquire()`之类的方法供操作状态。该同步器即可以作
	为排他模式也可以作为共享模式，当它被定义为一个排他模式时，其他线程对其的获取就被阻止，而共享模式对于多个线程获取都可以成功。

##二、AQS实现

1. Node节点

由于AQS依赖于FIFO队列，而队列中的元素为保存着线程引用和线程状态的Node对象。

    Node {
	    int waitStatus; //node状态
	    Node prev; //前驱
	    Node next; //后继
	    Node nextWaiter;	
	    Thread thread; //线程
	}

五个成员变量主要负责保存该节点的线程引用，同步等待队列（sync队列）的前驱和后继节点，同时也包括了同步状态

(1)waitStatus

    static final int CANCELLED =  1;
	static final int SIGNAL    = -1;
	static final int CONDITION = -2;
	static final int PROPAGATE = -3;

**CANCELLED**：因为超时或者中断，结点会被设置为取消状态，被取消状态的结点不应该去竞争锁，只能保持取消状态不变，不能转换为其他状态。
处于这种状态的结点会被踢出队列，被GC回收；

**SIGNAL**：表示这个结点的继任结点被阻塞了，到时需要通知它；

**CONDITION**：表示这个结点在条件队列中，因为等待某个条件而被阻塞；

**PROPAGATE**：在共享模式头结点有可能处于这种状态，表示锁的下一次获取可以无条件传播；

**0**：None of the above，新结点会处于这种状态。

(2)Node prev

前驱节点，比如当前节点被取消，那就需要前驱节点和后继节点来完成连接。

(3)Node next

后继结点

(4)Node nextWaiter

存储condition队列中的后继节点

(5)Thread thread

入队列时的当前线程

Node节点成为sync队列和condition队列构建的基础，在AQS中就包含了sync队列。AQS拥有三个成员变量：sync队列的头结点head、sync队列
的尾节点tail和状态state。对于锁的获取，请求形成节点，将其挂载在尾部(addWaiter())，而锁资源的转移（释放再获取）是从头部开始向后
进行。对于同步器维护的状态state，多个线程对其的获取将会产生一个链式的结构。

![1](https://i0.wp.com/ifeve.com/wp-content/uploads/2013/10/21.png?zoom=1.25&resize=500%2C134)


2.API

实现自定义同步器(Sync)时，需要使用同步器提供的getState()、setState()和compareAndSetState()方法来操纵状态的变迁。————`tryAcquire() tryRelease()`.

方法名称 | 描述

protected boolean tryAcquire(int arg) | 排它的获取这个状态。这个方法的实现需要查询当前状态是否允许获取，然后再进行获取（使用
compareAndSetState来做）状态。

protected boolean tryRelease(int arg) | 释放状态。

protected int tryAcquireShared(int arg) | 共享模式下获取状态。

protected boolean tryReleaseShared(int arg) | 	共享的模式下释放状态。

protected boolean isHeldExclusively() | 在排它模式下，状态是否被占用。

实现这些方法必须是非阻塞而且是线程安全的，推荐使用该同步器的父类java.util.concurrent.locks.AbstractOwnableSynchronizer来设
置当前的线程。

3.获取锁的伪码

    while(获取锁) {
		if (获取到) {
			退出while循环
		} else {
			if(当前线程没有入队列) {
				那么入队列
			}
			阻塞当前线程
		}
	}

4.释放锁的伪码

	if (释放成功) {
		删除头结点
		激活原头结点的后继节点
	}

5. AQS实现举例(见Jay2017)

##三、实现分析

1.**public final void acquire(int arg)**

**该方法以排他的方式获取锁，对中断不敏感。**

    public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }

上述逻辑主要包括：

	1.尝试获取（调用tryAcquire更改状态，需要保证原子性）；在tryAcquire方法中使用了同步器提供的对state操作的方法，利用compareAndSet
	保证只有一个线程能够对状态进行成功修改，而没有成功修改的线程将进入sync队列排队addWaiter()。

	2.如果获取不到，将当前线程构造成节点Node并加入sync队列；进入队列的每个线程都是一个节点Node，从而形成了一个双向队列，类似CLH队列，
	这样做的目的是线程间的通信会被限制在较小规模（也就是两个节点左右）。

	3.再次尝试获取acquireQueued()，如果没有获取到那么将当前线程从线程调度器上摘下，进入等待状态。使用LockSupport将当前线程unpark。

主要步骤:

①`tryAcquire(arg)`
	
    protected boolean tryAcquire(int arg) { //AQS方法，需要子类实现(Sync)
        throw new UnsupportedOperationException();
    }

    public boolean tryAcquire(int acquires) {  //子类实现
        assert acquires == 1; // Otherwise unused
        if (compareAndSetState(0, 1)) { //尝试更改状态
            setExclusiveOwnerThread(Thread.currentThread()); //设置锁的拥有线程
            return true; 
        }
        return false;
    }

尝试获取（调用tryAcquire更改状态，需要保证原子性）。在tryAcquire方法中使用了同步器提供的对state操作的方法，利用compareAndSet保证
只有一个线程能够对状态进行成功修改，而没有成功修改的线程将进入sync队列排队。

②addWaiter(Node.EXCLUSIVE), arg) 尝试获取不成功，进入Sync队列排队

	private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode); //当前线程，互斥模式
        // 快速尝试在尾部添加
        Node pred = tail;
        if (pred != null) { //尾节点存在
            node.prev = pred;
            if (compareAndSetTail(pred, node)) { //尝试追加在队列尾部
                pred.next = node;
                return node; //成功，则返回
            }
        }
        enq(node); //队尾添加失败或者第一次添加
        return node;
    }

	private Node enq(final Node node) {
        for (;;) {  //自旋
            Node t = tail;
            if (t == null) { // 第一次添加
                if (compareAndSetHead(new Node())) //初始化空节点
                    tail = head; 
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

方法逻辑：

（a）使用当前线程构造Node

对于一个节点需要做的是将当前节点(current=node)前驱节点prev指向尾节点（current.prev = tail），尾节点指向它（tail = current），
原有的尾节点的后继节点指向它（t.next = current）而这些操作要求是原子的。上面的操作是利用尾节点的设置来保证的，也就是compareAndSetTail
来完成的。

（b）先行尝试在队尾添加

如果尾节点已经有了，然后做如下操作：
(1)分配引用T指向尾节点；
(2)将节点的前驱节点更新为尾节点（current.prev = T）；
(3)如果尾节点是T，那么将当尾节点设置为该节点（tail = current，原子更新）；
(4)T的后继节点指向当前节点（T.next = current）。
注意第3点是要求原子的。
这样可以以最短路径O(1)的效果来完成线程入队，是最大化减少开销的一种方式。

（c）如果队尾添加失败或者是第一个入队的节点

enq()

如果是第1个节点，也就是sync队列没有初始化，那么会进入到enq这个方法，进入的线程可能有多个，或者说在addWaiter中没有成功入队的线程都将进入
enq这个方法。可以看到enq的逻辑是确保进入的Node都会有机会顺序的添加到sync队列中。

而加入的步骤如下：
(1)如果尾节点为空，那么原子化的分配一个头节点，并将尾节点指向头节点，这一步是初始化；
(2)然后是重复在addWaiter中做的工作，但是在一个while(true)的循环中，直到当前节点入队为止

③acquireQueued(final Node node, int arg)

    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {  //自省
                final Node p = node.predecessor(); //得到前驱节点
                if (p == head && tryAcquire(arg)) { //前驱节点是头结点且可以获得锁
                    setHead(node);  //设置本节点为头结点
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                if (shouldParkAfterFailedAcquire(p, node) && //等待
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }


进入sync队列之后，接下来就是要进行锁的获取，或者说是访问控制了，只有一个线程能够在同一时刻继续的运行，而其他的进入等待状态。而每个线程都是
一个独立的个体，它们自省(循环)的观察，当条件满足的时候（自己的前驱是头结点并且原子性的获取了状态），那么这个线程能够继续运行.

上述逻辑主要包括：

1. 获取当前节点的前驱节点；
需要获取当前节点的前驱节点，而头结点所对应的含义是当前站有锁且正在运行。
2. 当前驱节点是头结点并且能够获取状态，代表该当前节点占有锁；
如果满足上述条件，那么代表能够占有锁，根据节点对锁占有的含义，设置头结点为当前节点。
3. 否则进入等待状态。
如果没有轮到当前节点运行，那么将当前线程从线程调度器上摘下，也就是进入等待状态。

总流程：

![](https://i2.wp.com/img2.tbcdn.cn/L1/461/1/t_9683_1379328542_928191748.png)


----------


2.**public final boolean release(int arg)**

在unlock方法的实现中，使用了同步器的release方法。相对于在之前的acquire方法中可以得出调用acquire，保证能够获取到锁（成功获取状态），
而release则表示将状态设置回去，也就是将资源释放，或者说将锁释放。

	public final boolean release(int arg) {
        if (tryRelease(arg)) { //子类实现，尝试释放锁，若成功
            Node h = head;
            if (h != null && h.waitStatus != 0) //头结点不为空 且不是 新节点
                unparkSuccessor(h); //通知后继节点
            return true;
        }
        return false;
    }
	
	protected boolean tryRelease(int arg) { //AQS方法
        throw new UnsupportedOperationException();
    }

	protected boolean tryRelease(int releases) { //子类实现
        assert releases == 1; // Otherwise unused
        if (getState() == 0) throw new IllegalMonitorStateException();
        setExclusiveOwnerThread(null);
        setState(0);
        return true;
    }
	
步骤：

（a）尝试释放状态；

tryRelease能够保证原子化的将状态设置回去，当然需要使用compareAndSet来保证。如果释放状态成功过之后，将会进入后继节点的唤醒过程。

（b）唤醒当前节点的后继节点所包含的线程 unparkSuccessor()

    private void unparkSuccessor(Node node) {
		
        int ws = node.waitStatus;  //不是CANCELD
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);

        Node s = node.next; //后继节点
        if (s == null || s.waitStatus > 0) { //不满足
            s = null; 
            for (Node t = tail; t != null && t != node; t = t.prev) //从尾部开始找
                if (t.waitStatus <= 0) //如果不是CANCELD
                    s = t;
        }
        if (s != null)
            LockSupport.unpark(s.thread); //唤醒线程
    }

获取释放的整个过程：

	在获取时，维护了一个sync队列，每个节点都是一个线程在进行自旋，而依据就是自己是否是首节点的后继并且能够获取资源；

	在释放时，仅仅需要将资源还回去，然后通知一下后继节点并将其唤醒。这里需要注意，队列的维护（首节点的更换）是依靠消费者（获取时）来完成的，
	也就是说在满足了自旋退出的条件时的一刻，这个节点就会被设置成为首节点


**3.protected boolean tryAcquire(int arg)**

tryAcquire是自定义同步器Sync需要实现的方法，也就是自定义同步器Sync非阻塞原子化的获取状态，如果锁该方法一般用于Lock的tryLock实现中，这个
特性是synchronized无法提供的。

**4.public final void acquireInterruptibly(int arg)**

该方法提供获取状态能力，当然在无法获取状态的情况下会进入sync队列进行排队，这类似acquire，但是和acquire不同的地方在于**它能够在外界对当
前线程进行中断的时候提前结束获取状态的操作**，换句话说，就是**在类似synchronized获取锁时，外界能够对当前线程进行中断，并且获取锁的这个
操作能够响应中断并提前返回。**一个线程处于synchronized块中或者进行同步I/O操作时，对该线程进行中断操作，这时该线程的中断标识位被设置为true，
但是线程依旧继续运行。

如果在获取一个通过网络交互实现的锁时，这个锁资源突然进行了销毁，那么使用acquireInterruptibly的获取方式就能够让该时刻尝试获取锁的线程提前
返回。而同步器的这个特性被实现Lock接口中的lockInterruptibly方法。根据Lock的语义，在被中断时，lockInterruptibly将会抛出
InterruptedException来告知使用者。

	public final void acquireInterruptibly(int arg) throws InterruptedException {
		if (Thread.interrupted())   //检测是否已经中断
			throw new InterruptedException(); //是则抛出异常
		if (!tryAcquire(arg))  //尝试获取锁
			doAcquireInterruptibly(arg); //不成功，
	}
	
	private void doAcquireInterruptibly(int arg)
		throws InterruptedException {
		final Node node = addWaiter(Node.EXCLUSIVE);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head && tryAcquire(arg)) {  //前驱节点是头结点
					setHead(node);
					p.next = null; // help GC
					failed = false;
					return;
				}
				// 检测中断标志位
				if (shouldParkAfterFailedAcquire(p, node) &&
				parkAndCheckInterrupt()) //如果已经被中断
					throw new InterruptedException(); //抛出异常
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

步骤：

	1. 检测当前线程是否被中断；
	判断当前线程的中断标志位，如果已经被中断了，那么直接抛出异常。
	2. 尝试获取状态；
	调用tryAcquire获取状态，如果顺利会获取成功并返回。
	3. 构造节点并加入sync队列；
	获取状态失败后，将当前线程引用构造为节点并加入到sync队列中。退出队列的方式在没有中断的场景下和acquireQueued类似，当头结点
	自己的前驱节点并且能够获取到状态时，即可以运行，当然要将本节点设置为头结点，表示正在运行。
	4. 中断检测。
	在每次被唤醒时，进行中断检测，如果发现当前线程被中断，那么抛出InterruptedException并退出循环。

