package com.zzhh.design.structural.composite;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * 使用组合模式，组织层级结构的数据
 */
@Data
public class Menu {

    private Integer id;
    private String name;

    public Menu(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    //组合模式关注点
    private List<Menu> childs = new ArrayList<>();

    //提供添加层级的方法
    void addChildMenu(Menu menu) {
        childs.add(menu);
    }

    //层级遍历方法
    void printMenu(int deepth) {
        System.out.println(name);
        StringBuffer buffer = new StringBuffer();
        if (childs.size() > 0) {
            for (Menu child : childs) {
                for (int i = 0; i < deepth; i++) {
                    buffer.append("--");
                }
                System.out.print(buffer);
                child.printMenu(deepth);
            }
        }
    }

}
