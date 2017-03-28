package com.facebook.factoryAndProvider.animatedFactory.animatedImageFactory.animatedImage;

/**
 * Created by Administrator on 2017/3/28 0028.
 */

import android.graphics.Bitmap;

import com.facebook.references.CloseableReference;
import com.facebook.util.Preconditions;

import java.util.List;

import javax.annotation.Nullable;

/**
 * 解码一个animated image的结果，包含{@link AnimatedImage}以及额外的数据
 * The result of decoding an animated image. Contains the {@link AnimatedImage} as well as
 * additional data.
 */
public class AnimatedImageResult {

    private final AnimatedImage mImage;
    private final int mFrameForPreview;
    private @Nullable
    CloseableReference<Bitmap> mPreviewBitmap;
    private @Nullable
    List<CloseableReference<Bitmap>> mDecodedFrames;

    AnimatedImageResult(AnimatedImageResultBuilder builder) {
        mImage = Preconditions.checkNotNull(builder.getImage());
        mFrameForPreview = builder.getFrameForPreview();
        mPreviewBitmap = builder.getPreviewBitmap();
        mDecodedFrames = builder.getDecodedFrames();
    }

    private AnimatedImageResult(AnimatedImage image) {
        mImage = Preconditions.checkNotNull(image);
        mFrameForPreview = 0;
    }

    /**
     * 创建一个没有额外数据的{@link AnimatedImageResult}
     * Creates an {@link AnimatedImageResult} with no additional options.
     *
     * @param image the image
     * @return the result
     */
    public static AnimatedImageResult forAnimatedImage(AnimatedImage image) {
        return new AnimatedImageResult(image);
    }

    /**
     * 创建一个{@link AnimatedImageResultBuilder}给一个{@link AnimatedImageResult}
     * Creates an {@link AnimatedImageResultBuilder} for creating an {@link AnimatedImageResult}.
     *
     * @param image the image
     * @return the builder
     */
    public static AnimatedImageResultBuilder newBuilder(AnimatedImage image) {
        return new AnimatedImageResultBuilder(image);
    }

    /**
     * 获取image
     * Gets the underlying image.
     *
     * @return the underlying image
     */
    public AnimatedImage getImage() {
        return mImage;
    }

    /**
     * Gets the frame that should be used for the preview image. If the preview bitmap was fetched,
     * this is the frame that it's for.
     *
     * @return the frame that should be used for the preview image
     */
    public int getFrameForPreview() {
        return mFrameForPreview;
    }

    /**
     * Gets a decoded frame. This will only return non-null if the {@code ImageDecodeOptions}
     * were configured to decode all frames at decode time.
     *
     * @param index the index of the frame to get
     * @return a reference to the preview bitmap which must be released by the caller when done or
     *     null if there is no preview bitmap set
     */
    public synchronized @Nullable CloseableReference<Bitmap> getDecodedFrame(int index) {
        if (mDecodedFrames != null) {
            return CloseableReference.cloneOrNull(mDecodedFrames.get(index));
        }
        return null;
    }

    /**
     * Gets whether it has the decoded frame. This will only return true if the
     * {@code ImageDecodeOptions} were configured to decode all frames at decode time.
     *
     * @param index the index of the frame to get
     * @return true if the result has the decoded frame
     */
    public synchronized boolean hasDecodedFrame(int index) {
        return mDecodedFrames != null && mDecodedFrames.get(index) != null;
    }

    /**
     * Gets the bitmap for the preview frame. This will only return non-null if the
     * {@code ImageDecodeOptions} were configured to decode the preview frame.
     *
     * @return a reference to the preview bitmap which must be released by the caller when done or
     *     null if there is no preview bitmap set
     */
    public synchronized CloseableReference<Bitmap> getPreviewBitmap() {
        return CloseableReference.cloneOrNull(mPreviewBitmap);
    }

    /**
     * Disposes the result, which releases the reference to any bitmaps.
     */
    public synchronized void dispose() {
        CloseableReference.closeSafely(mPreviewBitmap);
        mPreviewBitmap = null;
        CloseableReference.closeSafely(mDecodedFrames);
        mDecodedFrames = null;
    }
}