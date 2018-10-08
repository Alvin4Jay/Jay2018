# `CopyOnWriteArrayList`源码解析 #

<br>
## 一、CopyOnWriteArrayList特点 ##

1. 它最适合于具有以下特征的应用程序：List 大小通常保持很小，只读操作远多于可变操作(需要拷贝数组)，需要在遍历期间防止线程间的冲突。
2. 它是**线程安全**的。
3. 因为通常需要复制整个基础数组，所以可变操作（add()、set() 和 remove() 等等）的**开销很大**。
4. 迭代器支持hasNext(), next()等不可变操作，但**不支持可变 remove()**等操作。
5. 使用迭代器进行遍历的速度很快，并且不会与其他线程发生冲突。在构造迭代器时，**迭代器依赖于不变的数组快照**。


## 二、原理 ##

1. **动态数组机制**

	它内部有个“volatile数组”(array)来保持数据。**在“添加/修改/删除”数据时，都会新建一个数组，并在新建的数组中进行操作，最后再将该数组赋值给“volatile数组”。**这就是它叫做CopyOnWriteArrayList的原因！CopyOnWriteArrayList就是通过这种方式实现的动态数组；不过正由于它在“添加/修改/删除”数据时，都会新建数组，所以涉及到修改数据的操作，CopyOnWriteArrayList效率很低；但是单单只是进行遍历查找的话，效率比较高。

2. **线程安全机制**

	是通过**volatile和互斥锁**来实现的。(01) CopyOnWriteArrayList是通过“volatile数组”来保存数据的。一个线程读取volatile数组时，总能看到其它线程对该volatile变量最后的写入；就这样，通过volatile提供了“**读取到的数据总是最新的**”这个机制的保证。(02) CopyOnWriteArrayList通过互斥锁来保护数据。在“添加/修改/删除”数据时，会先“获取互斥锁”，再修改完毕之后，先将数据更新到“volatile数组”中，然后再“释放互斥锁”；这样，就达到了**保护数据**的目的。 

## 三、部分源码解析 ##
	//volatile数组array
	private transient volatile Object[] array;
	//互斥锁
	final transient ReentrantLock lock = new ReentrantLock();
	//获取数组
	final Object[] getArray() {
        return array;
    }
	//设置数组
    final void setArray(Object[] a) {
        array = a;
    }

1. `add(E e)`操作

-----
	public boolean add(E e) {
		//获得互斥锁
	    final ReentrantLock lock = this.lock;
		//加锁
	    lock.lock();
	    try {
			//获取原始”volatile数组“中的数据和数据长度
	        Object[] elements = getArray();
	        int len = elements.length;
			//一、新建一个数组newElements，并将原始数据拷贝到newElements中；(满足happen-before原则)
			//newElements数组的长度=“原始数组的长度”+1
	        Object[] newElements = Arrays.copyOf(elements, len + 1);
			//二、将“新增加的元素”保存到newElements中
	        newElements[len] = e;
			//三、将newElements赋值给"volatile数组"
	        setArray(newElements);
	        return true;
	    } finally {
			//释放锁
	        lock.unlock();
	    }
	}

说明:add(E e)的作用就是将数据e添加到”volatile数组“中。它的实现方式是，新建一个数组，接着将原始的”volatile数组“的数据拷贝到新数组中，然后将新增数据也添加到新数组中；最后，将新数组赋值给"volatile数组"。注意:第一，在”添加操作“开始前，获取独占锁(lock)，若此时有需要线程要获取锁，则必须等待；在操作完毕后，释放独占锁(lock)，此时其它线程才能获取锁。通过独占锁，来防止多线程同时修改数据！第二，操作完毕时，会通过setArray()来更新”volatile数组“。而且，前面我们提过”即对一个volatile变量的读，总是能看到（任意线程）对这个volatile变量最后的写入“；这样，每次添加元素之后，其它线程都能看到新添加的元素。


2.`get(int index)`操作

	//get操作是对volatile数组进行读取操作，很容易
	public E get(int index) {
        return get(getArray(), index);
    }
	private E get(Object[] a, int index) {
        return (E) a[index];
    } 

3.`remove(int index)`删除操作

	public E remove(int index) {
		//获得锁，上锁
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
			//获取原始”volatile数组“中的数据和数据长度
            Object[] elements = getArray();
            int len = elements.length;
			//获取elements数组中的第index个数据
            E oldValue = get(elements, index);
			//删除之后，需要移动的元素个数
            int numMoved = len - index - 1;
			//删除的是最后一个元素，则直接通过Arrays.copyOf()进行处理，而不需要新建数组
            if (numMoved == 0)
                setArray(Arrays.copyOf(elements, len - 1));
            else {
			否则，新建数组，然后将”volatile数组中被删除元素之外的其它元素“拷贝到新数组中；最后，将新数组赋值给”volatile数组“
                Object[] newElements = new Object[len - 1];
                System.arraycopy(elements, 0, newElements, 0, index);
                System.arraycopy(elements, index + 1, newElements, index,
                                 numMoved);
                setArray(newElements);
            }
            return oldValue;
        } finally {
			//释放锁
            lock.unlock();
        }
    }

说明：remove(int index)的作用就是将”volatile数组“中第index个元素删除。它的实现方式是，如果被删除的是最后一个元素，则直接通过Arrays.copyOf()进行处理，而不需要新建数组。否则，新建数组，然后将”volatile数组中被删除元素之外的其它元素“拷贝到新数组中；最后，将新数组赋值给”volatile数组“。 和add(E e)一样，remove(int index)也是”在操作之前，获取独占锁；操作完成之后，释放独占是“；并且”在操作完成时，会通过将数据更新到volatile数组中“。

4.遍历

	public Iterator<E> iterator() {
        return new COWIterator<E>(getArray(), 0);
    }
	static final class COWIterator<E> implements ListIterator<E> {
        /** 遍历的是volatile数组的一个快照，不是volatile数组本身 */
        private final Object[] snapshot;
        /** 遍历的指针  */
        private int cursor;

        private COWIterator(Object[] elements, int initialCursor) {
            cursor = initialCursor;
            snapshot = elements;
        }
		//是否有下一个元素，通过指针判断是否小于length
        public boolean hasNext() {
            return cursor < snapshot.length;
        }
		//是否有前面一个元素 cursor > 0
        public boolean hasPrevious() {
            return cursor > 0;
        }

        //返回下一个元素，最后指针+1
        public E next() {
            if (! hasNext())
                throw new NoSuchElementException();
            return (E) snapshot[cursor++];
        }

        //返回前面一个元素
        public E previous() {
            if (! hasPrevious())
                throw new NoSuchElementException();
            return (E) snapshot[--cursor];
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor-1;
        }

        //不支持
        public void remove() {
            throw new UnsupportedOperationException();
        }

        //不支持
        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        //不支持
        public void add(E e) {
            throw new UnsupportedOperationException();
        }
    }

说明：COWIterator不支持修改元素的操作。例如，对于remove(),set(),add()等操作，COWIterator都会抛出异常！另外，需要提到的一点是，CopyOnWriteArrayList返回迭代器不会抛出ConcurrentModificationException异常，即它不是fail-fast机制的！

<font color="red" size="5px">
	注意(为什么add的时候还要创建一个新数组，不能直接操作volatile数组吗？？)：
	因为读的操作是没有同步处理直接读取的原volatile数组的，而写操作会添加锁操作，但是因为修改操作都是在新的数组中进行，所以保证其他线程能正常进行读操作而不被阻塞；当新的数组修改完毕拷贝给原volatile数组时，根据volatile的内存语意，此时再去读就是新的数据。如果按照在原来volatile的数组中直接操作，那么写操作会阻塞读操作(happen-before原则)，就退化成Vector了，效率低。总结就是适合多读少写的操作，并且写操作不会影响读操作。
</font>