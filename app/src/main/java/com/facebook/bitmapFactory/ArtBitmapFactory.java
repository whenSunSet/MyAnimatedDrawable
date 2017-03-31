package com.facebook.bitmapFactory;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;

import com.facebook.common.util.BitmapUtil;
import com.facebook.imagepipeline.nativecode.Bitmaps;
import com.facebook.pool.BitmapPool;
import com.facebook.references.CloseableReference;

import javax.annotation.concurrent.ThreadSafe;

/**
 * ART VM的bitmap工厂(对于5.0以下的android 版本)
 * Bitmap factory for ART VM (Lollipop and up).
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@ThreadSafe
public class ArtBitmapFactory extends PlatformBitmapFactory {

    private final BitmapPool mBitmapPool;

    public ArtBitmapFactory(BitmapPool bitmapPool) {
        mBitmapPool = bitmapPool;
    }

    /**
     * Creates a bitmap of the specified width and height.
     * @param width the width of the bitmap
     * @param height the height of the bitmap
     * @param bitmapConfig the {@link Bitmap.Config}
     * used to create the decoded Bitmap
     * @return a reference to the bitmap
     * @exception OutOfMemoryError if the Bitmap cannot be allocated
     */
    @Override
    public CloseableReference<Bitmap> createBitmapInternal(
            int width,
            int height,
            Bitmap.Config bitmapConfig) {
        int sizeInBytes = BitmapUtil.getSizeInByteForBitmap(width, height, bitmapConfig);
        Bitmap bitmap = mBitmapPool.get(sizeInBytes);
        Bitmaps.reconfigureBitmap(bitmap, width, height, bitmapConfig);
        return CloseableReference.of(bitmap, mBitmapPool);
    }
}
