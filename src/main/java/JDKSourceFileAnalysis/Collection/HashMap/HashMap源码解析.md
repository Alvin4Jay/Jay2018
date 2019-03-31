# HashMap源码解析(JDK8为主) #
`HashMap`根据键的`hashCode`值存储数据，大多数情况下可以直接定位到它的值，因而具有很快的访问速度<font  color="red">`O(1)`</font>，但遍历顺序却是不确定的。`HashMap`非线程安全，即任一时刻可以有多个线程同时写`HashMap`，可能会导致数据的不一致。如果需要满足线程安全，可以用`Collections`的<font  color="red" >`synchronizedMap`</font>方法使`HashMap`具有线程安全的能力，或者使用<font  color="red">`ConcurrentHashMap`</font>。

```java
// 在初始化HashMap时，将输入的容量任意值转化为大于等于此值的2的幂次方	
static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}
```

## 一、存储结构——字段

从结构实现来讲，`HashMap`是**数组+链表+红黑树**（JDK1.8增加了红黑树部分）实现的，如下图所示。
![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%E7%BB%93%E6%9E%84.png?x-oss-process=style/markdown-pic)

(1)数据底层存储的数据结构——Node[] table，即哈希桶数组，它是一个Node的数组。

```java
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;   //用来定位数组索引位置
    final K key;
    V data;
    Node<K,V> next;   //链表的下一个Node

    Node(int hash, K key, V data, Node<K,V> next) {
        this.hash = hash;
        this.key = key;
        this.data = data;
        this.next = next;
    }

    public final K getKey()        { return key; }
    public final V getValue()      { return data; }
    public final String toString() { return key + "=" + data; }

    public final int hashCode() {}

    public final V setValue(V newValue) {}

    public final boolean equals(Object o) {}
}
```

(2)`HashMap`底层使用数组+链表+红黑树这样的存储结构的优点(**哈希表**):
>  哈希表为解决冲突，可以采用**开放地址法**和**链地址法**等来解决问题，**Java中HashMap采用了链地址法**。链地址法，简单来说，就是数组加链表的结合。在每个数组元素上都有一个链表结构，当数据被Hash后，得到数组下标，把数据放在对应下标元素的链表上。

```java
map.put("杭州","西湖");
```
系统将调用”杭州”这个key的**hashCode()**方法得到其hashCode 值（该方法适用于每个Java对象），然后再通过Hash算法的后两步运算（**高位运算和取模运算**，下文有介绍）来定位该键值对的存储位置，有时两个key会定位到相同的位置，表示发生了Hash碰撞。当然Hash算法计算结果越分散均匀，Hash碰撞的概率就越小，map的存取效率就会越高。

(3)`HashMap`的字段

```java
int threshold;             // 所能容纳的key-value对阈值
final float loadFactor;    // 负载因子
int modCount;              // 结构性修改次数
int size;									 // key-value对个数
```

- Node[] table的初始化长度length(默认值是16)，loadFactor为负载因子(默认值是0.75)，threshold是HashMap所能容纳的最大数据量的Node(键值对)个数。`threshold = length * Load factor`。也就是说，**在数组定义好长度之后，负载因子越大，所能容纳的键值对个数越多**。

- 结合负载因子的定义公式可知，**threshold就是在此loadFactor和length(数组长度)对应下允许的最大元素数目，超过这个数目就重新resize(扩容)，扩容后的HashMap容量是之前容量的两倍。**默认的负载因子0.75是对空间和时间效率的一个平衡选择，建议不要修改，除非在时间和空间比较特殊的情况下，**如果内存空间很多而又对时间效率要求很高**，可以**降低**负载因子Load factor的值；相反，**如果内存空间紧张而对时间效率要求不高**，可以**增加**负载因子loadFactor的值，这个值可以大于1。

- 而**modCount**字段主要用来记录HashMap**内部结构发生变化**的次数，**主要用于迭代的快速失败**。强调一点，<font color="red">**内部结构发生变化指的是结构发生变化，例如put新键值对，但是某个key对应的value值被覆盖(更新)不属于结构变化。**</font>

- 特别要指出:在HashMap中，哈希桶数组table的长度**length**大小**必须为2的n次方**(一定是合数)，这是一种非常规的设计，常规的设计是把桶的大小设计为素数。相对来说素数导致冲突的概率要小于合数。<font  color="red">HashMap采用这种非常规设计，**主要是为了在取模和扩容时做优化；同时为了减少冲突，HashMap定位哈希桶索引位置时，也加入了高位参与运算的过程。**</font>

(4)JDK1.8引入红黑树
即使**负载因子和Hash算法设计的再合理**，也免不了会出现<font color="red">拉链过长</font>的情况，一旦出现拉链过长，则会严重影响HashMap的性能。于是，在JDK1.8版本中，对数据结构做了进一步的优化，引入了红黑树。而当链表长度太长（默认超过8）时，链表就转换为红黑树，利用红黑树快速增删改查的特点提高HashMap的性能，其中会<font color="red">用到红黑树的插入、删除、查找等算法</font>。

## 二、功能实现——方法

主要分析:①根据key获取哈希桶数组索引位置;②put方法的详细执行;③get方法解析④扩容过程

### 1.确定哈希桶数组索引位置
不管增加、删除、查找键值对，定位到哈希桶数组的位置都是很关键的第一步。

```java
方法一：
static final int hash(Object key) {   //jdk1.8源码，jdk1.7类似
 int h;
 // h = key.hashCode() 为第一步 取hashCode值，通用
 // h ^ (h >>> 16)  为第二步 高位参与运算，减少碰撞冲突
 return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
方法二：
static int indexFor(int h, int length) {  //jdk1.7的源码，jdk1.8没有这个方法，但是实现原理一样的
 return h & (length-1);  //第三步 取模运算，确定数组索引位置。这样分布比较均匀
}
```

可以看到，确定索引位置有三步：**取key的hashCode值、高位运算、取模运算。**

(a)方法二很巧妙，它通过h & (table.length -1)来得到该对象的保存位置，而HashMap底层数组的长度总是2的n次方，这是**HashMap在速度上的优化(取模)**。当length总是2的n次方时，h& (length-1)运算等价于对length取模，也就是h%length，但是**&比%具有更高的效率**。
(b)在JDK1.8的实现中，优化了**高位运算**的算法，通过hashCode()的高16位异或低16位实现的：(h = k.hashCode()) ^ (h >>> 16)，主要是从速度、功效、质量来考虑的，这么做可以在数组table的length比较小的时候，也能保证考虑到高低Bit都参与到Hash的计算中(减少冲突，分布均匀)，同时不会有太大的开销。

举例如下(n为table长度，n=16):
![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%20hash%E5%80%BC%E8%AE%A1%E7%AE%97.png)

### 2.put(K key, V data)方法

流程：

第一步: 判断键值对数组table[i]是否为null或长度为0，否则执行resize()进行扩容(或者称之为初始化)；

第二步: 根据键key计算hash值得到插入的数组索引i，如果table[i]==null，直接新建节点添加，进入第五步；否则进入第三步；
第三步: 如果table[i]!=null，判断table[i]的首个元素的键key是否和传入的key一样，如果相同直接覆盖value，这里的相同指的是hashCode以及equals；否则，进入第四步；
第四步: 首先判断该链表是否已经是红黑树，如果是红黑树，则直接在树中插入键值对；否则遍历table[i]，如果未找到存在的key，则直接在尾部插入，并根据需要转为红黑树结构；如果是已经存在原key，则直接更新，并返回旧值；
第五步: 操作完成后，如果结果是插入成功，则检查实际存在的键值对数量size是否超过了阈值threshold，如果超过，进行扩容。


```java
public V put(K key, V data) {
		//hash(key) 对key进行hash操作，计算hash值(包括高16位亦或运算)
    return putVal(hash(key), key, data, false, true);
}
final V putVal(int hash, K key, V data, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
		//第一步：table为空，则创建初始化，分配内存
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
		//第二步:计算出索引i, 若table[i]==null,直接添加新节点，直接进入第五步
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, data, null);
    else {
				//第三步:否则，判断table[i]的首个元素是否和key一样，如果相同，直接覆盖value
        Node<K,V> e; K k;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
				//第四步：该链表是红黑树，直接在红黑树中插入键值对
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, data);
        else {
						//这是一条普通链表，执行遍历操作
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) { //未找到key相等的Node，执行尾部插入
                    p.next = newNode(hash, key, data, null);
                    //若节点插入后链表长度大于8，转换为红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
								//key已存在，直接覆盖更新
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
      	// key已存在时，直接覆盖、更新
        if (e != null) { // existing mapping for key
            V oldValue = e.data;
            if (!onlyIfAbsent || oldValue == null)
                e.data = data;
            afterNodeAccess(e);
            return oldValue;
        }
    }
		//第五步，如果是插入成功操作，检测是否需要扩容
    ++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```

### 3.get()方法

流程：

第一步: 首先计算hash值，然后调用getNode()方法；
第二步: 先判断table是否null，为空直接返回null；否则，计算数组索引位置i，判断该索引位置处table[i]如果为null，直接返回null；否则，进入下面判断;
第三步: 判断table[i]的首个元素的键key是否和传入的key一样，如果相同，直接返回table[i]；否则，进入第四步；
第四步: 如果为红黑树结构，直接使用红黑树方式查找； 
第五步: 如果为普通链表，直接遍历查找，找到返回；找不到，返回null。

```java
public V get(Object key) {
    Node<K,V> e;
		//首先计算hash值，然后调用getNode()方法
    return (e = getNode(hash(key), key)) == null ? null : e.data;
}
final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
		//先判断table是否null,为空直接返回null；否则，计算数组索引位置i，判断该索引位置处table[i]如果为null，直接返回null;否则，进入下面判断
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
				//判断table[i]的首个元素的键key是否和传入的key一样，如果相同直接返回table[i]
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
				//查找
        if ((e = first.next) != null) {
						//如果为红黑树结构，直接使用红黑树方式查找
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            do {
								//普通链表，直接遍历查找，找到返回；找不到，返回null
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```

### 4.扩容机制resize()

当向HashMap对象里不停的添加元素，且size大于阈值threshold(JDK8；JDK7是在size等于threshold且插入元素之前进行扩容)，就需要扩大数组的长度，以便能装入更多的元素。<font color="red">方法是使用一个新的数组代替已有的容量小的数组，就像用一个小桶装水，如果想装更多的水，就得换大水桶。</font>

鉴于JDK1.8融入了红黑树，较复杂，为了便于理解仍然使用JDK1.7的代码进行分析，好理解一些，本质上区别不大。

```java
// jdk1.7扩容
void resize(int newCapacity) {   //传入新的容量
	Entry[] oldTable = table;    //引用扩容前的Entry数组
	int oldCapacity = oldTable.length;         
	if (oldCapacity == MAXIMUM_CAPACITY) {  //扩容前的数组大小如果已经达到最大(2^30)了
		threshold = Integer.MAX_VALUE; //修改阈值为int的最大值(2^31-1)，这样以后就不会扩容了
		return;
	}
 		
	Entry[] newTable = new Entry[newCapacity];  //初始化一个新的Entry数组
	transfer(newTable);                         //！！将数据转移到新的Entry数组里
	table = newTable;                           //HashMap的table属性引用新的Entry数组
	threshold = (int)(newCapacity * loadFactor);//修改阈值
}
```
这里就是使用一个容量更大的数组来代替已有的容量小的数组，transfer()方法将原有Entry数组的元素拷贝到新的Entry数组里。

```java
// jdk1.7 转移
void transfer(Entry[] newTable) {
	Entry[] src = table;                   //src引用了旧的Entry数组
	int newCapacity = newTable.length;
	for (int j = 0; j < src.length; j++) { //遍历旧的Entry数组
		Entry<K,V> e = src[j];             //取得旧Entry数组的每个元素
		if (e != null) {
			src[j] = null;//释放旧Entry数组的对象引用（for循环后，旧的Entry数组不再引用任何对象）
			do {
				Entry<K,V> next = e.next;
				int i = indexFor(e.hash, newCapacity); //！！重新计算每个元素在数组中的位置
				e.next = newTable[i]; //头插法
				newTable[i] = e;      //将元素放在数组上
				e = next;             //访问下一个Entry元素
			} while (e != null);
		}
	}
}
```

newTable[i]的引用赋给了e.next，也就是使用了单链表的头插入方式，同一位置上新元素总会被放在链表的头部位置；这样先放在一个索引上的元素终会被放到Entry链的尾部(如果发生了hash冲突的话），**这一点和Jdk1.8有区别**。**在旧数组中同一条Entry链上的元素，通过重新计算索引位置后，有可能被放到了新数组的不同位置上。**

下面举个例子说明下扩容过程。假设我们的hash算法就是简单的用key mod 一下表的大小（也就是数组的长度）。其中哈希桶数组table的长度n=2，key = 3、7、5，put顺序依次为 5、7、3。在mod 2以后都冲突在table[1]这里了。这里假设负载因子 loadFactor=1，即当要插入key=5时，且此时键值对的实际大小size 等于 table的实际大小，然后进行扩容。接下来的三个步骤是哈希桶数组 resize成4，然后key为3、7对应的Node**重新计算在新数组中的位置**的过程并移动，以及扩容后插入key5。(JDK7)
![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%20JDK7%20transfer.jpg)

<font color="red">JDK1.8优化:</font>

经过观测可以发现，我们使用的是2次幂的扩展(指长度扩为原来2倍)，所以，元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置。看下图可以明白这句话的意思，n为table的长度，图（a）表示扩容前的key1和key2两种key确定索引位置的示例，图（b）表示扩容后key1和key2两种key确定索引位置的示例，其中hash1是key1对应的哈希与高位运算结果。

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%20jdk8%20rehash1.png)

因为数组长度n变为2倍，那么n-1的mask范围在高位多1bit(红色)，因此元素新的index就会发生这样的变化：
![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%20jdk8%20rehash2.png)
因此，我们在扩充HashMap的时候，不需要像JDK1.7的实现那样重新计算元素索引位置，只需要看看原来的hash值新增的那个bit是1还是0就好了，是0的话索引没变，是1的话索引变成“原索引+oldCap”，如下图
![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%20jdk8%20rehash3.png)

这个设计确实非常的巧妙，既省去了重新计算元素在新数组中索引的时间，而且同时，由于新增的1bit是0还是1可以认为是随机的，因此resize的过程，均匀的把之前的冲突的节点分散到新的bucket了。这一块就是JDK1.8新增的优化点。有一点注意区别，JDK1.7中resize的时候，旧链表迁移新链表的时候，如果在新表的数组索引位置相同，则链表元素会倒置，但是从上图可以看出，JDK1.8不会倒置。JDK1.8的resize源码如下:

```java
// JDK1.8扩容过程
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length; // 原容量
    int oldThr = threshold; // 原阈值
    int newCap, newThr = 0; // 新容量、新阈值
    if (oldCap > 0) {
        // 容量已超过最大值就不再扩充了，就只好随你碰撞去吧
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        // 没超过最大值，就扩充为原来的2倍
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            newThr = oldThr << 1; // double threshold
    }
  	// oldThr变量保存着初始容量
    else if (oldThr > 0) // initial capacity was placed in threshold
        newCap = oldThr;
    else {               // zero initial threshold signifies using defaults
      	// oldThr、oldCap都为null，表示使用默认值
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    // 计算新的阈值
    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes"，"unchecked"})
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap]; // 创建新table
    table = newTab;
    if (oldTab != null) {
        // 把每个bucket中的节点都移动到新的table中
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;
                if (e.next == null) // 当前bucket只有一个节点
                    newTab[e.hash & (newCap - 1)] = e;
                else if (e instanceof TreeNode) // 当前bucket已经是红黑树结构，直接操作红黑树
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // 普通链表，则将该链表分为原索引，原索引+oldCap两个位置的两条链表
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;
                        // 原索引
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        // 原索引+oldCap
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    // 原索引放到bucket里，节点相对位置不变
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    // 原索引+oldCap放到bucket里，节点相对位置不变
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```

## 三、HashMap在JDK8与JDK7中实现的不同点

- 存储结构：JDK8中使用了数组+链表+红黑树的形式，JDK7中为数组+链表，无红黑树；
- put方法：JDK8添加新元素是添加到链表尾部，JDK7是头插法；
- resize：
  - JDK7：需要重新计算元素在新数组中的位置，并且原来在同一数组索引中的元素，若扩容后仍然在同一索引处，相对位置会颠倒(头插法)；
  - JDK8：不需要重新计算元素在新数组中的索引，并且原来在同一数组索引中的元素，若扩容后仍然在同一数组索引处，相对位置不变。

## 四、为什么JDK8中链表转为红黑树的阈值是8

源码说明(`Implementation notes.`)：

**TreeNodes占用空间是普通Nodes的两倍**，所以只有当bin包含足够多的节点时才会转成TreeNodes，而是否足够多就是由**TREEIFY_THRESHOLD**的值决定的。当bin中节点数变少时，又会转成普通的bin。并且我们查看源码的时候发现，链表长度达到8就转成红黑树，当长度降到6就转成普通bin。

这就解析了为什么不是一开始就将其转换为TreeNodes，而是需要一定节点数才转为TreeNodes，就是trade-off，空间和时间的权衡：

```java
Because TreeNodes are about twice the size of regular nodes, we
use them only when bins contain enough nodes to warrant use
(see TREEIFY_THRESHOLD). And when they become too small (due to
removal or resizing) they are converted back to plain bins.  In
usages with well-distributed user hashCodes, tree bins are
rarely used.  Ideally, under random hashCodes, the frequency of
nodes in bins follows a Poisson distribution
(http://en.wikipedia.org/wiki/Poisson_distribution) with a
parameter of about 0.5 on average for the default resizing
threshold of 0.75, although with a large variance because of
resizing granularity. Ignoring variance, the expected
occurrences of list size k are (exp(-0.5) * pow(0.5, k) /
factorial(k)). The first values are:
0:    0.60653066
1:    0.30326533
2:    0.07581633
3:    0.01263606
4:    0.00157952
5:    0.00015795
6:    0.00001316
7:    0.00000094
8:    0.00000006
more: less than 1 in ten million
```

这段内容说到：当hashCode离散性很好的时候，树型bin用到的概率非常小，因为数据均匀分布在每个bin中，几乎不会有bin中链表长度会达到阈值。但是在随机hashCode下，离散性可能会变差，然而JDK又不能阻止用户实现这种不好的hash算法，因此就可能导致不均匀的数据分布。不过理想情况下随机hashCode算法下所有bin中节点的分布频率会遵循**泊松分布**，可以看到，一个bin中链表长度达到8个元素的概率为0.00000006，几乎是不可能事件。所以之所以选择8，是根据概率统计决定的。

## 五、HashMap的死循环问题

由于HashMap并非是线程安全的，所以在高并发的情况下必然会出现问题，这是一个普遍的问题。如果是在单线程下使用HashMap，是没有问题的，如果后期由于代码优化，这段逻辑引入了多线程并发执行，在一个未知的时间点，会发现CPU占用100%，居高不下，通过查看堆栈，会惊讶的发现，线程都Hang在hashMap的get()方法上，服务重启之后，问题消失，过段时间可能又复现了。

**案例分析(以JDK7为例)**

假设HashMap初始化大小为4，插入个3节点，不巧的是，这3个节点都hash到同一个位置，使用默认的负载因子0.75。即当插入第4个节点时，需要扩容。

```java
void transfer(Entry[] newTable, boolean rehash) {
    int newCapacity = newTable.length;
    for (Entry<K,V> e : table) {
        while(null != e) {
            Entry<K,V> next = e.next;
            if (rehash) {
                e.hash = null == e.key ? 0 : hash(e.key);
            }
            int i = indexFor(e.hash, newCapacity);
            e.next = newTable[i];
            newTable[i] = e;
            e = next;
        }
    }
}
```

以上是节点移动的相关逻辑。

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%E6%AD%BB%E5%BE%AA%E7%8E%AF1.jpg)

插入第4个节点时，发生rehash(扩容)，假设现在有两个线程同时进行，线程1和线程2，两个线程都会新建新的数组。

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%E6%AD%BB%E5%BE%AA%E7%8E%AF2.jpg?x-oss-process=style/markdown-pic)

假设 **线程2** 在执行到`Entry<K,V> next = e.next;`之后，cpu时间片用完了，这时变量e指向节点a，变量next指向节点b。

**线程1**继续执行，很不巧，a、b、c节点rehash之后又是在同一个位置7，开始移动节点

第一步，移动节点a

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%E6%AD%BB%E5%BE%AA%E7%8E%AF3.jpg?x-oss-process=style/markdown-pic)

第二步，移动节点b

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%E6%AD%BB%E5%BE%AA%E7%8E%AF4.jpg?x-oss-process=style/markdown-pic)

注意，这里的顺序是反过来的，继续移动节点c

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%E6%AD%BB%E5%BE%AA%E7%8E%AF5.jpg?x-oss-process=style/markdown-pic)

这个时候 **线程1** 的时间片用完，内部的table还没有设置成新的newTable， **线程2** 开始执行，这时内部的引用关系如下：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%E6%AD%BB%E5%BE%AA%E7%8E%AF6.jpg?x-oss-process=style/markdown-pic)

这时，在 **线程2** 中，变量e指向节点a，变量next指向节点b，开始执行循环体的剩余逻辑。

```java
Entry<K,V> next = e.next;
int i = indexFor(e.hash, newCapacity);
e.next = newTable[i];
newTable[i] = e;
e = next;
```

执行之后的引用关系如下图

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%E6%AD%BB%E5%BE%AA%E7%8E%AF7.jpg?x-oss-process=style/markdown-pic)

执行后，变量e指向节点b，因为e不是null，则继续执行循环体，执行后的引用关系

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%E6%AD%BB%E5%BE%AA%E7%8E%AF8.jpg?x-oss-process=style/markdown-pic)

变量e又重新指回节点a，只能继续执行循环体，这里仔细分析下： 1、执行完`Entry<K,V> next = e.next;`，目前节点a没有next，所以变量next指向null； 2、`e.next = newTable[i];` 其中 newTable[i] 指向节点b，那就是把a的next指向了节点b，这样a和b就相互引用了，形成了一个环； 3、`newTable[i] = e` 把节点a放到了数组i位置； 4、`e = next;` 把变量e赋值为null，因为第一步中变量next就是指向null；

所以最终的引用关系是这样的：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/HashMap%E6%AD%BB%E5%BE%AA%E7%8E%AF9.jpg?x-oss-process=style/markdown-pic)

节点a和b互相引用，形成了一个环，当在数组该位置get寻找对应的key时，就发生了死循环。

另外，如果线程2把newTable设置成内部的table，节点c的数据就丢了，因此**还有数据遗失的问题**。

**总结**

所以在并发的情况，发生扩容时，可能会产生循环链表，在执行get的时候，会触发死循环，引起CPU的100%问题，所以一定要避免在并发环境下使用HashMap，要并发就用ConcurrentHashmap。

## 参考文章

- [Java 8系列之重新认识HashMap](<https://tech.meituan.com/2016/06/24/java-hashmap.html>)
- [老生常谈，HashMap的死循环](<https://juejin.im/post/5a66a08d5188253dc3321da0>)