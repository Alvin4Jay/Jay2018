# Netty拆包器分析

所谓拆包，指的是Netty读取到的二进制数据流(ByteBuf)首先需要经过**拆包器**的拆包之后得到一个个完整的业务数据包(ByteBuf)，这样子完整的业务数据包才能交给**业务数据包解码器**解码，最终得到业务Java对象。Netty里拆包器的抽象基类是ByteToMessageDecoder，具体的拆包器有FixedLengthFrameDecoder、LineBasedFrameDecoder、DelimiterBasedFrameDecoder和LengthFieldBasedFrameDecoder。本文主要分析拆包器抽象基类ByteToMessageDecoder的解码抽象实现，以及最常用的LengthFieldBasedFrameDecoder。

## 一、拆包器抽象ByteToMessageDecoder

```java
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof ByteBuf) {
        CodecOutputList out = CodecOutputList.newInstance(); // 从对象池中拿出CodecOutputList对象
        try {
            // 1.累加数据
            ByteBuf data = (ByteBuf) msg;
            first = (cumulation == null);
            if (first) {
                cumulation = data; // 第一次，直接指向传入的ByteBuf
            } else {
                cumulation = cumulator.cumulate(ctx.alloc(), cumulation, data); // 字节累加
            }
            // 2.解码、拆包
            callDecode(ctx, cumulation, out);
        } catch (DecoderException e) {
            throw e;
        } catch (Throwable t) {
            throw new DecoderException(t);
        } finally {
            if (cumulation != null && !cumulation.isReadable()) { // 字节容器cumulation如果无数据可读了
                numReads = 0;
                cumulation.release(); // 释放内存
                cumulation = null;
            } else if (++ numReads >= discardAfterReads) { // 字节容器cumulation还有数据可读，且channelRead次数到达discardAfterReads
                numReads = 0; // 重新开始计数
                discardSomeReadBytes(); // 3.清除字节容器cumulation废弃字节，防止OOM Error
            }

            int size = out.size(); // 拆出的业务数据包个数
            decodeWasNull = !out.insertSinceRecycled(); // 本次读取数据是否拆到一个业务数据包
            fireChannelRead(ctx, out, size); // 4.将解析到的对象向下传播
            out.recycle(); // CodecOutputList回收到对象池
        }
    } else {
        ctx.fireChannelRead(msg);
    }
}
```

ByteToMessageDecoder解码拆包的过程主要聚焦于channelRead()方法，可以分为如下的几个步骤:

- 累加字节流
- 调用子类的decode方法进行解析
- 清理字节容器
- 将解析到的ByteBuf向下传播

### 1.累加字节流

```java
private Cumulator cumulator = MERGE_CUMULATOR; // 累加器

// 1.累加数据
ByteBuf data = (ByteBuf) msg;
first = (cumulation == null);
if (first) {
    cumulation = data; // 第一次，直接指向传入的ByteBuf
} else {
    cumulation = cumulator.cumulate(ctx.alloc(), cumulation, data); // 字节累加
}
```

如果是第一次累加字节流，则将累加的字节容器cumulation直接指向传入的数据ByteBuf；否则，使用累加器累加传入的数据到字节容器cumulation。

累加器默认使用的是MERGE_CUMULATOR:

```java
public static final Cumulator MERGE_CUMULATOR = new Cumulator() {
    @Override
    public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in) {
        ByteBuf buffer;
        if (cumulation.writerIndex() > cumulation.maxCapacity() - in.readableBytes()
                || cumulation.refCnt() > 1) {
            buffer = expandCumulation(alloc, cumulation, in.readableBytes()); // 扩容字节容器
        } else {
            buffer = cumulation;
        }
        buffer.writeBytes(in); // 累加ByteBuf in
        in.release();
        return buffer;
    }
};
```

累加器累加数据之前会判断当前的字节容器容量是否能放下传入的最新数据，如果不能则需要扩容:

```java
// 扩容字节容器
static ByteBuf expandCumulation(ByteBufAllocator alloc, ByteBuf cumulation, int readable) {
    ByteBuf oldCumulation = cumulation;
    cumulation = alloc.buffer(oldCumulation.readableBytes() + readable);
    cumulation.writeBytes(oldCumulation); // 数据转移
    oldCumulation.release(); // 旧字节容器ByteBuf回收内存
    return cumulation;
}
```

处理完扩容问题后，累加器将传入的ByteBuf写入到字节容器中。

以上过程就完成字节流的累加操作。

### 2.调用子类的decode方法进行解析

```java
// 2.解码、拆包
callDecode(ctx, cumulation, out);
```

 接下去调用callDecode()方法进行拆包:

```java
protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    try {
        while (in.isReadable()) {
            int outSize = out.size();

            if (outSize > 0) {
                fireChannelRead(ctx, out, outSize); // 传播channelRead事件
                out.clear();

                if (ctx.isRemoved()) {
                    break;
                }
                outSize = 0;
            }

            int oldInputLength = in.readableBytes(); // 记录一下字节容器中有多少字节待拆
            decode(ctx, in, out); // 解码、拆包

            if (ctx.isRemoved()) {
                break;
            }

            if (outSize == out.size()) { // 没有解析到完整的数据包
              	// 如果可读字节数不变，说明子类解码时没有读取字节，直接退出
                if (oldInputLength == in.readableBytes()) { 
                    break;
                } else { // 子类解码时读取了部分字节，但不足以构成一个完整的数据包，继续读取
                    continue;
                }
            }

            if (oldInputLength == in.readableBytes()) { // 解析到数据包，但没有从当前累加器读取数据
                throw new DecoderException(
                        StringUtil.simpleClassName(getClass()) +
                        ".decode() did not read anything but decoded a message.");
            }

            if (isSingleDecode()) { // 是否每次只读取一个数据包
                break;
            }
        }
    } catch (DecoderException e) {
        throw e;
    } catch (Throwable cause) {
        throw new DecoderException(cause);
    }
}
```

callDecode()方法中使用了一个while循环，只要字节容器cumulation中有数据可读就不断循环。

循环中首先判断业务数据包容器out中是否已经拆出了业务数据包，如果`outSize > 0`，先将业务数据包向后面的业务handler传递。

然后记录下当前可读的字节数:

```java
int oldInputLength = in.readableBytes(); // 记录一下字节容器中有多少字节待拆
```

接着调用抽象方法(子类实现)decode完成具体的拆包动作:

```java
protected abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;
```

解码、拆包完成后进行下面的判断：

```java
if (outSize == out.size()) { // 没有解析到完整的数据包
  	// 如果可读字节数不变，说明子类解码时没有读取字节，直接退出
    if (oldInputLength == in.readableBytes()) { 
        break;
    } else { // 子类解码时读取了部分字节，但不足以构成一个完整的数据包，继续读取
        continue;
    }
}
```

如果`outSize == out.size()`条件成立，表明没有解析到完整的业务数据包。如果字节容器可读字节数不变，则表明子类解码时没有读取字节，那么直接退出循环；如果子类解码时读取了部分字节，但不足以构成一个完整的数据包，则继续读取。

如果`outSize == out.size()`条件不成立，即确实解码得到了业务数据包，则判断字节容器的可读字节数是否变化:

```java
if (oldInputLength == in.readableBytes()) { // 解析到数据包，但没有从当前累加器读取数据
    throw new DecoderException(
            StringUtil.simpleClassName(getClass()) +
            ".decode() did not read anything but decoded a message.");
}
```

如果未变化，说明虽然解析到了业务数据包，但没有从当前累加器读取数据，表明解码有问题，抛出异常。

循环的最后，判断是否每次channelRead调用时，是否只解码、拆包一个业务数据包，如果是，跳出循环；否则，继续拆包。

```java
if (isSingleDecode()) { // 是否每次只读取一个数据包
    break;
}
```

### 3.清理字节容器

```java
// channelRead()方法的finally代码块
if (cumulation != null && !cumulation.isReadable()) { // 字节容器cumulation如果无数据可读了
    numReads = 0;
    cumulation.release(); // 释放内存
    cumulation = null;
} else if (++ numReads >= discardAfterReads) { // 字节容器cumulation还有数据可读，且channelRead次数到达discardAfterReads
    // We did enough reads already try to discard some bytes so we not risk to see a OOME.
    // See https://github.com/netty/netty/issues/4275
    numReads = 0; // 重新开始计数
    discardSomeReadBytes(); // 3.清除字节容器cumulation废弃字节，防止OOM Error
}
int size = out.size(); // 拆出的业务数据包个数
decodeWasNull = !out.insertSinceRecycled(); // 本次读取数据是否拆到一个业务数据包
fireChannelRead(ctx, out, size); // 4.将解析到的对象向下传播
out.recycle(); // CodecOutputList回收到对象池
```

在channelRead()方法解码拆包完成之后，会判断字节容器是否还有数据可读。如果不可读，则将释放字节容器的内存；如果当前字节容器还是可读的，则判断channelRead()方法调用的次数是否达到16(discardAfterReads默认值)，如果达到了16，则调用`discardSomeReadBytes()`方法丢弃字节容器ByteBuf中的废弃已读字节:

```java
protected final void discardSomeReadBytes() {
    // 当前字节容器不为空，且引用计数为1，表明用户没有在字节容器上调用slice().retain() 
  	// or duplicate().retain() 可以安全的丢弃废弃字节
    if (cumulation != null && !first && cumulation.refCnt() == 1) {
        cumulation.discardSomeReadBytes();
    }
}
```

discardSomeReadBytes()调用之前，字节容器中的数据分布

```java
+--------------+----------+----------+
|废弃字节(已读)  | 可读字节  | 可写字节  | 
+--------------+----------+----------+
```

discardSomeReadBytes()调用之后，字节容器中的数据分布

```java
+----------+-------------------------+
| 可读字节  |      可写字节             | 
+----------+-------------------------+
```

这么做的目的是如果发送端发送数据过快，则接收端channelRead()方法调用频繁，如果不对字节容器做清理，字节容器占用大小会快速增大，可能会导致OOM。调用`discardSomeReadBytes()`可以释放已读数据的空间，增大可写字节的空间大小。

以上说的是在channelRead()方法中的清理字节容器的操作，下面看下channelReadComplete()方法里清理字节容器的操作：

```java
// channelReadComplete方法
public void channelReadComplete(ChannelHandlerContext ctx) throws Exception { // channel数据读取完毕
    numReads = 0; // 重新开始计数
    discardSomeReadBytes(); // 清理字节容器废弃字节
    if (decodeWasNull) { // 读取完毕，没有解码出一个数据包
        decodeWasNull = false;
        if (!ctx.channel().config().isAutoRead()) {
            // 即使不是auto-read，也要向selector注册op_read事件，以便于下一次能读到数据之后拼接成
          	// 一个完整的数据包
            ctx.read(); 
        }
    }
    ctx.fireChannelReadComplete();
}
```

在一次读取数据完毕之后，将调用channelReadComplete()方法，该方法将调用discardSomeReadBytes()方法清理字节容器。

需要说明的是，如果decodeWasNull为true，即上述的channelRead()方法未解码、拆得一个业务数据包，则即使channel配置的autoread为false，也要向selector注册op_read事件，以便于下一次能读到数据之后拼接成一个完整的业务数据包。

### 4.将解析到的ByteBuf向下传播

```java
int size = out.size(); // 拆出的业务数据包个数
decodeWasNull = !out.insertSinceRecycled(); // 本次读取数据是否拆到一个业务数据包
fireChannelRead(ctx, out, size); // 4.将解析到的对象向下传播
out.recycle(); // CodecOutputList回收到对象池
```

这个步骤中主要是调用`fireChannelRead(ctx, out, size);`，将解码、拆得的业务数据包向pipeline传递。

```java
static void fireChannelRead(ChannelHandlerContext ctx, CodecOutputList msgs, int numElements) {
    for (int i = 0; i < numElements; i ++) {
        ctx.fireChannelRead(msgs.getUnsafe(i)); // 逐个传播，通常是ByteBuf，传递给业务解码器
    }
}
```

以上便是拆包器抽象ByteToMessageDecoder进行解码、拆包的整体流程，下面分析拆包器的具体实现LengthFieldBasedFrameDecoder的拆包流程，它也是用的最多、功能最强大的拆包器。

## 二、LengthFieldBasedFrameDecoder分析

### 1.LengthFieldBasedFrameDecoder的用法

首先给出LengthFieldBasedFrameDecoder配置的重要参数及其含义:

```java
private final ByteOrder byteOrder; // 长度域的字节序
private final int maxFrameLength; // 最大帧长度
private final int lengthFieldOffset; // 长度域偏移
private final int lengthFieldLength; // 长度域占用字节数
private final int lengthFieldEndOffset; // 长度域末端偏移
private final int lengthAdjustment; // 长度域的值的补偿，长度域的值加上这个值之后的值是这个长度域后面还需要读取的字节数
private final int initialBytesToStrip; // 解码出一个数据包之后，去掉开头的字节数
```

- lengthFieldOffset：长度域字节偏移。
- lengthFieldLength：长度域占用字节数。
- lengthAdjustment：长度域的值的补偿，长度域的值加上这个值之后的值是这个长度域后面还需要读取的字节数。
- initialBytesToStrip：解码出一个业务数据包之后，去掉开头的字节数。

下面基于[LengthFieldBasedFrameDecoder](https://netty.io/4.1/api/io/netty/handler/codec/LengthFieldBasedFrameDecoder.html)  javadoc文档给出的例子，分析LengthFieldBasedFrameDecoder的用法。

>  2 bytes length field at offset 0, do not strip header

```java
 BEFORE DECODE (14 bytes)         AFTER DECODE (14 bytes)
 +--------+----------------+      +--------+----------------+
 | Length | Actual Content |----->| Length | Actual Content |
 | 0x000C | "HELLO, WORLD" |      | 0x000C | "HELLO, WORLD" |
 +--------+----------------+      +--------+----------------+
```

前提是长度域字节偏移为0，占用长度2字节，解析出一个数据包之后，不需要将长度域去除。因此参数配置如下：

```java
lengthFieldOffset   = 0
lengthFieldLength   = 2
lengthAdjustment    = 0 // 长度域的值是12，表示后面body的字节数，因此不需要调整
initialBytesToStrip = 0 (= do not strip header) // 不去掉长度域
```

> 2 bytes length field at offset 0, strip header

```java
BEFORE DECODE (14 bytes)         AFTER DECODE (12 bytes)
 +--------+----------------+      +----------------+
 | Length | Actual Content |----->| Actual Content |
 | 0x000C | "HELLO, WORLD" |      | "HELLO, WORLD" |
 +--------+----------------+      +----------------+
```

前提是长度域字节偏移为0，占用长度2字节，解析出一个数据包之后，需要将长度域去除。因此参数配置如下：

```java
lengthFieldOffset   = 0
lengthFieldLength   = 2
lengthAdjustment    = 0 // 长度域的值是12，表示后面body的字节数，因此不需要调整
initialBytesToStrip = 2 (= the length of the Length field) // 去掉长度域，2字节
```

>  2 bytes length field at offset 0, do not strip header, the length field represents the length of the whole message

```java
 BEFORE DECODE (14 bytes)         AFTER DECODE (14 bytes)
 +--------+----------------+      +--------+----------------+
 | Length | Actual Content |----->| Length | Actual Content |
 | 0x000E | "HELLO, WORLD" |      | 0x000E | "HELLO, WORLD" |
 +--------+----------------+      +--------+----------------+
```

前提是长度域字节偏移为0，占用长度2字节，长度域的值代表整个数据包的长度，解析出一个数据包之后，不需要将长度域去除。因此参数配置如下：

```java
lengthFieldOffset   =  0
lengthFieldLength   =  2
lengthAdjustment    = -2 (= the length of the Length field)
initialBytesToStrip =  0
```

`lengthAdjustment =-2`表示由于长度域的值为整个数据包的长度(14字节)，而实际上长度域的值表示长度域之后需要读取的字节数，因为这里body是12字节，所以长度域的值需要补偿-2，即后面读取12字节。

> 3 bytes length field at the end of 5 bytes header, do not strip header

```java
 BEFORE DECODE (17 bytes)                      AFTER DECODE (17 bytes)
 +----------+----------+----------------+      +----------+----------+----------------+
 | Header 1 |  Length  | Actual Content |----->| Header 1 |  Length  | Actual Content |
 |  0xCAFE  | 0x00000C | "HELLO, WORLD" |      |  0xCAFE  | 0x00000C | "HELLO, WORLD" |
 +----------+----------+----------------+      +----------+----------+----------------+
```

前提是长度域字节偏移2，长度域长度3，长度域的值表示body的长度，解析出一个数据包之后，不需要将header去除。因此参数配置如下：

```java
lengthFieldOffset   = 2 (= the length of Header 1)
lengthFieldLength   = 3
lengthAdjustment    = 0
initialBytesToStrip = 0
```

> 3 bytes length field at the beginning of 5 bytes header, do not strip header

```java
 BEFORE DECODE (17 bytes)                      AFTER DECODE (17 bytes)
 +----------+----------+----------------+      +----------+----------+----------------+
 |  Length  | Header 1 | Actual Content |----->|  Length  | Header 1 | Actual Content |
 | 0x00000C |  0xCAFE  | "HELLO, WORLD" |      | 0x00000C |  0xCAFE  | "HELLO, WORLD" |
 +----------+----------+----------------+      +----------+----------+----------------+
```

前提是长度域字节偏移0，长度域长度3，长度域的值表示body的长度12，长度域与body之间存在header1。解析出一个数据包之后，不需要将header去除。因此参数配置如下：

```java
lengthFieldOffset   = 0
lengthFieldLength   = 3
lengthAdjustment    = 2 (= the length of Header 1)
initialBytesToStrip = 0
```

lengthAdjustment需要设置为2，表示长度域之后需要读取12+2个字节。

> 2 bytes length field at offset 1 in the middle of 4 bytes header, strip the first header field and the length field

```java
BEFORE DECODE (16 bytes)                       AFTER DECODE (13 bytes)
 +------+--------+------+----------------+      +------+----------------+
 | HDR1 | Length | HDR2 | Actual Content |----->| HDR2 | Actual Content |
 | 0xCA | 0x000C | 0xFE | "HELLO, WORLD" |      | 0xFE | "HELLO, WORLD" |
 +------+--------+------+----------------+      +------+----------------+
```

前提是长度域字节偏移1，长度域长度2，长度域的值表示body的长度12，整个header长度为4。解析出一个数据包之后，需要去除header1+长度域。配置参数如下：

```java
lengthFieldOffset   = 1 (= the length of HDR1)
lengthFieldLength   = 2
lengthAdjustment    = 1 (= the length of HDR2)
initialBytesToStrip = 3 (= the length of HDR1 + LEN)
```

lengthAdjustment=1表示由于长度域和body之间存在header2，且目前长度域的值为body的长度，因此需要给长度域的值补偿1字节，从而读取header2+body。initialBytesToStrip=3，表示去除一个数据报中前3个字节，即header1+长度域。

>  2 bytes length field at offset 1 in the middle of 4 bytes header, strip the first header field and the length field, the length field represents the length of the whole message

```java
BEFORE DECODE (16 bytes)                       AFTER DECODE (13 bytes)
 +------+--------+------+----------------+      +------+----------------+
 | HDR1 | Length | HDR2 | Actual Content |----->| HDR2 | Actual Content |
 | 0xCA | 0x0010 | 0xFE | "HELLO, WORLD" |      | 0xFE | "HELLO, WORLD" |
 +------+--------+------+----------------+      +------+----------------+
```

与上一种情况不同的是，本例中长度域表示整个数据包的长度，其他一致。此时配置参数如下：

```java
lengthFieldOffset   =  1
lengthFieldLength   =  2
lengthAdjustment    = -3 (= the length of HDR1 + LEN, negative)
initialBytesToStrip =  3
```

lengthAdjustment=-3表示由于此时长度域的值为整个数据包的长度，而数据包中长度域之后需要读取的字节数为13，因此需要对长度域的值进行补偿，补偿长度为-3，即长度域之后只需读13个字节。

### 2.LengthFieldBasedFrameDecoder拆包流程分析

```java
protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    Object decoded = decode(ctx, in); // 解码、拆包
    if (decoded != null) {
        out.add(decoded);
    }
}
protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    // 3.2 丢弃模式下的处理
    if (discardingTooLongFrame) {
        long bytesToDiscard = this.bytesToDiscard;
        int localBytesToDiscard = (int) Math.min(bytesToDiscard, in.readableBytes());
        in.skipBytes(localBytesToDiscard);
        bytesToDiscard -= localBytesToDiscard;
        this.bytesToDiscard = bytesToDiscard; // 更新还需要丢弃的字节数

        failIfNecessary(false);
    }

    // 1.计算需要抽取的数据包(帧)长度
    // 可读字节数<长度域末端偏移，说明不足以读取一个数据包，直接返回null
    if (in.readableBytes() < lengthFieldEndOffset) { 
        return null;
    }

    int actualLengthFieldOffset = in.readerIndex() + lengthFieldOffset; // 绝对的长度域偏移
    // 拿到未调整过的帧长度
    long frameLength = getUnadjustedFrameLength(in, actualLengthFieldOffset, lengthFieldLength, byteOrder);

    if (frameLength < 0) { // 帧长度为负值，直接跳过长度域并抛出异常
        in.skipBytes(lengthFieldEndOffset);
        throw new CorruptedFrameException(
                "negative pre-adjustment length field: " + frameLength);
    }

    frameLength += lengthAdjustment + lengthFieldEndOffset; // 调整后的实际帧长度

  	// 调整后的实际帧长度小于长度域末端偏移，说明frameLength有问题，抛出异常
    if (frameLength < lengthFieldEndOffset) { 
        in.skipBytes(lengthFieldEndOffset);
        throw new CorruptedFrameException(
                "Adjusted frame length (" + frameLength + ") is less " +
                "than lengthFieldEndOffset: " + lengthFieldEndOffset);
    }

    // 3.1 丢弃模式下的处理
    if (frameLength > maxFrameLength) {
      	// 忽略frameLength字节, discard表示剩余需忽略的字节数
        long discard = frameLength - in.readableBytes(); 
        tooLongFrameLength = frameLength;

        if (discard < 0) {
            // 表明需要忽略的frameLength字节，均已忽略，后面有可能就是一个合法的数据包
            in.skipBytes((int) frameLength);
        } else {
            discardingTooLongFrame = true; // 进入丢弃模式
            bytesToDiscard = discard; // 还需要丢弃的字节
            in.skipBytes(in.readableBytes());
        }
        failIfNecessary(true);
        return null;
    }

    int frameLengthInt = (int) frameLength;
  	// 可读字节数小于实际帧长度，表示数据不够，返回null，当前啥都不做
    if (in.readableBytes() < frameLengthInt) {
        return null;
    }

    // 2.跳过字节逻辑处理
    if (initialBytesToStrip > frameLengthInt) { // 需要去掉的字节数大于实际帧长度，说明有问题
        in.skipBytes(frameLengthInt); // 跳过当前帧
        throw new CorruptedFrameException(
                "Adjusted frame length (" + frameLength + ") is less " +
                "than initialBytesToStrip: " + initialBytesToStrip);
    }
    in.skipBytes(initialBytesToStrip); // 跳过initialBytesToStrip字节

    // extract frame 抽取帧
    int readerIndex = in.readerIndex();
    int actualFrameLength = frameLengthInt - initialBytesToStrip; // 最终的帧长度
    ByteBuf frame = extractFrame(ctx, in, readerIndex, actualFrameLength);
    in.readerIndex(readerIndex + actualFrameLength); // 当前帧读完，设置读指针
    return frame;
}
```

从上面的代码逻辑可以看出，基于长度域的拆包器LengthFieldBasedFrameDecoder的拆包流程可以分为以下三个步骤：

- 计算需要抽取的数据包长度(帧长度)
- 跳过字节逻辑处理并抽取帧
- 丢弃模式下的处理

#### (a) 计算需要抽取的数据包长度(帧长度)

```java
// 1.计算需要抽取的数据包(帧)长度
if (in.readableBytes() < lengthFieldEndOffset) { // 可读字节数<长度域末端偏移，说明不足以读取一个数据包，直接返回null
    return null;
}

int actualLengthFieldOffset = in.readerIndex() + lengthFieldOffset; // 绝对的长度域偏移
// 拿到未调整过的帧长度
long frameLength = getUnadjustedFrameLength(in, actualLengthFieldOffset, lengthFieldLength, byteOrder);

if (frameLength < 0) { // 帧长度为负值，直接跳过长度域并抛出异常
    in.skipBytes(lengthFieldEndOffset);
    throw new CorruptedFrameException(
            "negative pre-adjustment length field: " + frameLength);
}

frameLength += lengthAdjustment + lengthFieldEndOffset; // 调整后的实际帧长度

if (frameLength < lengthFieldEndOffset) { // 调整后的实际帧长度小于长度域末端偏移，说明frameLength有问题，抛出异常
    in.skipBytes(lengthFieldEndOffset);
    throw new CorruptedFrameException(
            "Adjusted frame length (" + frameLength + ") is less " +
            "than lengthFieldEndOffset: " + lengthFieldEndOffset);
}
```

首先判断当前字节容器可读字节数是否小于长度域末端偏移lengthFieldEndOffset，如果是，则说明不足以读取一个数据包，直接返回null。否则计算绝对的长度域偏移：

```java
int actualLengthFieldOffset = in.readerIndex() + lengthFieldOffset; 
```

然后获取未调整过的帧长度：

```java
long frameLength = getUnadjustedFrameLength(in, actualLengthFieldOffset, lengthFieldLength, byteOrder);
protected long getUnadjustedFrameLength(ByteBuf buf, int offset, int length, ByteOrder order) {
    buf = buf.order(order); // 获取指定字节序的ByteBuf
    long frameLength; // 帧长度
    switch (length) {
    case 1:
        frameLength = buf.getUnsignedByte(offset);
        break;
    case 2:
        frameLength = buf.getUnsignedShort(offset);
        break;
    case 3:
        frameLength = buf.getUnsignedMedium(offset);
        break;
    case 4:
        frameLength = buf.getUnsignedInt(offset);
        break;
    case 8:
        frameLength = buf.getLong(offset);
        break;
    default:
        throw new DecoderException(
                "unsupported lengthFieldLength: " + lengthFieldLength + " (expected: 1, 2, 3, 4, or 8)");
    }
    return frameLength;
}
```

如果获取到的帧长度小于0，则字节容器直接跳过长度域并抛出异常。

```java
if (frameLength < 0) { // 帧长度为负值，直接跳过长度域并抛出异常
    in.skipBytes(lengthFieldEndOffset);
    throw new CorruptedFrameException(
            "negative pre-adjustment length field: " + frameLength);
}
```

然后计算实际的帧长度：

```java
frameLength += lengthAdjustment + lengthFieldEndOffset; // 调整后的实际帧长度
// 调整后的实际帧长度小于长度域末端偏移，说明frameLength有问题，抛出异常
if (frameLength < lengthFieldEndOffset) { 
    in.skipBytes(lengthFieldEndOffset);
    throw new CorruptedFrameException(
            "Adjusted frame length (" + frameLength + ") is less " +
            "than lengthFieldEndOffset: " + lengthFieldEndOffset);
}
```

计算实际帧长度时，考虑了lengthAdjustment和lengthFieldEndOffset，得到一个完整的业务数据包的帧长度。

至此，数据包长度计算完毕。

#### (b) 跳过字节逻辑处理并抽取帧

假设第一个步骤计算出的数据报帧长度没有超过最大帧长度maxFrameLength，则进入下面的处理。

```java
// never overflows because it's less than maxFrameLength
int frameLengthInt = (int) frameLength;
if (in.readableBytes() < frameLengthInt) { // 可读字节数小于实际帧长度，表示数据不够，返回null，当前啥都不做
    return null;
}

// 2.跳过字节逻辑处理
if (initialBytesToStrip > frameLengthInt) { // 需要去掉的字节数大于实际帧长度，说明有问题
    in.skipBytes(frameLengthInt); // 跳过当前帧
    throw new CorruptedFrameException(
            "Adjusted frame length (" + frameLength + ") is less " +
            "than initialBytesToStrip: " + initialBytesToStrip);
}
in.skipBytes(initialBytesToStrip); // 跳过initialBytesToStrip字节

// extract frame 抽取帧
int readerIndex = in.readerIndex();
int actualFrameLength = frameLengthInt - initialBytesToStrip; // 最终的帧长度
ByteBuf frame = extractFrame(ctx, in, readerIndex, actualFrameLength);
in.readerIndex(readerIndex + actualFrameLength); // 当前帧读完，设置读指针
return frame;
```

首先判断当前可读字节数是否小于帧长度，如果是，则表示字节容器数据不够，返回null，当前啥都不做。

然后判断initialBytesToStrip是否大于帧长度，如果是，说明当前帧长度有问题，直接跳过当前帧长度、抛出异常。否则表示帧长度正常，字节容器跳过initialBytesToStrip字节。接下来进行抽取帧操作。

首先计算出最终的帧长度(减去initialBytesToStrip)：

```java
int readerIndex = in.readerIndex();
int actualFrameLength = frameLengthInt - initialBytesToStrip; // 最终的帧长度
```

接着抽取帧：

```java
protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
    return buffer.retainedSlice(index, length); // retainedSlice方法不修改读写指针，引用计数+1
}
```

抽取帧之后，设置字节容器读指针并返回该帧数据：

```java
in.readerIndex(readerIndex + actualFrameLength); // 当前帧读完，设置读指针
```

#### (c) 丢弃模式下的处理

如果第一步计算得到的帧长度大于最大帧长度maxFrameLength，则需要进行字节丢弃处理。

```java
// 3.1 丢弃模式下的处理
if (frameLength > maxFrameLength) {
		// 忽略frameLength字节, discard表示剩余需忽略的字节数
    long discard = frameLength - in.readableBytes(); 
    tooLongFrameLength = frameLength;

    if (discard < 0) {
        // 表明需要忽略的frameLength字节，均已忽略，后面有可能就是一个合法的数据包
        in.skipBytes((int) frameLength); 
    } else {
        discardingTooLongFrame = true; // 进入丢弃模式
        bytesToDiscard = discard; // 还需要丢弃的字节
        in.skipBytes(in.readableBytes());
    }
    failIfNecessary(true);
    return null;
}
```

此时需要丢弃frameLength长度的字节。

`long discard = frameLength - in.readableBytes();`表示丢弃掉`in.readableBytes()`字节，还需要丢弃的字节数。

如果`discard < 0`，则当前可读的字节数大于需要丢弃的字节数frameLength，则字节容器直接跳过frameLength字节；否则，表示字节容器跳过当前可读字节数还不够，则进入丢弃模式。

```java
discardingTooLongFrame = true; // 进入丢弃模式
bytesToDiscard = discard; // 还需要丢弃的字节
in.skipBytes(in.readableBytes());
```

然后调用failIfNecessary(true)方法：

```java
private void failIfNecessary(boolean firstDetectionOfTooLongFrame) {
    if (bytesToDiscard == 0) { // 当前还需要忽略的字节数为0，进入非丢弃模式
        long tooLongFrameLength = this.tooLongFrameLength;
        this.tooLongFrameLength = 0;
        discardingTooLongFrame = false; // 进入非丢弃模式
        // 如果设置快速失败为false，或者设置了快速失败为true并且是第一次检测到大包错误，抛出异常，让handler去处理
        if (!failFast ||
            failFast && firstDetectionOfTooLongFrame) {
            fail(tooLongFrameLength);
        }
    } else {
        // Keep discarding and notify handlers if necessary. 继续忽略剩余的字节数
        // 如果设置了快速失败为true，并且是第一次检测到大包错误，抛出异常，让handler去处理
        if (failFast && firstDetectionOfTooLongFrame) {
            fail(tooLongFrameLength);
        }
    }
}
```

如果当前还需要丢弃的字节数为0，进入非丢弃模式，默认failfast为true，则调用fail(tooLongFrameLength)抛出异常:

```java
private void fail(long frameLength) {
    if (frameLength > 0) {
        throw new TooLongFrameException(
                        "Adjusted frame length exceeds " + maxFrameLength +
                        ": " + frameLength + " - discarded"); // 丢弃
    } else {
        throw new TooLongFrameException(
                        "Adjusted frame length exceeds " + maxFrameLength +
                        " - discarding");
    }
}
```

如果当前还需要丢弃的字节数>0，则表示继续丢弃剩余的字节数，默认failfast为true，则调用fail(tooLongFrameLength)抛出异常。

下面再来看decode()方法开头的部分代码，丢弃模式下处理的第二部分：

```java
// 3.2 丢弃模式下的处理
if (discardingTooLongFrame) {
    long bytesToDiscard = this.bytesToDiscard;
    int localBytesToDiscard = (int) Math.min(bytesToDiscard, in.readableBytes());
    in.skipBytes(localBytesToDiscard);
    bytesToDiscard -= localBytesToDiscard;
    this.bytesToDiscard = bytesToDiscard; // 更新还需要丢弃的字节数

    failIfNecessary(false);
}
```

如果进入decode()方法时已处于丢弃字节模式，则将还需丢弃的字节数`this.bytesToDiscard`与字节容器可读字节数比较，取较小值，使得字节容器跳过该值、丢弃对应字节数，并更新还需要丢弃的字节数。最后调用failIfNecessary(false)判断是否退出丢弃模式：

```java
private void failIfNecessary(boolean firstDetectionOfTooLongFrame) {
    if (bytesToDiscard == 0) { // 当前还需要忽略的字节数为0，进入非丢弃模式
        // Reset to the initial state and tell the handlers that
        // the frame was too large.
        long tooLongFrameLength = this.tooLongFrameLength;
        this.tooLongFrameLength = 0;
        discardingTooLongFrame = false; // 进入非丢弃模式
        // 如果设置快速失败为false，或者设置了快速失败为true并且是第一次检测到大包错误，抛出异常，让handler去处理
        if (!failFast ||
            failFast && firstDetectionOfTooLongFrame) {
            fail(tooLongFrameLength);
        }
    } else {
        // Keep discarding and notify handlers if necessary. 继续忽略剩余的字节数
        // 如果设置了快速失败为true，并且是第一次检测到大包错误，抛出异常，让handler去处理
        if (failFast && firstDetectionOfTooLongFrame) {
            fail(tooLongFrameLength);
        }
    }
}
```

如果还需丢弃的字节数为0，则表示可与退出丢弃模式，进入正常模式。否则，继续丢弃字节。

以上是丢弃模式下的处理逻辑，至此LengthFieldBasedFrameDecoder拆包流程分析完毕。

## 三、面试问题

### 1.拆包器抽象的拆包过程

根据上面的分析，可知拆包过程分为下面4步：

- 累加字节流
- 调用子类的decode方法进行解析
- 清理字节容器
- 将解析到的ByteBuf向下传播

### 2.Netty开箱即用的拆包器

有FixedLengthFrameDecoder、LineBasedFrameDecoder、DelimiterBasedFrameDecoder和LengthFieldBasedFrameDecoder等。

## 参考文章

- [netty源码分析之拆包器的奥秘](https://www.jianshu.com/p/dc26e944da95)
- [https://www.jianshu.com/p/a0a51fd79f62](https://www.jianshu.com/p/a0a51fd79f62)

