##ReferenceAnnotationBeanPostProcessor解析——纯基于注解的@Reference解析器，生成属性和方法的代理对象

```java
package com.alibaba.dubbo.config.spring.beans.factory.annotation;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.springframework.core.BridgeMethodResolver.findBridgedMethod;
import static org.springframework.core.BridgeMethodResolver.isVisibilityBridgeMethodPair;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation
 * that Consumer service {@link Reference} annotated fields
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2.5.7
 */
public class ReferenceAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
        implements MergedBeanDefinitionPostProcessor, PriorityOrdered, BeanClassLoaderAware, ApplicationContextAware,
        DisposableBean {

    /**
     * The bean name of {@link ReferenceAnnotationBeanPostProcessor}
     */
    public static final String BEAN_NAME = "referenceAnnotationBeanPostProcessor";

    private final Log logger = LogFactory.getLog(getClass());

    /** Spring应用上下文 */
    private ApplicationContext applicationContext;

    /** Bean类加载器 */
    private ClassLoader classLoader;

    /** 注入元数据缓存 */
    private final ConcurrentMap<String, InjectionMetadata> injectionMetadataCache =
            new ConcurrentHashMap<String, InjectionMetadata>(256);

    /** 引用Bean缓存 */
    private final ConcurrentMap<String, ReferenceBean<?>> referenceBeansCache =
            new ConcurrentHashMap<String, ReferenceBean<?>>(16);

    /// InstantiationAwareBeanPostProcessorAdapter
    ///consumer端，@Reference注解的属性和方法，元数据注入
    @Override
    public PropertyValues postProcessPropertyValues(
            PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName)
            throws BeanCreationException {
        //获取@Reference注解的元数据，包含属性和方法
        InjectionMetadata metadata = findReferenceMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs); //元数据注入，包含属性和方法
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of @Reference dependencies failed", ex);
        }
        return pvs;
    }

    /**
     * Finds {@link InjectionMetadata.InjectedElement} Metadata from annotated {@link Reference @Reference} fields  获取@Reference注解的属性字段的元数据
     *
     * @param beanClass The {@link Class} of Bean
     * @return non-null {@link List}
     */
    private List<InjectionMetadata.InjectedElement> findFieldReferenceMetadata(final Class<?> beanClass) {
        final List<InjectionMetadata.InjectedElement> elements = new LinkedList<InjectionMetadata.InjectedElement>();

        ReflectionUtils.doWithFields(beanClass, new ReflectionUtils.FieldCallback() {

            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                Reference reference = getAnnotation(field, Reference.class);
                if (reference != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("@Reference annotation is not supported on static fields: " + field);
                        }
                        return;
                    }
                    elements.add(new ReferenceFieldElement(field, reference));
                }
            }

        });

        return elements;
    }

    /**
     * Finds {@link InjectionMetadata.InjectedElement} Metadata from annotated {@link Reference @Reference} methods  获取@Reference注解的方法的元数据
     *
     * @param beanClass The {@link Class} of Bean
     * @return non-null {@link List}
     */
    private List<InjectionMetadata.InjectedElement> findMethodReferenceMetadata(final Class<?> beanClass) {
        final List<InjectionMetadata.InjectedElement> elements = new LinkedList<InjectionMetadata.InjectedElement>();

        ReflectionUtils.doWithMethods(beanClass, new ReflectionUtils.MethodCallback() {

            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                Method bridgedMethod = findBridgedMethod(method);
                if (!isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }

                Reference reference = findAnnotation(bridgedMethod, Reference.class);
                if (reference != null && method.equals(ClassUtils.getMostSpecificMethod(method, beanClass))) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("@Reference annotation is not supported on static methods: " + method);
                        }
                        return;
                    }
                    if (method.getParameterTypes().length == 0) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("@Reference annotation should only be used on methods with parameters: " +
                                    method);
                        }
                    }
                    PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, beanClass);
                    elements.add(new ReferenceMethodElement(method, pd, reference));
                }
            }

        });

        return elements;
    }

	//建立@Reference注解的属性和方法的元数据
    /**
     * @param beanClass The {@link Class} of Bean
     * @return {@link InjectionMetadata}
     */
    private InjectionMetadata buildReferenceMetadata(final Class<?> beanClass) {
        final List<InjectionMetadata.InjectedElement> elements = new LinkedList<InjectionMetadata.InjectedElement>();
        elements.addAll(findFieldReferenceMetadata(beanClass));
        elements.addAll(findMethodReferenceMetadata(beanClass));
        return new InjectionMetadata(beanClass, elements);
    }
	//获取@Reference注解的属性和方法的元数据
    private InjectionMetadata findReferenceMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    try {
                        metadata = buildReferenceMetadata(clazz);
                        this.injectionMetadataCache.put(cacheKey, metadata);
                    } catch (NoClassDefFoundError err) {
                        throw new IllegalStateException("Failed to introspect bean class [" + clazz.getName() +
                                "] for reference metadata: could not find class that it depends on", err);
                    }
                }
            }
        }
        return metadata;
    }

    /// MergedBeanDefinitionPostProcessor
    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (beanType != null) {
            InjectionMetadata metadata = findReferenceMetadata(beanName, beanType, null);
            metadata.checkConfigMembers(beanDefinition);
        }
    }
	//顺序
    /// PriorityOrdered
    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    /// BeanClassLoaderAware
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /// ApplicationContextAware
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
	//销毁服务引用
    /// DisposableBean
    @Override
    public void destroy() throws Exception {
        for (ReferenceBean referenceBean : referenceBeansCache.values()) {
            if (logger.isInfoEnabled()) {
                logger.info(referenceBean + " was destroying!");
            }
            // 销毁服务引用
            referenceBean.destroy();
        }

        injectionMetadataCache.clear();
        referenceBeansCache.clear();

        if (logger.isInfoEnabled()) {
            logger.info(getClass() + " was destroying!");
        }
    }

    //获取ReferenceBean的集合
    /**
     * Gets all beans of {@link ReferenceBean}
     *
     * @return non-null {@link Collection}
     */
    public Collection<ReferenceBean<?>> getReferenceBeans() {
        return Collections.unmodifiableCollection(this.referenceBeansCache.values());
    }
	
    //以下两个类是InjectedElement的子类，分别对应@Reference注解的属性和方法元数据
    /**
     * {@link Reference} {@link Method} {@link InjectionMetadata.InjectedElement}
     */
    private class ReferenceMethodElement extends InjectionMetadata.InjectedElement {

        private final Method method;

        private final Reference reference;

        ReferenceMethodElement(Method method, PropertyDescriptor pd, Reference reference) {
            super(method, pd);
            this.method = method;
            this.reference = reference;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
            Class<?> referenceClass = pd.getPropertyType();
            Object referenceBean = buildReferenceBean(reference, referenceClass);
            ReflectionUtils.makeAccessible(method);
            method.invoke(bean, referenceBean);
        }

    }

    /**
     * {@link Reference} {@link Field} {@link InjectionMetadata.InjectedElement}
     */
    private class ReferenceFieldElement extends InjectionMetadata.InjectedElement {

        private final Field field;

        private final Reference reference;

        ReferenceFieldElement(Field field, Reference reference) {
            super(field, null);
            this.field = field;
            this.reference = reference;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
            Class<?> referenceClass = field.getType();
            Object referenceBean = buildReferenceBean(reference, referenceClass);
            ReflectionUtils.makeAccessible(field);
            field.set(bean, referenceBean);
        }

    }
	
    //创建ReferenceBean，并缓存
    private Object buildReferenceBean(Reference reference, Class<?> referenceClass) throws Exception {
        String referenceBeanCacheKey = generateReferenceBeanCacheKey(reference, referenceClass);
        ReferenceBean<?> referenceBean = referenceBeansCache.get(referenceBeanCacheKey);
        if (referenceBean == null) {
            ReferenceBeanBuilder beanBuilder = ReferenceBeanBuilder
                    .create(reference, classLoader, applicationContext)
                    .interfaceClass(referenceClass);
            referenceBean = beanBuilder.build();   //重要，这里已经在初始化代理对象了
            referenceBeansCache.putIfAbsent(referenceBeanCacheKey, referenceBean);
        }
        return referenceBean.get(); //生成代理对象
    }

    //生成ReferenceBean的缓存key
    /**
     * Generate a cache key of {@link ReferenceBean}
     *
     * @param reference {@link Reference}
     * @param beanClass {@link Class}
     * @return cache key of {@link ReferenceBean}
     */
    private static String generateReferenceBeanCacheKey(Reference reference, Class<?> beanClass) {
        String interfaceName = resolveInterfaceName(reference, beanClass);
        return reference.group() + '/' + interfaceName + ':' + reference.version();
    }
	//获得引用的接口类型
    private static String resolveInterfaceName(Reference reference, Class<?> beanClass)
            throws IllegalStateException {
        String interfaceName;
        if (!"".equals(reference.interfaceName())) {
            interfaceName = reference.interfaceName();
        } else if (!void.class.equals(reference.interfaceClass())) {
            interfaceName = reference.interfaceClass().getName();
        } else if (beanClass.isInterface()) {
            interfaceName = beanClass.getName();
        } else {
            throw new IllegalStateException(
                    "The @Reference undefined interfaceClass or interfaceName, and the property type "
                            + beanClass.getName() + " is not a interface.");
        }
        return interfaceName;
    }

}

```

