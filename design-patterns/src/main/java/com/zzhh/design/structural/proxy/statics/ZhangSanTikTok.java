package com.zzhh.design.structural.proxy.statics;

/**
 * @ClassName ZhangSanTikTok
 * @Description 代理一般都是和被代理对象属于同一个接口
 * @Author zhanghao
 * @Create 2023年02月21日 15:22:18
 */
public class ZhangSanTikTok implements ManTikTok{

    private ManTikTok manTikTok;

    public ZhangSanTikTok(ManTikTok manTikTok) {
        this.manTikTok = manTikTok;
    }


    @Override
    public void tikTok() {
        //张三代理 去直播
        System.out.println("增强一些功能~~~");
        manTikTok.tikTok();
    }
}
