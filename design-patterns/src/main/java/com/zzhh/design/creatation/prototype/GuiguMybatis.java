package com.zzhh.design.creatation.prototype;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName GuiguMybatis
 * @Description <description class purpose>
 * @Author zhanghao
 * @Create 2023年02月21日 09:08:29
 */
public class GuiguMybatis {

    private Map<String, User> userCache = new HashMap<>();


    public User getUser(String userName) throws CloneNotSupportedException {
        User user = null;
        if (!userCache.containsKey(userName)) {
            // 数据库中拿
            user = getUserFromDB(userName);
        } else {
            System.out.println("缓存中拿..." + userName);
            // 缓存中拿，脏缓存问题
            user = userCache.get(userName);
            System.out.println("缓存中拿到的是：" + user);
            // 原型已经拿到，但不能直接给，要给一个克隆体
            user = (User) user.clone();
        }
        return user;
    }

    /**
     * 从数据库中查
     *
     * @param userName
     * @return
     */
    private User getUserFromDB(String userName) throws CloneNotSupportedException {
        System.out.println("数据库中查到..." + userName);
        User user = new User();
        user.setUserName(userName);
        user.setAge(18);
        // 缓存放一个克隆体
        User clone = (User) user.clone();
        userCache.put(userName, clone);
        return user;
    }

}
