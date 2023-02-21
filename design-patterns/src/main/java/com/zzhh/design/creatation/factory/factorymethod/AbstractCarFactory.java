package com.zzhh.design.creatation.factory.factorymethod;

/**
 * @ClassName AbstractCarFactory
 * @Description 抽象工厂
 * @Author zhanghao
 * @Create 2023年02月21日 11:03:33
 */
public abstract class AbstractCarFactory {

    /**
     * 造汽车
     * @return 汽车
     */
    public abstract AbstractCar newCar();
}
