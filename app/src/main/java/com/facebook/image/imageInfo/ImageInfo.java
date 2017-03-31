package com.facebook.image.imageInfo;

/**
 * Created by Administrator on 2017/3/28 0028.
 */
/**
 * Interface containing information about an image.
 */
public interface ImageInfo {

    /**
     * @return width of the image
     */
    int getWidth();

    /**
     * @return height of the image
     */
    int getHeight();

    /**
     * @return quality information for the image
     */
    QualityInfo getQualityInfo();
}

