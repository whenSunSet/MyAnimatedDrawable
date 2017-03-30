package com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.provider;

/**
 * Created by Administrator on 2017/3/28 0028.
 */

import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.AnimatedDrawableBackend;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.AnimatedDrawableCachingBackendImpl;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.other.AnimatedDrawableOptions;

/**
 * 一个提提供{@link AnimatedDrawableCachingBackendImpl}的类
 * Assisted provider for {@link AnimatedDrawableCachingBackendImpl}.
 */
public interface AnimatedDrawableCachingBackendImplProvider {

    /**
     * Creates a new {@link AnimatedDrawableCachingBackendImpl}.
     *
     * @param animatedDrawableBackend the backend to delegate to
     * @param options the options for the drawable
     * @return a new {@link AnimatedDrawableCachingBackendImpl}
     */
    AnimatedDrawableCachingBackendImpl get(
            AnimatedDrawableBackend animatedDrawableBackend,
            AnimatedDrawableOptions options);
}

