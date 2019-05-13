# TreeMap源码分析

TreeMap是一个有序的key-value集合，它内部是通过红-黑树实现的，如果对红-黑树不太了解，请参考下面这篇文章: [教你透彻了解红黑树](https://xuanjian1992.top/2019/04/30/%E6%95%99%E4%BD%A0%E9%80%8F%E5%BD%BB%E4%BA%86%E8%A7%A3%E7%BA%A2%E9%BB%91%E6%A0%91/)。下面先看TreeMap的类图:

```java
public class TreeMap<K,V>
    extends AbstractMap<K,V>
    implements NavigableMap<K,V>, Cloneable, java.io.Serializable
{
	// ...
}
```

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/java%E6%BA%90%E7%A0%81/TreeMap%E7%B1%BB%E5%9B%BE.png)

从类图可以看出，TreeMap继承自AbstractMap，实现了NavigableMap接口，意味着它支持一系列的导航方法，比如返回有序的key集合。它还实现了Cloneable接口，意味着它能被克隆。另外也实现了Serializable接口，表示它支持序列化。

TreeMap是基于红-黑树实现的，该映射根据其key的自然顺序进行排序，或者根据用户创建映射时提供的Comarator进行排序，另外，**TreeMap是非同步的**。

## 一、属性

```java
// 比较器。若为null，表示使用key的自然顺序
private final Comparator<? super K> comparator;
// 红黑树根节点
private transient Entry<K,V> root;

// 红黑树节点个数(映射个数)
private transient int size = 0;

// 结构性修改次数
private transient int modCount = 0;
```

## 二、构造器

```java
public TreeMap() { 
    comparator = null; // 使用key的自然顺序
}

public TreeMap(Comparator<? super K> comparator) {
    this.comparator = comparator; // 指定比较器
}


public TreeMap(Map<? extends K, ? extends V> m) {
    comparator = null;
    // 调用putAll方法。若m不是SortedMap，时间复杂度为O(NlgN);m为SortedMap，则时间复杂度为O(N)
    putAll(m); 
}

public TreeMap(SortedMap<K, ? extends V> m) {
    comparator = m.comparator();
    try {
      	// 调用buildFromSorted构建红黑树，时间复杂度O(N)。buildFromSorted下面分析
        buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
    } catch (java.io.IOException cannotHappen) {
    } catch (ClassNotFoundException cannotHappen) {
    }
}
```

## 三、Entry<K, V>

由于TreeMap是基于红黑树实现的，所以其内部维护了一个红-黑树的数据结构，每个key-value对也存储在一个Entry里，只不过这个Entry和HashMap或者HashTable中的Entry不同，TreeMap的Entry其实是红-黑树的一个节点。下面看一下Entry<K, V>的定义:

```java
// Red-black mechanics
private static final boolean RED   = false; // 颜色红
private static final boolean BLACK = true; // 颜色黑

// 红黑树节点
static final class Entry<K,V> implements Map.Entry<K,V> {
    K key;
    V value;
    Entry<K,V> left; // 左子树
    Entry<K,V> right; // 右子树
    Entry<K,V> parent; // 父节点
    boolean color = BLACK; // 节点颜色默认是黑色

  	// 构造器
    Entry(K key, V value, Entry<K,V> parent) {
        this.key = key;
        this.value = value;
        this.parent = parent;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

  	// 更新value，返回旧值
    public V setValue(V value) {
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Map.Entry))
            return false;
        Map.Entry<?,?> e = (Map.Entry<?,?>)o;

        return valEquals(key,e.getKey()) && valEquals(value,e.getValue());
    }

    public int hashCode() {
        int keyHash = (key==null ? 0 : key.hashCode());
        int valueHash = (value==null ? 0 : value.hashCode());
        return keyHash ^ valueHash;
    }

    public String toString() {
        return key + "=" + value;
    }
}
```

## 四、TreeMap get、put、remove方法详解

### 1.get(Object key)

时间复杂度O(lgN)

```java
public V get(Object key) {
    Entry<K,V> p = getEntry(key); // 根据key获取Entry
    return (p==null ? null : p.value); //Entry为null，返回null；否则返回value
}
```

```java
final Entry<K,V> getEntry(Object key) {
    // Offload comparator-based version for sake of performance
    if (comparator != null)
        return getEntryUsingComparator(key); // 若使用比较器，则调用getEntryUsingComparator方法
    if (key == null)
        throw new NullPointerException();
    @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
    Entry<K,V> p = root;
    while (p != null) { // 迭代查找，直到找到entry或者返回null
        int cmp = k.compareTo(p.key);
        if (cmp < 0)
            p = p.left;
        else if (cmp > 0)
            p = p.right;
        else
            return p;
    }
    return null;
}
```

```java
// 使用comparator的getEntry版本
final Entry<K,V> getEntryUsingComparator(Object key) {
    @SuppressWarnings("unchecked")
        K k = (K) key;
    Comparator<? super K> cpr = comparator;
    if (cpr != null) { 
        Entry<K,V> p = root;
        while (p != null) { // 迭代查找，直到找到或者返回null
            int cmp = cpr.compare(k, p.key);
            if (cmp < 0)
                p = p.left;
            else if (cmp > 0)
                p = p.right;
            else
                return p;
        }
    }
    return null;
}
```

### 2.put(K key, V value)

put方法对应于红黑树中的节点插入操作，时间复杂度O(lgN)，源码如下：

```java
public V put(K key, V value) {
    Entry<K,V> t = root; // 根节点
    if (t == null) { // 根节点为null
        compare(key, key); // type (and possibly null) check 类型与null检查
				// 插入的是根节点，直接将根节点置为黑色
        root = new Entry<>(key, value, null); // 直接创建根节点
        size = 1;
        modCount++;
        return null;
    }
    int cmp;
    Entry<K,V> parent;
    // split comparator and comparable paths 根据comparator是否为null，以及key值，查找对应key的映射是否存在
    Comparator<? super K> cpr = comparator;
    if (cpr != null) {
        do { // 迭代查找
            parent = t;
            cmp = cpr.compare(key, t.key);
            if (cmp < 0)
                t = t.left;
            else if (cmp > 0)
                t = t.right;
            else
                return t.setValue(value); // 找到，更新值，并返回旧值
        } while (t != null);
    }
    else {
        if (key == null)
            throw new NullPointerException();
        @SuppressWarnings("unchecked")
            Comparable<? super K> k = (Comparable<? super K>) key;
        do { // 迭代查找
            parent = t;
            cmp = k.compareTo(t.key);
            if (cmp < 0)
                t = t.left;
            else if (cmp > 0)
                t = t.right;
            else
                return t.setValue(value); // 找到，更新值，并返回旧值
        } while (t != null);
    }
  	// 未找到对应key的映射
    Entry<K,V> e = new Entry<>(key, value, parent); // 新建entry，插入
    if (cmp < 0) // 挂到parent左侧或右侧
        parent.left = e;
    else
        parent.right = e;
    fixAfterInsertion(e); // 节点插入后的修复操作
    size++;
    modCount++;
    return null;
}
```

红黑树插入操作的修复操作如下，可参考[教你透彻了解红黑树之红黑树的插入和插入修复](https://xuanjian1992.top/2019/04/30/教你透彻了解红黑树/)：

case:

- 被插入节点的父节点是黑色的，则节点插入后置为红色即可；
- **①如果当前结点的父结点是红色且祖父结点的另一个子结点（叔叔结点）是红色；**
- **②当前结点的父结点是红色,叔叔结点是黑色，当前结点是其父结点的右子；**
- **③当前结点的父结点是红色,叔叔结点是黑色，当前结点是其父结点的左子。**

```java
/** From CLR, x为插入的节点 */ 
private void fixAfterInsertion(Entry<K,V> x) {
    x.color = RED; // 插入节点默认为红色
		// 修复操作退出条件为x=root
    while (x != null && x != root && x.parent.color == RED) { // 父节点是红色
      	// 父节点是祖父节点的左子节点
        if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
            Entry<K,V> y = rightOf(parentOf(parentOf(x))); // y叔叔节点
            if (colorOf(y) == RED) { // 叔叔节点是红色 ①
                setColor(parentOf(x), BLACK);
                setColor(y, BLACK);
                setColor(parentOf(parentOf(x)), RED);
                x = parentOf(parentOf(x)); // 祖父节点作为当前节点
            } else {
              	// 叔叔节点是黑色 
                if (x == rightOf(parentOf(x))) { // 当前结点是其父结点的右子 ②
                    x = parentOf(x); // 当前节点变为父节点
                    rotateLeft(x); // 左旋
                }
                setColor(parentOf(x), BLACK); // ③
                setColor(parentOf(parentOf(x)), RED);
                rotateRight(parentOf(parentOf(x))); // 右旋
            }
        } else {
	          // 父节点是祖父节点的右子节点，以下逻辑与上面对称， "left/right" 互换。
            Entry<K,V> y = leftOf(parentOf(parentOf(x)));
            if (colorOf(y) == RED) {
                setColor(parentOf(x), BLACK);
                setColor(y, BLACK);
                setColor(parentOf(parentOf(x)), RED);
                x = parentOf(parentOf(x));
            } else {
                if (x == leftOf(parentOf(x))) {
                    x = parentOf(x);
                    rotateRight(x);
                }
                setColor(parentOf(x), BLACK);
                setColor(parentOf(parentOf(x)), RED);
                rotateLeft(parentOf(parentOf(x)));
            }
        }
    }
    root.color = BLACK; // 最后将根节点置为黑色
}
```

相关工具方法：

```java
private static <K,V> boolean colorOf(Entry<K,V> p) { // 获取节点的颜色，null节点为黑色
    return (p == null ? BLACK : p.color);
}

private static <K,V> Entry<K,V> parentOf(Entry<K,V> p) { // 获取节点的父节点
    return (p == null ? null: p.parent);
}

private static <K,V> void setColor(Entry<K,V> p, boolean c) { // 设置节点的颜色
    if (p != null)
        p.color = c;
}

private static <K,V> Entry<K,V> leftOf(Entry<K,V> p) { // 获取节点的左子节点
    return (p == null) ? null: p.left;
}

private static <K,V> Entry<K,V> rightOf(Entry<K,V> p) { // 获取节点的右子节点
    return (p == null) ? null: p.right;
}

/** 左旋，参考https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84/rbtree/2.jpg */ 
private void rotateLeft(Entry<K,V> p) {
    if (p != null) {
        Entry<K,V> r = p.right;
        p.right = r.left;
        if (r.left != null)
            r.left.parent = p;
        r.parent = p.parent;
        if (p.parent == null)
            root = r;
        else if (p.parent.left == p)
            p.parent.left = r;
        else
            p.parent.right = r;
        r.left = p;
        p.parent = r;
    }
}

/** 右旋，都是指针操作，参考https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84/rbtree/3.jpg */
private void rotateRight(Entry<K,V> p) {
    if (p != null) {
        Entry<K,V> l = p.left;
        p.left = l.right;
        if (l.right != null) l.right.parent = p;
        l.parent = p.parent;
        if (p.parent == null)
            root = l;
        else if (p.parent.right == p)
            p.parent.right = l;
        else p.parent.left = l;
        l.right = p;
        p.parent = l;
    }
}
```

### 3.remove(Object key)

remove方法对应于红黑树中的节点删除操作，时间复杂度O(lgN)，源码如下：

```java
public V remove(Object key) { // 根据key删除红黑树节点
    Entry<K,V> p = getEntry(key);
    if (p == null) // 不存在映射
        return null;

    V oldValue = p.value;
    deleteEntry(p); // 删除节点p
    return oldValue;
}
```

```java
private void deleteEntry(Entry<K,V> p) { // 删除操作
    modCount++;
    size--;

    // If strictly internal, copy successor's element to p and then make p
    // point to successor.
  	// p的左右子节点都不为null的情况
    if (p.left != null && p.right != null) {
        Entry<K,V> s = successor(p); // 先找到后继节点
        p.key = s.key; // 将s的key/value复制到p
        p.value = s.value;
        p = s; // p重新指向s，从而删除s对应的entry
    } // p has 2 children

    // Start fixup at replacement node, if it exists.
  	// 找到替代节点，可能不存在。
    Entry<K,V> replacement = (p.left != null ? p.left : p.right);

    if (replacement != null) { // 替代节点存在
        // Link replacement to parent
        replacement.parent = p.parent; // 调整指针，替代节点连接p.parent
        if (p.parent == null)
            root = replacement;
        else if (p == p.parent.left)
            p.parent.left  = replacement;
        else
            p.parent.right = replacement;

        // Null out links so they are OK to use by fixAfterDeletion.
        p.left = p.right = p.parent = null;

        // Fix replacement
        if (p.color == BLACK) // 如果删除节点是黑色，则修复删除操作;若被删除的节点是红色的，无需修复
            fixAfterDeletion(replacement);
    } else if (p.parent == null) { // return if we are the only node.
        root = null; // 删除的p是唯一节点，则直接返回
    } else { //  No children. Use self as phantom replacement and unlink.
        if (p.color == BLACK) // 无孩子节点，将自己作为替代者，调用fixAfterDeletion
            fixAfterDeletion(p);
		
        if (p.parent != null) { // 取消p的连接
            if (p == p.parent.left)
                p.parent.left = null;
            else if (p == p.parent.right)
                p.parent.right = null;
            p.parent = null;
        }
    }
}
```

红黑树节点删除修复操作，参考[教你透彻了解红黑树之红黑树的删除和删除修复](https://xuanjian1992.top/2019/04/30/教你透彻了解红黑树):

case

- ①当前节点是黑+黑，且兄弟节点是红色；
- ②当前节点是黑+黑，且兄弟是黑色，兄弟的两个子节点是黑色；
- ③当前节点是黑+黑，且兄弟节点为黑色，且兄弟节点左子节点为红色，右子节点为黑色 ；
- ④当前节点是黑+黑，且兄弟是黑色，兄弟的右子是红色，兄弟左子颜色任意。

```java
/** From CLR，删除操作的修复，x是替代的节点 */
private void fixAfterDeletion(Entry<K,V> x) {
  	// 当前替代节点是root节点，直接置为黑色即可；当前替代节点是红色节点，直接置为黑色即可。
    while (x != root && colorOf(x) == BLACK) { // 当前(替代)节点不是root，且为黑色
			// 当前节点是父节点的左子节点
      if (x == leftOf(parentOf(x))) { 
            Entry<K,V> sib = rightOf(parentOf(x)); // 兄弟节点

            if (colorOf(sib) == RED) { // 兄弟节点是红色，则置为黑色 ①
                setColor(sib, BLACK);
                setColor(parentOf(x), RED);
                rotateLeft(parentOf(x));
                sib = rightOf(parentOf(x));
            }

            if (colorOf(leftOf(sib))  == BLACK &&
                colorOf(rightOf(sib)) == BLACK) { // 兄弟节点为黑色，且其子节点都为黑色 ②
                setColor(sib, RED);
                x = parentOf(x);
            } else {
                if (colorOf(rightOf(sib)) == BLACK) { // 兄弟节点为黑色，且其左子节点为红色，右子节点为黑色 ③
                    setColor(leftOf(sib), BLACK);
                    setColor(sib, RED);
                    rotateRight(sib);
                    sib = rightOf(parentOf(x));
                }
              	// 兄弟是黑色，兄弟的右子是红色，兄弟左子颜色任意。 ④
                setColor(sib, colorOf(parentOf(x))); 
                setColor(parentOf(x), BLACK);
                setColor(rightOf(sib), BLACK);
                rotateLeft(parentOf(x));
                x = root;
            }
        } else { // symmetric 当前节点是父节点的右子节点，逻辑与上述对称，左右互换
            Entry<K,V> sib = leftOf(parentOf(x));

            if (colorOf(sib) == RED) {
                setColor(sib, BLACK);
                setColor(parentOf(x), RED);
                rotateRight(parentOf(x));
                sib = leftOf(parentOf(x));
            }

            if (colorOf(rightOf(sib)) == BLACK &&
                colorOf(leftOf(sib)) == BLACK) {
                setColor(sib, RED);
                x = parentOf(x);
            } else {
                if (colorOf(leftOf(sib)) == BLACK) {
                    setColor(rightOf(sib), BLACK);
                    setColor(sib, RED);
                    rotateLeft(sib);
                    sib = leftOf(parentOf(x));
                }
                setColor(sib, colorOf(parentOf(x)));
                setColor(parentOf(x), BLACK);
                setColor(leftOf(sib), BLACK);
                rotateRight(parentOf(x));
                x = root;
            }
        }
    }

    setColor(x, BLACK); // 最后将root置为黑色
}
```

## 五、buildFromSorted方法

在第二部分构造器中，**public TreeMap(SortedMap<K, ? extends V> m)**构造方法使用了buildFromSorted方法构建红黑树结构(**这是一个高效的红黑树构建算法**)，该方法定义如下：

```java
// 从SortedMap(it)或ObjectInputStream构建红黑树，时间复杂度O(N)
// @param size 待构建的红黑树中节点个数
// @param it 迭代器，如果不为空，则从迭代器中读取元素
// @param str 对象输入流，如果不为空，则从流中读取键值对
// @param defaultVal value默认值
private void buildFromSorted(int size, Iterator<?> it, java.io.ObjectInputStream str,
                             V defaultVal)
    throws  java.io.IOException, ClassNotFoundException {
    this.size = size;
    root = buildFromSorted(0, 0, size-1, computeRedLevel(size), // 实际的构建方法，返回root
                           it, str, defaultVal);
}
```

实际构建红黑树还需要调用重载方法buildFromSorted，该方法是一个递归方法，会递归的构建出红黑树的左子树和右子树：

先看computeRedLevel方法：

```java
// 给定树中节点个数，计算红色节点高度
// @param sz 树中节点个数
private static int computeRedLevel(int sz) { 
    int level = 0;
    for (int m = sz - 1; m >= 0; m = m / 2 - 1)
        level++;
  	// 由于buildFromSorted方法构建出来的红黑树，最后一层(叶子节点)之上的部分为完全二叉树，节点颜色
    // 为黑色，只有最后一层(叶子节点)的节点，颜色为红色，这种红黑树满足红黑树的性质。level=lgN
    return level; 
}
```

```java
// 递归构建红黑树
// @param level 树的当前高度，初始为0(root节点高度为0),表示的是代码中middle节点的高度
// @param lo 当前子树中第一个元素的下标，初始为0
// @param hi 当前子树中最后一个元素的下标，初始为size-1
// @param redLevel 红色节点的高度，表示构建时节点到达什么高度，其颜色被设置为红色
// @param it 迭代器，如果不为空，则从迭代器中读取元素。it中的值是按照顺序排列的
// @param str 对象输入流，如果不为空，则从流中读取键值对。str中的值是按照顺序排列的
// @param defaultVal value默认值
// @return 最终返回root
private final Entry<K,V> buildFromSorted(int level, int lo, int hi,
                                         int redLevel,
                                         Iterator<?> it,
                                         java.io.ObjectInputStream str,
                                         V defaultVal)
    throws  java.io.IOException, ClassNotFoundException {
  	// it或者str中的值是按照顺序排列的
    // Strategy: The root is the middlemost element(root是最中间的元素). To get to it, we
    // have to first recursively construct the entire left subtree(需要先构建左子树),
    // so as to grab all of its elements. We can then proceed with right
    // subtree(再继续构建右子树).
    //
  	// lo hi两个变量只是在构建红黑树时起到辅助的边界判断作用，实际上是不起索引作用的。数据还是按照迭代器it或str的顺序读取的
    // The lo and hi arguments are the minimum and maximum
    // indices to pull out of the iterator or stream for current subtree.
    // They are not actually indexed, we just proceed sequentially,
    // ensuring that items are extracted in corresponding order.

    if (hi < lo) return null; // 当前范围的子树无元素了，直接返回null

    int mid = (lo + hi) >>> 1; // 当前范围的中间节点索引

    Entry<K,V> left  = null; // 左子树
    if (lo < mid)
      	// 递归构建左子树，level+1
        left = buildFromSorted(level+1, lo, mid - 1, redLevel,
                               it, str, defaultVal);

    // extract key and/or value from iterator or stream
    K key;
    V value;
    if (it != null) { // it不为null，则从迭代器获取key value
        if (defaultVal==null) {
            Map.Entry<?,?> entry = (Map.Entry<?,?>)it.next();
            key = (K)entry.getKey();
            value = (V)entry.getValue();
        } else {
            key = (K)it.next();
            value = defaultVal;
        }
    } else { // use stream 否则从流中获取key value
        key = (K) str.readObject();
        value = (defaultVal != null ? defaultVal : (V) str.readObject());
    }
		// 当前范围的中间节点
    Entry<K,V> middle =  new Entry<>(key, value, null);

    // color nodes in non-full bottommost level red
    if (level == redLevel) // 如果当前middle节点的level已经到达redLevel，则直接设为红色
        middle.color = RED;

    if (left != null) { // 连接左子树
        middle.left = left;
        left.parent = middle;
    }

    if (mid < hi) { // 递归构建右子树，并连接
        Entry<K,V> right = buildFromSorted(level+1, mid+1, hi, redLevel,
                                           it, str, defaultVal);
        middle.right = right;
        right.parent = middle;
    }

    return middle; // 返回当前范围的中间节点
}
```

buildFromSorted(int size, Iterator<?> it, java.io.ObjectInputStream str, V defaultVal)方法的原理分析结束，该方法也用在了putAll()方法中。

TreeMap其中一个构造函数如下：

```java
public TreeMap(Map<? extends K, ? extends V> m) {
    comparator = null;
    putAll(m);
}
```

该构造函数调用了putAll方法，putAll方法如下，可见在构建TreeMap时，也使用了buildFromSorted来构建红黑树：

```java
public void putAll(Map<? extends K, ? extends V> map) {
    int mapSize = map.size();
  	// 当前树是一颗空树，并且map是SortedMap实例，而且map不是空树
    if (size==0 && mapSize!=0 && map instanceof SortedMap) {
        Comparator<?> c = ((SortedMap<?,?>)map).comparator();
      	// 如果map中的比较器与当前比较器相等
        if (c == comparator || (c != null && c.equals(comparator))) {
            ++modCount;
            try {
              	// 从SortedMap构建红黑树，时间复杂度O(N)
                buildFromSorted(mapSize, map.entrySet().iterator(),
                                null, null);
            } catch (java.io.IOException cannotHappen) {
            } catch (ClassNotFoundException cannotHappen) {
            }
            return;
        }
    }
    super.putAll(map); // 否则调用java.util.AbstractMap.putAll方法，时间复杂度O(NlgN)
}
```

## 六、参考资料

- [JDK TreeMap doc](https://docs.oracle.com/javase/8/docs/api/java/util/TreeMap.html)
- [java集合框架10——TreeMap和源码分析（一）](https://blog.csdn.net/eson_15/article/details/51217741)
- [java集合框架11——TreeMap和源码分析（二）](https://blog.csdn.net/eson_15/article/details/51239885)
- [jdk源码分析（七）——TreeMap](https://www.jianshu.com/p/06175895c05f)
- [TreeMap中有序序列建红黑树--buildFromSorted](https://blog.csdn.net/jiang_bing/article/details/7537803)
- [教你透彻了解红黑树](https://xuanjian1992.top/2019/04/30/教你透彻了解红黑树/)

