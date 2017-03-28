package com.facebook.executor;

/**
 * Created by Administrator on 2017/3/28 0028.
 */

import java.util.concurrent.ExecutorService;

/**
 * 串行执行任务的接口，他的任务遵循先进先出
 */
public interface SerialExecutorService extends ExecutorService {
}
