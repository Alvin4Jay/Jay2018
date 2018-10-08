## 多模块项目的POM重构

父模块  多个子模块

###一、dependencyManagement——消除多模块依赖配置重复
```
//父模块
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactid>junit</artifactId>
      <version>4.8.2</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>log4j</groupId>
      <artifactid>log4j</artifactId>
      <version>1.2.16</version>
    </dependency>
  </dependencies>
</dependencyManagement>
```

```
//子模引入依赖
<dependency>
    <groupId>junit</groupId>
    <artifactid>junit</artifactId>
  </dependency>
  <dependency>
    <groupId>log4j</groupId>
    <artifactid>log4j</artifactId>
  </dependency>
```
在多模块Maven项目中，dependencyManagement几乎是必不可少的，因为只有它是
才能够有效地帮我们维护依赖一致性。

###二、把dependencyManagement放到单独的POM中

把dependencyManagement放到单独的专门用来管理依赖的POM中，然后在需要使用依
赖的模块中通过import scope依赖，就可以引入dependencyManagement。（非继承
的方式）

```
//专门的POM
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.juvenxu.sample</groupId>
  <artifactId>sample-dependency-infrastructure</artifactId>
  <packaging>pom</packaging>
  <version>1.0-SNAPSHOT</version>
  <dependencyManagement>
    <dependencies>
        <dependency>
          <groupId>junit</groupId>
          <artifactid>junit</artifactId>
          <version>4.8.2</version>
          <scope>test</scope>
        </dependency>
        <dependency>
          <groupId>log4j</groupId>
          <artifactid>log4j</artifactId>
          <version>1.2.16</version>
        </dependency>
    </dependencies>
  </dependencyManagement>
</project>
```
```
//通过非继承的方式来引入这段依赖管理配置
 <dependencyManagement>
    <dependencies>
        <dependency>
          <groupId>com.juvenxu.sample</groupId>
          <artifactid>sample-dependency-infrastructure</artifactId>
          <version>1.0-SNAPSHOT</version>
          <type>pom</type>
          <scope>import</scope>
        </dependency>
    </dependencies>
  </dependencyManagement>
  //使用
  <dependency>
    <groupId>junit</groupId>
    <artifactid>junit</artifactId>
  </dependency>
  <dependency>
    <groupId>log4j</groupId>
    <artifactid>log4j</artifactId>
  </dependency>
```

###三、多模块插件配置重复

```
//使用pluginManagement元素管理插件
<build>
  <pluginManagement>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>2.3.2</version>
        <configuration>
          <source>1.5</source>
          <target>1.5</target>
          <encoding>UTF-8</encoding>
        </configuration>
      </plugin>
    </plugins>
  </pluginManagement>
</build>
```

不同子模块之间插件配置一般不一致，因此只有那些普适的插件配置才应该使用
pluginManagement提取到父POM中。