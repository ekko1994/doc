package com.zzhh.design.creatation.factory.abstractfactory;



/**
 * @ClassName MiniCar
 * @Description 具体产品
 * @Author zhanghao
 * @Create 2023年02月21日 10:46:17
 */
public class MiniCar extends AbstractCar {

    public MiniCar() {
        this.engine = "四缸水平对置发动机";
    }

    @Override
    public void run() {
        System.out.println("嘟嘟嘟...");
    }
}
