# zookeeper简介

## 什么是zookeeper？

ZooKeeper是用于维护配置信息，命名，提供分布式同步和提供组服务的集中式服务。所有这些类型的服务都以某种形式被分布式应用程序使用。每次实施它们时，都会进行很多工作来修复不可避免的错误和竞争条件。由于难以实现这类服务，因此应用程序通常最初会在其上跳过，从而使它们在存在更改的情况下变得脆弱并且难以管理。即使部署正确，这些服务的不同实现也会导致管理复杂。

## zookeeper应用场景

zookeeper是一个经典的分布式数据一致性解决方案，致力于为分布式应用提供一个**高性能**、**高可用**、且具有**严格顺序访问**控制能力的分布式协调存储服务。

- 维护配置信息
- 分布式锁服务
- 集群管理
- 生成分布式唯一ID

# zookeeper的数据模型

![](images/zookeeper%E6%95%B0%E6%8D%AE%E6%A8%A1%E5%9E%8B.png)

zookeeper的数据节点可以视为树状结构（或目录），书中的各节点被称为znode，一个znode可以有多个子节点。一个znode大体上分为3部分：

- 节点的数据：即znode data（节点path，节点data）的关系就像是map中（key，value）的关系
- 节点的子节点children
- 节点的状态stat：用来描述当前节点的创建、修改记录、包括cZxid、ctime等

## 节点类型

- 临时节点：该节点的生命周期依赖于创建它们的回话。一旦会话（session）结束，临时节点将被自动删除，当然也可以手动删除。虽然每个临时的Znode都会绑定到一个客户端会话，但它们对所有的客户端还是可见的。另外，zookeeper的临时节点不允许拥有子节点。
- 持久化节点：该节点的生命周期不依赖于会话，并且只有在客户端显示执行删除操作的时候，它们才被删除

# zookeeper单机安装

[zookeeper单机安装](https://github.com/jackhusky/doc/blob/master/zookeeper/zookeeper%E5%AE%89%E8%A3%85.md)

# zookeeper常用shell命令

## 新增节点

```shell
create [-s] [-e] path data #-s:有序节点，#-e临时节点
```

创建持久化节点并写入数据

```shell
[zk: localhost:2181(CONNECTED) 0] create /hadoop "123456"
```

创建持久化有序节点（可作为分布式id），此时创建的节点名为指定节点名+自增序号

```shell
[zk: localhost:2181(CONNECTED) 1] create -s /a 'a'
Created /a0000000001
[zk: localhost:2181(CONNECTED) 2] get /a0000000001
[zk: localhost:2181(CONNECTED) 3] create -s /b "b"
Created /b0000000002
```

创建临时节点，临时节点会在会话过期后被删除

```shell
[zk: localhost:2181(CONNECTED) 4] create -e /tmp "tmp"
Created /tmp
```

创建临时有序节点（可生成分布式锁），临时节点会在会话过期后被删除

```shell
[zk: localhost:2181(CONNECTED) 1] create -s -e /aa 'aa'
Created /aa0000000004
[zk: localhost:2181(CONNECTED) 2] create -s -e /bb 'bb'
Created /bb0000000005
```

## 更新节点

更新节点的命令是`set`，可以直接进行修改，如下

```shell
[zk: localhost:2181(CONNECTED) 2] set /hadoop "345"
cZxid = 0x4
ctime = Fri Jun 05 14:38:58 CST 2020
mZxid = 0x10
mtime = Fri Jun 05 14:51:30 CST 2020
pZxid = 0x4
cversion = 0
dataVersion = 1
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 3
numChildren = 0
```

也可以基于版本号进行更改，此时类似于乐观锁机制，当你传入的数据版本号（dataVersion）和当前节点的数据版本号不一致时，zookeeper会拒绝本次修改

```shell
[zk: localhost:2181(CONNECTED) 4] set /hadoop "3456789"
cZxid = 0x4
ctime = Fri Jun 05 14:38:58 CST 2020
mZxid = 0x11
mtime = Fri Jun 05 14:54:48 CST 2020
pZxid = 0x4
cversion = 0
dataVersion = 2
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 7
numChildren = 0
[zk: localhost:2181(CONNECTED) 5] set /hadoop "666" 1  
version No is not valid : /hadoop
```

## 删除节点

删除节点的语法如下：

```shell
delete path [version]
```

和更新节点数据一样，也可以传入版本号，版本号一致才可以成功删除

```shell
[zk: localhost:2181(CONNECTED) 12] delete /hadoop 1       
version No is not valid : /hadoop
[zk: localhost:2181(CONNECTED) 13] delete /hadoop 0
[zk: localhost:2181(CONNECTED) 14]
```

要想删除某个节点及其所有后代节点，可以使用递归删除，命令为`rmr path`

```shell
[zk: localhost:2181(CONNECTED) 18] delete /hadoop
Node not empty: /hadoop
[zk: localhost:2181(CONNECTED) 19] rmr /hadoop
[zk: localhost:2181(CONNECTED) 20] 
```

## 查看节点

```shell
get path
```

```shell
[zk: localhost:2181(CONNECTED) 23] get /hadoop            
123456
cZxid = 0x1e
ctime = Fri Jun 05 15:05:02 CST 2020
mZxid = 0x1e
mtime = Fri Jun 05 15:05:02 CST 2020
pZxid = 0x1e
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 6
numChildren = 0
```

节点各个属性如下表，其中一个重要的概念是Zxid（Zookeeper Transaction id），zookeeper节点的每一次更改都具有唯一的Zxid，如果Zxid1小于Zxid2，则Zxid1的更改发生在Zxid2更改之前

| 状态属性       | 说明                                                         |
| -------------- | ------------------------------------------------------------ |
| cZxid          | 数据节点创建时的事务ID                                       |
| ctime          | 数据节点创建时的时间                                         |
| mZxid          | 数据节点最后一次更新时的事务ID                               |
| mtime          | 数据节点最后一次更新时的时间                                 |
| pZxid          | 数据节点的子节点最后一次被修改时的事务ID                     |
| cversion       | 子节点的更改次数                                             |
| dataVersion    | 节点数据的更改次数                                           |
| aclVersion     | 节点的ACL的更改次数                                          |
| ephemeralOwner | 如果节点是临时节点，则表示创建节点的回话的SessionID；如果节点是持久节点，则该属性值是0 |
| dataLength     | 数据内容的长度                                               |
| numChildren    | 数据节点当前的子节点个数                                     |

### 查看节点状态

可以使用`stat`命令查看节点状态，他的返回值和`get`命令类似，但不会返回节点数据

```shell
[zk: localhost:2181(CONNECTED) 34] stat /hadoop
cZxid = 0x1e
ctime = Fri Jun 05 15:05:02 CST 2020
mZxid = 0x22
mtime = Fri Jun 05 15:19:47 CST 2020
pZxid = 0x21
cversion = 2
dataVersion = 2
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 4
numChildren = 2
```

### 查看节点列表

查看节点列表有`ls -path`和`ls2 path`两个命令，后者是前者的增强，不仅可以查看指定路径下的所有节点，还可以查看当前节点的信息

```shell
[zk: localhost:2181(CONNECTED) 36] ls /hadoop
[node2, node1]
[zk: localhost:2181(CONNECTED) 37] ls2 /hadoop
[node2, node1]
cZxid = 0x1e
ctime = Fri Jun 05 15:05:02 CST 2020
mZxid = 0x22
mtime = Fri Jun 05 15:19:47 CST 2020
pZxid = 0x21
cversion = 2
dataVersion = 2
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 4
numChildren = 2
```

### 监听器get path [watch]

使用`get path [watch]`注册的监听器能够在节点**内容**发生改变的时候，向客户端发出通知。需要注意的是zookeeper的触发器是一次性的，即出发一次后就会立即失效。

```shell
[zk: localhost:2181(CONNECTED) 40] get /hadoop watch
[zk: localhost:2181(CONNECTED) 41] set /hadoop "4332"
WATCHER::

WatchedEvent state:SyncConnected type:NodeDataChanged path:/hadoop
```

### 监听器stat path [watch]

使用`stat path [watch]`注册的监听器能够在节点**状态**发生改变的时候，向客户端发出通知。

```shell
[zk: localhost:2181(CONNECTED) 43] stat /hadoop watch
[zk: localhost:2181(CONNECTED) 44] set /hadoop "4332"
[zk: localhost:2181(CONNECTED) 45] 
WATCHER::

WatchedEvent state:SyncConnected type:NodeDataChanged path:/hadoop
```

### 监听器ls/ls2 path [watch]

使用`ls path [watch]`或者`ls2 path [watch]`注册的监听器能够监听该节点下所有子节点的增加和删除操作。

```shell
[zk: localhost:2181(CONNECTED) 45] ls /hadoop watch
[node2, node1]
[zk: localhost:2181(CONNECTED) 46] create /hadoop/node3 "node3"
WATCHER::Created /hadoop/node3

WatchedEvent state:SyncConnected type:NodeChildrenChanged path:/hadoop
```

# zookeeper的acl权限控制

## 概述

zookeeper类似文件系统，client可以创建节点、更新节点、删除节点，那么如何做到节点的权限控制？zookeeper的access control list访问控制列表可以做到这一点

acl权限控制，使用scheme：id：permission来标识，主要涵盖3个方面：

- 权限模式（scheme）：授权的策略

- 授权对象（id）：授权的对象

- 权限（permission）：授予的权限

  其特性如下：

- zookeeper的权限控制是基于每个znode节点的，需要对每个节点设置权限

- 每个znode支持设置多种权限控制方案和多个权限

- 子节点不会继承父节点的权限，客户端无权访问某节点，但可能可以访问它的子节点

例如：

```shell
[zk: localhost:2181(CONNECTED) 53] setAcl /node1 world:anyone:drwa
```

## 权限模式

| 方案   | 描述                                                  |
| ------ | ----------------------------------------------------- |
| world  | 只有一个用户：anyone，代表登录zookeeper所有人（默认） |
| ip     | 对客户端使用IP地址认证                                |
| auth   | 使用已添加认证的用户认证                              |
| digest | 使用“用户名：密码”方式认证                            |

## 授权的对象

给谁授予权限

授权对象ID是指，权限赋予的实体，例如：IP地址或用户

## 授予的权限

授予什么权限

create、delete、read、writer、admin也就是增、删、改、查、管理权限，这5种权限简写为cdrwa，注意：这5种权限，delete是指对子节点的删除权限，其他4种权限指定自身节点的操作权限

| 权限   | ACL简写 | 描述                             |
| ------ | ------- | -------------------------------- |
| create | c       | 可以创建子节点                   |
| delete | d       | 可以删除子节点（仅下一级节点）   |
| read   | r       | 可以读取节点数据及显示子节点列表 |
| write  | w       | 可以设置节点数据                 |
| admin  | a       | 可以设置节点访问控制列表权限     |

## 授权的相关命令

| 命令    | 使用方式                | 描述         |
| ------- | ----------------------- | ------------ |
| getAcl  | getAcl <path>           | 读取ACL权限  |
| setAcl  | setAcl <path> <acl>     | 设置ACL权限  |
| addauth | addauth <scheme> <auth> | 添加认证用户 |

## 案例

- world授权模式

  命令

  ```shell
  setAcl <path> world:anyone:<acl>
  ```

  案例

  ```shell
  [zk: localhost:2181(CONNECTED) 53] setAcl /node1 world:anyone:drwa
  [zk: localhost:2181(CONNECTED) 59] getAcl /node1
  'world,'anyone
  : drwa
  ```

- IP授权模式

  命令

  ```shell
  setAcl <path> ip:<ip>:<acl>
  ```

  案例：

  注意：远程登录zookeeper命令：./zkCli.sh -server ip（关闭防火墙）

  ```shell
  [zk: 192.168.44.139(CONNECTED) 3] setAcl /node2 ip:192.168.44.140:cdrwa
  cZxid = 0x35
ctime = Fri Jun 05 19:07:00 CST 2020
  mZxid = 0x42
  mtime = Fri Jun 05 19:54:42 CST 2020
  pZxid = 0x35
  cversion = 0
  dataVersion = 1
  aclVersion = 3
  ephemeralOwner = 0x0
  dataLength = 8
  numChildren = 0
  [zk: 192.168.44.139(CONNECTED) 4] get /node2
  Authentication is not valid : /node2
  ```
  
- Auth授权模式

  命令

  ```shell
  addauth digest <user>:<password> # 添加认证用户
  setAcl <path> auth:<user>:<acl>
  ```

  案例

  ```shell
  [zk: 192.168.44.139(CONNECTED) 7] addauth digest jack:jack
  [zk: 192.168.44.139(CONNECTED) 8] setAcl /node3 auth:jack:cdrwa
  cZxid = 0x45
  ctime = Fri Jun 05 20:07:09 CST 2020
  mZxid = 0x45
  mtime = Fri Jun 05 20:07:09 CST 2020
  pZxid = 0x45
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: 192.168.44.139(CONNECTED) 9] getAcl /node3
  'digest,'jack:p4FVWKzcf0HsYG6jAmAOvoHGCt8=
  : cdrwa
  [zk: 192.168.44.139(CONNECTED) 11] quit
  [zk: 192.168.44.139(CONNECTED) 0] get /node3
  Authentication is not valid : /node3
  [zk: 192.168.44.139(CONNECTED) 1] addauth digest jack:jack
  [zk: 192.168.44.139(CONNECTED) 2] get /node3              
  node3
  cZxid = 0x45
  ctime = Fri Jun 05 20:07:09 CST 2020
  mZxid = 0x45
  mtime = Fri Jun 05 20:07:09 CST 2020
  pZxid = 0x45
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  ```

- Digest授权模式

  命令

  ```shell
  setAcl <path> digest:<user>:<password>:<acl>
  ```

  这里的密码是经过SHA1及BASE64处理的密文，在SHELL中可以通过以下命令计算

  ```shell
  echo -n <user>:<password> | openssl dgst -binary -sha1 | openssl base64
  ```

  积算一个密文

  ```shell
  [root@localhost ~]# echo -n jack:jack | openssl dgst -binary -sha1 | openssl base64
  p4FVWKzcf0HsYG6jAmAOvoHGCt8=
  ```

  案例

  ```shell
  [zk: 192.168.44.139(CONNECTED) 2] setAcl /node4 digest:jack:p4FVWKzcf0HsYG6jAmAOvoHGCt8=:cdrwa
  cZxid = 0x4c
  ctime = Fri Jun 05 20:15:26 CST 2020
  mZxid = 0x4c
  mtime = Fri Jun 05 20:15:26 CST 2020
  pZxid = 0x4c
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: 192.168.44.139(CONNECTED) 3] getAcl /node4
  Authentication is not valid : /node4
  [zk: 192.168.44.139(CONNECTED) 4] addauth digest jack:jack
  [zk: 192.168.44.139(CONNECTED) 5] getAcl /node4           
  'digest,'jack:p4FVWKzcf0HsYG6jAmAOvoHGCt8=
  : cdrwa
  ```

- 多种模式授权

  同一个节点可以同时使用多种模式授权

  ```shell
  [zk: 192.168.44.139(CONNECTED) 9] setAcl /node5 ip:192.168.44.140:cdra,auth:iacast:cdrwa,digest:jack:p4F6jAmAOvoHGCt8=:cdrwa
  ```

## acl超级管理员

zookeeper的权限管理模式有一种叫做super，该模式提供一个超管可以方便的访问任何权限的节点

假设这个超管是：super:admin，需要先为超管生成密码的密文

```shell
[root@localhost ~]# echo -n super:admin | openssl dgst -binary -sha1 | openssl base64
xQJmxLMiHGwaqBvst5y6rkB6HQs=
```

那么打开zookeeper目录下的/bin/zkServer.sh服务器脚本文件，找到一行：

```sh
nohup "$JAVA" "-Dzookeeper.log.dir=${ZOO_LOG_DIR}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}"
```

这就是脚本中启动zookeeper的命令，默认只有以上两个配置项，我们需要加一个超管的配置项

```sh
"-Dzookeeper.DigestAuthenticationProvider.superDigest=super:xQJmxLMiHGwaqBvst5y6rkB6HQs="
```

修改后

```sh
nohup "$JAVA" "-Dzookeeper.log.dir=${ZOO_LOG_DIR}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}" "-Dzookeeper.DigestAuthenticationProvider.superDigest=super:xQJmxLMiHGwaqBvst5y6rkB6HQs=" \
    -cp "$CLASSPATH" $JVMFLAGS $ZOOMAIN "$ZOOCFG" > "$_ZOO_DAEMON_OUT" 2>&1 < /dev/null &
```

启动zookeeper

```shell
[zk: 192.168.44.139(CONNECTED) 2] setAcl /node6 ip:192.168.44.140:cdrwa
cZxid = 0x57
ctime = Fri Jun 05 21:21:14 CST 2020
mZxid = 0x57
mtime = Fri Jun 05 21:21:14 CST 2020
pZxid = 0x57
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 5
numChildren = 0
[zk: 192.168.44.139(CONNECTED) 3] get /node6
Authentication is not valid : /node6
[zk: 192.168.44.139(CONNECTED) 4] addauth digest super:admin
[zk: 192.168.44.139(CONNECTED) 5] get /node6                
node6
cZxid = 0x57
ctime = Fri Jun 05 21:21:14 CST 2020
mZxid = 0x57
mtime = Fri Jun 05 21:21:14 CST 2020
pZxid = 0x57
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 5
numChildren = 0
```

# zookeeper javaAPI

znode是zookeeper集合的核心组件，zookeeper API提供了一小组方法使用zookeeper集合来操纵znode的所有细节

- 连接到zookeeper服务器。zookeeper服务器为客户端分配会话ID
- 定期向服务器发送心跳。否则，zookeeper服务器将过期会话ID，客户端需要重新连接
- 只要会话ID处于活动状态，就可以获取、设置znode
- 所有任务完成后，断开zookeeper服务器的连接。如果客户端长时间不活动，则zookeeper服务器将自动断开客户端

## 连接到zookeeper

```java
package com.itcast.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * @author zhanghao
 * @date 2020/6/5 - 22:12
 */
public class ZookeeperConnection {

    public static void main(String[] args) {
        try{
            CountDownLatch countDownLatch = new CountDownLatch(1);
            ZooKeeper zooKeeper = new ZooKeeper("192.168.44.139:2181", 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState()== Event.KeeperState.SyncConnected) {
                        System.out.println("连接创建成功！");
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await();
            System.out.println(zooKeeper.getSessionId());
            zooKeeper.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
```

## 新增节点

```java
package com.itcast.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhanghao
 * @date 2020/6/7 - 16:42
 */
public class ZKCreate {

    ZooKeeper zooKeeper;

    String IP = "192.168.44.139:2181";

    @Before
    public void before() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(IP, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("连接创建成功！");
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();
    }

    @After
    public void after() throws Exception {
        zooKeeper.close();
    }

    @Test
    public void create1() throws Exception {
        //arg1:节点的路径
        //arg2:节点的数据
        //arg3:权限列表 world:anyone:cdrwa
        //arg4:节点类型 持久化节点
        zooKeeper.create("/create/node1","node1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Test
    public void create4() throws Exception {
        // ip授权模式
        List<ACL> acls = new ArrayList<>();
        Id id = new Id("ip", "192.168.44.139");
        acls.add(new ACL(ZooDefs.Perms.ALL,id));

        zooKeeper.create("/create/node4","node4".getBytes(), acls, CreateMode.PERSISTENT);
    }

    @Test
    public void create5() throws Exception {

        // auth授权模式
        zooKeeper.addAuthInfo("digest","jack:jack".getBytes());
        zooKeeper.create("/create/node5","node5".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
    }

    @Test
    public void create11() throws Exception {
        //异步方式创建节点
        zooKeeper.create("/create/node11", "node11".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, new AsyncCallback.StringCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, String name) {
                // 0 代表创建成功
                System.out.println(rc);
                //节点的路径
                System.out.println(path);
                // 上下文参数
                System.out.println(ctx);
                // 节点的路径
                System.out.println(name);
            }
        },"i am context");
        Thread.sleep(10000);
        System.out.println("end");
    }
}
```

## 更新节点

```java
package com.itcast.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZKSet {

    ZooKeeper zooKeeper;

    String IP = "192.168.44.139:2181";

    @Before
    public void before() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(IP, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("连接创建成功！");
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();
    }

    @After
    public void after() throws Exception {
        zooKeeper.close();
    }

    @Test
    //同步方式
    public void set1() throws Exception {
        // arg1:节点的路径
        // arg2:修改的数据
        // arg3:数据的版本号 -1代表版本号不参与更新
        Stat stat = zooKeeper.setData("/set/node1", "node11".getBytes(), -1);
        // 当前节点的版本号
        System.out.println(stat.getVersion());
    }

    @Test
    public void set2() throws Exception {
        // 异步方式
        zooKeeper.setData("/set/node1", "node14".getBytes(), -1, new AsyncCallback.StatCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, Stat stat) {
                // Stat stat:属性描述对象
            }
        },"i am context");
        Thread.sleep(10000);
        System.out.println("end");
    }
}
```

## 删除节点

```java
package com.itcast.zookeeper;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class ZKDelete {

    ZooKeeper zooKeeper;

    String IP = "192.168.44.139:2181";

    @Before
    public void before() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(IP, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("连接创建成功！");
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();
    }

    @After
    public void after() throws Exception {
        zooKeeper.close();
    }

    @Test
    public void delete1() throws Exception {
        //arg1:删除节点的路径
        //arg2:数据版本信息 -1代表版本不参与删除
        zooKeeper.delete("/delete/node1",-1);
    }

    @Test
    public void delete2() throws Exception {
        //异步方式
        zooKeeper.delete("/delete/node2", -1, new AsyncCallback.VoidCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx) {
                // 是否删除成功 0?
                System.out.println(rc);
                // 删除路径
                System.out.println(path);
                System.out.println(ctx);
            }
        },"i am context");
        Thread.sleep(10000);
        System.out.println("end");
    }
}
```

## 查看节点

```java
package com.itcast.zookeeper;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class ZKGet {

    ZooKeeper zooKeeper;

    java.lang.String IP = "192.168.44.139:2181";

    @Before
    public void before() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(IP, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("连接创建成功！");
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();
    }

    @After
    public void after() throws Exception {
        zooKeeper.close();
    }

    @Test
    public void get1() throws Exception {
        Stat stat = new Stat();
        byte[] data = zooKeeper.getData("/get/node1", false, stat);
        System.out.println(new String(data));
    }

    @Test
    public void get2() throws Exception {
        zooKeeper.getData("/get/node2", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println(rc);
                System.out.println(new String(data));
            }
        },"i am context");
        Thread.sleep(10000);
        System.out.println("end");
    }
}
```

## 查看子节点

```java
package com.itcast.zookeeper;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZKGetChild {

    ZooKeeper zooKeeper;

    String IP = "192.168.44.139:2181";

    @Before
    public void before() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(IP, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("连接创建成功！");
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();
    }

    @After
    public void after() throws Exception {
        zooKeeper.close();
    }

    @Test
    public void get1() throws Exception {
        List<String> children = zooKeeper.getChildren("/get", false);
        System.out.println(children);
    }

    @Test
    public void get2() throws Exception {
        zooKeeper.getChildren("/get", false, new AsyncCallback.ChildrenCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, List<String> children) {
                System.out.println(children);
            }
        },"i am context");
        Thread.sleep(1000);
        System.out.println("end");
    }
}
```

## 检查节点是否存在

```java
package com.itcast.zookeeper;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class ZKExists {

    ZooKeeper zooKeeper;

    String IP = "192.168.44.139:2181";

    @Before
    public void before() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(IP, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("连接创建成功！");
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();
    }

    @After
    public void after() throws Exception {
        zooKeeper.close();
    }

    @Test
    public void exists1() throws Exception {
        Stat exists = zooKeeper.exists("/get", false);
        System.out.println(exists.getVersion());
    }

    @Test
    public void exists2() throws Exception {
        zooKeeper.exists("/get", false, new AsyncCallback.StatCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, Stat stat) {
                System.out.println(rc);
            }
        },"i am context");
        Thread.sleep(1000);
        System.out.println("end");
    }
}
```































