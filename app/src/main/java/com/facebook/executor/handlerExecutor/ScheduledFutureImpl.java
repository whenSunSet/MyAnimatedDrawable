package com.facebook.executor.handlerExecutor;

/**
 * Created by Administrator on 2017/3/29 0029.
 */

import android.os.Handler;

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

/**
 * 一个被使用在{@link HandlerExecutorServiceImpl}上的{@link ScheduledFuture}
 * A {@link ScheduledFuture} for {@link HandlerExecutorServiceImpl}.
 */
public class ScheduledFutureImpl<V> implements RunnableFuture<V>, ScheduledFuture<V> {

    private final Handler mHandler;
    private final FutureTask<V> mListenableFuture;

    public ScheduledFutureImpl(Handler handler, Callable<V> callable) {
        mHandler = handler;
        mListenableFuture = new FutureTask<V>(callable);
    }

    public ScheduledFutureImpl(Handler handler, Runnable runnable, @Nullable V result) {
        mHandler = handler;
        mListenableFuture = new FutureTask<V>(runnable, result);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Delayed other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void run() {
        mListenableFuture.run();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return mListenableFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return mListenableFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return mListenableFuture.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return mListenableFuture.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return mListenableFuture.get(timeout, unit);
    }
}
