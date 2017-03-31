package com.facebook.executor.handlerExecutor;

/**
 * Created by Administrator on 2017/3/29 0029.
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 一个使当前线程转换为Handler所在的线程的ExecutorService 接口
 * 常用的实现有{@link UiThreadImmediateExecutorService}将当前线程转到Ui线程
 * An {@link ExecutorService} that is backed by a handler.
 */
public interface HandlerExecutorService extends ScheduledExecutorService {

    /**
     * 关闭Handler
     */
    void quit();

    /**
     * 检查我们目前是不是在Handler的线程在这个HandlerExecutorService中
     * Check if we are currently in the handler thread of this HandlerExecutorService.
     */
    boolean isHandlerThread();
}
