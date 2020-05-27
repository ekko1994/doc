# 一、redis 安装及启动

## 1、安装redis

```shell
$ wget http://download.redis.io/releases/redis-4.0.0.tar.gz
$ tar xzf redis-4.0.0.tar.gz
$ cd redis-4.0.0
$ make install
```

## 2、指定配置文件启动

```shell
[root@centos7 redis-4.0.0]# redis-server conf/redis-6379.conf 
[root@centos7 redis-4.0.0]# ps -ef|grep redis-
root     42480     1  0 18:20 ?        00:00:00 redis-server *:6379
root     42485 41663  0 18:21 pts/2    00:00:00 grep --color=auto redis-
```

# 二、redis 持久化

## 1、持久化简介

**什么是持久化？**

利用永久性存储介质将数据进行保存，在特定的时间将保存的数据进行恢复的工作机制称为持久化

**为什么要进行持久化？**

防止数据的意外丢失，确保数据安全性

**持久化过程保存什么？**

- 将当前数据状态进行保存，快照形式，存储数据结果，存储格式简单，关注点在数据
- 将数据的操作过程进行保存，日志形式，存储操作过程，存储格式复杂，关注点在数据的操作过程

![](https://github.com/jackhusky/doc/tree/master/redis/images/rdb持久化方式.png)

## 2、RDB

### 2.1、RDB启动方式

#### save指令

```shell
127.0.0.1:6379> set name jack
OK
127.0.0.1:6379> SAVE
OK
```

#### save指令相关配置

- `dbfilename` dump.rdb

  说明：设置本地数据库文件名，默认值为dump.rdb

  经验：通常设置为**dump-端口号.rdb**

- `dir `

  说明：设置存储.rdb文件的路径

  经验：通常设置成存储空间较大的目录中，目录名称**data**

- `rdbcompression` yes

  说明：设置存储至本地数据库时是否研所数据，默认为yes，采用LZF压缩

  经验：通常默认为开启状态，如果设置为no，可以节省CPU运行时间，但会使存储的文件变大（巨大）

- `rdbchecksum` yes

  说明：设置是否进行RDB文件格式校验，该校验过程在写文件和读文件过程均进行

  经验：通常默认为开启状态，如果设置为no，可以节约读写过程约10%时间消耗，但是存储一定的数据损坏风险

#### save指令工作原理

![](https://github.com/jackhusky/doc/tree/master/redis/images/save指令工作原理.png)

**注意：save指令的执行会阻塞当前redis服务器，直到当前RDB过程完成为止，有可能会造成长时间阻塞，线上环境不建议使用**

数据量过大，单线程执行方式造成效率过低如何处理？

**后台执行**

#### bgsave指令

```shell
127.0.0.1:6379> set addr beijing
OK
127.0.0.1:6379> BGSAVE
Background saving started
```

####bgsave指令工作原理

![](https://github.com/jackhusky/doc/tree/master/redis/images/bgsave指令工作原理.png)

**注意：bgsave命令是针对save阻塞问题做的优化。redis内部所有涉及到RDB操作都采用bgsave的方式，save命令可以放弃使用。**

#### bgsave指令相关配置

`save`指令配置（上面已有）+ stop-writes-on-bgsave-error yes

说明：后台存储过程中如果出现错误现象，是否停止保存操作

经验：通常默认为开启状态

#### save配置

- 配置

  ```conf
  save second changes
  ```

- 作用

  满足限定时间范围内key的变化数量达到指定数量即进行持久化

- 参数

  `second`：监控时间范围

  `changes`：监控key的变化量

- 位置

  在`conf`文件中进行配置

- 范例

  ```conf
  save 900 1
  save 300 10
  save 60 10000
  ```

#### save配置原理

![](https://github.com/jackhusky/doc/tree/master/redis/images/save配置原理.png)

**注意：save配置要根据实际业务情况进行配置，频度过高或过低都会出现性能问题，结果可能是灾难性的**

​			**save配置中对于second与changes设置通常具有互补对应关系，尽量不要设置成包含性关系**

​			**save配置启动后执行的是bgsave操作**

#### RDB两种启动方式对比

| 方式           | save指令 | bgsave指令 |
| -------------- | -------- | ---------- |
| 读写           | 同步     | 异步       |
| 阻塞客户端指令 | 是       | 否         |
| 额外内存消耗   | 否       | 是         |
| 启动新进程     | 否       | 是         |

#### RDB特殊启动形式

- 全量复制（在主从复制中详解）

- 服务器运行过程中重启

  ```shell
  debug reload
  ```

- 关闭服务器时指定保存数据

  ```shell
  shutdown save
  ```

### 2.2、RDB优点和缺点

#### 优点

- `RDB`是一个紧凑压缩的二进制文件，存储效率较高
- `RDB`内部存储的是`redis`在某个时间点的数据快照，非常适合用于数据备份，全量复制等场景
- `RDB`恢复数据的速度要比`AOF`块很多
- 应用：服务器中每X小时执行`bgsave`备份，并将`RDB`文件拷贝到远程机器中，用于灾难恢复

#### 缺点

- `RDB`方式无论是执行指令还是利用配置，无法做到实时持久化，具有较大的可能性丢失数据
- `bgsve`指令每次运行要执行`fork`操作创建子进程，要牺牲掉一些性能
- `redis`的众多版本中未进行`RDB`文件格式的版本统一，有可能出现各版本服务之间数据格式无法兼容现象

## 3、AOF

**RDB存储的弊端**

- 存储数据较大，效率较低

  基于快照思想，每次读写都是全部数据，当数据量巨大时，效率非常低

- 大数据量下的IO性能较低

- 基于`fork`创建子进程，内存产生额外消耗

- 宕机带来的数据丢失风险

**解决思路**

- 不写全数据，仅记录部分数据
- 改记录数据为操作过程
- 对所有操作均进行记录，排除丢失数据的风险

### 3.1、AOF概念

- 以独立日志的方式记录每次写命令，重启时再重新执行`AOF`文件中命令达到恢复数据的目的。与`RDB`相比可以简单描述为改记录数据为记录数据产生的过程

- `AOF`的主要作用是解决了数据持久化的实时性，目前已经是`redis`持久化的主流方式

### 3.2、AOF写数据过程

![](https://github.com/jackhusky/doc/tree/master/redis/images/AOF写数据过程.png)

**AOF写数据三种策略（appendfsync）**

- `always`（每次）

  每次写入操作均同步到AOF文件中，**数据零误差，性能较差**

- `everysec`（每秒）

  每秒将缓冲区中的指令同步到AOF文件中，**数据准确性较高，性能较高**，建议使用，也是默认配置

  在系统突然宕机的情况下丢失1秒内的数据

- `no`（系统控制）

  有操作系统控制每次同步到AOF文件的周期，整体过程**不可控**

### 3.3、AOF功能开启

- 配置

  ```conf
  appendonly yes|no
  ```

- 作用

  是否开启`AOF`持久化功能，默认为不开启状态

- 配置

  ```conf
  appendfsync always|everysec|no
  ```

- 作用

  `AOF`写数据策略

### 3.4、AOF相关配置

- 配置

  ```conf
  appendfilename filename
  ```

- 作用

  `AOF`持久化文件名，默认文件名是appendonly.aof，建议配置为appendonly-端口号.aof

- 配置

  ```conf
  dir
  ```

- 作用

  `AOF`持久化文件保存路径，与`RDB`持久化文件保持一致即可

### 3.5、AOF重写

随着命令不断写入`AOF`，文件会越来越大，为了解决这个问题，`redis`引入了`AOF`重写机制压缩文件体积。`AOF`文件重写是将`redis`进程内的数据转化为写命令同步到新`AOF`文件的过程。简单说就是将对同一个数据的若干个条命令执行结果转化成最终结果数据对应的指令进行记录

#### 作用

- 降低磁盘占用量，提高磁盘利用率
- 提高持久化效率，降低持久化写时间，提高IO性能
- 降低数据恢复用时，提高数据恢复效率

#### 重写规则

- 进程内已超时的数据不再写入文件

- 忽略无效指令，重写时使用进程内数据直接生成，这样新的`AOF`文件只保留最终数据的写入命令

  如del key1、hdel key2、srem key2、set key4 111、set key4 222等

- 对同一数据的多条写命令合并为一条命令

  如lpush list a、lpush list b、lpush list c可以转化为：lpush list a b c

  为防止数据量过大造成客户端缓冲区溢出，对list、set、hash、zset等类型，每条指令最多写入64个元素

#### AOF重写方式

- 手动重写

  ```shell
  bgrewriteaof
  ```

- 自动重写

  ```shell
  auto-aof-rewrite-percentage percentage
  auto-aof-rewrite-min-size  size
  ```

#### bgrewriteaof指令工作原理

![](https://github.com/jackhusky/doc/tree/master/redis/images/AOF手动重写_bgrewriteaof指令工作原理.png)

#### AOF自动重写方式

- 自动重写触发条件设置

  ```conf
  auto-aof-rewrite-percentage percent
  auto-aof-rewrite-min-size  size
  ```

- 自动重写出发比对参数（运行指令`info Persistence`获取具体信息）

  ```conf
  aof_current_size
  aof_base_size
  ```

- 自动重写触发条件

  ```conf
  aof_current_size > auto-aof-rewrite-min-size
  (aof_current_size - aof_base_size)/aof_base_size >= auto-aof-rewrite-percentage
  ```

#### AOF重写工作原理

![](https://github.com/jackhusky/doc/tree/master/redis/images/AOF重写流程.png)

![](https://github.com/jackhusky/doc/tree/master/redis/images/AOF重写工作原理.png)

## 4、RDB与AOF区别

| 持久化方式   | RDB                | AOF                |
| ------------ | ------------------ | ------------------ |
| 占用存储空间 | 小（数据级：压缩） | 大（指令级：重写） |
| 存储速度     | 慢                 | 块                 |
| 恢复速度     | 块                 | 慢                 |
| 数据安全性   | 会丢失数据         | 依据策略决定       |
| 资源消耗     | 高/重量级          | 低/轻量级          |
| 启动优先级   | 低                 | 高                 |

### 4.1、RDB与AOF的选择？

- 对数据非常敏感，建议使用默认的`AOF`持久化方案
  - `AOF`持久化策略使用everysecond，每秒钟`fsync`一次。该策略`redis`仍可以保持很好的处理性能，当出现问题时，最多丢失0-1秒内的数据
  - 注意：优于`AOF`文件存储体积较大，且恢复速度较慢
- 数据呈现阶段有效性，建议使用`RDB`持久化方案
  - 数据可以良好的做到阶段内无丢失（该阶段是开发者或运维人员手工维护的），且恢复速度较快，阶段点数据恢复通常采用`RDB`方案
  - 注意：利用`RDB`实现紧凑的数据持久化会使redis降的很低
- 综合比对
  - `RDB`与`AOF`的选择实际上是在做一种权衡，每种都有利弊
  - 如不能承受数分钟以内的数据丢失，对业务数据非常敏感，选用`AOF`
  - 如能承受数分钟以内的数据丢失，且追求大数据集的恢复速度，选择`RDB`
  - 灾难恢复选用`RDB`
  - 双保险策略，同时开启`RDB`和`AOF`，重启后，`redis`优先使用`AOF`来恢复数据，降低丢失数据的量

> redis-check-aof --fix  appendonly-6379.aof 可以用来修复坏的AOF文件，RDB同理

### 4.2、持久化应用场景

![](https://github.com/jackhusky/doc/tree/master/redis/images/持久化应用场景.png)

## 5、redis 的事务

### 5.1、事务简介

什么是事务？

`redis`执行指令过程中，多条连续执行的指令被干扰，打断，插队

`redis`事务就是一个命令执行的队列，将一系列预定义命令包装成一个整体（一个队列）。当执行时，一次性按照添加顺序依次执行，中间不会被打断或者干扰。

一个队列中，一次性、顺序性、排他性的执行一系列命令

![](https://github.com/jackhusky/doc/tree/master/redis/images/redis事务.png)

### 5.2、事务基本操作

**事务的边界**

![](https://github.com/jackhusky/doc/tree/master/redis/images/事务的边界.png)

- 开启事务

  ```shell
  127.0.0.1:6379> MULTI
  OK
  ```

- 作用

  设定事务的开启位置，此指令执行后，后续的所有指令均加入到事务中

- 执行事务

  ```shell
  127.0.0.1:6379> exec
  ```

- 作用

  设定事务的结束位置，同时执行事务。与`multi`成对出现，成对使用

**注意：加入事务的命令暂时进入到任务队列中，并没有立即执行，只有执行`exec`命令才开始执行**

**事务定义过程中发现除了问题，怎么办？**

- 取消事务

  ```shell
  127.0.0.1:6379> DISCARD
  ```

- 作用

  终止当前事务的定义，发生`multi`之后，`exec`之前

### 5.3、事务的工作流程

![](https://github.com/jackhusky/doc/tree/master/redis/images/事务的工作流程.png)

### 5.4、事务的注意事项

**定义事务的过程中，命令格式输入错误怎么办？**

- 语法错误

  指命令书写格式有误

- 处理结果

  如果定义的事务中所包含的命令存在语法错误，整体事务中所有命令都不会执行。包括语法正确的命令

  ```shell
  127.0.0.1:6379> MULTI
  OK
  127.0.0.1:6379> set age 11
  QUEUED
  127.0.0.1:6379> aaa bb cc
  (error) ERR unknown command 'aaa'
  127.0.0.1:6379> EXEC
  (error) EXECABORT Transaction discarded because of previous errors.
  ```

**定义事务的过程中，命令执行出现错误怎么办？**

- 运行错误

  指命令格式正确，但是无法正确的执行。例如对`list`进行`incr`操作

- 处理结果

  能够正确运行的命令会执行，运行错误的命令不会执行

  ```shell
  127.0.0.1:6379> MULTI
  OK
  127.0.0.1:6379> set name jack
  QUEUED
  127.0.0.1:6379> get name
  QUEUED
  127.0.0.1:6379> lpush name a b c
  QUEUED
  127.0.0.1:6379> get name
  QUEUED
  127.0.0.1:6379> exec
  1) OK
  2) "jack"
  3) (error) WRONGTYPE Operation against a key holding the wrong kind of value
  4) "jack"
  ```

  **注意：已经执行完毕的命令对应的数据不会自动回滚，需要程序员自己在代码中实现回滚**

**手动进行事务回滚**

- 记录操作过程中被影响的数据之前的状态
  - 单数据：string
  - 多数据：hash、list、set、zset
- 设置指令恢复所有的被修改的项
  - 单数据：直接set（注意周边属性，例如时效）
  - 多数据：修改对应值或整体克隆恢复

### 5.4、锁

#### 基于特定条件的事务执行——锁

业务场景

天猫双11热卖过程中，对已经售罄的货物追加补货，4个业务员都有权限进行补货。补货的操作可能是一系列的操作，牵扯到多个连续操作，如何保障不会重复操作？

业务分析：

- 多个客户端有可能同时操作同一组数据，并且该数据一旦被操作后，将不适用于继续执行
- 在操作之前锁定要操作的数据，一旦发生变化，终止当前操作

解决方案

- 对key添加监视锁，在执行`exec`前如果`key`放生了变化，终止事务执行

  ```shell
  127.0.0.1:6379> WATCH name age
  OK
  ```

- 取消对所有`key`的监视

  ```shell
  127.0.0.1:6379> UNWATCH
  ```

  ```shell
  127.0.0.1:6379> WATCH name age
  OK
  127.0.0.1:6379> MULTI
  OK
  127.0.0.1:6379> set aa cc #在exec前另一客户端修改了name
  QUEUED
  127.0.0.1:6379> get aa
  QUEUED
  127.0.0.1:6379> EXEC
  (nil)
  127.0.0.1:6379> WATCH name
  OK
  127.0.0.1:6379> UNWATCH
  OK
  127.0.0.1:6379> MULTI
  OK
  127.0.0.1:6379> set aa cc #在exec前另一客户端修改了name
  QUEUED
  127.0.0.1:6379> get aa
  QUEUED
  127.0.0.1:6379> EXEC
  1) OK
  2) "cc"
  ```

  **redis应用于基于状态控制的批量任务执行**

业务场景

天猫双11热卖过程中，对已经售罄的货物追加不会，且补货完成。客户购买热情高涨，3秒内将所有商品购买完毕，本次补货已经将库存全部清空，如何避免最后一件商品不被多人同时购买？【超卖问题】

业务分析

- 使用`watch`监控一个key有没有改变已经不能解决问题，此处要监控的是具体数据
- 虽然`redis`是单线程的，但是多个客户端对同一数据同时进行操作时，如何避免不被同时修改？

#### 基于特定条件的事务执行——分布式锁

结局方案

- 使用`setnx`设置一个公共锁

  ```shell
  setnx lock-key value
  ```

  利用`setnx`命令的返回值特定，有值则返回设置失败，无值则返回设置成功

  - 对于返回设置成功的，拥有控制权，进行下一步的具体业务操作
  - 对于返回设置失败的，不具有控制权，排队或等待操作完毕通过`del`操作释放锁

  ```shell
  127.0.0.1:6379> set num 10
  OK
  127.0.0.1:6379> SETNX lock-num 1
  (integer) 1
  127.0.0.1:6379> INCRBY num -1
  (integer) 9
  127.0.0.1:6379> DEL lock-num
  (integer) 1
  ```

**注意：上述解决方案是一种设计概念，依赖规范保障，具有风险性**

**redis应用于分布式锁对应的场景控制**

业务场景

依赖分布式锁的机制，某个用户操作时对应客户端宕机，且此时已经获取到锁，如何解决？

业务分析

- 优于锁操作由用户控制加速解锁，必定会存在加锁后未解锁的风险
- 需要解锁操作不能仅依赖用户控制，系统级别要给出对应的保底处理方案

解决方案

- 使用`expire`为锁`key`添加时间限定，到时不释放锁，放弃锁

  ```shell
  expire lock-key second
  pexpire lock-key milliseconds
  ```

  ```shell
  127.0.0.1:6379> set name 123
  OK
  127.0.0.1:6379> SETNX lock-name 1
  (integer) 1
  127.0.0.1:6379> EXPIRE lock-name 20
  (integer) 1
  127.0.0.1:6379> get name
  "123"
  127.0.0.1:6379> del lock-name
  (integer) 1
  127.0.0.1:6379> setnx lock-name 1
  (integer) 1
  127.0.0.1:6379> EXPIRE lock-name 10
  (integer) 1
  ```

  由于操作通常都是微秒或者毫秒级，因此该锁定时间不宜设置过大。具体时间需要业务测试后确认。

  - 例如：持有锁的操作最长执行时间127ms，最短执行时间7ms。
  - 测试百万次最长执行时间对应命令的最大耗时，测试百万次网络延迟平均耗时
  - 锁时间设定推荐：最大耗时*120%+平均网络延迟*110%
  - 如果业务最大耗时<<网络平均延迟，通常为2个数量级，取其中单个耗时较长即可

## 6、redis 的删除策略

**已经过期的数据**真的删除了吗？

数据删除策略

- 定时删除
- 惰性删除
- 定期删除

**时效性数据的存储结构**

![](https://github.com/jackhusky/doc/tree/master/redis/images/时效性数据的数据结构.png)

**数据删除策略的目标**

在内存占用与`CPU`占用之间寻找一种平衡，顾此失彼都会造成整体`redis`性能的下降，甚至引发服务器宕机或内存泄漏

### 6.1、定时删除

- 创建一个定时器，当`key`设置有过期时间，且过期时间到达时，由定时器任务立即执行对键的删除操作
- 优点：节约内存，到时就删除，快速释放掉不必要的内存占用
- 缺点：`CPU`压力很大，无论`CPU`此时负载量多高，均占用`CPU`，会影响`redis`服务器响应时间和指令吞吐量
- 总结：用处理器性能换取存储空间（时间换空间）

![](https://github.com/jackhusky/doc/tree/master/redis/images/定时删除策略1.png)![](https://github.com/jackhusky/doc/tree/master/redis/images/定时删除策略2.png)![](https://github.com/jackhusky/doc/tree/master/redis/images/定时删除策略3.png)

### 6.2、惰性删除

- 数据达到过期时间，不做处理。等下次访问该数据时
  - 如果未过期，返回数据
  - 发现已过期，删除，返回不存在
- 优点：节约`CPU`性能，发现必须删除的时候才删除
- 缺点：内存压力很大，出现长期占用内存的数据
- 总结：用存储空间换取处理器性能（空间换时间）

![](https://github.com/jackhusky/doc/tree/master/redis/images/惰性删除1.png)![](https://github.com/jackhusky/doc/tree/master/redis/images/惰性删除2.png)

### 6.3、定期删除

两种方案都太极端，有没有折中方案？

![](https://github.com/jackhusky/doc/tree/master/redis/images/定期删除策略.png)

- 周期轮训`redis`库中的时效性数据，采用随机抽取的策略，利用过期数据占比的方式控制删除频度

- 特定1：`CPU`性能占用设置有峰值，检测频度可自定义设置
- 特点2：内存压力不是很大，长期占用内存的冷数据会被持续清理
- 总结：周期性抽查存储空间（随机抽查，重点抽查）

### 6.4、逐出算法

当新数据进入`redis`时，如果内存不足怎么办？

![](https://github.com/jackhusky/doc/tree/master/redis/images/逐出算法.png)

#### 影响数据逐出的相关配置

- 最大可使用内存

  ```conf
  maxmemory
  ```

  占用物理内存的比例，默认值为0，表示不限制。生产环境中根据需求设定，通常设置在50%以上

- 每次选取待删除数据的个数

  ```conf
  maxmemory-samples
  ```

  选取数据时并不会全库扫描，导致严重的性能消耗，降低读写性能。因此采用随机获取数据的方式作为待检测删除数据

- 删除策略

  ```conf
  maxmemory-policy
  ```

  达到最大内存后，对被挑选出来的数据进行删除的策略



- 检测易失数据（可能会过期的数据集server.db[i].expires）

  - volatile-lru：挑选最近最少使用的数据淘汰
  - volatile-lfu：挑选最近使用次数最少的数据淘汰
  - volatile-ttl：挑选将要过期的数据淘汰
  - volatile-random：任意选择数据淘汰

  ![](https://github.com/jackhusky/doc/tree/master/redis/images/LRU和LFU的意思.png)

- 检测全库数据（所有数据集server.db[i].dict）

  - allkeys-lru：挑选最近最少使用的数据淘汰
  - allkeys-lfu：挑选最近使用次数最少的数据淘汰
  - allkeys-random：任意选择数据淘汰

- 放弃数据驱逐

  - no-enviction（驱逐）：禁止驱逐数据（redis4.0中默认策略），会引发错误OOM

  ```conf
  maxmemory-policy volatile-lru
  ```

**数据逐出策略配置依据**

- 使用`INFO`命令输出监控信息，查询缓存hit和miss的次数，根据业务需求调优redis配置

  ```shell
  keyspace_hits:9
  keyspace_misses:0
  ```

## 7、redis 的核心配置

### 7.1、服务器基础配置

#### 服务器端设定

- 设置服务器以守护进程的方式运行

  ```conf
  daemonize yes|no
  ```

- 绑定主机地址

  ```conf
  bind 127.0.0.1
  ```

- 设置服务器端口号

  ```conf
  port 6379
  ```

- 设置数据库数量

  ```conf
  databases 16
  ```

#### 日志配置

- 设置服务器以指定日志记录级别,默认为`verbose`

  ```conf
  loglevel debug|verbose|notice|warning
  ```

- 日志记录文件名

  ```conf
   logfile 端口号.log
  ```

  注意：日志级别开发期设置为`verbose`即可，生产环境中配置为`notice`，简化日志输出量，降低写日志IO的频度

#### 客户端配置

- 设置同一时间最大客户端连接数，默认无限制。当客户端连接到达上限，redis会关闭新的连接

  ```conf
  maxclients 0
  ```

- 客户端闲置等待最大时长，达到最大值后关闭连接。如需关闭该功能，设置为0

  ```conf
  timeout 300
  ```

#### 多服务器快捷配置

- 导入并加载指定配置文件信息，用于快速创建`redis`公共配置较多的redis实例配置文件，便于维护

  ```conf
  include /path/server-端口号.conf
  ```

  

## 8、redis 的高级数据类型

### 8.1、Bitmaps

存储需求

![](https://github.com/jackhusky/doc/tree/master/redis/images/引出Bitmaps的存储需求.png)

#### 基本操作

```shell
127.0.0.1:6379> SETBIT bits 0 1
(integer) 0
127.0.0.1:6379> GETBIT bits 0
(integer) 1
127.0.0.1:6379> GETBIT bits 10
(integer) 0
127.0.0.1:6379> SETBIT bits 1000000000 1
(integer) 0
(0.63s)
```

#### 扩展操作

业务场景

电影网站

- 统计每天某一部电影是否被点播
- 统计每天有多少部电影被点播
- 统计每周、每月、每年多少部电影被点播
- 统计年度哪部电影没有被点播

业务分析

![](https://github.com/jackhusky/doc/tree/master/redis/images/Bitmaps扩展操作业务场景.png)

- 对指定key按位进行交、并、非、异或操作，并将结果保存到destKey中

  ```shell
  bitop op destKey key1 [key2]
  ```

  - and：交
  - or：并
  - not：非
  - xor：异或

- 统计指定`key`中1的数量

  ```shell
  bitcount key [start end]
  ```

  ```shell
  127.0.0.1:6379> SETBIT 20880808 0 1
  (integer) 0
  127.0.0.1:6379> SETBIT 20880808 4 1
  (integer) 0
  127.0.0.1:6379> SETBIT 20880808 8 1
  (integer) 0
  127.0.0.1:6379> SETBIT 20880809 0 1
  (integer) 0
  127.0.0.1:6379> SETBIT 20880809 5 1
  (integer) 0
  127.0.0.1:6379> SETBIT 20880809 8 1
  (integer) 0
  127.0.0.1:6379> BITCOUNT 20880808
  (integer) 3
  127.0.0.1:6379> BITCOUNT 20880809
  (integer) 3
  127.0.0.1:6379> SETBIT 20880808 6 1
  (integer) 0
  127.0.0.1:6379> BITCOUNT 20880808
  (integer) 4
  127.0.0.1:6379> BITOP or 08-09 20880808 20880809
  (integer) 2
  127.0.0.1:6379> BITCOUNT 08-09
  (integer) 5
  ```

  **redis应用于信息状态统计**

### 8.2、HyperLoglog

**基数**

- 基数是数据集去重后元素个数
- `HyperLoglog`是用来做基数统计的，运用了`LogLog`算法

![](https://github.com/jackhusky/doc/tree/master/redis/images/基数.png)

#### 基本操作

```shell
127.0.0.1:6379> PFADD hll 001
(integer) 0
127.0.0.1:6379> PFADD hll 002
(integer) 1
127.0.0.1:6379> PFADD hll 002
(integer) 0
127.0.0.1:6379> PFCOUNT hll
(integer) 2
127.0.0.1:6379> PFADD hll2 003
(integer) 1
127.0.0.1:6379> PFADD hll2 003
(integer) 0
127.0.0.1:6379> PFADD hll2 004
(integer) 1
127.0.0.1:6379> PFADD hll2 002
(integer) 1
127.0.0.1:6379> PFADD hll2 002
(integer) 0
127.0.0.1:6379> PFMERGE hll3 hll hll2
OK
127.0.0.1:6379> PFCOUNT hll3
(integer) 4
```

**redis应用于独立信息统计**

**相关说明**

- 用于进行基数统计，不是集合，不保存数据，只记录数量而不是具体数据
- 核心是基数估算算法，最终数值存在一定误差
- 误差范围：基数估计的结果是一个导游0.81%标准村务的近似值
- 耗空间极小，每个`HyperLoglog` `key`占用了12K的内存用于标记基数
- `pfadd`命令不是一次性分配12K内存使用，会随着基数的值增加内存逐渐增大
- `pfmerge`命令合并后占用的存储空间为12K，无论合并之前数据量是多少

### 8.3、GEO

地理位置：两点的关系，要知道两点坐标

#### 基本操作

```shell
127.0.0.1:6379> GEOADD geos 1 1 a
(integer) 1
127.0.0.1:6379> GEOADD geos 2 2 b
(integer) 1
127.0.0.1:6379> GEOPOS geos a
1) 1) "0.99999994039535522"
   2) "0.99999945914297683"
127.0.0.1:6379> GEODIST geos a b
"157270.0561"
127.0.0.1:6379> GEODIST geos a b m
"157270.0561"
127.0.0.1:6379> GEODIST geos a b km
"157.2701"
```

- 根据坐标求范围内的数据

  ```shell
  GEORADIUS key longitude latitude radius m|km|ft|mi [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count] [ASC|DESC] [STORE key] [STOREDIST key]
  ```

- 根据点求范围内数据

  ```shell
  GEORADIUSBYMEMBER key member radius m|km|ft|mi [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count] [ASC|DESC] [STORE key] [STOREDIST key]
  ```

- 获取指定点对应的坐标hash值

  ```shell
  GEOHASH key member [member ...]
  ```

  **redis应用于地理位置计算**

  