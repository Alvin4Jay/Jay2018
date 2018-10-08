## Maven插件命令

###1.clean插件——清除target目录

    //清理构建目录下的全部文件，默认为target目录. 若配置project.build.directory, project.build.outputDirectory, 
    project.build.testOutputDirectory, project.reporting.outputDirectory这四个目录,调用这个插件时同时也会
    清理这几个目录下的文件.
    #1
    mvn clean
    mvn clean:clean
    
    #2
    mvn clean -Dmaven.clean.skip=true  //命令行方式跳过执行清理
    //pom文件配置跳过执行清理
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-clean-plugin</artifactId>
        <version>3.0.0</version>
        <configuration>
            <skip>true</skip>
        </configuration>
    </plugin>
    
    #3忽略清理的错误(如mvn clean package, 防止这样的命令，在clean执行出错后导致整个构建停止)
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-clean-plugin</artifactId>
        <version>3.0.0</version>
        <configuration>
            <failOnError>false</failOnError>
        </configuration>
    </plugin>
    
    http://www.cnblogs.com/qyf404/p/5070126.html
    
###2.compiler插件——编译源码

    #1
    mvn compiler:compile //绑定到compile 阶段，用以编译main/路径下的源代码
    mvn compiler:testCompile  //绑定到test-compile阶段，用以编译test/路径下的源代码
    
    #2指定编译器，现默认为javax.tools.JavaCompiler
    -Dmaven.compiler.forceJavacCompilerUse=true -->使用JDK自带的javac编译器
    
###3.jar插件——打包

    mvn jar:jar   //打包源码文件
    mvn jar:test-jar  //打包测试文件
    
###4.install插件——打包保存到本地仓库

    mvn install:install  //生成项目的构建，保存到本地仓库
    mvn install:install-file //将任意jar或war文件直接保存到本地仓库
    mvn install:install-file -Dfile=test.jar -DgroupId=com.regaltec.test -DartifactId=test -Dversion=1.0 -Dpackaging=jar( -Dfile指定保存的文件路径)

###5.surefire插件——单元测试

    mvn surefire:test  //运行所有测试用例
    mvn test //运行所有测试用例
    mvn test -Dtest=xxx  //运行某个测试用例
    
###6.Spring boot插件

    mvn package spring-boot:repackage //将mvn package生成的软件包，再次打包为可执行的软件包，并将mvn package生成的软件包重命名为*.original

###7.maven-archetype-plugin——生成项目骨架

    mvn archetype:generate //使用交互式的方式提示用户输入必要的信息以创建项目
    
###8.maven-dependency-plugin——依赖分析

    mvn dependency:analyze //依赖分析，分析得到申明但未使用的，使用但未申明的等
    mvn dependency:tree  //描述项目的依赖树
    mvn dependency:list  //项目最终解析到的依赖列表

###9.maven-help-plugin——辅助工具

    mvn help:system  //打印所有可用的环境变量和Java系统属性
    mvn help:effective-pom //打印项目的有效POM，即pom.xml和父pom的集合
        有效POM是指合并了所有父POM（包括Super POM）后的XML，当你不确定POM的某些信息从何而来时，就可以查看有效POM。
    mvn help:effective-setting //有效settings