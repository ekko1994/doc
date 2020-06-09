* [zookeeper开源客户端curator](#zookeeper%E5%BC%80%E6%BA%90%E5%AE%A2%E6%88%B7%E7%AB%AFcurator)
  * [curator简介](#curator%E7%AE%80%E4%BB%8B)
  * [连接到zookeeper](#%E8%BF%9E%E6%8E%A5%E5%88%B0zookeeper)
  * [新增节点](#%E6%96%B0%E5%A2%9E%E8%8A%82%E7%82%B9)
  * [更新节点](#%E6%9B%B4%E6%96%B0%E8%8A%82%E7%82%B9)
  * [删除节点](#%E5%88%A0%E9%99%A4%E8%8A%82%E7%82%B9)
  * [查看节点](#%E6%9F%A5%E7%9C%8B%E8%8A%82%E7%82%B9)
  * [查看子节点](#%E6%9F%A5%E7%9C%8B%E5%AD%90%E8%8A%82%E7%82%B9)
  * [检查节点是否存在](#%E6%A3%80%E6%9F%A5%E8%8A%82%E7%82%B9%E6%98%AF%E5%90%A6%E5%AD%98%E5%9C%A8)
  * [watcherAPI](#watcherapi)
  * [事务](#%E4%BA%8B%E5%8A%A1)
  * [分布式锁](#%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81)
* [zookeeper四字监控命令](#zookeeper%E5%9B%9B%E5%AD%97%E7%9B%91%E6%8E%A7%E5%91%BD%E4%BB%A4)
  * [conf命令](#conf%E5%91%BD%E4%BB%A4)
  * [cons命令](#cons%E5%91%BD%E4%BB%A4)
  * [crst命令](#crst%E5%91%BD%E4%BB%A4)
  * [dump命令](#dump%E5%91%BD%E4%BB%A4)
  * [envi命令](#envi%E5%91%BD%E4%BB%A4)
  * [ruok命令](#ruok%E5%91%BD%E4%BB%A4)
  * [stat命令](#stat%E5%91%BD%E4%BB%A4)
  * [srst命令](#srst%E5%91%BD%E4%BB%A4)
  * [wchs命令](#wchs%E5%91%BD%E4%BB%A4)
  * [wchc命令](#wchc%E5%91%BD%E4%BB%A4)
  * [wchp命令](#wchp%E5%91%BD%E4%BB%A4)
  * [mntr命令](#mntr%E5%91%BD%E4%BB%A4)
* [zookeeper图形化的客户端工具（ZooInspector）](#zookeeper%E5%9B%BE%E5%BD%A2%E5%8C%96%E7%9A%84%E5%AE%A2%E6%88%B7%E7%AB%AF%E5%B7%A5%E5%85%B7zooinspector)
* [taokeeper监控工具的使用](#taokeeper%E7%9B%91%E6%8E%A7%E5%B7%A5%E5%85%B7%E7%9A%84%E4%BD%BF%E7%94%A8)

# zookeeper开源客户端curator

## curator简介

curator框架在zookeeper原生API接口上进行了封装，解决很多zookeeper客户端非常底层的细节开发。提供了zookeeper各种应用场景（比如：分布式锁服务、集群领导选举、共享计数器、缓存机制、分布式队列等）的抽象封装，是最好用，最流行的zookeeper的客户端。

原生zookeeperAPI的不足：

- 连接对象异步创建，需要开发人员自行编码等待
- 连接没有自动重连超时机制
- watcher一次注册生效一次
- 不支持递归创建树形节点

curator特点：

- 解决session会话超时重连
- watcher反复注册
- 简化开发API
- 遵循Fluent风格的API
- 提供了分布式锁服务、共享计数器、缓存机制等

```pom
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>CuratorAPI</groupId>
    <artifactId>CuratorAPI</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.7</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-framework</artifactId>
            <version>2.6.0</version>
            <type>jar</type>
            <exclusions>
                <exclusion>
                    <groupId>org.apache.zookeeper</groupId>
                    <artifactId>zookeeper</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>3.4.14</version>
            <type>jar</type>
        </dependency>
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-recipes</artifactId>
            <version>2.6.0</version>
            <type>jar</type>
        </dependency>
    </dependencies>

</project>
```

## 连接到zookeeper

```java
package com.itcast.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryOneTime;

/**
 * @author zhanghao
 * @date 2020/6/9 - 9:21
 */
public class CuratorConnection {
    public static void main(String[] args) {
        // 创建连接对象
        CuratorFramework client = CuratorFrameworkFactory.builder()
                // ip地址端口号
                .connectString("192.168.44.139:2181,192.168.44.139:2182,192.168.44.139:2183")
                // 会话超时时间
                .sessionTimeoutMs(5000)
                // 重连机制
                .retryPolicy(new RetryOneTime(3000))
                // 命名空间
                .namespace("create")
                // 构建连接对象
                .build();
        // 打开链接
        client.start();
        System.out.println(client.isStarted());
        // 关闭连接
        client.close();
    }
}
```

## 新增节点

```java
package com.itcast.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhanghao
 * @date 2020/6/9 - 9:37
 */
public class CuratorCreate {

    String IP = "192.168.44.139:2181,192.168.44.139:2182,192.168.44.139:2183";
    CuratorFramework client;

    @Before
    public void before(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3 );
        client = CuratorFrameworkFactory.builder()
                .connectString(IP)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace("create")
                .build();
        client.start();
    }

    @After
    public void after(){
        client.close();
    }

    @Test
    public void create1() throws Exception {
        client.create()
                // 节点类型
                .withMode(CreateMode.PERSISTENT)
                // 节点的权限列表
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                // arg1:节点的路径 arg2:节点的数据
                .forPath("/node1","node1".getBytes());
        System.out.println("end");
    }

    @Test
    public void create2() throws Exception {
        // 自定义权限列表
        List<ACL> list = new ArrayList<>();
        // 授权模式和授权对象
        Id id = new Id("ip", "192.168.44.139");
        list.add(new ACL(ZooDefs.Perms.ALL,id));
        client.create().withMode(CreateMode.PERSISTENT).withACL(list).forPath("/node2","node2".getBytes());
        System.out.println("end");
    }

    @Test
    public void create3() throws Exception {
        // 递归创建节点树
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .forPath("/node3/node33","node33".getBytes());
        System.out.println("end");
    }

    @Test
    public void create4() throws Exception {
        // 异步方式创建节点
        client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                // 异步回调接口
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                        System.out.println(event.getPath());
                        System.out.println(event.getType());
                    }
                })
                .forPath("/node4","node4".getBytes());
        try { TimeUnit.SECONDS.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println("end");

    }
}
```

## 更新节点

```java
package com.itcast.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhanghao
 * @date 2020/6/9 - 9:37
 */
public class CuratorSet {

    String IP = "192.168.44.139:2181,192.168.44.139:2182,192.168.44.139:2183";
    CuratorFramework client;

    @Before
    public void before(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3 );
        client = CuratorFrameworkFactory.builder()
                .connectString(IP)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace("set")
                .build();
        client.start();
    }

    @After
    public void after(){
        client.close();
    }

    @Test
    public void set1()throws Exception {
        // 更新节点
        client.setData()
                .forPath("/node1","node12".getBytes());
    }

    @Test
    public void set2()throws Exception {
        client.setData()
                .withVersion(-1)
                .forPath("/node1","node1".getBytes());
        System.out.println("end");
    }

    @Test
    public void set3()throws Exception {
        // 异步方式修改节点数据
        client.setData()
                .withVersion(-1)
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                        System.out.println(event.getPath());
                        System.out.println(event.getType());
                    }
                }).forPath("/node1","node12".getBytes());
        try { TimeUnit.SECONDS.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println("end");
    }
}
```

## 删除节点

```java
package com.itcast.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author zhanghao
 * @date 2020/6/9 - 9:37
 */
public class CuratorDelete {

    String IP = "192.168.44.139:2181,192.168.44.139:2182,192.168.44.139:2183";
    CuratorFramework client;

    @Before
    public void before(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3 );
        client = CuratorFrameworkFactory.builder()
                .connectString(IP)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace("delete")
                .build();
        client.start();
    }

    @After
    public void after(){
        client.close();
    }

    @Test
    public void delete1()throws Exception {
        // 删除节点
        client.delete()
                .forPath("/node1");
        System.out.println("end");
    }

    @Test
    public void delete2()throws Exception {
        client.delete()
                .withVersion(0)
                .forPath("/node1");
        System.out.println("end");
    }

    @Test
    public void delete3()throws Exception {
        client.delete()
                .deletingChildrenIfNeeded()
                .withVersion(-1)
                .forPath("/node1");
        System.out.println("end");
    }

    @Test
    public void delete4()throws Exception {
        // 异步方式删除节点
        client.delete()
                .deletingChildrenIfNeeded()
                .withVersion(-1)
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                        System.out.println(event.getType());
                        System.out.println(event.getPath());
                    }
                }).forPath("/node1");
        try { TimeUnit.SECONDS.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println("end");
    }
}
```

## 查看节点

```java
package com.itcast.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author zhanghao
 * @date 2020/6/9 - 9:37
 */
public class CuratorGet {

    String IP = "192.168.44.139:2181,192.168.44.139:2182,192.168.44.139:2183";
    CuratorFramework client;

    @Before
    public void before(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3 );
        client = CuratorFrameworkFactory.builder()
                .connectString(IP)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace("get")
                .build();
        client.start();
    }

    @After
    public void after(){
        client.close();
    }

    @Test
    public void get1()throws Exception {
        byte[] bytes = client.getData()
                .forPath("/node2");
        System.out.println(new String(bytes));
    }

    @Test
    public void get2()throws Exception {
        // 读取节点的属性
        Stat stat = new Stat();
        byte[] bytes = client.getData().storingStatIn(stat)
                .forPath("/node1");
        System.out.println(new String(bytes));
        System.out.println(stat.getVersion());
    }

    @Test
    public void get3()throws Exception {
        // 异步方式读取节点的数据
        client.getData()
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                        System.out.println(event.getPath());
                        System.out.println(event.getType());
                        System.out.println(new String(event.getData()));
                    }
                }).forPath("/node3");
        try { TimeUnit.SECONDS.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println("end");
    }
}
```

## 查看子节点

```java
package com.itcast.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhanghao
 * @date 2020/6/9 - 9:37
 */
public class CuratorGetChild {

    String IP = "192.168.44.139:2181,192.168.44.139:2182,192.168.44.139:2183";
    CuratorFramework client;

    @Before
    public void before(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3 );
        client = CuratorFrameworkFactory.builder()
                .connectString(IP)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
    }

    @After
    public void after(){
        client.close();
    }

    @Test
    public void getChild1()throws Exception {
        List<String> list = client.getChildren()
                .forPath("/get");
        for (String str : list) {
            System.out.println(str);
        }
    }

    @Test
    public void getChild2()throws Exception {
        // 异步方式读取子节点数据
        client.getChildren()
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                        List<String> list = event.getChildren();
                        System.out.println(list);
                        System.out.println(event.getType());
                        System.out.println(event.getPath());
                    }
                })
                .forPath("/get");
        try { TimeUnit.SECONDS.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println("end");
    }

}
```

## 检查节点是否存在

```java
package com.itcast.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhanghao
 * @date 2020/6/9 - 9:37
 */
public class CuratorExists {

    String IP = "192.168.44.139:2181,192.168.44.139:2182,192.168.44.139:2183";
    CuratorFramework client;

    @Before
    public void before(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3 );
        client = CuratorFrameworkFactory.builder()
                .connectString(IP)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace("get")
                .build();
        client.start();
    }

    @After
    public void after(){
        client.close();
    }

    @Test
    public void exists1()throws Exception {
        Stat stat = client.checkExists()
                .forPath("/node1");
        System.out.println(stat);
    }

    @Test
    public void exists2()throws Exception {
        // 异步方式判断节点是否存在
        client.checkExists()
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                        System.out.println(event.getType());
                        System.out.println(event.getPath());
                        System.out.println(event.getStat());
                    }
                }).forPath("/node2");
        try { TimeUnit.SECONDS.sleep(4);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println("end");
    }
}
```

## watcherAPI

curator提供了两种watcher（cache）来监听节点的变化

- Node Cache：只是监听某一个特定的节点，监听节点的新增和修改
- PathChildren Cache：监控一个ZNode的子节点，当一个子节点增加，更新，删除时，Path Cache会改变它的状态，会包含最新的子节点，子节点的数据和状态

```java
package com.itcast.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author zhanghao
 * @date 2020/6/9 - 9:37
 */
public class CuratorWatcher {

    String IP = "192.168.44.139:2181,192.168.44.139:2182,192.168.44.139:2183";
    CuratorFramework client;

    @Before
    public void before(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3 );
        client = CuratorFrameworkFactory.builder()
                .connectString(IP)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
    }

    @After
    public void after(){
        client.close();
    }

    @Test
    public void watcher1()throws Exception {
        // 监听某个节点的数据变化
        //arg1:连接对象 arg2:监听的节点路径
        final NodeCache nodeCache = new NodeCache(client, "/watcher1");
        nodeCache.start();
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            // 节点变化回调的方法
            @Override
            public void nodeChanged() throws Exception {
                System.out.println(nodeCache.getCurrentData().getPath());
                System.out.println(new String(nodeCache.getCurrentData().getData()));
            }
        });
        try { TimeUnit.SECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println("end");
        nodeCache.close();
    }

    @Test
    public void watcher2()throws Exception {
        // 监视子节点的变化
        //arg1:连接对象 arg2:监视的节点路径 arg3:事件中是否可以获取节点的数据
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, "/watcher1", true);
        pathChildrenCache.start();

        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            // 当子节点发声变化时回调的方法
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                System.out.println(event.getType());
                System.out.println(event.getData().getPath());
                System.out.println(new String(event.getData().getData()));
            }
        });

        try { TimeUnit.SECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println("end");
        pathChildrenCache.close();

    }
}
```

## 事务

```java
package com.itcast.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author zhanghao
 * @date 2020/6/9 - 9:37
 */
public class CuratorTransaction {

    String IP = "192.168.44.139:2181,192.168.44.139:2182,192.168.44.139:2183";
    CuratorFramework client;

    @Before
    public void before(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3 );
        client = CuratorFrameworkFactory.builder()
                .connectString(IP)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace("create")
                .build();
        client.start();
    }

    @After
    public void after(){
        client.close();
    }

    @Test
    public void ts1()throws Exception {
        // 第一句会成功
        client.create().forPath("/node1","node1".getBytes());
        client.setData().forPath("/node2","node2".getBytes());
    }

    @Test
    public void ts2()throws Exception {
        // 全部失败
        client.inTransaction()
                .create().forPath("/node1","node1".getBytes())
                .and()
                .setData().forPath("/node2","node2".getBytes())
                .and()
                .commit();
    }
}
```

## 分布式锁

InterProcessMutex: 分布式可重入排他锁

InterProcessReadWriteLock: 分布式读写锁

```java
package com.itcast.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author zhanghao
 * @date 2020/6/9 - 9:37
 */
public class CuratorLock {

    String IP = "192.168.44.139:2181,192.168.44.139:2182,192.168.44.139:2183";
    CuratorFramework client;

    @Before
    public void before(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3 );
        client = CuratorFrameworkFactory.builder()
                .connectString(IP)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
    }

    @After
    public void after(){
        client.close();
    }

    @Test
    public void lock1()throws Exception {
        // 排它锁
        // arg1:连接对象 arg2:节点路径
        InterProcessLock interProcessLock = new InterProcessMutex(client, "/lock1");
        System.out.println("等待获取锁对象");
        interProcessLock.acquire();
        for (int i = 0; i < 10; i++) {
            try { TimeUnit.SECONDS.sleep(3);} catch (InterruptedException e) {e.printStackTrace();}
            System.out.println(i);
        }
        interProcessLock.release();
        System.out.println("等待释放锁");
    }

    @Test
    public void lock2()throws Exception {
        // 读写锁
        InterProcessReadWriteLock interProcessReadWriteLock = new InterProcessReadWriteLock(client, "/lock1");
        // 获取读锁对象
        InterProcessMutex interProcessLock = interProcessReadWriteLock.readLock();
        System.out.println("等待获取锁对象");
        interProcessLock.acquire();
        for (int i = 0; i < 10; i++) {
            try { TimeUnit.SECONDS.sleep(3);} catch (InterruptedException e) {e.printStackTrace();}
            System.out.println(i);
        }
        interProcessLock.release();
        System.out.println("等待释放锁");
    }

    @Test
    public void lock3()throws Exception {
        // 读写锁
        InterProcessReadWriteLock interProcessReadWriteLock = new InterProcessReadWriteLock(client, "/lock1");
        // 获取写锁对象
        InterProcessMutex interProcessLock = interProcessReadWriteLock.writeLock();
        System.out.println("等待获取锁对象");
        interProcessLock.acquire();
        for (int i = 0; i < 10; i++) {
            try { TimeUnit.SECONDS.sleep(3);} catch (InterruptedException e) {e.printStackTrace();}
            System.out.println(i);
        }
        interProcessLock.release();
        System.out.println("等待释放锁");
    }
}
```

# zookeeper四字监控命令

zookeeper支持某些特定的四字命令与其的交互。它们大多是查询命令，用来获取zookeeper服务的当前状态及相关信息。用户客户端可以通过telnet或nc向zookeeper提交相应的命令。zookeeper常用四字命令如下表所示：

| 命令 | 描述                                                         |
| ---- | ------------------------------------------------------------ |
| conf | 输出相关服务器配置的详细信息。比如端口、zk数据及日志配置路径、最大连接数，session超时时间、serverId等 |
| cons | 列出所有连接到这台服务器的客户端连接/会话的详细信息。包括“接收/发送”的包数量、session id、操作延迟、最后的操作执行等信息 |
| crst | 重置当前这台服务器所有连接/会话的统计信息                    |
| dump | 列出未经处理的会话和临时节点                                 |
| envi | 输出关于服务器的环境详细信息                                 |
| ruok | 测试服务是否处于正常运行状态。如果正常返回“imok”，否则返回空 |
| stat | 输出服务器的详细信息：接收/发送包数量、连接数、模式（leader/follower）、节点总数、延迟。所有客户端的列表 |
| srst | 重置server状态                                               |
| wchs | 列出服务器watches的简洁信息：连接总数、watching节点总数和watches总数 |
| wchc | 通过session分组，列出watch的所有节点，它的输出是一个与watch相关的会话的节点列表 |
| mntr | 列出集群的健康状态。包括“接收/发送“的包数量、操作延迟、当前服务器模式（leader/follower）、节点总数、watch总数、临时节点总数 |

```shell
[root@localhost ~]# telnet 192.168.44.139 2181
Trying 192.168.44.139...
Connected to 192.168.44.139.
Escape character is '^]'.
mntr
zk_version	3.4.14-4c25d480e66aadd371de8bd2fd8da255ac140bcf, built on 03/06/2019 16:18 GMT
zk_avg_latency	0
zk_max_latency	69
zk_min_latency	0
zk_packets_received	4061
zk_packets_sent	4068
zk_num_alive_connections	2
zk_outstanding_requests	0
zk_server_state	follower
zk_znode_count	22
zk_watch_count	0
zk_ephemerals_count	0
zk_approximate_data_size	287
zk_open_file_descriptor_count	34
zk_max_file_descriptor_count	4096
zk_fsync_threshold_exceed_count	0
Connection closed by foreign host.
```

```shell
[root@localhost ~]# echo mntr | nc 192.168.44.139 2181
zk_version	3.4.14-4c25d480e66aadd371de8bd2fd8da255ac140bcf, built on 03/06/2019 16:18 GMT
zk_avg_latency	0
zk_max_latency	69
zk_min_latency	0
zk_packets_received	4077
zk_packets_sent	4084
zk_num_alive_connections	2
zk_outstanding_requests	0
zk_server_state	follower
zk_znode_count	22
zk_watch_count	0
zk_ephemerals_count	0
zk_approximate_data_size	287
zk_open_file_descriptor_count	34
zk_max_file_descriptor_count	4096
zk_fsync_threshold_exceed_count	0
```

## conf命令

输出相关服务器配置的详细信息。

```shell
[root@localhost ~]# echo conf | nc 192.168.44.139 2181
```

| 属性              | 含义                                                         |
| ----------------- | ------------------------------------------------------------ |
| clientPort        | 客户端端口号                                                 |
| dataDir           | 数据快照文件目录默认情况下100000次事务操作生成一次快照       |
| dataLogDir        | 事务日志文件目录，生产环境中放在独立的磁盘                   |
| tickTime          | 服务器之间或客户端与服务器之间维持心跳的时间间隔（以毫秒为单位） |
| maxClientsCnxns   | 最大连接数                                                   |
| minSessionTimeout | 最小session超时 minSessionTimeout=tickTime*2                 |
| maxSessionTimeout | 最大session超时 maxSessionTimeout=tickTime*20                |
| serverId          | 服务器编号                                                   |
| initLimit         | 集群中follower服务器与leader服务器之间初始连接时能容忍的最多心跳数 |
| syncLimit         | 集群中follower服务器与leader服务器之间 请求和应答之间能容忍的最多心跳数 |
| electionAlg       | 选举的算法 3：基于TCP的FastLeaderElection                    |
| electionPort      | 选举端口                                                     |
| quorumPort        | 数据通信端口                                                 |
| peerType          | 是否为观察者 1为观察者                                       |

## cons命令

列出所有连接到这台服务器的客户端连接/会话的详细信息

```shell
[root@localhost ~]# echo cons | nc 192.168.44.139 2181
 /192.168.44.139:33878[1](queued=0,recved=1049,sent=1049,sid=0x10002a6eee40004,lop=PING,est=1591667162473,to=30000,lcxid=0x3c,lzxid=0xffffffffffffffff,lresp=77318002,llat=1,minlat=0,avglat=0,maxlat=69)
 /192.168.44.139:33890[0](queued=0,recved=1,sent=0)
```

| 属性   | 含义                                                 |
| ------ | ---------------------------------------------------- |
| ip     | ip地址                                               |
| port   | 端口号                                               |
| queued | 等待被处理的请求数，请求缓存在队列中                 |
| recved | 收到的包数                                           |
| send   | 发送的包数                                           |
| sid    | 会话id                                               |
| lop    | 最后的操作 GETD-读取数据 DELE-删除数据 CREA-创建数据 |
| est    | 连接时间戳                                           |
| to     | 超时时间                                             |
| lcxid  | 当前会话的操作id                                     |
| lzxid  | 最大事务id                                           |
| lresp  | 最后响应时间戳                                       |
| llat   | 最后、最新 延迟                                      |
| minlat | 最小延迟                                             |
| avglat | 最大延迟                                             |
| maxlat | 平均延迟                                             |

## crst命令

重置当前这台服务器所有连接/会话的统计信息

```shell
[root@localhost ~]# echo crst | nc 192.168.44.139 2181
Connection stats reset.
```

## dump命令

列出未经处理的会话和临时节点

```shell
[root@localhost ~]# echo dump | nc 192.168.44.139 2181
SessionTracker dump:
org.apache.zookeeper.server.quorum.LearnerSessionTracker@7200ec6
ephemeral nodes dump:
Sessions with Ephemerals (2):
0x10002a6eee40014:
	/tmp1
	/tmp
0x10002a6eee40015:
	/tmp22
	/tmp2
[root@localhost ~]# 
```

## envi命令

  输出关于服务器的环境详细信息

```shell
[root@localhost ~]# echo envi | nc 192.168.44.139 2181
```

| 属性              | 含义                                        |
| ----------------- | ------------------------------------------- |
| zookeeper.version | 版本                                        |
| host.name         | host信息                                    |
| java.version      | java版本                                    |
| java.vendor       | 供应商                                      |
| java.home         | 运行环境所在目录                            |
| java.class.path   | classpath                                   |
| java.library.path | 第三方库指定非java类包的位置（如：dll，so） |
| java.compiler     | JIT编译器的名称                             |
| os.name           | Linux                                       |
| os.arch           | amd64                                       |
| os.version        | 内核版本号：3.10.0-862.el7.x86_64           |
| user.name         | zookeeper                                   |
| user.home         | /home/zookeeper                             |
| user.dir          | /home/zookeeper/zookeeper2181/bin           |
| java.io.tmpdir    | 默认的临时文件路径                          |

## ruok命令

测试服务是否处于正常运行状态

```shell
[root@localhost ~]# echo ruok | nc 192.168.44.139 2181
imok
```

## stat命令

输出服务器的详细信息，与srvr相似，但是多了每个连接的会话信息

```shell
[root@localhost ~]# echo stat | nc 192.168.44.139 2183
Zookeeper version: 3.4.14-4c25d480e66aadd371de8bd2fd8da255ac140bcf, built on 03/06/2019 16:18 GMT
Clients:
 /192.168.44.139:46390[1](queued=0,recved=1,sent=1)
 /192.168.44.139:46392[0](queued=0,recved=1,sent=0)

Latency min/avg/max: 0/0/61
Received: 2996
Sent: 2995
Connections: 2
Outstanding: 0
Zxid: 0x1000000bd
Mode: observer
Node count: 26
```



| 属性                | 含义       |
| ------------------- | ---------- |
| Zookeeper version   | 版本       |
| Latency min/avg/max | 延迟       |
| Received            | 收包       |
| Sent                | 发包       |
| Connections         | 连接数     |
| Outstanding         | 堆积数     |
| Zxid                | 最大事务id |
| Mode                | 服务器角色 |
| Node count          | 节点数     |

## srst命令

重置server状态

```shell
[root@localhost ~]# echo srst | nc 192.168.44.139 2182
Server stats reset.
```

## wchs命令

列出服务器watches的简洁信息

```shell
[root@localhost ~]# echo wchs | nc 192.168.44.139 2181
2 connections watching 2 paths
Total watches:2
```

## wchc命令

通过session分组，列出watch的所有节点，它的输出是一个与watch相关的会话的节点列表

```shell
[root@localhost ~]# echo wchc | nc 192.168.44.139 2181
wchc is not executed because it is not in the whitelist.
```

**解决**

```sh
# 修改zkServer.sh 
    echo "JMX disabled by user request" >&2
    ZOOMAIN="org.apache.zookeeper.server.quorum.QuorumPeerMain"
fi
# 添加以下内容
ZOOMAIN="-Dzookeeper.4lw.commands.whitelist=* ${ZOOMAIN}"
```

```shell
[zookeeper@localhost bin]$ echo wchc | nc 192.168.44.139 2181
0x10004d4d1e50000
	/watcher1
	/watcher2
```

## wchp命令

通过路径分组，列出所有的watch的session id信息

```shell
[zookeeper@localhost bin]$ echo wchp | nc 192.168.44.139 2181
/watcher1
	0x10004d4d1e50000
/watcher2
	0x10004d4d1e50000
```

## mntr命令

列出集群的健康状态

```shell
[zookeeper@localhost bin]$ echo mntr | nc 192.168.44.139 2181
zk_version	3.4.14-4c25d480e66aadd371de8bd2fd8da255ac140bcf, built on 03/06/2019 16:18 GMT
zk_avg_latency	0 #延迟
zk_max_latency	9
zk_min_latency	0
zk_packets_received	72
zk_packets_sent	71
zk_num_alive_connections	2
zk_outstanding_requests	0 #堆积请求数
zk_server_state	follower 
zk_znode_count	22
zk_watch_count	2
zk_ephemerals_count	0
zk_approximate_data_size	287 #数据大小
zk_open_file_descriptor_count	34 #打开文件描述符数量
zk_max_file_descriptor_count	4096 #最大文件描述符数量
zk_fsync_threshold_exceed_count	0
```

# zookeeper图形化的客户端工具（ZooInspector）

下载地址

```
https://issues.apache.org/jira/secure/attachment/12436620/ZooInspector.zip
```

解压进入ZooInspector\build

```cmd
java -jar .\zookeeper-dev-ZooInspector.jar
```

# taokeeper监控工具的使用































