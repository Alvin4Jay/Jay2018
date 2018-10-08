# Spring MVC请求流程 #

![](http://i.imgur.com/eWCg3mL.png)

![](http://i.imgur.com/9J9fIVq.png)

![](http://i.imgur.com/IXNJ5lb.png)

具体化：

![](http://img.blog.csdn.net/20141129165243297?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvemhhb2xpamluZzIwMTI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

具体步骤：

   1、  首先用户发送请求——>DispatcherServlet，前端控制器收到请求后自己不进行处理，而是委托给其他的解析器进行处理，作为统一访问点，进行全局的流程控制；<br>

   2、  DispatcherServlet——>HandlerMapping，HandlerMapping 将会把请求映射为 HandlerExecutionChain 对象（包含一个 Handler 处理器（页面控制器）对象、多个 HandlerInterceptor 拦截器）对象，通过这种策略模式，很容易添加新的映射策略；<br>

   3、  DispatcherServlet——>HandlerAdapter，HandlerAdapter 将会把处理器包装为适配器，从而支持多种类型的处理器，即适配器设计模式的应用，从而很容易支持很多类型的处理器；<br>

   4、  HandlerAdapter——>处理器功能处理方法的调用，HandlerAdapter 将会根据适配的结果调用真正的处理器的功能处理方法，完成功能处理；并返回一个 ModelAndView 对象（包含模型数据、逻辑视图名）；<br>

   5、  ModelAndView 的逻辑视图名——> ViewResolver， ViewResolver 将把逻辑视图名解析为具体的 View，通过这种策略模式，很容易更换其他视图技术；<br>

   6、  View——>渲染，View 会根据传进来的 Model 模型数据进行渲染，此处的 Model 实际是一个 Map 数据结构，因此很容易支持其他视图技术；<br>

   7、  返回控制权给 DispatcherServlet，由 DispatcherServlet 返回响应给用户，到此一个流程结束。

开发步骤:

1、  DispatcherServlet 在 web.xml 中的部署描述，从而拦截请求到 Spring Web MVC <br>
2、  HandlerMapping 的配置，从而将请求映射到处理器 <br>
3、  HandlerAdapter 的配置，从而支持多种类型的处理器 <br>
4、  ViewResolver 的配置，从而将逻辑视图名解析为具体视图技术 <br>
5、  处理器（页面控制器）的配置，从而进行功能处理 <br>