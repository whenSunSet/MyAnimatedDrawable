package com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.other;

/**
 * Created by Administrator on 2017/3/29 0029.
 */

import android.graphics.Canvas;
import android.graphics.Rect;

import com.facebook.factoryAndProvider.animatedFactory.animatedDrawableFactory.animatedBackend.AnimatedDrawableCachingBackend;

/**
 * 实现了{@link AnimatedDrawableDiagnostics}但是不干任何事
 * Implementation of {@link AnimatedDrawableDiagnostics} that does nothing.
 */
public class AnimatedDrawableDiagnosticsNoop implements AnimatedDrawableDiagnostics {

    private static AnimatedDrawableDiagnosticsNoop sInstance = new AnimatedDrawableDiagnosticsNoop();

    public static AnimatedDrawableDiagnosticsNoop getInstance() {
        return sInstance;
    }

    @Override
    public void setBackend(AnimatedDrawableCachingBackend animatedDrawableBackend) {
    }

    @Override
    public void onStartMethodBegin() {
    }

    @Override
    public void onStartMethodEnd() {
    }

    @Override
    public void onNextFrameMethodBegin() {
    }

    @Override
    public void onNextFrameMethodEnd() {
    }

    @Override
    public void incrementDroppedFrames(int droppedFrames) {
    }

    @Override
    public void incrementDrawnFrames(int drawnFrames) {
    }

    @Override
    public void onDrawMethodBegin() {
    }

    @Override
    public void onDrawMethodEnd() {
    }

    @Override
    public void drawDebugOverlay(Canvas canvas, Rect destRect) {
    }
}
