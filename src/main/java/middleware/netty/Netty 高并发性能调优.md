# Netty 高并发性能调优

## 一、单机百万连接调优

### 1.如何模拟百万连接

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/middleware/netty/Netty%20perf.jpg)

服务端单机绑定8000至8100这100个端口，来模拟单机百万连接。

### 2.突破局部文件句柄限制

- unlimit -n 查看单进程最大文件句柄数限制

- /etc/serucity/limits.conf追加下面两行

  ```
  * hard nofile 1000000
  * soft nofile 1000000
  ```

### 3.突破全局文件句柄限制

- cat /proc/sys/fs/file-max 查看所有进程能打开的最大文件句柄数限制

- /etc/sysctl.conf 追加一行：

  fs.file-max=1000000

## 二、Netty应用级别性能调优

- ①耗时任务要在业务线程池中执行，不要阻塞Reactor线程；

  ```java
  // 调节业务线程数
  private static ExecutorService threadPool = Executors.newFixedThreadPool(1000);
  
  protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
      ByteBuf data = Unpooled.directBuffer();
      data.writeBytes(msg);
      threadPool.submit(() -> {
          Object result = getResult(data);  // 耗时任务在业务线程池执行
          ctx.channel().writeAndFlush(result);
      });
  }
  ```

- 调节业务线程数

- ②ChannelHandler业务逻辑可以在业务NioEventLoopGroup线程池中执行

  ```java
  EventLoopGroup businessGroup = new NioEventLoopGroup(1000); // 调节线程数
  
  bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
      @Override
      protected void initChannel(SocketChannel ch) {
          ch.pipeline().addLast(new FixedLengthFrameDecoder(Long.BYTES));
        	// ServerBusinessHandler逻辑在业务NioEventLoopGroup线程池中执行
          ch.pipeline().addLast(businessGroup, ServerBusinessHandler.INSTANCE);
      }
  });
  ```