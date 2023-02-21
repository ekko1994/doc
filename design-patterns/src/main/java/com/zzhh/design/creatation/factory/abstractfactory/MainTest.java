package com.zzhh.design.creatation.factory.abstractfactory;

/**
 * @ClassName MainTest
 * @Description 抽象工厂测试类
 * @Author zhanghao
 * @Create 2023年02月21日 10:20:35
 */
public class MainTest {

    public static void main(String[] args) {
        WuLinFactory wuLinWuHanMaskFactory = new WuLinWuHanMaskFactory();
        AbstractMask abstractMask = wuLinWuHanMaskFactory.newMask();
        abstractMask.protectMe();

        WuLinFactory wuLinHangZhouMaskFactory = new WuLinHangZhouMaskFactory();
        AbstractMask abstractMask1 = wuLinHangZhouMaskFactory.newMask();
        abstractMask1.protectMe();

    }
}
