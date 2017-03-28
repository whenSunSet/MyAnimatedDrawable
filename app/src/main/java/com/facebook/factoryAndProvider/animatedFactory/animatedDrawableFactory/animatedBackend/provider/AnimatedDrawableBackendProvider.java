package com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.provider;

/**
 * Created by Administrator on 2017/3/28 0028.
 */

import android.graphics.Rect;

import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.AnimatedDrawableBackend;
import com.facebook.factoryAndProvider.animatedFactory.animatedImageFactory.animatedImage.AnimatedImageResult;

/**
 *
 * 一个提供{@link AnimatedDrawableBackend}的类
 * Assisted provider for {@link AnimatedDrawableBackend}.
 */
public interface AnimatedDrawableBackendProvider {

    /**
     * 创建一个{@link AnimatedDrawableBackend}
     *
     * @param animatedImageResult the image result.
     * @param bounds the initial bounds for the drawable
     * @return a new {@link AnimatedDrawableBackend}
     */
    AnimatedDrawableBackend get(AnimatedImageResult animatedImageResult, Rect bounds);
}
