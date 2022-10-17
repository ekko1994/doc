package com.zzhh.design.creatation.singleton;

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
    }
}
