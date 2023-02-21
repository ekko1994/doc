package com.zzhh.design.creatation.factory.abstractfactory;

/**
 * @ClassName WuLinFactory
 * @Description 总厂规范
 * @Author zhanghao
 * @Create 2023年02月21日 11:35:49
 */
public abstract class WuLinFactory {

    /**
     * 造汽车
     * @return 汽车
     */
    public abstract AbstractCar newCar();

    /**
     * 造口罩
     * @return 口罩
     */
    public abstract AbstractMask newMask();


}
