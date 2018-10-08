# `HashMap`源码解析 #
它根据键的`hashCode`值存储数据，大多数情况下可以直接定位到它的值，因而具有很快的访问速度<font  color="red" size="5px">`O(1)`</font>，但遍历顺序却是不确定的。`HashMap`非线程安全，即任一时刻可以有多个线程同时写`HashMap`，可能会导致数据的不一致。如果需要满足线程安全，可以用`Collections`的<font  color="red" size="5px">`synchronizedMap`</font>方法使`HashMap`具有线程安全的能力，或者使用<font  color="red" size="5px">`ConcurrentHashMap`</font>。
	
	//在初始化HashMap时，将我们输入的容量任意值转化为大于等于此值的2的幂次方	
	static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

## 一、存储结构———字段 ##
从结构实现来讲，`HashMap`是**数组+链表+红黑树**（JDK1.8增加了红黑树部分）实现的，如下如所示。
![](http://onh97xzo0.bkt.clouddn.com/hashMap%E5%86%85%E5%AD%98%E7%BB%93%E6%9E%84%E5%9B%BE.png)<br>
**(1)数据底层存储的数据结构**————Node[] table，即哈希桶数组，它是一个Node的数组.
 
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

(2)`HashMap`底层使用数组+链表——红黑树这样的存储结构的优点(**哈希表**):
>  哈希表为解决冲突，可以采用**开放地址法**和**链地址法**等来解决问题，**Java中HashMap采用了链地址法**。链地址法，简单来说，就是数组加链表的结合。在每个数组元素上都一个链表结构，当数据被Hash后，得到数组下标，把数据放在对应下标元素的链表上。

	map.put("美团","小美");
系统将调用”美团”这个key的**hashCode()**方法得到其hashCode 值（该方法适用于每个Java对象），然后再通过Hash算法的后两步运算（**高位运算和取模运算**，下文有介绍）来定位该键值对的存储位置，有时两个key会定位到相同的位置，表示发生了Hash碰撞。当然Hash算法计算结果越分散均匀，Hash碰撞的概率就越小，map的存取效率就会越高。

(3)`HashMap`的字段

    int threshold;             // 所能容纳的key-value对极限值
	final float loadFactor;    // 负载因子
	int modCount;  
	int size;

- Node[] table的初始化长度length(默认值是16)，loadFactor为负载因子(默认值是0.75)，threshold是HashMap所能容纳的最大数据量的Node(键值对)个数。`threshold = length * Load factor`。也就是说，**在数组定义好长度之后，负载因子越大，所能容纳的键值对个数越多**。

- 结合负载因子的定义公式可知，**threshold就是在此loadFactor和length(数组长度)对应下允许的最大元素数目，超过这个数目就重新resize(扩容)，扩容后的HashMap容量是之前容量的两倍。**默认的负载因子0.75是对空间和时间效率的一个平衡选择，建议大家不要修改，除非在时间和空间比较特殊的情况下，**如果内存空间很多而又对时间效率要求很高**，可以**降低**负载因子Load factor的值；相反，**如果内存空间紧张而对时间效率要求不高**，可以**增加**负载因子loadFactor的值，这个值可以大于1。

- 而**modCount**字段主要用来记录HashMap**内部结构发生变化**的次数，**主要用于迭代的快速失败**。强调一点，<font color="red">**内部结构发生变化指的是结构发生变化，例如put新键值对，但是某个key对应的value值被覆盖(更新)不属于结构变化。**</font>

- 特别要指出:在HashMap中，哈希桶数组table的长度**length**大小**必须为2的n次方**(一定是合数)，这是一种非常规的设计，常规的设计是把桶的大小设计为素数。相对来说素数导致冲突的概率要小于合数。<font  color="red" size="4px">HashMap采用这种非常规设计，**主要是为了在取模和扩容时做优化，同时为了减少冲突，HashMap定位哈希桶索引位置时，也加入了高位参与运算的过程。**</font>

(4)JDK1.8引入红黑树
即使**负载因子和Hash算法设计的再合理**，也免不了会出现<font color="red">拉链过长</font>的情况，一旦出现拉链过长，则会严重影响HashMap的性能。于是，在JDK1.8版本中，对数据结构做了进一步的优化，引入了红黑树。而当链表长度太长（默认超过8）时，链表就转换为红黑树，利用红黑树快速增删改查的特点提高HashMap的性能，其中会<font color="red">用到红黑树的插入、删除、查找等算法</font>。

##二、功能实现———方法 ##

主要说明:①根据key获取哈希桶数组索引位置;②put方法的详细执行;③get方法解析④扩容过程

1.确定哈希桶数组索引位置
不管增加、删除、查找键值对，定位到哈希桶数组的位置都是很关键的第一步。

    方法一：
	static final int hash(Object key) {   //jdk1.8 & jdk1.7
     int h;
     // h = key.hashCode() 为第一步 取hashCode值，通用
     // h ^ (h >>> 16)  为第二步 高位参与运算，减少碰撞冲突
     return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
	}
	方法二：
	static int indexFor(int h, int length) {  //jdk1.7的源码，jdk1.8没有这个方法，但是实现原理一样的
     return h & (length-1);  //第三步 取模运算，确定数组索引位置。这样分布比较均匀
	}

可以看到，确定索引位置有三步：**取key的hashCode值、高位运算、取模运算.**

(a)方法二很巧妙，它通过h & (table.length -1)来得到该对象的保存位置，而HashMap底层数组的长度总是2的n次方，这是**HashMap在速度上的优化**。当length总是2的n次方时，h& (length-1)运算等价于对length取模，也就是h%length，但是**&比%具有更高的效率**。
(b)在JDK1.8的实现中，优化了**高位运算**的算法，通过hashCode()的高16位异或低16位实现的：(h = k.hashCode()) ^ (h >>> 16)，主要是从速度、功效、质量来考虑的，这么做可以在数组table的length比较小的时候，也能保证考虑到高低Bit都参与到Hash的计算中(减少冲突，分布均匀)，同时不会有太大的开销。

举例如下(n为table长度，n=16):
![](http://tech.meituan.com/img/java-hashmap/hashMap%E5%93%88%E5%B8%8C%E7%AE%97%E6%B3%95%E4%BE%8B%E5%9B%BE.png)

2.put(K key, V data)方法详解

操作流程如图：
![](http://tech.meituan.com/img/java-hashmap/hashMap%20put%E6%96%B9%E6%B3%95%E6%89%A7%E8%A1%8C%E6%B5%81%E7%A8%8B%E5%9B%BE.png)

第一步: 判断键值对数组table[i]是否为空或为null，否则执行resize()进行扩容(或者称之为初始化)；<br />
第二步: 根据键值key计算hash值得到插入的数组索引i，如果table[i]==null，直接新建节点添加，进入第五步；否则进入第三步；<br/>
第三步: 如果table[i]!=null, .判断table[i]的首个元素是否和key一样，如果相同直接覆盖value,这里的相同指的是hashCode以及equals；否则，进入第四步；<br/>
第四步: 首先判断该链表是否已经是红黑树，如果是红黑树，则转化为红黑树节点进行插入操作；否则遍历table[i]，如果未找到存在的key，则直接插入；如果是已经存在原key，则直接更新.<br/>
第五步: 操作完成后，如果结果是插入成功，则检查实际存在的键值对数量size是否超多了最大容量threshold，如果超过，进行扩容。


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
			//第三步:否则，判断table[i]的首个元素是否和key一样，如果相同直接覆盖value
            Node<K,V> e; K k;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
			//第四步：判断该链表是红黑树
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, data);
            else {
			//这一一条普通链表，执行遍历操作
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, data, null);
						 //链表长度大于8转换为红黑树进行处理
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
            if (e != null) { // existing mapping for key
                V oldValue = e.data;
                if (!onlyIfAbsent || oldValue == null)
                    e.data = data;
                afterNodeAccess(e);
                return oldValue;
            }
        }
		//如果是插入成功操作，检测是否需要扩容
        ++modCount;
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }

3.get()方法解析

第一步: 首先计算hash值，然后调用getNode()方法;<br>
第二步: 先判断table是否null,为空直接返回null；否则，计算数组索引位置i，判断是否该索引位置处，table[i]如果为null，直接返回null;否则，进入下面判断;<br>
第三步: 判断table[i]的首个元素是否和key一样，如果相同直接返回table[i];否则，进入第四步；<br>
第四步: 如果为红黑树结构，直接使用红黑树方式查找; <br>
第五步: 如果为普通链表，直接遍历查找，找到返回；找不到，返回null

	public V get(Object key) {
        Node<K,V> e;
		//首先计算hash值，然后调用getNode()方法
        return (e = getNode(hash(key), key)) == null ? null : e.data;
    }
	final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
		//先判断table是否null,为空直接返回null；否则，计算数组索引位置i，判断是否该索引位置处，table[i]如果为null，直接返回null;否则，进入下面判断
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
			//判断table[i]的首个元素是否和key一样，如果相同直接返回table[i]
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

4.扩容机制(resize())

当向HashMap对象里不停的添加元素，而HashMap对象内部的数组无法装载更多的元素时，对象就需要扩大数组的长度，以便能装入更多的元素。<font color="red">方法是使用一个新的数组代替已有的容量小的数组，就像我们用一个小桶装水，如果想装更多的水，就得换大水桶。</font>

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
这里就是使用一个容量更大的数组来代替已有的容量小的数组，transfer()方法将原有Entry数组的元素拷贝到新的Entry数组里。

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
					e = next;             //访问下一个Entry链上的元素
				} while (e != null);
			}
		}
	}

newTable[i]的引用赋给了e.next，也就是使用了单链表的头插入方式，同一位置上新元素总会被放在链表的头部位置；这样先放在一个索引上的元素终会被放到Entry链的尾部(如果发生了hash冲突的话），这一点和Jdk1.8有区别。**在旧数组中同一条Entry链上的元素，通过重新计算索引位置后，有可能被放到了新数组的不同位置上。**

下面举个例子说明下扩容过程。假设了我们的hash算法就是简单的用key mod 一下表的大小（也就是数组的长度）。其中哈希桶数组table的size=2， 所以key = 3、7、5，put顺序依次为 5、7、3。在mod 2以后都冲突在table[1]这里了。这里假设负载因子 loadFactor=1，即当键值对的实际大小size 大于 table的实际大小时进行扩容。接下来的三个步骤是哈希桶数组 resize成4，然后所有的Node重新rehash的过程。
![](http://tech.meituan.com/img/java-hashmap/jdk1.7%E6%89%A9%E5%AE%B9%E4%BE%8B%E5%9B%BE.png)

<font color="red" size="4px">JDK1.8优化:</font>

经过观测可以发现，我们使用的是2次幂的扩展(指长度扩为原来2倍)，所以，元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置。看下图可以明白这句话的意思，n为table的长度，图（a）表示扩容前的key1和key2两种key确定索引位置的示例，图（b）表示扩容后key1和key2两种key确定索引位置的示例，其中hash1是key1对应的哈希与高位运算结果。

![](http://tech.meituan.com/img/java-hashmap/hashMap%201.8%20%E5%93%88%E5%B8%8C%E7%AE%97%E6%B3%95%E4%BE%8B%E5%9B%BE1.png)

元素在重新计算hash之后，因为n变为2倍，那么n-1的mask范围在高位多1bit(红色)，因此新的index就会发生这样的变化：
![](http://tech.meituan.com/img/java-hashmap/hashMap%201.8%20%E5%93%88%E5%B8%8C%E7%AE%97%E6%B3%95%E4%BE%8B%E5%9B%BE2.png)
因此，我们在扩充HashMap的时候，不需要像JDK1.7的实现那样重新计算hash，只需要看看原来的hash值新增的那个bit是1还是0就好了，是0的话索引没变，是1的话索引变成“原索引+oldCap”，如下图
![](http://tech.meituan.com/img/java-hashmap/jdk1.8%20hashMap%E6%89%A9%E5%AE%B9%E4%BE%8B%E5%9B%BE.png)

这个设计确实非常的巧妙，既省去了重新计算hash值的时间，而且同时，由于新增的1bit是0还是1可以认为是随机的，因此resize的过程，均匀的把之前的冲突的节点分散到新的bucket了。这一块就是JDK1.8新增的优化点。