# `ConcurrentHashMap`源码分析 #
	
##一、JDK1.6 ConcurrentHashMap分析

采用**分段锁**的机制，实现并发的更新操作，底层采用**数组+链表+红黑树**的存储结构。其包含两个核心静态内部类<font color=red>Segment和HashEntry</font>.一个 ConcurrentHashMap 实例中包含由若干个 Segment 对象组成的数组.

![](http://upload-images.jianshu.io/upload_images/2184951-728c319f48ebe35a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- Segment继承ReentrantLock用来充当锁的角色，每个 Segment 对象守护每个散列映射表的若干个桶;
- HashEntry 用来封装映射表的键/值对;
- 每个桶是由若干个HashEntry对象链接起来的链表.

##二、JDK1.8 ConcurrentHashMap分析

JDK1.8的实现已经抛弃了Segment分段锁机制，利用CAS+Synchronized来保证并发更新的安全，底层依然采用数组+链表+红黑树的存储结构。

![](http://upload-images.jianshu.io/upload_images/2184951-3d2365ca5996274f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

###1.JDK1.8 ConcurrentHashMap重要字段说明

(1)table：默认为null，初始化发生在第一次插入操作(put)，默认大小为16的数组，用来存储Node节点数据，扩容时大小总是2的幂次方；

(2)nextTable：默认为null，扩容时新生成的数组，其大小为原数组的两倍,这个变量是作为扩容临时使用的；

(3)sizeCtl ：默认为0，用来控制table的初始化和扩容操作;

- -1 代表table正在初始化
- -N 表示有N-1个线程正在进行扩容操作
- 其余情况：
	①如果table未初始化，表示table需要初始化的大小；②、如果table初始化完成，表示table的扩容阈值，默认是table大小的0.75倍，计算用公式0.75（n - (n >>> 2)）。

(4)Node子类:保存key，value及key的hash值的数据结构.

    class Node<K,V> implements Map.Entry<K,V> {
	  final int hash;
	  final K key;
	  volatile V val;
	  volatile Node<K,V> next;
	  ... 省略部分代码
	}


其中value和next都用volatile修饰，保证并发的可见性。

(5)ForwardingNode子类:一个特殊的Node节点，hash值为-1，其中存储nextTable的引用。只有table发生扩容的时候，ForwardingNode才会发挥作用，作为一个占位符放在table中表示当前节点为null或则已经被移动，扩容完成。

	final class ForwardingNode<K,V> extends Node<K,V> {
	  final Node<K,V>[] nextTable;
	  ForwardingNode(Node<K,V>[] tab) {
	      super(MOVED, null, null, null);
	      this.nextTable = tab;
	  }
	}


###2.ConcurrentHashMap初始化

    public ConcurrentHashMap(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException();
        int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ?
                   MAXIMUM_CAPACITY :
                   tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
        this.sizeCtl = cap; //初始化只确定了sizeCtl的值，即table数组的大小
    }

**注意：**ConcurrentHashMap在构造函数中只会初始化sizeCtl值，并不会直接初始化table，而是延缓到第一次put操作。

###3.table数组初始化

table初始化操作会延缓到第一次put行为。但是put是可以并发执行的，Doug Lea(作者)是如何实现table只初始化一次的？

	//初始化table
    private final Node<K,V>[] initTable() {
	    Node<K,V>[] tab; int sc;
	    while ((tab = table) == null || tab.length == 0) { //table还未初始化
	        if ((sc = sizeCtl) < 0)  //多线程进入，其中一个线程CAS成功，使得sizeCtl变为-1
	            Thread.yield(); // lost initialization race; just spin //让出CPU，等待初始化完成
	        else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) { //有且只有一个线程CAS成功修改sizeCtl为-1,正在进行table的初始化
	            try {
	                if ((tab = table) == null || tab.length == 0) {
	                    int n = (sc > 0) ? sc : DEFAULT_CAPACITY; //2的n次 / 16
	                    @SuppressWarnings("unchecked")
	                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n]; //初始化
	                    table = tab = nt;
	                    sc = n - (n >>> 2); //算出扩容阈值
	                }
	            } finally {
	                sizeCtl = sc; //sizeCtl设置为扩容阈值
	            }
	            break;
	        }
	    }
	    return tab;
	}
	
sizeCtl默认为0，如果ConcurrentHashMap实例化时有传参数，sizeCtl会是一个2的幂次方的值。所以执行第一次put操作的线程会执行Unsafe.compareAndSwapInt方法修改sizeCtl为-1，有且只有一个线程能够修改成功，其它线程通过Thread.yield()让出CPU时间片等待table初始化完成。

---

###4.put操作

假设table已经初始化完成，put操作采用CAS+synchronized实现并发插入或更新操作。

	//put方法，返回旧值v
	final V putVal(K key, V data, boolean onlyIfAbsent) {
        if (key == null || data == null) throw new NullPointerException();
        int hash = spread(key.hashCode()); //hashCode高低16位异或，减少碰撞，计算出hash
        int binCount = 0; //桶的节点计数？？
        for (Node<K,V>[] tab = table;;) {  //循环
            Node<K,V> f; int n, i, fh;  
            if (tab == null || (n = tab.length) == 0) //table数组为空
                tab = initTable(); //并发的初始化
			//i = (n - 1) & hash)确定table数组桶位置索引
			//tabAt采用CAS获取对象元素
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) { //这个桶位置为空null
                if (casTabAt(tab, i, null,   //使用CAS插入新节点
                             new Node<K,V>(hash, key, data, null)))
                    break;   // no lock when adding to empty bin，成功就退出，不成功等自旋下次循环再重试
            }
            else if ((fh = f.hash) == MOVED)  //ForwardingNode节点，表示这个位置其他线程正在扩容，则本线程帮助其他线程一起扩容。
                tab = helpTransfer(tab, f); //帮助扩容，扩容在新数组中重新插入
            else {
                V oldVal = null; //旧值
                synchronized (f) {  //锁住当前节点
                    if (tabAt(tab, i) == f) {  //再次确认i索引处节点是否是f,以防其他线程修改了
                        if (fh >= 0) {  //f的hash值大于0，表明是链表，而不是红黑树
                            binCount = 1; //已经有了1个节点
                            for (Node<K,V> e = f;; ++binCount) { //遍历链表，插入或者更新
                                K ek;
                                if (e.hash == hash &&   //更新
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent) 
                                        e.val = data;
                                    break;
                                }
                                Node<K,V> pred = e;  
                                if ((e = e.next) == null) { //找不到，则插入
                                    pred.next = new Node<K,V>(hash, key,
                                                              data, null);
                                    break;
                                }
                            }
                        }
                        else if (f instanceof TreeBin) { //是红黑树结构
                            Node<K,V> p;
                            binCount = 2; 
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key, //以红黑树方式插入
                                                           data)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = data;
                            }
                        }
                    }
                }
                if (binCount != 0) {  
                    if (binCount >= TREEIFY_THRESHOLD) //如果链表结点大于TREEIFY_THRESHOLD
                        treeifyBin(tab, i); //树化这个桶的节点
                    if (oldVal != null)
                        return oldVal; //如果是更新，直接返回旧值
                    break;
                }
            }
        }
		//将当前ConcurrentHashMap的元素数量+1，table的扩容是在这里发生的
        addCount(1L, binCount); //如果是插入，增加总数，判断是否需要扩容
        return null;
    }

(1)hash算法

    static final int spread(int h) {return (h ^ (h >>> 16)) & HASH_BITS;}

(2)table中定位索引位置，n是table的大小

	int index = (n - 1) & hash

(3)获取table中对应索引的元素f

**Doug Lea采用Unsafe.getObjectVolatile来获取，也许有人质疑，直接table[index]不可以么，为什么要这么复杂？**

在java内存模型中，我们已经知道每个线程都有一个工作内存，里面存储着table的副本，虽然table是volatile修饰的，但不能保证线程每次都拿到table中的最新元素，Unsafe.getObjectVolatile可以直接获取指定内存的数据，保证了每次拿到数据都是最新的。

(4)如果f为null，说明table中这个位置第一次插入元素，利用Unsafe.compareAndSwapObject方法插入Node节点。

- 如果CAS成功，说明Node节点已经插入，随后addCount(1L, binCount)方法会检查当前容量是否需要进行扩容。
- 如果CAS失败，说明有其它线程提前插入了节点，自旋重新尝试在这个位置插入节点

(5)如果f的hash值为-1，说明当前f是ForwardingNode节点，意味有其它线程正在扩容，则一起进行扩容操作。

(6)其余情况把新的Node节点按链表或红黑树的方式插入到合适的位置，这个过程采用同步内置锁实现并发.

在节点f上进行同步，节点插入之前，再次利用tabAt(tab, i) == f判断，防止被其它线程修改。

1. 如果f.hash >= 0，说明f是链表结构的头结点，遍历链表，如果找到对应的node节点，则修改value，否则在链表尾部加入节点。
2. 如果f是TreeBin类型节点，说明f是红黑树根节点，则在树结构上遍历元素，更新或增加节点。
3. 如果链表中节点数binCount >= TREEIFY_THRESHOLD(默认是8)，则把链表转化为红黑树结构。



###5.get操作

	public V get(Object key) {
        Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
		//计算hash
        int h = spread(key.hashCode());
		//根据hash值确定节点位置
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (e = tabAt(tab, (n - 1) & h)) != null) {
			//如果搜索到的节点key与传入的key相同且不为null,直接返回这个节点 
            if ((eh = e.hash) == h) {
                if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                    return e.val;
            }
			//hash小于0，说明是红黑树，在树中寻找
            else if (eh < 0)
                return (p = e.find(h, key)) != null ? p.val : null;
			//否则遍历链表 找到对应的值并返回
            while ((e = e.next) != null) {
                if (e.hash == h &&
                    ((ek = e.key) == key || (ek != null && key.equals(ek))))
                    return e.val;
            }
        }
        return null; //找不到
    }

- 判断table是否为空，如果为空，直接返回null。
- 计算key的hash值，并获取指定table中指定位置的Node节点，通过遍历链表或则树结构找到对应的节点，返回value值。

###6.扩容操作

当table容量不足的时候，即table的元素数量达到容量阈值sizeCtl，需要对table进行扩容。

####a)扩容的情况

当往hashMap中成功插入一个key/value节点时，有可能触发扩容动作：

①如果新增节点之后，所在链表的元素个数达到了阈值 8，则会调用treeifyBin方法把链表转换成红黑树，不过在结构转换之前，会对数组长度进行判断，实现如下：
![](http://upload-images.jianshu.io/upload_images/2184951-4073dae738451fa1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如果数组长度n小于阈值MIN_TREEIFY_CAPACITY（使用扩容而不是树化），默认是64，则会调用tryPresize方法把数组长度扩大到原来的两倍，并触发transfer方法，重新调整节点的位置。
![](http://upload-images.jianshu.io/upload_images/2184951-c78970f8aff43a74.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

②新增节点之后，会调用addCount方法记录元素个数，并检查是否需要进行扩容，当数组元素个数达到阈值时，会触发transfer方法，重新调整节点的位置。
![](http://upload-images.jianshu.io/upload_images/2184951-3e1931b61dc804fb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

####b)transfer实现

transfer方法实现了在并发的情况下，高效的从原始组数往新数组中移动元素，假设扩容之前节点的分布如下，这里区分蓝色节点和红色节点，是为了后续更好的分析：

![](http://upload-images.jianshu.io/upload_images/2184951-8566ed56aa3a1a9c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

在上图中，第14个槽位插入新节点之后，链表元素个数已经达到了8，且数组长度为16，优先通过扩容来缓解链表过长的问题，实现如下：

**transfer方法**
	
	//一个过渡的table表  只有在扩容的时候才会使用
	private transient volatile Node<K,V>[] nextTable;

	private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
        int n = tab.length, stride;  //n为table大小，stride 主要和CPU相关
		//主要是判断CPU处理的量，如果小于16则直接赋值16
        if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
            stride = MIN_TRANSFER_STRIDE; // subdivide range
		//只能有一个线程进行构造nextTable，如果别的线程进入发现不为空就不用构造nextTable了
        if (nextTab == null) {            // initiating
            try {
                @SuppressWarnings("unchecked")
                Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1]; //n << 1原来的两倍
                nextTab = nt; //赋值给nextTab
            } catch (Throwable ex) {      // try to cope with OOME
                sizeCtl = Integer.MAX_VALUE;
                return;
            }
            nextTable = nextTab; 
            transferIndex = n; //扩容索引位置，大小为原先数组大小
        }
		//新数组大小
        int nextn = nextTab.length;
		//初始化ForwardingNode节点，其中保存了新数组nextTable的引用，在处理完每个槽位的节点之后当做占位节点，表示该槽位已经处理过了
        ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
		//并发扩容的关键属性 如果等于true 说明这个节点已经处理过
        boolean advance = true;
		//扩容结束标志
        boolean finishing = false; // to ensure sweep before committing nextTab
		//这个while循环体的作用就是在控制i--  通过i--可以依次遍历原hash表中的节点,从n-1到0
        for (int i = 0, bound = 0;;) {
            Node<K,V> f; int fh;
            while (advance) {
                int nextIndex, nextBound;
                if (--i >= bound || finishing) //i==0
                    advance = false;
                else if ((nextIndex = transferIndex) <= 0) { //遍历完了
                    i = -1;
                    advance = false;
                }
                else if (U.compareAndSwapInt //执行一次，初始化i和bound=0
                         (this, TRANSFERINDEX, nextIndex,
                          nextBound = (nextIndex > stride ?
                                       nextIndex - stride : 0))) {
                    bound = nextBound;
                    i = nextIndex - 1; //初始化i=n-1
                    advance = false; //表示还没有遍历过
                }
            }
            if (i < 0 || i >= n || i + n >= nextn) {
                int sc;
                if (finishing) {  
				//如果所有的节点都已经完成复制工作  就把nextTable赋值给table 清空临时对象nextTable
                    nextTable = null;
                    table = nextTab;
                    sizeCtl = (n << 1) - (n >>> 1);//扩容阈值设置为原来容量的1.5倍  依然相当于现在容量的0.75倍
                    return;
                }
				//利用CAS方法更新这个扩容阈值，在这里面sizectl值减一，说明新加入一个线程参与到扩容操作
                if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                    if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                        return;
                    finishing = advance = true;
                    i = n; // recheck before commit
                }
            }
			//如果遍历到的节点为空 则放入ForwardingNode指针,后面线程如果遍历到的时候直接跳过
            else if ((f = tabAt(tab, i)) == null)
                advance = casTabAt(tab, i, null, fwd);
			//如果遍历到ForwardingNode节点  说明这个点已经被处理过了 直接跳过  这里是控制并发扩容的核心
            else if ((fh = f.hash) == MOVED)
                advance = true; // already processed
            else {
				//对头节点进行加锁，禁止别的线程进入.一个槽只能一个线程进行扩容操作
                synchronized (f) {
					//CAS校验这个节点是否在table对应的i处
                    if (tabAt(tab, i) == f) {
						//分别保存hash值的第X位为0和1的节点
                        Node<K,V> ln, hn;
						//如果fh>=0 证明这是一个链表
                        if (fh >= 0) {
							 //以下的部分在完成的工作是构造两个链表  一个是原链表  另一个是原链表的反序排列
                            int runBit = fh & n;
                            Node<K,V> lastRun = f;
                            for (Node<K,V> p = f.next; p != null; p = p.next) {
                                int b = p.hash & n;
                                if (b != runBit) {
                                    runBit = b;
                                    lastRun = p;
                                }
                            }
                            if (runBit == 0) {
                                ln = lastRun;
                                hn = null;
                            }
                            else {
                                hn = lastRun;
                                ln = null;
                            }
                            for (Node<K,V> p = f; p != lastRun; p = p.next) {
                                int ph = p.hash; K pk = p.key; V pv = p.val;
                                if ((ph & n) == 0)
                                    ln = new Node<K,V>(ph, pk, pv, ln);
                                else
                                    hn = new Node<K,V>(ph, pk, pv, hn);
                            }
                            setTabAt(nextTab, i, ln);//在nextTable的i位置上插入一个链表
                            setTabAt(nextTab, i + n, hn);//在nextTable的i+n的位置上插入另一个链表
                            setTabAt(tab, i, fwd);//在table的i位置上插入forwardNode节点  表示已经处理过该节点
                            advance = true;//设置advance为true 返回到上面的while循环中 就可以执行i--操作
                        }
                        else if (f instanceof TreeBin) {//对TreeBin对象进行处理  与上面的过程类似，红黑树
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> lo = null, loTail = null;
                            TreeNode<K,V> hi = null, hiTail = null;
                            int lc = 0, hc = 0;
							//构造正序和反序两个链表
                            for (Node<K,V> e = t.first; e != null; e = e.next) {
                                int h = e.hash;
                                TreeNode<K,V> p = new TreeNode<K,V>
                                    (h, e.key, e.val, null, null);
                                if ((h & n) == 0) {
                                    if ((p.prev = loTail) == null)
                                        lo = p;
                                    else
                                        loTail.next = p;
                                    loTail = p;
                                    ++lc;
                                }
                                else {
                                    if ((p.prev = hiTail) == null)
                                        hi = p;
                                    else
                                        hiTail.next = p;
                                    hiTail = p;
                                    ++hc;
                                }
                            }
							//如果拆分后的树的节点数量已经少于6个就需要重新转化为链表;否则还是树
                            ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                                (hc != 0) ? new TreeBin<K,V>(lo) : t;
                            hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                                (lc != 0) ? new TreeBin<K,V>(hi) : t;
							//在nextTable的i位置上插入一个链表 
                            setTabAt(nextTab, i, ln);
							 //在nextTable的i+n的位置上插入另一个链表
                            setTabAt(nextTab, i + n, hn);
							//在table的i位置上插入forwardNode节点  表示已经处理过该节点
                            setTabAt(tab, i, fwd);
							//设置advance为true 返回到上面的while循环中 就可以执行i--操作
                            advance = true;
                        }
                    }
                }
            }
        }
    }

1.根据当前数组长度n，新建一个两倍长度的数组nextTable；

![](http://upload-images.jianshu.io/upload_images/2184951-f3165276cfd79a23.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

2.初始化ForwardingNode节点，其中保存了新数组nextTable的引用，在处理完每个槽位的节点之后当做占位节点，表示该槽位已经处理过了；

![](http://upload-images.jianshu.io/upload_images/2184951-3484ef20cbc2c7e6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240) 

3.通过for自循环处理每个槽位中的链表元素，默认advace为真，通过CAS设置transferIndex属性值，并初始化i和bound值，i指当前处理的槽位序号，bound指需要处理的槽位边界，先处理槽位15的节点；

![](http://upload-images.jianshu.io/upload_images/2184951-3c4e5dc1be928d93.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

4.在当前假设条件下，槽位15中没有节点，则通过CAS插入在第二步中初始化的ForwardingNode节点，用于告诉其它线程该槽位已经处理过了；

![](http://upload-images.jianshu.io/upload_images/2184951-c96e02b437097f16.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

5.如果槽位15已经被线程A处理了，那么线程B处理到这个节点时，取到该节点的hash值应该为MOVED，值为-1，则直接跳过，继续处理下一个槽位14的节点；

![](http://upload-images.jianshu.io/upload_images/2184951-45fc2b3e99030c62.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

6.处理槽位14的节点，是一个链表结构，先定义两个变量节点ln和hn，按我的理解应该是lowNode和highNode，分别保存hash值的第X位为0和1的节点，具体实现如下：

![](http://upload-images.jianshu.io/upload_images/2184951-4b7b87d7848d5403.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

使用fn&n可以快速把链表中的元素区分成两类，A类是hash值的第X位为0，B类是hash值的第X位为1，并通过lastRun记录最后需要处理的节点，A类和B类节点可以分散到新数组的槽位14和30中，在原数组的槽位14中，蓝色节点第X为0，红色节点第X为1，把链表拉平显示如下：

![](http://upload-images.jianshu.io/upload_images/2184951-5e60c316353e8a8f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 通过遍历链表，记录runBit和lastRun，分别为1和节点6，所以设置hn为节点6，ln为null；
- 重新遍历链表，以lastRun节点为终止条件，根据第X位的值分别构造ln链表和hn链表：

	ln链：和原来链表相比，顺序已经不一样了
	![](http://upload-images.jianshu.io/upload_images/2184951-00e946e7b274a8af.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

	hn链：![](http://upload-images.jianshu.io/upload_images/2184951-bcc2a0170ec52d9d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

最后，通过CAS把ln链表设置到新数组的i位置，hn链表设置到i+n的位置。

7.如果该槽位是红黑树结构，则构造树节点lo和hi，遍历红黑树中的节点，同样根据hash&n算法，把节点分为两类，分别插入到lo和hi为头的链表中，根据lo和hi链表中的元素个数分别生成ln和hn节点，其中ln节点的生成逻辑如下：

- 如果lo链表的元素个数小于等于UNTREEIFY_THRESHOLD，默认为6，则通过untreeify方法把树节点链表转化成普通节点链表；
- 否则判断hi链表中的元素个数是否等于0：如果等于0，表示lo链表中包含了所有原始节点，则设置原始红黑树给ln，否则根据lo链表重新构造红黑树。

![](http://upload-images.jianshu.io/upload_images/2184951-068339720dccf9e7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

最后，同样的通过CAS把ln设置到新数组的i位置，hn设置到i+n位置。

###7.红黑树构造

**注意：**如果链表结构中元素超过TREEIFY_THRESHOLD阈值，默认为8个，则把链表转化为红黑树，提高遍历查询效率。

	if (binCount != 0) {
	    if (binCount >= TREEIFY_THRESHOLD) //8
	        treeifyBin(tab, i);
	    if (oldVal != null)
	        return oldVal;
	    break;
	}
	//构造树
	private final void treeifyBin(Node<K,V>[] tab, int index) {
	    Node<K,V> b; int n, sc;
	    if (tab != null) {
	        if ((n = tab.length) < MIN_TREEIFY_CAPACITY)
	            tryPresize(n << 1); //如果数组大小小于64,会先扩容
	        else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
	            synchronized (b) {
	                if (tabAt(tab, index) == b) { //再次确认
	                    TreeNode<K,V> hd = null, tl = null; //头尾树节点
	                    for (Node<K,V> e = b; e != null; e = e.next) {
	                        TreeNode<K,V> p =
	                            new TreeNode<K,V>(e.hash, e.key, e.val,
	                                              null, null);
	                        if ((p.prev = tl) == null)
	                            hd = p;
	                        else
	                            tl.next = p;
	                        tl = p;
	                    } //构造链表结构
	                    setTabAt(tab, index, new TreeBin<K,V>(hd)); //生成TreeBin结构，设置到index处
	                }
	            }
	        }
	    }
	}

可以看出，生成树节点的代码块是同步的，进入同步代码块之后，再次验证table中index位置元素是否被修改过。

1、根据table中index位置Node链表，重新生成一个hd为头结点的TreeNode链表。

2、根据hd头结点，生成TreeBin树结构，并把树结构的root节点写到table的index位置的内存中。

**参考文献：**

- 深入浅出ConcurrentHashMap1.8 [http://www.jianshu.com/p/c0642afe03e0](http://www.jianshu.com/p/c0642afe03e0 "深入浅出ConcurrentHashMap1.8")
- 深入分析ConcurrentHashMap1.8的扩容实现 [http://www.jianshu.com/p/f6730d5784ad](http://www.jianshu.com/p/f6730d5784ad "深入分析ConcurrentHashMap1.8的扩容实现")
-  Java并发编程（五）ConcurrentHashMap的实现原理和源码分析 [http://blog.csdn.net/itachi85/article/details/51816668#t6](http://blog.csdn.net/itachi85/article/details/51816668#t6)
-  JDK1.8逐字逐句带你理解ConcurrentHashMap(3) [http://blog.csdn.net/u012403290/article/details/68488562](http://blog.csdn.net/u012403290/article/details/68488562 "JDK1.8逐字逐句带你理解ConcurrentHashMap(3)")