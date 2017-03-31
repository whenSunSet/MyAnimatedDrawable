package com.facebook.pool;

/**
 * Created by heshixiyang on 2017/3/31.
 */

import android.support.annotation.VisibleForTesting;

import com.facebook.common.s.Preconditions;
import com.facebook.pool.poolParams.PoolParams;
import com.facebook.pool.poolUtil.NoOpPoolStatsTracker;
import com.facebook.pool.poolUtil.PoolStatsTracker;
import com.facebook.references.CloseableReference;
import com.facebook.references.OOMSoftReference;
import com.facebook.references.ResourceReleaser;
import com.facebook.trimmable.MemoryTrimmableRegistry;

import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

/**
 * 一个特别的byte-array pool，只使分配极小的内存
 * A special byte-array pool designed to minimize allocations.
 *
 * <p>The length of each bucket's free list is capped at the number of threads using the pool.
 * <p>The free list of each bucket uses {@link OOMSoftReference}s.
 */
@ThreadSafe
public class FlexByteArrayPool {

    private final ResourceReleaser<byte[]> mResourceReleaser;
    @VisibleForTesting
    final SoftRefByteArrayPool mDelegatePool;

    public FlexByteArrayPool(
            MemoryTrimmableRegistry memoryTrimmableRegistry,
            PoolParams params) {
        Preconditions.checkArgument(params.maxNumThreads > 0);
        mDelegatePool = new SoftRefByteArrayPool(
                memoryTrimmableRegistry,
                params,
                NoOpPoolStatsTracker.getInstance());
        mResourceReleaser = new ResourceReleaser<byte[]>() {
            @Override
            public void release(byte[] unused) {
                FlexByteArrayPool.this.release(unused);
            }
        };
    }

    public CloseableReference<byte[]> get(int size) {
        return CloseableReference.of(mDelegatePool.get(size), mResourceReleaser);
    }

    public void release(byte[] value) {
        mDelegatePool.release(value);
    }

    public Map<String, Integer> getStats() {
        return mDelegatePool.getStats();
    }

    public int getMinBufferSize() {
        return mDelegatePool.getMinBufferSize();
    }

    @VisibleForTesting
    static class SoftRefByteArrayPool extends GenericByteArrayPool {
        public SoftRefByteArrayPool(
                MemoryTrimmableRegistry memoryTrimmableRegistry,
                PoolParams poolParams,
                PoolStatsTracker poolStatsTracker) {
            super(memoryTrimmableRegistry, poolParams, poolStatsTracker);
        }

        @Override
        Bucket<byte[]> newBucket(int bucketedSize) {
            return new OOMSoftReferenceBucket<>(
                    getSizeInBytes(bucketedSize),
                    mPoolParams.maxNumThreads,
                    0);
        }
    }
}
