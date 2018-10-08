#IDEA快捷键

##1.高效定位代码
###1.1 无处不在的跳转


	快速寻找功能快捷键
		ctrl+shift+A
	项目的跳转   
		ctrl+Alt+[]
	文件的跳转
		ctrl+e  最近的文件
		ctrl+shift+e 最近编辑的文件
	浏览修改位置的跳转
		ctrl+shift+backspace
	快速选择代码
		Ctrl+shift+左/右箭头
	最新浏览位置的修改
		ctrl+alt+左/右箭头
	使用书签进行跳转
		标记书签 ctrl+f11+数字或字母/ f11
		跳转书签 ctrl+数字或者字母
		总览书签 shift+F11
	EmacsIdeas 
		Alt + z + ‘p’ + ‘a’ 先按下alt+z，寻找p开头的单词，标示为a
	收藏文件或者函数(定位到函数名)
		Alt + shift + F

###1.2 精准搜索

	定位类
		ctrl+n
	定位文件
		ctrl + shift +n
	定位函数或者属性
		ctrl + shift + alt +n
	字符串
		ctrl + shift + f

##2.代码助手
###2.1 列操作

	每行选中相同的字符(列操作) ctrl+shift+alt +j
	以每个单词为单位跳动 Ctrl+←→
	移动并选中单词 Ctrl+Shift+↔
	大小写切换  Ctrl+Shift+U 
	代码格式化  ctrl+alt+L

###2.2 live template

	创建java类 alt + insert
	Live template    ctrl+shift + A 查找live template配置

###2.3 postfix

    //ctrl+shift+A 查找postfix(不可编辑)
	fori for循环
	sout 输出
	field 属性
	return 返回值
	name.field——可自动添加this.name = name 以及private String name;
	user.nn——if(user!=null){}
	uesr.return——return user——个人在尝试的时候，输入一个r就有return 所以我觉得直接写可能更简便

###2.4 alter+enter

	自动提示创建函数
	list replace //list遍历时，普通循环 替换为 增强for循环
	字符串format //字符串格式化 ||| 字符串连接使用StringBuilder
	实现接口

##3.重构

	重构变量
		Shift + F6
	重构方法签名
		Ctrl+F6 || alter + enter
	抽取
		抽取变量 ctrl+alt +v
		抽取静态变量 ctrl+alt +c
		抽取成员变量 ctrl+alt +f
		抽取方法形参 ctrl+alt +p
		抽取方法 ctrl+alt +m

##4.寻找修改轨迹
###4.1 git集成

	在看不懂的代码前右击，选中annotate，可以找到代码的所有者，更进一步点击，还可以找到该作者的修改记录。
	
	遍历修改记录：Ctrl+Alt+Shift+上下箭头（牛逼的快捷键）
	
	Revert：Ctrl+Alt+Z

###4.2 local history
	
	crtl+shift+A查找

##5.程序调试
	添加/取消断点   ctrl+F8

	单步运行   F8
	
	跳到下一个断点   F9

	查看所有断点    shift+Ctrl+F8
	
	禁止所有断点   debug后在左下角的Mute breakPoints
	
	条件断点   在需要用条件断点的断点处，在断点所在行使用shift+Ctrl+F8

	表达式求值  alt+F8
	
	运行到指定行（光标所在行）   ALT+F9
	
	setValue调试过程中动态改变值   F2


