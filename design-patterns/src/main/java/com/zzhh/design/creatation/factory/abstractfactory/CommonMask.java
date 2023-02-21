package com.zzhh.design.creatation.factory.abstractfactory;

/**
 * @ClassName CommonMask
 * @Description 具体产品
 * @Author zhanghao
 * @Create 2023年02月21日 11:34:43
 */
public class CommonMask extends AbstractMask{

    public CommonMask() {
        this.price = 1;
    }

    @Override
    public void protectMe() {
        System.out.println("普通口罩防护一般");
    }
}
