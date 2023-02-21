package com.zzhh.design.creatation.factory.factorymethod;

/**
 * @ClassName WuLinMiniFactory
 * @Description 具体工厂
 * @Author zhanghao
 * @Create 2023年02月21日 11:05:08
 */
public class WuLinMiniFactory extends AbstractCarFactory{

    @Override
    public AbstractCar newCar() {
        return new MiniCar();
    }
}
