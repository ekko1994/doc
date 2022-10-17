package com.ekko.juc.cf;

import java.util.concurrent.*;

/**
 * @ClassName FutureTaskDemo
 * @Description FutureTaskDemo 测试
 * @Author zhanghao
 * @Create 2022年10月17日 15:45:36
 */
public class FutureTaskDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        //method1();
        method2();

    }

    private static void method2() throws InterruptedException, ExecutionException {
        FutureTask<Integer> futureTask = new FutureTask<>(() ->{
            System.out.println(Thread.currentThread().getName() + "\t come in");
            try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) {e.printStackTrace();}
            return 10;
        });

        //new Thread(futureTask,"t1").start();
        Executors.newSingleThreadExecutor().submit(futureTask,new Integer(0));

        while (true){
            if (futureTask.isDone()) {
                System.out.println("result = " + futureTask.get());
                break;
            }
            System.out.println("还在计算中！");
        }
    }

    /**
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    private static void method1() throws InterruptedException, ExecutionException, TimeoutException {
        FutureTask<Integer> futureTask = new FutureTask<>(() -> {
            System.out.println(Thread.currentThread().getName() + "\t ----come in");
            try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) {e.printStackTrace();}
            return 10;
        });

        new Thread(futureTask,"t1").start();
        //不见不散。出现get方法，不管是否计算完成都阻塞等待结果出来再运行
        //System.out.println(futureTask.get());

        //过时不候。3秒钟才出现结果，我只等1秒钟
        System.out.println(Thread.currentThread().getName() + "\t" + futureTask.get(1L,TimeUnit.SECONDS));
    }
}
