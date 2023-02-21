package com.zzhh.design.structural.decorator;

/**
 * @ClassName TikTokDecorator
 * @Description 抽象装饰
 * @Author zhanghao
 * @Create 2023年02月21日 15:02:19
 */
public interface TikTokDecorator extends ManTikTok{

    /**
     * 增强方法
     */
    void enable();

}
