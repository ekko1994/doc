package com.zzhh.design.structural.decorator;

/**
 * @ClassName ZhangHaoTikTok
 * @Description 具体构建
 * @Author zhanghao
 * @Create 2023年02月21日 15:00:36
 */
public class ZhangHaoTikTok implements ManTikTok{

    @Override
    public void tikTok() {
        System.out.println("张浩在直播...");
    }
}
