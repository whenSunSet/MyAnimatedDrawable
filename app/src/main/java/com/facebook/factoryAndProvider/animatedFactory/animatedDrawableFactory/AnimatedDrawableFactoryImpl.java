package com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory;

/**
 * Created by Administrator on 2017/3/28 0028.
 */

import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.DisplayMetrics;

import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.AnimatedDrawableBackend;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.provider.AnimatedDrawableBackendProvider;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.AnimatedDrawableCachingBackend;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.provider.AnimatedDrawableCachingBackendImplProvider;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedDrawable.AnimatedDrawable;
import com.facebook.factoryAndProvider.animatedFactory.animatedImageFactory.animatedImage.AnimatedImage;
import com.facebook.factoryAndProvider.animatedFactory.animatedImageFactory.animatedImage.AnimatedImageResult;
import com.facebook.image.CloseableAnimatedImage;
import com.facebook.image.CloseableImage;
import com.facebook.util.AnimatedDrawableUtil;
import com.facebook.util.MonotonicClock;

import java.util.concurrent.ScheduledExecutorService;

/**
 * {@link AnimatedDrawable}.的工厂的实现
 * Factory for instances of {@link AnimatedDrawable}.
 */
public class AnimatedDrawableFactoryImpl implements AnimatedDrawableFactory {

    private final AnimatedDrawableBackendProvider mAnimatedDrawableBackendProvider;
    private final AnimatedDrawableCachingBackendImplProvider mAnimatedDrawableCachingBackendProvider;
    private final AnimatedDrawableUtil mAnimatedDrawableUtil;
    private final ScheduledExecutorService mScheduledExecutorServiceForUiThread;
    private final MonotonicClock mMonotonicClock;
    private final Resources mResources;

    public AnimatedDrawableFactoryImpl(
            AnimatedDrawableBackendProvider animatedDrawableBackendProvider,
            AnimatedDrawableCachingBackendImplProvider animatedDrawableCachingBackendProvider,
            AnimatedDrawableUtil animatedDrawableUtil,
            ScheduledExecutorService scheduledExecutorService,
            Resources resources) {
        mAnimatedDrawableBackendProvider = animatedDrawableBackendProvider;
        mAnimatedDrawableCachingBackendProvider = animatedDrawableCachingBackendProvider;
        mAnimatedDrawableUtil = animatedDrawableUtil;
        mScheduledExecutorServiceForUiThread = scheduledExecutorService;
        mMonotonicClock = new MonotonicClock() {
            @Override
            public long now() {
                // Must be SystemClock.uptimeMillis to be compatible with what Android's View uses.
                return SystemClock.uptimeMillis();
            }
        };
        mResources = resources;
    }

    /**
     * 通过 {@link CloseableImage}CloseableAnimatedImage，创建一个{@link AnimatedDrawable}，其为
     * Creates an {@link AnimatedDrawable} based on an {@link CloseableImage} which should be a
     * CloseableAnimatedImage.
     *
     * @param closeableImage The CloseableAnimatedImage to use for the AnimatedDrawable
     * @return a newly constructed {@link AnimatedDrawable}
     */
    @Override
    public Drawable create(CloseableImage closeableImage) {
        if (closeableImage instanceof CloseableAnimatedImage) {
            final AnimatedImageResult result = ((CloseableAnimatedImage) closeableImage).getImageResult();
            return create(result, AnimatedDrawableOptions.DEFAULTS);
        } else {
            throw new UnsupportedOperationException("Unrecognized image class: " + closeableImage);
        }
    }

    /**
     * 通过{@link AnimatedImage}创建一个{@link AnimatedDrawable}
     * Creates an {@link AnimatedDrawable} based on an {@link AnimatedImage}.
     *
     * @param animatedImageResult the result of the code
     * @param options additional options
     * @return a newly constructed {@link AnimatedDrawable}
     */
    private AnimatedDrawable create(
            AnimatedImageResult animatedImageResult,
            AnimatedDrawableOptions options) {
        AnimatedImage animatedImage = animatedImageResult.getImage();
        Rect initialBounds = new Rect(0, 0, animatedImage.getWidth(), animatedImage.getHeight());
        AnimatedDrawableBackend animatedDrawableBackend =
                mAnimatedDrawableBackendProvider.get(animatedImageResult, initialBounds);
        return createAnimatedDrawable(options, animatedDrawableBackend);
    }

    private AnimatedImageResult getImageIfCloseableAnimatedImage(CloseableImage image) {
        if (image instanceof CloseableAnimatedImage) {
            return ((CloseableAnimatedImage) image).getImageResult();
        }
        return null;
    }

    private AnimatedDrawable createAnimatedDrawable(
            AnimatedDrawableOptions options,
            AnimatedDrawableBackend animatedDrawableBackend) {
        DisplayMetrics displayMetrics = mResources.getDisplayMetrics();
        AnimatedDrawableDiagnostics animatedDrawableDiagnostics;
        AnimatedDrawableCachingBackend animatedDrawableCachingBackend =
                mAnimatedDrawableCachingBackendProvider.get(
                        animatedDrawableBackend,
                        options);
        if (options.enableDebugging) {
            animatedDrawableDiagnostics =
                    new AnimatedDrawableDiagnosticsImpl(mAnimatedDrawableUtil, displayMetrics);
        } else {
            animatedDrawableDiagnostics = AnimatedDrawableDiagnosticsNoop.getInstance();
        }

        return new AnimatedDrawable(
                mScheduledExecutorServiceForUiThread,
                animatedDrawableCachingBackend,
                animatedDrawableDiagnostics,
                mMonotonicClock);
    }
}
