package com.zzhh.design.structural.decorator;

/**
 * @ClassName MeiYanTikTokDecorator
 * @Description <description class purpose>
 * @Author zhanghao
 * @Create 2023年02月21日 15:07:20
 */
public class MeiYanTikTokDecorator implements TikTokDecorator{

    private ManTikTok manTikTok;

    public MeiYanTikTokDecorator(ManTikTok manTikTok) {
        this.manTikTok = manTikTok;
    }

    @Override
    public void tikTok() {
        // 开启美颜
        enable();
        // 开始直播
        manTikTok.tikTok();
    }

    @Override
    public void enable() {
        System.out.println("开启美颜功能，相当帅！");
    }
}
