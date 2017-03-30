package com.facebook.factoryAndProvider.animatedFactory;

import android.content.Context;

import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.AnimatedDrawableFactory;
import com.facebook.factoryAndProvider.animatedFactory.animatedImageFactory.AnimatedImageFactory;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Created by Administrator on 2017/3/28 0028.
 */

/**
 * 提供 动画Factory 的Factory，默认实现是{@link AnimatedFactoryImpl}
 */
@NotThreadSafe
public interface AnimatedFactory {

    AnimatedDrawableFactory getAnimatedDrawableFactory(Context context);

    AnimatedImageFactory getAnimatedImageFactory();
}
