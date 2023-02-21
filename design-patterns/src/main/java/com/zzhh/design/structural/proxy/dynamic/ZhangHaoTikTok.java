package com.zzhh.design.structural.proxy.dynamic;

/**
 * @ClassName ZhangHaoTikTok
 * @Description Subject 主体
 * @Author zhanghao
 * @Create 2023年02月21日 15:00:36
 */
public class ZhangHaoTikTok implements ManTikTok,SellTikTok {

    @Override
    public void tikTok() {
        System.out.println("张浩在直播...");
    }

    @Override
    public void sell() {
        System.out.println("直播间内五折优惠~~~");
    }
}
