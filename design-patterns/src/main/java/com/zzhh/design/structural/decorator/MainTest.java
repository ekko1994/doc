package com.zzhh.design.structural.decorator;

/**
 * @ClassName MainTest
 * @Description <description class purpose>
 * @Author zhanghao
 * @Create 2023年02月21日 14:59:36
 */
public class MainTest {

    public static void main(String[] args) {
        ZhangHaoTikTok zhangHaoTikTok = new ZhangHaoTikTok();
        MeiYanTikTokDecorator decorator = new MeiYanTikTokDecorator(zhangHaoTikTok);

        decorator.tikTok();

    }
}
