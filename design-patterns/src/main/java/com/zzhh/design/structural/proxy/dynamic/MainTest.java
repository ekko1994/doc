package com.zzhh.design.structural.proxy.dynamic;

/**
 * @ClassName MainTest
 * @Description
 * 动态代理模式：
 * JDK要求被代理对象必须有接口
 *
 * 代理对象和目标对象的相同点在于都是同一个接口
 * @Author zhanghao
 * @Create 2023年02月21日 15:18:06
 */
public class MainTest {

    public static void main(String[] args) {
        ManTikTok zhangHaoTikTok = new ZhangHaoTikTok();
        ManTikTok getproxy = JdkTiktokProxy.getproxy(zhangHaoTikTok);

        getproxy.tikTok();

    }

}
