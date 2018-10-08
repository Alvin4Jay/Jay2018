##Java面试题2

###1. List遍历时使用删除操作

1) 会报错的删除方式
    1) 在`Iterator`遍历时使用list删除
    ```
    Iterator<String> it = list.iterator();  
    while(it.hasNext()){  
       String item = it.next();  
       list.remove(item); //报错java.util.ConcurrentModificationException
    }  
    ```
    2) `foreach`遍历方式中删除(本质上也是使用`Iterator`)
    ```
    for(String s : list){  
        list.remove(s); //报错！！！  
    }  
    ```
2) 不会报错，但是有可能漏删或不能完全的删除方式(原因：在remove后list.size()发生了变化（一直在减少），
同时后面的元素会往前移动，导致list中的索引index指向的数据有变化。同时我们的for中的i是一直在加大的！)
    1) 漏删
    ```
    List<Integer> list = new ArrayList<Integer>();  
    list.add(1);  
    list.add(2);  
    list.add(2);  
    list.add(3);  
    list.add(4);  
    System.out.println("----------list大小1：--"+list.size());  
    for (int i = 0; i < list.size(); i++) {  
        if (2 == list.get(i)) {  
            list.remove(i);      
        }  
        System.out.println(list.get(i));  
    }  
    System.out.println("最后输出=" + list.toString()); 
    ```
    2) 不能完全删除的情况
    ```
    List<Integer> list = new ArrayList<Integer>();  
    list.add(1);  
    list.add(2);  
    list.add(2);  
    list.add(3);  
    list.add(4);  
    System.out.println("----------list大小1：--"+list.size());  
    for (int i = 0; i < list.size(); i++) {  
        list.remove(i);  
    }  
    System.out.println("最后输出=" + list.toString()); 
    ```
3) 遍历时建议的删除方式
```
Iterator<Integer> it = list.iterator();  
while(it.hasNext()){  
    Integer item = it.next();  
    if (2 == item) {  
        it.remove(); //使用迭代器的删除方法删除
    }  
    System.out.println(item);  
}  
System.out.println("最后输出=" + list.toString());  
```

###2.`switch`中的参数类型
支持 byte、short、char、int或者其对应的封装类以及 Enum 类型, 也支持字符串

###3.`equals`与`==`的区别
1. ==是一个运算符，它比较的是值
    1) 对于基本数据类型，直接比较其数据值是否相等。
    2) 对于引用类型，==比较的还是值，只不过此时比较的是两个对象变量的内存地址。
2. `equals`具体业务实现

###4.强引用、软引用、弱引用、虚引用
![](file:///D:\AlgorithmsIDEA\Jay2017\java基础\引用.PNG) 
