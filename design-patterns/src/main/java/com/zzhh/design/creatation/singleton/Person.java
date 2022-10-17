package com.zzhh.design.creatation.singleton;

/**
 * @ClassName Person
 * @Description 单例类
 * @Author zhanghao
 * @Create 2022年09月10日 01:24:50
 */
public class Person {

    private String name;
    private int age;

    private volatile static  Person instance;

    private Person(String name, int age) {
        this.name = name;
        this.age = age;
        System.out.println("创建了Person");
    }

    public static Person getGetInstance() {
        if (instance == null){
            synchronized (Person.class){
                if (instance == null){

                    instance = new Person("jack",4);
                }
            }
        }
        return instance;
    }
}
