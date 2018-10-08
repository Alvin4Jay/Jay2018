##常用Linux命令

###一、基本指令
1. `grep`——查找
    ```
    grep [options] pattern [files]
    grep root /etc/passwd  在passwd文件中查找root
    grep -v 反向查找，即查找不带pattern的内容
    grep -c 看有多少行匹配到 grep -c bash /etc/passwd
    grep -cv 查找不带pattern的内容有几行
    
    -i 忽略pattern的大小写
    -r 递归搜索，在某个目录下搜索全部匹配的文件
    -l 只显示文件名，不显示匹配到的行
    -E 扩展模式，后可接正则表达式
    
    
    正则表达式：
    grep -ni '^A' test  //-n列出行号，^匹配句首
    grep -ni '\.$'  test // \起转义作用，$标识匹配句尾
    grep -ni '^$' test //匹配空行
    grep -ni '.' test  //匹配任意字符
    grep -n 'l*' test //l零次或多次重复
    grep -n '\b.re\b' test // \b单词分界线，空格或者tab
    ```
2. `sort`—给文件内容排序
    ```
    sort file  按照第一个字符(字母或数字)排序，将文件内容每行输出
    sort -n file 依照整体数值的大小排序
    sort -n -t : -k 4 /etc/passwd  -t指定分隔符，-k指定列数，-n指定整体数值比较（banana:30:5.5:222）
    
    组合：
    ps -ef | sort -n //给进程排序
    ls -al | sort -nk //根据文件大小排序
    ls -al | sort -nrk //根据文件大小反向排序
    
    ```
3. `uniq`
    ```
    uniq file 去重  -c 显示重复次数， -u 显示单一行
    ```
4. `stat`
   ```
   stat file  //显示文件详细信息,比ls -l详细
   ```
5. `tail -f`
    ```
    tail -f file  //显示文件最新追加的内容,查看日志
    tail file  //显示文件的最后10行
    tail +20 file //显示从第20行开始，到末尾的内容
    ```
6. `less/more`
    ```
    文件行数超过terminal高度，用more/less查看
    more 只能往下翻， less允许用户往上翻
    
    搜索移动 /向下搜索关键词  ?向上搜索  n搜搜索下一个  N搜索上一个 
    翻页  f向前翻一页 b向后翻一页  d向下翻半页  u向上翻半页
    移动  G移到末尾 g移到头部  q或ZZ退出 10j向下移动10行 5k向上移动5行
    模仿 tail -f追踪文件流， 按 F ，ctrl+C退出
    ctrl+g 显示当前进度，文件信息（行数、字节数）
    v 使用默认编辑器编辑文件
    less file1 file2  多文件操作
    ```
7. `wget`
    ```
    1.下载单一文件  wget "http://url.com/file"
    2.将下载的文件重命名  wget "http://url.com/file" -O rename
    3.下载限速 wget --limit-rate=200k "http://url.com/file.tar.bz2"  (200kB/s)
    4.继续下载  wget -c "http://url.com/file.tar.bz2"  防止网络中断而重新下载，可以继续下载
    5.后台下载 wget -b "http://url.com/downloads.tar" -o downloads.log -O download_file" -o指定记录文件名称，-O指定所下载文件的名称
    6.指定下载的User Agent wget --user-agent="I'm not Wget.." "http://url.com/downloads.tar"
    7.测试目标文件 wget --spider "localhost/user.zip" -O /dev/null
    8.设置重试次数   wget --tries=75 "http://url.com/file.tar.bz2"
    9.批量下载 wget -i url.txt   //url.txt存放很多下载链接
    10.超过设定大小后自动退出  wget -Q5m -i url.txt
    11.仅下载特定类型的文件  wget -r -A.pdf "http://url-to-webpage-with-pdfs.com/"  -r表示递归下载，所有pdf文件
    12.ftp登录 wget --ftp-user=USERNAME --ftp-password=PASSWORD "DOWNLOAD.com"
    
    ```
    
8. `telnet`    
 
    telnet ip port
    
9. `ls`   

    ls -lh | grep pattern  //以人性化的方式查找包含pattern的文件  
    
###二、系统性能监控指令

1. `free`——显示系统的内存和交换空间的使用状态
    ```
    free -m以MB为单位显示，-g以GB为单位显示，-h以易读的方式显示，-t显示统计行(显示内存总和列)
    ```
2. `top`
    ```
    top 实时显示当前CPU运行状态, 内存使用状态, 系统负载状态, 进程列表等
    ```
3. `df`
    ```
    df -h 以可读性较高的方式查看磁盘空间
    df -Th  T显示分区的类型
    ```
4. `du`
    ```
    显示目录及其内文件的大小.
    ```
5. `netstat`——显示网络连接的状态
    ```
    列出所有端口 (包括监听和未监听的)
    netstat -a 显示当前活动的网络连接 netstat -an
    netstat -at 列出所有tcp端口
    netstat -au 列出所有udp端口
    
    列出所有处于监听状态的 Sockets
    netstat -l 列出所有处于监听状态的socket
    netstat -lt       #只列出所有监听 tcp 端口
    netstat -lu       #只列出所有监听 udp 端口
    netstat -lx       #只列出所有监听 UNIX 端口
    
    显示每个协议的统计信息
    netstat -s   显示所有端口的统计信息
    netstat -st   显示TCP端口的统计信息
    netstat -su   显示UDP端口的统计信息
    
    显示核心路由信息 netstat -r
    
    netstat –-tcp –-numeric  列出本机的TCP连接
    netstat --tcp --listening –-programs  显示系统正在监听的端口以及程序
    ```    
6. `ps`
    ```
    ps aux  显示当前系统中所有正在运行的进程
    ps auxf  打印进程树
    ps fU mr 
    
    ```

###三、禁用命令

    
    vi file   //将文件全部加载到内存，如果file很大，内存可能瞬间撑满，导致其他java进程退出。
    kill -9 pid号  //杀死进程
    rm -r //递归删除
    
