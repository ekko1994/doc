package com.zzhh.design.creatation.factory.simplefactory;

/**
 * @ClassName MainTest
 * @Description 简单工厂
 * @Author zhanghao
 * @Create 2023年02月21日 10:20:35
 */
public class MainTest {

    public static void main(String[] args) {
        WuLinSimpleFactory factory = new WuLinSimpleFactory();
        AbstractCar van = factory.newCar("van");
        van.run();

        AbstractCar mini = factory.newCar("mini");
        mini.run();
    }
}
