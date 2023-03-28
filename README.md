## 一种优雅的springboot插件jar加载方式

###实现机制

1.实现springboot启动事件监听接口,springboot启动时会以SPI方式加载<br>
2.在contextLoaded之后,获取当前类加载器,反射调用URLClassLoader的addUrl方法,把插件包的路径,添加到类加载器<br>
3.springboot容器初始化刷新时,插件jar的bean也会被扫描注册(bean路径需在包扫描范围)