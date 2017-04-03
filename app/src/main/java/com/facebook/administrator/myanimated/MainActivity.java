package com.facebook.administrator.myanimated;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pools;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.animation.ValueAnimator;

import com.example.administrator.myanimated.R;
import com.facebook.bitmapFactory.ArtBitmapFactory;
import com.facebook.bitmapFactory.EmptyJpegGenerator;
import com.facebook.bitmapFactory.GingerbreadBitmapFactory;
import com.facebook.bitmapFactory.HoneycombBitmapFactory;
import com.facebook.bitmapFactory.PlatformBitmapFactory;
import com.facebook.common.util.SDCardUtils;
import com.facebook.executor.executorSupplier.DefaultExecutorSupplier;
import com.facebook.factoryAndProvider.AnimatedFactoryProvider;
import com.facebook.factoryAndProvider.animatedFactory.AnimatedFactory;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.AnimatedDrawableFactory;
import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedDrawable.AnimatedDrawable;
import com.facebook.image.CloseableImage;
import com.facebook.image.CloseableStaticBitmap;
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
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initFresco();

        pngButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseableImage closeableImage=initCloseableImage("1.png");
                if (closeableImage!=null)imageView.setImageBitmap(((CloseableStaticBitmap)closeableImage).getUnderlyingBitmap());
            }
        });
        jpgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseableImage closeableImage=initCloseableImage("2.jpg");
                if (closeableImage!=null)imageView.setImageBitmap(((CloseableStaticBitmap)closeableImage).getUnderlyingBitmap());
            }
        });
        staticWebpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseableImage closeableImage=initCloseableImage("3.webp");
                if (closeableImage!=null)imageView.setImageBitmap(((CloseableStaticBitmap)closeableImage).getUnderlyingBitmap());
            }
        });
        dynamicWebpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseableImage closeableImage=initCloseableImage("4.webp");
                AnimatedDrawable animatedDrawable=(AnimatedDrawable)mAnimatedDrawableFactory.create(closeableImage);
                if (closeableImage!=null)imageView.setImageDrawable(animatedDrawable);
                animatedDrawable.start();
            }
        });
        gifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseableImage closeableImage=initCloseableImage("5.gif");
                AnimatedDrawable animatedDrawable=(AnimatedDrawable)mAnimatedDrawableFactory.create(closeableImage);
                if (closeableImage!=null)imageView.setImageDrawable(animatedDrawable);
                ValueAnimator valueAnimator=animatedDrawable.createValueAnimator();
                valueAnimator.start();
            }
        });
    }

    Button pngButton;
    Button jpgButton;
    Button staticWebpButton;
    Button dynamicWebpButton;
    Button gifButton;
    ImageView imageView;
    private void initView(){
        pngButton=(Button)findViewById(R.id.setPng);
        jpgButton=(Button)findViewById(R.id.setJpg);
        staticWebpButton=(Button)findViewById(R.id.setStaticWebp);
        dynamicWebpButton=(Button)findViewById(R.id.setDynamicWebp);
        gifButton=(Button)findViewById(R.id.setGif);
        imageView=(ImageView)findViewById(R.id.testImageView);
    }

    AnimatedDrawableFactory mAnimatedDrawableFactory;
    ImageDecoder mImageDecoder;
    PooledByteBufferFactory pooledByteBufferFactory;
    private void initFresco(){
        PoolFactory poolFactory=new PoolFactory(PoolConfig.newBuilder().build());
        PlatformDecoder mPlatformDecoder=buildPlatformDecoder(poolFactory,true);
        PlatformBitmapFactory mPlatformBitmapFactory=buildPlatformBitmapFactory(
                poolFactory
                ,mPlatformDecoder);
        AnimatedFactory mAnimatedFactory= AnimatedFactoryProvider.getAnimatedFactory(
                mPlatformBitmapFactory,
                new DefaultExecutorSupplier(2) );

        mImageDecoder = new DefaultImageDecoder(
                mAnimatedFactory.getAnimatedImageFactory(),
                mPlatformDecoder,
                Bitmap.Config.ARGB_8888);

        mAnimatedDrawableFactory=mAnimatedFactory.getAnimatedDrawableFactory(this);
        pooledByteBufferFactory=poolFactory.getPooledByteBufferFactory();
    }

    private CloseableImage initCloseableImage(String fileName){
        CloseableReference<PooledByteBuffer> ref;
        File file=new File(SDCardUtils.getCacheDir(MainActivity.this)+"/"+fileName);
        FileInputStream fileInputStream;
        PooledByteBuffer pooledByteBuffer=null;
        try {
            if (file==null)return null;
            fileInputStream=new FileInputStream(file);
            pooledByteBuffer=pooledByteBufferFactory.newByteBuffer(fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ref = CloseableReference.of(pooledByteBuffer);
        EncodedImage encodedImage=new EncodedImage(ref);
        CloseableImage image=mImageDecoder.decode(encodedImage,encodedImage.getSize(),ImmutableQualityInfo.FULL_QUALITY, ImageDecodeOptions.newBuilder().build());
        return image;
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
