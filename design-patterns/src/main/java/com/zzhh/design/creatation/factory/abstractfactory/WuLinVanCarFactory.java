package com.zzhh.design.creatation.factory.abstractfactory;

/**
 * @ClassName WuLinVanCarFactory
 * @Description 具体产品工厂
 * @Author zhanghao
 * @Create 2023年02月21日 11:43:13
 */
public class WuLinVanCarFactory extends WuLinFactory {
    @Override
    public AbstractCar newCar() {
        return new VanCar();
    }

    @Override
    public AbstractMask newMask() {
        return null;
    }
}
