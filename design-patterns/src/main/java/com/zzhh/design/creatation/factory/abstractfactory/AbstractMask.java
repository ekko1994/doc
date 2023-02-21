package com.zzhh.design.creatation.factory.abstractfactory;

/**
 * @ClassName AbstractMask
 * @Description 抽象产品
 * @Author zhanghao
 * @Create 2023年02月21日 11:29:06
 */
public abstract class AbstractMask {

    Integer price;

    /**
     * 保护我
     */
    public abstract void protectMe();
}
