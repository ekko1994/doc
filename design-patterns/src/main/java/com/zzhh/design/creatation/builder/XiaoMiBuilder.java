package com.zzhh.design.creatation.builder;

/**
 * @ClassName XiaoMiBuilder
 * @Description 具体建造者
 * @Author zhanghao
 * @Create 2023年02月21日 12:20:57
 */
public class XiaoMiBuilder extends AbstractBuilder {

    public XiaoMiBuilder() {
        phone = Phone.builder().build();
    }

    @Override
    AbstractBuilder customCpu(String cpu) {
        phone.cpu = cpu;
        return this;
    }

    @Override
    AbstractBuilder customMem(String mem) {
        phone.mem = mem;
        return this;
    }

    @Override
    AbstractBuilder customDisk(String disk) {
        phone.disk = disk;
        return this;
    }

    @Override
    AbstractBuilder customCamera(String camera) {
        phone.camera = camera;
        return this;
    }
}
