# AQS源码分析2——共享锁的获取与释放

## 共享锁与独占锁的区别

共享锁与独占锁最大的区别在于，独占锁是**独占的、排他的**，因此在独占锁中有一个`exclusiveOwnerThread`属性，用来记录当前持有锁的线程。**当独占锁已经被某个线程持有时，其他线程只能等待它被释放后才能去争锁，并且同一时刻只有一个线程能争锁成功。**

而对于共享锁而言，由于锁是可以被共享的，因此**它可以被多个线程同时持有**。换句话说，如果一个线程成功获取了共享锁，那么其他等待在这个共享锁上的线程就也可以尝试去获取锁，并且极有可能获取成功。

共享锁的实现和独占锁是对应的，可以从下面这张表中看出：

| 独占锁                                      | 共享锁                                            |
| ------------------------------------------- | :------------------------------------------------ |
| tryAcquire(int arg)                         | tryAcquireShared(int arg)                         |
| tryAcquireNanos(int arg, long nanosTimeout) | tryAcquireSharedNanos(int arg, long nanosTimeout) |
| acquire(int arg)                            | acquireShared(int arg)                            |
| acquireQueued(final Node node, int arg)     | doAcquireShared(int arg)                          |
| acquireInterruptibly(int arg)               | acquireSharedInterruptibly(int arg)               |
| doAcquireInterruptibly(int arg)             | doAcquireSharedInterruptibly(int arg)             |
| doAcquireNanos(int arg, long nanosTimeout)  | doAcquireSharedNanos(int arg, long nanosTimeout)  |
| release(int arg)                            | releaseShared(int arg)                            |
| tryRelease(int arg)                         | tryReleaseShared(int arg)                         |
| 无                                          | doReleaseShared()                                 |

可以看出，除了最后一个属于共享锁的`doReleaseShared()`方法没有对应外，其他的方法，独占锁和共享锁都是想相应的。事实上，其实与`doReleaseShared()`对应的独占锁的方法应当是`unparkSuccessor(h)`，只是`doReleaseShared()`逻辑不仅仅包含了`unparkSuccessor(h)`，还包含了其他操作，这一点下面分析源码的时候再详述。

另外需要注意的是，在独占锁模式中，只有在获取了独占锁的节点释放锁时，才会唤醒后继节点——这是合理的，因为独占锁只能被一个线程持有，如果它还没有被释放，就没有必要去唤醒它的后继节点。

然而在共享锁模式下，当一个节点获取到了共享锁，在获取成功后就可以唤醒后继节点了，而不需要等到该节点释放锁的时候，这是因为共享锁可以被多个线程同时持有，一个线程获取到了锁，则后继的节点都可以直接来获取。因此，**在共享锁模式下，在获取锁和释放锁结束时，都会唤醒后继节点。** 这一点也是`doReleaseShared()`方法与`unparkSuccessor(h)`方法无法直接对应的根本原因所在。

## 共享锁的获取

```java
// 获取共享锁
public final void acquireShared(int arg) {
    if (tryAcquireShared(arg) < 0) // 尝试获取共享锁
        doAcquireShared(arg); // 非响应中断地获取共享锁
}
```

拿它和独占锁模式进行对比：

```java
// 独占锁模式
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

这两者的结构看上去似乎有点差别，但事实上是一样的，只不过在共享锁模式下，将与`addWaiter(Node.EXCLUSIVE)`对应的`addWaiter(Node.SHARED)`，以及`selfInterrupt()`操作全部移到了`doAcquireShared`方法内部，这一点在下面分析`doAcquireShared`方法时就可以看到。

**注意**：相对于独占锁的`tryAcquire(int arg)`返回boolean类型的值，共享锁的`tryAcquireShared(int acquires)`返回的是一个**整型值**：

- 如果该值小于0，则代表当前线程获取共享锁失败；
- 如果该值大于0，则代表当前线程获取共享锁成功，并且接下来其他线程尝试获取共享锁的行为很可能成功；
- 如果该值等于0，则代表当前线程获取共享锁成功，但是接下来其他线程尝试获取共享锁的行为会失败。

因此只要该返回值大于等于0，就表示获取共享锁成功。`acquireShared`中的`tryAcquireShared`方法由具体的子类负责实现，这里不解释。

接下来看看`doAcquireShared`方法，它对应于独占锁的`acquireQueued`，把它们相同的部分注释掉，只看不同的部分：

```java
// 非响应中断地获取共享锁
private void doAcquireShared(int arg) {
    // 添加节点
    final Node node = addWaiter(Node.SHARED);
    /*boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();*/
            if (p == head) {
                int r = tryAcquireShared(arg); // 尝试获取共享锁
                if (r >= 0) { // 成功
                    setHeadAndPropagate(node, r); // 设置头结点，还有剩余资源可以再唤醒之后的线程
                    p.next = null; // help GC
                    if (interrupted)
                        selfInterrupt(); // 中断
                    failed = false;
                    return;
                }
            }
            /*if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }*/
}
```

关于上面的**if部分**，独占锁对应的`acquireQueued`方法为：

```java
if (p == head && tryAcquire(arg)) {
    setHead(node);
    p.next = null; // help GC
    failed = false;
    return interrupted;
}
```

因此，综合来看，这两者的逻辑仅有两处不同：

- `addWaiter(Node.EXCLUSIVE)` --> `addWaiter(Node.SHARED)`

- `setHead(node)` --> `setHeadAndPropagate(node, r)`

1.这里第一点不同是独占锁的`acquireQueued`调用的是`addWaiter(Node.EXCLUSIVE)`，而共享锁调用的是`addWaiter(Node.SHARED)`，表明了该节点处于共享模式，这两种模式的定义为：

```java
/** Marker to indicate a node is waiting in shared mode */
static final Node SHARED = new Node();
/** Marker to indicate a node is waiting in exclusive mode */
static final Node EXCLUSIVE = null;
```

该模式被赋值给了节点的`nextWaiter`属性：

```java
Node(Thread thread, Node mode) {     // Used by addWaiter
    this.nextWaiter = mode;
    this.thread = thread;
}
```

在条件队列中，`nextWaiter`是指向条件队列中的下一个节点的，它将条件队列中的节点串起来，构成了单链表。但是在`sync queue`队列中，只用`prev`,`next`属性来串联节点，形成双向链表，`nextWaiter`属性在这里只起到一个**标记作用**，不会串联节点，这里不要被`Node SHARED = new Node()`所指向的空节点迷惑，这个空节点并不属于`sync queue`，不代表任何线程，它只起到标记作用，仅仅用作判断节点是否处于共享模式的依据：

```java
// Node.isShard()
final boolean isShared() {
    return nextWaiter == SHARED;
}
```

**总结一下`nextWaiter`属性的作用：某节点`nextWaiter`属性为null，表明该节点属于`sync queue`；某节点`nextWaiter`属性不为null且不等于`SHARED`，表明该节点属于`condition queue`；某节点`nextWaiter`属性为`SHARED`，表明该节点处于共享模式。**

2.第二点不同就在于获取锁成功后的行为，对于独占锁而言，是直接调用了`setHead(node)`方法，而共享锁调用的是`setHeadAndPropagate(node, r)`：

```java
// 设置头结点，还有剩余资源可以再唤醒之后的线程
private void setHeadAndPropagate(Node node, int propagate) {
    Node h = head; // Record old head for check below
    setHead(node); // 设置头结点

    if (propagate > 0 || h == null || h.waitStatus < 0 ||
        (h = head) == null || h.waitStatus < 0) {
        Node s = node.next;
        if (s == null || s.isShared())
            doReleaseShared(); // 唤醒后继节点
    }
}
```

在该方法内部不仅调用了`setHead(node)`，还在一定条件下调用了`doReleaseShared()`来唤醒后继的节点。这是因为**在共享锁模式下，锁可以被多个线程所共同持有，既然当前线程已经拿到共享锁了，那么就可以直接通知后继节点来拿锁，而不必等待锁被释放的时候再通知。**关于这个`doReleaseShared`方法，下面分析共享锁释放的时候再看。

## 共享锁的释放

通过AQS中的`releaseShared(int arg)`方法来释放共享锁：

```java
// 释放共享锁
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) { // 尝试释放共享锁
        doReleaseShared(); // 唤醒后继节点
        return true;
    }
    return false;
}
```

该方法对应于独占锁的`release(int arg)`方法：

```java
// 独占模式下的释放独占锁逻辑
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

在独占锁模式下，由于头节点就是持有独占锁的节点，在它释放独占锁后，如果发现自己的waitStatus不为0，则它将负责唤醒它的后继节点。

在共享锁模式下，头节点就是持有共享锁的节点，在它释放共享锁后，它也应该唤醒它的后继节点，但是值得注意的是，在之前的`setHeadAndPropagate`方法中可能已经调用过该方法了，也就是说**它可能会被同一个头节点调用两次**，也有可能在从`releaseShared`方法中调用它时，当前的头节点已经易主了。下面来详细看看这个方法：

```java
// 唤醒后继节点，确保传播
private void doReleaseShared() {
    for (;;) { // 自旋
        Node h = head; // 头结点
        if (h != null && h != tail) { // 队列不为空
            int ws = h.waitStatus; 
            if (ws == Node.SIGNAL) { 
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0)) // 多线程CAS
                    continue;            // loop to recheck cases
                unparkSuccessor(h);  // CAS只有一个线程能成功，然后唤醒后继节点
            }
            else if (ws == 0 &&
                     !compareAndSetWaitStatus(h, 0, Node.PROPAGATE)) // ?
                continue;                // loop on failed CAS
        }
        if (h == head)                   // loop if head changed
            break; // 头结点不变的情况下，退出循环
    }
}
```

在分析该方法时，先明确如下的问题：

**(1) 该方法被调用的地方**

该方法有两处调用，一处在`acquireShared`方法的末尾，当线程成功获取到共享锁后，在一定条件下调用该方法；一处在`releaseShared`方法中，当线程释放共享锁的时候调用。

**(2) 调用该方法的线程**

在独占锁中，只有获取了锁的线程才能调用release释放锁，因此调用`unparkSuccessor(h)`唤醒后继节点的必然是持有锁的线程，该线程可看做是当前的头节点(虽然在`setHead`方法中已经将头节点的`thread`属性设为了null，但是这个头节点曾经代表的就是这个线程)。

在共享锁中，持有共享锁的线程可以有多个，这些线程都可以调用`releaseShared`方法释放锁；而这些线程想要获得共享锁，则它们必然**曾经成为过头节点，或者就是现在的头节点**。因此，如果是在`releaseShared`方法中调用的`doReleaseShared`，可能此时调用方法的线程已经不是头节点所代表的线程了，头节点可能已经被易主好几次了。

**(3) 调用该方法的目的**

无论是在`acquireShared`中调用，还是在`releaseShared`方法中调用，该方法的目的都是在当前共享锁是可获取的状态时，**唤醒head节点的下一个节点**。这一点看上去和独占锁似乎一样，但是它们的一个重要的差别是——在共享锁中，当头节点发生变化时，是会回到循环中再**立即**唤醒head节点的下一个节点的。也就是说，在当前节点完成唤醒后继节点的任务之后将要退出时，如果发现被唤醒后继节点已经成为了新的头节点，则会立即触发**唤醒head节点的下一个节点**的操作，如此周而复始。

**(4) 退出该方法的条件**

该方法是一个自旋操作(`for(;;)`)，退出该方法的唯一办法是最后的break语句：

```java
if (h == head)   // loop if head changed
    break; // 头结点不变的情况下，退出循环
```

即只有在当前head没有变化时才会退出，否则继续循环。 

> **理解该退出条件**：
>
> 为了说明问题，假设目前`sync queue`队列中依次排列有
>
> ```dummy node -> A -> B -> C -> D```
>
> 现在假设A已经拿到了共享锁，则它将成为新的dummy node，
>
> ```dummy node (A) -> B -> C -> D```
>
> 此时，A线程会调用`doReleaseShared`，写做`doReleaseShared[A]`，在该方法中将唤醒后继的节点B，它很快获得了共享锁，成为了新的头节点：
>
> ```dummy node (B) -> C -> D```
>
> 此时，B线程也会调用`doReleaseShared`，我们写做`doReleaseShared[B]`，在该方法中将唤醒后继的节点C，但是别忘了，在`doReleaseShared[B]`调用的时候，`doReleaseShared[A]`还没运行结束呢，当它运行到`if(h == head)`时，发现头节点现在已经变了，所以它将继续回到for循环中，与此同时，`doReleaseShared[B]`也没闲着，它在执行过程中也进入到了for循环中。
>
> 由此可见，这里形成了一个`doReleaseShared`的“**调用风暴**”，大量的线程在同时执行`doReleaseShared`，这极大地加速了唤醒后继节点的速度，提升了效率，同时该方法内部的CAS操作又保证了多个线程同时唤醒一个节点时，只有一个线程能操作成功。
>
> 注意：那如果这里`doReleaseShared[A]`执行结束时，节点B还没有成为新的头节点时，`doReleaseShared[A]`方法不就退出了吗？是的，但即使这样也没有关系，因为它已经成功唤醒了线程B，即使`doReleaseShared[A]`退出了，当B线程成为新的头节点时，`doReleaseShared[B]`就开始执行了，它也会负责唤醒后继节点的，这样即使变成这种每个节点只唤醒自己后继节点的模式，从功能上讲，最终也可以实现唤醒所有等待共享锁的节点的目的，只是效率上没有之前的“调用风暴”快。
>
> 由此可知，这里的“调用风暴”事实上是一个优化操作，因为在执行到该方法的末尾时，`unparkSuccessor`基本上已经被调用过了，而由于现在是共享锁模式，所以**被唤醒的后继节点**极有可能已经获取到了共享锁，成为了新的head节点，当它成为新的head节点后，它可能还是要在`setHeadAndPropagate`方法中调用`doReleaseShared`唤醒它的后继节点。

明确了上述问题后，再来详细分析这个方法，它最重要的部分就是下面这两个if语句：

```java
if (ws == Node.SIGNAL) {
    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
        continue;            // loop to recheck cases
    unparkSuccessor(h);
}
else if (ws == 0 &&
         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
    continue;                // loop on failed CAS
```

第一个if很好理解，如果当前ws值为`Node.SIGNAL`，则说明后继节点需要唤醒，这里采用CAS操作先将`Node.SIGNAL`状态改为0，这是因为前面讲过，可能有大量的`doReleaseShared`方法在同时执行，我们只需要其中一个执行`unparkSuccessor(h)`操作就行了，这里通过CAS操作保证了`unparkSuccessor(h)`只被执行一次。

**(个人理解)**比较难理解的是第二个else if，首先要弄清楚ws什么时候为0，一种是上面的`compareAndSetWaitStatus(h, Node.SIGNAL, 0)`会导致ws为0，但是很明显，如果是因为这个原因，则它是不会进入到else if语句块的。所以这里的ws为0是指**当前队列存在两个节点(头结点和新加入的节点)**，并且新加入的节点调用了`compareAndSetTail`，还没来得及`compareAndSetWaitStatus`。

其次，`compareAndSetWaitStatus(h, 0, Node.PROPAGATE)`这个操作失败的原因为：说明在执行这个操作的瞬间，ws此时已经不为0了，表示新加入的节点调用了`compareAndSetWaitStatus`方法，ws被改为了`Node.SIGNAL`，此时我们将调用`continue`，在下次循环中直接将这个刚刚新入队但准备挂起的线程唤醒。

这里复习一下新节点入队的过程，在发现新节点的前驱不是head节点的时候，它将调用`shouldParkAfterFailedAcquire`：

```java
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
        /*
         * This node has already set status asking a release
         * to signal it, so it can safely park.
         */
        return true;
    if (ws > 0) {
        /*
         * Predecessor was cancelled. Skip over predecessors and
         * indicate retry.
         */
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        /*
         * waitStatus must be 0 or PROPAGATE.  Indicate that we
         * need a signal, but don't park yet.  Caller will need to
         * retry to make sure it cannot acquire before parking.
         */
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}
```

由于前驱节点的ws值现在还为0，新节点将会把它改为`Node.SIGNAL`，但修改后，该方法返回的是false，也就是说线程不会立即挂起，而是回到上层再尝试一次抢锁：

```java
private void doAcquireShared(int arg) {
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head) {
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    if (interrupted)
                        selfInterrupt();
                    failed = false;
                    return;
                }
            }
            // shouldParkAfterFailedAcquire的返回处
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

当再次回到`for(;;)`循环中，由于此时当前节点的前驱节点已经成为了新的head，所以它可以参与抢锁，由于它抢的是共享锁，所以大概率它是抢的到的，所以极有可能它不会被挂起。这有可能导致在上面的`doReleaseShared`调用`unparkSuccessor`方法`unpark`了一个并没有被`park`的线程。然而，这一操作是被允许的，当`unpark`一个并没有被`park`的线程时，该线程在下一次调用`park`方法时就不会被挂起，而这一行为是符合场景的——因为当前的共享锁处于可获取的状态，后继的线程应该直接来获取锁，不应该被挂起。

## 总结

- 共享锁的调用框架和独占锁很相似，它们最大的不同在于获取锁的逻辑——共享锁可以被多个线程同时持有，而独占锁同一时刻只能被一个线程持有。
- 由于共享锁同一时刻可以被多个线程持有，因此当头节点获取到共享锁时，可以立即唤醒后继节点来争锁，而不必等到释放锁的时候。因此，共享锁触发唤醒后继节点的行为可能有两处，一处在当前节点成功获得共享锁后，一处在当前节点释放共享锁后。

## 参考

- [逐行分析AQS源码(1)——独占锁的获取](https://segmentfault.com/a/1190000015739343)
- [逐行分析AQS源码(2)——独占锁的释放](https://segmentfault.com/a/1190000015752512)
- [逐行分析AQS源码(3)——共享锁的获取与释放](https://segmentfault.com/a/1190000016447307)
- [Java并发之AQS详解](https://www.cnblogs.com/waterystone/p/4920797.html)

