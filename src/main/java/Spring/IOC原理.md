# IOC原理 #

## 一、核心组件 ##
1.Bean组件

包含Bean的定义、创建与解析三部分。

(1)Bean的定义

Bean的定义由BeanDefinition描述。Bean的定义完整描述了在Spring配置文件中定义的<bean/>节点中的所有信息，包括子节点。当Spring解析到一个<Bean/>节点后，在Spring内部就被转换为BeanDefinition对象，以后所有的操作都对这个对象进行。

(2)Bean创建

工厂设计模式，顶层接口是BeanFactory.
![](http://i.imgur.com/uEBbYBb.png)

(3)Bean的解析

Bean的解析主要是对Spring配置文件的解析。

![](http://i.imgur.com/Yzs4u65.png)

2.Context组件

	1. 标示一个应用环境
	2. 利用BeanFactory创建Bean对象
	3. 保存对象关系表
	4. 能够捕获各种事件

Context作为Spring的IOC容器，整合了Spring的 大部分功能，或者是大部分功能的基础。

3.Core组件

Core组件一个重要的功能就是定义了资源的访问方式。

## 二、IOC的工作原理 ##

1.5个重要组件的接口

	Resource
	BeanDefinition
	BeanDefinitionReader
	BeanFactory
	ApplicationContext

(1)Resource

是对资源的抽象，每一个接口实现类都代表了一种资源类型，如 ClasspathResource 、 URLResource ， FileSystemResource 等。每一个资源类型都封装了对某一种特定资源的访问策略。它是 spring 资源访问策略的一个基础实现，应用在很多场景。

![](http://dl.iteye.com/upload/attachment/536177/ead09fcb-3c6c-3740-9e36-6de33fd65cca.jpg)

(2)BeanDefinition

用来抽象和描述一个具体 bean 对象。是描述一个 bean 对象的基本数据结构。

(3)BeanDefinitionReader
BeanDefinitionReader 将外部资源对象描述的 bean 定义统一转化为统一的内部数据结构 BeanDefinition 。对应不同的描述需要有不同的 Reader 。如 XmlBeanDefinitionReader 用
来读取 xml 描述配置的 bean 对象。

![](http://dl.iteye.com/upload/attachment/536179/c4143d16-02e2-3d7b-9734-4d19a9a984dd.jpg)

(4)BeanFactory

用来定义一个很纯粹的 bean 容器。它是一个 bean 容器的必备结构。同时和外部应用环境等隔离。 BeanDefinition 是它的基本数据结构。它维护一个 BeanDefinitions Map, 并可根据 BeanDefinition 的描述进行 bean 的创建和管理。

![](http://dl.iteye.com/upload/attachment/536181/24095923-75cd-363b-bc2f-9fbc603c341f.jpg)

(5)ApplicationContext

从名字来看叫应用上下文，是和应用环境息息相关的。没错这个就是我们平时开发中经常直接使用打交道的一个类，应用上下文，或者也叫做 spring 容器。其实它的基本实现是会持有一个 BeanFactory 对象，并基于此提供一些包装和功能扩展。为什么要这么做呢？因为 BeanFactory 实现了一个容器基本结构和功能，但是与外部环境隔离。那么读取配置文件，并将配置文件解析成 BeanDefinition ，然后注册到 BeanFactory 的这一个过程的封装自然就需要 ApplicationContext 。 ApplicationContext 和应用环境细细相关，常见实现有 ClasspathXmlApplicationContext,FileSystemXmlApplicationContext,WebApplicationContext 等。 Classpath 、 xml 、 FileSystem 、 Web 等词都代表了应用和环境相关的一些意思，从字面上不难理解各自代表的含义。
当然 ApplicationContext 和 BeanFactory 的区别远不止于此，有：
1.  资源访问功能：在 Resource 和 ResourceLoader 的基础上可以灵活的访问不同的资源。
2.  支持不同的信息源。
3.  支持应用事件：继承了接口 ApplicationEventPublisher ，这样在上下文中为 bean 之间提供了事件机制。

![](http://dl.iteye.com/upload/attachment/536183/c456f949-7b9c-34db-ad1a-ca3141219b6d.jpg)

**以上 5 个组件基本代表了 ioc 容器的一个最基本组成，而组件的组合是放在 ApplicationContext 的实现这一层来完成。**

<font color=red>以ClasspathXmlApplicationContext 容器实现为例，其组合关系如下：</font>

![](http://dl.iteye.com/upload/attachment/536932/ae2612a4-5840-3b00-a585-037a5a4980ac.jpg)

ClassPathXmlApplicationContext的refresh() 方法负责完成了整个容器的初始化。

为 什么叫refresh？也就是说其实是刷新的意思，该IOC容器里面维护了一个单例的BeanFactory，如果bean的配置有修改，也可以直接调用 refresh方法，它将销毁之前的BeanFactory，重新创建一个BeanFactory。所以叫refresh也是能理解的。

以下是Refresh的基本步骤：<br>
1.把配置xml文件转换成resource。resource的转换是先通过ResourcePatternResolver来解析可识别格式的配置文件的路径<br>
(如"classpath*:"等)，如果没有指定格式，默认会按照类路径的资源来处理。<br> 
2.利用XmlBeanDefinitionReader完成对xml的解析，将xml Resource里定义的bean对象转换成统一的BeanDefinition。<br>
3.将BeanDefinition注册到BeanFactory，完成对BeanFactory的初始化。BeanFactory里将会维护一个BeanDefinition的Map。<br>

当getBean的时候就会根据调用BeanFactory，根据bean的BeanDifinition来实例化一个bean。当然根据bean的lazy-init、protetype等属性设置不同以上过程略有差别。

