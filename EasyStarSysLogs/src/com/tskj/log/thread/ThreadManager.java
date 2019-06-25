package com.tskj.log.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author JRX
 * @Description: 单例FixedThreadPool线程池的构建
 * @create 2018/12/13 10:51
 * @Param
 * @return
 **/
public class ThreadManager {
    // volatile  避免了无序性，防止指令重排造成没有正确判断
    private volatile static ThreadPool mThreadPool;


    // 获取单例的线程池对象
    public static ThreadPool getThreadPool() {
        if (mThreadPool == null) {
            synchronized (ThreadManager.class) {
                if (mThreadPool == null) {
                    // TODO 获取线程数 可能会改变成定值 不通過cpu获取
                    int cpuNum = Runtime.getRuntime().availableProcessors();// 获取处理器数量
                    System.err.println("处理器数量:" + cpuNum);
                    int threadNum = cpuNum * 2 + 1;// 根据cpu数量,计算出合理的线程并发数
                    System.err.println("threadNum并发数：" + threadNum);
                    mThreadPool = new ThreadPool(threadNum, threadNum, 0L);
                }
            }
        }
        return mThreadPool;
    }

    public static class ThreadPool {

        private ThreadPoolExecutor mexecutor;
        private int corepoolsize;
        private int maximumpoolsize;
        private long keepalivetime;

        private ThreadPool(int corepoolsize, int maximumpoolsize, long keepalivetime) {
            this.corepoolsize = corepoolsize;
            this.maximumpoolsize = maximumpoolsize;
            this.keepalivetime = keepalivetime;
        }

        public void executor(Runnable runnable) {

            if (runnable == null) {
                return;
            }
            if (mexecutor == null) {
                mexecutor = new ThreadPoolExecutor(corepoolsize, //核心线程数
                        maximumpoolsize, //最大线程数
                        keepalivetime, //闲置线程存活时间
                        TimeUnit.MILLISECONDS, // 时间单位
                        new LinkedBlockingDeque<Runnable>(), //线程队列
                        Executors.defaultThreadFactory(), //线程工厂
                        new ThreadPoolExecutor.AbortPolicy() //队列已满，而且当前线程数已经超过最大线程数时的异常处理策略
                );
            }
            mexecutor.execute(runnable);
        }

        public void cancel(Runnable runnable) {
            if (mexecutor != null) {
                mexecutor.getQueue().remove(runnable);
            }
        }

        /*
         * 判断是否是最后一个任务
         */
        protected boolean isTaskEnd() {
            if (mexecutor.getActiveCount() == 0) {
                return true;
            } else {
                return false;
            }
        }

        /*
         * 获取缓存大小
         */
        public int getQueue() {
            return mexecutor.getQueue().size();
        }

        /*
         * 获取线程池中的线程数目
         */
        public int getPoolSize() {
            return mexecutor.getPoolSize();
        }

        /*
         * 获取已完成的任务数
         */
        public long getCompletedTaskCount() {
            return mexecutor.getCompletedTaskCount();
        }
    }
}
