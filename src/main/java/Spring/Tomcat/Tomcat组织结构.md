# Tomcat组织结构

## 一、总体结构图

![](https://www.ibm.com/developerworks/cn/java/j-lo-tomcat1/image001.gif)

从上图可以看出，Tomcat服务器包含多个Service服务，一个Service服务包含多个Connector组件和一个Container组件，对外提供服务。Connector和Container这两个是Tomcat的核心组件。整个 Tomcat 的生命周期由 Server 控制。

## 二、Service

主要是为了关联 Connector 和 Container，同时会初始化它下面的其它组件，如日志组件Loging、Session组件。

Service 接口的标准实现类是 StandardService,它不仅实现了 Service 接口，同时还实现了 Lifecycle 接口，这样它就可以控制它**下面的组件**的生命周期了。

## 三、Server

能够提供一个接口让其它程序能够访问到这个 Service 集合、同时要维护它所包含的所有 Service 的生命周期，包括如何初始化、如何结束服务、如何找到别人要访问的 Service。

## 四、Connector组件

一个Connecter将在某个指定的端口上侦听客户请求，接收浏览器的发过来的 tcp 连接请求，创建一个 Request 和 Response 对象分别用于和请求端交换数据，然后会产生一个线程来处理这个请求并把产生的 Request 和 Response 对象传给处理Engine(Container中的一部分)，从Engine出获得响应并返回客户。 

## 五、Container(责任链设计模式，处理请求从父容器传递到子容器，并有Servlet处理，返回响应)

Container 是容器的父接口，所有子容器都必须实现这个接口，Container 容器的设计用的是典型的责任链的设计模式，它有四个子容器组件构成，分别是：Engine、Host、Context、Wrapper，这四个组件不是平行的，而是父子关系，Engine 包含 Host,Host 包含 Context，Context 包含 Wrapper。通常一个 Servlet class 对应一个 Wrapper。


