# 容器监控之CAdvisor+InfluxDB+Granfana

## 原生命令

```sh
docker stats
```

![image-20220118212346079](images/image-20220118212346079.png)

通过docker stats命令可以很方便的看到当前宿主机上所有容器的CPU,内存以及网络流量等数据，一般小公司够用了。。。。

但是，docker stats统计结果只能是当前宿主机的全部容器，数据资料是实时的，没有地方存储、没有健康指标过线预警等功能

## 是什么

### 容器监控3剑客

<img src="images/image-20220118212447471.png" alt="image-20220118212447471" style="zoom:50%;" />

CAdvisor监控收集+InfluxDB存储数据+Granfana展示图表

#### CAdvisor

<img src="images/image-20220118212525866.png" alt="image-20220118212525866" style="zoom: 80%;" />

#### InfluxDB

<img src="images/image-20220118212606843.png" alt="image-20220118212606843" style="zoom:80%;" />



#### Granfana

<img src="images/image-20220118212618713.png" alt="image-20220118212618713" style="zoom:80%;" />

#### 总结

<img src="images/image-20220118212655981.png" alt="image-20220118212655981" style="zoom:80%;" />





## compose容器编排，一套带走

### 新建目录

<img src="images/image-20220118212814473.png" alt="image-20220118212814473" style="zoom:80%;" />

### 新建3件套组合的docker-compose.yml

```yaml
version: '3.1'
 
volumes:
  grafana_data: {}
 
services:
 influxdb:
  image: tutum/influxdb:0.9
  restart: always
  environment:
    - PRE_CREATE_DB=cadvisor
  ports:
    - "8083:8083"
    - "8086:8086"
  volumes:
    - ./data/influxdb:/data
 
 cadvisor:
  image: google/cadvisor
  links:
    - influxdb:influxsrv
  command: -storage_driver=influxdb -storage_driver_db=cadvisor -storage_driver_host=influxsrv:8086
  restart: always
  ports:
    - "8080:8080"
  volumes:
    - /:/rootfs:ro
    - /var/run:/var/run:rw
    - /sys:/sys:ro
    - /var/lib/docker/:/var/lib/docker:ro
 
 grafana:
  user: "104"
  image: grafana/grafana
  user: "104"
  restart: always
  links:
    - influxdb:influxsrv
  ports:
    - "3000:3000"
  volumes:
    - grafana_data:/var/lib/grafana
  environment:
    - HTTP_USER=admin
    - HTTP_PASS=admin
    - INFLUXDB_HOST=influxsrv
    - INFLUXDB_PORT=8086
    - INFLUXDB_NAME=cadvisor
    - INFLUXDB_USER=root
    - INFLUXDB_PASS=root
```

### 启动docker-compose文件

```sh
docker-compose up
```

![image-20220118213103447](images/image-20220118213103447.png)

![image-20220118213108264](images/image-20220118213108264.png)

### 查看三个服务容器是否启动

![image-20220118213200057](images/image-20220118213200057.png)

### 测试

```sh
#浏览cAdvisor收集服务，http://ip:8080/
http://192.168.48.111:8080/containers/
```

cadvisor也有基础的图形展现功能，这里主要用它来作数据采集



```sh
#浏览influxdb存储服务，http://ip:8083/
http://192.168.48.111:8083/
```



```sh
#浏览grafana展现服务，http://ip:3000,ip+3000端口的方式访问,默认帐户密码（admin/admin）
http://192.168.48.111:3000/
```

![image-20220118213651389](images/image-20220118213651389.png)

配置步骤：

1. 配置数据源

   <img src="images/image-20220118213824599.png" alt="image-20220118213824599" style="zoom:80%;" />

2. 选择influxdb数据源

   <img src="images/image-20220118213926847.png" alt="image-20220118213926847" style="zoom:80%;" />

3. 配置细节

   <img src="images/image-20220118213951294.png" alt="image-20220118213951294" style="zoom:80%;" />

   <img src="images/image-20220118214038984.png" alt="image-20220118214038984" style="zoom:80%;" />

   <img src="images/image-20220118214053568.png" alt="image-20220118214053568" style="zoom:80%;" />

   

   

4. 配置面板panel

   <img src="images/image-20220118214527070.png" alt="image-20220118214527070" style="zoom:80%;" />![image-20220118214650664](images/image-20220118214650664.png)

   <img src="images/image-20220118214527070.png" alt="image-20220118214527070" style="zoom:80%;" />![image-20220118214650664](images/image-20220118214650664.png)

   ![image-20220118214711214](images/image-20220118214711214.png)

   ![image-20220118214721594](images/image-20220118214721594.png)

   ![image-20220118214730825](images/image-20220118214730825.png)

   ![image-20220118214738397](images/image-20220118214738397.png)

   

5. 到这里cAdvisor+InfluxDB+Grafana容器监控系统就部署完成了

   

   

   

   

