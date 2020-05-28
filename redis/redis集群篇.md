# 一、主从复制

## 1、主从复制简介

**互联网“三高”架构**

- 高并发
- 高性能
- 高可用

**单机redis的风险与问题**

- 问题1机器故障
- 问题2容量瓶颈

**多台服务器连接方案**

- 提供数据方：master

  ​	主服务器，主节点，主库

  ​	主客户端

- 接收数据方：slave

  ​	从服务器，从节点，从库

  ​	从客户端

- 需要解决的问题：

  ​	数据同步

- 核心工作：

  ​	master的数据复制到slave中

  ![](https://github.com/jackhusky/doc/blob/master/redis/images/多台服务器连接方案.png)

### 1.1、主从复制

主从复制即将master中的数据即时、有效的复制到slave中

特征：一个master可以拥有多个slave，一个slave只对应一个master

职责：

- master：
  - 写数据
  - 执行写操作时，将出现变化的数据自动同步到slave
  - 读数据（可忽略）
- slave：
  - 读数据
  - 写数据（禁止）

### 1.2、主从复制的作用

- 读写分离：master写、slave读，提高束武器的读写负载能力
- 负载均衡：基于主从结构，配合读写分离，由slave分担master负载，并根据需求的变化，改变slave的数量，通过多个从节点分担数据读取负载，大大提高redis服务器并发量与数据吞吐量
- 故障恢复：当master出现问题时，由slave提供服务，实现快速的故障恢复
- 数据冗余：实现数据热备份，是持久化之外的一种数据冗余方式
- 高可用基石：基于主从复制，构建哨兵模式与集群，实现redis的高可用方案

## 2、主从复制工作流程

**总述**

- 主从复制过程大体可以分为3个阶段

  - 建立连接阶段（即准备阶段）
  - 数据同步阶段
  - 命令传播阶段

  ![](https://github.com/jackhusky/doc/blob/master/redis/images/主从复制大体过程.png)

### 2.1、阶段一：建立连接阶段

- 建立slave到master的连接，使master能够识别到slave，并保存slave端口号

#### 建立连接阶段工作流程

步骤1：设置master的地址和端口，保存master信息

步骤2：建立socket连接

步骤3：发送ping命令（定时器任务）

步骤4：身份验证

步骤5：发送slave端口信息

至此，主从连接成功！

![](https://github.com/jackhusky/doc/blob/master/redis/images/建立连接阶段工作流程.png)

#### 主从连接（slave连接master）

- 方式一：客户端发送命令

  ```shell
  SLAVEOF host port
  ```

- 方式二：启动服务器参数

  ```shell
  redis-server redis-6380.conf --slaveof host port
  ```

- 方式三：从服务器配置

  ```conf
  SLAVEOF host port
  ```

#### 授权访问

- master配置文件设置密码

  ```shell
  requirepass <password>
  ```

- master客户端发送命令设置密码

  ```shell
  CONFIG SET requirepass <password>
  CONFIG GET requirepass
  ```

- slave客户端发送命令设置密码

  ```shell
  AUTH <password>
  ```

- slave配置文件设置密码

  ```conf
  masterauth <master-password>
  ```

- 启动客户端设置密码

  ```shell
  redis-cli -a <password>
  ```

### 2.2、阶段二：数据同步阶段工作流程

- 在slave初次连接master后，复制master中的所有数据到slave
- 将slave的数据库状态更新成master当前的数据库状态

#### 数据同步阶段工作流程

步骤1：请求同步数据

步骤2：创建RDB同步数据

步骤3：恢复RDB同步数据

步骤4：请求部分同步数据

步骤5：恢复部分同步数据

至此，数据同步工作完成

![](https://github.com/jackhusky/doc/blob/master/redis/images/数据同步阶段工作流程.png)

**数据同步阶段master说明**

1、如果master数据量大，数据同步阶段应避开流量高峰期，避免造成master阻塞，影响业务正常执行

2、复制缓冲区大小设置不合理，会导致数据溢出。如进行全量复制周期太长，进行部分复制时发现数据已经存在丢失的情况，必须进行第二次全量复制，致使slave陷入死循环状态

```conf
repl-backlog-size 1mb
```

3、master单机内存占用主机内存的比例不应过大，建议使用50%-70%，留下30%-50%的内存用于执行bgsave命令和创建复制缓冲区

**数据同步阶段slave说明**

1、为避免slave进行全量复制、部分复制时服务器响应阻塞或数据不同步，建议关闭此期间的对外服务

```conf
slave-serve-stale-data yes|no
```

2、数据同步阶段，master发送给slave信息可以理解master是slave的一个客户端，主动向slave发送命令

3、多个slave同时对master请求数据同步，master发送的RDB文件增多，会对带宽造成巨大冲击，如果master带宽不足，因此数据同步需要根据业务需求，适量错峰

4、slave过多时，建议调整拓扑结构，由一主多从结构变为树状结构，中间的节点既是master，也是slave。注意使用树状结构时，由于层次深度，导致深度越高的slave与最顶层master间数据同步延迟较大，数据一致性变差，应谨慎选择

### 2.3、阶段三：命令传播阶段

- 当master数据库状态被修改后，导致主服务器数据库状态不一致，此时需要让主从数据同步到一致的状态，同步的动作成为命令传播
- master将接收到的数据变更命令发送给slave，slave接收命令后执行命令

#### 命令传播阶段的部分复制

- 命令传播阶段出现了断网现象
  - 网络闪断闪连		  忽略
  - 长时间网络中断      全量复制
  - 短时间网络中断      部分复制

- 部分复制的三个核心要素
  - 服务器运行id（run id）
  - 主服务器的复制积压缓冲区
  - 主从服务器的复制偏移量

#### 服务器ID（runid）

- 概念：服务器运行ID时每一台服务器每次运行的身份识别码，一台服务器多次运行可以生成多个运行id

- 组成：运行id由40位字符组成，是一个随机的十六进制字符

- 作用：运行id被用于在服务器建进行传输，识别身份

  ​	如果想两次操作均对同一台服务器进行，必须每次操作携带对应的运行id，用于对方识别

- 实现方式：运行id在每台服务器启动时生产的，master在首次连接slave时，会将自己的运行ID发送给slave，slave保存此ID，通过info server命令可以查看节点的runid

#### 复制缓冲区

- 概念：复制缓冲区，又名复制积压缓冲区，是一个先进先出（FIFO）的队列，由于存储服务器执行过的命令，每次传播命令，master都会讲传播的命令记录下来，并存储在复制缓冲区

  - 复制缓冲区默认数据存储空间大小是1M，由于存储空间大小是固定的，当入队元素的数量大于队列长度时，最先入队的元素会被弹出，而新元素会被放入队列

  ![](https://github.com/jackhusky/doc/blob/master/redis/images/复制缓冲区.png)

- 由来：每台服务器启动时，如果开启有AOF或被连接成为master节点，即创建复制缓冲区

- 作用：用于保存master收到的所有指令（仅影响数据变更的指令，列入set，select）

- 数据来源：当master接收到主客户端的指令时，除了将指令执行，会将该指令存储到缓冲区

**复制缓冲区内部工作原理**

![](https://github.com/jackhusky/doc/blob/master/redis/images/复制缓冲区内部工作原理.png)

#### 主从服务器复制偏移量（offset）

- 概念：一个数字，描述复制缓冲区中的指令字节位置
- 分类：
  - master复制偏移量：记录发送给所有slave的指令字节对应的位置（多个）
  - slave复制偏移量：记录slave接收master发送过来的指令字节对应的位置（一个）
- 数据来源：
  - master端：发送一次记录一次
  - slave端：接收一次记录一次
- 作用：同步信息，比对master与slave的差异，当slave断线后，恢复数据使用

### 2.4、数据同步+命令传播阶段工作流程

![](https://github.com/jackhusky/doc/blob/master/redis/images/数据同步+命令传播工作流程.png)

### 2.5、心跳机制

- 进入命令传播阶段时，master与slave间需要进行信息交换，使用心跳机制进行维护，实现双方连接保持在线
- master心跳：
  - 指令：PING
  - 周期：由repl-ping-slave-period决定，默认10秒
  - 作用：判断slave是否在线
  - 查询：INFO Replication，获取slave最后一次连接时间间隔，lag项维持在0或1视为正常
- slave心跳：
  - 指令：REPLICATION ACK {offset}
  - 周期：1秒
  - 作用1：汇报slave自己的复制偏移量，获取最新的数据变更指令
  - 作用2：判断master是否在线

#### 心跳阶段注意事项

- 当slave多数掉线，或延迟过高时，master为保障数据稳定性，将拒绝所有信息同步操作

  ```conf
  min-slave-to-write 2
  min-slaves-max-lag 8
  ```

  - slave数量少于2个，或者所有slave的延迟都大于等于10秒时，强制关闭master写过能，停止数据同步

- slave数量由slave发送REPLICATION ACK命令做确认

- slave延迟由slave发送REPLICATION ACK命令做确认

![](https://github.com/jackhusky/doc/blob/master/redis/images/主从复制工作流程（完整）.png)

## 3、主从复制常见问题

### 3.1、频繁的全量复制

#### 频繁的全量复制（1）

![](https://github.com/jackhusky/doc/blob/master/redis/images/频繁的全量复制（1）.png)

#### 频繁的全量复制（2）

- 问题现象

  - 网络环境不佳，出现网络中断，slave不提供服务

- 问题原因

  - 复制缓冲区过小，断网后slave的offset月结，触发全量复制

- 最终结果

  - slave反复进行全量复制

- 解决方案

  - 修改复制缓冲区大小

    ```conf
    repl-backlog-size
    ```

- 建议设置如下：

  - 测算从master到slave的重连平均时长second
  - 获取master平均每秒产生写命令数据总量write_size_per_second
  - 最优复制缓冲区空间 = 2 * second * write_size_per_second

### 3.2、频繁的网络中断

#### 频繁的网络中断（1）

- 问题显现

  - master的CPU占用过高或slave频繁断开连接

- 问题原因

  - slave每1秒发送REPLICATION ACK命令到master
  - 当slave接到了慢查询（keys *，hgetall等），会大量占用CPU性能
  - master每1秒调用复制定时函数replication()，对比slave发现长时间没有进行相应

- 最终结果

  - master各种资源（输出缓冲区、宽带、连接等）被严重占用

- 解决方案

  - 通过设置合理的超时时间，确认是否释放slave

    ```conf
    repl-timeout
    ```

    该参数定义了超时时间的阈值（默认60秒），超过该值，释放slave

#### 频繁的网络中断（2）

- 问题现象

  - slave与master连接断开

- 问题原因

  - master发送ping指令频度较低
  - master设定超时时间较短
  - ping指令在网络中存在丢包

- 解决方案

  - 提高ping指令发送的频度

    ```conf
    repl-ping-slave-period
    ```

    超时时间repli-time的时间至少是ping指令频度的10倍，否则slave很容易判定超时

### 3.3、数据不一致

- 问题现象

  - 多个slave获取相同数据不同步

- 问题原因

  - 网络信息不同步，数据发送有延迟

- 解决方案

  - 优化主从间的网络环境，通常放置在同一个机房部署，如使用阿里云等云服务器时要注意此现象

  - 监控主从节点延迟（通过offset）判断，如果slave延迟过大，暂时屏蔽程序对该slave的数据访问

    ```conf
    slave-server-stale-data yes|no
    ```

    开启后仅响应info、slaveof等少数命令（慎用，除非对数据一致性要求很高）

# 二、哨兵模式

## 1、哨兵简介

**主机宕机**

![](https://github.com/jackhusky/doc/blob/master/redis/images/主机宕机的问题.png)

### 1.1、哨兵

哨兵（sentinel）是一个分布式系统，用于对主从结构中的每台服务器进行**监控**，当出现故障时通过投票机制**选择**新的master并将所有slave连接到新的master

![](https://github.com/jackhusky/doc/blob/master/redis/images/哨兵简介.png)

### 1.2、哨兵的作用

- 监控

  ​      不断的检查master和slave是否正常运行

  ​      master存活检测、master与slave运行情况检测

- 通知（提醒）

  ​	   当被监控的服务器出现问题时，向其他（哨兵间，客户端）发送通知

- 自动故障转移

  ​		断开master与slave连接，选取一个slave作为master，将其他slave连接到新的master，并告知客户端新的服务器地址

  注意：

  ​		哨兵也是一台redis服务器，只是不提供数据服务

  ​		通常哨兵配置数量为单数

## 2、启用哨兵模式

### 2.1、配置哨兵

- 配置一拖二的主从结构

- 配置三个哨兵（配置相同，端口不同）

  ​		参看sentinel.conf

- 启动哨兵

  ```shell
  redis-sentinel sentinel-端口号.conf
  ```

## 3、哨兵工作原理

**主从切换**

- 哨兵在进行主从切换过程中经历三个阶段
  - 监控	
  - 通知
  - 故障转移

### 3.1、阶段一：监控阶段

- 用于同步各个节点的状态信息
  - 获取各个sentinel的状态（是否在线）
  - 获取master的状态
    - master属性
      - runid
      - role：master
    - 各个slave的详细信息
  - 获取所有slave的状态（根据master中的slave信息）
    - runid
    - role：slave
    - master_host、master_port
    - offset
    - ......

![](https://github.com/jackhusky/doc/blob/master/redis/images/sentinel监控阶段.png)

![](https://github.com/jackhusky/doc/blob/master/redis/images/sentinel监控阶段详细.png)

### 3.2、阶段二：通知阶段

![](https://github.com/jackhusky/doc/blob/master/redis/images/通知阶段.png)

### 3.3、阶段三：故障转移阶段

![](https://github.com/jackhusky/doc/blob/master/redis/images/故障转移阶段1.png)

![](https://github.com/jackhusky/doc/blob/master/redis/images/sentinel内部选举.png)

- 服务器列表中挑选备选master

  - 在线的
  - 响应慢的

  - 与原master断开时间久的
  - 优先原则
    - 优先级
    - offset
    - runid

- 发送指令（sentinel）

  - 向新的master发送slaveof no one
  - 向其他slave发送slaveof新master ip端口

![](https://github.com/jackhusky/doc/blob/master/redis/images/sentinel选出新的master.png)

# 三、集群

## 1、集群简介

**现状问题**

业务发展过程中遇到的峰值瓶颈

- redis提供的服务OPS可以达到10万/秒，当前业务OPS已经达到20万/秒
- 内存单机容量达到256G，当前业务需求内存荣利郎1T

**集群架构**

- 集群就是使用网络将若干台计算机联通起来，并提供统一的管理方式，使其对外呈现单机的服务效果

**集群作用**

- 分散单台服务器的访问压力，实现负载均衡
- 分散单台服务器的存储压力，实现可扩展性
- 降低单台服务器宕机带来的业务灾难

## 2、Redis集群结构设计

### 数据存储设计

- 通过算法设计，计算出key应该保存的位置

- 将所有的存储空间计划切割成16384份，每台主机保存一部分

  ​	每份代表的是一个存储空间，不是一个key的保存空间

- 将key按照计算出的结果放到对应的存储空间

![](https://github.com/jackhusky/doc/blob/master/redis/images/数据存储设计.png)

### 集群内部通讯设计

- 各个数据库相互通信，保存各个库中槽的编号数据
- 一次命中，直接返回
- 一次未命中，告知具体位置

![](https://github.com/jackhusky/doc/blob/master/redis/images/集群内部通讯设计.png)

## 3、Cluster集群构建搭建

### Cluster配置

- 设置加入cluster，成为其中的节点

  ```conf
  cluster-enabled yes|no
  ```

- cluster配置文件名，该文件属于自动生成，仅用于快速查找文件并查询文件内容

  ```conf
  cluster-config-file <filename>
  ```

- 节点服务响应超时时间，用于判定该节点是否下线或切换为从节点

  ```conf
  cluster-node-timeout <milliseconds>
  ```

- master连接的slave最小数量

  ```conf
  cluster-migration-barrier <count>
  ```

### Cluster节点操作命令

- 查看集群节点信息

  ```shell
  cluster nodes
  ```

- 进入一个从节点redis，切换其主节点

  ```shell
  cluster replicate <master-id>
  ```

- 发现一个新节点，新增主节点

  ```shell
  cluster meet ip:port
  ```

- 忽略一个没有solt的节点

  ```shell
  cluster forget <id>
  ```

- 手动故障转移

  ```shell
  cluster failover
  ```

  