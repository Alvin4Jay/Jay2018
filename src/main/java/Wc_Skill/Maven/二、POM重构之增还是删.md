##POM重构之增还是删

###一、删

> 1.当POM中存在一些 依赖或者插件 配置，但实际代码没有用到这些配置的时候，
应该尽早删掉它们以免给人带来困惑。

> 2.POM中配置了继承，当前模块与父模块使用同样的groupId和version时，就
可以将`<groupId>`和`<version>`元素删除。
```
<project>
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.juvenxu.sample</groupId>
    <artifactId>sample-parent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
  </parent>
  <artifactId>sample-foo</artifactId> //不能删除，artifactId不一样
  <packaging>jar</packaging>
...
</project>
```


###二、增

插件配置：
    
    //无groupId及version，易读性不高，不可取
    <plugin>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <source>1.5</source>
        <target>1.5</target>
      </configuration>
    </plugin>
    
    //改为
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>2.3.2</version>
      <configuration>
        <source>1.5</source>
        <target>1.5</target>
      </configuration>
    </plugin>
    
    在配置项目依赖的时候，我们也应当一直显式地写明依赖版本，以避免Maven在不同
    的时刻引入不同版本的依赖而导致项目构建的不稳定。
    
    
###三、Maven Dependency Plugin 分析依赖是否删除或增加

    mvn denpendency:analyze  //命令
    
###四、增加项目URL，开发者信息，SCM信息，持续集成服务器信息

    <project>
      <description>...</description>
      <url>...</url>
      <licenses>...</licenses>
      <organization>...</organization>
      <developers>...</developers>
      <issueManagement>...</issueManagement>
      <ciManagement>...</ciManagement>
      <mailingLists>...</mailingLists>
      <scm>...</scm>
    </project>