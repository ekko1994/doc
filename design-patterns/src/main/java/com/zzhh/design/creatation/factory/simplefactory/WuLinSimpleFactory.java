package com.zzhh.design.creatation.factory.simplefactory;

/**
 * @ClassName WuLinFactory
 * @Description 简单工厂，产品极少
 * @Author zhanghao
 * @Create 2023年02月21日 10:47:21
 */
public class WuLinSimpleFactory {


    public AbstractCar newCar(String type) {

        if ("van".equalsIgnoreCase(type)) {
            return new VanCar();
        } else if ("mini".equalsIgnoreCase(type)) {
            return new MiniCar();
        } else {
            return null;
        }

    }
}
