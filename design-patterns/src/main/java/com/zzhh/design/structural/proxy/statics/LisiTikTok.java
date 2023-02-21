package com.zzhh.design.structural.proxy.statics;

/**
 * @ClassName LisiTikTok
 * @Description 具体构件
 * @Author zhanghao
 * @Create 2023年02月21日 15:01:28
 */
public class LisiTikTok implements ManTikTok {
    @Override
    public void tikTok() {
        System.out.println("李四在直播...");
    }
}
