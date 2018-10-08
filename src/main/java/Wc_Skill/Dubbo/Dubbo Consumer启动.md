## Dubbo Consumer启动

1. refer 获取invoker——consumer url注册到zk，并订阅、监听provider、route、comfigurators等节点——notify。
2. notify的过程中会打开netty server 监听端口
3. 获取invoker后，创建代理对象
