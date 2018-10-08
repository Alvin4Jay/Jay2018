# InnoDB关键特性 #

## 一、插入缓冲(Insert Buffer) ##

###1.Insert Buffer(非聚集索引优化)

####(a)聚集索引与非聚集索引简介

在InnodB存储引擎中，**主键(聚集索引)**是行唯一的标识符。应用程序中行记录的插入顺序是按照主键递增的顺序插入的。所以，插入**聚集索引(Primary Key)**是顺序插入的，不需要磁盘的随机读取。

	CREATE TABLE t(
		a INT AUTO_INCREMENT,
		b VARCHAR(30),
		PRIMARY KEY(a) //聚集索引,唯一性
	);

每一张表上不可能只有一个聚集索引，更多情况下，一张表上有多个**非聚集索引的辅助索引(secondary index)**。

	CREATE TABLE t(
		a INT AUTO_INCREMENT,
		b VARCHAR(30),
		PRIMARY KEY(a) //聚集索引,唯一性
		key(b)  //辅助索引，非唯一
	);

在这种情况下产生了一个**非聚集的且不是唯一的索引**。在进行插入操作时，**数据页的存放还是按照主键a进行顺序存放的**，但是对于非聚集索引叶子结点的插入不再是顺序的，这需要**离散的访问非聚集索引页**，由于随机读取的存在而导致插入性能的下降，这是有**Demo.B+树的特性**决定了非聚集索引插入的离散型。

####(b)Insert Buffer工作原理

对于为非唯一索引，辅助索引的修改操作并非实时更新索引的叶子页，而是把若干对同一页面的更新缓存起来做，合并为**一次性更新操作，减少IO，转随机IO为顺序IO**,这样可以避免随机IO带来性能损耗，提高数据库的写性能。

先判断要更新的这一页在不在缓冲池中

a、若在，则直接插入；

b、若不在，则将index page 存入Insert Buffer，按照Master Thread的调度规则来合并非唯一索引和索引页中的叶子结点。

####(c)Insert Buffer使用需满足的条件

- 索引是辅助索引
- 索引不是唯一的(数据库不会去查索引是否唯一)

####(d)插入缓冲对于非聚集索引页的离散IO逻辑请求降低了2/3; IBUF_POOL_SIZE_PER_MAX_SIZE可对插入缓冲的内存占用情况进行限制。


###2.Change Buffer(非聚集的辅助索引)

InnoDB引擎在1.0.x版本引入Change Buffer，是对Insert Buffer的升级。引擎可以对DML操作进行缓冲——INSERT、DELETE、UPDATE,分别是Insert Buffer,Delete Buffer,Purge Buffer。

###3.Insert Buffer实现

####1.简介
Insert Buffer的数据结构是B+树，全局只有一颗Insert Buffer Demo.B+树,负责对所有的表的辅助索引进行Insert Buffer。Demo.B+树存放在共享表空间。


####2.实现
1.Demo.B+树由叶子结点和非叶子结点组成。**insert buffer的非叶子节点存放的是查询的search key**（键值）

其构造包括三个字段：space （4 byte）+ marker（1byte） + offset（4byte） = search key （9 byte ）

space表示待插入记录所在的(索引)表空间id，InnoDB中，每个表有一个唯一的space id，可以通过space id查询得知是哪张表；<br>
marker是用来兼容老版本的insert buffer；<br>
offset表示页所在的偏移量。<br>

2.当一个辅助索引需要插入到页（space， offset）时，如果这个页不在缓冲池中，那么InnoDB首先根据上述规则构造一个search key，接下来查询insert buffer这棵B+树，然后再将这条记录插入到insert buffer Demo.B+树的叶子节点中.

3.对于插入到insert buffer Demo.B+树叶子节点的记录，需要根据如下规则进行构造：

space | marker | offset | metadata | secondary index record


####3.Insert Buffer Bitmap

启用insert buffer索引后，辅助索引页（space、page_no）中的记录可能被插入到insert buffer Demo.B+树中，所以为了保证每次merge insert buffer页必须成功，还需要有一个特殊的页来标记每个辅助索引页（space、page_no）的可用空间，这个页的类型为insert buffer bitmap。

####4.Merge Insert Buffer

- 辅助索引页被读取到缓冲池(内存)中时
- Insert Buffer Bitmap页追踪到该辅助索引页已无可用空间
- Master Thread(合并插入缓冲)


## 二、两次写(Double Write) ##

保证数据页的可靠性，防止数据丢失。


###1.页断裂

页断裂是数据库宕机时(OS重启，或主机掉电重启)，数据库页面只有部分写入磁盘，导致页面出现不一致的情况。

###2.double write

页断裂可以通过重做日志进行恢复。在应用重做日志前，用于需要一个页的副本，当写入失效时，先通过页的副本还原该页，再进行重做。

###3.原理
![](http://onh97xzo0.bkt.clouddn.com/%E6%8D%95%E8%8E%B7.PNG)

在对缓冲池的脏页进行缓冲时，不是直接写磁盘，而是通过memcpy函数将脏页先复制到内存中的doublewrite buffer，之后通过doublewrite buffer再分两次，每次1MB顺序地写入共享表空间的物理磁盘上，马上调用`fsync`函数，同步磁盘，避免缓冲带来的问题。

## 三、自适应哈希索引(Adaptive Hash Index，索引页查询优化) ##

Demo.B+树的查找时间取决于B+树的高度，在生产环境中B+树的高度一般为3-4层，需要查询3-4次。

InnoDB存储引擎会监控对表上各索引页的查询。如果观察到建立哈希索引可以带来速度提升，则建立哈希索引，称之为自适应哈希索引(Adaptive Hash Index, AHI)。AHI是通过缓冲池的B+树页构造而来，因此建立的速度很快，而且不需要对整张表构建哈希索引。InnoDB存储引擎会自动根据访问的频率和模式来自动地为某些热点页建立哈希索引。

AHI有一个要求，对这个页的连续访问模式必须是一样的。例如对于(a,b)这样的联合索引页，其访问模式可以是下面情况： <br>
1）where a=xxx <br>
2）where a =xxx and b=xxx<br>

AHI还有下面几个要求： <br>
1)以该模式访问了100次 <br>
2)页通过该模式访问了N次，其中N=页中记录*1/16<br>

InnoDB存储引擎官方文档显示，启用AHI后，读取和写入速度可以提高2倍，辅助索引的连接操作性能可以提高5倍。AHI的设计思想是数据库自优化，不需要DBA对数据库进行手动调整。 

<font color=red>哈希索引只能用来搜索等值的查询，如select * from table where index_col=’xxx’; 
对于其它类型的查找，比如范围查找，是不能使用哈希索引的.</font>

## 四、异步IO(Async IO) ##

目的是提高磁盘的操作性能。优势：

- 用户可以在发出一个IO请求后立即再发出另一个IO请求，当全部IO请求发送完毕后，等待所有IO操作的完成，即AIO。
- 可以进行IO Merge操作，将多个IO合并为一个IO，提高每秒读写次数(IOPS)。

## 五、刷新领接页(Insert Buffer) ##

当刷新一个脏页时，InnoDB存储引擎会检测该页所在区(extent)的所有页，如果是脏页，那么一起进行刷新。 通过AIO可以将多个IO写入操作合并为一个IO操作，故该工作机制在传统机械磁盘下有着显著的优势。

## 六、InnoDB引擎启动、关闭与恢复 ##

###1.关闭innodb_fast_shutdown=
- 0 完成所有的full purge和merge insert buffer操作(如：做InnoDB plugin升级时)，并且将所有的脏页刷新会磁盘。
- 1 默认，不需要完成上述操作，但会刷新缓冲池中的脏页
- 2 不完成上述三个操作，而是将日志写入日志文件，下次启动时，会执行恢复操作recovery
没有正常地关闭数据库(如：kill命令)/innodb_fast_shutdown=2时，需要进行恢复操作。

###2.恢复innodb_force_recovery=
- 0 默认，但需要恢复时执行所有恢复操作
- 1 忽略检查到的corrupt页
- 2 阻止主线程的运行，如主线程需要执行full purge操作，会导致crash
- 3 不执行事务回滚操作
- 4 不执行插入缓冲的合并操作
- 5 不查看撤销日志undo log，InnoDB存储引擎会将所有未提交的事务视为已提交
- 6 不执行前滚的操作