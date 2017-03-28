package com.facebook.imageDecode;

import com.facebook.factoryAndProvider.animatedFactory.animatedImageFactory.animatedImage.AnimatedImage;

/**
 * Created by Administrator on 2017/3/28 0028.
 */
public interface AnimatedImageDecoder {

    /**
     * Factory method to create the AnimatedImage from the
     * @param nativePtr The native pointer
     * @param sizeInBytes The size in byte to allocate
     * @return The AnimatedImage allocation
     */
    AnimatedImage decode(long nativePtr, int sizeInBytes);
}
