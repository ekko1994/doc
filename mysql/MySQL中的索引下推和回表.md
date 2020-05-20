# MySQL中的索引下推

索引下推是数据库检索数据过程中为减少回表次数而做的优化。

## 什么是数据库回表

回表是一种数据库检索过程。通常发生在使用二级索引检索非主索引数据的过程中。

![usertest表](https://github.com/jackhusky/doc/blob/master/mysql/images/usertest表.png)

假设有上面一张表（数据库是MYSQL，存储引擎是Innodb），上面的ID字段是主键索引，age是普通索引。

对比下面两条SQL语句：

select id from usertest where age = 10;

select name from usertest where age = 10;

第一条SQL语句不会产生回表：普通索引存储的值是主键的值。也就是说age索引里面存储的结构是下面的情况

![age索引](https://github.com/jackhusky/doc/blob/master/mysql/images/age索引.png)

根据age查询id的时候，索引中的值完全可以覆盖查询结果集字段时，不会产生回表操作。

由此也可以看出第二条SQL语句会产生回表是因为查询的结果集无法通过索引中的值直接获取。需要根据age查询到的id值再回到主键索引里面再次查询，这个过程叫做回表。

## 索引下推

还是上面的usertest表，只是索引变了，ID字段是仍主键索引，但是我们加上一个复合索引name_age(name,age)。执行下面一条SQL语句：

select * from usertest where name like 'a%' and age = 10;

在Mysql5.6之前的执行流程是这样的：

1.根据最左前缀原则，执行name like 'a%'可以快速检索出id的值为1，5。

![sql结果集1](https://github.com/jackhusky/doc/blob/master/mysql/images/sql结果集1.png)

2.然后根据id的值进行回表操作，再次进行过滤age=10的数据。

查询id=1回表1次，id=5回表1次，这个过程总共回表了2次。

可能到这里都会有疑问，为什么不在索引里面直接过滤age=10的数据，因为复合索引里面也存了age的数据，这样明明可以减少回表1次。恭喜啦，Mysql5.6以后就这么做了，这就是索引下推。

这样可以看出，索引下推具体是在复合索引的查询中，针对特定的过滤条件而进行减少回表次数而做的优化