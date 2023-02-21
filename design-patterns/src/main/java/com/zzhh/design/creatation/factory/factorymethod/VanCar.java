package com.zzhh.design.creatation.factory.factorymethod;

/**
 * @ClassName VanCar
 * @Description 具体产品
 * @Author zhanghao
 * @Create 2023年02月21日 10:44:29
 */
public class VanCar extends AbstractCar {

    public VanCar() {
        this.engine = "单杠柴油机";
    }

    @Override
    public void run() {
        System.out.println("哒哒哒...");
    }
}
