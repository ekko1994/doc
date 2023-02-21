package com.zzhh.design.creatation.factory.abstractfactory;

/**
 * @ClassName WuLinWuHanMaskFactory
 * @Description 具体工厂
 * @Author zhanghao
 * @Create 2023年02月21日 11:38:31
 */
public class WuLinWuHanMaskFactory extends WuLinFactory{
    @Override
    public AbstractCar newCar() {
        return null;
    }

    @Override
    public AbstractMask newMask() {
        return new N95Mask();
    }
}
