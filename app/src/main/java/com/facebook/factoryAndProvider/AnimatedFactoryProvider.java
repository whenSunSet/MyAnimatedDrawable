package com.facebook.factoryAndProvider;


import com.facebook.executor.ExecutorSupplier;
import com.facebook.factoryAndProvider.animatedFactory.AnimatedFactory;
import com.facebook.util.PlatformBitmapFactory;

import java.lang.reflect.Constructor;

/**
 * Created by heshixiyang on 2017/3/16.
 */
public class AnimatedFactoryProvider {

    private static boolean sImplLoaded;

    private static AnimatedFactory sImpl = null;

    public static AnimatedFactory getAnimatedFactory(
            PlatformBitmapFactory platformBitmapFactory,
            ExecutorSupplier executorSupplier) {
        if (!sImplLoaded) {
            try {
                final Class<?> clazz =
                        Class.forName("com.facebook.imagepipeline.animated.factory.AnimatedFactoryImplSupport");
                final Constructor<?> constructor = clazz.getConstructor(
                        PlatformBitmapFactory.class,
                        ExecutorSupplier.class);
                sImpl = (AnimatedFactory) constructor.newInstance(
                        platformBitmapFactory,
                        executorSupplier);
            } catch (Throwable e) {
                // Head in the sand
            }
            if (sImpl != null) {
                sImplLoaded = true;
                return sImpl;
            }
            try {
                final Class<?> clazz =
                        Class.forName("com.facebook.imagepipeline.animated.factory.AnimatedFactoryImpl");
                final Constructor<?> constructor = clazz.getConstructor(
                        PlatformBitmapFactory.class,
                        ExecutorSupplier.class);
                sImpl = (AnimatedFactory) constructor.newInstance(
                        platformBitmapFactory,
                        executorSupplier);
            } catch (Throwable e) {
                // Head in the sand
            }
            sImplLoaded = true;
        }
        return sImpl;
    }

}
