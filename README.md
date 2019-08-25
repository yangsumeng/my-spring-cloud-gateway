
####内容
- [X] gatewa的二级动态路由
    1. 一级路由根绝输入请求生成一个二级路由
    2. 跳转到二级路由并执行
- [ ] 自定义全局过滤器
- [ ] 自动以局部过滤器
- [ ] Redis动态路由
- [ ] 占位符使用
- [ ] 集成spring cloud 其他组件


####打jar包方法（也可以自己打包）
1. 执行maven打包命令 mvn clear install
2. 生成的tar.gz文件放到服务器上，解压
3. 外挂配置文件
4. sh service.sh start 执行启动命令