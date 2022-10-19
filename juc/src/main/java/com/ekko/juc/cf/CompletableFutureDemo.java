package com.ekko.juc.cf;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName CompletableFutureDemo
 * @Description CompletableFuture 测试
 * @Author zhanghao
 * @Create 2022年10月19日 10:01:17
 */
public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //m1();
        //m2();
        m3();
    }

    private static void m3() {
        CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t ---come in");
            int result = ThreadLocalRandom.current().nextInt(10);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("-----计算结束耗时1秒钟，result: " + result);
            if (result > 6) {
                int age = 10 / 0;
            }
            return result;
        }).whenComplete((v, e) -> {
            if (null == e) {
                System.out.println("-----result: " + v);
            }
        }).exceptionally(e -> {
            System.out.println("----exception: " + e.getCause() + "\t" + e.getMessage());
            return -1;
        });
        //主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭:暂停3秒钟线程
        try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) {e.printStackTrace();}
    }


    /**
     * 有返回值
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private static void m2() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t ----come in");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 28;
        });
        System.out.println(future.get());
    }

    /**
     * 无返回值
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private static void m1() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t ----come in");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("----task over");

        });
        System.out.println(future.get());
    }
}
