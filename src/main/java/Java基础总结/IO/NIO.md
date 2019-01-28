# Java NIO
# Java NIO

## 一、Java NIO 概述

​	Java NIO由Channel、Buffer、Selector等核心部分组成。

### 1. Channel 与 Buffer

​	所有的 IO 在NIO 中都从一个Channel 开始。Channel 有点像流。 数据可以从Channel读到Buffer中，也可以从Buffer 写到Channel中。

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/practicesummary/nio-1.png?x-oss-process=style/markdown-pic)

​	Channel有FileChannel、DatagramChannel、SocketChannel、ServerSocketChannel等实现，这些通道涵盖了UDP 和 TCP 网络IO，以及文件IO。

​	Buffer有ByteBuffer、CharBuffer、DoubleBuffer、FloatBuffer、IntBuffer、LongBuffer、ShortBuffer，这些Buffer覆盖了能通过IO发送的基本数据类型：byte, short, int, long, float, double 和 char。

### 2.Selector

​	Selector允许单线程处理多个 Channel。如果应用打开了多个连接（通道），但每个连接的流量都很低，使用Selector就会很方便。例如在一个聊天服务器中。

​	以下是**在一个单线程中使用一个Selector处理3个Channel**的例子：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/practicesummary/nio-2.png?x-oss-process=style/markdown-pic)

​	**要使用Selector，得向Selector注册Channel，然后调用它的select()方法**。这个方法会一直**阻塞**到某个注册的通道有事件就绪。一旦这个方法返回，线程就可以处理这些事件，事件的例子有如**新连接进来(ServerSocketChannel  Accept)，数据接收(Read)**等。

## 二、Channel

​	Channel相比于流的不同之处：

- 既可以从通道中读取数据，又可以写数据到通道。但流的读写通常是单向的。
- 通道可以**非阻塞**地读写。
- 通道中的数据总是要先读到一个Buffer，或者总是要从一个Buffer中写入。

### 1.Channel实现

- FileChannel    从文件中读写数据
- DatagramChannel  通过UDP协议读写网络中的数据
- SocketChannel  通过TCP协议读写网络中的数据
- ServerSocketChannel  监听新进来的TCP连接，像Web服务器那样，对每一个新进来的连接都会创建一个SocketChannel

### 2.Channel示例

```java
// 使用FileChannel读取数据到Buffer中
// 首先读取数据到Buffer，然后反转Buffer,接着再从Buffer中读取数据。
public class ChannelTest {
    public static void main(String[] args){
        try(RandomAccessFile raf = new RandomAccessFile("data/nio-data.txt", "rw")) {
            FileChannel channel = raf.getChannel();
            // 48 Bytes
            ByteBuffer buffer = ByteBuffer.allocate(48);

            int count;
            StringBuilder sb = new StringBuilder();
            while ((count = channel.read(buffer))> 0) {
                System.out.println("read count per turn: " + count);
                // limit=pos, pos=0，切换为读模式
                buffer.flip();
                while (buffer.hasRemaining()) {
                    sb.append((char)buffer.get());
                }
                // pos=0, limit=capacity，清空缓冲，准备写入
                buffer.clear();
            }
            System.out.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

## 三、Buffer

​	Java NIO中的Buffer用于和NIO通道进行交互。数据是从通道读入缓冲区，从缓冲区写入到通道中的。缓冲区本质上是一块可以写入数据，然后可以从中读取数据的内存。这块内存被包装成NIO Buffer对象，并提供了一组方法，用来方便的访问该块内存。

### 1.Buffer的基本用法

使用Buffer读写数据一般遵循以下四个步骤：

1. 写入数据到Buffer
2. 调用`flip()`方法
3. 从Buffer中读取数据
4. 调用`clear()`方法或者`compact()`方法

​	当向buffer写入数据时，buffer会记录下写了多少数据。一旦要读取数据，需要通过flip()方法将Buffer从写模式切换到读模式。在读模式下，可以读取之前写入到buffer的所有数据。

​	一旦读完了所有的数据，就需要清空缓冲区，让它可以再次被写入。有两种方式能清空缓冲区：调用clear()或compact()方法。**clear()方法会清空整个缓冲区。compact()方法只会清除已经读过的数据。任何未读的数据都被移到缓冲区的起始处，新写入的数据将放到缓冲区未读数据的后面。**

```java
// 使用Buffer的例子：
RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");
FileChannel inChannel = aFile.getChannel();

// 创建具有48字节容量的缓冲区
ByteBuffer buf = ByteBuffer.allocate(48);

int bytesRead = inChannel.read(buf); // channel读入缓冲
while (bytesRead != -1) {

  buf.flip();  // 准备缓冲中数据的读取

  while(buf.hasRemaining()){
      System.out.print((char) buf.get()); // 一次读取1个字节
  }

  buf.clear(); // 清空，channel再次准备写入缓冲
  bytesRead = inChannel.read(buf);
}
aFile.close();
```

### 2.Buffer的position/limit/capacity

​	Buffer的工作机制与position、limit、capacity这三个属性密不可分。position和limit的含义取决于Buffer处在读模式还是写模式。不管Buffer处在什么模式，capacity的含义总是一样的。如下图所示：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/practicesummary/nio-3.png?x-oss-process=style/markdown-pic)

- capacity:   作为一个内存块，Buffer有一个固定的大小值，也叫“capacity”。只能往里写capacity个byte、long，char等类型。一旦Buffer满了，需要将其清空才能继续往里写数据。
- position:   当写数据到Buffer中时，position表示当前的位置。初始的position值为0.当一个byte、long等数据写到Buffer后， position会向前移动到下一个可插入数据的Buffer单元。position最大可为capacity – 1。 当读取数据时，也是从某个特定位置读。当将Buffer从写模式切换到读模式，position会被重置为0. 当从Buffer的position处读取数据时，position向前移动到下一个可读的位置。
- limit:   在写模式下，Buffer的limit表示最多能往Buffer里写多少数据。 写模式下，limit等于Buffer的capacity。当切换Buffer到读模式时， limit表示最多能读到多少数据。因此，当切换Buffer到读模式时，limit会被设置成写模式下的position值。换句话说，能读到之前写入的所有数据（limit被设置成已写数据的数量，这个值在写模式下就是position）

### 3.Buffer的类型

​	Buffer有ByteBuffer、MappedByteBuffer、CharBuffer、DoubleBuffer、FloatBuffer、IntBuffer、LongBuffer、ShortBuffer，这些Buffer覆盖了能通过IO发送的基本数据类型：byte, short, int, long, float, double 和 char。

### 4.Buffer的分配

​	要想获得一个Buffer对象，首先要进行分配。 每一个Buffer类都有一个allocate方法。

```java
ByteBuffer buf = ByteBuffer.allocate(48); // 48字节capacity的ByteBuffer
CharBuffer buf2 = CharBuffer.allocate(1024); //1024字符capacity的CharBuffer
```

### 5.向Buffer写数据

​	主要有：

​	①从Channel写到Buffer；

```java
int bytesRead = inChannel.read(buf); //read into buffer.
```

​	②通过Buffer的put()方法写到Buffer里。

```java
buf.put(127);  // 有不同的put方法，可以选择，如绝对定位、相对定位、put数组。。。
```

​	③flip()方法

​	flip方法将Buffer从写模式切换到读模式。调用flip()方法会将position设回0，并将limit设置成之前position的值，即能读取多少字节、字符等。

### 6.从Buffer读取数据

​	主要有：

​	①从Buffer读取数据到Channel

```java
//read from buffer into channel.
int bytesWritten = inChannel.write(buf);
```

​	②使用get()方法从Buffer中读取数据

```java
byte aByte = buf.get(); // 有不同的get方法，可以选择，如绝对定位、相对定位、get数组。。。
```

### 7.clear()与compact()方法

​	一旦读完Buffer中的数据，需要让Buffer准备好再次被写入。可以通过clear()或compact()方法来完成。

- 如果调用的是clear()方法，**position将被设回0，limit被设置成 capacity的值**。换句话说，Buffer 被清空了。Buffer中的数据并未清除，只是这些标记告诉我们可以从哪里开始往Buffer里写数据。如果Buffer中有一些未读的数据，调用clear()方法，**数据将“被遗忘”**，意味着不再有任何标记会告诉你哪些数据被读过，哪些还没有。
- 如果**Buffer中仍有未读的数据，且后续还需要这些数据，但是此时想要先写些数据，**那么使用compact()方法。<u>compact()方法将所有未读的数据拷贝到Buffer起始处。然后将position设到最后一个未读元素正后面。limit属性依然像clear()方法一样，设置成capacity。现在Buffer准备好写数据了，但是不会覆盖未读的数据。</u>

### 8.mark()与reset()方法

​	通过调用Buffer.mark()方法，可以标记Buffer中的一个特定position。之后可以通过调用Buffer.reset()方法恢复到这个position。

```java
buffer.mark(); // 标记

//多次调用get()方法，移动指针

buffer.reset();  // 回到标记位置 
```

### 9.equals()与compareTo()方法

​	用于比较两个Buffer。

- equals()方法

  当满足下列条件时，表示两个Buffer相等：

  1. 有相同的类型（byte、char、int等）。
  2. Buffer中剩余的byte、char等的个数相等。**(剩余元素是从 position到limit之间的元素)**
  3. Buffer中所有剩余的byte、char等都相同。

  **equals只是比较Buffer的一部分，不是每一个在它里面的元素都比较。实际上，它只比较Buffer中的剩余元素。**

- compareTo()方法

  compareTo()方法比较两个Buffer的剩余元素(byte、char等)， 如果满足下列条件，则认为**一个Buffer“小于”另一个Buffer**：

  1. 第一个不相等的元素小于另一个Buffer中对应的元素 。
  2. 所有元素都相等，但<u>第一个Buffer比另一个先耗尽(第一个Buffer的元素个数比另一个少)</u>。

## 四、Scattering Reads 与Gathering Writes

### 1.Scattering Reads

​	是指数据从一个channel读取到多个buffer中。如下图所示：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/practicesummary/nio-4.png?x-oss-process=style/markdown-pic)

```java
ByteBuffer header = ByteBuffer.allocate(128);
ByteBuffer body   = ByteBuffer.allocate(1024);

ByteBuffer[] bufferArray = { header, body };

channel.read(bufferArray);
```

​	read()方法按照buffer在数组中的顺序将从channel中读取的数据写入到buffer，当一个buffer被写满后，channel紧接着向另一个buffer中写。Scattering Reads在移动下一个buffer前，必须填满当前的buffer，这也意味着它不适用于动态消息。（消息大小不固定）

### 2.Gathering Writers

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/practicesummary/nio-5.png?x-oss-process=style/markdown-pic)

​	是指数据从多个buffer写入到同一个channel。

```java
ByteBuffer header = ByteBuffer.allocate(128);
ByteBuffer body   = ByteBuffer.allocate(1024);

//write data into buffers

ByteBuffer[] bufferArray = { header, body };

channel.write(bufferArray);
```

​	write()方法会按照buffer在数组中的顺序，将数据写入到channel，注意**只有position和limit之间的数据才会被写入**。**与Scattering Reads相反，Gathering Writes能较好的处理动态消息。**

## 五、Selector

​	Selector（选择器）是Java NIO中能够检测一到多个NIO通道，并能够知晓通道是否为诸如读写事件做好准备的组件。因此，**一个单独的线程可以管理多个channel，从而管理多个网络连接**。

###1.使用Selector的原因

​	仅用单个线程来处理多个Channels的好处是，只需要更少的线程来处理通道。事实上，可以只用一个线程处理所有的通道。对于操作系统来说，线程之间上下文切换的开销很大，而且每个线程都要占用系统的一些资源（如内存）。因此，使用的线程越少越好。下图是单线程使用一个Selector处理3个channel的示例图。

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/practicesummary/nio-2.png?x-oss-process=style/markdown-pic)

### 2.Selector的创建与通道的注册

- Selector创建

  ```java
  Selector selector = Selector.open();
  ```

- 向Selector注册通道

  ​	为了将Channel和Selector配合使用，必须将channel注册到selector上。通过**SelectableChannel.register()**方法来实现。

  ```java
  // 非阻塞
  channel.configureBlocking(false);
  // 注册channel和感兴趣的事件
  SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
  ```

  ​	(1)**与Selector一起使用时，Channel必须处于非阻塞模式下。**这意味着**不能将FileChannel与Selector一起使用**，因为FileChannel不能切换到非阻塞模式。而套接字通道都可以。

  ​	(2)register()方法的第二个参数是一个"interest"集合，表示在通过Selector监听Channel时对什么事件感兴趣。有**Connect、Accept、Read、Write**四种事件类型，用SelectionKey的四个常量**SelectionKey.OP_ACCEPT、SelectionKey.OP_CONNECT、SelectionKey.OP_READ、SelectionKey.OP_WRITE**来表示。

  ​	**通道触发了一个事件**意思是**该事件已经就绪**。某个channel成功连接到另一个服务器称为“**连接就绪**”。一个server socket channel准备好接收新进入的连接称为“**接收就绪**”。一个有数据可读的通道可以说是“**读就绪**”。等待写数据的通道可以说是“**写就绪**”。

  ​	**监听多个事件，可以用“位或”操作符将事件常量连接起来**，如下所示。

  ```java
  int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
  // 设置感兴趣的事件集合
  SelectionKey.interestOps(interestSet)；
  ```

### 3.SelectionKey

​	当向Selector注册Channel时，register()方法会返回一个SelectionKey对象，该对象包含了interest集合、ready集合、Selector、Channel、附加的对象等。

#### (1)interest集合

​	interest集合是Channel感兴趣的事件集合。可以通过SelectionKey**读写**interest集合。

```java
int interestSet = selectionKey.interestOps(); // 获取感兴趣的事件集合
// 分别判断某个事件是否在感兴趣的事件集合中
boolean isInterestedInAccept  = (interestSet & SelectionKey.OP_ACCEPT) != 0;
boolean isInterestedInConnect = (interestSet & SelectionKey.OP_CONNECT) != 0;
boolean isInterestedInRead    = (interestSet & SelectionKey.OP_READ) != 0;
boolean isInterestedInWrite   = (interestSet & SelectionKey.OP_WRITE) != 0;
```

​	用“位与”操作interest 集合和给定的SelectionKey常量，可以确定某个确定的事件是否在interest 集合中。

#### (2)ready集合

​	ready集合是通道已经准备就绪的操作的集合。在一次选择(Selection)之后，你会首先访问这个ready set。可以像如下的方式进行访问：

```java
int readySet = selectionKey.readyOps();
```

​	可以用像检测interest集合那样的方法(位操作)，来检测channel中什么事件或操作已经就绪。也可以如下判断：

```java
selectionKey.isAcceptable(); // 原理也是位操作判断
selectionKey.isConnectable();
selectionKey.isReadable();
selectionKey.isWritable();
```

#### (3)从SelectionKey获取Channel和Selector

```java
Channel  channel  = selectionKey.channel();

Selector selector = selectionKey.selector();  
```

#### (4)附加对象

​	可以将一个对象或者更多信息附着到SelectionKey上，这样就能**方便的识别某个给定的通道**。例如，可以附加 与通道一起使用的Buffer，或是包含聚集数据的某个对象。

```java
// 方式1
selectionKey.attach(theObject);

Object attachedObj = selectionKey.attachment();

// 方式2
SelectionKey key = channel.register(selector, SelectionKey.OP_READ, theObject);
```

### 4.通过Selector选择Channel

​	一旦向Selector注册了一或多个通道，就可以调用几个重载的select()方法。这些方法返回你所感兴趣的事件（如连接、接受、读或写）已经准备就绪的那些通道的个数。换句话说，如果对“读就绪”的通道感兴趣，select()方法会返回读事件已经就绪的那些通道。

```java
int select();  // 阻塞到至少有一个通道在注册的事件上就绪了
int select(long timeout); // 最长会阻塞timeout毫秒(参数)
int selectNow(); // 非阻塞的选择操作。如果自从前一次选择操作后，没有通道变成可选择的，则此方法直接返回零。
```

​	**select()方法返回的int值表示有多少通道已经就绪**。即自上次调用select()方法后有多少通道变成就绪状态。如果调用select()方法，因为有一个通道变成就绪状态，返回了1，若再次调用select()方法，如果另一个通道就绪了，它会再次返回1。如果对第一个就绪的channel没有做任何操作，现在就有两个就绪的通道，但在每次select()方法调用之间，只有一个通道就绪了。(**注意区别**)

#### 1.selectedKeys()方法

​	一旦调用了select()方法，并且返回值表明有一个或更多个通道就绪了，然后可以通过调用Selector的selectedKeys()方法，访问“已选择键集（selected key set）”中的就绪通道。可通过这些SelectionKey对象，访问对应的Channek对象。

```java
Set<SelectionKey> selectedKeys = selector.selectedKeys();   
```

​	可以遍历这个已选择的键集合来访问就绪的通道。

```java
// 这个循环遍历已选择键集中的每个键，并检测各个键所对应的通道的就绪事件。
Set<SelectionKey> selectedKeys = selector.selectedKeys();

Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

while(keyIterator.hasNext()) {
    SelectionKey key = keyIterator.next();

    if(key.isAcceptable()) {
        // a connection was accepted by a ServerSocketChannel.
		// SelectionKey.channel()方法返回的通道需要转型成你要处理的类型，如ServerSocketChannel或SocketChannel等。
    }
    if (key.isConnectable()) {
        // a connection was established with a remote server.
    }
    if (key.isReadable()) {
        // a channel is ready for reading
    }
    if (key.isWritable()) {
        // a channel is ready for writing
    }
	// Selector不会自己从已选择键集中移除SelectionKey实例。必须在处理完通道时自己移除。下次该通道变成就绪时，Selector会再次将其放入已选择键集中。
    keyIterator.remove();
}
```

### 5.wakeUp()方法

​	某个线程调用select()方法后阻塞了，即使没有通道已经就绪，也有办法让其从select()方法返回。**只要让其它线程在第一个线程调用select()方法的那个对象上调用Selector.wakeup()方法即可。**阻塞在select()方法上的线程会立马返回。

​	如果有其它线程调用了wakeup()方法，但当前没有线程阻塞在select()方法上，下个调用select()方法的线程会立即“醒来(wake up)”。

### 6.close()方法

​	用完Selector后调用其close()方法会关闭该Selector，且使注册到该Selector上的所有SelectionKey实例无效。通道本身并不会关闭。

### 7.完整的示例

```java
// 打开Selector
Selector selector = Selector.open();
// channel必须非阻塞
channel.configureBlocking(false);
// 注册Channel
SelectionKey key = channel.register(selector, SelectionKey.OP_READ);

while(true) {
  // 获取已就绪的channel数
  int readyChannels = selector.select();
  // 没有，则继续等待
  if(readyChannels == 0) continue;
  // 获取就绪通道对应的SelectionKey
  Set<SelectionKey> selectedKeys = selector.selectedKeys();
  // 获取迭代器  
  Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
  // 迭代遍历
  while(keyIterator.hasNext()) {
    SelectionKey key = keyIterator.next();
	// 处理事件
    if(key.isAcceptable()) {
        // a connection was accepted by a ServerSocketChannel.
    }
    if (key.isConnectable()) {
        // a connection was established with a remote server.
    }
    if (key.isReadable()) {
        // a channel is ready for reading
    }
    if (key.isWritable()) {
        // a channel is ready for writing
    }
	// 删除已处理的SelectionKey
    keyIterator.remove();
  }
}
```

## 六、FileChannel

​	Java NIO中的FileChannel是一个连接到文件的通道。可以通过文件通道读写文件。**FileChannel无法设置为非阻塞模式，它总是运行在阻塞模式下。**

### 1.打开FileChannel

​	在使用FileChannel之前，必须先打开它。但是，无法直接打开一个FileChannel，需要通过使用一个InputStream、OutputStream或RandomAccessFile来获取一个FileChannel实例。

```java
// 通过RandomAccessFile打开FileChannel
RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");
FileChannel inChannel = aFile.getChannel();
```

### 2. 从FileChannel读取数据

​	调用多个read()方法之一从FileChannel中读取数据。如下所示

```java
ByteBuffer buf = ByteBuffer.allocate(48);

int bytesRead = inChannel.read(buf);
```

- 首先，分配一个Buffer。从FileChannel中读取的数据将被读到Buffer中。
- 然后，调用FileChannel.read()方法。该方法将数据从FileChannel读取到Buffer中。read()方法返回的int值表示了有多少字节被读到了Buffer中。如果返回-1，表示到了文件末尾。

### 3.向FileChannel写入数据

​	使用FileChannel.write()方法向FileChannel写数据，该方法的参数是一个Buffer。如下所示：

```java
String newData = "New String to write to file..." + System.currentTimeMillis();

ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());

buf.flip();

while(buf.hasRemaining()) {
    channel.write(buf);
}
```

​	注意FileChannel.write()是在while循环中调用的。因为无法保证write()方法一次能向FileChannel写入多少字节，因此**需要重复调用write()方法，直到Buffer中已经没有尚未写入通道的字节**。

### 4.关闭FileChannel

```java
// 用完FileChannel后必须将其关闭。
channel.close(); 
```

### 5.FileChannel的position、size、truncate、force方法

#### (1)position方法

​	可能需要在FileChannel的某个特定位置进行数据的读/写操作。可以通过调用position()方法获取FileChannel的当前位置。也可以通过调用position(long pos)方法设置FileChannel的当前位置。

```java
long pos = channel.position(); // get pos

channel.position(pos +123); // set pos
```

​	如果将位置设置在文件结束符之后，然后试图从文件通道中读取数据，读方法将返回-1 —— 文件结束标志。如果将位置设置在文件结束符之后，然后向通道中写数据，文件将撑大到当前位置并写入数据。这可能导致“文件空洞”，磁盘上物理文件中写入的数据间有空隙。

#### (2)size方法

​	FileChannel实例的size()方法将返回该实例所关联文件的大小。

```java
long fileSize = channel.size();    
```

####(3)truncate方法

​	可以使用FileChannel.truncate()方法截取一个文件。截取文件时，文件s中指定长度后面的部分将被删除。

```java
channel.truncate(1024); // 截取文件的前1024个字节
```

#### (4)force方法

​	FileChannel.force()方法将通道里尚未写入磁盘的数据强制写到磁盘上。出于性能方面的考虑，操作系统会将数据缓存在内存中，所以无法保证写入到FileChannel里的数据一定会即时写到磁盘上。要保证这一点，需要调用force()方法。 force()方法有一个boolean类型的参数，**指明是否同时将文件元数据（权限信息等）写到磁盘上**。

```java
channel.force(true); // 同时将文件数据和元数据强制写到磁盘上
```

## 七、SocketChannel

​	Java NIO中的SocketChannel是一个**连接到TCP网络套接字**的通道。可以通过以下2种方式创建SocketChannel：

- 打开(open)一个SocketChannel并连接(connect)到互联网上的某台服务器。

- 一个新连接到达ServerSocketChannel时，会创建(accept)一个SocketChannel。

### 1.打开SocketChannel

```java
SocketChannel socketChannel = SocketChannel.open();  // 打开
socketChannel.connect(new InetSocketAddress("http://jenkov.com", 80)); // 连接到服务器
```

### 2.关闭SocketChannel

```java
socketChannel.close(); 
```

### 3.从SocketChannel读取数据

​	要从SocketChannel中读取数据，调用一个read()的方法之一。如下所示：

```java
ByteBuffer buf = ByteBuffer.allocate(48);

int bytesRead = socketChannel.read(buf);
```

- 首先，分配一个Buffer。从SocketChannel读取到的数据将会放到这个Buffer中。

- 然后，调用SocketChannel.read()。该方法将数据从SocketChannel 读到Buffer中。read()方法返回的int值表示读了多少字节进Buffer里。如果返回的是-1，表示已经读到了流的末尾（连接关闭了）。

### 4.写数据到SocketChannel

​	写数据到SocketChannel用的是SocketChannel.write()方法，该方法以一个Buffer作为参数。如下所示：

```java
String newData = "New String to write to file..." + System.currentTimeMillis();

ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());

buf.flip();

while(buf.hasRemaining()) {
    channel.write(buf);
}
```

​	<font color=red>注意SocketChannel.write()方法的调用是在一个while循环中的。Write()方法无法保证能写多少字节到SocketChanne。所以，我们重复调用write()直到Buffer没有要写的字节为止。</font>(非阻塞模式)

### 5.非阻塞模式

​	可以设置 SocketChannel 为非阻塞模式（non-blocking mode）。设置之后，就可以在异步模式下调用connect(), read() 和write()。

#### 1.connect方法

​	如果SocketChannel在非阻塞模式下，此时调用connect()，该方法可能在连接建立之前就返回了。为了确定连接是否建立，可以调用finishConnect()的方法。(轮询)

```java
socketChannel.connect(new InetSocketAddress("http://jenkov.com", 80));
socketChannel.configureBlocking(false); // 非阻塞模式设置
// 轮询
while(! socketChannel.finishConnect() ){
    //wait, or do something else...    
}
```

#### 2.write方法

​	非阻塞模式下，write()方法在尚未写出任何内容时可能就返回了。所以需要在循环中调用write()。(前例)

#### 3.read方法

​	非阻塞模式下,read()方法在尚未读取到任何数据时可能就返回了。所以需要关注它的int返回值，它会告诉你读取了多少字节。

## 八、ServerSocketChannel

​	Java NIO中的 ServerSocketChannel 是一个可以**监听新进来的TCP连接**的通道, 就像标准IO中的**ServerSocket**一样。举例如下：

```java
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); // 打开

serverSocketChannel.socket().bind(new InetSocketAddress(9999)); // 绑定
// 监听新连接
while(true){
    SocketChannel socketChannel = serverSocketChannel.accept(); // 监听
    //do something with socketChannel...
}
```

### 1.打开ServerSocketChannel

​	通过调用 ServerSocketChannel.open() 方法来打开ServerSocketChannel.

```java
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
```

### 2.关闭ServerSocketChannel

​	通过调用ServerSocketChannel.close() 方法来关闭ServerSocketChannel.

```java
serverSocketChannel.close();
```

### 3.监听新进来的连接

​	通过 **ServerSocketChannel.accept()** 方法监听新进来的连接。当 accept()方法返回的时候,它返回一个包含新进来的连接的 SocketChannel。因此, **accept()方法会一直阻塞到有新连接到达(阻塞模式)**。**通常不会仅仅只监听一个连接，因此在while循环中调用 accept()方法。**

```java
while(true){
    SocketChannel socketChannel = serverSocketChannel.accept(); // 监听新连接
    //do something with socketChannel...  // 处理新连接，读写数据
}
```

### 4.非阻塞模式

​	ServerSocketChannel可以设置成非阻塞模式。在非阻塞模式下，accept() 方法会立刻返回，如果还没有新进来的连接,返回的将是null。 因此，需要检查返回的SocketChannel是否是null。

```java
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
serverSocketChannel.socket().bind(new InetSocketAddress(9999));
serverSocketChannel.configureBlocking(false); // 设置为非阻塞模式

while(true){
    SocketChannel socketChannel = serverSocketChannel.accept();
	// 判断是否为 null
    if(socketChannel != null){
        //do something with socketChannel...
    }
}
```

## 九、非阻塞式服务器(重点 Non-blocking Server)

​	详见(需要好好看原理、源码)

- http://ifeve.com/non-blocking-server/   
- http://tutorials.jenkov.com/java-nio/non-blocking-server.html
- https://github.com/jjenkov/java-nio-server

## 十、DatagramChannel

​	Java NIO中的DatagramChannel是一个能**收发UDP包的通道**。因为UDP是无连接的网络协议，所以不能像其它通道那样读取和写入。它**发送和接收的是数据包**。

### 1.打开DatagramChannel

```java
DatagramChannel channel = DatagramChannel.open();  // 打开
channel.socket().bind(new InetSocketAddress(9999)); // 绑定到本地端口
```

​	打开的 DatagramChannel可以**在UDP端口9999上接收数据包**。

### 2.接收数据

```java
ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
// 接收数据包到buffer
channel.receive(buf); // 阻塞模式下，阻塞到有数据时返回；非阻塞模式下，返回null
```

​	通过receive()方法从DatagramChannel接收数据。receive()方法会将接收到的数据包内容复制到指定的Buffer. 如果Buffer容不下收到的数据，多出的数据将被丢弃。

### 3.发送数据

​	通过send()方法从DatagramChannel发送数据。如下所示：

```java
String newData = "New String to write to file..." + System.currentTimeMillis();
    
ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());
buf.flip();
// 发送数据包到指定地址
int bytesSent = channel.send(buf, new InetSocketAddress("jenkov.com", 80));
```

​	上面发送一串字符到”jenkov.com”服务器的UDP端口80。 因为服务端并没有监控这个端口，所以什么也不会发生。也不会通知你发出的数据包是否已收到，因为**UDP在数据传送方面没有任何保证**。

### 4.连接到指定的地址

​	可以将DatagramChannel“连接”到网络中的特定地址的。**由于UDP是无连接的，连接到特定地址并不会像TCP通道那样创建一个真正的连接。而是锁住DatagramChannel ，让其只能从特定地址收发数据。**

```java
channel.connect(new InetSocketAddress("jenkov.com", 80));  // 连接到指定的地址 
```

​	当连接后，也可以使用read()和write()方法，就像在用传统的通道一样。只是**在数据传送方面没有任何保证**。

```java
int bytesRead = channel.read(buf); 
int bytesWritten = channel.write(buf);
```

## 十一、Pipe(管道)

​	Java NIO **管道**是**2个线程之间的单向数据连接**。`Pipe`有一个source通道和一个sink通道。<u>数据会被写到sink通道，从source通道读取。</u>Pipe原理如下：

![](https://alvin-jay.oss-cn-hangzhou.aliyuncs.com/practicesummary/nio-6.png)

### 1.创建管道

```java
Pipe pipe = Pipe.open(); // 通过Pipe.open()方法打开管道
```

### 2.向管道写数据

​	要向管道写数据，需要访问sink通道。

```java
Pipe.SinkChannel sinkChannel = pipe.sink();
```

​	通过调用SinkChannel的`write()`方法，将数据写入`SinkChannel`。

```java
String newData = "New String to write to file..." + System.currentTimeMillis();

ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());

buf.flip();

while(buf.hasRemaining()) {
    sinkChannel.write(buf);
}
```

### 3.从管道读取数据

​	从读取管道的数据，需要访问source通道。

```java
Pipe.SourceChannel sourceChannel = pipe.source();
```

​	调用source通道的`read()`方法来读取数据.

```java
ByteBuffer buf = ByteBuffer.allocate(48);

int bytesRead = inChannel.read(buf);
```

​	`read()`方法返回的int值会告诉我们多少字节被读进了缓冲区。

## 十二、Java NIO与IO的区别

```java
IO                NIO
面向流            面向缓冲
阻塞IO            非阻塞IO
无                选择器
```

参考如下文章：

- [Java NIO与IO](http://ifeve.com/java-nio-vs-io/)
- [Java NIO vs. IO](http://tutorials.jenkov.com/java-nio/nio-vs-io.html)

## 十三、Path/Files

​	见 <<Java核心技术 高级特性 第二章第五节>>



