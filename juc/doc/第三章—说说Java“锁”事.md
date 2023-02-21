# 说说Java“锁”事

## 1. 从轻松的乐观锁和悲观锁开讲

### 1、悲观锁

认为自己在使用数据的时候一定有别的线程来修改数据，因此在获取数据的时候会先加锁，确保数据不会被别的线程修改。

`synchronized`关键字和`Lock`的实现类都是悲观锁。

- 适合写操作多的场景，先加锁可以保证写操作时数据正确。

- 显式的锁定之后再操作同步资源。

```java
 
//=============悲观锁的调用方式
public synchronized void m1()
{
    //加锁后的业务逻辑......
}

// 保证多个线程使用的是同一个lock对象的前提下
ReentrantLock lock = new ReentrantLock();
public void m2() {
    lock.lock();
    try {
        // 操作同步资源
    }finally {
        lock.unlock();
    }
}

//=============乐观锁的调用方式
// 保证多个线程使用的是同一个AtomicInteger
private AtomicInteger atomicInteger = new AtomicInteger();
atomicInteger.incrementAndGet();
 
```

### 2、乐观锁

适合读操作多的场景，不加锁的特点能够使其读操作的性能大幅提升。

乐观锁则直接去操作同步资源，是一种无锁算法，得之我幸不得我命，再抢

乐观锁一般有两种实现方式：

1、采用版本号机制

2、CAS（Compare-and-Swap，即比较并替换）算法实现

## 2. 通过8种情况演示锁运行案例，看看我们到底锁的是什么