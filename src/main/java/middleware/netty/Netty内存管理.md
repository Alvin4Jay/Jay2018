# Netty内存管理

在Netty中，ByteBuf随处可见，作为读写数据的字节容器，ByteBuf的内存分配十分复杂且值得仔细研究。本文从ByteBuf和ByteBufAllocator两大抽象开始，详细分析Netty内存分配与回收的细节。

本文内容：

- 内存与内存分配器的抽象
- 不同规格大小和不同类别的内存的分配策略
- 内存的回收过程

## 一、ByteBuf

### 1.结构

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-1.jpg)

上图显示了ByteBuf的结构，主要由废弃字节、可读字节、可写字节三部分组成，使用`readerIndex`与`writerIndex`分隔，三部分加起来称为容量capacity。`readerIndex`表示可读字节的起始位置，`writerIndex`表示可写字节的起始位置。

### 2.重要API

```java
public abstract byte  readByte(); // 读取一个字节，readerIndex右移一位

public abstract ByteBuf writeByte(int value);// 写出一个字节，writerIndex右移一位

public abstract ByteBuf setByte(int index, int value);// 设置数据，readerIndex、writerIndex不变

public abstract ByteBuf markReaderIndex(); // 标记读写指针，可以重置
public abstract ByteBuf resetReaderIndex();
public abstract ByteBuf markWriterIndex();
public abstract ByteBuf resetWriterIndex();
```

API详细介绍见闪电侠文章[数据传输载体 ByteBuf 介绍](https://juejin.im/book/5b4bc28bf265da0f60130116/section/5b4db03b6fb9a04fe91a6e93)

### 3.分类

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-2.png)

从上图ByteBuf的类继承体系可以看出，ByteBuf可以从三个角度进行分类:

- Heap和Direct

  表示ByteBuf底层使用的是堆内存字节数组，或者堆外内存DirectByteBuffer。

- Pooled和Unpooled

  Pooled表示池化的内存，即分配内存时可以从预先分配好的内存中取出一块内存；Unpooled表示非池化的内存，即直接调用API向操作系统申请一块内存。

- Unsafe和非Unsafe 

  表示ByteBuf读写数据时是否依赖JDK Unsafe对象，Unsafe表示依赖。

一般情况下，用户在分配ByteBuf内存时只需根据Heap和Direct、Pooled和Unpooled两个角度进行内存分配，Unsafe和非Unsafe是由Netty根据运行环境自动识别的。

### 4.抽象实现AbstractByteBuf

AbstractByteBuf是ByteBuf的抽象实现，实现了ByteBuf公共的方法，如readableBytes()、writableBytes()等。

```java
public int readableBytes() {
    return writerIndex - readerIndex;
}
public int writableBytes() {
    return capacity() - writerIndex;
}
```

对于读写数据的方法，如readByte()、writeByte()，实现了基本逻辑，并提供抽象方法由不同子类去具体实现:

```java
public byte readByte() {
    checkReadableBytes0(1);
    int i = readerIndex;
    byte b = _getByte(i);
    readerIndex = i + 1;
    return b;
}
public ByteBuf writeByte(int value) {
    ensureAccessible();
    ensureWritable0(1); // 确保写空间足够，必要时扩容
    _setByte(writerIndex++, value);
    return this;
}
```

对于readByte()、writeByte()方法来说，提供了`_getByte()、_setByte()`两个抽象方法:

```java
protected abstract byte _getByte(int index);
protected abstract void _setByte(int index, int value);
```

其他的读写数据方法类似的，也提供了抽象方法让子类去实现:

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-4.jpg?x-oss-process=style/markdown-pic)

## 二、ByteBufAllocator

### 1.功能

ByteBufAllocator作为内存分配器的抽象，具有如下的功能:

```java
// 1.分配ByteBuf，是否是堆外内存依赖于具体的实现
ByteBuf buffer(int initialCapacity, int maxCapacity);
// 2.分配适合于IO的ByteBuf，期待是堆外内存
ByteBuf ioBuffer(int initialCapacity, int maxCapacity);
// 3.分配堆内存ByteBuf
ByteBuf heapBuffer(int initialCapacity, int maxCapacity);
// 4.分配堆外内存DirectByteBuffer
ByteBuf directBuffer(int initialCapacity, int maxCapacity);
```

### 2.抽象实现AbstractByteBufAllocator

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-3.jpg)

```java
public ByteBuf directBuffer(int initialCapacity, int maxCapacity) {
    if (initialCapacity == 0 && maxCapacity == 0) {
        return emptyBuf; // 返回空ByteBuf
    }
    validate(initialCapacity, maxCapacity); // 校验参数有效性
    return newDirectBuffer(initialCapacity, maxCapacity);
}

public ByteBuf heapBuffer(int initialCapacity, int maxCapacity) {
    if (initialCapacity == 0 && maxCapacity == 0) {
        return emptyBuf;
    }
    validate(initialCapacity, maxCapacity);
    return newHeapBuffer(initialCapacity, maxCapacity);
}

protected abstract ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity);
protected abstract ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity);
```

AbstractByteBufAllocator实际上是ByteBufAllocator的骨架实现，暴露了newHeapBuffer()、newDirectBuffer()两个抽象方法给具体实现子类UnpooledByteBufAllocator、PooledByteBufAllocator，用于这两个子类实现具体的分配动作。

## 三、UnpooledByteBufAllocator内存分配

### 1.Heap内存的分配

直接看UnpooledByteBufAllocator的newHeapBuffer()方法:

```java
protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
    return PlatformDependent.hasUnsafe() ? new UnpooledUnsafeHeapByteBuf(this, initialCapacity, maxCapacity)
            : new UnpooledHeapByteBuf(this, initialCapacity, maxCapacity);
}
```

newHeapBuffer()方法根据PlatformDependent.hasUnsafe()结果分别创建了UnpooledHeapByteBuf和UnpooledUnsafeHeapByteBuf。

> UnpooledHeapByteBuf

```java
protected UnpooledHeapByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
    this(alloc, new byte[initialCapacity], 0, 0, maxCapacity);
}
private UnpooledHeapByteBuf(
        ByteBufAllocator alloc, byte[] initialArray, int readerIndex, int writerIndex, int maxCapacity) {

    super(maxCapacity);

    this.alloc = alloc;
    setArray(initialArray);
    setIndex(readerIndex, writerIndex); // 设置readerIndex，writerIndex
}

private void setArray(byte[] initialArray) {
    array = initialArray;
    tmpNioBuf = null;
}
public ByteBuf setIndex(int readerIndex, int writerIndex) {
    setIndex0(readerIndex, writerIndex);
    return this;
}
```

可见创建UnpooledHeapByteBuf并初始化时，直接new了一个字节数组并传入初始化大小(申请堆内存)，然后保存字节数组引用、设置读写指针。

下面看下UnpooledHeapByteBuf读写数据的方法实现:

- 读数据

```java
// 读取数据
public byte getByte(int index) {
    ensureAccessible();
    return _getByte(index);
}
protected byte _getByte(int index) {
    return HeapByteBufUtil.getByte(array, index);
}
static byte getByte(byte[] memory, int index) {
    return memory[index]; // 数组索引
}
```

从代码中可以看出，UnpooledHeapByteBuf读取字节数据时，是直接从字节数组中取出对应索引位置的数据。

- 写数据

```java
public ByteBuf setByte(int index, int value) {
    ensureAccessible();
    _setByte(index, value);
    return this;
}
protected void _setByte(int index, int value) {
    HeapByteBufUtil.setByte(array, index, value);
}
static void setByte(byte[] memory, int index, int value) {
    memory[index] = (byte) value; // 高24位忽略
}
```

从代码中可以看出，UnpooledHeapByteBuf写字节数据时，是直接将字节数组中对应索引位置的数据置为传入的值。

> UnpooledUnsafeHeapByteBuf

```java
UnpooledUnsafeHeapByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
    super(alloc, initialCapacity, maxCapacity);
}
```

UnpooledUnsafeHeapByteBuf继承UnpooledHeapByteBuf类，在初始化时直接调用了父类UnpooledHeapByteBuf的构造器，同样通过new字节数组的方式申请堆内存。

下面看下UnpooledUnsafeHeapByteBuf读写数据的方法实现:

- 读数据

```java
public byte getByte(int index) {
    checkIndex(index);
    return _getByte(index);
}
protected byte _getByte(int index) {
    return UnsafeByteBufUtil.getByte(array, index);
}
static byte getByte(byte[] array, int index) {
    return PlatformDependent.getByte(array, index);
}
public static byte getByte(byte[] data, int index) {
    return PlatformDependent0.getByte(data, index);
}
static byte getByte(byte[] data, int index) {
    return UNSAFE.getByte(data, BYTE_ARRAY_BASE_OFFSET + index);
}
```

可以看出，UnpooledUnsafeHeapByteBuf在读取字节数据时，是使用了JDK Unsafe对象，直接通过内存地址读取字节数组数据。

- 写数据

```java
public ByteBuf setByte(int index, int value) {
    checkIndex(index);
    _setByte(index, value);
    return this;
}
protected void _setByte(int index, int value) {
    UnsafeByteBufUtil.setByte(array, index, value);
}
static void setByte(byte[] array, int index, int value) {
    PlatformDependent.putByte(array, index, (byte) value);
}
public static void putByte(byte[] data, int index, byte value) {
    PlatformDependent0.putByte(data, index, value);
}
static void putByte(byte[] data, int index, byte value) {
    UNSAFE.putByte(data, BYTE_ARRAY_BASE_OFFSET + index, value);
}
```

可以看出，UnpooledUnsafeHeapByteBuf在写字节数据时，是使用了JDK Unsafe对象，直接通过内存地址写字节数据，相对于UnpooledHeapByteBuf通过数组索引的方式写数据的性能更好。

### 2.direct堆外内存的分配

直接看UnpooledByteBufAllocator的newDirectBuffer()方法:

```java
protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity) {
    ByteBuf buf = PlatformDependent.hasUnsafe() ?
            UnsafeByteBufUtil.newUnsafeDirectByteBuf(this, initialCapacity, maxCapacity) :
            new UnpooledDirectByteBuf(this, initialCapacity, maxCapacity);

    return disableLeakDetector ? buf : toLeakAwareBuffer(buf);
}
```

newDirectBuffer()方法同样根据PlatformDependent.hasUnsafe()结果分别创建了UnpooledDirectByteBuf和UnpooledUnsafeDirectByteBuf。

> UnpooledDirectByteBuf

```java
protected UnpooledDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
    super(maxCapacity);

    this.alloc = alloc;
    setByteBuffer(ByteBuffer.allocateDirect(initialCapacity));
}
private void setByteBuffer(ByteBuffer buffer) {
    ByteBuffer oldBuffer = this.buffer;
    if (oldBuffer != null) {
        if (doNotFree) {
            doNotFree = false;
        } else {
            freeDirect(oldBuffer);
        }
    }

    this.buffer = buffer;
    tmpNioBuf = null;
    capacity = buffer.remaining();
}
```

可见创建UnpooledDirectByteBuf并初始化时，通过ByteBuffer API申请了DirectByteBuffer堆外内存，然后保存DirectByteBuffer引用。

下面看下UnpooledDirectByteBuf读写数据的方法实现:

- 读数据

```java
public byte getByte(int index) {
    ensureAccessible();
    return _getByte(index);
}
protected byte _getByte(int index) {
    return buffer.get(index);
}
```

可以看出，UnpooledDirectByteBuf读取字节数据时，是直接通过DirectByteBuffer API获取字节数据。

- 写数据

```java
public ByteBuf setByte(int index, int value) {
    ensureAccessible();
    _setByte(index, value);
    return this;
}
protected void _setByte(int index, int value) {
    buffer.put(index, (byte) value);
}
```

可以看出，UnpooledDirectByteBuf写字节数据时，也是通过DirectByteBuffer API写出字节数据。

> UnpooledUnsafeDirectByteBuf

```java
protected UnpooledUnsafeDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
    super(maxCapacity);
    this.alloc = alloc;
    setByteBuffer(allocateDirect(initialCapacity), false);
}
protected ByteBuffer allocateDirect(int initialCapacity) {
    return ByteBuffer.allocateDirect(initialCapacity);
}
final void setByteBuffer(ByteBuffer buffer, boolean tryFree) {
    if (tryFree) {
        ByteBuffer oldBuffer = this.buffer;
        if (oldBuffer != null) {
            if (doNotFree) {
                doNotFree = false;
            } else {
                freeDirect(oldBuffer);
            }
        }
    }
    this.buffer = buffer;
    // 获取DirectByteBuffer address变量的内存地址
    memoryAddress = PlatformDependent.directBufferAddress(buffer); 
    tmpNioBuf = null;
    capacity = buffer.remaining();
}
```

可见创建UnpooledUnsafeDirectByteBuf并初始化时，也是通过ByteBuffer API申请了DirectByteBuffer堆外内存，然后保存DirectByteBuffer引用。

需要注意的是，UnpooledUnsafeDirectByteBuf在申请到DirectByteBuffer时，会通过`PlatformDependent.directBufferAddress(buffer);`代码计算DirectByteBuffer内存块起始地址，即`memoryAddress`，后续读写数据的时候会用到。

```java
memoryAddress = PlatformDependent.directBufferAddress(buffer);

public static long directBufferAddress(ByteBuffer buffer) {
    return PlatformDependent0.directBufferAddress(buffer);
}
static long directBufferAddress(ByteBuffer buffer) {
  	// ADDRESS_FIELD_OFFSET: long address字段内存偏移
    return getLong(buffer, ADDRESS_FIELD_OFFSET);
}
private static long getLong(Object object, long fieldOffset) {
    return UNSAFE.getLong(object, fieldOffset);
}
```

下面看下UnpooledUnsafeDirectByteBuf读写数据的方法实现:

- 读数据

```java
protected byte _getByte(int index) {
    return UnsafeByteBufUtil.getByte(addr(index));
}
long addr(int index) {
    return memoryAddress + index; // address内存地址+字节index
}
static byte getByte(long address) {
    return PlatformDependent.getByte(address);
}
public static byte getByte(long address) {
    return PlatformDependent0.getByte(address);
}
static byte getByte(long address) {
    return UNSAFE.getByte(address);
}
```

读取字节数据时，首先调用`addr(index)`获取实际的内存偏移，即`memoryAddress + index`，然后通过JDK Unsafe对象，通过内存地址获取字节数据，相对于UnpooledDirectByteBuf通过DirectByteBuffer API获取字节数据的方式性能更好。

- 写数据

```java
protected void _setByte(int index, int value) {
    UnsafeByteBufUtil.setByte(addr(index), value);
}
static void setByte(long address, int value) {
    PlatformDependent.putByte(address, (byte) value);
}
public static void putByte(long address, byte value) {
    PlatformDependent0.putByte(address, value);
}
static void putByte(long address, byte value) {
    UNSAFE.putByte(address, value);
}
```

可以看到，写数据时同样先获取index索引实际的内存地址，然后通过JDK Unsafe对象，根据内存地址直接完成数据设置。

## 四、PooledByteBufAllocator内存分配

下面以堆外内存的分配为例，分析PooledByteBufAllocator的内存分配流程。

```java
protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity) {
    PoolThreadCache cache = threadCache.get();
    PoolArena<ByteBuffer> directArena = cache.directArena;

    ByteBuf buf;
    if (directArena != null) {
        buf = directArena.allocate(cache, initialCapacity, maxCapacity); // 分配池化的堆外内存
    } else {
        if (PlatformDependent.hasUnsafe()) {
            buf = UnsafeByteBufUtil.newUnsafeDirectByteBuf(this, initialCapacity, maxCapacity);
        } else {
            buf = new UnpooledDirectByteBuf(this, initialCapacity, maxCapacity);
        }
    }

    return toLeakAwareBuffer(buf);
}
```

首先通过`threadCache(PoolThreadLocalCache)`拿到PoolThreadCache对象。下面看PoolThreadLocalCache的实现:

```java
// PooledByteBufAllocator字段
private final PoolArena<byte[]>[] heapArenas;
private final PoolArena<ByteBuffer>[] directArenas;
private final int tinyCacheSize;
private final int smallCacheSize;
private final int normalCacheSize;

final class PoolThreadLocalCache extends FastThreadLocal<PoolThreadCache> {

    @Override
    protected synchronized PoolThreadCache initialValue() {
        final PoolArena<byte[]> heapArena = leastUsedArena(heapArenas); // 选择被NIO线程使用最少的PoolArena
        final PoolArena<ByteBuffer> directArena = leastUsedArena(directArenas);

        return new PoolThreadCache(
                heapArena, directArena, tinyCacheSize, smallCacheSize, normalCacheSize,
                DEFAULT_MAX_CACHED_BUFFER_CAPACITY, DEFAULT_CACHE_TRIM_INTERVAL);
    }

    @Override
    protected void onRemoval(PoolThreadCache threadCache) {
        threadCache.free();
    }

    private <T> PoolArena<T> leastUsedArena(PoolArena<T>[] arenas) {
        if (arenas == null || arenas.length == 0) {
            return null;
        }

        PoolArena<T> minArena = arenas[0];
        for (int i = 1; i < arenas.length; i++) {
            PoolArena<T> arena = arenas[i];
            if (arena.numThreadCaches.get() < minArena.numThreadCaches.get()) {
                minArena = arena;
            }
        }

        return minArena;
    }
}
```

可知PoolThreadLocalCache为FastThreadLocal实现，因此调用`threadCache.get()`获取PoolThreadCache时，不同NIO线程拿到的是各自的PoolThreadCache，这样使得多线程内存分配减少了竞争。

然后调用`PoolThreadCache.directArena;`获取堆外内存相关的PoolArena，再调用`PoolArena.allocate()`方法进行内存分配。从PoolThreadLocalCache的initialValue()方法获取初始化的PoolThreadCache可以看出，PoolArena实际上由PooledByteBufAllocator进行管理，因此NIO线程最终拿到的也是PooledByteBufAllocator管理的PoolArena。

### 1.PoolThreadCache结构

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-5.jpg)

上图为PoolThreadCache的主要结构，PoolThreadCache主要由PoolArena和MemoryRegionCache两部分组成，PoolArena表示分配内存时需要进行实际分配的内存，MemoryRegionCache表示已缓存的内存，可以直接拿来用。

### 2.分配堆外内存的流程

```java
PooledByteBuf<T> allocate(PoolThreadCache cache, int reqCapacity, int maxCapacity) {
    PooledByteBuf<T> buf = newByteBuf(maxCapacity); // 从Recycler对象池中获取PooledByteBuf
    allocate(cache, buf, reqCapacity); // 分配内存
    return buf;
}
```

> newByteBuf(maxCapacity)

```java
protected PooledByteBuf<ByteBuffer> newByteBuf(int maxCapacity) {
    if (HAS_UNSAFE) {
        return PooledUnsafeDirectByteBuf.newInstance(maxCapacity); // 这里！！
    } else {
        return PooledDirectByteBuf.newInstance(maxCapacity);
    }
}
// 对象池
private static final Recycler<PooledUnsafeDirectByteBuf> RECYCLER = new Recycler<PooledUnsafeDirectByteBuf>() {
    @Override
    protected PooledUnsafeDirectByteBuf newObject(Handle<PooledUnsafeDirectByteBuf> handle) {
        return new PooledUnsafeDirectByteBuf(handle, 0); // 新建PooledUnsafeDirectByteBuf对象
    }
};

static PooledUnsafeDirectByteBuf newInstance(int maxCapacity) {
    PooledUnsafeDirectByteBuf buf = RECYCLER.get();
    buf.reuse(maxCapacity); // 重用PooledUnsafeDirectByteBuf之前，初始化设置maxCapacity等参数
    return buf;
}
```

首先获取PooledUnsafeDirectByteBuf实例。可知获取PooledUnsafeDirectByteBuf时，是通过Recycler对象池进行获取的。如果对象池中PooledUnsafeDirectByteBuf可以直接拿来复用，则直接返回。否则Recycler对象池需要调用newObject()新建一个PooledUnsafeDirectByteBuf实例返回。使用对象池技术，是为了减少对象的创建、减少GC。

> allocate(cache, buf, reqCapacity)

```java
private void allocate(PoolThreadCache cache, PooledByteBuf<T> buf, final int reqCapacity) {
    final int normCapacity = normalizeCapacity(reqCapacity);
    if (isTinyOrSmall(normCapacity)) { // normCapacity < pageSize 8kB
        int tableIdx;
        PoolSubpage<T>[] table;
        boolean tiny = isTiny(normCapacity); // 是否小于512B
        if (tiny) { // < 512B
            // 1.首先在已存在的缓存buffer(MemoryRegionCache)中分配
            if (cache.allocateTiny(this, buf, reqCapacity, normCapacity)) {
                // was able to allocate out of the cache so move on 能够从缓存中进行分配
                return;
            }
            tableIdx = tinyIdx(normCapacity);
            table = tinySubpagePools;
        } else {
            // 1.首先在已存在的缓存buffer(MemoryRegionCache)中分配
            if (cache.allocateSmall(this, buf, reqCapacity, normCapacity)) {
                // was able to allocate out of the cache so move on
                return;
            }
            tableIdx = smallIdx(normCapacity);
            table = smallSubpagePools;
        }

        // 2.否则尝试在内存堆subpage上分配, head表示头部
        final PoolSubpage<T> head = table[tableIdx];

        // Synchronize on the head. This is needed as {@link PoolChunk#allocateSubpage(int)}
      	// and {@link PoolChunk#free(long)} may modify the doubly linked list as well.
        synchronized (head) {
            final PoolSubpage<T> s = head.next;
            if (s != head) { // 指向自己，表示subpage链表为空
                assert s.doNotDestroy && s.elemSize == normCapacity; // 断言大小匹配
                long handle = s.allocate(); // 分配内存，返回handle: bitmap索引 + memoryMapIdx
                assert handle >= 0;
                s.chunk.initBufWithSubpage(buf, handle, reqCapacity); // ByteBuf初始化

                if (tiny) { // 统计
                    allocationsTiny.increment();
                } else {
                    allocationsSmall.increment();
                }
                return;
            }
        }
        allocateNormal(buf, reqCapacity, normCapacity); // subpage级别的内存分配
        return;
    }
    if (normCapacity <= chunkSize) { // <=16MB
        // 1.首先在已存在的缓存buffer中分配
        if (cache.allocateNormal(this, buf, reqCapacity, normCapacity)) {
            // was able to allocate out of the cache so move on
            return;
        }
        // 2.否则在内存堆上分配
        allocateNormal(buf, reqCapacity, normCapacity); // page级别的内存分配
    } else { // >16MB
        // Huge allocations are never served via the cache so just call allocateHuge
        // 在内存堆上分配
        allocateHuge(buf, reqCapacity);
    }
}
```

以上代码便是在PoolThreadCache上进行内存分配的主要流程。

对于申请的`reqCapacity`内存大小，首先进行规格化:

```java
final int normCapacity = normalizeCapacity(reqCapacity);
// 内存大小规格化
int normalizeCapacity(int reqCapacity) {
    if (reqCapacity < 0) {
        throw new IllegalArgumentException("capacity: " + reqCapacity + " (expected: 0+)");
    }
    if (reqCapacity >= chunkSize) { // >16MB，表示为huge，不作处理，直接返回
        return reqCapacity;
    }

    if (!isTiny(reqCapacity)) { // >= 512，small/normal
        // Doubled

        int normalizedCapacity = reqCapacity;
        normalizedCapacity --;
        normalizedCapacity |= normalizedCapacity >>>  1;
        normalizedCapacity |= normalizedCapacity >>>  2;
        normalizedCapacity |= normalizedCapacity >>>  4;
        normalizedCapacity |= normalizedCapacity >>>  8;
        normalizedCapacity |= normalizedCapacity >>> 16;
        normalizedCapacity ++;

        // 到这里normalizedCapacity已经为大于等于reqCapacity的2的幂次

        if (normalizedCapacity < 0) {
            normalizedCapacity >>>= 1; // 无符号右移1位
        }

        return normalizedCapacity;
    }

    // tiny
    // Quantum-spaced
    if ((reqCapacity & 15) == 0) { // reqCapacity低4位为0，直接返回，因为已经为16的倍数。
        return reqCapacity;
    }

    return (reqCapacity & ~15) + 16; // reqCapacity去除低4位，加16字节，确保返回值是16字节的倍数
}
```

从allocate()方法和normalizeCapacity()方法的逻辑可以看出，请求分配的内存分为tiny、small、normal、huge四种：tiny：0-512B；small：512B-8K；normal：8k-16M；huge：>=16MB。经过normalizeCapacity()方法规格化请求分配的内存后，得到`normCapacity`：tiny：16B的整数倍，小于512B；small：512B、1K、2K、4K；normal：8K、16K...到16MB；huge：>=16MB。

其次根据规格化后的内存大小(tiny、small、normal、huge四种)，完成内存分配。

- tiny、small

  首先在已存在的缓存MemoryRegionCache中分配，否则尝试在内存堆subpage上分配，如果subpage链表为空，则调用allocateNormal()方法完成实际的内存分配。

- normal

  首先在已存在的缓存MemoryRegionCache中分配，否则调用allocateNormal()方法完成实际的内存分配。

- huge

  调用allocateHuge()方法在内存堆上进行实际的内存分配。

### 3.内存规格大小

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-6.jpg)

从上面的分析可以看出，经过规格化之后的内存大小可以分为tiny、small、normal、huge四种：tiny：0-512B；small：512B-8K；normal：8k-16M；huge：>=16MB。

Netty在首次分配内存时，先申请PoolChunk，大小为16MB的内存。PoolChunK细分为2048个page，每个page大小为8kB。如果申请的内存小于8kB，则page将细分为PoolSubpage，如上图所示。

下面将按照上面所述的流程，分①命中缓存MemoryRegionCache的内存分配、②page级别的内存分配、③subPage级别的内存分配、④PoolByteBuf的回收这四部分内容详细分析PooledByteBufAllocator堆外内存的分配与回收。

## 五、命中缓存MemoryRegionCache的内存分配

### 1.MemoryRegionCache缓存结构

```java
// PoolThreadCache
private final MemoryRegionCache<ByteBuffer>[] tinySubPageDirectCaches; // 32
private final MemoryRegionCache<ByteBuffer>[] smallSubPageDirectCaches; // 4
private final MemoryRegionCache<ByteBuffer>[] normalDirectCaches; // 3

private abstract static class MemoryRegionCache<T> {
    private final int size;
    private final Queue<Entry<T>> queue;
    private final SizeClass sizeClass;
    private int allocations;

    MemoryRegionCache(int size, SizeClass sizeClass) {
        this.size = MathUtil.safeFindNextPositivePowerOfTwo(size); // 找到下一个大于等于size的power of 2
        queue = PlatformDependent.newFixedMpscQueue(this.size); // 队列
        this.sizeClass = sizeClass;
    }
    // ...
}
```

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-7.jpg)

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-8.jpg)

MemoryRegionCache表示缓存的内存，即当ByteBuf调用release()方法回收内存时，会根据内存的大小放到对应的MemoryRegionCache中，以便下次申请相同大小的内存时可以快速分配。

MemoryRegionCache分为三种类型：tinySubPageDirectCaches、smallSubPageDirectCaches和normalDirectCaches，数组大小分别为32(第0个位置不使用)、4、3。从上面两幅图可以看出，tinySubPageDirectCaches对应tiny的内存缓存，大小范围为16B到496B，共31种；smallSubPageDirectCaches对应small的内存缓存，大小范围为512B、1k、2k、4k，共4种；normalDirectCaches对应normal的内存缓存，大小范围为8k、16k、32k，共3种。

每个MemoryRegionCache中有一个队列`Queue<Entry<T>> queue`，以及字段`SizeClass sizeClass`标识缓存的内存类型，如tiny、small、normal。队列用于缓存内存的地址，用Entry标识:

```java
static final class Entry<T> {
    final Handle<Entry<?>> recyclerHandle;
    PoolChunk<T> chunk;
    long handle = -1; // 指向chunk的唯一一段连续内存

    Entry(Handle<Entry<?>> recyclerHandle) {
        this.recyclerHandle = recyclerHandle;
    }

    void recycle() {
        chunk = null;
        handle = -1;
        recyclerHandle.recycle(this);
    }
}
```

Entry中chunk标识缓存的内存来自于哪个chunk，handle指向chunk的唯一一段连续内存，这样就能确定唯一一块内存。

### 2.命中缓存的内存分配流程

```java
private void allocate(PoolThreadCache cache, PooledByteBuf<T> buf, final int reqCapacity) {
    final int normCapacity = normalizeCapacity(reqCapacity);
    if (isTinyOrSmall(normCapacity)) { // normCapacity < pageSize 8kB
        int tableIdx;
        PoolSubpage<T>[] table;
        boolean tiny = isTiny(normCapacity); // 是否小于512B
        if (tiny) { // < 512B
            // 1.首先在已存在的缓存buffer(MemoryRegionCache)中分配
            if (cache.allocateTiny(this, buf, reqCapacity, normCapacity)) {
                // was able to allocate out of the cache so move on 能够从缓存中进行分配
                return;
            }
            tableIdx = tinyIdx(normCapacity);
            table = tinySubpagePools;
        } else {
            // ...
        }
    }  
		//...
}
// 从MemoryRegionCache缓存中进行分配内存
cache.allocateTiny(this, buf, reqCapacity, normCapacity)
```

下面以cache.allocateTiny()方法为例，分析命中缓存的内存分配流程。

```java
boolean allocateTiny(PoolArena<?> area, PooledByteBuf<?> buf, int reqCapacity, int normCapacity) {
    return allocate(cacheForTiny(area, normCapacity), buf, reqCapacity);
}
```

> 找到对应size的MemoryRegionCache

cacheForTiny(area, normCapacity)：

```java
private MemoryRegionCache<?> cacheForTiny(PoolArena<?> area, int normCapacity) {
    int idx = PoolArena.tinyIdx(normCapacity); // 获取tinySubPageDirectCaches数组索引
    if (area.isDirect()) {
        return cache(tinySubPageDirectCaches, idx);
    }
    return cache(tinySubPageHeapCaches, idx);
}
```

首先调用`PoolArena.tinyIdx(normCapacity)`，根据normCapacity获取tinySubPageDirectCaches中对应MemoryRegionCache的索引，然后再调用`cache(tinySubPageDirectCaches, idx)`获取对应normCapacity的MemoryRegionCache:

```java
private static <T> MemoryRegionCache<T> cache(MemoryRegionCache<T>[] cache, int idx) {
    if (cache == null || idx > cache.length - 1) { // 超出数组索引，返回null
        return null;
    }
    return cache[idx];
}
```

> 从MemoryRegionCache的queue中弹出一个Entry给PooledByteBuf初始化(PoolChunk、handle)

```java
private boolean allocate(MemoryRegionCache<?> cache, PooledByteBuf buf, int reqCapacity) {
    if (cache == null) {
        // no cache found so just return false here 没有找到cache，返回false
        return false;
    }
    boolean allocated = cache.allocate(buf, reqCapacity); // 分配已缓存的内存
    if (++ allocations >= freeSweepAllocationThreshold) {
        allocations = 0;
        trim();
    }
    return allocated;
}
```

看下` cache.allocate(buf, reqCapacity)`的逻辑:

```java
public final boolean allocate(PooledByteBuf<T> buf, int reqCapacity) {
    Entry<T> entry = queue.poll(); // 从队列中取出Entry，拿到该Entry对应的chunk和handle
    if (entry == null) {
        return false;
    }
    initBuf(entry.chunk, entry.handle, buf, reqCapacity); // ByteBuf初始化
    entry.recycle(); // 将弹出(用完)的Entry扔到对象池中复用，置chunk=null，handle=-1

    ++ allocations;
    return true;
}
```

通过`queue.poll()`从队列中取出Entry对象，然后初始化PooledByteBuf。

```
initBuf(entry.chunk, entry.handle, buf, reqCapacity); // ByteBuf初始化
```

```java
private static final class SubPageMemoryRegionCache<T> extends MemoryRegionCache<T> {
    SubPageMemoryRegionCache(int size, SizeClass sizeClass) {
        super(size, sizeClass);
    }

    @Override
    protected void initBuf(
            PoolChunk<T> chunk, long handle, PooledByteBuf<T> buf, int reqCapacity) {
        chunk.initBufWithSubpage(buf, handle, reqCapacity);
    }
}

// PoolChunk
private final PoolSubpage<T>[] subpages; // 大小：2048
void initBufWithSubpage(PooledByteBuf<T> buf, long handle, int reqCapacity) {
    initBufWithSubpage(buf, handle, bitmapIdx(handle), reqCapacity);
}
private void initBufWithSubpage(PooledByteBuf<T> buf, long handle, int bitmapIdx, int reqCapacity) {
    assert bitmapIdx != 0;

    int memoryMapIdx = memoryMapIdx(handle); // page 索引

    // subpageIdx(memoryMapIdx)：获取page相对于最左端的偏移，如2048，为0；2049，则为1(在深度11这一层)
    PoolSubpage<T> subpage = subpages[subpageIdx(memoryMapIdx)]; // 获取对应的subpage
    assert subpage.doNotDestroy;
    assert reqCapacity <= subpage.elemSize;

    // runOffset(memoryMapIdx) + (bitmapIdx & 0x3FFFFFFF) * subpage.elemSize: 计算分配到的内存相对于当前PoolChunk的偏移字节数
    // - runOffset(memoryMapIdx): 获取当前分配到的内存对应的page，相对于当前PoolChunk的偏移字节数，
    // - (bitmapIdx & 0x3FFFFFFF) * subpage.elemSize：获取当前page中，分配到的内存相对于page的偏移字节数
    buf.init( // ByteBuf初始化
        this, handle,
        runOffset(memoryMapIdx) + (bitmapIdx & 0x3FFFFFFF) * subpage.elemSize, reqCapacity, subpage.elemSize,
        arena.parent.threadCache());
}
```

初始化PooledByteBuf时，首先通过handle获取PoolChunk中对应page的memoryMapIdx:

```java
private static int memoryMapIdx(long handle) {
    return (int) handle; // 低32位为memoryMap索引
}
```

在下面subPage级别的内存分配将看到，该long类型handle由高32位bitmapIdx和低32位memoryMapIdx组成。memoryMapIdx表示该PoolChunk中对应page的索引，bitmapIdx表示该page中子subpage的内存区域的索引。

然后调用`subpages[subpageIdx(memoryMapIdx)]`获取PoolSubpage，即对应page细分之后(比如细分为1kB)创建的对象。最后初始化PooledByteBuf。

```java
// PooledByteBuf
void init(PoolChunk<T> chunk, long handle, int offset, int length, int maxLength, PoolThreadCache cache) {
    assert handle >= 0;
    assert chunk != null;

    this.chunk = chunk; // 内存块
    this.handle = handle; // 地址
    memory = chunk.memory; // DirectByteBuf/byte[]
    this.offset = offset; // 内存偏移(相对于chunk内存块)
    this.length = length; // 请求分配的大小
    this.maxLength = maxLength; // handle对应的内存的最大字节大小
    tmpNioBuf = null;
    this.cache = cache; // PoolThreadCache
}

// PooledUnsafeDirectByteBuf
void init(PoolChunk<ByteBuffer> chunk, long handle, int offset, int length, int maxLength,
          PoolThreadCache cache) {
  	// 调用PooledByteBuf构造器
    super.init(chunk, handle, offset, length, maxLength, cache);
    initMemoryAddress(); // 计算内存实际起始位置
}
private void initMemoryAddress() {
    // 一个DirectByteBuf(memory)对应一个PoolChunk，offset为相对于PoolChunk的偏移字节数,
    // PlatformDependent.directBufferAddress(memory)得到DirectByteBuf实际内存起始地址
    memoryAddress = PlatformDependent.directBufferAddress(memory) + offset; // 得到该ByteBuf实际内存地址
}
```

可以看到，PooledUnsafeDirectByteBuf在初始化调用父类PooledByteBuf构造器保存各种参数之后，还调用`initMemoryAddress()`计算该内存的实际起始位置，便于后续直接通过JDK Unsafe对象和内存地址读写数据。

> 将弹出的Entry扔到对象池Recycler中复用

```java
public final boolean allocate(PooledByteBuf<T> buf, int reqCapacity) {
    Entry<T> entry = queue.poll(); // 从队列中取出Entry，拿到该Entry对应的chunk和handle
    if (entry == null) {
        return false;
    }
    initBuf(entry.chunk, entry.handle, buf, reqCapacity); // ByteBuf初始化
    entry.recycle(); // 将弹出(用完)的Entry扔到对象池中复用，置chunk=null，handle=-1

    ++ allocations;
    return true;
}
```

在初始化PooledByteBuf之后，需要回收Entry对象到对象池中，以便后续复用。

```java
void recycle() {
    chunk = null;
    handle = -1;
    recyclerHandle.recycle(this);
}
public void recycle(Object object) {
    if (object != value) {
        throw new IllegalArgumentException("object does not belong to handle");
    }
    stack.push(this); // 对象回收时将DefaultHandle放入栈中
}
```

recycle()方法中主要重置了chunk、handle字段的值，然后将当前Entry放到了对象池中。该对象会在ByteBuf回收时取出并给字段重新赋值，便于后续分配相同大小的ByteBuf时，直接从缓存的内存中取出使用。

```java
private static Entry newEntry(PoolChunk<?> chunk, long handle) {
    Entry entry = RECYCLER.get(); // 从对象池中获取
    entry.chunk = chunk;
    entry.handle = handle;
    return entry;
}
```

## 六、page级别的内存分配：allocateNormal

### 1.PoolArena、PoolChunk、page、PoolSubpage的概念和结构

> PoolArena

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-9.jpg)

```java
enum SizeClass {
    Tiny,
    Small,
    Normal
}

private final PoolSubpage<T>[] tinySubpagePools; // 大小：32
private final PoolSubpage<T>[] smallSubpagePools; // 大小：4

private final PoolChunkList<T> q050;
private final PoolChunkList<T> q025;
private final PoolChunkList<T> q000;
private final PoolChunkList<T> qInit;
private final PoolChunkList<T> q075;
private final PoolChunkList<T> q100;
```

从上面可以看到，PoolArena主要由PoolChunkList和PoolSubpage两部分组成，PoolSubpage用于subpage的内存分配，PoolChunkList用于page和subpage的内存分配。在PoolArena里面涉及到PoolChunkList和PoolSubpage对应的结构有PoolChunk和PoolSubpage两个。

PoolChunkList之间使用双向链表连接，单个PoolChunkList由PoolChunk双向链表构成。tinySubpagePools、smallSubpagePools中的PoolSubpage引用，同样指向PoolSubpage构成的双向链表。

> PoolChunk

第一次申请内存的时候，PoolChunkList、PoolSubpage都是默认值(为空)，需要创建一个PoolChunk，默认一个PoolChunk是16MB。内部结构是完全二叉树，一共有4096个节点，有2048个叶子节点（每个叶子节点大小为一个page，就是8k），非叶子节点的内存大小等于左子树内存大小加上右子树内存大小。

完全二叉树结构如下：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-10.jpg)

上图中page0到page2047的叶子节点，每个大小8kB。

这颗完全二叉树在PoolChunk中是使用数组来进行表示的。

```java
// Generate the memory map. (重点)
memoryMap = new byte[maxSubpageAllocs << 1]; // maxSubpageAllocs: 2048, memoryMap.length: 4096
depthMap = new byte[memoryMap.length];
int memoryMapIndex = 1;
for (int d = 0; d <= maxOrder; ++ d) { // move down the tree one level at a time
    int depth = 1 << d;
    for (int p = 0; p < depth; ++ p) {
        // in each level traverse left to right and set value to the depth of subtree
        memoryMap[memoryMapIndex] = (byte) d;
        depthMap[memoryMapIndex] = (byte) d;
        memoryMapIndex ++;
    }
}
```

`depthMap`的值初始化后不再改变，表示这4096个节点各自的深度；`memoryMap`的值则随着节点分配而改变，初始值为树的深度，从0开始。初始化时，深度为0的节点表示可以分配16MB，深度为1的节点可以分配8MB，深度为11的节点可以分配8KB。如果该节点已经分配完成，就设置为12即可。

> page、PoolSubpage

从PoolChunk的结构可知，page表示完全二叉树的叶子节点，大小为8kB。如果申请的内存小于8KB，则page会被细分为subPage。比如申请的内存是2KB，则page被细分为4个2KB的subPage，对应的对象就是PoolSubpage。

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-11.jpg)

page细分时创建的PoolSubpage也会加入到PoolArena维护的tinySubpagePools、smallSubpagePools的双向链表结构中，便于subpage级别的内存分配时快速索引：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-12.jpg)

### 2.page级别的内存分配：allocateNormal

```java
private void allocate(PoolThreadCache cache, PooledByteBuf<T> buf, final int reqCapacity) {
    final int normCapacity = normalizeCapacity(reqCapacity);
    // ...省略
    if (normCapacity <= chunkSize) { // <=16MB
        // 1.首先在已存在的缓存buffer中分配
        if (cache.allocateNormal(this, buf, reqCapacity, normCapacity)) {
            // was able to allocate out of the cache so move on
            return;
        }
        // 2.否则在内存堆上分配（这里！！！）
        allocateNormal(buf, reqCapacity, normCapacity); // page级别的内存分配
    }
}
```

下面看allocateNormal()进行page级别的内存分配的逻辑。

```java
private synchronized void allocateNormal(PooledByteBuf<T> buf, int reqCapacity, int normCapacity) {
    // 1.先在PoolChunkList中已存在的PoolChunk上分配(normal/subpage)
    if (q050.allocate(buf, reqCapacity, normCapacity) || q025.allocate(buf, reqCapacity, normCapacity) ||
        q000.allocate(buf, reqCapacity, normCapacity) || qInit.allocate(buf, reqCapacity, normCapacity) ||
        q075.allocate(buf, reqCapacity, normCapacity)) {
        ++allocationsNormal; // 分配成功，返回
        return;
    }

    // Add a new chunk. 2.否则创建一个PoolChunk进行分配
    // pageSize: 8kB, maxOrder:11, pageShifts: 13, chunkSize: 16MB
    PoolChunk<T> c = newChunk(pageSize, maxOrder, pageShifts, chunkSize);
    long handle = c.allocate(normCapacity); // 分配内存，返回id(normal: memoryMapIdx, subpage: bitmapIdx+memoryMapIdx)
    ++allocationsNormal;
    assert handle > 0;
    c.initBuf(buf, handle, reqCapacity); // 初始化ByteBuf
    qInit.add(c); // 添加到PoolChunkList
}
```

> 尝试在现有的chunk上分配

首先在PoolChunkList中现有的PoolChunk中进行内存分配:

```java
boolean allocate(PooledByteBuf<T> buf, int reqCapacity, int normCapacity) {
    // PoolChunkList为空，或者请求的capacity太大，PoolChunkList中的PoolChunk无法分配
    if (head == null || normCapacity > maxCapacity) { 
        return false;
    }

    for (PoolChunk<T> cur = head;;) {
        long handle = cur.allocate(normCapacity);
        if (handle < 0) { // cur无法分配
            cur = cur.next;
            if (cur == null) { // 该PoolChunkList中的所有PoolChunk都无法分配，返回false
                return false;
            }
        } else { // cur可以分配
            cur.initBuf(buf, handle, reqCapacity); // ByteBuf初始化
            if (cur.usage() >= maxUsage) { // 判断使用率
                remove(cur); // 从当前chink list移除
                nextList.add(cur); // 加入到下一个chunk list
            }
            return true; // 分配成功
        }
    }
}
```

对于某个PoolChunkList，如果PoolChunkList为空(head=null)，或者请求的capacity太大，则PoolChunkList中的PoolChunk无法分配，直接返回false。否则遍历该PoolChunkList中的PoolChunk，只要有一个分配内存成功，就返回true。

下面看下内存分配过程：

```java
long allocate(int normCapacity) {
    if ((normCapacity & subpageOverflowMask) != 0) { // >= pageSize 8kB
        return allocateRun(normCapacity); // normal分配
    } else {
        return allocateSubpage(normCapacity); // subpage分配
    }
}
```

allocate()方法根据normCapacity的不同，完成normal内存和subpage内存的分配。由于这里主要分析page级别的内存分配，因此进入allocateRun()方法:

```java
private long allocateRun(int normCapacity) {
    int d = maxOrder - (log2(normCapacity) - pageShifts); // 计算平衡二叉树深度
    int id = allocateNode(d);
    if (id < 0) { // 不可用，返回
        return id;
    }
    freeBytes -= runLength(id); // 计算剩余可用字节数，runLength(id): 计算id对应节点的字节数
    return id;
}
```

allocateRun()方法中首先根据normCapacity计算出完全二叉树节点所在的树深度，然后根据树深度调用`allocateNode(d)`分配内存：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-13.jpg)

```java
private int allocateNode(int d) {
    int id = 1;
    int initial = - (1 << d); // has last d bits = 0 and rest all = 1
    byte val = value(id);
    if (val > d) { // unusable 不可用
        return -1;
    }
    while (val < d || (id & initial) == 0) { // id & initial == 1 << d for all ids at depth d, for < d it is 0
        id <<= 1; // 向下层遍历
        val = value(id);
        if (val > d) {
            id ^= 1;  // 计算兄弟节点id
            val = value(id);
        }
    }
    byte value = value(id); // 得到满足分配要求的id
    assert value == d && (id & initial) == 1 << d : String.format("val = %d, id & initial = %d, d = %d",
            value, id & initial, d);
    setValue(id, unusable); // mark as unusable id指向的内存段标记为不可用
    updateParentsAlloc(id); // 更新
    return id;
}
```

allocateNode()方法是page级别内存分配时，PoolChunk的完全二叉树节点查找算法的实现，读者需要仔细debug来理解其节点查找流程。找到了对应的节点id，即表示分配了对应的内存。id小于0，表示分配失败。

```java
boolean allocate(PooledByteBuf<T> buf, int reqCapacity, int normCapacity) {
    if (head == null || normCapacity > maxCapacity) { // PoolChunkList为空，或者请求的capacity太大，PoolChunkList中的PoolChunk无法分配
        return false;
    }

    for (PoolChunk<T> cur = head;;) {
        long handle = cur.allocate(normCapacity);
        if (handle < 0) { // cur无法分配
            cur = cur.next;
            if (cur == null) { // 该PoolChunkList中的所有PoolChunk都无法分配，返回false
                return false;
            }
        } else { // cur可以分配
            cur.initBuf(buf, handle, reqCapacity); // ByteBuf初始化
            if (cur.usage() >= maxUsage) { // 判断使用率
                remove(cur); // 从当前chink list移除
                nextList.add(cur); // 加入到下一个chunk list
            }
            return true; // 分配成功
        }
    }
}
```

PoolChunkList.allocate()方法在调用`cur.allocate(normCapacity)`代码分配内存之后，得到handle，即PoolChunk节点id。

> 初始化PooledByteBuf

```java
// PoolChunk
void initBuf(PooledByteBuf<T> buf, long handle, int reqCapacity) {
    int memoryMapIdx = memoryMapIdx(handle);
    int bitmapIdx = bitmapIdx(handle); // 该变量只在subpage分配时不为0
    if (bitmapIdx == 0) { // normal
        byte val = value(memoryMapIdx);
        assert val == unusable : String.valueOf(val);
        // runOffset(memoryMapIdx): chunk内存的字节偏移， runLength(memoryMapIdx): memoryMapIdx对应那块内存的最大值
        buf.init(this, handle, runOffset(memoryMapIdx), reqCapacity, runLength(memoryMapIdx),
                 arena.parent.threadCache());
    } else {
        initBufWithSubpage(buf, handle, bitmapIdx, reqCapacity); // 根据subPage初始化ByteBuf
    }
}
```

PooledByteBuf初始化时，首先通过`memoryMapIdx(handle)`获取handle对应的PoolChunk完全二叉树节点在memoryMap数组中的索引，此时`bitmapIdx(handle)`为0。下面进行PooledByteBuf真正的初始化:

```java
// runOffset(memoryMapIdx): chunk内存的字节偏移， runLength(memoryMapIdx): memoryMapIdx对应那块内存的最大值
buf.init(this, handle, runOffset(memoryMapIdx), reqCapacity, runLength(memoryMapIdx),
         arena.parent.threadCache());
```

PooledByteBuf(PooledUnsafeByteBuf)的初始化过程在前面**命中缓存的分配流程**中已经详细分析，这里不再介绍。

> 创建一个chunk进行内存分配

如果通过PoolChunkList在现有的chunk上无法完成内存分配，比如第一次分配内存时，此时需要创建一个chunk进行内存分配。

```java
private synchronized void allocateNormal(PooledByteBuf<T> buf, int reqCapacity, int normCapacity) {
    // 1.先在PoolChunkList中已存在的PoolChunk上分配(normal/subpage)
    if (q050.allocate(buf, reqCapacity, normCapacity) || q025.allocate(buf, reqCapacity, normCapacity) ||
        q000.allocate(buf, reqCapacity, normCapacity) || qInit.allocate(buf, reqCapacity, normCapacity) ||
        q075.allocate(buf, reqCapacity, normCapacity)) {
        ++allocationsNormal; // 分配成功，返回
        return;
    }

    // Add a new chunk. 2.否则创建一个PoolChunk进行分配
    // pageSize: 8kB, maxOrder:11, pageShifts: 13, chunkSize: 16MB
    PoolChunk<T> c = newChunk(pageSize, maxOrder, pageShifts, chunkSize);
    long handle = c.allocate(normCapacity); // 分配内存，返回id(normal: memoryMapIdx, subpage: bitmapIdx+memoryMapIdx)
    ++allocationsNormal;
    assert handle > 0;
    c.initBuf(buf, handle, reqCapacity); // 初始化ByteBuf
    qInit.add(c); // 添加到PoolChunkList
}
```

创建PoolChunk：

```java
// DirectArena
protected PoolChunk<ByteBuffer> newChunk(int pageSize, int maxOrder, int pageShifts, int chunkSize) {
    return new PoolChunk<ByteBuffer>(
            this, allocateDirect(chunkSize), // 向操作系统申请DirectByteBuffer内存
            pageSize, maxOrder, pageShifts, chunkSize);
}
```

调用newChunk创建PoolChunk时，首先调用` allocateDirect(chunkSize)`向操作系统申请DirectByteBuffer内存，然后初始化PoolChunk并返回。

```java
private static ByteBuffer allocateDirect(int capacity) {
    return PlatformDependent.useDirectBufferNoCleaner() ? // true
            PlatformDependent.allocateDirectNoCleaner(capacity) : ByteBuffer.allocateDirect(capacity);
}
```

初始化PoolChunk：

```java
PoolChunk(PoolArena<T> arena, T memory, int pageSize, int maxOrder, int pageShifts, int chunkSize) {
    unpooled = false;
    this.arena = arena;
    this.memory = memory; // DirectByteBuffer/byte[]
    this.pageSize = pageSize; // 8kB
    this.pageShifts = pageShifts; // 13
    this.maxOrder = maxOrder; // 11
    this.chunkSize = chunkSize; // 16MB
    unusable = (byte) (maxOrder + 1); // 12表示不可用
    log2ChunkSize = log2(chunkSize); // 24
    subpageOverflowMask = ~(pageSize - 1); // -pageSize: 8192, 8kB
    freeBytes = chunkSize; // 16MB

    assert maxOrder < 30 : "maxOrder should be < 30, but is: " + maxOrder;
    maxSubpageAllocs = 1 << maxOrder; // maxOrder: 11

    // Generate the memory map. (重点)
    memoryMap = new byte[maxSubpageAllocs << 1]; // maxSubpageAllocs: 2048, memoryMap.length: 4096
    depthMap = new byte[memoryMap.length];
    int memoryMapIndex = 1;
    for (int d = 0; d <= maxOrder; ++ d) { // move down the tree one level at a time
        int depth = 1 << d;
        for (int p = 0; p < depth; ++ p) {
            // in each level traverse left to right and set value to the depth of subtree
            memoryMap[memoryMapIndex] = (byte) d;
            depthMap[memoryMapIndex] = (byte) d;
            memoryMapIndex ++;
        }
    }

    subpages = newSubpageArray(maxSubpageAllocs); // 2048个subpage对象
}
```

在创建PoolChunk完毕后，调用`PoolChunk.allocate(normCapacity)`分配内存并初始化PooledByteBuf，该过程与通过PoolChunkLIst在现有的chunk上分配内存的逻辑相同，这里不再说明。

## 七、subPage级别的内存分配

```java
// PoolChunk
long allocate(int normCapacity) {
    if ((normCapacity & subpageOverflowMask) != 0) { // >= pageSize 8kB
        return allocateRun(normCapacity); // normal分配
    } else {
        return allocateSubpage(normCapacity); // subpage分配
    }
}
```

假设申请的内存小于page 8KB，则此时将进入`allocateSubpage(normCapacity)`逻辑。

```java
// PoolChunk
private long allocateSubpage(int normCapacity) {
    PoolSubpage<T> head = arena.findSubpagePoolHead(normCapacity);
    synchronized (head) {
        int d = maxOrder; //  subpage内存分配，只在leaves节点 --> 11
        int id = allocateNode(d); // 分配page
        if (id < 0) { // 无法分配
            return id;
        }

        final PoolSubpage<T>[] subpages = this.subpages; // subpages.length: 2048
        final int pageSize = this.pageSize; // 8kB

        freeBytes -= pageSize; // 更新可用字节数

        int subpageIdx = subpageIdx(id); // subpage索引，当前page索引对应的subpage对象
        PoolSubpage<T> subpage = subpages[subpageIdx];
        if (subpage == null) {
            // 创建PoolSubpage, 会添加到head链表
            // runOffset(id): 计算id对应page相对于该PoolChunk的字节偏移
            subpage = new PoolSubpage<T>(head, this, id, runOffset(id), pageSize, normCapacity);
            subpages[subpageIdx] = subpage;
        } else {
            subpage.init(head, normCapacity);
        }
        return subpage.allocate(); // subpage内存分配
    }
}
```

subPage级别的内存分配分为定位到SubPage、初始化SubPage、初始化PoolByteBuf三部分。在定位到PoolSubpage之前，执行`arena.findSubpagePoolHead(normCapacity)`获取该PoolSubpage将会插入到的双向链表的head，该head由PoolArena进行管理:

```java
// PoolArena
PoolSubpage<T> findSubpagePoolHead(int elemSize) {
    int tableIdx;
    PoolSubpage<T>[] table;
    if (isTiny(elemSize)) { // < 512byte
        tableIdx = elemSize >>> 4; // elemSize/16
        table = tinySubpagePools;
    } else {
        tableIdx = 0;
        elemSize >>>= 10;
        while (elemSize != 0) {
            elemSize >>>= 1;
            tableIdx ++;
        }
        table = smallSubpagePools;
    }

    return table[tableIdx];
}
```

findSubpagePoolHead()方法根据normCapacity找到对应双向链表的head，双向链表的head已在PoolArena初始化时构造完成:

```java
// PoolArena
private final PoolSubpage<T>[] tinySubpagePools;
private final PoolSubpage<T>[] smallSubpagePools;

protected PoolArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize) {
    // ...省略
    tinySubpagePools = newSubpagePoolArray(numTinySubpagePools); // 数目 32
    for (int i = 0; i < tinySubpagePools.length; i ++) {
        tinySubpagePools[i] = newSubpagePoolHead(pageSize); // 创建head PoolSubpage
    }

    numSmallSubpagePools = pageShifts - 9; // 4
    smallSubpagePools = newSubpagePoolArray(numSmallSubpagePools);
    for (int i = 0; i < smallSubpagePools.length; i ++) {
        smallSubpagePools[i] = newSubpagePoolHead(pageSize);
    }
    // ...省略
 }
private PoolSubpage<T> newSubpagePoolHead(int pageSize) {
    PoolSubpage<T> head = new PoolSubpage<T>(pageSize);
    head.prev = head;
    head.next = head;
    return head;
}
```

### 1.定位到SubPage

```java
int d = maxOrder; //  subpage内存分配，只在leaves节点 --> 11
int id = allocateNode(d); // 分配page
if (id < 0) { // 无法分配
    return id;
}

final PoolSubpage<T>[] subpages = this.subpages; // subpages.length: 2048
final int pageSize = this.pageSize; // 8kB

freeBytes -= pageSize; // 更新可用字节数

int subpageIdx = subpageIdx(id); // subpage索引，当前page索引对应的subpage对象
PoolSubpage<T> subpage = subpages[subpageIdx];
```

由于subpage级别的内存分配只在PoolChunk完全二叉树的叶子节点上进行分配，因此这里直接调用了`allocateNode(d)`得到page叶子节点的id(编号)。然后根据id调用`subpageIdx(id)`获取PoolSubpage对象在subpages数组中的索引:

```java
private int subpageIdx(int memoryMapIdx) {
    return memoryMapIdx ^ maxSubpageAllocs; // remove highest set bit, to get offset
}
```

然后取出PoolSubpage对象，第一次分配内存时，PoolSubpage对象为null。

### 2.初始化SubPage

```java
PoolSubpage<T> subpage = subpages[subpageIdx];
if (subpage == null) {
    // 创建PoolSubpage, 会添加到head链表
    // runOffset(id): 计算id对应page相对于该PoolChunk的字节偏移
    subpage = new PoolSubpage<T>(head, this, id, runOffset(id), pageSize, normCapacity);
    subpages[subpageIdx] = subpage;
} else {
    subpage.init(head, normCapacity);
}
```

由于`subpage`为null，则创建一个新的PoolSubpage，并设置到`subpages`数组。

```java
PoolSubpage(PoolSubpage<T> head, PoolChunk<T> chunk, int memoryMapIdx, int runOffset, int pageSize, int elemSize) {
    this.chunk = chunk; // 所属chunk
    this.memoryMapIdx = memoryMapIdx; // page id
    this.runOffset = runOffset; // id对应page相对于chunk的内存偏移
    this.pageSize = pageSize; // 8kB
    // 这里bitmap长度8已足够，因为elemSize最小为16B，一个page 8kB，最多可以分为512份，这512份可以用8个long类型数表示，
    // 一个long类型数表示64份，每一个bit位表示每一份的分配情况
    bitmap = new long[pageSize >>> 10]; // pageSize / 16 / 64 = 8
    init(head, elemSize);
}
void init(PoolSubpage<T> head, int elemSize) {
    doNotDestroy = true;
    this.elemSize = elemSize;
    if (elemSize != 0) {
        maxNumElems = numAvail = pageSize / elemSize; // page划分的份数
        nextAvail = 0;

        bitmapLength = maxNumElems >>> 6; // 计算需要使用的bitmap long类型数个数，即bitmapLength
        if ((maxNumElems & 63) != 0) {
            bitmapLength ++;
        }

        for (int i = 0; i < bitmapLength; i ++) {
            bitmap[i] = 0; // 初始化，bitmap一个long类型元素可表示64个内存段的使用情况(一个long 64位)
        }
    }
    addToPool(head); // 添加到subpage链表
}
```

PoolSubpage初始化时，会保存chunk、memoryMapIdx等字段的值，同时创建了一个long[] bitmap数组，该数组长度为8，因为每个long型元素一共有64位，每一位可表示大小为elemSize的内存段的使用情况，因此bitmap数组一共最大可表示512个大小为elemSize的内存段的使用情况。这是通过elemSize为16B(最小)时计算出来的，8KB/16B=512， 512/64=8。

PoolSubpage初始化时同时调用了`init(head, elemSize);`，在该方法中计算了maxNumElems、numAvail、bitmapLength等值，bitmapLength表示实际bitmap数组中用到的long型元素个数，并根据bitmapLength初始化bitmap。在init方法的最后，调用`addToPool(head)`将当前PoolSubpage对象添加到head开头的PoolSubpage双向链表中。

```java
private void addToPool(PoolSubpage<T> head) {
    assert prev == null && next == null;
    prev = head; // 头插法插入
    next = head.next;
    next.prev = this;
    head.next = this;
}
```

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-14.jpg)

初始化PoolSubpage之后，同时会将该对象设置到PoolChunk管理的`PoolSubpage<T>[] subpages`数组对应索引位置处：

```java
subpages[subpageIdx] = subpage;
```

### 3.分配内存

```java
subpage.allocate();
```

在获取到PoolSubpage之后，调用其allocate()方法分配内存。

```java
long allocate() {
    final int bitmapIdx = getNextAvail(); // 内存段位置，如67=64+3
    int q = bitmapIdx >>> 6; // bitmap数组索引，如1
    int r = bitmapIdx & 63; //  bitmap某个数组元素64位中第几位，如3
    assert ((bitmap[q] >>> r) & 1) == 0; // 断言该内存段空闲
    bitmap[q] |= 1L << r; // 或操作，赋值

    if (-- numAvail == 0) { // 可用内存段减1
        removeFromPool(); // 将自己从链表移除
    }

    return toHandle(bitmapIdx);
}
```

allocate()方法获取下一个可用的细分内存段位置bitmapIdx，同时根据该bitmapIdx值更新bitmap数组中的元素值，主要是修改某个long型元素的某一个bit位值，标记该内存段已被使用。在标记之后，调用`toHandle(bitmapIdx)`将bitmapIdx转换成handle返回:

```java
private long toHandle(int bitmapIdx) { // bitmapIdx + memoryMapIdx
    return 0x4000000000000000L | (long) bitmapIdx << 32 | memoryMapIdx;
}
```

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/ByteBuf-15.jpg)

可以看到，返回的long型 handle值主要由两部分构成，高32位bitmapIdx，低32位memoryMapIdx。memoryMapIdx表示当前PoolSubpage对应的PoolChunk完全二叉树叶子节点page在memoryMap数组中的位置，即编号id；bitmapIdx表示该page细分成更小的内存段后，当前分配给PooledByteBuf的内存段在所有细分内存段中的位置。

### 4.初始化PoolByteBuf

 在subpage级别的内存分配完毕之后，下面进入PooledByteBuf的初始化:

```java
// PoolArena
private synchronized void allocateNormal(PooledByteBuf<T> buf, int reqCapacity, int normCapacity) {
   	// ...省略
  	
    // Add a new chunk. 否则创建一个PoolChunk进行分配
    // pageSize: 8kB, maxOrder:11, pageShifts: 13, chunkSize: 16MB
    PoolChunk<T> c = newChunk(pageSize, maxOrder, pageShifts, chunkSize);
    long handle = c.allocate(normCapacity); // 分配内存，返回id(normal: memoryMapIdx, subpage: bitmapIdx+memoryMapIdx)
    ++allocationsNormal;
    assert handle > 0;
    c.initBuf(buf, handle, reqCapacity); // 初始化ByteBuf
    qInit.add(c); // 添加到PoolChunkList
}
```

```java
c.initBuf(buf, handle, reqCapacity); // 初始化ByteBuf
```

```java
void initBuf(PooledByteBuf<T> buf, long handle, int reqCapacity) {
    int memoryMapIdx = memoryMapIdx(handle);
    int bitmapIdx = bitmapIdx(handle); // 该变量只在subpage分配时不为0
    if (bitmapIdx == 0) { // normal
        byte val = value(memoryMapIdx);
        assert val == unusable : String.valueOf(val);
        // runOffset(memoryMapIdx): chunk内存的字节偏移， runLength(memoryMapIdx): memoryMapIdx对应那块内存的最大值
        buf.init(this, handle, runOffset(memoryMapIdx), reqCapacity, runLength(memoryMapIdx),
                 arena.parent.threadCache());
    } else {
        initBufWithSubpage(buf, handle, bitmapIdx, reqCapacity); // 根据subPage初始化ByteBuf
    }
}
```

initBuf()方法中由于handle值主要由高32位bitmapIdx、低32位memoryMapIdx两部分构成，因此`bitmapIdx(handle)`返回值bitmapIdx不为0:

```java
private static int memoryMapIdx(long handle) {
    return (int) handle; // 低32位为memoryMap索引，得到page id
}

private static int bitmapIdx(long handle) { // 得到subpage细分内存段偏移
    return (int) (handle >>> Integer.SIZE);
```

此时进入initBufWithSubpage()方法:

```java
private void initBufWithSubpage(PooledByteBuf<T> buf, long handle, int bitmapIdx, int reqCapacity) {
    assert bitmapIdx != 0;

    int memoryMapIdx = memoryMapIdx(handle); // page 索引

    // subpageIdx(memoryMapIdx)：获取page相对于最左端的偏移，如2048，为0；2049，则为1(在深度11这一层)
    PoolSubpage<T> subpage = subpages[subpageIdx(memoryMapIdx)]; // 获取对应的subpage
    assert subpage.doNotDestroy;
    assert reqCapacity <= subpage.elemSize;

    // runOffset(memoryMapIdx) + (bitmapIdx & 0x3FFFFFFF) * subpage.elemSize: 计算分配到的内存相对于当前PoolChunk的偏移字节数
    // - runOffset(memoryMapIdx): 获取当前分配到的内存对应的page，相对于当前PoolChunk的偏移字节数，
    // - (bitmapIdx & 0x3FFFFFFF) * subpage.elemSize：获取当前page中，分配到的内存相对于page的偏移字节数
    buf.init( // ByteBuf初始化
        this, handle,
        runOffset(memoryMapIdx) + (bitmapIdx & 0x3FFFFFFF) * subpage.elemSize, reqCapacity, subpage.elemSize,
        arena.parent.threadCache());
}
```

initBufWithSubpage()方法中首先根据handle获取page memoryMapIdx，然后根据memoryMapIdx得到PoolSubpage。然后计算分配到的内存相对于当前PoolChunk的偏移字节数:

```java
runOffset(memoryMapIdx) + (bitmapIdx & 0x3FFFFFFF) * subpage.elemSize, reqCapacity, subpage.elemSize
```

计算时`runOffset(memoryMapIdx)`获取当前分配到的内存对应的page，相对于当前PoolChunk的偏移字节数；`(bitmapIdx & 0x3FFFFFFF) * subpage.elemSize`获取当前page中，分配到的内存相对于page的偏移字节数，最终得到的是分配到的内存相对于当前PoolChunk的偏移字节数。然后初始化PooledByteBuf：

```java
// PooledByteBuf
void init(PoolChunk<T> chunk, long handle, int offset, int length, int maxLength, PoolThreadCache cache) {
    assert handle >= 0;
    assert chunk != null;

    this.chunk = chunk; // 内存块
    this.handle = handle; // bitmapIdx+momorymapIdx
    memory = chunk.memory; // DirectByteBuf
    this.offset = offset; // 内存偏移(相对于chunk内存块)
    this.length = length; // 请求分配的大小
    this.maxLength = maxLength; // handle对应的内存的最大字节大小
    tmpNioBuf = null;
    this.cache = cache; // PoolThreadCache
}
// PooledUnsafeDirectByteBuf
void init(PoolChunk<ByteBuffer> chunk, long handle, int offset, int length, int maxLength,
          PoolThreadCache cache) {
    super.init(chunk, handle, offset, length, maxLength, cache);
    initMemoryAddress(); // 计算内存实际起始位置
}
```

详细的初始化过程前面已分析，不再介绍。

## 八、PooledByteBuf的回收

```java
ByteBuf.release();

public boolean release() {
    return release0(1);
}
private boolean release0(int decrement) {
    for (;;) {
        int refCnt = this.refCnt;
        if (refCnt < decrement) {
            throw new IllegalReferenceCountException(refCnt, -decrement);
        }

        if (refCntUpdater.compareAndSet(this, refCnt, refCnt - decrement)) { // CAS更新，减少引用计数
            if (refCnt == decrement) {
                deallocate(); // 回收分配的ByteBuf内存
                return true; // 引用计数到达0，回收内存，返回true
            }
            return false;
        }
    }
}
```

当调用ByteBuf.release()方法减少引用计数时，如果引用计数到达0，则调用deallocate()方法回收分配的ByteBuf内存。

```java
// PooledByteBuf
protected final void deallocate() {
    if (handle >= 0) {
        final long handle = this.handle;
        this.handle = -1;
        memory = null;
        chunk.arena.free(chunk, handle, maxLength, cache);
        recycle(); // PoolByteBuf加到对象池
    }
}
```

deallocate()逻辑可以分为两部分：ByteBuf对应的连续内存区段加到缓存MemoryRegionCache中、PooledByteBuf加到对象池Recycler以复用。

### 1.连续的内存区段加到缓存MemoryRegionCache

```java
void free(PoolChunk<T> chunk, long handle, int normCapacity, PoolThreadCache cache) {
    if (chunk.unpooled) {
        int size = chunk.chunkSize();
        destroyChunk(chunk);
        activeBytesHuge.add(-size);
        deallocationsHuge.increment();
    } else { // 走else分支
        SizeClass sizeClass = sizeClass(normCapacity);
        if (cache != null && cache.add(this, chunk, handle, normCapacity, sizeClass)) {
            // cached so not free it.
            return;
        }

        freeChunk(chunk, handle, sizeClass); // 缓存队列满了
    }
}
```

free()方法将调用`PoolThreadCache.add(this, chunk, handle, normCapacity, sizeClass)`方法将该连续的内存区段加到MemoryRegionCache缓存中:

```java
boolean add(PoolArena<?> area, PoolChunk chunk, long handle, int normCapacity, SizeClass sizeClass) {
    MemoryRegionCache<?> cache = cache(area, normCapacity, sizeClass);
    if (cache == null) {
        return false;
    }
    return cache.add(chunk, handle);
}
private MemoryRegionCache<?> cache(PoolArena<?> area, int normCapacity, SizeClass sizeClass) {
    switch (sizeClass) {
    case Normal:
        return cacheForNormal(area, normCapacity);
    case Small:
        return cacheForSmall(area, normCapacity);
    case Tiny:
        return cacheForTiny(area, normCapacity);
    default:
        throw new Error();
    }
}
```

add()方法添加内存区段时，首先找到对应的MemoryRegionCache，然后调用MemoryRegionCache.add()方法:

```java
public final boolean add(PoolChunk<T> chunk, long handle) {
    Entry<T> entry = newEntry(chunk, handle);
    boolean queued = queue.offer(entry);
    if (!queued) {
        // If it was not possible to cache the chunk, immediately recycle the entry
        entry.recycle(); // 回收Entry对象到对象池
    }

    return queued;
}
private static Entry newEntry(PoolChunk<?> chunk, long handle) {
    Entry entry = RECYCLER.get(); // 从对象池中获取
    entry.chunk = chunk;
    entry.handle = handle;
    return entry;
}
```

MemoryRegionCache.add()方法中首先从Entry对象池中拿出Entry对象进行复用，并设置chunk、handle字段的值，然后将该Entry放入MemoryRegionCache对应的queue中缓存，以便下次申请相同大小的内存时可以直接使用。如果添加到queue失败，则Entry对象会被再次回收到其对象池，以便别的ByteBuf释放内存时再使用。

再来看free()方法:

```java
void free(PoolChunk<T> chunk, long handle, int normCapacity, PoolThreadCache cache) {
    if (chunk.unpooled) {
        int size = chunk.chunkSize();
        destroyChunk(chunk);
        activeBytesHuge.add(-size);
        deallocationsHuge.increment();
    } else { // 走else分支
        SizeClass sizeClass = sizeClass(normCapacity);
        if (cache != null && cache.add(this, chunk, handle, normCapacity, sizeClass)) {
            // cached so not free it.
            return;
        }

        freeChunk(chunk, handle, sizeClass); // 缓存队列满了
    }
}
```

如果连续的内存区段加到缓存MemoryRegionCache失败，可能是因为MemoryRegionCache对应的queue满了，则调用freeChunk()方法，标记连续的内存区段为未使用:

```java
// PoolArena
void freeChunk(PoolChunk<T> chunk, long handle, SizeClass sizeClass) {
    final boolean destroyChunk;
    synchronized (this) {
        switch (sizeClass) {
        case Normal:
            ++deallocationsNormal;
            break;
        case Small:
            ++deallocationsSmall;
            break;
        case Tiny:
            ++deallocationsTiny;
            break;
        default:
            throw new Error();
        }
        destroyChunk = !chunk.parent.free(chunk, handle); // 这里！！！
    }
    if (destroyChunk) {
        destroyChunk(chunk);
    }
}
// PoolChunkList
boolean free(PoolChunk<T> chunk, long handle) {
    chunk.free(handle); // 标记该段内存未使用
    if (chunk.usage() < minUsage) {
        remove(chunk);
        // Move the PoolChunk down the PoolChunkList linked-list.
        return move0(chunk);
    }
    return true;
}
```

freeChunk()方法最终调用到`chunk.free(handle);`：

```java
// PoolChunk
void free(long handle) {
    int memoryMapIdx = memoryMapIdx(handle);
    int bitmapIdx = bitmapIdx(handle); // sub page

    if (bitmapIdx != 0) { // free a subpage
        PoolSubpage<T> subpage = subpages[subpageIdx(memoryMapIdx)];
        assert subpage != null && subpage.doNotDestroy;

        PoolSubpage<T> head = arena.findSubpagePoolHead(subpage.elemSize);
        synchronized (head) {
            if (subpage.free(head, bitmapIdx & 0x3FFFFFFF)) { // 标记为可用
                return;
            }
        }
    }
    freeBytes += runLength(memoryMapIdx); // 可用字节数更新
    setValue(memoryMapIdx, depth(memoryMapIdx)); // memoryMapIdx节点标记为可用
    updateParentsFree(memoryMapIdx); // 更新父节点
}
```

PoolChunk.free()方法主要根据handle判断回收的内存是subpage级别的内存还是normal级别的内存，`bitmapIdx != 0`表示回收的内存是subpage级别的内存，然后标记该段内存为可用。

### 2.PooledByteBuf加到对象池Recycler

在连续的内存区段加到缓存MemoryRegionCache之后，将PooledByteBuf加到对象池Recycler以复用。

```java
// PooledByteBuf
recycle(); // PoolByteBuf加到对象池

private void recycle() {
    recyclerHandle.recycle(this);
}

// DefaultHandle
public void recycle(Object object) {
    if (object != value) {
        throw new IllegalArgumentException("object does not belong to handle");
    }
    stack.push(this);
}
```

##九、池化内存分配的总结

池化内存分配的过程中，用到了对象池(PooledByteBuf、Entry...)、缓存(MemoryRegionCache)、完全二叉树(page)、位图(PoolSubpage)等技术。

## 十、面试问题

### 1.ByteBuf内存的类别有哪些 

- Heap和Direct

  表示ByteBuf底层使用的是堆内存字节数组，或者堆外内存DirectByteBuffer。

- Pooled和Unpooled

  Pooled表示池化的内存，即分配内存时可以从预先分配好的内存中取出一块内存；Unpooled表示非池化的内存，即直接调用API向操作系统申请一块内存。

- Unsafe和非Unsafe 

  表示ByteBuf读写数据时是否依赖JDK Unsafe对象，Unsafe表示依赖。

一般情况下，用户在分配ByteBuf内存时只需根据Heap和Direct、Pooled和Unpooled两个角度进行内存分配，Unsafe和非Unsafe是由Netty根据运行环境自动识别的。

### 2.如何减少多线程内存分配之间的竞争

```java
// PooledByteBufAllocator#newDirectBuffer
PoolThreadCache cache = threadCache.get(); // threadCache: PoolThreadLocalCache
PoolArena<ByteBuffer> directArena = cache.directArena;
```

通过PoolThreadLocalCache实现，PoolThreadLocalCache为FastThreadLocal实现，因此调用`threadCache.get()`获取PoolThreadCache时，不同NIO线程拿到的是各自的PoolThreadCache，这样使得多线程内存分配减少了竞争。

### 3.不同大小的内存是如何进行分配的

根据上面分析的内容，page级别的内存是通过PoolChunk的**完全二叉树**根据一定算法分配的；subpage级别的内存是通过subpage对应的**bitmap位图**完成分配的。

## 参考文章

- [Netty的内存管理的一些细节！](http://www.jiangxinlingdu.com/netty/2019/08/11/netty-bytebuf-allocator.html)
- [深入浅出Netty内存管理 PoolChunk](https://www.jianshu.com/p/c4bd37a3555b)
- [深入浅出Netty内存管理 PoolSubpage](https://www.jianshu.com/p/d91060311437)
- [深入浅出Netty内存管理 PoolChunkList](https://www.jianshu.com/p/a1debfe4ff02)
- [深入浅出Netty内存管理 PoolArena](https://www.jianshu.com/p/4856bd30dd56)

