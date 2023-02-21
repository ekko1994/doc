package com.zzhh.design.creatation.builder;

/**
 * @ClassName MainTest
 * @Description 建造者测试类
 * @Author zhanghao
 * @Create 2023年02月21日 12:23:57
 */
public class MainTest {

    public static void main(String[] args) {
        AbstractBuilder xiaoMiBuilder = new XiaoMiBuilder();
        Phone phone = xiaoMiBuilder.customCamera("1")
                .customCpu("2")
                .customDisk("3")
                .customMem("4")
                .getProduct();
        System.out.println(phone);

        Phone build = Phone.builder().cpu("1")
                .camera("2")
                .disk("3")
                .mem("4")
                .build();
        System.out.println(build);
    }
}
