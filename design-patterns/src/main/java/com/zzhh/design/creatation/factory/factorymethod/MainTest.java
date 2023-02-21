package com.zzhh.design.creatation.factory.factorymethod;

/**
 * @ClassName MainTest
 * @Description 工厂方法测试类
 * @Author zhanghao
 * @Create 2023年02月21日 10:20:35
 */
public class MainTest {

    public static void main(String[] args) {
        WuLinVanFactory wuLinVanFactory = new WuLinVanFactory();
        VanCar car1 = (VanCar) wuLinVanFactory.newCar();
        car1.run();

        WuLinMiniFactory wuLinMiniFactory = new WuLinMiniFactory();
        MiniCar car2 = (MiniCar) wuLinMiniFactory.newCar();
        car2.run();
    }
}
