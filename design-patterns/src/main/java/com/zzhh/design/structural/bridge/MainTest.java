package com.zzhh.design.structural.bridge;

/**
 * @ClassName MainTest
 * @Description <description class purpose>
 * @Author zhanghao
 * @Create 2023年02月21日 14:19:27
 */
public class MainTest {

    public static void main(String[] args) {
        MiPhone iPhone = new MiPhone();
        iPhone.setSale(new StudentSale("学生",1000));

        String phone = iPhone.getPhone();
        System.out.println(phone);
    }
}
