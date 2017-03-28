package com.facebook.factoryAndProvider.animatedFactory;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;

import com.facebook.executor.DefaultSerialExecutorService;
import com.facebook.executor.ExecutorSupplier;
import com.facebook.executor.SerialExecutorService;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.AnimatedDrawableFactory;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.AnimatedDrawableBackend;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.AnimatedDrawableBackendImpl;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.AnimatedDrawableCachingBackendImpl;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.provider.AnimatedDrawableBackendProvider;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.provider.AnimatedDrawableCachingBackendImplProvider;
import com.facebook.factoryAndProvider.animatedFactory.animatedImageFactory.AnimatedImageFactory;
import com.facebook.factoryAndProvider.animatedFactory.animatedImageFactory.AnimatedImageFactoryImpl;
import com.facebook.factoryAndProvider.animatedFactory.animatedImageFactory.animatedImage.AnimatedImageResult;
import com.facebook.util.AnimatedDrawableUtil;
import com.facebook.util.DoNotStrip;
import com.facebook.util.MonotonicClock;
import com.facebook.util.PlatformBitmapFactory;

import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Created by Administrator on 2017/3/28 0028.
 */
@NotThreadSafe
@DoNotStrip
public class AnimatedFactoryImpl implements AnimatedFactory {

    private AnimatedDrawableBackendProvider mAnimatedDrawableBackendProvider;
    private AnimatedDrawableUtil mAnimatedDrawableUtil;
    private AnimatedDrawableFactory mAnimatedDrawableFactory;
    private AnimatedImageFactory mAnimatedImageFactory;

    private ExecutorSupplier mExecutorSupplier;

    private PlatformBitmapFactory mPlatformBitmapFactory;

    @DoNotStrip
    public AnimatedFactoryImpl(
            PlatformBitmapFactory platformBitmapFactory,
            ExecutorSupplier executorSupplier) {
        this.mPlatformBitmapFactory = platformBitmapFactory;
        this.mExecutorSupplier = executorSupplier;
    }

    private AnimatedDrawableFactory buildAnimatedDrawableFactory(
            final SerialExecutorService serialExecutorService,
            final ActivityManager activityManager,
            final AnimatedDrawableUtil animatedDrawableUtil,
            AnimatedDrawableBackendProvider animatedDrawableBackendProvider,
            ScheduledExecutorService scheduledExecutorService,
            final MonotonicClock monotonicClock,
            Resources resources) {
        AnimatedDrawableCachingBackendImplProvider animatedDrawableCachingBackendImplProvider =
                new AnimatedDrawableCachingBackendImplProvider() {
                    @Override
                    public AnimatedDrawableCachingBackendImpl get(
                            AnimatedDrawableBackend animatedDrawableBackend,
                            AnimatedDrawableOptions options) {
                        return new AnimatedDrawableCachingBackendImpl(
                                serialExecutorService,
                                activityManager,
                                animatedDrawableUtil,
                                monotonicClock,
                                animatedDrawableBackend,
                                options);
                    }
                };

        return createAnimatedDrawableFactory(
                animatedDrawableBackendProvider,
                animatedDrawableCachingBackendImplProvider,
                animatedDrawableUtil,
                scheduledExecutorService,
                resources);
    }

    private AnimatedDrawableBackendProvider getAnimatedDrawableBackendProvider() {
        if (mAnimatedDrawableBackendProvider == null) {
            mAnimatedDrawableBackendProvider = new AnimatedDrawableBackendProvider() {
                @Override
                public AnimatedDrawableBackend get(AnimatedImageResult animatedImageResult, Rect bounds) {
                    return new AnimatedDrawableBackendImpl(
                            getAnimatedDrawableUtil(),
                            animatedImageResult,
                            bounds);
                }
            };
        }
        return mAnimatedDrawableBackendProvider;
    }

    @Override
    public AnimatedDrawableFactory getAnimatedDrawableFactory(Context context) {
        if (mAnimatedDrawableFactory == null) {
            SerialExecutorService serialExecutorService =
                    new DefaultSerialExecutorService(mExecutorSupplier.forDecode());
            ActivityManager activityManager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            mAnimatedDrawableFactory = buildAnimatedDrawableFactory(
                    serialExecutorService,
                    activityManager,
                    getAnimatedDrawableUtil(),
                    getAnimatedDrawableBackendProvider(),
                    UiThreadImmediateExecutorService.getInstance(),
                    RealtimeSinceBootClock.get(),
                    context.getResources());
        }
        return mAnimatedDrawableFactory;
    }

    // We need some of these methods public for now so internal code can use them.
    private AnimatedDrawableUtil getAnimatedDrawableUtil() {
        if (mAnimatedDrawableUtil == null) {
            mAnimatedDrawableUtil = new AnimatedDrawableUtil();
        }
        return mAnimatedDrawableUtil;
    }

    private AnimatedImageFactory buildAnimatedImageFactory() {
        AnimatedDrawableBackendProvider animatedDrawableBackendProvider =
                new AnimatedDrawableBackendProvider() {
                    @Override
                    public AnimatedDrawableBackend get(AnimatedImageResult imageResult, Rect bounds) {
                        return new AnimatedDrawableBackendImpl(getAnimatedDrawableUtil(), imageResult, bounds);
                    }
                };
        return new AnimatedImageFactoryImpl(animatedDrawableBackendProvider, mPlatformBitmapFactory);
    }

    @Override
    public AnimatedImageFactory getAnimatedImageFactory() {
        if (mAnimatedImageFactory == null) {
            mAnimatedImageFactory = buildAnimatedImageFactory();
        }
        return mAnimatedImageFactory;
    }

    protected AnimatedDrawableFactory createAnimatedDrawableFactory(
            AnimatedDrawableBackendProvider animatedDrawableBackendProvider,
            AnimatedDrawableCachingBackendImplProvider animatedDrawableCachingBackendImplProvider,
            AnimatedDrawableUtil animatedDrawableUtil,
            ScheduledExecutorService scheduledExecutorService,
            Resources resources) {
        return new AnimatedDrawableFactoryImpl(
                animatedDrawableBackendProvider,
                animatedDrawableCachingBackendImplProvider,
                animatedDrawableUtil,
                scheduledExecutorService,
                resources);
    }
}
