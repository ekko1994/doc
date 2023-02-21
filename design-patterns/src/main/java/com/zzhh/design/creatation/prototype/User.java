package com.zzhh.design.creatation.prototype;

/**
 * @ClassName User
 * @Description <description class purpose>
 * @Author zhanghao
 * @Create 2023年02月21日 09:06:17
 */
public class User implements Cloneable{

    private String userName;
    private Integer age;

    public User() {
        System.out.println("创建User对象...");
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", age=" + age +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        User user = new User();
        user.setUserName(userName);
        user.setAge(age);
        return user;
    }
}
