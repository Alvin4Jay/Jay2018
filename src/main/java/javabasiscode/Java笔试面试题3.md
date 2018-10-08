###Java笔试、面试题3

###1. `String.split()`方法 
    String str = "12.03";  
    String[] res = str.spilt(".");    //出错！！！ 
    //括号里是正则表达式 "\\."
    
###2. `equals` and `hashCode`区别
1. 判断两个对象是否相同，取决于equals方法，而两个对象的hashCode值是否相等是两个对象是否相同的必要条件。
所以有以下结论：
（1）如果两个对象的hashCode值不等，根据必要条件理论，那么这两个对象一定不是同一个对象，即他们的equals方法一定要返回false；
（2）如果两个对象的hashCode值相等，这两个对象也不一定是同一个对象，即他们的equals方法返回值不确定；
反过来，
（1）如果equals方法返回true，即是同一个对象，它们的hashCode值一定相等；
（2）如果equals方法返回false，hashCode值也不一定不相等，即是不确定的；
    
2. 很多时候我们可能会重写equals方法，来判断这两个对象是否相等，此时，为了保证满足上面的结论，
即满足hashCode值相等是equals返回true的必要条件，我们也需要重写hashCode方法，以保证判断两个
对象的逻辑一致（所谓的逻辑一致，是指equals和hashCode方法都是用来判断对象是否相等） HashMap

###3. `ArrayList`、`Vector`、`LeetCode.LinkedList`区别
1) `ArrayList`和`Vector`其底层都是通过new出的Object[]数组实现,在获取数据方面即get()的时候比较高效，而在add()插入
或者remove()的时候，由于需要移动元素，效率相对不高。
2) `ArrayList`和`Vector`区别
`ArrayList`不是线程安全的，而`Vector`是线程安全的
3) `LeetCode.LinkedList`底层是通过双向循环链表实现的，所以在大量增加或删除元素时（即add和remove操作），
由于不需要移动元素有更好的性能。但是在获取数据（get操作）方面要差。
    
    
    