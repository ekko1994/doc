# Java Api

## NIO

Java NIO系统的核心在于：通道(Channel)和缓冲区 (Buffer)。通道表示打开到 IO 设备(例如：文件、 套接字)的连接。若需要使用 NIO 系统，需要获取 用于连接 IO 设备的通道以及用于容纳数据的缓冲 区。然后操作缓冲区，对数据进行处理。简而言之，Channel 负责传输， Buffer 负责存储。

Buffer 中的重要概念：

- 容量 (capacity) ：表示 Buffer 最大数据容量，缓冲区容量不能为负，并且创 建后不能更改。
- 限制 (limit)：第一个不应该读取或写入的数据的索引，即位于 limit 后的数据 不可读写。缓冲区的限制不能为负，并且不能大于其容量。 
- 位置 (position)：下一个要读取或写入的数据的索引。缓冲区的位置不能为 负，并且不能大于其限制 。
- 标记 (mark)与重置 (reset)：标记是一个索引，通过 Buffer 中的 mark() 方法 指定 Buffer 中一个特定的 position，之后可以通过调用 reset() 方法恢复到这 个 position.