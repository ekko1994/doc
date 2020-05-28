* [一、索引优化分析](#%E4%B8%80%E7%B4%A2%E5%BC%95%E4%BC%98%E5%8C%96%E5%88%86%E6%9E%90)
  * [1、性能下降SQL慢，执行时间长，等待时间长？](#1%E6%80%A7%E8%83%BD%E4%B8%8B%E9%99%8Dsql%E6%85%A2%E6%89%A7%E8%A1%8C%E6%97%B6%E9%97%B4%E9%95%BF%E7%AD%89%E5%BE%85%E6%97%B6%E9%97%B4%E9%95%BF)
  * [2、常见通用的join查询](#2%E5%B8%B8%E8%A7%81%E9%80%9A%E7%94%A8%E7%9A%84join%E6%9F%A5%E8%AF%A2)
    * [2\.1、SQL执行顺序](#21sql%E6%89%A7%E8%A1%8C%E9%A1%BA%E5%BA%8F)
    * [2\.2、七大Join图](#22%E4%B8%83%E5%A4%A7join%E5%9B%BE)
  * [3、索引简介](#3%E7%B4%A2%E5%BC%95%E7%AE%80%E4%BB%8B)
    * [3\.1、优势](#31%E4%BC%98%E5%8A%BF)
    * [3\.2、劣势](#32%E5%8A%A3%E5%8A%BF)
    * [3\.3、mysql索引分类](#33mysql%E7%B4%A2%E5%BC%95%E5%88%86%E7%B1%BB)
    * [3\.4、基本语法](#34%E5%9F%BA%E6%9C%AC%E8%AF%AD%E6%B3%95)
    * [3\.5、mysql索引结构](#35mysql%E7%B4%A2%E5%BC%95%E7%BB%93%E6%9E%84)
    * [3\.6、哪些情况需要创建索引](#36%E5%93%AA%E4%BA%9B%E6%83%85%E5%86%B5%E9%9C%80%E8%A6%81%E5%88%9B%E5%BB%BA%E7%B4%A2%E5%BC%95)
    * [3\.7、哪些情况不要创建索引](#37%E5%93%AA%E4%BA%9B%E6%83%85%E5%86%B5%E4%B8%8D%E8%A6%81%E5%88%9B%E5%BB%BA%E7%B4%A2%E5%BC%95)
  * [4、性能分析](#4%E6%80%A7%E8%83%BD%E5%88%86%E6%9E%90)
    * [4\.1、MySQL常见瓶颈](#41mysql%E5%B8%B8%E8%A7%81%E7%93%B6%E9%A2%88)
    * [4\.2、Explain](#42explain)
  * [5、索引优化](#5%E7%B4%A2%E5%BC%95%E4%BC%98%E5%8C%96)
    * [5\.1、建表sql](#51%E5%BB%BA%E8%A1%A8sql)
    * [5\.2、案例](#52%E6%A1%88%E4%BE%8B)
      * [5\.2\.1、全值匹配我最爱](#521%E5%85%A8%E5%80%BC%E5%8C%B9%E9%85%8D%E6%88%91%E6%9C%80%E7%88%B1)
      * [5\.2\.2、最佳左前缀法则](#522%E6%9C%80%E4%BD%B3%E5%B7%A6%E5%89%8D%E7%BC%80%E6%B3%95%E5%88%99)
      * [5\.2\.3、不在索引列上做任何操作（计算、函数、（自动or手动）类型转换），会导致索引失效而转向全表扫描](#523%E4%B8%8D%E5%9C%A8%E7%B4%A2%E5%BC%95%E5%88%97%E4%B8%8A%E5%81%9A%E4%BB%BB%E4%BD%95%E6%93%8D%E4%BD%9C%E8%AE%A1%E7%AE%97%E5%87%BD%E6%95%B0%E8%87%AA%E5%8A%A8or%E6%89%8B%E5%8A%A8%E7%B1%BB%E5%9E%8B%E8%BD%AC%E6%8D%A2%E4%BC%9A%E5%AF%BC%E8%87%B4%E7%B4%A2%E5%BC%95%E5%A4%B1%E6%95%88%E8%80%8C%E8%BD%AC%E5%90%91%E5%85%A8%E8%A1%A8%E6%89%AB%E6%8F%8F)
      * [5\.2\.4、存储引擎不能使用索引中范围条件右边的列](#524%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E4%B8%8D%E8%83%BD%E4%BD%BF%E7%94%A8%E7%B4%A2%E5%BC%95%E4%B8%AD%E8%8C%83%E5%9B%B4%E6%9D%A1%E4%BB%B6%E5%8F%B3%E8%BE%B9%E7%9A%84%E5%88%97)
      * [5\.2\.5、尽量使用覆盖索引（只访问索引的查询（索引列和查询列一致）），减少select\*](#525%E5%B0%BD%E9%87%8F%E4%BD%BF%E7%94%A8%E8%A6%86%E7%9B%96%E7%B4%A2%E5%BC%95%E5%8F%AA%E8%AE%BF%E9%97%AE%E7%B4%A2%E5%BC%95%E7%9A%84%E6%9F%A5%E8%AF%A2%E7%B4%A2%E5%BC%95%E5%88%97%E5%92%8C%E6%9F%A5%E8%AF%A2%E5%88%97%E4%B8%80%E8%87%B4%E5%87%8F%E5%B0%91select)
      * [5\.2\.6、mysql在使用不等于（！=或者&lt;&gt;）的时候无法使用索引会导致全表扫描](#526mysql%E5%9C%A8%E4%BD%BF%E7%94%A8%E4%B8%8D%E7%AD%89%E4%BA%8E%E6%88%96%E8%80%85%E7%9A%84%E6%97%B6%E5%80%99%E6%97%A0%E6%B3%95%E4%BD%BF%E7%94%A8%E7%B4%A2%E5%BC%95%E4%BC%9A%E5%AF%BC%E8%87%B4%E5%85%A8%E8%A1%A8%E6%89%AB%E6%8F%8F)
      * [5\.2\.7、is null,is not null 也无法使用索引](#527is-nullis-not-null-%E4%B9%9F%E6%97%A0%E6%B3%95%E4%BD%BF%E7%94%A8%E7%B4%A2%E5%BC%95)
      * [5\.2\.8、like以通配符开头（'%abc\.\.\.'）mysql索引失效会变成全表扫描操作](#528like%E4%BB%A5%E9%80%9A%E9%85%8D%E7%AC%A6%E5%BC%80%E5%A4%B4abcmysql%E7%B4%A2%E5%BC%95%E5%A4%B1%E6%95%88%E4%BC%9A%E5%8F%98%E6%88%90%E5%85%A8%E8%A1%A8%E6%89%AB%E6%8F%8F%E6%93%8D%E4%BD%9C)
      * [5\.2\.9、字符串不加单引号索引失效（<strong>重罪</strong>）](#529%E5%AD%97%E7%AC%A6%E4%B8%B2%E4%B8%8D%E5%8A%A0%E5%8D%95%E5%BC%95%E5%8F%B7%E7%B4%A2%E5%BC%95%E5%A4%B1%E6%95%88%E9%87%8D%E7%BD%AA)
      * [5\.2\.10、少用or,用它连接时会索引失效](#5210%E5%B0%91%E7%94%A8or%E7%94%A8%E5%AE%83%E8%BF%9E%E6%8E%A5%E6%97%B6%E4%BC%9A%E7%B4%A2%E5%BC%95%E5%A4%B1%E6%95%88)
    * [5\.3、面试题讲解](#53%E9%9D%A2%E8%AF%95%E9%A2%98%E8%AE%B2%E8%A7%A3)
    * [5\.4、一般性建议](#54%E4%B8%80%E8%88%AC%E6%80%A7%E5%BB%BA%E8%AE%AE)
    * [5\.5、你的sql优化？](#55%E4%BD%A0%E7%9A%84sql%E4%BC%98%E5%8C%96)
* [二、查询截取分析](#%E4%BA%8C%E6%9F%A5%E8%AF%A2%E6%88%AA%E5%8F%96%E5%88%86%E6%9E%90)
  * [1、查询优化](#1%E6%9F%A5%E8%AF%A2%E4%BC%98%E5%8C%96)
    * [1\.1 永远小表驱动大表，类似嵌套循环Nested Loop](#11-%E6%B0%B8%E8%BF%9C%E5%B0%8F%E8%A1%A8%E9%A9%B1%E5%8A%A8%E5%A4%A7%E8%A1%A8%E7%B1%BB%E4%BC%BC%E5%B5%8C%E5%A5%97%E5%BE%AA%E7%8E%AFnested-loop)
  * [2、order by关键字优化](#2order-by%E5%85%B3%E9%94%AE%E5%AD%97%E4%BC%98%E5%8C%96)
    * [2\.1、ORDER BY子句，尽量使用Index方式排序，避免使用FileSort方式排序](#21order-by%E5%AD%90%E5%8F%A5%E5%B0%BD%E9%87%8F%E4%BD%BF%E7%94%A8index%E6%96%B9%E5%BC%8F%E6%8E%92%E5%BA%8F%E9%81%BF%E5%85%8D%E4%BD%BF%E7%94%A8filesort%E6%96%B9%E5%BC%8F%E6%8E%92%E5%BA%8F)
    * [2\.2、如果不在索引列上，filesort有两种算法：mysql就要启动双路排序和单路排序](#22%E5%A6%82%E6%9E%9C%E4%B8%8D%E5%9C%A8%E7%B4%A2%E5%BC%95%E5%88%97%E4%B8%8Afilesort%E6%9C%89%E4%B8%A4%E7%A7%8D%E7%AE%97%E6%B3%95mysql%E5%B0%B1%E8%A6%81%E5%90%AF%E5%8A%A8%E5%8F%8C%E8%B7%AF%E6%8E%92%E5%BA%8F%E5%92%8C%E5%8D%95%E8%B7%AF%E6%8E%92%E5%BA%8F)
      * [2\.2\.1、双路排序](#221%E5%8F%8C%E8%B7%AF%E6%8E%92%E5%BA%8F)
      * [2\.2\.2、单路排序](#222%E5%8D%95%E8%B7%AF%E6%8E%92%E5%BA%8F)
      * [2\.2\.3、结论及引申出的问题](#223%E7%BB%93%E8%AE%BA%E5%8F%8A%E5%BC%95%E7%94%B3%E5%87%BA%E7%9A%84%E9%97%AE%E9%A2%98)
      * [2\.2\.4、小总结](#224%E5%B0%8F%E6%80%BB%E7%BB%93)
  * [3、GROUP BY关键字优化](#3group-by%E5%85%B3%E9%94%AE%E5%AD%97%E4%BC%98%E5%8C%96)
  * [4、慢查询日志](#4%E6%85%A2%E6%9F%A5%E8%AF%A2%E6%97%A5%E5%BF%97)
    * [4\.1、怎么用？](#41%E6%80%8E%E4%B9%88%E7%94%A8)
      * [4\.1\.1、查看是否开启？默认是关闭的。](#411%E6%9F%A5%E7%9C%8B%E6%98%AF%E5%90%A6%E5%BC%80%E5%90%AF%E9%BB%98%E8%AE%A4%E6%98%AF%E5%85%B3%E9%97%AD%E7%9A%84)
      * [4\.1\.2、那么开启慢查询日志后，什么样的SQL参会记录到慢查询里面？](#412%E9%82%A3%E4%B9%88%E5%BC%80%E5%90%AF%E6%85%A2%E6%9F%A5%E8%AF%A2%E6%97%A5%E5%BF%97%E5%90%8E%E4%BB%80%E4%B9%88%E6%A0%B7%E7%9A%84sql%E5%8F%82%E4%BC%9A%E8%AE%B0%E5%BD%95%E5%88%B0%E6%85%A2%E6%9F%A5%E8%AF%A2%E9%87%8C%E9%9D%A2)
    * [4\.2、日志分析工具mysqldumpshow](#42%E6%97%A5%E5%BF%97%E5%88%86%E6%9E%90%E5%B7%A5%E5%85%B7mysqldumpshow)
  * [5、批量数据脚本](#5%E6%89%B9%E9%87%8F%E6%95%B0%E6%8D%AE%E8%84%9A%E6%9C%AC)
    * [5\.1、建表sql](#51%E5%BB%BA%E8%A1%A8sql-1)
    * [5\.2、创建函数保证每条数据都不同](#52%E5%88%9B%E5%BB%BA%E5%87%BD%E6%95%B0%E4%BF%9D%E8%AF%81%E6%AF%8F%E6%9D%A1%E6%95%B0%E6%8D%AE%E9%83%BD%E4%B8%8D%E5%90%8C)
    * [5\.3、创建存储过程](#53%E5%88%9B%E5%BB%BA%E5%AD%98%E5%82%A8%E8%BF%87%E7%A8%8B)
    * [5\.4、调用存储过程](#54%E8%B0%83%E7%94%A8%E5%AD%98%E5%82%A8%E8%BF%87%E7%A8%8B)
  * [6、Show profiles](#6show-profiles)
    * [6\.1、诊断SQL](#61%E8%AF%8A%E6%96%ADsql)
    * [6\.2、日常开发需要注意的结论](#62%E6%97%A5%E5%B8%B8%E5%BC%80%E5%8F%91%E9%9C%80%E8%A6%81%E6%B3%A8%E6%84%8F%E7%9A%84%E7%BB%93%E8%AE%BA)
  * [7、全局查询日志](#7%E5%85%A8%E5%B1%80%E6%9F%A5%E8%AF%A2%E6%97%A5%E5%BF%97)
* [三、Mysql锁机制](#%E4%B8%89mysql%E9%94%81%E6%9C%BA%E5%88%B6)
  * [1、表锁（偏读）](#1%E8%A1%A8%E9%94%81%E5%81%8F%E8%AF%BB)
    * [1\.1、建表sql](#11%E5%BB%BA%E8%A1%A8sql)
    * [1\.2、加读锁](#12%E5%8A%A0%E8%AF%BB%E9%94%81)
    * [1\.3、加写锁](#13%E5%8A%A0%E5%86%99%E9%94%81)
    * [1\.4、案例结论](#14%E6%A1%88%E4%BE%8B%E7%BB%93%E8%AE%BA)
    * [1\.5、表锁分析](#15%E8%A1%A8%E9%94%81%E5%88%86%E6%9E%90)
  * [2、行锁（偏写）](#2%E8%A1%8C%E9%94%81%E5%81%8F%E5%86%99)
    * [2\.1、事务](#21%E4%BA%8B%E5%8A%A1)
    * [2\.2、建表sql](#22%E5%BB%BA%E8%A1%A8sql)
    * [2\.3、行锁演示](#23%E8%A1%8C%E9%94%81%E6%BC%94%E7%A4%BA)
    * [2\.4、间隙锁危害](#24%E9%97%B4%E9%9A%99%E9%94%81%E5%8D%B1%E5%AE%B3)
    * [2\.5 如何锁定一行](#25-%E5%A6%82%E4%BD%95%E9%94%81%E5%AE%9A%E4%B8%80%E8%A1%8C)
    * [2\.6、案例结论](#26%E6%A1%88%E4%BE%8B%E7%BB%93%E8%AE%BA)
    * [2\.7、行锁分析](#27%E8%A1%8C%E9%94%81%E5%88%86%E6%9E%90)
* [四、主从复制](#%E5%9B%9B%E4%B8%BB%E4%BB%8E%E5%A4%8D%E5%88%B6)
  * [1、复制的基本原理](#1%E5%A4%8D%E5%88%B6%E7%9A%84%E5%9F%BA%E6%9C%AC%E5%8E%9F%E7%90%86)
  * [2、复制的基本原则](#2%E5%A4%8D%E5%88%B6%E7%9A%84%E5%9F%BA%E6%9C%AC%E5%8E%9F%E5%88%99)
  * [3、复制的问题](#3%E5%A4%8D%E5%88%B6%E7%9A%84%E9%97%AE%E9%A2%98)
  * [4、一主一从常见配置](#4%E4%B8%80%E4%B8%BB%E4%B8%80%E4%BB%8E%E5%B8%B8%E8%A7%81%E9%85%8D%E7%BD%AE)
    * [4\.1、修改主机my\.cnf](#41%E4%BF%AE%E6%94%B9%E4%B8%BB%E6%9C%BAmycnf)
    * [4\.2、从机修改my\.cnf](#42%E4%BB%8E%E6%9C%BA%E4%BF%AE%E6%94%B9mycnf)
    * [4\.3、重启服务，关闭防火墙](#43%E9%87%8D%E5%90%AF%E6%9C%8D%E5%8A%A1%E5%85%B3%E9%97%AD%E9%98%B2%E7%81%AB%E5%A2%99)
    * [4\.4、在主机上建立账户并授权slave](#44%E5%9C%A8%E4%B8%BB%E6%9C%BA%E4%B8%8A%E5%BB%BA%E7%AB%8B%E8%B4%A6%E6%88%B7%E5%B9%B6%E6%8E%88%E6%9D%83slave)
    * [4\.5、在从机上配置需要复制的主机](#45%E5%9C%A8%E4%BB%8E%E6%9C%BA%E4%B8%8A%E9%85%8D%E7%BD%AE%E9%9C%80%E8%A6%81%E5%A4%8D%E5%88%B6%E7%9A%84%E4%B8%BB%E6%9C%BA)
    * [4\.6、如何停止从服务复制功能](#46%E5%A6%82%E4%BD%95%E5%81%9C%E6%AD%A2%E4%BB%8E%E6%9C%8D%E5%8A%A1%E5%A4%8D%E5%88%B6%E5%8A%9F%E8%83%BD)

# 一、索引优化分析

## 1、性能下降SQL慢，执行时间长，等待时间长？

- 查询语句写的烂
- 索引失效
- 关联查询太多join(设计缺陷或不得已的需求)
- 服务器调优及各个参数设置(缓冲\线程数等)

## 2、常见通用的join查询

### 2.1、SQL执行顺序

手写mysql：

```mysql
select 
  distinct  <select_list>
      from   <left_table> 
          <join_type> join <right_table> 
              on <join_condition>
                 where <where_condition>
                     group by <groupby_list>
                        having  <having_condition>
                            order by <orderby_conditoin>
                                  limit <limit number>
```

机读mysql：

```mysql
1. FROM  <left_table>
2. 	ON <join_codition>
3. 		<join_type> JOIN <right_table>
4.			WHERE <where_condition>
5. 				GROUP BY <group_by_list>
6. 					HAVING <HAVING_condition>
7. 						SELECT 
8. 							DISTINCT <select_list>
9. 								ORDER BY <order_by_condition>
10. 								LIMIT <limit_number>
```

### 2.2、七大Join图

![sql-join](https://github.com/jackhusky/doc/blob/master/mysql/images/sql-join.png)

## 3、索引简介

MySQL官方对索引的定义为：索引(Index)是帮助MySQL高效获取数据的**数据结构**。
你可以简单理解为"排好序的快速查找数据结构"。

![mysql_索引简介](https://github.com/jackhusky/doc/blob/master/mysql/images/mysql_索引简介.bmp)

我们平时所说的索引，如果没有特别指明，都是指B树(多路搜索树，并不一定是二叉树)结构组织的索引。其中聚集索引，次要索引，覆盖索引，复合索引，前缀索引，唯一索引默认都是使用B+树索引，统称索引。当然,除了B+树这种类型的索引之外，还有哈希索引(hash index)等。

### 3.1、优势

- 类似大学图书馆建书目索引，提高数据检索效率，降低数据库的IO成本

- 通过索引列对数据进行排序，降低数据排序成本，降低了CPU的消耗

### 3.2、劣势

- 实际上索引也是一张表，该表保存了主键和索引字段，并指向实体表的记录,所以索引列也是要占用空间的。

- 虽然索引大大提高了查询速度，同时却会降低更新表的速度,如果对表INSERT,UPDATE和DELETE。因为更新表时，MySQL不仅要不存数据，还要保存一下索引文件每次更新添加了索引列的字段，都会调整因为更新所带来的键值变化后的索引信息。

- 索引只是提高效率的一个因素，如果你的MySQL有大数据量的表，就需要花时间研究建立优秀的索引，或优化查询语句。

### 3.3、mysql索引分类

- 单值索引：即一个索引只包含单个列，一个表可以有多个单列索引（建议一张表索引不要超过5个
  优先考虑复合索引）

- 唯一索引：索引列的值必须唯一，但允许有空值
- 复合索引：即一个索引包含多个列

### 3.4、基本语法

**创建**：CREATE [UNIQUE] INDEX  indexName ON mytable(columnname(length));（如果是CHAR,VARCHAR类型，length可以小于字段实际长度；如果是BLOB和TEXT类型，必须指定length。）

**修改**：ALTER mytable ADD [UNIQUE]  INDEX [indexName] ON(columnname(length));

**删除**：DROP INDEX [indexName] ON mytable;

**查看**：SHOW INDEX FROM table_name\G

**使用ALTER 命令添加和删除索引**：

- **ALTER TABLE tbl_name ADD PRIMARY KEY (column_list)**:该语句添加一个主键，这意味着索引值必须是唯一的，且不能为NULL。
- **ALTER TABLE tbl_name ADD UNIQUE index_name (column_list):** 这条语句创建索引的值必须是唯一的（除了NULL外，NULL可能会出现多次）。
- **ALTER TABLE tbl_name ADD INDEX index_name (column_list):** 添加普通索引，索引值可出现多次。
- **ALTER TABLE tbl_name ADD FULLTEXT index_name (column_list):**该语句指定了索引为 FULLTEXT ，用于全文索引。

### 3.5、mysql索引结构

**BTree索引**

![B-Tree](https://github.com/jackhusky/doc/blob/master/mysql/images/B-Tree.jpg)

每个节点占用一个盘块的磁盘空间，一个节点上有两个升序排序的关键字和三个指向子树根节点的指针，指针存储的是子节点所在磁盘块的地址。

以根节点为例，关键字为 17 和 35，P1 指针指向的子树的数据范围为小于 17，P2 指针指向的子树的数据范围为 17~35，P3 指针指向的子树的数据范围为大于 35。

模拟查找关键字 29 的过程：

1. 根据根节点找到磁盘块 1，读入内存。【磁盘 I/O 操作第 1 次】
2. 比较关键字 29 在区间（17,35），找到磁盘块 1 的指针 P2。
3. 根据 P2 指针找到磁盘块 3，读入内存。【磁盘 I/O 操作第 2 次】
4. 比较关键字 29 在区间（26,30），找到磁盘块 3 的指针 P2。
5. 根据 P2 指针找到磁盘块 8，读入内存。【磁盘 I/O 操作第 3 次】
6. 在磁盘块 8 中的关键字列表中找到关键字 29。

MySQL 的 InnoDB 存储引擎在设计时是将根节点常驻内存的，因此力求达到树的深度不超过 3，也就是说 I/O 不需要超过 3 次。

分析上面过程，发现需要 3 次磁盘 I/O 操作，和 3 次内存查找操作。由于内存中的关键字是一个有序表结构，可以利用二分法查找提高效率。

而 3 次磁盘 I/O 操作是影响整个 B-Tree 查找效率的决定因素。

B-Tree 相对于 AVLTree 缩减了节点个数，使每次磁盘 I/O 取到内存的数据都发挥了作用，从而提高了查询效率。

B+Tree 是在 B-Tree 基础上的一种优化，使其更适合实现外存储索引结构，InnoDB 存储引擎就是用 B+Tree 实现其索引结构。

在 B-Tree 中，每个节点中有 key，也有 data，而每一个页的存储空间是有限的，如果 data 数据较大时将会导致每个节点（即一个页）能存储的 key 的数量很小。

当存储的数据量很大时同样会导致 B-Tree 的深度较大，增大查询时的磁盘 I/O 次数，进而影响查询效率。

在 **B+Tree** 中，**所有数据**记录节点都是按照键值大小顺序存**放在同一层的叶子节点上**，而非叶子节点上只存储 key 值信息，这样可以大大**加大每个节点存储的 key 值数量，降低 B+Tree 的高度**。

**B+Tree 在 B-Tree 的基础上有两点变化：**

- **数据是存在叶子节点中的；**

- **数据节点之间是有指针指向的。**

由于 B+Tree 的非叶子节点只存储键值信息，假设每个磁盘块能存储 4 个键值及指针信息，则变成 B+Tree 后其结构如下图所示：

![B+Tree](https://github.com/jackhusky/doc/blob/master/mysql/images/B+Tree.jpg)

通常在B+Tree上有两个头指针，一个指向根节点，另一个指向关键字最小的叶子节点，而且所有叶子节点（即数据节点）之间是一种链式环结构。因此可以对B+Tree进行两种查找运算：一种是对于主键的范围查找和分页查找，另一种是从根节点开始，进行随机查找。

数据库中的B+Tree索引可以分为聚集索引（`clustered index`）和辅助索引（`secondary index`）。上面的B+Tree示例图在数据库中的实现即为聚集索引，聚集索引的B+Tree中的叶子节点存放的是整张表的行记录数据。辅助索引与聚集索引的区别在于辅助索引的叶子节点并不包含行记录的全部数据，而是存储相应行数据的聚集索引键，即主键。当通过辅助索引来查询数据时，`InnoDB`存储引擎会遍历辅助索引找到主键，然后再通过主键在聚集索引中找到完整的行记录数据。

### 3.6、哪些情况需要创建索引

- 主键自动建立唯一索引
- 频繁作为查询的条件的字段应该创建索引
- 查询中与其他表关联的字段，外键关系建立索引
- 频繁更新的字段不适合创建索引（因为每次更新不单单是更新了记录还会更新索引，加重IO负担）
- Where条件里用不到的字段不创建索引
- 单间/组合索引的选择问题，who？（在高并发下倾向创建组合索引）
- 查询中排序的字段，排序字段若通过索引去访问将大大提高排序的速度
- 查询中统计或者分组字段

### 3.7、哪些情况不要创建索引

- 表记录太少

- 经常增删改的表
- 数据重复且分布平均的表字段，因此应该只为经常查询和经常排序的数据列建立索引。
  注意，如果某个数据列包含许多重复的内容，为它建立索引就没有太大的实际效果。

## 4、性能分析

### 4.1、MySQL常见瓶颈

- CPU:CPU在饱和的时候一般发生在数据装入在内存或从磁盘上读取数据时候
- IO:磁盘I/O瓶颈发生在装入数据远大于内存容量时
- 服务器硬件的性能瓶颈：top,free,iostat和vmstat来查看系统的性能状态

### 4.2、Explain

使用EXPLAIN关键字可以模拟优化器执行SQL语句，从而知道MySQL是如何处理你的SQL语句的。分析你的查询语句或是结构的性能瓶颈

```mysql
mysql> explain select * from article;
+----+-------------+---------+------+---------------+------+---------+------+------+-------+
| id | select_type | table   | type | possible_keys | key  | key_len | ref  | rows | Extra |
+----+-------------+---------+------+---------------+------+---------+------+------+-------+
|  1 | SIMPLE      | article | ALL  | NULL          | NULL | NULL    | NULL |    3 |       |
+----+-------------+---------+------+---------------+------+---------+------+------+-------+
1 row in set (0.00 sec)
```

各个字段解释:

- `id`：select查询的序列号，包含一组数字，表示查询中执行select子句或操作表的顺序

  - id相同，执行顺序由上至下
  - id不同，如果是子查询，id的序号会递增，id值越大优先级越高，越先被执行
  - id相同不同，同时存在

- `select_type` ：查询的类型，主要用于区别，普通查询、联合查询、子查询等的复杂查询

  - SIMPLE：简单的select查询，查询中不包含子查询或者UNION
  - PRIMARY：最外层 SELECT
  - SUBQUERY：在SELECT或者WHERE列表中包含了子查询
  - DERIVED：在FROM列表中包含的子查询被标记为DERIVED（衍生），MySQL会递归执行这些子查询，把结果放在临时表里。
  - UNION：若第二个SELECT出现在UNION之后，则被标记为UNION;若UNION包含在FROM子句的子查询中，外层SELECT将被标记为：DERIVED
  - UNION RESULT：从UNION表获取结果的SELECT

- table：显示这一行的数据是关于哪张表的

- type：访问类型，从最好到最差依次是：system>const>eq_ref>ref>range>index>ALL

  - system：表只有一行记录（等于系统表），这是const类型的特例，平时不会出现，这个也可以忽略不计
  - const：表示通过索引一次就找到了，const用于比较primary key或者unique索引。因为只匹配一行数据，所以很快。如将主键至于where列表中，MySQL就能将该查询转换为一个常量

  - eq_ref：唯一性索引，对于每个索引键，表中**只有一条记录与之匹配**，常见于主键或唯一索引扫描

  - ref：非唯一索引扫描，**返回匹配某个单独值的所有行**。本质上也是一种索引访问，它返回所有匹配某个单独值的行，然而，它可能会找到多个符合条件的行，所以他应该属于查找和扫描的混合体。

  - range：只检索给定范围的行，使用一个索引来选择行。key列显示使用了哪个索引，一般就是在你的where语句中出现了between、<、>、in等的查询，这种范围扫描索引扫描比全表扫描要好，因为他只需要开始索引的某一点，而结束语另一点，不用扫描全部索引。

  - index：Full Index Scan,index与ALL区别为index类型只遍历索引树。这通常比ALL快，因为索引文件通常比数据文件小。（也就是说虽然all和index都是读全表，但index是从索引中读取的，而all是从硬盘中读的）。
  - all：FullTable Scan,将遍历全表以找到匹配的行。

  > 一般来说，得保证查询只是达到range级别，最好达到ref

- possible_keys：显示可能应用在这张表中的索引,一个或多个。查询涉及的字段上若存在索引，则该索引将被列出，但不一定被查询实际使用。

- key：实际使用的索引。如果为null则没有使用索引。查询中若使用了覆盖索引，则索引和查询的select字段重叠。

- key_len：表示索引中使用的字节数，可通过该列计算查询中使用的索引的长度。在不损失精确性的情况下，长度越短越好。key_len显示的值为索引最大可能长度，并非实际使用长度，即key_len是根据表定义计算而得，不是通过表内检索出的。

- ref：显示索引那一列被使用了，如果可能的话，是一个常数。那些列或常量被用于查找索引列上的值。

- rows：根据表统计信息及索引选用情况，大致估算出找到所需的记录所需要读取的行数。

- Extra：包含不适合在其他列中显示但十分重要的额外信息。

  - **Using filesort**：说明mysql会对数据使用一个外部的索引排序，而不是按照表内的索引顺序进行读取。
    MySQL中无法利用索引完成排序操作成为“文件排序”。（危险）
  - **Using temporary**：使用了临时表保存中间结果，MySQL在对查询结果排序时使用临时表。常见于排序order by 和分组查询 group by。（更加危险）
  - **Using index**：表示相应的select操作中使用了覆盖索引（Coveing Index）,避免访问了表的数据行，效率不错！如果同时出现using where，表明索引被用来执行索引键值的查找；如果没有同时出现using where，表面索引用来读取数据而非执行查找动作。
    - 覆盖索引（Covering Index）:如果一个索引包含(或覆盖)所有需要查询的字段的值，称为‘覆盖索引’。
  - Using where：表面使用了where过滤
  - Using join buffer：使用了连接缓存（可以调大一些）
  - impossible where：where子句的值总是false，不能用来获取任何元组
  - select tables optimized away：在没有GROUPBY子句的情况下，基于索引优化MIN/MAX操作或者
    对于MyISAM存储引擎优化COUNT(*)操作，不必等到执行阶段再进行计算，查询执行计划生成的阶段即完成优化。
  - distinct：优化distinct，在找到第一匹配的元组后即停止找同样值的工作

==左连接索引加在右表，右连接索引加左表。==

## 5、索引优化

### 5.1、建表sql

```mysql
CREATE TABLE `staffs`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '姓名',
  `age` int(11) NULL DEFAULT 0 COMMENT '年龄',
  `pos` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '职位',
  `add_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '入职时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_staffs_nameAgePos`(`NAME`, `age`, `pos`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '员工记录表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of staffs
-- ----------------------------
INSERT INTO `staffs` VALUES (1, 'z3', 22, 'manager', '2020-02-08 03:56:28');
INSERT INTO `staffs` VALUES (2, 'july', 23, 'dev', '2020-05-20 17:26:58');
INSERT INTO `staffs` VALUES (3, '2000', 23, 'dev', '2020-05-20 17:27:18');
```

### 5.2、案例

#### 5.2.1、全值匹配我最爱

```mysql
mysql> explain SELECT * FROM staffs WHERE `NAME`='july';
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+-------------+
| id | select_type | table  | type | possible_keys         | key                   | key_len | ref   | rows | Extra       |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+-------------+
|  1 | SIMPLE      | staffs | ref  | idx_staffs_nameAgePos | idx_staffs_nameAgePos | 74      | const |    1 | Using where |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+-------------+
1 row in set (0.00 sec)

mysql> explain SELECT * FROM staffs WHERE `NAME`='july' AND age=25;
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------+------+-------------+
| id | select_type | table  | type | possible_keys         | key                   | key_len | ref         | rows | Extra       |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------+------+-------------+
|  1 | SIMPLE      | staffs | ref  | idx_staffs_nameAgePos | idx_staffs_nameAgePos | 79      | const,const |    1 | Using where |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------+------+-------------+
1 row in set (0.00 sec)

mysql> explain SELECT * FROM staffs WHERE `NAME`='july' AND age=25 AND pos='dev';
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------------+------+-------------+
| id | select_type | table  | type | possible_keys         | key                   | key_len | ref               | rows | Extra       |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------------+------+-------------+
|  1 | SIMPLE      | staffs | ref  | idx_staffs_nameAgePos | idx_staffs_nameAgePos | 141     | const,const,const |    1 | Using where |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------------+------+-------------+
1 row in set (0.00 sec)
```

#### 5.2.2、最佳左前缀法则

如果索引了多例，要遵守最左前缀法则。指的是查询从索引的最左前列开始并且**不跳过索引中的列**。

```mysql
mysql> explain SELECT * FROM staffs WHERE age=25 AND pos='dev';
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
| id | select_type | table  | type | possible_keys | key  | key_len | ref  | rows | Extra       |
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
|  1 | SIMPLE      | staffs | ALL  | NULL          | NULL | NULL    | NULL |    3 | Using where |
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
1 row in set (0.00 sec)

mysql> explain SELECT * FROM staffs WHERE pos='dev';
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
| id | select_type | table  | type | possible_keys | key  | key_len | ref  | rows | Extra       |
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
|  1 | SIMPLE      | staffs | ALL  | NULL          | NULL | NULL    | NULL |    3 | Using where |
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
1 row in set (0.00 sec)

mysql> explain SELECT * FROM staffs WHERE `NAME`='july';
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+-------------+
| id | select_type | table  | type | possible_keys         | key                   | key_len | ref   | rows | Extra       |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+-------------+
|  1 | SIMPLE      | staffs | ref  | idx_staffs_nameAgePos | idx_staffs_nameAgePos | 74      | const |    1 | Using where |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+-------------+
1 row in set (0.00 sec)
```

#### 5.2.3、不在索引列上做任何操作（计算、函数、（自动or手动）类型转换），会导致索引失效而转向全表扫描

```mysql
mysql> EXPLAIN SELECT * FROM staffs WHERE LEFT(`name`,4)='july';
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
| id | select_type | table  | type | possible_keys | key  | key_len | ref  | rows | Extra       |
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
|  1 | SIMPLE      | staffs | ALL  | NULL          | NULL | NULL    | NULL |    3 | Using where |
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
1 row in set (0.00 sec)
```

#### 5.2.4、存储引擎不能使用索引中范围条件右边的列

```mysql
mysql> explain SELECT * FROM staffs WHERE `NAME`='july' AND age=25 AND pos='dev';
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------------+------+-------------+
| id | select_type | table  | type | possible_keys         | key                   | key_len | ref               | rows | Extra       |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------------+------+-------------+
|  1 | SIMPLE      | staffs | ref  | idx_staffs_nameAgePos | idx_staffs_nameAgePos | 141     | const,const,const |    1 | Using where |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------------+------+-------------+
1 row in set (0.00 sec)

mysql> explain SELECT * FROM staffs WHERE `NAME`='july' AND age>25 AND pos='manager';
+----+-------------+--------+-------+-----------------------+-----------------------+---------+------+------+-------------+
| id | select_type | table  | type  | possible_keys         | key                   | key_len | ref  | rows | Extra       |
+----+-------------+--------+-------+-----------------------+-----------------------+---------+------+------+-------------+
|  1 | SIMPLE      | staffs | range | idx_staffs_nameAgePos | idx_staffs_nameAgePos | 79      | NULL |    1 | Using where |
+----+-------------+--------+-------+-----------------------+-----------------------+---------+------+------+-------------+
1 row in set (0.00 sec)
```

#### 5.2.5、尽量使用覆盖索引（只访问索引的查询（索引列和查询列一致）），减少select*

```mysql
mysql> explain SELECT * FROM staffs WHERE `NAME`='july' AND age=25 AND pos='manager';
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------------+------+-------------+
| id | select_type | table  | type | possible_keys         | key                   | key_len | ref               | rows | Extra       |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------------+------+-------------+
|  1 | SIMPLE      | staffs | ref  | idx_staffs_nameAgePos | idx_staffs_nameAgePos | 141     | const,const,const |    1 | Using where |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------------+------+-------------+
1 row in set (0.00 sec)

mysql> explain SELECT name,age,pos FROM staffs WHERE `NAME`='july' AND age=25 AND pos='manager';
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------------+------+--------------------------+
| id | select_type | table  | type | possible_keys         | key                   | key_len | ref               | rows | Extra                    |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------------+------+--------------------------+
|  1 | SIMPLE      | staffs | ref  | idx_staffs_nameAgePos | idx_staffs_nameAgePos | 141     | const,const,const |    1 | Using where; Using index |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------------------+------+--------------------------+
1 row in set (0.00 sec)

mysql> explain SELECT name,age,pos FROM staffs WHERE `NAME`='july' AND age>25 AND pos='manager';
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+--------------------------+
| id | select_type | table  | type | possible_keys         | key                   | key_len | ref   | rows | Extra                    |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+--------------------------+
|  1 | SIMPLE      | staffs | ref  | idx_staffs_nameAgePos | idx_staffs_nameAgePos | 74      | const |    1 | Using where; Using index |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+--------------------------+
1 row in set (0.00 sec)
```

#### 5.2.6、mysql在使用不等于（！=或者<>）的时候无法使用索引会导致全表扫描

```mysql
mysql> explain SELECT * FROM staffs WHERE `NAME`='july';
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+-------------+
| id | select_type | table  | type | possible_keys         | key                   | key_len | ref   | rows | Extra       |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+-------------+
|  1 | SIMPLE      | staffs | ref  | idx_staffs_nameAgePos | idx_staffs_nameAgePos | 74      | const |    1 | Using where |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+-------------+
1 row in set (0.00 sec)

mysql> explain SELECT * FROM staffs WHERE `NAME`!='july';
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
| id | select_type | table  | type | possible_keys         | key  | key_len | ref  | rows | Extra       |
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
|  1 | SIMPLE      | staffs | ALL  | idx_staffs_nameAgePos | NULL | NULL    | NULL |    3 | Using where |
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
1 row in set (0.00 sec)

mysql> explain SELECT * FROM staffs WHERE `NAME`<>'july';
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
| id | select_type | table  | type | possible_keys         | key  | key_len | ref  | rows | Extra       |
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
|  1 | SIMPLE      | staffs | ALL  | idx_staffs_nameAgePos | NULL | NULL    | NULL |    3 | Using where |
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
1 row in set (0.00 sec)
```

#### 5.2.7、is null,is not null 也无法使用索引

```mysql
mysql> explain SELECT * FROM staffs WHERE `NAME` is null;
+----+-------------+-------+------+---------------+------+---------+------+------+------------------+
| id | select_type | table | type | possible_keys | key  | key_len | ref  | rows | Extra            |
+----+-------------+-------+------+---------------+------+---------+------+------+------------------+
|  1 | SIMPLE      | NULL  | NULL | NULL          | NULL | NULL    | NULL | NULL | Impossible WHERE |
+----+-------------+-------+------+---------------+------+---------+------+------+------------------+
1 row in set (0.00 sec)

mysql> explain SELECT * FROM staffs WHERE `NAME` is not null;
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
| id | select_type | table  | type | possible_keys         | key  | key_len | ref  | rows | Extra       |
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
|  1 | SIMPLE      | staffs | ALL  | idx_staffs_nameAgePos | NULL | NULL    | NULL |    3 | Using where |
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
1 row in set (0.00 sec)
```

#### 5.2.8、like以通配符开头（'%abc...'）mysql索引失效会变成全表扫描操作

```mysql
mysql> explain SELECT * FROM staffs WHERE `NAME` like '%july%';
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
| id | select_type | table  | type | possible_keys | key  | key_len | ref  | rows | Extra       |
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
|  1 | SIMPLE      | staffs | ALL  | NULL          | NULL | NULL    | NULL |    3 | Using where |
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
1 row in set (0.01 sec)

mysql> explain SELECT * FROM staffs WHERE `NAME` like '%july';
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
| id | select_type | table  | type | possible_keys | key  | key_len | ref  | rows | Extra       |
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
|  1 | SIMPLE      | staffs | ALL  | NULL          | NULL | NULL    | NULL |    3 | Using where |
+----+-------------+--------+------+---------------+------+---------+------+------+-------------+
1 row in set (0.00 sec)

mysql> explain SELECT * FROM staffs WHERE `NAME` like 'july%';
+----+-------------+--------+-------+-----------------------+-----------------------+---------+------+------+-------------+
| id | select_type | table  | type  | possible_keys         | key                   | key_len | ref  | rows | Extra       |
+----+-------------+--------+-------+-----------------------+-----------------------+---------+------+------+-------------+
|  1 | SIMPLE      | staffs | range | idx_staffs_nameAgePos | idx_staffs_nameAgePos | 74      | NULL |    1 | Using where |
+----+-------------+--------+-------+-----------------------+-----------------------+---------+------+------+-------------+
1 row in set (0.00 sec)
```

**问题：解决like'%字符串%'索引不被使用的方法？？**

**使用覆盖索引解决**，查询的列要被建的索引包含，可以不完全包含。

#### 5.2.9、字符串不加单引号索引失效（**重罪**）

```mysql
mysql> explain SELECT * FROM staffs WHERE `NAME`='2000';
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+-------------+
| id | select_type | table  | type | possible_keys         | key                   | key_len | ref   | rows | Extra       |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+-------------+
|  1 | SIMPLE      | staffs | ref  | idx_staffs_nameAgePos | idx_staffs_nameAgePos | 74      | const |    1 | Using where |
+----+-------------+--------+------+-----------------------+-----------------------+---------+-------+------+-------------+
1 row in set (0.00 sec)

mysql> explain SELECT * FROM staffs WHERE `NAME`=2000;
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
| id | select_type | table  | type | possible_keys         | key  | key_len | ref  | rows | Extra       |
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
|  1 | SIMPLE      | staffs | ALL  | idx_staffs_nameAgePos | NULL | NULL    | NULL |    3 | Using where |
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
1 row in set (0.00 sec)
```

#### 5.2.10、少用or,用它连接时会索引失效

```mysql
mysql> explain SELECT * FROM staffs WHERE `NAME`='2000' or `NAME`='z3';
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
| id | select_type | table  | type | possible_keys         | key  | key_len | ref  | rows | Extra       |
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
|  1 | SIMPLE      | staffs | ALL  | idx_staffs_nameAgePos | NULL | NULL    | NULL |    3 | Using where |
+----+-------------+--------+------+-----------------------+------+---------+------+------+-------------+
1 row in set (0.00 sec)
```

### 5.3、面试题讲解

```mysql
CREATE TABLE `test03`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `c1` char(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `c2` char(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `c3` char(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `c4` char(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `c5` char(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_test03_c1234`(`c1`, `c2`, `c3`, `c4`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of test03
-- ----------------------------
INSERT INTO `test03` VALUES (1, 'a1', 'a2', 'a3', 'a4', 'a5');
INSERT INTO `test03` VALUES (2, 'b1', 'b2', 'b3', 'b4', 'b5');
INSERT INTO `test03` VALUES (3, 'c1', 'c2', 'c3', 'c4', 'c5');
INSERT INTO `test03` VALUES (4, 'd1', 'd2', 'd3', 'd4', 'd5');
INSERT INTO `test03` VALUES (5, 'e1', 'e2', 'e3', 'e4', 'e5');
```

1）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c3='a3' AND c4='a4';

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c3='a3' AND c4='a4';
+----+-------------+--------+------+------------------+------------------+---------+-------------------------+------+-------------+
| id | select_type | table  | type | possible_keys    | key              | key_len | ref                     | rows | Extra       |
+----+-------------+--------+------+------------------+------------------+---------+-------------------------+------+-------------+
|  1 | SIMPLE      | test03 | ref  | idx_test03_c1234 | idx_test03_c1234 | 124     | const,const,const,const |    1 | Using where |
+----+-------------+--------+------+------------------+------------------+---------+-------------------------+------+-------------+
```

2）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c4='a4' AND c3='a3';

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c4='a4' AND c3='a3';
+----+-------------+--------+------+------------------+------------------+---------+-------------------------+------+-------------+
| id | select_type | table  | type | possible_keys    | key              | key_len | ref                     | rows | Extra       |
+----+-------------+--------+------+------------------+------------------+---------+-------------------------+------+-------------+
|  1 | SIMPLE      | test03 | ref  | idx_test03_c1234 | idx_test03_c1234 | 124     | const,const,const,const |    1 | Using where |
+----+-------------+--------+------+------------------+------------------+---------+-------------------------+------+-------------+
```

3）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c3>'a3' AND c4='a4';

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c3>'a3' AND c4='a4';
+----+-------------+--------+-------+------------------+------------------+---------+------+------+-------------+
| id | select_type | table  | type  | possible_keys    | key              | key_len | ref  | rows | Extra       |
+----+-------------+--------+-------+------------------+------------------+---------+------+------+-------------+
|  1 | SIMPLE      | test03 | range | idx_test03_c1234 | idx_test03_c1234 | 93      | NULL |    1 | Using where |
+----+-------------+--------+-------+------------------+------------------+---------+------+------+-------------+
```

4）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c4>'a4' AND c3='a3';

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c4>'a4' AND c3='a3';
+----+-------------+--------+-------+------------------+------------------+---------+------+------+-------------+
| id | select_type | table  | type  | possible_keys    | key              | key_len | ref  | rows | Extra       |
+----+-------------+--------+-------+------------------+------------------+---------+------+------+-------------+
|  1 | SIMPLE      | test03 | range | idx_test03_c1234 | idx_test03_c1234 | 124     | NULL |    1 | Using where |
+----+-------------+--------+-------+------------------+------------------+---------+------+------+-------------+
```

5）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c4='a4' ORDER BY c3;

c3的作用用于排序。

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c4='a4' ORDER BY c3;
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
| id | select_type | table  | type | possible_keys    | key              | key_len | ref         | rows | Extra       |
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
|  1 | SIMPLE      | test03 | ref  | idx_test03_c1234 | idx_test03_c1234 | 62      | const,const |    1 | Using where |
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
```

6）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' ORDER BY c3;

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' ORDER BY c3;
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
| id | select_type | table  | type | possible_keys    | key              | key_len | ref         | rows | Extra       |
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
|  1 | SIMPLE      | test03 | ref  | idx_test03_c1234 | idx_test03_c1234 | 62      | const,const |    1 | Using where |
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
```

7）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' ORDER BY c4;

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' ORDER BY c4;
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-----------------------------+
| id | select_type | table  | type | possible_keys    | key              | key_len | ref         | rows | Extra                       |
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-----------------------------+
|  1 | SIMPLE      | test03 | ref  | idx_test03_c1234 | idx_test03_c1234 | 62      | const,const |    1 | Using where; Using filesort |
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-----------------------------+
```

8.1）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c5='a5' ORDER BY c2,c3;

只用c1一个字段索引，但是c2，c3用于排序，没有filesort

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c5='a5' ORDER BY c2,c3;
+----+-------------+--------+------+------------------+------------------+---------+-------+------+-------------+
| id | select_type | table  | type | possible_keys    | key              | key_len | ref   | rows | Extra       |
+----+-------------+--------+------+------------------+------------------+---------+-------+------+-------------+
|  1 | SIMPLE      | test03 | ref  | idx_test03_c1234 | idx_test03_c1234 | 31      | const |    1 | Using where |
+----+-------------+--------+------+------------------+------------------+---------+-------+------+-------------+
```

8.2）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c5='a5' ORDER BY c3,c2;

出现了filesort，我们建的索引是1234，它没有按顺序来，3,2颠倒了

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c5='a5' ORDER BY c3,c2;
+----+-------------+--------+------+------------------+------------------+---------+-------+------+-----------------------------+
| id | select_type | table  | type | possible_keys    | key              | key_len | ref   | rows | Extra                       |
+----+-------------+--------+------+------------------+------------------+---------+-------+------+-----------------------------+
|  1 | SIMPLE      | test03 | ref  | idx_test03_c1234 | idx_test03_c1234 | 31      | const |    1 | Using where; Using filesort |
+----+-------------+--------+------+------------------+------------------+---------+-------+------+-----------------------------+
```

9）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' ORDER BY c2,c3;

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' ORDER BY c2,c3;
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
| id | select_type | table  | type | possible_keys    | key              | key_len | ref         | rows | Extra       |
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
|  1 | SIMPLE      | test03 | ref  | idx_test03_c1234 | idx_test03_c1234 | 62      | const,const |    1 | Using where |
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
```

10）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c5='c5' ORDER BY c2,c3;

用c1，c2两个字段索引，但是c2，c3用于排序，没有filesort

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c5='c5' ORDER BY c2,c3;
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
| id | select_type | table  | type | possible_keys    | key              | key_len | ref         | rows | Extra       |
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
|  1 | SIMPLE      | test03 | ref  | idx_test03_c1234 | idx_test03_c1234 | 62      | const,const |    1 | Using where |
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
```

11）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c5='c5' ORDER BY c3,c2;

本例有常量c2的情况，和8.2对比

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c2='a2' AND c5='c5' ORDER BY c3,c2;
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
| id | select_type | table  | type | possible_keys    | key              | key_len | ref         | rows | Extra       |
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
|  1 | SIMPLE      | test03 | ref  | idx_test03_c1234 | idx_test03_c1234 | 62      | const,const |    1 | Using where |
+----+-------------+--------+------+------------------+------------------+---------+-------------+------+-------------+
```

12）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c4='a4' GROUP BY c2,c3;

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c4='a4' GROUP BY c2,c3;
+----+-------------+--------+------+------------------+------------------+---------+-------+------+-------------+
| id | select_type | table  | type | possible_keys    | key              | key_len | ref   | rows | Extra       |
+----+-------------+--------+------+------------------+------------------+---------+-------+------+-------------+
|  1 | SIMPLE      | test03 | ref  | idx_test03_c1234 | idx_test03_c1234 | 31      | const |    1 | Using where |
+----+-------------+--------+------+------------------+------------------+---------+-------+------+-------------+
```

13）、EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c4='a4' GROUP BY c3,c2;

```mysql
mysql> EXPLAIN SELECT * FROM test03 WHERE c1='a1' AND c4='a4' GROUP BY c3,c2;
+----+-------------+--------+------+------------------+------------------+---------+-------+------+----------------------------------------------+
| id | select_type | table  | type | possible_keys    | key              | key_len | ref   | rows | Extra                                        |
+----+-------------+--------+------+------------------+------------------+---------+-------+------+----------------------------------------------+
|  1 | SIMPLE      | test03 | ref  | idx_test03_c1234 | idx_test03_c1234 | 31      | const |    1 | Using where; Using temporary; Using filesort |
+----+-------------+--------+------+------------------+------------------+---------+-------+------+----------------------------------------------+
```

**group by基本上都需要进行排序，会有临时表产生。**

### 5.4、一般性建议

- 对于单键索引，尽量选择针对当前query过滤性更好的索引
- 在选择组合索引的时候，当前query中过滤性最好的字段在索引字段顺序中，位置越靠前越好
- 在选择组合索引的时候，尽量选择可以能够包含当前query中的where子句中更对字段的索引
- 尽可能通过分析统计信息和调整query的写法来达到选择合适索引的目的

### 5.5、你的sql优化？

1、观察，至少跑一天，看看生产的慢sql情况。

2、开启慢查询日志，设置阈值，比如超过五秒钟的就是慢sql，并将它抓取出来。

3、explain+慢sql分析。

4、show profiles查询sql在在mysql服务器里面执行细节和生命周期情况。

5、运维经理orDBA进行SQL数据库服务器的参数调优。

# 二、查询截取分析

## 1、查询优化

### 1.1 永远小表驱动大表，类似嵌套循环Nested Loop
```mysql
当B表的数据集小于A表的数据集，用in优于exists
select * from A where id in (select id from B)
等价于
for select id from B
for select * from A where A.id = B.id
当A表的数据集小于B表的数据集，用exists优于in
select * from A where exists (select 1 from B where B.id = A.id)
等价于
for select * from A
for select * from B where B.id = A.id
注意：A表与B表的ID字段应建立索引
```

## 2、order by关键字优化

### 2.1、ORDER BY子句，尽量使用Index方式排序，避免使用FileSort方式排序

```mysql
mysql> explain select * from tblA where age>20 order by age;
+----+-------------+-------+-------+----------------+----------------+---------+------+------+--------------------------+
| id | select_type | table | type  | possible_keys  | key            | key_len | ref  | rows | Extra                    |
+----+-------------+-------+-------+----------------+----------------+---------+------+------+--------------------------+
|  1 | SIMPLE      | tblA  | index | idx_A_ageBirth | idx_A_ageBirth | 9       | NULL |    3 | Using where; Using index |
+----+-------------+-------+-------+----------------+----------------+---------+------+------+--------------------------+
```

```mysql
mysql> explain select * from tblA where age>20 order by age,birth;
+----+-------------+-------+-------+----------------+----------------+---------+------+------+--------------------------+
| id | select_type | table | type  | possible_keys  | key            | key_len | ref  | rows | Extra                    |
+----+-------------+-------+-------+----------------+----------------+---------+------+------+--------------------------+
|  1 | SIMPLE      | tblA  | index | idx_A_ageBirth | idx_A_ageBirth | 9       | NULL |    3 | Using where; Using index |
+----+-------------+-------+-------+----------------+----------------+---------+------+------+--------------------------+
```

```mysql
mysql> explain select * from tblA where age>20 order by birth;
+----+-------------+-------+-------+----------------+----------------+---------+------+------+------------------------------------------+
| id | select_type | table | type  | possible_keys  | key            | key_len | ref  | rows | Extra                                    |
+----+-------------+-------+-------+----------------+----------------+---------+------+------+------------------------------------------+
|  1 | SIMPLE      | tblA  | index | idx_A_ageBirth | idx_A_ageBirth | 9       | NULL |    3 | Using where; Using index; Using filesort |
+----+-------------+-------+-------+----------------+----------------+---------+------+------+------------------------------------------+
```

```mysql
mysql> explain select * from tblA where age>20 order by birth,age;
+----+-------------+-------+-------+----------------+----------------+---------+------+------+------------------------------------------+
| id | select_type | table | type  | possible_keys  | key            | key_len | ref  | rows | Extra                                    |
+----+-------------+-------+-------+----------------+----------------+---------+------+------+------------------------------------------+
|  1 | SIMPLE      | tblA  | index | idx_A_ageBirth | idx_A_ageBirth | 9       | NULL |    3 | Using where; Using index; Using filesort |
+----+-------------+-------+-------+----------------+----------------+---------+------+------+------------------------------------------+
```

```mysql
mysql> explain select * from tblA order by birth;
+----+-------------+-------+-------+---------------+----------------+---------+------+------+-----------------------------+
| id | select_type | table | type  | possible_keys | key            | key_len | ref  | rows | Extra                       |
+----+-------------+-------+-------+---------------+----------------+---------+------+------+-----------------------------+
|  1 | SIMPLE      | tblA  | index | NULL          | idx_A_ageBirth | 9       | NULL |    3 | Using index; Using filesort |
+----+-------------+-------+-------+---------------+----------------+---------+------+------+-----------------------------+
```

```mysql
mysql> explain select * from tblA order by age ASC,birth DESC;
+----+-------------+-------+-------+---------------+----------------+---------+------+------+-----------------------------+
| id | select_type | table | type  | possible_keys | key            | key_len | ref  | rows | Extra                       |
+----+-------------+-------+-------+---------------+----------------+---------+------+------+-----------------------------+
|  1 | SIMPLE      | tblA  | index | NULL          | idx_A_ageBirth | 9       | NULL |    3 | Using index; Using filesort |
+----+-------------+-------+-------+---------------+----------------+---------+------+------+-----------------------------+
```

**MySQL支持二种方式的排序，FileSort和Index,Index效率高。它指MySQL扫描索引本身完成排序。FileSort方式效率较低。**

**ORDER BY满足两情况，会使用Index方式排序**：

- ORDER BY语句使用索引最左前列
- 使用where子句与OrderBy子句条件列组合满足索引最左前列

### 2.2、如果不在索引列上，filesort有两种算法：mysql就要启动双路排序和单路排序
#### 2.2.1、双路排序

MySQL4.1之前是使用双路排序，字面意思是**两次扫描磁盘**，最终得到数据。读取行指针和orderby列，对他们进行排序，然后扫描已经排序好的列表，按照列表中的值重新从列表中读取对应的数据传输。

从磁盘取排序字段，在buffer进行排序，再从磁盘取其他字段。

取一批数据，要对磁盘进行两次扫描，众所周知，I\O是很耗时的，所以在mysql4.1之后，出现了第二张改进的算法，就是单路排序。

#### 2.2.2、单路排序

从磁盘读取查询需要的所有列，按照orderby列在buffer对它们进行排序，然后扫描排序后的列表进行输出，它的效率更快一些，避免了第二次读取数据，并且把随机IO变成顺序IO，但是它会使用更多的空间，因为它把每一行都保存在内存中了。

#### 2.2.3、结论及引申出的问题

由于单路是后出来的，总体而言好过双路。但是用单路有问题。

由于是把排序操作放到了一个buffer里面，就会导致buffer的容量如果比较小，数据一次读不完，就会继续读。甚至需要不止两次`I/O`。

**优化策略：增大`sort_buffer_size`参数的设置；增大`max_length_for_sort_data`参数的设置。**

提高order by的速度：

- `order by`时`select *` 是一个大忌只query需要的字段，这点非常重要。
  - 当`query`的字段大小总和小于`max_length_for_sort_data`而且排序字段不是`TEXT|BLOB`类型时，会用改进后的算法——单路排序，否则用——多路排序。
  - 两种算法的数据都有可能超出sort_buffer的容量，超出之后，会创建tmp文件进行合并排序，导致多次`I/O`，但是用单路排序算法的风险会更大一些，所以要提高`sort_buffer_size`。
- 尝试提高`sort_buffer_size`：不管用哪种算法，提高这个参数都会提高效率，当然，要根据系统的能力去提高，因为这个参数是针对每个进程的。
- 尝试提高`max_length_for_sort_data`：提高这个参数，会增加用改进算法的概率。但是如果设的太高，数据总容量超出`sort_buffer_size`的概率就增大，明显症状高的磁盘`I/O`活动和低的处理器使用率。

#### 2.2.4、小总结

`mysql`两种排序方式：文件排序或扫描有序索引排序。

`mysql`能为排序与查询使用相同的索引。

索引 `key a_b_c(a,b,c)`，`order by`能使用索引最左前缀：

```mysql
 order by a
 order by a,b
 order by a,b,c
 order by a DESC,b DESC,c DESC (同升或同降)
```

如果`where`使用索引的最左前缀定义为常量，则`order by`能使用索引：

```mysql
where a = const order by b,c
where a = const and b = const order by c
where a = const and b > const oder by b,c
```

不能使用索引进行排序：

```mysql
order by a ASC,b DESC, c DESC (排序不一致)
where g = const order by b,c (丢失a索引)
where a = const order by c (丢失b索引)
where a = const order by a,d (d不是索引的一部分)
where a in(...) order by b,c (对于排序来说，多个相等条件也是范围查询)
```

## 3、GROUP BY关键字优化

- group by实质是先排序后进行分组，遵照索引建的最佳左前缀

- 当无法使用索引列，增大max_length_for_sort_data参数的设置+增大sort_buffer_size参数的设置
- where高于having,能写在where限定的条件就不要去having限定了。

## 4、慢查询日志

`MySQL`的慢查询日志是`MySQL`提供的一种日志记录，它用来记录在`MySQL`中响应时间超过阀值的语句，具体指运行时间超过`long_query_time`值的`SQL`，则会被记录到慢查询日志中。`long_query_time`的默认值为10，意思是运行10S以上的语句。默认情况下，`Mysql`数据库并不启动慢查询日志，需要我们手动来设置这个参数，当然，如果不是调优需要的话，一般不建议启动该参数，因为开启慢查询日志会或多或少带来一定的性能影响。慢查询日志支持将日志记录写入文件，也支持将日志记录写入数据库表。

### 4.1、怎么用？

#### 4.1.1、查看是否开启？默认是关闭的。

```mysql
mysql> show variables like '%slow_query_log%'; #查看慢日志状态
+---------------------+--------------------------------+
| Variable_name       | Value                          |
+---------------------+--------------------------------+
| slow_query_log      | OFF                            |
| slow_query_log_file | /var/lib/mysql/master-slow.log |
+---------------------+--------------------------------+
```

开启功能：**set global slow_query_log = 1**，开启慢日志,只对本次有效,重启之后还是关闭的。

如果想要永久生效的话,就需要修改my.cnf文件。

#### 4.1.2、那么开启慢查询日志后，什么样的SQL参会记录到慢查询里面？

```mysql
mysql> SHOW VARIABLES LIKE 'long_query_time%'; #查看设置的时间阈值
+-----------------+-----------+
| Variable_name   | Value     |
+-----------------+-----------+
| long_query_time | 10.000000 |
+-----------------+-----------+
```

```mysql
set global long_query_time=3; #设置慢查询时间的阈值为3秒
```

需要重新连接或新开一个会话才能看到修改值。

```mysql
mysql> show global variables like 'long_query_time';
+-----------------+----------+
| Variable_name   | Value    |
+-----------------+----------+
| long_query_time | 3.000000 |
+-----------------+----------+
```

```mysql
select sleep(4);#在这条sql执行期间,睡眠4秒
```

查看日志

```shell
[root@master mysql]# cat /var/lib/mysql/master-slow.log 

/usr/sbin/mysqld, Version: 5.5.48-log (MySQL Community Server (GPL)). started with:
Tcp port: 3306  Unix socket: /var/lib/mysql/mysql.sock
Time                 Id Command    Argument
# Time: 200208 10:03:20
# User@Host: root[root] @ localhost []
# Query_time: 4.000249  Lock_time: 0.000000 Rows_sent: 1  Rows_examined: 0
use db01;
SET timestamp=1581127400;
select sleep(4);
```

查看慢日志中有多少条sql

```mysql
mysql> show global status like '%Slow_queries%';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| Slow_queries  | 1     |
+---------------+-------+
```

### 4.2、日志分析工具mysqldumpshow

`mysqldumpslow --help`

```shell
s:表示按照何种方式排序
c:访问次数
l:锁定时间
r:返回记录
t:查询时间
al:平均锁定时间
ar:平均返回记录数
at:平均查询时间
t:即为返回前面多少条的数据:
g:后边搭配一个正则匹配模式,大小写不敏感的
```

工作中常用参考：

```shell
#得到返回记录集最多的10个sql
mysqldumpslow -s r -t 10 /var/lib/mysql/master-slow.log
#得到访问次数最多的10个sql
mysqldumpslow -s c -t 10 /var/lib/mysql/master-slow.log 
#得到按照时间排序的前10条里面含有左连接的查询语句
mysqldumpslow -s t -t 10 -g "left join" /var/lib/mysql/master-slow.log
#另外建议在使用这些命令时结合|more使用，否则可能出现爆屏情况
mysqldumpslow -s r -t 10 /var/lib/mysql/master-slow.log | more
```

## 5、批量数据脚本

### 5.1、建表sql

```mysql
CREATE TABLE `dept`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `deptno` mediumint(8) UNSIGNED NOT NULL DEFAULT 0,
  `dname` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `loc` varchar(13) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

CREATE TABLE `emp`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `empno` mediumint(9) NOT NULL DEFAULT 0,
  `ename` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `job` varchar(9) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `mgr` mediumint(8) UNSIGNED NOT NULL DEFAULT 0,
  `hiredate` date NOT NULL,
  `sal` decimal(7, 2) NOT NULL,
  `comm` decimal(7, 2) NOT NULL,
  `deptno` mediumint(8) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 500001 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

```

往表里插入1000W数据，创建函数假如报错：This function has none of DETERMINISTIC......

优于开启过慢查询日志，因为开启了bin-log，就必须为function指定一个参数，**set global log_bin_trust_function_creators=1**。

```mysql
mysql> show variables like 'log_bin_trust_function_creators';
+---------------------------------+-------+
| Variable_name                   | Value |
+---------------------------------+-------+
| log_bin_trust_function_creators | OFF   |
+---------------------------------+-------+
```

### 5.2、创建函数保证每条数据都不同

```mysql
#随机产生字符串
CREATE DEFINER=`root`@`%` FUNCTION `rand_string`(n INT) RETURNS varchar(255) CHARSET utf8
BEGIN
		DECLARE chars_str VARCHAR(100) DEFAULT 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
		DECLARE return_str VARCHAR(255) DEFAULT '';
		DECLARE i INT DEFAULT 0;
		while i < n DO
			set return_str = concat(return_str,substring(chars_str,floor(1+rand()*52),1));
			set i =  i + 1;
		END while;
		return return_str;
END
```

```mysql
#随机产生部门编号
CREATE DEFINER=`root`@`%` FUNCTION `rand_num`() RETURNS int(5)
BEGIN 
		DECLARE i INT DEFAULT 0;
		set i = floor(100+rand()*10);
		return i;
END
```

### 5.3、创建存储过程

```mysql
#创建往dept表中插入数据的存储过程
CREATE DEFINER=`root`@`%` PROCEDURE `insert_dept`(IN START INT(10),IN max_num INT(10))
BEGIN
	DECLARE i INT DEFAULT 0;
	set autocommit=0;
	REPEAT
	set i = i+1;
	insert into dept(deptno,dname,loc) VALUES((START+i),rand_string(10),rand_string(8));
UNTIL i=max_num END REPEAT;
commit;
END
```

```mysql
#创建往emp表中插入数据的存储过程
CREATE DEFINER=`root`@`%` PROCEDURE `insert_emp`(in start int(10),in max_num int(10))
BEGIN
	DECLARE i int DEFAULT 0;
	set autocommit=0;
	REPEAT
	set i = i+1;
	insert into emp(empno,ename,job,mgr,hiredate,sal,comm,deptno) VALUES((start+i)
	,rand_string(6),'SALESMAN',0001,CURDATE(),2000,400,rand_num());
	
UNTIL i=max_num END REPEAT;
commit;
END
```

### 5.4、调用存储过程

```mysql
call CALL insert_emp(100001,500000);
call CALL insert_dept(100,10);
```

## 6、Show profiles

是什么：是`mysql`提供可以用来分析当前会话中语句执行的资源消耗情况。可以用于`SQL`的调优测量。

默认情况下，参数处于关闭状态，并保存最近15次的运行结果.

```mysql
#查看状态
mysql> show variables like 'profiling';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| profiling     | OFF   |
+---------------+-------+
```

```mysql
#开启功能
mysql> set profiling=on;
```

```mysql
#查看结果
mysql> show profiles;
+----------+------------+-----------------------------------------------+
| Query_ID | Duration   | Query                                         |
+----------+------------+-----------------------------------------------+
|        1 | 0.00034075 | show variables like 'profiling'               |
|        2 | 0.00040100 | show variables like 'profiling'               |
|        3 | 0.00017125 | select * from emp group by id%10 limit 150000 |
|        4 | 0.00027450 | SELECT DATABASE()                             |
|        5 | 0.00009275 | select * from emp group by id%10 limit 150000 |
|        6 | 0.00005025 | select * from emp group by id%20 order by 5   |
+----------+------------+-----------------------------------------------+
```

### 6.1、诊断SQL

**show profile cpu,block io for query Query_ID；**

```mysql
mysql> show profile cpu,block io for query 5;
+--------------------------------+----------+----------+------------+--------------+---------------+
| Status                         | Duration | CPU_user | CPU_system | Block_ops_in | Block_ops_out |
+--------------------------------+----------+----------+------------+--------------+---------------+
| starting                       | 0.000029 | 0.000000 |   0.000000 |            0 |             0 |
| Waiting for query cache lock   | 0.000002 | 0.000000 |   0.000000 |            0 |             0 |
| checking query cache for query | 0.000004 | 0.000000 |   0.000000 |            0 |             0 |
| checking privileges on cached  | 0.000002 | 0.000000 |   0.000000 |            0 |             0 |
| checking permissions           | 0.000031 | 0.000000 |   0.000000 |            0 |             0 |
| sending cached result to clien | 0.000023 | 0.000000 |   0.000000 |            0 |             0 |
| logging slow query             | 0.000001 | 0.000000 |   0.000000 |            0 |             0 |
| cleaning up                    | 0.000002 | 0.000000 |   0.000000 |            0 |             0 |
+--------------------------------+----------+----------+------------+--------------+---------------+
8 rows in set (0.00 sec)
```

> type:
>
> ALL：显示所有的开销信息
>
> BLOCK IO：显示块IO相关开销
>
> CONTEXT SWITCHES：上线文切换相关开销
>
> CPU：显示CPU相关开销信息
>
> IPC：显示发送和接收相关开销信息
>
> MEMORY：显示内存相关开销信息
>
> PAGE FAULTS：显示页面错误相关开销信息
>
> SOURCE：显示和Source_function, Source_file, Source_line相关开销信息
>
> SWAPS：显示交换次数相关开销信息

### 6.2、日常开发需要注意的结论

- converting HEAP to MyISAM 查询结果太大，内存都不够用了往磁盘上搬了。
- Creating tmp table 创建临时表
- Copying to tmp table on disk 把内存中临时表复制到磁盘，危险！！！
- locked

## 7、全局查询日志

**永远不要在生产环境开启这个功能。**

```mysql
mysql> set global general_log=1;
mysql> set global log_output='TABLE';
mysql> select * from mysql.general_log;
```

# 三、Mysql锁机制

## 1、表锁（偏读）

偏向MyISAM存储引擎，开销小，加锁快，无死锁，锁定粒度大，发生锁冲突的概率最高，并发最低

### 1.1、建表sql

```mysql
CREATE TABLE `mylock`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
```

查看表的锁状态

```mysql
mysql> show open tables;
```

### 1.2、加读锁

```mysql
mysql> lock table mylock read;
```

| session-1                                                    | session-2                                                    |
| :----------------------------------------------------------- | ------------------------------------------------------------ |
| 获得表mylock的READ锁定<br />![](https://github.com/jackhusky/doc/blob/master/mysql/images/session-1-获得表READ锁定.png) | 连接终端                                                     |
| 当前session可以查询表记录<br />![](https://github.com/jackhusky/doc/blob/master/mysql/images/session-1查询本表.png) | 其他session也可以查询该表的记录<br />![](https://github.com/jackhusky/doc/blob/master/mysql/images/session-2查看表记录.png) |
| 当前session不能查询其他没有锁定的表<br />![](https://github.com/jackhusky/doc/blob/master/mysql/images/session-1查看其他表.png) | 其他session可以查询或者更新未锁定的表<br />![](https://github.com/jackhusky/doc/blob/master/mysql/images/session-2查询更新其它没锁定的表.png) |
| 当前session中插入或者更新锁定的表都会提示错误<br />![](https://github.com/jackhusky/doc/blob/master/mysql/images/session-1修改锁定的表.png) | 其他session插入插入或者更新锁定的表会一直等待获得锁<br />![](https://github.com/jackhusky/doc/blob/master/mysql/images/session-2修改锁定表.png) |
| 释放锁<br />![](https://github.com/jackhusky/doc/blob/master/mysql/images/session-1释放锁.png) | session-2获得锁，插入操作完成<br />![](https://github.com/jackhusky/doc/blob/master/mysql/images/session-2插入数据.png) |

### 1.3、加写锁

```mysql
mysql> lock table mylock write;
```

| session-1                                                    | session-2                                                    |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| 获得表mylock的WRITE锁定<br />![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-1加写锁.png) | 待session-1开启写锁后，session-2再连接终端                   |
| 当前session对锁定表的查询+更新+插入语操作都可执行<br />![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-1对锁定表的更新+查询.png) | 其他session对锁定表的查询被阻塞，需要等待锁释放<br />![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-2查询写锁的表.png) |
| 释放锁<br />![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-1释放写锁.png) | session-2获得，查询返回<br />![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-2获得锁查询返回.png) |

### 1.4、案例结论

`mysql`的表级锁有两种模式：

- 表共享读锁（Table Read Lock）
- 表独占写锁（Table Write Lock）

| 锁类型 | 可否兼容 | 读锁 | 写锁 |
| ------ | -------- | ---- | ---- |
| 读锁   | 是       | 是   | 否   |
| 写锁   | 是       | 否   | 否   |

结论：

结合上表，所以对`MyISAM`表进行操作，会有以下情况：

1、对`MyISAM`表的读操作（加读锁），不会阻塞其他进程对同一表的读请求，但会阻塞对同一表的写请求。只有当读锁释放后，才会执行其他进程的写操作。

2、对`MyISAM`表的写操作（加写锁），会阻塞其他进程对同一表的读和写操作，只有当写锁释放后，才会执行其他进程的读写操作。

**简而言之，就是读锁会阻塞写，但是不会堵塞读。而写锁则会把读和写都阻塞。**

### 1.5、表锁分析

`Table_locks_immediate`：产生表级锁定的次数，表示可以立即获取锁的查询次数，每次获取锁+1；

`Table_locks_waited`：出现表级锁定争用而发生等待的次数（不能立即获取锁的次数，每等待一次+1），此值高则说明存在着较严重的表级锁争用情况

```mysql
mysql> show status like'table%';
+-----------------------+---------+
| Variable_name         | Value   |
+-----------------------+---------+
| Table_locks_immediate | 2500752 |
| Table_locks_waited    | 2       |
+-----------------------+---------+
```

**此外，`MyISAM`的读写锁调度是写优先，这也是`MyISAM`不适合做写为主表的引擎。因为写锁后，其他线程不能做任何操作，大量的更新会使查询很难得到锁，从而造成永远阻塞。**

## 2、行锁（偏写）

偏向InnoDB存储引擎，开销大，加锁慢；会出现死锁；锁定粒度最小，发生锁冲突的概率最低，并发度也最高。

InnoDB与MyISAM的最大不同有两点：一是支持事务（TRANSACTION）;二是采用了行级锁

### 2.1、事务

**是由一组SQL语句组成的逻辑处理单元，事务具有以下4个属性，通常简称为事务的ACID属性。**

- **原子性（Atomicity）**：事务是一个原子操作单元，其对数据的修改，要么全都执行，要么全都不执行。
- **一致性（Consistent）**：在事务开始和完成时，数据都必须保持一致状态。这意味着所有相关的数据规则都必须应用于事务的修改，以保持数据的完整性；事务结束时，所有的内部数据结构（如B树索引或双向链表）也都必须正确。（比如：10个人的账号金额总数不变。A账号往B账号里转5000，这时候数据库要执行两行代码：A：减去5000，B：加上5000。在执行完A的时候，这时候数据是不满足一致性条件的！必须要执行完第二行代码，数据才恢复到一致性的状态！）
- **隔离性（Isolation）**：数据库系统提供一定的隔离机制，保证事务在不受外部并发操作影响的“独立”环境执行。这意味着事务处理过程中的中间状态对外部是不可见的，反之亦然。
- **持久性（Durable）**：事务完成之后，它对于数据的修改是永久性的，即使出现系统故障也能够保持。

**并发事务处理带来的问题：**

- **更新丢失（Lost Update）**：当两个或者多个事务选择同一行，然后基于最初选定的值进行更新操作时，由于每个事务都不知道其他事务的存在，则会发生丢失更新问题，即最后的更新并覆盖了前一个程序员所做的更改。

- **脏读（Dirty Reads）**：一个事务正在对一条记录做修改，在这个事务完成并提交之前，这条记录的数据处于不一致状态；此时，另一个事务也来读取同一条记录，如果不加控制，第二个事务读取了这些“脏”数据，并做进一步的处理，就会产生未提交的数据依赖。

  一句话：事务A读取到了事务B已修改但尚未提交的数据，还在这个数据基础上做了操作。此时，如果事务B回滚，A读取到的数据无效，不符合一致性要求。

- **不可重复读（Non-Repeatable Reads）**：不可重复读是指在事务1内，读取了一个数据，事务1还没有结束时，事务2也访问了这个数据，修改了这个数据，并提交。紧接着，事务1又读这个数据。由于事务2的修改，那么事务1两次读到的的数据可能是不一样的，因此称为是不可重复读。
- **幻读（Phantom Reads）**：所谓幻读，指的是当某个事务在读取某个范围内的记录时，另外一个事务又在该范围内插入了新的记录，当之前的事务再次读取该范围的记录时，会产生幻行。

**事务隔离级别**：show variables like 'tx_isolation'

| 读数据一致性及允许的并发副作用隔离级别 | 读数据一致性                             | 脏读 | 不可重复读 | 幻读 |
| -------------------------------------- | ---------------------------------------- | ---- | ---------- | ---- |
| 读未提交（read-uncommitted）           | 最低级别，只能保证不读取物理上损坏的数据 | 是   | 是         | 是   |
| 读已提交（read-committed）             | 语句级                                   | 否   | 是         | 是   |
| 可重复读（repeatable-read）            | 事务级                                   | 否   | 否         | 是   |
| 串行化（serializable）                 | 最高级别，事务级                         | 否   | 否         | 否   |

### 2.2、建表sql

```mysql
CREATE TABLE `test_innodb_lock`  (
  `a` int(11) NULL DEFAULT NULL,
  `b` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  INDEX `test_innodb_a_ind`(`a`) USING BTREE,
  INDEX `test_innodb_lock_b_ind`(`b`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of test_innodb_lock
-- ----------------------------
INSERT INTO `test_innodb_lock` VALUES (1, 'b2');
INSERT INTO `test_innodb_lock` VALUES (3, '3');
INSERT INTO `test_innodb_lock` VALUES (4, '4000');
INSERT INTO `test_innodb_lock` VALUES (5, '5000');
INSERT INTO `test_innodb_lock` VALUES (6, '6000');
INSERT INTO `test_innodb_lock` VALUES (7, '7000');
INSERT INTO `test_innodb_lock` VALUES (8, '8000');
INSERT INTO `test_innodb_lock` VALUES (9, '9000');
INSERT INTO `test_innodb_lock` VALUES (1, 'b1');
```

### 2.3、行锁演示

| session-1                                                    | session-2                                                    |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| ![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-1设置不自动提交.png) | ![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-2设置不自动提交.png) |
| 更新但是不提交，没有手写commit<br />![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-1更新不提交.png) | session-2被阻塞，只能等待<br />![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-2被阻塞等待.png) |
| 提交更新<br />![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-1提交更新.png) | 解除阻塞，更新正常进行<br />![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-2解除阻塞.png) |
|                                                              | commit命令执行                                               |

**无索引行锁升级为表锁**：varchar 不用 引号，导致系统自动转换类型，行锁变表锁。

### 2.4、间隙锁危害

| session-1                                                    | session-2                                                    |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| ![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-1见习锁修改操作.png) | 阻塞产生，暂时不能插入<br />![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-2间隙锁插入阻塞.png) |
| commit                                                       | 阻塞解除，完成插入<br />![](https://github.com/jackhusky/doc/tree/master/mysql/images/session-2阻塞解除.png) |

**什么是间隙锁**

当我们**范围条件**而不是相等条件检索数据，并请求共享或排他锁时，InnoDB会给符合条件的已有数据记录的索引项加锁；对于键值在条件范围内但并不存在的记录，叫做间隙（GAP），InnoDB也会对这个“间隙”加锁，这种锁机制就是间隙锁（Next-Key锁）。

**危害**

因为Query执行过程中通过范围查找的话，他会锁定这个范围内所有的索引键值，即使这个键值并不存在。

间隙锁有一个比较致命的弱点，就是当锁定一个范围键值之后，即使某些不存在的键值也会被无辜的锁定，而造成在锁定的时候无法插入锁定键值范围内的任何数据。在某些场景下这可能会对性能造成很大的危害。

### 2.5 如何锁定一行

```mysql
mysql> select * from test_innodb_lock where a = 8 for update;
+---+-----+
| a | b   |
+---+-----+
| 8 | 666 |
+---+-----+
1 row in set (0.04 sec)
```

### 2.6、案例结论

Innodb存储引擎由于实现了行级锁定，虽然在锁定机制的实现方面所带来的性能损耗可能比表级锁定会要更高一些，但是在整体并发处理能力要远远优于MyISAM的表级锁定。当系统并发量较高的时候，InnoDB的整体性能和MyISAM相比就会有比较明显的优势了。但是在当使用不当的时候，可能会让InnoDB的整体性能表现不仅不能比MyISAM高，甚至可能会更差。

### 2.7、行锁分析

```mysql
mysql> show status like 'innodb_row_lock%';
+-------------------------------+--------+
| Variable_name                 | Value  |
+-------------------------------+--------+
| Innodb_row_lock_current_waits | 0      |
| Innodb_row_lock_time          | 214549 |
| Innodb_row_lock_time_avg      | 30649  |
| Innodb_row_lock_time_max      | 51189  |
| Innodb_row_lock_waits         | 7      |
+-------------------------------+--------+
```

`Innodb_row_lock_current_waits`：当前正在等待锁定的数量；

**`Innodb_row_lock_time`：从系统启动到现在锁定总时间长度；**

**`Innodb_row_lock_time_avg`：每次等待锁花平均时间；**

`Innodb_row_lock_time_max`：从系统启动到现在等待最长的一次所花时间；

**`Innodb_row_lock_waits`：系统启动后到现在总共等待的次数；**

# 四、主从复制

## 1、复制的基本原理

- master将改变记录到二进制日志（binary log），这些记录过程叫做二进制日志事件，binary log events。

- slave将master的binary log events拷贝到它的中继日志（relay log）。

- slave重做中继日志中的事件，将改变应用到自己的数据库。mysql复制是异步的且串行化的。

![](https://github.com/jackhusky/doc/tree/master/mysql/images/mysql主从原理图.jpg)

## 2、复制的基本原则

- 每个slave只有一个master
- 每个slave只能有一个唯一的服务器ID
- 每个master可以有多个slave

## 3、复制的问题

- 主库宕机，数据可能丢失；

- 主从延时：主库写并发执行，但是从库同步主库数据的过程是串行化的，所以会有主从延时的问题。

**解决方案**

- 半同步复制：解决主库数据丢失问题

​       主库写入binlog日志之后，就会将强制此时立即将数据同步到从库， 从库将日志写入自己本地的relay log之后，接着会返回一个ack给主库，主库接收到至少一个从库的ack之后才会认为写操作完成了

- 并行复制：解决主从同步延时问题

  从库开启多个线程，并行读取relay log中不同库的日志，然后并行重放不同库的日志，这是库级别的并行

## 4、一主一从常见配置

mysql版本一致且后台以服务运行，主从都配置在【mysqld】结点下，都是小写

### 4.1、修改主机my.cnf

1、【必须】主服务器唯一ID

2、【必须】启用二进制日志

3、【可选】启动错误日志

4、【可选】根目录

5、【可选】临时目录

6、【可选】数据目录

7、read-only=0读写都可以

8、【可选】设置不要复制的数据库

9、【可选】设置需要复制的数据

```shell
server-id       = 1
log-bin=mysql-bin
```

### 4.2、从机修改my.cnf

1、【必须】从服务器唯一ID

2、【可选】启用二进制文件

```shell
server-id       = 2
log-bin=mysql-bin
```

### 4.3、重启服务，关闭防火墙

### 4.4、在主机上建立账户并授权slave

```mysql
GRANT REPLICATION SLAVE  ON*.* TO 'root'@'192.168.44.130' IDENTIFIED BY '123456';
flush privileges;
```

**记录下File和Position的值**

```mysql
mysql> show master status;
+------------------+-----------+--------------+------------------+
| File             | Position  | Binlog_Do_DB | Binlog_Ignore_DB |
+------------------+-----------+--------------+------------------+
| mysql-bin.000011 | 799314740 |              |                  |
+------------------+-----------+--------------+------------------+
1 row in set (0.00 sec)
```

**执行完此步骤后不再执行主服务器MySQL，防止主服务器状态值变化**

### 4.5、在从机上配置需要复制的主机

```mysql
mysql> CHANGE MASTER TO MASTER_HOST='192.168.44.130',MASTER_USER='root',MASTER_PASSWORD='123456',MASTER_LOG_FILE='mysql-bin.000011',MASTER_LOG_POS=799314740;
Query OK, 0 rows affected (0.01 sec)
```

**启动从服务器复制功能**

```mysql
mysql> start slave;
Query OK, 0 rows affected (0.00 sec)
```

**show slave status\G**，`Slave_IO_Running`，`Slave_SQL_Running`为yes成功

```mysql
mysql> show slave status\G
*************************** 1. row ***************************
               Slave_IO_State: Waiting for master to send event
                  Master_Host: 192.168.44.130
                  Master_User: root
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000011
          Read_Master_Log_Pos: 799314740
               Relay_Log_File: slaver-relay-bin.000002
                Relay_Log_Pos: 253
        Relay_Master_Log_File: mysql-bin.000011
             Slave_IO_Running: Yes
            Slave_SQL_Running: Yes
              Replicate_Do_DB: 
          Replicate_Ignore_DB: 
           Replicate_Do_Table: 
       Replicate_Ignore_Table: 
      Replicate_Wild_Do_Table: 
  Replicate_Wild_Ignore_Table: 
                   Last_Errno: 0
                   Last_Error: 
                 Skip_Counter: 0
          Exec_Master_Log_Pos: 799314740
              Relay_Log_Space: 410
              Until_Condition: None
               Until_Log_File: 
                Until_Log_Pos: 0
           Master_SSL_Allowed: No
           Master_SSL_CA_File: 
           Master_SSL_CA_Path: 
              Master_SSL_Cert: 
            Master_SSL_Cipher: 
               Master_SSL_Key: 
        Seconds_Behind_Master: 0
Master_SSL_Verify_Server_Cert: No
                Last_IO_Errno: 0
                Last_IO_Error: 
               Last_SQL_Errno: 0
               Last_SQL_Error: 
  Replicate_Ignore_Server_Ids: 
             Master_Server_Id: 1
1 row in set (0.00 sec)
```

### 4.6、如何停止从服务复制功能

```mysql
stop slave;
```

