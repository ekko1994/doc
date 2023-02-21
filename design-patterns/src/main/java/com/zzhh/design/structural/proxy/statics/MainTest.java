package com.zzhh.design.structural.proxy.statics;

/**
 * @ClassName MainTest
 * @Description <description class purpose>
 * @Author zhanghao
 * @Create 2023年02月21日 15:18:06
 */
public class MainTest {

    public static void main(String[] args) {
        ZhangSanTikTok zhangSanTikTok = new ZhangSanTikTok(new ZhangHaoTikTok());

        zhangSanTikTok.tikTok();
    }
}
