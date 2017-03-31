package com.facebook.pool.poolParams;

/**
 * Created by heshixiyang on 2017/3/17.
 */

import android.util.SparseIntArray;

import com.facebook.common.s.ByteConstants;
import com.facebook.pool.BitmapPool;

/**
 * 提供一个parameters给{@link BitmapPool}
 * Provides pool parameters for {@link BitmapPool}
 */
public class DefaultBitmapPoolParams {
    /**
     * We are not reusing Bitmaps and want to free them as soon as possible.
     */
    private static final int MAX_SIZE_SOFT_CAP = 0;

    private DefaultBitmapPoolParams() {
    }

    /**
     * Our Bitmaps live in ashmem, meaning that they are pinned in androids' shared native memory.
     * Therefore, we are not constrained by the max heap size of the dalvik heap, but we want to make
     * sure we don't use too much memory on low end devices, so that we don't force other background
     * process to be evicted.
     */
    private static int getMaxSizeHardCap() {
        final int maxMemory = (int)Math.min(Runtime.getRuntime().maxMemory(), Integer.MAX_VALUE);
        if (maxMemory > 16 * ByteConstants.MB) {
            return maxMemory / 4 * 3;
        } else {
            return maxMemory / 2;
        }
    }

    /**
     * This will cause all get/release calls to behave like alloc/free calls i.e. no pooling.
     */
    private static final SparseIntArray DEFAULT_BUCKETS = new SparseIntArray(0);

    public static PoolParams get() {
        return new PoolParams(
                MAX_SIZE_SOFT_CAP,
                getMaxSizeHardCap(),
                DEFAULT_BUCKETS
        );
    }
}
