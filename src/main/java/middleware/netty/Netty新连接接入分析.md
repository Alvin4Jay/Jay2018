# Netty新连接接入分析

本篇文章介绍Netty新连接接入时的处理逻辑，包括检测新连接、创建NioSocketCHannel、新连接分配NioEventLoop并注册到Selector、向Selector注册读事件等。阅读本文前，最好先了解[Netty服务端启动流程分析](https://xuanjian1992.top/2019/07/28/Netty%E6%9C%8D%E5%8A%A1%E7%AB%AF%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B%E5%88%86%E6%9E%90/)、[Netty NioEventLoop分析](https://xuanjian1992.top/2019/08/19/Netty-NioEventLoop%E5%88%86%E6%9E%90/)这两篇文章，熟悉一下Netty服务端启动流程以及Netty NIO线程(Reactor线程)所做的事情。

## 一、检测新连接

当Netty服务端启动之后，服务端的Channel已经注册到了Boss NIO线程中，Boss NIO线程不断检测新的Accept事件出现。

```java
// NioEventLoop类
private void processSelectedKey(SelectionKey k, AbstractNioChannel ch) {
    final AbstractNioChannel.NioUnsafe unsafe = ch.unsafe(); // 与Channel绑定的Unsafe对象

    try {
        int readyOps = k.readyOps(); // 就绪的事件集

        if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
            // 对于NioServerSocketChannel，是NioMessageUnsafe.read()
            unsafe.read();
            if (!ch.isOpen()) {
                // Connection already closed - no need to handle write.
                return;
            }
        }
    } catch (CancelledKeyException ignored) {
        unsafe.close(unsafe.voidPromise()); // 出现异常，关闭Channel
    }
}
```

当Boss NIO线程轮询到Accept事件时，即`(readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0`，说明有新的连接进入，此时将调用Channel的 `unsafe`来进行实际的操作。这里的`unsafe`是`NioMessageUnsafe`。

## 二、创建NioSocketChannel

下面进入到NioMessageUnsafe.read()方法:

```java
public void read() {
    assert eventLoop().inEventLoop();
    final ChannelConfig config = config();
    final ChannelPipeline pipeline = pipeline();
    // allocHandle: AdaptiveRecvByteBufAllocator.HandleImpl 继承 MaxMessageHandle
    // 用于控制新连接接入速度(消息读取速度)
    final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle(); 
    allocHandle.reset(config);

    do {
        int localRead = doReadMessages(readBuf); // 接收新连接
        if (localRead == 0) {
            break; // 没有可以继续接收的连接了，退出
        }
        if (localRead < 0) {
            closed = true;
            break;
        }

        allocHandle.incMessagesRead(localRead); // 读到的消息计数+1
    // allocHandle: AdaptiveRecvByteBufAllocator.HandleImpl
    } while (allocHandle.continueReading()); 

    int size = readBuf.size();
    for (int i = 0; i < size; i ++) {
        readPending = false;
        pipeline.fireChannelRead(readBuf.get(i)); // msg: NioSocketChannel 发送channelRead事件
    }
    readBuf.clear();
    allocHandle.readComplete();
    pipeline.fireChannelReadComplete(); // 发送channelReadComplete事件
}
```

read()方法首先获取服务端Channel的ChannelConfig、ChannelPipeline和RecvByteBufAllocator.Handle。然后调用doReadMessages()方法接收一个个新连接，然后循环调用 pipeline.fireChannelRead()方法，将得到的每一个新连接交给服务端pipeline处理。下面首先看doReadMessages()方法接收新连接的过程:

### 1.doReadMessages

```java
protected int doReadMessages(List<Object> buf) throws Exception {
    SocketChannel ch = javaChannel().accept(); // 调用JDK ServerSocketChannel.accept()方法接受连接

    try {
        if (ch != null) {
            // 将JDK连接的SocketChannel封装成Netty NioSocketChannel
            buf.add(new NioSocketChannel(this, ch)); 
            return 1; // 成功接收连接，返回1
        }
    } catch (Throwable t) {
        logger.warn("Failed to create a new channel from an accepted socket.", t);

        try {
            ch.close();
        } catch (Throwable t2) {
            logger.warn("Failed to close a socket.", t2);
        }
    }

    return 0;
}
```

doReadMessages()方法中Netty Boss NIO线程先调用javaChannel()方法获取JDK ServerSocketChannel，然后调用其accept()方法接收客户端新连接，得到JDK SocketChannel。接着将JDK SocketChannel封装成Netty NioSocketChannel。

### 2.new NioSocketChannel(Channel, SocketChannel)

下面看创建NioSocketChannel的过程。

```java
// doReadMessages()方法部分代码
if (ch != null) {
    buf.add(new NioSocketChannel(this, ch)); // 将JDK连接的SocketChannel封装成Netty NioSocketChannel
    return 1; // 成功接收连接，返回1
}
```

创建NioSocketChannel时，传入NioServerSocketChannel和JDK SocketChannel。

```java
public NioSocketChannel(Channel parent, SocketChannel socket) {
    super(parent, socket);
    config = new NioSocketChannelConfig(this, socket.socket());
}
```

> (a)调用父类构造器

```java
protected AbstractNioByteChannel(Channel parent, SelectableChannel ch) {
    super(parent, ch, SelectionKey.OP_READ);
}
```

首先调用父类AbstractNioByteChannel的构造器，AbstractNioByteChannel的构造器中调用了父类AbstractNioChannel的构造器，并传入了`SelectionKey.OP_READ`事件。

```java
protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
    super(parent);
    this.ch = ch; // JDK Channel
    this.readInterestOp = readInterestOp;
    try {
        // 配置为非阻塞模式
        ch.configureBlocking(false);
    } catch (IOException e) {
        // 省略...
    }
}
```

AbstractNioChannel构造器中主要保存了JDK SocketChannel到`ch`变量并保存了感兴趣的事件`readInterestOp`，即`SelectionKey.OP_READ`。同时配置JDK SocketChannel为非阻塞模式。

然后再看父类AbstractChannel的构造器:

```java
protected AbstractChannel(Channel parent) {
    this.parent = parent;
    id = newId(); // 唯一标识符
    unsafe = newUnsafe(); // TCP读写相关 客户端channel: NioSocketChannelUnsafe; 服务端channel: NioMessageUnsafe
    pipeline = newChannelPipeline(); // DefaultChannelPipeline
}
```

AbstractChannel构造器中主要保存了父Channel，即NioServerSocketChannel。同时创建了NioSocketChannel的三大组件DefaultChannelId、NioSocketChannelUnsafe、DefaultChannelPipeline。其中NioSocketChannelUnsafe继承自父类NioByteUnsafe。

> (b)创建NioSocketChannel的NioSocketChannelConfig对象

NioSocketChannel在调用父类构造器配置相关参数和创建相关组件之后，接着创建NioSocketChannelConfig配置对象，传入NioSocketChannel对象本身和JDK SocketChannel对应的Socket对象。

```java
config = new NioSocketChannelConfig(this, socket.socket());

private final class NioSocketChannelConfig  extends DefaultSocketChannelConfig {
    private NioSocketChannelConfig(NioSocketChannel channel, Socket javaSocket) {
        super(channel, javaSocket);
    }

    @Override
    protected void autoReadCleared() {
        clearReadPending();
    }
}
```

NioSocketChannelConfig继承自DefaultSocketChannelConfig，在创建实例时调用父类DefaultSocketChannelConfig的构造器。

```java
public DefaultSocketChannelConfig(SocketChannel channel, Socket javaSocket) {
    super(channel);
    if (javaSocket == null) {
        throw new NullPointerException("javaSocket");
    }
    this.javaSocket = javaSocket;

    // Enable TCP_NODELAY by default if possible.
    if (PlatformDependent.canEnableTcpNoDelayByDefault()) {
        try {
            setTcpNoDelay(true); // 禁用Nagle算法
        } catch (Exception e) {
            // Ignore.
        }
    }
}
```

DefaultSocketChannelConfig构造器中主要做的事情是保存Socket对象到`javaSocket`变量，同时调用` setTcpNoDelay(true)`方法禁用Nagle算法。

```java
public SocketChannelConfig setTcpNoDelay(boolean tcpNoDelay) {
    try {
        javaSocket.setTcpNoDelay(tcpNoDelay); // 禁用Nagle算法
    } catch (SocketException e) { 
        throw new ChannelException(e); 
    }
    return this;
}
// Socket类
public void setTcpNoDelay(boolean on) throws SocketException {
    if (isClosed())
        throw new SocketException("Socket is closed");
    getImpl().setOption(SocketOptions.TCP_NODELAY, Boolean.valueOf(on));
}
```

在DefaultSocketChannelConfig构造器中，同时调用了父类的构造器:

```java
public DefaultChannelConfig(Channel channel) {
    this(channel, new AdaptiveRecvByteBufAllocator());
}
protected DefaultChannelConfig(Channel channel, RecvByteBufAllocator allocator) {
    setRecvByteBufAllocator(allocator, channel.metadata());
    this.channel = channel;
}
```

DefaultChannelConfig构造器中保存了NioSocketChannel对象，同时调用了setRecvByteBufAllocator()方法。

```java
private void setRecvByteBufAllocator(RecvByteBufAllocator allocator, ChannelMetadata metadata) {
    if (allocator instanceof MaxMessagesRecvByteBufAllocator) {
        // metadata.defaultMaxMessagesPerRead(): 16
        ((MaxMessagesRecvByteBufAllocator) allocator).maxMessagesPerRead(metadata.defaultMaxMessagesPerRead());
    } else if (allocator == null) {
        throw new NullPointerException("allocator");
    }
    rcvBufAllocator = allocator;
}
```

由于allocator是AdaptiveRecvByteBufAllocator实例，实现了MaxMessagesRecvByteBufAllocator接口，因此会调用MaxMessagesRecvByteBufAllocator的maxMessagesPerRead()方法，将maxMessagesPerRead设置为16。同时保存该allocator实例。

至此，新连接NioSocketChannel创建结束。下面根据[Netty服务端启动流程分析](https://xuanjian1992.top/2019/07/28/Netty%E6%9C%8D%E5%8A%A1%E7%AB%AF%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B%E5%88%86%E6%9E%90/)、[Netty NioEventLoop分析](https://xuanjian1992.top/2019/08/19/Netty-NioEventLoop%E5%88%86%E6%9E%90/)这两篇文章的描述以及上面的介绍，总结一下Netty Channel的分类。

### 3.Netty Channel分类

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/Netty%E6%96%B0%E8%BF%9E%E6%8E%A5%E6%8E%A5%E5%85%A5-1.jpg)

分析服务端NioServerSocketChannel和客户端NioSocketChannel的类继承关系，可得如下的几点:

- Channel是IO读写、连接、绑定的组件抽象；
- AbstractChannel是Channel的骨架实现；
- AbstractNioChannel基于Selector实现IO事件的处理；
- AbstractChannel保存了Channel的公共组件ChannelId、Unsafe、DefaultChannelPipeline对象，其中服务端NioServerSocketChannel对应的Unsafe实例是NioMessageUnsafe，客户端NioSocketChannel对应的Unsafe实例是NioSocketChannelUnsafe，继承自NioByteUnsafe。
- 服务端NioServerSocketChannel对应的ChannelConfig实例是NioServerSocketChannelConfig，客户端NioSocketChannel对应的ChannelConfig实例是NioSocketChannelConfig。

## 三、新连接分配NioEventLoop并注册到Selector

再来看NioMessageUnsafe的read()方法:

```java
public void read() {
    assert eventLoop().inEventLoop();
    final ChannelConfig config = config();
    final ChannelPipeline pipeline = pipeline();
    // allocHandle: AdaptiveRecvByteBufAllocator.HandleImpl 继承 MaxMessageHandle
    // 控制连接接入速度(消息读取速度)
    final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle(); 
    allocHandle.reset(config);

    do {
        int localRead = doReadMessages(readBuf); // 接收新连接
        if (localRead == 0) {
            break; // 没有可以继续接收的连接了，退出
        }
        if (localRead < 0) {
            closed = true;
            break;
        }

        allocHandle.incMessagesRead(localRead); // 读到的消息计数+1
    // allocHandle: AdaptiveRecvByteBufAllocator.HandleImpl
    } while (allocHandle.continueReading()); 

    int size = readBuf.size();
    for (int i = 0; i < size; i ++) {
        readPending = false;
        pipeline.fireChannelRead(readBuf.get(i)); // msg: NioSocketChannel 发送channelRead事件
    }
    readBuf.clear();
    allocHandle.readComplete();
    pipeline.fireChannelReadComplete(); // 发送channelReadComplete事件
}
```

在doReadMessages()方法接收新连接之后，会调用pipeline.fireChannelRead()方法将接收到的NioSocketChannel交给服务端pipeline处理。此时服务端的pipeline如下:

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/Netty%E6%96%B0%E8%BF%9E%E6%8E%A5%E6%8E%A5%E5%85%A5-2.jpg)

最终channelRead()事件会传播到ServerBootstrapAcceptor.channelRead()方法:

```java
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    final Channel child = (Channel) msg; // NioSocketChannel
    // 添加ChannelInitializer
    child.pipeline().addLast(childHandler);
    // 设置ChannelOption
    for (Entry<ChannelOption<?>, Object> e: childOptions) {
        try {
            if (!child.config().setOption((ChannelOption<Object>) e.getKey(), e.getValue())) {
                logger.warn("Unknown channel option: " + e);
            }
        } catch (Throwable t) {
            logger.warn("Failed to set a channel option: " + child, t);
        }
    }
    // 设置Attribute
    for (Entry<AttributeKey<?>, Object> e: childAttrs) {
        child.attr((AttributeKey<Object>) e.getKey()).set(e.getValue());
    }

    try {
        // NioSocketChannel选择、绑定NioEventLoop并注册到对应的Selector
        childGroup.register(child).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    forceClose(child, future.cause());
                }
            }
        });
    } catch (Throwable t) {
        forceClose(child, t);
    }
}
```

该方法中第一步将msg转为NioSocketChannel，然后添加childHandler到NioSocketChannel的pipeline中。childHandler是用户自定义的ChannelInitializer实现(ChannelHandler)，是服务端ServerBootstrap配置时调用childHandler()方法添加的:

```java
ServerBootstrap b = new ServerBootstrap();
b.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .handler(new ServerHandler())
        .localAddress(new InetSocketAddress(8888))
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childAttr(AttributeKey.newInstance("childAttr"), "childAttrValue")
        .childHandler(new ChannelInitializer<SocketChannel>() { // ChannelInitializer
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new AuthHandler());
            }
        });
```

该ChannelInitializer实现会在其`handlerAdded()`方法调用时，回调其initChannel()方法将自定义的ChannelHandler添加至NioSocketChannel的pipeline，并在完成时删除ChannelInitializer自身。

ServerBootstrapAcceptor.channelRead()方法第二步做的事是将配置的ChannelOptions和Attributes添加到NioSocketChannel中。

最重要的第三步是为NioSocketChannel选择、绑定NioEventLoop并注册到对应的Selector:

```java
// NioSocketChannel选择、绑定NioEventLoop并注册到对应的Selector
childGroup.register(child).addListener(new ChannelFutureListener() {
    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            forceClose(child, future.cause());
        }
    }
});
```

这里调用了childGroup的register()方法，childGroup即Worker NioEventLoopGroup:

```java
// MultithreadEventLoopGroup类
public ChannelFuture register(Channel channel) {
    // next(): 选择NioEventLoop
    return next().register(channel);
}
```

MultithreadEventLoopGroup.register()方法调用next()方法选择一个Worker NioEventLoop，然后调用NioEventLoop.register()方法(以下的逻辑与服务端Channel注册到Boss NioEventLoop的过程类似):

```java
// SingleThreadEventLoop类
public ChannelFuture register(Channel channel) {
    return register(new DefaultChannelPromise(channel, this));
}
public ChannelFuture register(final ChannelPromise promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    // NioSocketChannel: promise.channel().unsafe(): NioSocketChannelUnsafe，继承AbstractUnsafe
    promise.channel().unsafe().register(this, promise);
    return promise;
}

```

这里调用NioSocketChannel对应的NioSocketChannelUnsafe(NioByteUnsafe)的register()方法:

```java
// AbstractUnsafe类
public final void register(EventLoop eventLoop, final ChannelPromise promise) {
    // 绑定NioSocketChannel到EventLoop
    AbstractChannel.this.eventLoop = eventLoop;

    if (eventLoop.inEventLoop()) { // false
        register0(promise);
    } else {
        try {
            // 将任务放入任务队列中，此处会创建Nio线程并启动，然后处理该注册任务。
            eventLoop.execute(new Runnable() {
                @Override
                public void run() {
                    register0(promise);
                }
            });
        } catch (Throwable t) {
            // 省略
        }
    }
}
```

register()方法中首先将NioSocketChannel绑定到Worker NioEventLoop，此时`eventLoop.inEventLoop()`为false，因此将执行else分支的逻辑，将`register0(promise)`逻辑分装成Runnable任务。在调用`eventLoop.execute(...)`时，会创建该Worker NioEventLoop底层的线程FastThreadLocalThread并启动，同时将该任务放入任务队列taskQueue，于是这个已启动的NioEventLoop将处理该任务。下面进入`register0(promise)`逻辑：

```java
private void register0(ChannelPromise promise) {
    boolean firstRegistration = neverRegistered;
    doRegister(); // 1.Channel注册到Selector
    neverRegistered = false;
    registered = true;

    pipeline.invokeHandlerAddedIfNeeded(); // 2.回调handlerAdded方法

    safeSetSuccess(promise); // 设置promise为已完成状态
    pipeline.fireChannelRegistered(); // 3.回调channelRegistered方法
    if (isActive()) { // true
        if (firstRegistration) {
            pipeline.fireChannelActive(); // 4.NioSocketChannel注册读事件
        } else if (config().isAutoRead()) {
            beginRead();
        }
    }
}
```

以上register0()方法为简化后的代码，主要做了4件事。

> 1.将NioSocketChannel注册到Selector

```java
protected void doRegister() throws Exception {
    boolean selected = false;
    for (;;) {
        try {
            // 注册到Selector
            // javaChannel(): JDK SocketChannel, this: NioSocketChannel,
            // ops:0, 表示不关心任何事件(这里使用0，主要是考虑到AbstractNioChannel有服务端和客户端
          	// 两个实现，每个实现关心的事件不同。这里先注册0，然后在后续pipeline回调channelActive事件
          	// 的时候向Selector注册Channel READ事件)
            selectionKey = javaChannel().register(eventLoop().selector, 0, this);
            return;
        } catch (CancelledKeyException e) {
            if (!selected) {
                eventLoop().selectNow();
                selected = true;
            } else {
                throw e;
            }
        }
    }
}
```

这里调用doRegister()方法，将JDK SocketChannel注册到Selector，同时attachment为NioSocketChannel对象。并且

注册时设置感兴趣的事件集为0，后续会在调用pipeline.channelActive()方法时向Selector注册该NioSocketChannel的SelectionKey.OP_READ读事件。

> 2.回调ChannelInitializer.handlerAdded方法

第二步调用`pipeline.invokeHandlerAddedIfNeeded()`代码，回调上面所述的ChannelInitializer的handlerAdded()方法:

```java
public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    if (ctx.channel().isRegistered()) {
        initChannel(ctx);
    }
}
private boolean initChannel(ChannelHandlerContext ctx) throws Exception {
    if (initMap.putIfAbsent(ctx, Boolean.TRUE) == null) { // Guard against re-entrance.
        try {
            initChannel((C) ctx.channel()); // 用户实现
        } catch (Throwable cause) {
            exceptionCaught(ctx, cause);
        } finally {
            remove(ctx); // 移除ChannelInitializer自身
        }
        return true;
    }
    return false;
}
private void remove(ChannelHandlerContext ctx) {
    try {
        ChannelPipeline pipeline = ctx.pipeline();
        if (pipeline.context(this) != null) {
            pipeline.remove(this);
        }
    } finally {
        initMap.remove(ctx);
    }
}
```

可以看到，handlerAdded()方法最终调用到initChannel(Channel)方法，该方法由用户自定义实现，用于向客户端Channe对应的pipeline添加ChannelHander，并在添加完成后调用remove方法移除ChannelInitializer自身。

> 3.回调ChannelHandler.channelRegistered方法

ChannelHandler.channelRegistered方法的回调逻辑比较简单，这里不再介绍。

> 4.NioSocketChannel向Selector注册读事件

```java
// AbstractUnsafe.register0()部分代码
if (isActive()) { // true
    if (firstRegistration) {
        pipeline.fireChannelActive(); // 4.NioSocketChannel注册读事件
    } else if (config().isAutoRead()) {
        beginRead();
    }
}
```

这里首先调用isActive()方法判断NioSocketChannel是否已经激活:

```java
public boolean isActive() {
    SocketChannel ch = javaChannel();
    return ch.isOpen() && ch.isConnected();
}
```

这里isActive()方法返回true，并且firstRegistration为true，表示NioSocketChannel是第一次注册，因此下面将调用`pipeline.fireChannelActive();`代码。

## 四、NioSocketChannel向Selector注册读事件

```java
// AbstractUnsafe.register0()部分代码
if (isActive()) { // true
    if (firstRegistration) {
        pipeline.fireChannelActive(); // 4.NioSocketChannel注册读事件
    } else if (config().isAutoRead()) {
        beginRead();
    }
}
```

这里调用`pipeline.fireChannelActive();`代码。假设用户通过ChannelInitializer添加了一个`ServerHandler`处理器，该处理器的`channelActive()`方法如下：

```java
public void channelActive(ChannelHandlerContext ctx) {
    System.out.println("channelActive");
    ctx.fireChannelActive(); // 向后传播active事件
}
```

此时该客户端NioSocketChannel的pipeline如下:

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/Netty%E6%96%B0%E8%BF%9E%E6%8E%A5%E6%8E%A5%E5%85%A5-3.jpg)

因此，channelActive事件将首先经过HeadContext。

```java
// DefaultPipeline
public final ChannelPipeline fireChannelActive() {
    AbstractChannelHandlerContext.invokeChannelActive(head);
    return this;
}
```

```java
// AbstractChannelHandlerContext
static void invokeChannelActive(final AbstractChannelHandlerContext next) {
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) { // true
        next.invokeChannelActive();
    } else {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                next.invokeChannelActive();
            }
        });
    }
}
```

上面的next是HeadContext，且`executor.inEventLoop()`为true，则执行`next.invokeChannelActive();`代码。

```java
// HeadContext
private void invokeChannelActive() {
    if (invokeHandler()) {
        try {
            ((ChannelInboundHandler) handler()).channelActive(this);
        } catch (Throwable t) {
            notifyHandlerException(t);
        }
    } else {
        fireChannelActive();
    }
}
```

这里将调用HeadContext的channelActive()方法:

```java
// HeadContext
public void channelActive(ChannelHandlerContext ctx) throws Exception {
    ctx.fireChannelActive();

    readIfIsAutoRead();
}
```

HeadContext首先将channelActive事件传播给后面的ChannelHandler，如ServerHandler、TailContext，然后调用`readIfIsAutoRead()`方法:

```java
// HeadContext
private void readIfIsAutoRead() {
    if (channel.config().isAutoRead()) { // true
        channel.read(); // 开始读取数据
    }
}
```

默认情况下`channel.config().isAutoRead()`为true，因此调用`channel.read()`方法:

```java
// AbstractChannel
public Channel read() {
    pipeline.read();
    return this;
}
```

`channel.read()`方法将调动`pipeline.read()`方法:

```java
// DefaultPipeline
public final ChannelPipeline read() {
    tail.read();
    return this;
}
```

`DefaultPipeline.read()`方法从尾部TailContext开始调用read()方法:

```java
// TailContext
public ChannelHandlerContext read() {
    final AbstractChannelHandlerContext next = findContextOutbound();
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
        next.invokeRead();
    } else {
        Runnable task = next.invokeReadTask;
        if (task == null) {
            next.invokeReadTask = task = new Runnable() {
                @Override
                public void run() {
                    next.invokeRead();
                }
            };
        }
        executor.execute(task);
    }

    return this;
}
```

`TailContext.read()`方法调用findContextOutbound()方法获取outbound ChannelHandler。

```java
private AbstractChannelHandlerContext findContextOutbound() {
    AbstractChannelHandlerContext ctx = this; // this: TailContext
    do {
        ctx = ctx.prev;
    } while (!ctx.outbound);
    return ctx;
}
```

findContextOutbound()将找到HeadContext ChannelHandler，下面执行`(HeadContext)next.invokeRead();`:

```java
// HeadContext
private void invokeRead() {
    if (invokeHandler()) {
        try {
            ((ChannelOutboundHandler) handler()).read(this); // handler(): HeadContext
        } catch (Throwable t) {
            notifyHandlerException(t);
        }
    } else {
        read();
    }
}
```

```java
// HeadContext
public void read(ChannelHandlerContext ctx) {
    unsafe.beginRead();
}
```

`HeadContext.read()`方法将调用NioSocketChannel对应的Unsafe实例的beginRead()方法执行实际的读取操作。

```java
// AbstractUnsafe
public final void beginRead() {
    assertEventLoop();

    if (!isActive()) {
        return;
    }

    try {
        doBeginRead();
    } catch (final Exception e) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                pipeline.fireExceptionCaught(e);
            }
        });
        close(voidPromise());
    }
}
```

beginRead()方法中调用了doBeginRead()方法:

```java
// AbstractNioChannel
protected void doBeginRead() throws Exception {
    // Channel.read() or ChannelHandlerContext.read() was called
    final SelectionKey selectionKey = this.selectionKey;
    if (!selectionKey.isValid()) {
        return;
    }

    readPending = true;

    final int interestOps = selectionKey.interestOps();
    if ((interestOps & readInterestOp) == 0) {
        selectionKey.interestOps(interestOps | readInterestOp); // 设置感兴趣的Read事件
    }
}
```

doBeginRead()实际调用了AbstractNioChannel的doBeginRead()方法，从这个方法可以看出，这里NioSocketChannel向Selector注册了`SelectionKey.OP_READ`事件(`readInterestOp=SelectionKey.OP_READ`)。

至此，NioSocketChannel向Selector注册读事件的过程分析完毕，可以看到`channel.read()`事件是从NioSocketChannel的pipeline尾部向头部传播的。

## 五、问题

- Netty是在哪里检测新连接接入的？

   服务端Channel Boss NioEventLoop轮询出`SelectionKey.OP_ACCEPT`IO事件，处理该`SelectionKey.OP_ACCEPT`IO事件的时候。

- 新连接NioSocketChannel是怎样注册到NioEventLoop线程的？

  由Boss NioEventLoop线程使用EventExecutorChooser选择Worker NioEventLoop，并将NioSocketChannel注册到Worker NioEventLoop对应的Selector上，并注册读事件`SelectionKey.OP_READ`。

## 参考文章

- [Netty服务端启动流程分析](https://xuanjian1992.top/2019/07/28/Netty服务端启动流程分析/)
- [Netty NioEventLoop分析](https://xuanjian1992.top/2019/08/19/Netty-NioEventLoop%E5%88%86%E6%9E%90/)
- [netty源码分析之新连接接入全解析](https://www.jianshu.com/p/0242b1d4dd21)