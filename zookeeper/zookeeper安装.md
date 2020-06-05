# zookeeper单机安装

当前系统环境：centos7

jdk：jdk-8u60-linux-x64.tar.gz

zookeeper：zookeeper-3.4.14.tar.gz

1、在centos中使用root用户创建zookeeper用户，用户名：zookeeper，密码：zookeeper

```shell
[root@localhost jack]# useradd zookeeper
[root@localhost jack]# passwd zookeeper
```

2、zookeeper底层依赖于jdk，zookeeper用户登录后，根目录下先进性jdk的安装，jdk使用jdk-8u60-linux-x64.tar.gz版本，上传并解压jdk

```shell
[zookeeper@localhost ~]$ tar -zxvf jdk-8u60-linux-x64.tar.gz 
```

3、配置jdk环境变量

```shell
[zookeeper@localhost ~]$ vim .bash_profile 

# .bash_profile

# Get the aliases and functions
if [ -f ~/.bashrc ]; then
        . ~/.bashrc
fi

# User specific environment and startup programs

PATH=$PATH:$HOME/.local/bin:$HOME/bin

export PATH

JAVA_HOME=/home/zookeeper/jdk1.8.0_60
export JAVA_HOME

PATH=$JAVA_HOME/bin:$PATH
export PATH
```

```shell
# 使环境变量生效
[zookeeper@localhost ~]$ . .bash_profile 
```

4、检测jdk安装

```shell
[zookeeper@localhost ~]$ java -version
java version "1.8.0_60"
Java(TM) SE Runtime Environment (build 1.8.0_60-b27)
Java HotSpot(TM) 64-Bit Server VM (build 25.60-b23, mixed mode)
```

5、zookeeper使用zookeeper-3.4.14.tar.gz ，上传并解压

```shell
[zookeeper@localhost ~]$ tar -xzvf zookeeper-3.4.14.tar.gz 
```

6、为zookeeper准备配置文件

```shell
[zookeeper@localhost ~]$ cd zookeeper-3.4.14/conf/
[zookeeper@localhost conf]$ cp zoo_sample.cfg zoo.cfg
[zookeeper@localhost conf]$ cd ..
[zookeeper@localhost zookeeper-3.4.14]$ mkdir data
[zookeeper@localhost zookeeper-3.4.14]$ cd conf/
[zookeeper@localhost conf]$ vim zoo.cfg 
# 修改为创建的data路径
dataDir=/home/zookeeper/zookeeper-3.4.14/data
```

7、启动zookeeper

```shell
[zookeeper@localhost bin]$ ./zkServer.sh start
ZooKeeper JMX enabled by default
Using config: /home/zookeeper/zookeeper-3.4.14/bin/../conf/zoo.cfg
Starting zookeeper ... STARTED
[zookeeper@localhost bin]$ ./zkServer.sh status
ZooKeeper JMX enabled by default
Using config: /home/zookeeper/zookeeper-3.4.14/bin/../conf/zoo.cfg
Mode: standalone
[zookeeper@localhost bin]$ ./zkCli.sh 
# 启动 zkServer.sh start
# 停止 zkServer.sh stop
# 查看状态 zkServer.sh status
```

