package com.zzhh.design.creatation.factory.abstractfactory;

/**
 * @ClassName WuLinHangZhouMaskFactory
 * @Description 具体工厂
 * @Author zhanghao
 * @Create 2023年02月21日 11:39:56
 */
public class WuLinHangZhouMaskFactory extends WuLinFactory{
    @Override
    public AbstractCar newCar() {
        return null;
    }

    @Override
    public AbstractMask newMask() {
        return new CommonMask();
    }
}
