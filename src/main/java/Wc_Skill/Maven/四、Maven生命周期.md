## Maven生命周期

###1.clean

    mvn clean  //移除根目录下生成的target目录
    
###2.validate

    mvn validate  //检查工程配置是否正确，完成构建过程的所有必要信息是否能够获取到。
    
###3.compile

    mvn compile  //编译工程源码。
    
###4.test
    
    mvn test  //使用适当的单元测试框架（例如JUnit）运行测试。
    
###5.package

    mvn package  //获取编译后的代码，并按照可发布的格式进行打包，例如 JAR、WAR 或者 EAR 文件。
    
###6.verify

    mvn verify  //运行检查操作来验证工程包是有效的，并满足质量要求。
    
###7.install

    mvn install //安装工程包到本地仓库中，该仓库可以作为本地其他工程的依赖。
    
###8.deploy

    mvn deploy //拷贝最终的工程包到远程仓库中，以共享给其他开发人员和工程
    