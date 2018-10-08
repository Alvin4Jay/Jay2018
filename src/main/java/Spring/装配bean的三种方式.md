# 装配bean的方式 #

---
## 一、自动装配 ##

`@Autowired`和`Component`注解(组件扫描、自动装配)

## 二、基于Java的配置 ##

1.一个Java配置类`@Configuration`

2.配置类中声明`@Bean`注解实现Bean生成和注入

## 三、基于XML的配置 ##

1.xml文件配置bean

```
<bean id="compactDisc" class="soundsystem.SgtPeppers"/>
```

2. 注入方式

(1)构造器注入

(a).注入引用

    <bean id="cdPlayer" class="soundsystem.CDPlayer">
        <constructor-arg ref="compactDisc" />  
	</bean>

(b). 注入字面量

    <bean id="compactDisc" class="soundsystem.BlankDisc">
	    <constructor-arg value="Sgt. Pepper's Lonely Hearts Club Band"/>  <!--字面量值注入使用value属性-->
	    <constructor-arg value="The Beatles"/>
	</bean>

(2)设置属性的方式(setter方法)

(a)注入引用

    <bean id="cdPlayer2" class="soundsystem.CDPlayer2">
        <property name="compactDisc" ref="compactDisc"/>  属性setter方式注入 ,使用property元素
    </bean>

(b)注入字面量

    <property name="title" value="Sgt. Pepper's Lonely Hearts Club Band"/>

