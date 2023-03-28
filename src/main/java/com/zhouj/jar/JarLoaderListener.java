package com.zhouj.jar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * springboot jar加载
 * <p>
 * jar包内的bean保证在springboot扫描路径下,即可自动被注册到容器
 *
 * @author zhoujie77
 * @date 2023/3/28
 */
public class JarLoaderListener implements SpringApplicationRunListener {

    private volatile int flag = 0;

    private Logger logger = LoggerFactory.getLogger(JarLoaderListener.class);

    /**
     * jar包路径 多个包用;隔开
     */
    private final String classPath = "plugin.classpath";

    public JarLoaderListener(SpringApplication application, String[] args) {

    }

    public void contextLoaded(ConfigurableApplicationContext context) {
        if (flag > 0) {
            return;
        }
        //保证springCloud只执行一次
        flag = 1;
        Environment environment = context.getEnvironment();
        URLClassLoader classLoader1 = (URLClassLoader) this.getClass().getClassLoader();
        Method method;
        try {
            //支持AppClassLoader和springboot classLoader
            if (classLoader1.getClass().getSuperclass().equals(URLClassLoader.class)) {
                method = classLoader1.getClass().getDeclaredMethod("addURL", URL.class);
            } else if (classLoader1.getClass().equals(URLClassLoader.class)) {
                method = classLoader1.getClass().getDeclaredMethod("addURL", URL.class);
            } else {
                throw new RuntimeException(classLoader1.getClass().getCanonicalName()+"未实现");
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        method.setAccessible(true);
        String paths = environment.getProperty(classPath);
        if (StringUtils.isNotBlank(paths)) {
            String[] path = paths.split(";");
            for (int i = 0; i < path.length; i++) {
                File file = new File(path[i]);
                try {
                    method.invoke(classLoader1, file.toURI().toURL());
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    logger.error(e.getMessage(), e);
                } catch (MalformedURLException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }


}
