# Netty服务端启动流程分析

Netty服务端启动过程主要分为四个步骤：创建NioServerSocketChannel、初始化NioServerSocketChannel、注册NioServerSocketChannel到Selector及绑定端口。下面以如下的Netty服务端启动Demo为例，展开Netty服务端启动流程的分析。

```java
public final class Server {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new ServerHandler())
                    .localAddress(new InetSocketAddress(8888))
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childAttr(AttributeKey.newInstance("childAttr"), "childAttrValue")
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            // ch.pipeline().addLast(new AuthHandler());
                        }
                    });

            ChannelFuture f = b.bind().sync(); // 服务端启动入口: bind操作

            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

// 对比JDK NIO编程：
public class NIOServer {
    public static void main(String[] args) throws IOException {
        Selector serverSelector = Selector.open();
        Selector clientSelector = Selector.open();

        new Thread(() -> {
            try {
                // 对应Netty服务端启动的4个步骤
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
                serverSocketChannel.socket().bind(new InetSocketAddress(8000));

                while (true) {
                    // 监测是否有新的连接，这里的1指的是阻塞的时间为1ms
                    if (serverSelector.select(1) > 0) {
                        Set<SelectionKey> selectionKeys = serverSelector.selectedKeys();
                        Iterator<SelectionKey> keys = selectionKeys.iterator();
                        while (keys.hasNext()) {
                            SelectionKey key = keys.next();
                            if (key.isAcceptable()) {
                                try {
                                    // (1) 每来一个新连接，不需要创建一个线程，而是直接注册到clientSelector
                                    SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
                                    clientChannel.configureBlocking(false);
                                    clientChannel.register(clientSelector, SelectionKey.OP_READ);
                                } finally {
                                    // 删除已处理的SelectionKey
                                    keys.remove();
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                while (true) {
                    // (2) 批量轮询是否有哪些连接有数据可读，这里的1指的是阻塞的时间为1ms
                    if (clientSelector.select(1) > 0) {
                        Set<SelectionKey> selectionKeys = clientSelector.selectedKeys();
                        Iterator<SelectionKey> keys = selectionKeys.iterator();
                        while (keys.hasNext()) {
                            SelectionKey key = keys.next();
                            if (key.isReadable()) {
                                try {
                                    SocketChannel clientChannel = (SocketChannel) key.channel();
                                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                    // (3) 读取数据以块为单位批量读取
                                    clientChannel.read(byteBuffer);
                                    // 写模式切换为读模式
                                    byteBuffer.flip();
                                    System.out.println(Charset.defaultCharset().newDecoder().decode(byteBuffer).toString());
                                } finally {
                                    // 删除已处理的SelectionKey
                                    keys.remove();
                                    key.interestOps(SelectionKey.OP_READ);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }
}
```

## 一、创建NioServerSocketChannel

下面从入口函数AbstractBootstrap.bind()开始分析。

```java
public ChannelFuture bind() {
    validate();
    SocketAddress localAddress = this.localAddress;
    if (localAddress == null) {
        throw new IllegalStateException("localAddress not set");
    }
    return doBind(localAddress);
}

private ChannelFuture doBind(final SocketAddress localAddress) {
    // 1.创建NioServerSocketChannel, 2.初始化NioServerSocketChannel, 3.注册到Selector
    final ChannelFuture regFuture = initAndRegister();
    final Channel channel = regFuture.channel();
    if (regFuture.cause() != null) {
        return regFuture;
    }

    if (regFuture.isDone()) { // 若注册到Selector的操作已完成，在这个分支4.绑定端口
        // At this point we know that the registration was complete and successful.
        ChannelPromise promise = channel.newPromise();
        doBind0(regFuture, channel, localAddress, promise);
        return promise;
    } else { // 否则在下面绑定端口
        // Registration future is almost always fulfilled already, but just in case it's not.
        final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
        regFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Throwable cause = future.cause();
                if (cause != null) {
                    promise.setFailure(cause);
                } else {
                    promise.registered();
										// 4.绑定端口
                    doBind0(regFuture, channel, localAddress, promise);
                }
            }
        });
        return promise;
    }
}
```

创建NioServerSocketChannel的逻辑在initAndRegister方法中：

```java
final ChannelFuture initAndRegister() {
    Channel channel = null;
    try {
        channel = channelFactory.newChannel(); // 1.创建服务端Channel
        init(channel); // 2.初始化服务端Channel
    } catch (Throwable t) {
        if (channel != null) {
            channel.unsafe().closeForcibly();
        }
        return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(t);
    }

    // 3.服务端Channel注册到Selector(事件轮询器)
    // config().group(): NioEventLoopGroup
    ChannelFuture regFuture = config().group().register(channel); // DefaultChannelPromise
    if (regFuture.cause() != null) {
        if (channel.isRegistered()) {
            channel.close();
        } else {
            channel.unsafe().closeForcibly();
        }
    }
    return regFuture;
}
```

可以看到，`channel = channelFactory.newChannel();`这句代码创建了NioServerSocketChannel。

```java
// ReflectiveChannelFactory
public T newChannel() {
    try {
        return clazz.newInstance(); // 反射创建Channel
    } catch (Throwable t) {
        throw new ChannelException("Unable to create Channel from class " + clazz, t);
    }
}
```

上述newChannel方法中的clazz是NioServerSocketChannel Class对象，因此这里通过反射调用无参构造器创建了NioServerSocketChannel对象。下面看下NioServerSocketChannel的无参构造器执行的逻辑。

```java
// NioServerSocketChannel
public NioServerSocketChannel() {
    this(newSocket(DEFAULT_SELECTOR_PROVIDER));
}
private static ServerSocketChannel newSocket(SelectorProvider provider) {
    try {
        // 创建JDK NIO ServerSocketChannel
        return provider.openServerSocketChannel();
    } catch (IOException e) {
        throw new ChannelException(
                "Failed to open a server socket.", e);
    }
}
```

构造器中首先调用newSocket方法创建了JDK的ServerSocketChannel，然后继续调用重载的构造方法。

```java
public NioServerSocketChannel(ServerSocketChannel channel) {
    super(null, channel, SelectionKey.OP_ACCEPT);
    // config: TCP参数配置
    config = new NioServerSocketChannelConfig(this, javaChannel().socket());
}
```

重载构造方法中首先使用super调用了父类构造器，然后创建了TCP参数配置实例NioServerSocketChannelConfig。其中的javaChannel()方法返回值就是newSocket方法创建的ServerSocketChannel。下面在深入父类构造器之前，先看下NioServerSocketChannel的类继承结构：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/Netty-Server-Start-1.png?x-oss-process=style/markdown-pic)

从上图可以看出，在`public NioServerSocketChannel(ServerSocketChannel channel) `构造方法中执行`super(null, channel, SelectionKey.OP_ACCEPT);`，会依次调用AbstractNioMessageChannel、AbstractNioCHannel、AbstractChannel的构造器。

```java
// AbstractNioMessageChannel
protected AbstractNioMessageChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
    super(parent, ch, readInterestOp);
}
```

```java
// AbstractNioCHannel
private final SelectableChannel ch; // JDK NIO Channel(ServerSocketChannel)
protected final int readInterestOp; // 感兴趣的事件集

protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
    super(parent);
    this.ch = ch;
    this.readInterestOp = readInterestOp;
    try {
        // Channel配置为非阻塞模式
        ch.configureBlocking(false);
    } catch (IOException e) {
        try {
            ch.close();
        } catch (IOException e2) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "Failed to close a partially initialized socket.", e2);
            }
        }

        throw new ChannelException("Failed to enter non-blocking mode.", e);
    }
}
```

```java
// AbstractChannel
protected AbstractChannel(Channel parent) {
    this.parent = parent;
    id = newId(); // 唯一标识符
    unsafe = newUnsafe(); // TCP读写相关
    pipeline = newChannelPipeline(); // DefaultChannelPipeline
}
```

在`protected AbstractChannel(Channel parent)`构造函数中，会初始化NioServerSocketChannel的唯一标识符id、Unsafe实例NioMessageUnsafe实例和DefaultChannelPipeline实例。其中NioMessageUnsafe和DefaultChannelPipeline实例的构造过程如下：

```java
// AbstractNioMessageChannel
protected AbstractNioUnsafe newUnsafe() {
    return new NioMessageUnsafe();
}
```

```java
// AbstractChannel
protected DefaultChannelPipeline newChannelPipeline() {
    return new DefaultChannelPipeline(this);
}
```

在DefaultChannelPipeline初始化时会初始化两个特殊的ChannelHandlerContext，如下：

```java
protected DefaultChannelPipeline(Channel channel) {
    this.channel = ObjectUtil.checkNotNull(channel, "channel");
    succeededFuture = new SucceededChannelFuture(channel, null);
    voidPromise =  new VoidChannelPromise(channel, true);

    tail = new TailContext(this); // 尾部ChannelHandlerContext
    head = new HeadContext(this); // 头部ChannelHandlerContext

    head.next = tail; // 双向链表
    tail.prev = head;
}
```

以上就分析完了NioServerSocketChannel实例创建的逻辑。

## 二、初始化NioServerSocketChannel

首先再次看下AbstractBootstrap.initAndRegister方法。在创建NioServerSocketChannel之后便是`init(channel);`，初始化NioServerSocketChannel实例。

```java
final ChannelFuture initAndRegister() {
    Channel channel = null;
    try {
        channel = channelFactory.newChannel(); // 1.创建服务端Channel
        init(channel); // 2.初始化服务端Channel
    } catch (Throwable t) {
        if (channel != null) {
            channel.unsafe().closeForcibly();
        }
        return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(t);
    }

    // 3.服务端Channel注册到Selector(事件轮询器)
    // config().group(): NioEventLoopGroup
    ChannelFuture regFuture = config().group().register(channel); // DefaultChannelPromise
    if (regFuture.cause() != null) {
        if (channel.isRegistered()) {
            channel.close();
        } else {
            channel.unsafe().closeForcibly();
        }
    }
    return regFuture;
}
```

该`init(channel);`方法的实现在AbstractBootstrap的子类ServerBootstrap中。

```java
// ServerBootstrap
void init(Channel channel) throws Exception {
    // 1.设置服务端Channel的ChannelOptions和Attributes
    final Map<ChannelOption<?>, Object> options = options0();
    synchronized (options) {
        channel.config().setOptions(options);
    }

    final Map<AttributeKey<?>, Object> attrs = attrs0();
    synchronized (attrs) {
        for (Entry<AttributeKey<?>, Object> e: attrs.entrySet()) {
            @SuppressWarnings("unchecked")
            AttributeKey<Object> key = (AttributeKey<Object>) e.getKey();
            channel.attr(key).set(e.getValue());
        }
    }

    ChannelPipeline p = channel.pipeline(); // Server Channel pipeline
    // 2.得到子Channel的相关配置
    final EventLoopGroup currentChildGroup = childGroup;
    final ChannelHandler currentChildHandler = childHandler;
    final Entry<ChannelOption<?>, Object>[] currentChildOptions;
    final Entry<AttributeKey<?>, Object>[] currentChildAttrs;
    synchronized (childOptions) {
        currentChildOptions = childOptions.entrySet().toArray(newOptionArray(childOptions.size()));
    }
    synchronized (childAttrs) {
        currentChildAttrs = childAttrs.entrySet().toArray(newAttrArray(childAttrs.size()));
    }

    // 3.配置服务端pipeline，添加ChannelInitializer实例
    p.addLast(new ChannelInitializer<Channel>() {
        @Override
        public void initChannel(Channel ch) throws Exception {
            final ChannelPipeline pipeline = ch.pipeline();
            // 添加用户自定义的服务端ChannelHandler --> 通过ServerBootstrap.handler()方法设置(见demo)
            ChannelHandler handler = config.handler();
            if (handler != null) {
                pipeline.addLast(handler);
            }

            ch.eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    // 添加ServerBootstrapAcceptor
                    pipeline.addLast(new ServerBootstrapAcceptor(
                            currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
                }
            });
        }
    });
}
```

可以看到，初始化NioServerSocketChannel的init方法中，主要做了三件事：设置服务端Channel的ChannelOptions和Attributes、得到子Channel的相关配置及配置服务端pipeline，添加ChannelInitializer实例。

前面两个步骤相对简单，这里重点说下第三个步骤——配置服务端pipeline，添加ChannelInitializer实例。在` p.addLast(new ChannelInitializer<Channel>() {...});`这句代码执行时，由于`p`是DefaultChannelPipeline实例，因此调用了DefaultChannelPipeline的add方法添加ChannelInitializer实例。

```java
// DefaultChannelPipeline
public final ChannelPipeline addLast(ChannelHandler... handlers) {
    return addLast(null, handlers);
}
public final ChannelPipeline addLast(EventExecutorGroup executor, ChannelHandler... handlers) {
    if (handlers == null) {
        throw new NullPointerException("handlers");
    }

    for (ChannelHandler h: handlers) {
        if (h == null) {
            break;
        }
        addLast(executor, null, h);
    }

    return this;
}
public final ChannelPipeline addLast(EventExecutorGroup group, String name, ChannelHandler handler) {
    final AbstractChannelHandlerContext newCtx;
    synchronized (this) {
        checkMultiplicity(handler);// 检查是否注解了@Sharable(是否可以在ChannelPipeline之间共享)
				// 新建ChannelHandlerContext
        newCtx = newContext(group, filterName(name, handler), handler); 

        addLast0(newCtx); // 添加到链表最后，tail之前

        // If the registered is false it means that the channel was not registered on an eventloop yet.
        // In this case we add the context to the pipeline and add a task that will call
        // ChannelHandler.handlerAdded(...) once the channel is registered.
        if (!registered) { // 注意这里!!! registered = false
           // 更新handler状态为ADD_PENDING，即ChannelHandler.handlerAdded方法即将被调用。
            newCtx.setAddPending();
            callHandlerCallbackLater(newCtx, true);
            return this;
        }

        EventExecutor executor = newCtx.executor();
        if (!executor.inEventLoop()) {
            newCtx.setAddPending();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    callHandlerAdded0(newCtx);
                }
            });
            return this;
        }
    }
    callHandlerAdded0(newCtx);
    return this;
}
```

由于此时Channel还未注册到EventLoop，因此DefaultChannelPipeline的成员变量registered为false，因此执行如下的逻辑：

```java
if (!registered) { // 注意这里!!!
  	// 更新handler状态为ADD_PENDING，即ChannelHandler.handlerAdded即将被调用。
    newCtx.setAddPending(); 
    callHandlerCallbackLater(newCtx, true);
    return this;
}
```

callHandlerCallbackLater方法逻辑如下：

```java
private void callHandlerCallbackLater(AbstractChannelHandlerContext ctx, boolean added) {
    assert !registered;

    PendingHandlerCallback task = added ? new PendingHandlerAddedTask(ctx) : new PendingHandlerRemovedTask(ctx);
    PendingHandlerCallback pending = pendingHandlerCallbackHead;
    if (pending == null) {
        pendingHandlerCallbackHead = task;
    } else {
        // Find the tail of the linked-list.
        while (pending.next != null) { // 找到链表尾部，插入节点
            pending = pending.next;
        }
        pending.next = task;
    }
}
```

此时动作为添加AbstractChannelHandlerContext实例，因此callHandlerCallbackLater方法将AbstractChannelHandlerContext包装成了PendingHandlerAddedTask实例，并添加到了PendingHandlerCallback实例构造的单向链表上，在Channel注册到EventLoop时会执行该链表中的任务。

说完上述Serverbootstrap.init方法中配置服务端pipeline，添加ChannelInitializer实例的逻辑之后，再来看看ChannelInitializer实例的initChannel回调方法做了哪些事情。

```java
// Serverbootstrap.init()
// 配置服务端pipeline，添加ChannelInitializer实例
p.addLast(new ChannelInitializer<Channel>() {
    @Override
    public void initChannel(Channel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        // 添加用户自定义的服务端ChannelHandler --> 通过ServerBootstrap.handler()方法设置(见demo)
        ChannelHandler handler = config.handler();
        if (handler != null) {
            pipeline.addLast(handler);
        }

        ch.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                // 添加ServerBootstrapAcceptor
                pipeline.addLast(new ServerBootstrapAcceptor(
                        currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
            }
        });
    }
});
```

可以看到在ChannelInitializer的initChannel方法中，主要是添加了用户自定义的ChannelHandler，即用户通过ServerBootstrap.handler()方法设置的ChannelHandler；同时，向NioServerSocketChannel的EventLoop中添加了一个任务，任务是将ServerBootstrapAcceptor(ChannelInboundHandler)添加到当前NioServerSocketChannel的ChannelPipeline中。**ServerBootstrapAcceptor**的作用在后续文章中会分析。

以上便是初始化NioServerSocketChannel的流程。

## 三、注册NioServerSocketChannel到Selector

```java
final ChannelFuture initAndRegister() {
    Channel channel = null;
    try {
        channel = channelFactory.newChannel(); // 1.创建服务端Channel
        init(channel); // 2.初始化服务端Channel
    } catch (Throwable t) {
        if (channel != null) {
            channel.unsafe().closeForcibly();
        }
        return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(t);
    }

    // 3.服务端Channel注册到Selector(事件轮询器)
    // config().group(): NioEventLoopGroup
    ChannelFuture regFuture = config().group().register(channel); // DefaultChannelPromise
    if (regFuture.cause() != null) {
        if (channel.isRegistered()) {
            channel.close();
        } else {
            channel.unsafe().closeForcibly();
        }
    }
    return regFuture;
}
```

在前面两个步骤之后，便是服务端Channel注册到Selector(事件轮询器)的流程。在Channel注册到Selector的过程中，调用了NioEventLoopGroup的register方法。首先来看下NioEventLoopGroup的类继承结构：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/Netty-Server-Start-2.png?x-oss-process=style/markdown-pic)

实际上NioEventLoopGroup的register方法实现定义在父类MultithreadEventLoopGroup中:

```java
// MultithreadEventLoopGroup
public ChannelFuture register(Channel channel) {
    // next(): 选择NioEventLoop
    return next().register(channel);
}
```

该register方法中首先调用了next()方法选择Channel需绑定的EventLoop实例。

```java
// MultithreadEventLoopGroup
public EventLoop next() {
    return (EventLoop) super.next();
}
// MultithreadEventExecutorGroup
public EventExecutor next() {
    return chooser.next();
}
```

其中MultithreadEventExecutorGroup中的chooser是EventExecutorChooserFactory.EventExecutorChooser类型的实例，即EventExecutor选择器，也就是EventLoop选择器。该选择其有两种实现：PowerOfTowEventExecutorChooser和GenericEventExecutorChooser。这两种实现的EventExecutor选择逻辑和区别在后面分析NioEventLoopGroup和NioEventLoop时再详述。总之，经过`chooser.next();`调用后，能返回一个`NioEventLoop`实例。NioEventLoop的类继承结构如下所示：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/Netty-Server-Start-3.png?x-oss-process=style/markdown-pic)

然后调用NioEventLoop.register方法，其实现定义在父类SingleThreadEventLoop中。

```java
// SingleThreadEventLoop
public ChannelFuture register(Channel channel) {
    return register(new DefaultChannelPromise(channel, this));
}
public ChannelFuture register(final ChannelPromise promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    // 服务端promise.channel().unsafe(): NioMessageUnsafe，继承AbstractUnsafe
    promise.channel().unsafe().register(this, promise); // 注册过程异步
    return promise; // 返回DefaultChannelPromise实例
}
```

可以看到，register(ChannelPromise)实际上调用了NioMessageUnsafe的register方法进行注册，该方法定义在父类AbstractUnsafe中：

```java
// AbstractUnsafe
public final void register(EventLoop eventLoop, final ChannelPromise promise) {
    // ...

    // 绑定服务端Channel到EventLoop
    AbstractChannel.this.eventLoop = eventLoop;

    if (eventLoop.inEventLoop()) { // false
        register0(promise);
    } else {
        try {
            // 将任务放入任务队列后，主线程main返回。此处会创建Nio线程并启动，然后处理该注册任务
            eventLoop.execute(new Runnable() {
                @Override
                public void run() {
                    register0(promise);
                }
            });
        } catch (Throwable t) {
            logger.warn(
                    "Force-closing a channel whose registration task was not accepted by an event loop: {}",
                    AbstractChannel.this, t);
            closeForcibly();
            closeFuture.setClosed();
            safeSetFailure(promise, t);
        }
    }
}
```

可以看到在`register(EventLoop eventLoop, final ChannelPromise promise)`中，将传入的eventLoop赋值给了AbstractChannel(NioServerSocketChannel)的eventLoop变量。然后判断当前线程是否在事件循环中：

```java
// AbstractEventExecutor
public boolean inEventLoop() {
    return inEventLoop(Thread.currentThread());
}
// SingleThreadEventExecutor
public boolean inEventLoop(Thread thread) {
    return thread == this.thread; // this.thread目前为null
}
```

由于当前调用register方法的线程是主线程main，因此`eventLoop.inEventLoop()`条件不成立，执行else的逻辑。`eventLoop.execute(new Runnable() {});`这行代码将实际的注册任务添加到了事件循环NioEventLoop中。下面先看下`eventLoop.execute()`的执行逻辑，实现在NioEventLoop的父类SingleThreadEventExecutor中。

```java
// SingleThreadEventExecutor
public void execute(Runnable task) {
    if (task == null) {
        throw new NullPointerException("task");
    }

    boolean inEventLoop = inEventLoop(); // false
    if (inEventLoop) {
        addTask(task);
    } else { // 这里！！！
        startThread(); // 创建Nio线程并启动
        addTask(task); // 添加任务到TaskQueue
        if (isShutdown() && removeTask(task)) {
            reject();
        }
    }

    if (!addTaskWakesUp && wakesUpForTask(task)) {
        wakeup(inEventLoop);
    }
}
```

由于inEventLoop()方法返回false，因此执行else分支的逻辑。首先调用startThread方法，创建Nio线程并启动。

```java
private void startThread() {
    if (STATE_UPDATER.get(this) == ST_NOT_STARTED) {
      	// 设置执行器状态为已启动
        if (STATE_UPDATER.compareAndSet(this, ST_NOT_STARTED, ST_STARTED)) {
            doStartThread(); // 创建Nio线程并启动
        }
    }
}

private void doStartThread() {
    assert thread == null;
    // executor: ThreadPerTaskExecutor，execute方法创建了Nio线程并启动(NioEventLoop)
    executor.execute(new Runnable() {
        @Override
        public void run() {
            // ...
        }
    });
}
```

在doStartThread方法中，executor是ThreadPerTaskExecutor实例(见后续文章关于NioEventLoop的分析)。因此doStartThread方法中`executor.execute(new Runnable() {});`的实现在ThreadPerTaskExecutor中：

```java
public final class ThreadPerTaskExecutor implements Executor {
    private final ThreadFactory threadFactory;

    public ThreadPerTaskExecutor(ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException("threadFactory");
        }
        this.threadFactory = threadFactory;
    }

    @Override
    public void execute(Runnable command) {
        // threadFactory: DefaultThreadFactory，创建线程并启动
        // 每次执行任务，都会创建一个线程实体FastThreadLocalThread
        threadFactory.newThread(command).start();
    }
}
```

可以看到ThreadPerTaskExecutor在每次调用其execute方法时都会创建一个线程实体FastThreadLocalThread(Nio线程)并启动，执行逻辑在`Runnable command`中。在doStartThread方法中，该FastThreadLocalThread线程执行的逻辑是：

```java
// executor: ThreadPerTaskExecutor
executor.execute(new Runnable() {
    @Override
    public void run() {
        thread = Thread.currentThread(); // FastThreadLocalThread
        if (interrupted) {
            thread.interrupt();
        }

        boolean success = false;
        updateLastExecutionTime();
        try {
            SingleThreadEventExecutor.this.run(); // Nio线程核心逻辑在这里
            success = true;
        } catch (Throwable t) {
            logger.warn("Unexpected exception from an event executor: ", t);
        } finally {
            // ...
        }
    }
});
```

FastThreadLocalThread主要是将自己赋值给了成员变量thread，同时调用了` SingleThreadEventExecutor.this.run();`。这句代码是该Nio线程的核心逻辑所在，将在下篇文章分析NioEventLoop时介绍。在startThread方法执行结束后，再来看SingleThreadEventExecutor.execute方法：

```java
// SingleThreadEventExecutor
public void execute(Runnable task) {
    if (task == null) {
        throw new NullPointerException("task");
    }

    boolean inEventLoop = inEventLoop(); // false
    if (inEventLoop) {
        addTask(task);
    } else { // 这里！！！
        startThread(); // 创建Nio线程并启动
        addTask(task); // 添加任务到TaskQueue
        if (isShutdown() && removeTask(task)) {
            reject();
        }
    }

    if (!addTaskWakesUp && wakesUpForTask(task)) {
        wakeup(inEventLoop);
    }
}
```

在创建Nio线程并启动之后，会调用` addTask(task);`添加任务到任务队列：

```java
protected void addTask(Runnable task) {
    if (task == null) {
        throw new NullPointerException("task");
    }
    if (!offerTask(task)) {
        reject(task);
    }
}

final boolean offerTask(Runnable task) {
    if (isShutdown()) {
        reject();
    }
    return taskQueue.offer(task); // taskQueue: MpscQueue，多生产者单消费者队列
}
```

在将task添加到taskQueue之后，再来回看AbstractUnsafe.register方法。

```java
// AbstractUnsafe
public final void register(EventLoop eventLoop, final ChannelPromise promise) {
    // ...

    // 绑定服务端Channel到EventLoop
    AbstractChannel.this.eventLoop = eventLoop;

    if (eventLoop.inEventLoop()) { // false
        register0(promise);
    } else {
        try {
            // 将任务放入任务队列后，主线程main返回。此处会创建Nio线程并启动，然后处理该注册任务
            eventLoop.execute(new Runnable() {
                @Override
                public void run() {
                    register0(promise);
                }
            });
        } catch (Throwable t) {
            logger.warn(
                    "Force-closing a channel whose registration task was not accepted by an event loop: {}",
                    AbstractChannel.this, t);
            closeForcibly();
            closeFuture.setClosed();
            safeSetFailure(promise, t);
        }
    }
}
```

Nio线程将会执行添加到taskQueue的Runnbale任务，即`register0(promise);`。

```java
// AbstractUnsafe
private void register0(ChannelPromise promise) {
    try {
        if (!promise.setUncancellable() || !ensureOpen(promise)) {
            return;
        }
        boolean firstRegistration = neverRegistered; // true
        doRegister(); // 1.Channel注册到Selector
        neverRegistered = false;
        registered = true;

        pipeline.invokeHandlerAddedIfNeeded(); // 2.回调handlerAdded方法 in eventLoop

        safeSetSuccess(promise); // 设置promise为已完成状态
        pipeline.fireChannelRegistered(); // 3.回调channelRegistered方法

      	if (isActive()) { // 此时false，不会回调channelActive方法
            if (firstRegistration) {
                pipeline.fireChannelActive();
            } else if (config().isAutoRead()) {
                beginRead();
            }
        }
    } catch (Throwable t) {
        // Close the channel directly to avoid FD leak.
        closeForcibly();
        closeFuture.setClosed();
        safeSetFailure(promise, t);
    }
}
```

register0方法主要做了以下事：

- Channel注册到Selector；
- 回调handlerAdded方法；
- 设置promise为已完成状态，回调channelRegistered方法。

首先是`doRegister();`的执行：

```java
protected void doRegister() throws Exception {
    boolean selected = false;
    for (;;) {
        try {
            // 注册到Selector
            // javaChannel(): JDK ServerSocketChannel, this: NioServerSocketChannel,
            // ops:0, 表示不关心任何事件(这里使用0，主要是考虑到AbstractNioChannel有服务端和客户端两个实现，每个实现关心的事件不同
            // 这里先注册0，然后在后续服务端绑定bind完成后修改感兴趣的事件)
            selectionKey = javaChannel().register(eventLoop().selector, 0, this);
            return;
        } catch (CancelledKeyException e) {
            // ...
        }
    }
}
```

此处需要注意：在将JDK NIO Channel注册到事件轮询器Selector时，并没有设置感兴趣的事件类型，它会在Channel绑定端口完成的时候进行设置。此外，在Channel注册到事件轮询器Selector时的attachment设置为了this，即本NioServerSocketChannel实例。

Channel注册到Selector之后，会调用pipeline的invokeHandlerAddedIfNeeded方法，回调已注册到ChannelPipeline的ChannelHandler的handlerAdded方法。invokeHandlerAddedIfNeeded方法如下：

```java
final void invokeHandlerAddedIfNeeded() {
    assert channel.eventLoop().inEventLoop(); // 断言当前线程在事件循环中
    if (firstRegistration) {
        firstRegistration = false;
        callHandlerAddedForAllHandlers();
    }
}
```

```java
private void callHandlerAddedForAllHandlers() {
    final PendingHandlerCallback pendingHandlerCallbackHead;
    synchronized (this) {
        assert !registered;

        registered = true;

        pendingHandlerCallbackHead = this.pendingHandlerCallbackHead;
        // Null out so it can be GC'ed.
        this.pendingHandlerCallbackHead = null;
    }

    PendingHandlerCallback task = pendingHandlerCallbackHead;
    while (task != null) {
        task.execute(); // 执行
        task = task.next;
    }
}
```

前面分析初始化NioServerSocketChannel流程的时候，可知向服务端Channel Pipeline添加的ChannelHandler会被包装成PendingHandlerCallback，添加到以pendingHandlerCallbackHead开头的单向链表中。因此，在Channel注册到Selector之后便到了执行这些PendingHandlerCallback的时候。服务端Channel之前已添加的ChannelHandler只有ChannelInitializer实例，如下：

```java
// 3.配置服务端pipeline，添加ChannelInitializer实例
p.addLast(new ChannelInitializer<Channel>() {
    @Override
    public void initChannel(Channel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        // 添加用户自定义的服务端ChannelHandler --> 通过ServerBootstrap.handler()方法设置(见demo)
        ChannelHandler handler = config.handler();
        if (handler != null) {
            pipeline.addLast(handler);
        }

        ch.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                // 添加ServerBootstrapAcceptor
                pipeline.addLast(new ServerBootstrapAcceptor(
                        currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
            }
        });
    }
});
```

因此invokeHandlerAddedIfNeeded方法调用的ChannelHandler也只有该ChannelInitializer实例。此时ChannelInitializer的initChannel执行时，向pipeline添加了用户自定义的handler，同时向与Channel绑定的事件循环中添加了任务(向pipeline添加ServerBootstrapAcceptor的任务)，等待执行。在当前的事件循环NioEventLoop执行完`AbstractUnsafe.register0`任务之后，便会执行该**向pipeline添加ServerBootstrapAcceptor的任务**。

`AbstractUnsafe.register0`中在`pipeline.invokeHandlerAddedIfNeeded();`执行之后，会设置promise为已完成状态。此时再看文章前面的initAndRegister和doBind方法：

```java
final ChannelFuture initAndRegister() {
    Channel channel = null;
    try {
        channel = channelFactory.newChannel(); // 1.创建服务端Channel
        init(channel); // 2.初始化服务端Channel
    } catch (Throwable t) {
        if (channel != null) {
            channel.unsafe().closeForcibly();
        }
        return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(t);
    }

    // 3.服务端Channel注册到Selector(事件轮询器)
    // config().group(): NioEventLoopGroup
    ChannelFuture regFuture = config().group().register(channel); // DefaultChannelPromise
    if (regFuture.cause() != null) {
        if (channel.isRegistered()) {
            channel.close();
        } else {
            channel.unsafe().closeForcibly();
        }
    }
    return regFuture; // 返回DefaultChannelPromise
}

private ChannelFuture doBind(final SocketAddress localAddress) {
    // 1.创建NioServerSocketChannel, 2.初始化NioServerSocketChannel, 3.注册到Selector
    final ChannelFuture regFuture = initAndRegister(); // DefaultChannelPromise
    final Channel channel = regFuture.channel();
    if (regFuture.cause() != null) {
        return regFuture;
    }

    if (regFuture.isDone()) { // 若注册到Selector的操作已完成，在这个分支4.绑定端口
        // At this point we know that the registration was complete and successful.
        ChannelPromise promise = channel.newPromise();
        doBind0(regFuture, channel, localAddress, promise);
        return promise;
    } else { // 否则在下面绑定端口
        // Registration future is almost always fulfilled already, but just in case it's not.
        final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
        regFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Throwable cause = future.cause();
                if (cause != null) {
                    promise.setFailure(cause);
                } else {
                    promise.registered();
										// 4.绑定端口(注意这里！！！)
                    doBind0(regFuture, channel, localAddress, promise);
                }
            }
        });
        return promise;
    }
}
```

由于initAndRegister方法是个异步的过程且该方法的返回值就是前面Channel注册到Selector的结果promise，因此在注册NioSeverSocketChannel到Selector整体流程完成之前，initAndRegister方法返回的promise可能是未完成状态，因此会执行上面判断的else分支，即向promise注册了一个监听器。在promise完成时，调用`doBind0(regFuture, channel, localAddress, promise);`。

因此上面`AbstractUnsafe.register0`中设置promise为已完成状态时，会回调注册的监听器。由于向promise注册的监听器里调用了doBind0方法，下面就看下该方法：

```java
private static void doBind0(
        final ChannelFuture regFuture, final Channel channel,
        final SocketAddress localAddress, final ChannelPromise promise) {

    channel.eventLoop().execute(new Runnable() {
        @Override
        public void run() {
            if (regFuture.isSuccess()) {
                channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                promise.setFailure(regFuture.cause());
            }
        }
    });
}
```

可以看到doBind0方法，也向EventLoop添加了一个任务`channel.bind(localAddress, promise)`。因为在该任务添加到事件循环之前，taskQueue已经添加了一个`向服务端Channel pipeline添加ServerBootstrapAcceptor`的任务，因此`channel.bind(localAddress, promise)`任务在`向服务端Channel pipeline添加ServerBootstrapAcceptor`之后执行(顺序执行任务)。

在`AbstractUnsafe.register0`中设置promise为已完成状态并回调注册的监听器之后，执行`pipeline.fireChannelRegistered();`回调已注册到Channel Pipeline的ChannelHandler的channelRegistered方法。

最后，在NioEventLoop执行`AbstractUnsafe.register0`任务完成后，会先执行向服务端Channel pipeline添加ServerBootstrapAcceptor的任务，然后执行上面所述的doBind任务。

## 四、绑定端口

根据前面的分析，绑定端口的任务是在事件循环NioEventLoop中执行的。

```java
private static void doBind0(
        final ChannelFuture regFuture, final Channel channel,
  			// 这里的promise是个新的PendingRegistrationPromise实例
        final SocketAddress localAddress, final ChannelPromise promise) {

    channel.eventLoop().execute(new Runnable() {
        @Override
        public void run() {
            if (regFuture.isSuccess()) {
              	// NioEventLoop中执行
                channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                promise.setFailure(regFuture.cause());
            }
        }
    });
}
```

其中NioServerSocketChannel调用了bind方法进行端口绑定：`channel.bind(localAddress, promise)`。

```java
// AbstractChannel
public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
    return pipeline.bind(localAddress, promise);
}
// DefaultChannelPipeline
public final ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
    return tail.bind(localAddress, promise);
}
// TailContext: AbstractChannelHandlerContext
public ChannelFuture bind(final SocketAddress localAddress, final ChannelPromise promise) {
    if (localAddress == null) {
        throw new NullPointerException("localAddress");
    }
    if (!validatePromise(promise, false)) {
        // cancelled
        return promise;
    }

    final AbstractChannelHandlerContext next = findContextOutbound(); // HeadContext
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
        next.invokeBind(localAddress, promise); // 调用HeadContext.invokeBind方法
    } else {
        safeExecute(executor, new Runnable() {
            @Override
            public void run() {
                next.invokeBind(localAddress, promise);
            }
        }, promise, null);
    }
    return promise;
}
```

`channel.bind(localAddress, promise)`最终调用了HeadContext.invokeBind方法：

```java
// HeadContext(AbstractChannelHandlerContext)
private void invokeBind(SocketAddress localAddress, ChannelPromise promise) {
    if (invokeHandler()) {
        try {
          	// HeafContext.bind
            ((ChannelOutboundHandler) handler()).bind(this, localAddress, promise);
        } catch (Throwable t) {
            notifyOutboundHandlerException(t, promise);
        }
    } else {
        bind(localAddress, promise);
    }
}
// HeadContext
public void bind(
        ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
        throws Exception {
    unsafe.bind(localAddress, promise); // unsafe: NioMessageUnsafe
}
```

然后调用NioMessageUnsafe的bind方法，方法实现在其父类AbstractUnsafe中：

```java
// AbstractUnsafe
// 端口绑定
@Override
public final void bind(final SocketAddress localAddress, final ChannelPromise promise) {
    assertEventLoop();

    if (!promise.setUncancellable() || !ensureOpen(promise)) {
        return;
    }

    // 省略部分代码

    boolean wasActive = isActive(); // javaChannel().socket().isBound() wasActive: false
    try {
        doBind(localAddress);
    } catch (Throwable t) {
        safeSetFailure(promise, t);
        closeIfClosed();
        return;
    }

    if (!wasActive && isActive()) { // wasActive: false, isActive(): true
      	// 向事件循环taskQueue添加任务————回调ChannelActive方法
        invokeLater(new Runnable() {
            @Override
            public void run() {
                pipeline.fireChannelActive(); // 回调ChannelActive方法
            }
        });
    }

    safeSetSuccess(promise); // 设置promise为成功状态
}
```

AbstractUnsafe.bind方法主要做了两件事：

- 绑定端口；

- 向事件循环taskQueue添加任务，回调channelActive方法。

端口绑定的实现如下：

```java
protected void doBind(SocketAddress localAddress) throws Exception {
    if (PlatformDependent.javaVersion() >= 7) {
        // JDK底层端口绑定
        javaChannel().bind(localAddress, config.getBacklog());
    } else {
        javaChannel().socket().bind(localAddress, config.getBacklog());
    }
}
```

最终调用JDK NIO的ServerSocketChannel.bind方法进行端口的绑定。

向事件循环taskQueue添加任务的逻辑如下：

```java
private void invokeLater(Runnable task) {
    try {
        eventLoop().execute(task); // 添加任务
    } catch (RejectedExecutionException e) {
        logger.warn("Can't invoke task later as EventLoop rejected it", e);
    }
}
```

在NioEventLoop执行完端口绑定的任务之后，便会执行`回调ChannelHandler的channelActive方法`的任务。

### 回调ChannelHandler的channelActive方法

```java
// AbstractUnsafe.bind方法
pipeline.fireChannelActive(); // 回调channelActive方法

// DefaultChannelPipeline
public final ChannelPipeline fireChannelActive() {
    AbstractChannelHandlerContext.invokeChannelActive(head);
    return this;
}
// AbstractChannelHandlerContext
static void invokeChannelActive(final AbstractChannelHandlerContext next) {
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
        next.invokeChannelActive(); // 这里
    } else {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                next.invokeChannelActive();
            }
        });
    }
}
// AbstractChannelHandlerContext
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

AbstractChannelHandlerContext.invokeChannelActive方法会执行HeadContext.channelActive方法：

```java
// HeadContext
public void channelActive(ChannelHandlerContext ctx) throws Exception {
    ctx.fireChannelActive(); // 先传播事件

    readIfIsAutoRead(); // 重点
}
```

可以看到，HeadContext.channelActive方法首先调用了ctx.fireChannelActive()传播事件，然后调用readIfIsAutoRead方法。

```java
// HeadContext
private void readIfIsAutoRead() {
    if (channel.config().isAutoRead()) { // 默认true
        channel.read(); // 开始读取数据
    }
}
// AbstractChannel
public Channel read() {
    pipeline.read();
    return this;
}
// DefaultChannelPipeline
public final ChannelPipeline read() {
    tail.read();
    return this;
}
```

readIfIsAutoRead方法最终调用了TailContext.read方法：

```java
public ChannelHandlerContext read() {
    final AbstractChannelHandlerContext next = findContextOutbound(); // HeadContext
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
        next.invokeRead(); // 这里
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

TailContext.read方法调用HeadContext.read方法：

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
// HeadContext
public void read(ChannelHandlerContext ctx) {
    unsafe.beginRead(); // NioMessageUnsafe
}
// NioMessageUnsafe的父类AbstractUnsafe
public final void beginRead() {
    assertEventLoop();

    if (!isActive()) {
        return;
    }

    try {
        doBeginRead(); // 实现在AbstractNioChannel
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

最终调用到AbstractNioChannel.doBeginRead方法，该方法设置了Server Channel感兴趣的事件。

```java
protected void doBeginRead() throws Exception {
    // Channel.read() or ChannelHandlerContext.read() was called
    final SelectionKey selectionKey = this.selectionKey;
    if (!selectionKey.isValid()) {
        return;
    }

    readPending = true;

    final int interestOps = selectionKey.interestOps();
    if ((interestOps & readInterestOp) == 0) {
        selectionKey.interestOps(interestOps | readInterestOp); // 设置感兴趣的事件Accept
    }
}
```

总结下回调ChannelHandler.channelActive方法的流程：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/Netty-Server-Start-4.png)

至此，Netty服务端启动流程分析结束。

## 五、总结

### 1.启动流程

- 创建NioServerSocketChannel；
- 初始化NioServerSocketChannel；
- 注册NioServerSocketChannel到Selector；
- 绑定端口。

### 2.启动流程中NioEventLoop执行的任务

- `AbstractUnsafe.register0`注册服务端Channel到Selector；
- `向服务端Channel pipeline添加ServerBootstrapAcceptor`；
- `channel.bind(localAddress, promise)`绑定本地端口；
- 回调ChannelHandler的channelActive方法，为服务端Channel(SelectionKey)设置感兴趣的事件。

