package com.zzhh.design.creatation.singleton;

import java.util.Map;
import java.util.Properties;

/**
 * @ClassName MainTest
 * @Description 单例模式测试类
 * @Author zhanghao
 * @Create 2022年09月10日 01:23:49
 */
public class MainTest {

    public static void main(String[] args) {
        Person p1 = Person.getGetInstance();
        Person p2 = Person.getGetInstance();

        System.out.println("p1 == p2 ?" + (p1 == p2));

        System.out.println("-------------------");


        Properties properties = System.getProperties();
        System.out.println(properties);

        System.out.println("-------------------");

        Map<String, String> getenv = System.getenv();
        System.out.println(getenv);

    }
}