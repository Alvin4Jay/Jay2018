# AOP原理 #

1.AOP概念

AOP(Aspect Oriented Programming)是从另外一个角度来考虑整个程序的，AOP将每一个方法调用，即连接点作为编程的入口，针对方法调用进行编程。从执行的逻辑上来看，相当于在之前纵向的按照时间轴执行的程序横向切入。相当于将之前的程序横向切割成若干的面，即Aspect.每个面被称为切面。

![](http://img.blog.csdn.net/20160410143816511)

2.动态代理

![](http://img.blog.csdn.net/20140603152131484?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDM0OTE2OQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

(1)InvocationHandler

	Object invoke(Object proxy, Method method, Object[] args) throws Throwable

	proxy:　　指代我们所代理的那个真实对象
	method:　　指代的是我们所要调用真实对象的某个方法的Method对象
	args:　　指代的是调用真实对象某个方法时接受的参数

(2)Proxy
	
	public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h) throws IllegalArgumentException

	loader:　　一个ClassLoader对象，定义了由哪个ClassLoader对象来对生成的代理对象进行加载

	interfaces:　　一个Interface对象的数组，表示的是我将要给我需要代理的对象提供一组什么接口，如果我提供了一组接口给它，那么这个代理对象就宣称实现了该接口(多态)，这样我就能调用这组接口中的方法了

	h:　　一个InvocationHandler对象，表示的是当我这个动态代理对象在调用方法的时候，会关联到哪一个InvocationHandler对象上

3.Spring 实现AOP的功能

分为两步。

(1)通过ProxyFactoryBean生成代理对象

a) ProxyFactoryBean.getObject() 方法
	
	生成通知链initializeAdvisorChain()到缓存中---->getSingletonInstance()方法

	public Object getObject() throws BeansException {
		initializeAdvisorChain(); //初始化advisor方法链  
		if (isSingleton()) { /返回单例实例
			return getSingletonInstance();
		}
		else {
			if (this.targetName == null) {
				logger.warn("Using non-singleton proxies with singleton targets is often undesirable. " +
						"Enable prototype proxies by setting the 'targetName' property.");
			}
			return newPrototypeInstance();  //返回prototype原形实例  
		}
	}

b) getSingletonInstance()方法
	
	private synchronized Object getSingletonInstance() {
		if (this.singletonInstance == null) {
			//获得目标对象
			this.targetSource = freshTargetSource();
			if (this.autodetectInterfaces && getProxiedInterfaces().length == 0 && !isProxyTargetClass()) {
				// Rely on AOP infrastructure to tell us what interfaces to proxy.
				Class<?> targetClass = getTargetClass();
				if (targetClass == null) {
					throw new FactoryBeanNotInitializedException("Cannot determine target class for proxy");
				}
				//设置代理对象的接口
				setInterfaces(ClassUtils.getAllInterfacesForClass(targetClass, this.proxyClassLoader));
			}
			// 调用工厂方法获取代理对象
			super.setFrozen(this.freezeProxy);
			this.singletonInstance = getProxy(createAopProxy());
		}
		return this.singletonInstance;
	}

	AopProxy的工厂方法getProxy(createAopProxy())获取代理对象

c) 构造代理工厂

	@Override
	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
			Class<?> targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			//如果目标对象实现了接口  则构造一个JDK动态代理工程 
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(config);
			}
			//否则构造一个CGLIB动态代理工厂 
			return new ObjenesisCglibAopProxy(config);
		}
		else {
			return new JdkDynamicAopProxy(config);
		}
	}

d) JdkDynamicAopProxy类

①JDK动态代理

	@Override
	public Object getProxy(ClassLoader classLoader) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating JDK dynamic proxy: target source is " + this.advised.getTargetSource());
		}
		//获取代理接口
		Class<?>[] proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised, true);
		findDefinedEqualsAndHashCodeMethods(proxiedInterfaces);
		//返回代理实例
		return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);
	}

② CGLIB方式

// 配置CGLIB enhancer增强对象  

(2)JDK代理对象生效

a) 这个invoke方法实际上定义在JdkDynamicAopProxy类中，他实现了我们熟悉的InvocationHandler接口。这个方法是整个AOP过程的提纲，整个过程的几个核心步骤都在此处进行了调用。特别需要注意的是，对于equals和hashcode方法，spring是不会对他们进行增强处理的。

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodInvocation invocation;
		Object oldProxy = null;
		boolean setProxyContext = false;
		
		// 获得目标对象
		TargetSource targetSource = this.advised.targetSource;
		Class<?> targetClass = null;
		Object target = null;

		try {
			//对于equals  hashcode方法不做AOP增强，直接执行方法 
			if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {
				// The target does not implement the equals(Object) method itself.
				return equals(args[0]);
			}
			
			else if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
				// The target does not implement the hashCode() method itself.
				return hashCode();
			}
			else if (method.getDeclaringClass() == DecoratingProxy.class) {
				// There is only getDecoratedClass() declared -> dispatch to proxy config.
				return AopProxyUtils.ultimateTargetClass(this.advised);
			}
			else if (!this.advised.opaque && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				// Service invocations on ProxyConfig with the proxy config...
				return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
			}
			//定义方法返回值
			Object retVal;

			if (this.advised.exposeProxy) {
				// Make invocation available if necessary.
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}

			// May be null. Get as late as possible to minimize the time we "own" the target,
			// in case it comes from a pool.
			target = targetSource.getTarget();
			if (target != null) {
				targetClass = target.getClass();
			}

			// 获得AOP拦截方法链 在这里面完成了pointcut与advice的匹配 它确定了这个方法都需要有哪些增强处理  
			List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

			// 如果方法链为空，那么不执行任何增强处理，直接调用方法  
			if (chain.isEmpty()) {
				// We can skip creating a MethodInvocation: just invoke the target directly
				// Note that the final invoker must be an InvokerInterceptor so we know it does
				// nothing but a reflective operation on the target, and no hot swapping or fancy proxying.
				Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
				retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
			}
			else {
				// We need to create a method invocation...
				invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
				//增强的方法执行入口,调用通知链
				retVal = invocation.proceed();
			}

			// Massage return value if necessary.
			Class<?> returnType = method.getReturnType();
			if (retVal != null && retVal == target &&
					returnType != Object.class && returnType.isInstance(proxy) &&
					!RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
				// Special case: it returned "this" and the return type of the method
				// is type-compatible. Note that we can't help if the target sets
				// a reference to itself in another returned object.
				retVal = proxy;
			}
			else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
				throw new AopInvocationException(
						"Null return value from advice does not match primitive return type for: " + method);
			}
			return retVal;
		}
		finally {
			if (target != null && !targetSource.isStatic()) {
				// Must have come from TargetSource.
				targetSource.releaseTarget(target);
			}
			if (setProxyContext) {
				// Restore old proxy.
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}

b) 获取匹配的advice方法

	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) {
		//缓存对象
		MethodCacheKey cacheKey = new MethodCacheKey(method);
		List<Object> cached = this.methodCache.get(cacheKey);
		if (cached == null) {
			//通过工厂得到匹配的advice对象
			cached = this.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(
					this, method, targetClass);
			this.methodCache.put(cacheKey, cached);
		}
		return cached;
	}

c) proceed方法链调用

如果没找到advice，则直接调用目标方法，同a);

如果找到通知链，递归执行ReflectiveMethodInvocation.proceed()

	public Object proceed() throws Throwable {
		//如果拦截器迭代调用完毕，这里开始调用目标方法，通过反射机制
		if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
			return invokeJoinpoint();
		}
		//依次从方法链list中获得相对应的 interceptor  
		Object interceptorOrInterceptionAdvice =
				this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
		if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
			//对拦截器进行动态判断，如果和定义的切入点匹配，就执行这个通知
			InterceptorAndDynamicMethodMatcher dm =
					(InterceptorAndDynamicMethodMatcher) interceptorOrInterceptionAdvice;
			if (dm.methodMatcher.matches(this.method, this.targetClass, this.arguments)) {
				return dm.interceptor.invoke(this);
			}
			else {
				//不匹配，这个proceed方法递归调用，直到所有的拦截器（通知）都运行过
				return proceed();
			}
		}
		else {
			 //调用对应interceptor中的invoke方法  
			return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
		}
	}

(3)JDKProxy和Cglib的区别

![](https://pic4.zhimg.com/v2-9c094762df7c8bb44c4ba3193fa0979b_r.png)

一、为什么不直接都使用JDK动态代理：

JDK动态代理只能代理接口类，所以很多人设计架构的时候会使用XxxService, XxxServiceImpl的形式设计，一是让接口和实现分离，二是也有助于代理。
有些没有借口，只有类，所以只能用Cglib以继承类的方式实现代理，作为补充。

二、为什么不都使用Cgilb代理：

因为JDK动态代理不依赖其他包，Cglib需要导入ASM包，对于简单的有接口的代理使用JDK动态代理可以少导入一个包。

(4)SpringAOP过程总结

经过了上面的分析，发现AOP的过程并不复杂。就如开篇所说，它分为两个部分

第一是得到代理对象，这里根据目标对象的不同，可以使用JDK动态代理或者CGLIB来生成代理对象，对于Advisor，仅是将他们设置到对应的对象中，并没有进行任何其他的处理

第二个过程是AOP方法驱动的过程，对于目标方法的调用，他们或是驱动了invoke方法，或是调用了intercept方法，这样就进入到AOP增强的过程。在这个过程中，完成了获得advice方法，封装advice方法到对应的interceptor，组装advice方法链，递归调用proceed方法这一系列的过程，这样就完成了AOP增强的功能。
