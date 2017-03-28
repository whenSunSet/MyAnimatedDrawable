package com.facebook.imageDecode;

/**
 * Created by Administrator on 2017/3/28 0028.
 */

import com.facebook.image.CloseableImage;
import com.facebook.image.EncodedImage;
import com.facebook.image.QualityInfo;

/**
 * image解码接口，从一个{@link EncodedImage}中获取一个{@link CloseableImage}
 * Image decoder interface. Takes an {@link EncodedImage} and creates a {@link CloseableImage}.
 */
public interface ImageDecoder {

    CloseableImage decode(
            EncodedImage encodedImage,
            int length,
            QualityInfo qualityInfo,
            ImageDecodeOptions options);
}

