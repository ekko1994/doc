package com.zzhh.design.creatation.builder;

/**
 * @ClassName AbstractBuilder
 * @Description 抽象建造者
 * @Author zhanghao
 * @Create 2023年02月21日 12:10:44
 */
public abstract class AbstractBuilder {

    Phone phone;

    /**
     * 定制化CPU
     *
     * @return AbstractBuilder
     */
    abstract AbstractBuilder customCpu(String cpu);

    /**
     * 定制化mem
     *
     * @return AbstractBuilder
     */
    abstract AbstractBuilder customMem(String mem);

    /**
     * 定制化disk
     *
     * @return AbstractBuilder
     */
    abstract AbstractBuilder customDisk(String disk);

    /**
     * 定制化camera
     *
     * @return AbstractBuilder
     */
    abstract AbstractBuilder customCamera(String camera);

    public Phone getProduct() {
        return phone;
    }

}
