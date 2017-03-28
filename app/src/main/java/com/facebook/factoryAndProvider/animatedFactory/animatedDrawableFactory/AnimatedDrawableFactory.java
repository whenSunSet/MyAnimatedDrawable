package com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory;

import android.graphics.drawable.Drawable;

import com.facebook.image.CloseableImage;

/**
 * Created by Administrator on 2017/3/28 0028.
 */
public interface AnimatedDrawableFactory {

    /**
     * 创建一个{@link AnimatedDrawable}基于一个{@link AnimatedImage}
     *
     * Creates an {@link AnimatedDrawable} based on an {@link AnimatedImage}.
     *
     * @param closeableImage the result of the code
     * @return a newly constructed {@link Drawable}
     */
    Drawable create(CloseableImage closeableImage);


}
