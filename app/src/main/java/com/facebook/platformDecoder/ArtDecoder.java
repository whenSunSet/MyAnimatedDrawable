package com.facebook.platformDecoder;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pools;

import com.facebook.common.s.Preconditions;
import com.facebook.common.stream.LimitedInputStream;
import com.facebook.common.stream.TailAppendingInputStream;
import com.facebook.common.util.BitmapUtil;
import com.facebook.common.util.JfifUtil;
import com.facebook.image.EncodedImage;
import com.facebook.pool.BitmapPool;
import com.facebook.references.CloseableReference;

import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.annotation.concurrent.ThreadSafe;

/**
 * ART VM 解码bitmap，在5.0以下使用
 * Bitmap decoder for ART VM (Lollipop and up).
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@ThreadSafe
public class ArtDecoder implements PlatformDecoder {

    /**
     * Size of temporary array. Value recommended by Android docs for decoding Bitmaps.
     */
    private static final int DECODE_BUFFER_SIZE = 16 * 1024;

    private final BitmapPool mBitmapPool;

    /**
     * ArtPlatformImageDecoder decodes images from InputStream - to do so we need to provide
     * temporary buffer, otherwise framework will allocate one for us for each decode request
     */
    @VisibleForTesting
    final Pools.SynchronizedPool<ByteBuffer> mDecodeBuffers;

    // TODO (5884402) - remove dependency on JfifUtil
    private static final byte[] EOI_TAIL = new byte[] {
            (byte) JfifUtil.MARKER_FIRST_BYTE,
            (byte) JfifUtil.MARKER_EOI};

    public ArtDecoder(BitmapPool bitmapPool, int maxNumThreads, Pools.SynchronizedPool decodeBuffers) {
        mBitmapPool = bitmapPool;
        mDecodeBuffers = decodeBuffers;
        for (int i = 0; i < maxNumThreads; i++) {
            mDecodeBuffers.release(ByteBuffer.allocate(DECODE_BUFFER_SIZE));
        }
    }

    /**
     * Creates a bitmap from encoded bytes.
     * @param encodedImage the encoded image with a reference to the encoded bytes
     * @param bitmapConfig the {@link Bitmap.Config}
     * used to create the decoded Bitmap
     * @return the bitmap
     * @exception OutOfMemoryError if the Bitmap cannot be allocated
     */
    @Override
    public CloseableReference<Bitmap> decodeFromEncodedImage(
            EncodedImage encodedImage,
            Bitmap.Config bitmapConfig) {
        final BitmapFactory.Options options = getDecodeOptionsForStream(encodedImage, bitmapConfig);
        boolean retryOnFail=options.inPreferredConfig != Bitmap.Config.ARGB_8888;
        try {
            return decodeStaticImageFromStream(encodedImage.getInputStream(), options);
        } catch (RuntimeException re) {
            if (retryOnFail) {
                return decodeFromEncodedImage(encodedImage, Bitmap.Config.ARGB_8888);
            }
            throw re;
        }
    }

    /**
     * Creates a bitmap from encoded JPEG bytes. Supports a partial JPEG image.
     * @param encodedImage the encoded image with reference to the encoded bytes
     * @param bitmapConfig the {@link Bitmap.Config}
     * used to create the decoded Bitmap
     * @param length the number of encoded bytes in the buffer
     * @return the bitmap
     * @exception OutOfMemoryError if the Bitmap cannot be allocated
     */
    @Override
    public CloseableReference<Bitmap> decodeJPEGFromEncodedImage(
            EncodedImage encodedImage,
            Bitmap.Config bitmapConfig,
            int length) {
        boolean isJpegComplete = encodedImage.isCompleteAt(length);
        final BitmapFactory.Options options = getDecodeOptionsForStream(encodedImage, bitmapConfig);

        InputStream jpegDataStream = encodedImage.getInputStream();
        // At this point the InputStream from the encoded image should not be null since in the
        // pipeline,this comes from a call stack where this was checked before. Also this method needs
        // the InputStream to decode the image so this can't be null.
        Preconditions.checkNotNull(jpegDataStream);
        if (encodedImage.getSize() > length) {
            jpegDataStream = new LimitedInputStream(jpegDataStream, length);
        }
        if (!isJpegComplete) {
            jpegDataStream = new TailAppendingInputStream(jpegDataStream, EOI_TAIL);
        }
        boolean retryOnFail=options.inPreferredConfig != Bitmap.Config.ARGB_8888;
        try {
            return decodeStaticImageFromStream(jpegDataStream, options);
        } catch (RuntimeException re) {
            if (retryOnFail) {
                return decodeFromEncodedImage(encodedImage, Bitmap.Config.ARGB_8888);
            }
            throw re;
        }
    }

    protected CloseableReference<Bitmap> decodeStaticImageFromStream(
            InputStream inputStream,
            BitmapFactory.Options options) {
        Preconditions.checkNotNull(inputStream);
        int sizeInBytes = BitmapUtil.getSizeInByteForBitmap(
                options.outWidth,
                options.outHeight,
                options.inPreferredConfig);
        final Bitmap bitmapToReuse = mBitmapPool.get(sizeInBytes);
        if (bitmapToReuse == null) {
            throw new NullPointerException("BitmapPool.get returned null");
        }
        options.inBitmap = bitmapToReuse;

        Bitmap decodedBitmap;
        ByteBuffer byteBuffer = mDecodeBuffers.acquire();
        if (byteBuffer == null) {
            byteBuffer = ByteBuffer.allocate(DECODE_BUFFER_SIZE);
        }
        try {
            options.inTempStorage = byteBuffer.array();
            decodedBitmap = BitmapFactory.decodeStream(inputStream, null, options);
        } catch (RuntimeException re) {
            mBitmapPool.release(bitmapToReuse);
            throw re;
        } finally {
            mDecodeBuffers.release(byteBuffer);
        }

        if (bitmapToReuse != decodedBitmap) {
            mBitmapPool.release(bitmapToReuse);
            decodedBitmap.recycle();
            throw new IllegalStateException();
        }

        return CloseableReference.of(decodedBitmap, mBitmapPool);
    }

    /**
     * Options returned by this method are configured with mDecodeBuffer which is GuardedBy("this")
     */
    private static BitmapFactory.Options getDecodeOptionsForStream(
            EncodedImage encodedImage,
            Bitmap.Config bitmapConfig) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // Sample size should ONLY be different than 1 when downsampling is enabled in the pipeline
        options.inSampleSize = encodedImage.getSampleSize();
        options.inJustDecodeBounds = true;
        // fill outWidth and outHeight
        BitmapFactory.decodeStream(encodedImage.getInputStream(), null, options);
        if (options.outWidth == -1 || options.outHeight == -1) {
            throw new IllegalArgumentException();
        }

        options.inJustDecodeBounds = false;
        options.inDither = true;
        options.inPreferredConfig = bitmapConfig;
        options.inMutable = true;

        return options;
    }
}
