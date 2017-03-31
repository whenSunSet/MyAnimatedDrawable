package com.facebook.executor.executorSupplier;

/**
 * Created by heshixiyang on 2017/3/30.
 */

import android.os.Process;

import java.util.concurrent.ThreadFactory;

/**
 * ThreadFactory that applies a priority to the threads it creates.
 */
public class PriorityThreadFactory implements ThreadFactory {

    private final int mThreadPriority;

    /**
     * Creates a new PriorityThreadFactory with a given priority.
     *
     * <p>This value should be set to a value compatible with
     * {@link android.os.Process#setThreadPriority}, not {@link Thread#setPriority}.
     *
     */
    public PriorityThreadFactory(int threadPriority) {
        mThreadPriority = threadPriority;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        Runnable wrapperRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(mThreadPriority);
                } catch (Throwable t) {
                    // just to be safe
                }
                runnable.run();
            }
        };
        return new Thread(wrapperRunnable);
    }

}
