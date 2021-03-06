package com.facebook.pool;

/**
 * Created by heshixiyang on 2017/3/31.
 */

import android.util.SparseIntArray;

import com.facebook.common.s.Preconditions;
import com.facebook.imagepipeline.memory.NativeMemoryChunk;
import com.facebook.pool.poolParams.PoolParams;
import com.facebook.pool.poolUtil.PoolStatsTracker;
import com.facebook.trimmable.MemoryTrimmableRegistry;

import javax.annotation.concurrent.ThreadSafe;

/**
 * 一个本地内存块{@link NativeMemoryChunk}的Pool
 * Manages a pool of native memory chunks ({@link NativeMemoryChunk})
 */
@ThreadSafe
public class NativeMemoryChunkPool extends BasePool<NativeMemoryChunk> {
    private final int[] mBucketSizes;

    /**
     * Creates a new instance of the NativeMemoryChunkPool class
     * @param memoryTrimmableRegistry the memory manager to register with
     * @param poolParams provider for pool parameters
     * @param nativeMemoryChunkPoolStatsTracker
     */
    public NativeMemoryChunkPool(
            MemoryTrimmableRegistry memoryTrimmableRegistry,
            PoolParams poolParams,
            PoolStatsTracker nativeMemoryChunkPoolStatsTracker) {
        super(memoryTrimmableRegistry, poolParams, nativeMemoryChunkPoolStatsTracker);
        SparseIntArray bucketSizes = poolParams.bucketSizes;
        mBucketSizes = new int[bucketSizes.size()];
        for (int i = 0; i < mBucketSizes.length; ++i) {
            mBucketSizes[i] = bucketSizes.keyAt(i);
        }
        initialize();
    }

    /**
     * Gets the smallest size supported by the pool
     * @return the smallest size supported by the pool
     */
    public int getMinBufferSize() {
        return mBucketSizes[0];
    }

    /**
     * Allocate a native memory chunk larger than or equal to the specified size
     * @param bucketedSize size of the buffer requested
     * @return a native memory chunk of the specified or larger size. Null if the size is invalid
     */
    @Override
    protected NativeMemoryChunk alloc(int bucketedSize) {
        return new NativeMemoryChunk(bucketedSize);
    }

    /**
     * Frees the 'value'
     * @param value the value to free
     */
    @Override
    protected void free(NativeMemoryChunk value) {
        Preconditions.checkNotNull(value);
        value.close();
    }

    /**
     * Gets the size in bytes for the given 'bucketed' size
     * @param bucketedSize the bucketed size
     * @return size in bytes
     */
    @Override
    protected int getSizeInBytes(int bucketedSize) {
        return bucketedSize;
    }

    /**
     * Get the 'bucketed' size for the given request size. The 'bucketed' size is a size that is
     * the same or larger than the request size. We walk through our list of pre-defined bucket
     * sizes, and use that to determine the smallest bucket size that is larger than the requested
     * size.
     * If no such 'bucketedSize' is found, then we simply return "requestSize"
     * @param requestSize the logical request size
     * @return the bucketed size
     * @throws InvalidSizeException, if the requested size was invalid
     */
    @Override
    protected int getBucketedSize(int requestSize) {
        int intRequestSize = requestSize;
        if (intRequestSize <= 0) {
            throw new InvalidSizeException(requestSize);
        }

        // find the smallest bucketed size that is larger than the requested size
        for (int bucketedSize : mBucketSizes) {
            if (bucketedSize >= intRequestSize) {
                return bucketedSize;
            }
        }

        // requested size doesn't match our existing buckets - just return the requested size
        // this will eventually translate into a plain alloc/free paradigm
        return requestSize;
    }

    /**
     * Gets the bucketed size of the value
     * @param value the value
     * @return just the length of the value
     */
    @Override
    protected int getBucketedSizeForValue(NativeMemoryChunk value) {
        Preconditions.checkNotNull(value);
        return value.getSize();
    }

    /**
     * Checks if the value is reusable (for subseequent {@link #get(int)} operations.
     * The value is reusable, if
     *  - it hasn't already been freed
     * @param value the value to test for reusability
     * @return true, if the value is reusable
     */
    @Override
    protected boolean isReusable(NativeMemoryChunk value) {
        Preconditions.checkNotNull(value);
        return !value.isClosed();
    }
}
