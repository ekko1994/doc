# 一、索引优化分析

##1、性能下降SQL慢，执行时间长，等待时间长？

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

InnoDB存储引擎中页的大小为16KB，一般表的主键类型为INT（占用4个字节）或BIGINT（占用8个字节），指针类型也一般为4或8个字节，也就是说一个页（B+Tree中的一个节点）中大概存储16KB/(8B+8B)=1K个键值（因为是估值，为方便计算，这里的K取值为〖10〗^3）。也就是说一个深度为3的B+Tree索引可以维护10^3 * 10^3 * 10^3 = 10亿 条记录。

实际情况中每个节点可能不能填充满，因此在数据库中，B+Tree的高度一般都在2~4层。`mysql`的`InnoDB`存储引擎在设计时是将根节点常驻内存的，也就是说查找某一键值的行记录时最多只需要1~3次磁盘I/O操作。

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

#### 5.2.2、==最佳左前缀法则==

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

==使用覆盖索引解决==，查询的列要被建的索引包含，可以不完全包含。

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

