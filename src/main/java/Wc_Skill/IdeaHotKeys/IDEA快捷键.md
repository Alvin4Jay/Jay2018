#IDEA快捷键

##1.高效定位代码
###1.1 无处不在的跳转

	快速寻找功能快捷键
		command+shift+A
	项目的跳转   
		command+`   command+shift+`
	文件的跳转
		command+e  最近的文件  Recent files 
		command+shift+e 最近编辑的文件 Recent Change files
	浏览上次修改位置的跳转
		command+shift+backspace
	快速选择代码
		command+shift+左/右箭头
		option+shift+左/右箭头
	最新浏览位置的修改
		command+option+左/右箭头
	使用书签进行跳转
		标记书签 option+f3+数字或字母/ f3
		跳转书签 ctrl+数字或者字母
		总览书签 command+F3
	EmacsIdeas   
		ctrl + l + ‘p’ + ‘a’   ctrl+l，寻找p开头的单词，标示为a
	收藏文件或者函数(定位到函数名)
		option + shift + F

###1.2 精准搜索

	定位类
		command+o
	定位文件
		command + shift +o
	定位函数或者属性
		command+option+o
	字符串
		command + shift + f

##2.代码助手
###2.1 列操作   

	每行选中相同的字符(列操作) command+ctrl+G
	以每个单词为单位跳动 option+←→
	移动并选中单词 option+Shift+↔
	大小写切换  command+Shift+U 
	代码格式化  command+option+L

###2.2 live template   

	创建java类 command + n
	Live template    ctrl+shift + A 查找live template配置
    command+J //插入live template

###2.3 postfix

    //ctrl+shift+A 查找postfix(不可编辑)
	fori for循环
	sout 输出
	field 属性
	return 返回值
	name.field——可自动添加this.name = name 以及private String name;
	user.nn——if(user!=null){}
	uesr.return——return user——个人在尝试的时候，输入一个r就有return 所以我觉得直接写可能更简便

###2.4 option+enter

	自动提示创建函数
	list replace //list遍历时，普通循环 替换为 增强for循环
	字符串format //字符串格式化 ||| 字符串连接使用StringBuilder
	实现接口

##3.重构

	重构变量
		ctrl + T  || shift + f6
	重构方法签名
		command+F6 || alter + enter
	抽取
		抽取变量 command+option +v
		抽取静态变量 command+option +c
		抽取成员变量 command+option +f
		抽取方法形参 command+option +p
		抽取方法 command+option +m

##4.寻找修改轨迹
###4.1 git集成

	在看不懂的代码前右击，选中annotate，可以找到代码的所有者，更进一步点击，还可以找到该作者的修改记录。
	
	遍历修改记录：control+option+Shift+上下箭头（牛逼的快捷键）
	
	Revert：command+option+Z

###4.2 local history
	
	command+shift+A查找
	put label  //添加local histtory的标签

##5.程序调试   
	添加/取消断点   command+F8
    
    debug ctrl+option+D
    run ctrl+option+R
    
	单步运行   F8
	
	跳到下一个断点   F9

	查看所有断点    shift+command+F8
	
	禁止所有断点   debug后在左下角的Mute breakPoints
	
	条件断点   在需要用条件断点的断点处，在断点所在行使用shift+command+F8

	表达式求值  option+F8
	
	运行到指定行（光标所在行）   option+F9 run to cursor
	
	setValue调试过程中动态改变值   F2
	
    运行函数(当前上下文) ctrl+shift+D/R

    edit configuration shift+command+J
    
##6.其他操作
    #当前文件夹下的文件操作
    copy  F5
    move f6
    
    #文件名copy
    command+c 复制文件名
    command+shift+c 复制完整路径+文件名 
    command+shift+option+C 复制文件引用
    
    shift+command+v  剪切板

    #结构图
    查看文件结构field method  commamd+f12 
    查看maven依赖，类图   shift+option+command+U
    
    #查看类继承结构，  ctrl+H
     方法调用层次   ctrl+option+H   