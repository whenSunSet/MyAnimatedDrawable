package com.facebook.administrator.myanimated;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pools;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.example.administrator.myanimated.R;
import com.facebook.bitmapFactory.ArtBitmapFactory;
import com.facebook.bitmapFactory.EmptyJpegGenerator;
import com.facebook.bitmapFactory.GingerbreadBitmapFactory;
import com.facebook.bitmapFactory.HoneycombBitmapFactory;
import com.facebook.bitmapFactory.PlatformBitmapFactory;
import com.facebook.executor.executorSupplier.DefaultExecutorSupplier;
import com.facebook.factoryAndProvider.AnimatedFactoryProvider;
import com.facebook.factoryAndProvider.animatedFactory.AnimatedFactory;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.AnimatedDrawableFactory;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedDrawable.AnimatedDrawable;
import com.facebook.factoryAndProvider.animatedFactory.animatedImageFactory.AnimatedImageFactory;
import com.facebook.image.CloseableImage;
import com.facebook.image.EncodedImage;
import com.facebook.image.imageDecode.DefaultImageDecoder;
import com.facebook.image.imageDecode.ImageDecodeOptions;
import com.facebook.image.imageDecode.ImageDecoder;
import com.facebook.image.imageInfo.ImmutableQualityInfo;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.platformDecoder.ArtDecoder;
import com.facebook.platformDecoder.GingerbreadPurgeableDecoder;
import com.facebook.platformDecoder.KitKatPurgeableDecoder;
import com.facebook.platformDecoder.PlatformDecoder;
import com.facebook.pool.poolFactory.PoolConfig;
import com.facebook.pool.poolFactory.PoolFactory;
import com.facebook.pool.poolFactory.PooledByteBufferFactory;
import com.facebook.references.CloseableReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private AnimatedDrawableFactory mAnimatedDrawableFactory;
    private AnimatedImageFactory mAnimatedImageFactory;
    private AnimatedFactory mAnimatedFactory;
    private PoolFactory mPoolFactory;
    private PlatformDecoder mPlatformDecoder;
    private PlatformBitmapFactory mPlatformBitmapFactory;
    private ImageDecoder mImageDecoder;

    private PooledByteBufferFactory mPooledByteBufferFactory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPoolFactory=new PoolFactory(PoolConfig.newBuilder().build());
        mPlatformDecoder=buildPlatformDecoder(mPoolFactory,true);
        mPlatformBitmapFactory=buildPlatformBitmapFactory(
                mPoolFactory
                ,mPlatformDecoder);
        mAnimatedFactory = AnimatedFactoryProvider.getAnimatedFactory(
                mPlatformBitmapFactory,
                new DefaultExecutorSupplier(2) );

        mAnimatedImageFactory=mAnimatedFactory.getAnimatedImageFactory();
        mAnimatedDrawableFactory=mAnimatedFactory.getAnimatedDrawableFactory(this);

        mImageDecoder = new DefaultImageDecoder(
                mAnimatedImageFactory,
                mPlatformDecoder,
                Bitmap.Config.ARGB_8888);

        CloseableReference<PooledByteBuffer> ref = null;
        try {
            File file=new File("/storage/emulated/0/表情相册/test.gif");
            FileInputStream fileInputStream=new FileInputStream(file);
            PooledByteBufferFactory pooledByteBufferFactory=mPoolFactory.getPooledByteBufferFactory();
            PooledByteBuffer pooledByteBuffer=pooledByteBufferFactory.newByteBuffer(fileInputStream);
            ref = CloseableReference.of(pooledByteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        EncodedImage encodedImage=new EncodedImage(ref);

        CloseableImage image=mImageDecoder.decode(encodedImage,encodedImage.getSize(),ImmutableQualityInfo.FULL_QUALITY, ImageDecodeOptions.newBuilder().build());

        AnimatedDrawable animatedDrawable=(AnimatedDrawable)mAnimatedDrawableFactory.create(image);
        ImageView imageView=(ImageView)findViewById(R.id.testImageView);
        imageView.setImageDrawable(animatedDrawable);
        animatedDrawable.start();
    }

    public static PlatformBitmapFactory buildPlatformBitmapFactory(
            PoolFactory poolFactory,
            PlatformDecoder platformDecoder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new ArtBitmapFactory(poolFactory.getBitmapPool());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new HoneycombBitmapFactory(
                    new EmptyJpegGenerator(poolFactory.getPooledByteBufferFactory()),
                    platformDecoder);
        } else {
            return new GingerbreadBitmapFactory();
        }
    }

    public static PlatformDecoder buildPlatformDecoder(
            PoolFactory poolFactory,
            boolean directWebpDirectDecodingEnabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int maxNumThreads = poolFactory.getFlexByteArrayPoolMaxNumThreads();
            return new ArtDecoder(
                    poolFactory.getBitmapPool(),
                    maxNumThreads,
                    new Pools.SynchronizedPool<>(maxNumThreads));
        } else {
            if (directWebpDirectDecodingEnabled
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                return new GingerbreadPurgeableDecoder();
            } else {
                return new KitKatPurgeableDecoder(poolFactory.getFlexByteArrayPool());
            }
        }
    }

}
