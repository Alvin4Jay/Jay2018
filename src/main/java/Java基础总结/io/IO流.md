# IO流

## 一、IO流概述

###1.数据源和目的媒介

​	`Java`的`IO`包主要关注从原始数据源读取数据以及输出原始数据到目的媒介。典型的数据源和目的媒介有如下几种：①文件；②管道；③网络连接；④内存缓存；⑤System.in, System.out, System.error(注：Java标准输入、输出、错误输出)。

### 2.流

​	流从概念上来说是一个连续的数据流。既可以从流中读取数据，也可以往流中写数据。流与数据源或者数据流向的媒介相关联。在Java IO中流既可以是字节流(以字节为单位进行读写)，也可以是字符流(以字符为单位进行读写)。

### 3.InputStream、OutputStream、Reader、Writer

​	一个程序需要InputStream或者Reader从数据源读取数据，需要OutputStream或者Writer将数据写入到目标媒介中，如下图所示：	![](http://pbku1z6p0.bkt.clouddn.com/IO-1.png)

### 4.IO的用途

​	文件访问；网络访问；内存缓存访问；线程内部通信(管道)；缓冲；过滤；解析；读写文本 (Readers / Writers)；读写基本类型数据 (long, int etc.)；读写对象等。

### 5.Java IO类整理

![](http://pbku1z6p0.bkt.clouddn.com/IO-2.png)

##二、字节与字符数组

### 1.读入字节和字符数组

```java
public class ByteArrayInputStreamTest {
    public static void main(String[] args) {
        String message = "Hello World!";
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);

        try (InputStream in = new ByteArrayInputStream(bytes);
             Reader reader = new CharArrayReader(message.toCharArray())) {
			// 读取字节数组
            int b;
            // Hello World!
            while ((b = in.read()) != -1) {
                System.out.print(Integer.toHexString(b));
            }

            System.out.println("\n----------");
			// 读取字符数组
            int c;
            while ((c = reader.read()) != -1) {
                System.out.print((char) c);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
```

### 2.写出字节和字符数组

```java
public class ByteArrayOutputStreamTest {
    public static void main(String[] args) {
		// 写出字节数组
        try (OutputStream out = new ByteArrayOutputStream()) {
            out.write("This text is converted to bytes".getBytes(StandardCharsets.UTF_8));
            byte[] bytes = ((ByteArrayOutputStream) out).toByteArray();
            System.out.println(new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
		// 写出字符数组
        try (Writer writer = new CharArrayWriter()) {
            writer.write("Hello World!");
            System.out.println(((CharArrayWriter) writer).toCharArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
```

##三、System.in/out/err

```java
public class SystemTest {
    public static void main(String[] args) {
        try {
            // InputStream
            Scanner sc = new Scanner(System.in);
			
            // PrintStream
            InputStream input = new FileInputStream("aa.txt");
            System.out.println("File opened...");

            OutputStream out = new FileOutputStream("test.txt");
            PrintStream printStream = new PrintStream(out);
            // 重定向标准输出
            System.setOut(printStream);
            System.out.println("name is lbj.");
            System.out.flush(); // 刷新
            System.out.close(); // 关闭
        } catch (IOException e) {
            // PrintStream
            System.err.println("File opening failed.");
            e.printStackTrace();
        }
    }
}
```

##四、流

### 1.InputStream读取数据

```java
InputStream input = new FileInputStream("c:\\data\\input-file.txt");

int data;
// read()方法返回一个整数，代表了读取到的字节的内容(0 ~ 255)。当达到流末尾没有更多数据可以读取的时候，read()方法返回-1。
while((data = input.read()) != -1){
  // do some thing...
}
```

###2.OutputStream写出数据

```java
OutputStream output = new FileOutputStream("c:\\data\\output-file.txt");
output.write("Hello World".getBytes());
output.close();
```

### 3.Reader读取数据

```java
public class ReaderTest {
    public static void main(String[] args){
        try (Reader reader = new FileReader("aa.txt")) {
            int c;
            // 读取字符，返回值int的范围在0到65535之间(当达到流末尾时，同样返回-1)
            while ((c = reader.read()) != -1) {
                System.out.print((char) c);
            }
			// 转换流(字节流--->字符流)
            Reader inputStreamReader = new InputStreamReader(new FileInputStream("aa.txt"),
                    StandardCharsets.UTF_8);
			// 带缓冲的reader
            Reader bufferedReader = new BufferedReader(new FileReader("aa.txt"));	
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### 4.Writer写出数据

```java
public class WriterTest {
    public static void main(String[] args) {
        // 写出，true代表追加，而不是覆盖
        try (Writer writer = new FileWriter("aa.txt", true)) {
            writer.write("Hello World Writer!");
			// 转换流(字符流--->字节流)
            Writer outputStreamWriter = new OutputStreamWriter(new FileOutputStream("aa.txt"));
			// 带缓冲的writer
            Writer bufferedWriter = new BufferedWriter(new FileWriter("aa.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

##五、异常处理(try-with-resources)

```java
private static void printFileJava7() throws IOException {
	// Java7 try-with-resources 
    // 自动关闭资源时(close())抛出的异常会被抑制，只抛出try代码块里的异常；
    // 自动按照资源创建的顺序，反序关闭资源，先关闭bufferedInput，后关闭input。
    try(  FileInputStream     input         = new FileInputStream("file.txt");
          BufferedInputStream bufferedInput = new BufferedInputStream(input)) {
        int data = bufferedInput.read();
        while(data != -1){
            System.out.print((char) data);
        	data = bufferedInput.read();
        }
    }
}
```

```java
// 可自定义类实现AutoCloseable接口，创建可自动关闭(try-with-resources)的资源
public class MyAutoClosable implements AutoCloseable {
    public void doIt() {
        System.out.println("MyAutoClosable doing it!");
    }

    @Override
    public void close() throws Exception {
        System.out.println("MyAutoClosable closed!");
    }
}

private static void myAutoClosable() throws Exception {
    try(MyAutoClosable myAutoClosable = new MyAutoClosable()){
        myAutoClosable.doIt();
    }
}

// out:
MyAutoClosable doing it!
MyAutoClosable closed!
```

## 六、InputStream

### 1.读取基于字节的数据(read)

```java
try(InputStream inputstream = new FileInputStream("file.txt")) {
    int data;
	// 每次读取一个字节(int值 0-255).读到末尾，返回-1
    while((data  = inputstream.read()) != -1){
        System.out.print((char) data);
    }
}
```

###2.read(byte[])

```java
InputStream inputstream = new FileInputStream("c:\\data\\input-text.txt");

byte[] data      = new byte[1024];
int    bytesRead = inputstream.read(data);

while(bytesRead != -1) {
  doSomethingWithData(data, bytesRead);

  bytesRead = inputstream.read(data);
}
inputstream.close();
```

## 七、OutputStream

### 1.write(int)

```java
OutputStream output = new FileOutputStream("c:\\data\\output-text.txt");

while(hasMoreData()) {
  int data = getMoreData();
  // 只会写入int变量的低8位单个字节，其余位忽略
  output.write(data);
}
output.close();
```

### 2.write(byte[])

- write(byte[])把字节数组中所有数据写入到输出流中。

- write(byte[], int offset, int length)把字节数据中从offset位置开始，length个字节的数据写入到输出流。

### 3.flush()

​	通过调用flush()方法，可以把输出缓冲区内的数据刷新到磁盘(或者网络，以及其他任何形式的目标媒介)中。

### 4.close()

```java
OutputStream output = null;

try{
  output = new FileOutputStream("c:\\data\\output-text.txt");

  while(hasMoreData()) {
    int data = getMoreData();
    output.write(data);
  }
} finally {
    // 关闭流，但不是一个完美的异常处理方案
    if(output != null) {
        output.close();
    }
}
```

## 八、RandomAccessFile

### 1.创建一个RandomAccessFile

```java
RandomAccessFile file = new RandomAccessFile("c:\\data\\file.txt", "rw");
```

### 2.指定位置读写

```java
RandomAccessFile file = new RandomAccessFile("c:\\data\\file.txt", "rw");
// 跳转到指定位置seek
file.seek(200);
// 获得当前文件指针的位置 long
long pointer = file.getFilePointer();

file.close();
```

### 3.读数据

```java
RandomAccessFile file = new RandomAccessFile("c:\\data\\file.txt", "rw");
// 自动移动指针
int aByte = file.read();

file.close();
```

​	**read()方法在读取完一个字节之后，会自动把指针移动到下一个可读字节**。这意味着使用者在调用完read()方法之后不需要手动移动文件指针。

### 4.写数据

```java
RandomAccessFile file = new RandomAccessFile("c:\\data\\file.txt", "rw");

file.write("Hello World".getBytes());

file.close();
```

​	与read()方法类似，**write()方法在调用结束之后自动移动文件指针**，所以不需要频繁地把指针移动到下一个将要写入数据的位置。

## 九、File

### 1.实例化File

```java
File file = new File("c:\\data\\input-file.txt");
```

### 2.检测文件是否存在

```java
File file = new File("c:\\data\\input-file.txt");
// 存在，true
boolean fileExists = file.exists(); 
```

###3.创建目录

```
// 代表目录
File file = new File("c:\\users\\jakobjenkov\\newdir");
// 创建目录，父目录不存在不会创建父目录，返回创建失败false
boolean dirCreated = file.mkdir();

// ----------------------
// 代表目录
File file = new File("c:\\users\\jakobjenkov\\newdir");
// 创建目录，父目录不存在会自动创建父目录。
boolean dirCreated = file.mkdirs();
```

### 4.获取文件长度

```java
File file = new File("c:\\data\\input-file.txt");

long length = file.length();
```

### 5.重命名或移动文件

```java
File file = new File("c:\\data\\input-file.txt");
// 重命名文件
boolean success = file.renameTo(new File("c:\\data\\new-file.txt"));
```

### 6.删除文件

```java
File file = new File("c:\\data\\input-file.txt");

boolean success = file.delete();
```

### 7.检查File是文件还是目录

```java
File file = new File("c:\\data");
// true表示目录
boolean isDirectory = file.isDirectory();
```

###8.读取目录中为文件列表

```java
File file = new File("c:\\data");
// list() 返回该目录下的 文件和目录 的String表示，不递归
String[] fileNames = file.list();
// listFiles() 返回该目录下的 文件和目录 的File表示，不递归
File[]   files = file.listFiles();
```

## 十、ByteArrayInputStream/ByteArrayOutputStream

###1.ByteArrayInputStream

```java
byte[] bytes = ... //get byte array from somewhere.

InputStream input = new ByteArrayInputStream(bytes);
// 读取字节数组
int data = input.read();
while(data != -1) {
  //do something with data
  data = input.read();
}
input.close();
```

###2.ByteArrayOutputStream

```java
ByteArrayOutputStream output = new ByteArrayOutputStream();

//write data to output stream

// toByteArray()获取写出的字节数组数据
byte[] bytes = output.toByteArray();
```

## 十一、BufferedStream与DataStream

### 1.BufferedInputstream(带缓冲区)

```java
InputStream input = new BufferedInputStream(
                      new FileInputStream("c:\\data\\input-file.txt"));

// 设置缓冲区大小, 8KB
int bufferSize = 8 * 1024;
    
InputStream input = new BufferedInputStream(
                      new FileInputStream("c:\\data\\input-file.txt"),
                      bufferSize
    );
```

### 2.BufferedOutputStream

```java
OutputStream output = new BufferedOutputStream(
                      new FileOutputStream("c:\\data\\output-file.txt"));

// 设置缓冲区大小, 8KB
int bufferSize = 8 * 1024;
OutputStream output = new BufferedOutputStream(
                      new FileOutputStream("c:\\data\\output-file.txt"),
                          bufferSize
);
```

### 3.DataInputStream

​	DataInputStream可以从输入流中读取Java基本类型数据，而不必每次读取字节数据。

```java
DataInputStream dataInputStream = new DataInputStream(
                            new FileInputStream("binary.data"));

int    aByte   = input.read();
int    anInt   = input.readInt();
float  aFloat  = input.readFloat();
double aDouble = input.readDouble(); //etc.

input.close();
```

### 4.DataOutputStream

​	DataOutputStream可以往输出流中写入Java基本类型数据。

```java
DataOutputStream dataOutputStream = new DataOutputStream(
                            new FileOutputStream("binary.data"));

dataOutputStream.write(45);            //byte data
dataOutputStream.writeInt(4545);       //int data
dataOutputStream.writeDouble(109.123); //double data

dataOutputStream.close();
```

##十二、PrintStream

### 1.使用举例

```java
PrintStream printStream = new PrintStream(outputStream);

printStream.print(true);
printStream.print(123);
printStream.print((float) 123.456);

printStream.close();
```

### 2.printf

```java
PrintStream printStream = new PrintStream(outputStream);
// 格式化输出
printStream.printf(Locale.UK, "Text + data: %1$", 123);

printStream.close();
```

## 十三、Reader和Writer

​	Reader基于字符而非基于字节，用于读取文本。Java内部使用**UTF8**编码表示字符串。输入流中一个字节可能并不等同于一个UTF8字符。如果从输入流中以字节为单位读取UTF8编码的文本，并且尝试将读取到的字节转换成字符，可能会得不到预期的结果。

​	Writer基于字符而非基于字节，用于写入文本。Writer的write(int c)方法，会将传入参数的低16位写入到Writer中，忽略高16位的数据。

##十四、InputStreamReader和OutputStreamWriter

###1.InputStreamReader

```java
InputStream inputStream = new FileInputStream("c:\\data\\input.txt");
// 构造转换流，指定字符集。(字节流-->字符流)
Reader reader = new InputStreamReader(inputStream, "UTF-8");
// 读取一个字符
int data = reader.read();

while(data != -1){
	// 转为字符，无数据损失
    char theChar = (char) data;
    data = reader.read();
}
// 关闭流
reader.close();
```

### 2.OutputStreamWriter

```java
OutputStream outputStream = new FileOutputStream("c:\\data\\output.txt");
// 字符流-->字节流(转换流)，指定编码
Writer outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
outputStreamWriter.write("Hello World");
outputStreamWriter.close();
```

## 十五、FileReader与FileWriter

### 1.FileReader

```java
Reader fileReader = new FileReader("c:\\data\\input-text.txt");

int data = fileReader.read();
while(data != -1) {
  //do something with data...
  doSomethingWithData(data);

  data = fileReader.read();
}
fileReader.close();
```

​	FileReader能够以字符流的形式读取文件内容。如果你想明确指定一种编码方案，**利用InputStreamReader配合FileInputStream来替代FileReader(FileReader没有可以指定编码的构造函数)**。InputStreamReader可以让你设置编码处理从底层文件中读取的字节。

### 2.FileWriter

​	FileWriter能够把数据以字符流的形式写入文件。

```java
Writer fileWriter = new FileWriter("data\\filewriter.txt");

fileWriter.write("data 1");
fileWriter.write("data 2");
fileWriter.write("data 3");

fileWriter.close();
```

​	通过指定构造器参数，表明是否覆盖或者追加该文件。

```java
Writer fileWriter = new FileWriter("c:\\data\\output.txt");  // overrides file

Writer fileWriter = new FileWriter("c:\\data\\output.txt", true);  //appends to file

Writer fileWriter = new FileWriter("c:\\data\\output.txt", false); //overwrites file
```

​	**FileWriter不能指定编码，可以通过OutputStreamWriter配合FileOutputStream替代FileWriter。**

## 十六、CharArrayReader与CharArrayWriter

### 1.CharArrayReader

```java
char[] chars = "123".toCharArray();

CharArrayReader charArrayReader = new CharArrayReader(chars);

int data = charArrayReader.read();
while(data != -1) {
  //do something with data

  data = charArrayReader.read();
}

charArrayReader.close();

System.out.println("------------------------------------------------------")
    
// 根据字符数组的一部分内容创建CharArrayReader
char[] chars = "0123456789".toCharArray();

int offset = 2;
int length = 6;

CharArrayReader charArrayReader = new CharArrayReader(chars, offset, length);
```

### 2.CharArrayWriter

```java
CharArrayWriter charArrayWriter = new CharArrayWriter();

charArrayWriter.write("CharArrayWriter");

char[] chars1 = charArrayWriter.toCharArray();

charArrayWriter.close();
```

```java
int initialSize = 1024;
// 指定字符数组大小
CharArrayWriter charArrayWriter = new CharArrayWriter(initialSize);
```

## 十七、BufferedReader与BufferedWriter

### 1.BufferedReader

```java
int bufferSize = 8 * 1024;
// 指定缓冲区大小
BufferedReader bufferedReader = new BufferedReader( new FileReader("c:\\data\\input-file.txt"), bufferSize);

// readLine()方法
String line = bufferedReader.readLine();
```

###2.BufferedWriter

```java
int bufferSize = 8 * 1024;
// 指定缓冲区大小
BufferedWriter bufferedWriter =  new BufferedWriter(new FileWriter("c:\\data\\output-file.txt"), bufferSize);
```



##参考文献

- [Java IO Tutorial](http://tutorials.jenkov.com/java-io/index.html)
- [Java IO教程](http://ifeve.com/java-io/)

