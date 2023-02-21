package com.zzhh.design.structural.facade;

/**
 * @ClassName MainTest
 * @Description 门面/外观模式测试类
 * @Author zhanghao
 * @Create 2023年02月21日 16:12:31
 */
public class MainTest {

    public static void main(String[] args) {
        WeiXinFacade weiXinFacade = new WeiXinFacade();
        weiXinFacade.handleXxx("张浩");
    }
}
