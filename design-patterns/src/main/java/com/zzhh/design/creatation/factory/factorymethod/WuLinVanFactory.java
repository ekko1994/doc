package com.zzhh.design.creatation.factory.factorymethod;

/**
 * @ClassName WuLinVanFactory
 * @Description 具体工厂
 * @Author zhanghao
 * @Create 2023年02月21日 11:06:00
 */
public class WuLinVanFactory extends AbstractCarFactory{
    @Override
    public AbstractCar newCar() {
        return new VanCar();
    }
}
