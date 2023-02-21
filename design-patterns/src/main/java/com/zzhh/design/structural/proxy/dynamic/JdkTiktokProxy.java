package com.zzhh.design.structural.proxy.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @ClassName JdkTiktokProxy
 * @Description <description class purpose>
 * @Author zhanghao
 * @Create 2023年02月21日 15:39:54
 */
public class JdkTiktokProxy<T> implements InvocationHandler {

    private T target;

    public JdkTiktokProxy(T target) {
        this.target = target;
    }

    public static<T> T getproxy(T t){

        return  (T) Proxy.newProxyInstance(t.getClass().getClassLoader(),
                t.getClass().getInterfaces(),
                new JdkTiktokProxy(t));

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("代理前...");
        Object invoke = method.invoke(target, args);
        System.out.println("代理后...");
        return invoke;
    }
}
