package com.zzhh.design.creatation.factory.abstractfactory;

/**
 * @ClassName WulinMiniCarFactory
 * @Description 具体工厂
 * @Author zhanghao
 * @Create 2023年02月21日 11:42:23
 */
public class WulinMiniCarFactory extends WuLinFactory{
    @Override
    public AbstractCar newCar() {
        return new MiniCar();
    }

    @Override
    public AbstractMask newMask() {
        return null;
    }
}
