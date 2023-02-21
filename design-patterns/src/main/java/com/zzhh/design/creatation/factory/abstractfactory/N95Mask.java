package com.zzhh.design.creatation.factory.abstractfactory;

/**
 * @ClassName N95Mask
 * @Description 具体产品
 * @Author zhanghao
 * @Create 2023年02月21日 11:30:07
 */
public class N95Mask extends AbstractMask{

    public N95Mask(){
        this.price = 2;
    }

    @Override
    public void protectMe() {
        System.out.println("N95口罩很强");
    }
}
