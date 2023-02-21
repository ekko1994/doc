package com.zzhh.design.creatation.builder;

import lombok.Builder;

/**
 * @ClassName Phone
 * @Description 产品角色
 * @Author zhanghao
 * @Create 2023年02月21日 11:59:21
 */
@Builder
public class Phone {

    protected String cpu;
    protected String mem;
    protected String disk;
    protected String camera;

    public Phone(String cpu, String mem, String disk, String camera) {
        this.cpu = cpu;
        this.mem = mem;
        this.disk = disk;
        this.camera = camera;
    }

    @Override
    public String toString() {
        return "Phone{" +
                "cpu='" + cpu + '\'' +
                ", mem='" + mem + '\'' +
                ", disk='" + disk + '\'' +
                ", camera='" + camera + '\'' +
                '}';
    }
}
