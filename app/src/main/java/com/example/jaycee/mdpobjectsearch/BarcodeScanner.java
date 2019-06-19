package com.example.jaycee.mdpobjectsearch;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.jaycee.mdpobjectsearch.rendering.SurfaceRenderer;

public class BarcodeScanner implements Runnable
{
    private static final String TAG = BarcodeScanner.class.getSimpleName();

    private static final int O_NOTHING = 0;

    private Bitmap test;

    private SurfaceRenderer renderer;

    private boolean running = false;

    private int code = O_NOTHING;

    public BarcodeScanner(int scannerWidth, int scannerHeight, SurfaceRenderer renderer, float[] focalLength, float[] principlePoint, float[] distortionMatrix)
    {
        this.renderer = renderer;

        this.test = Bitmap.createBitmap(scannerWidth, scannerHeight, Bitmap.Config.ARGB_8888);

        JNIBridge.initDetector(scannerWidth, scannerHeight, focalLength, principlePoint, distortionMatrix);
    }

    @Override
    public void run()
    {
        running = true;
        Log.v(TAG, "Running scanner");
        code = O_NOTHING;

        Log.v(TAG, "Requesting lock");
        renderer.getScanner().getLock().lock();
        try
        {
            Log.v(TAG, "Got lock");
            JNIBridge.processImage(test, renderer.getScanner().getBuffer());
        }
        finally
        {
            renderer.getScanner().getLock().unlock();
        }
        running = false;
    }

    public void stop()
    {
        renderer.getScanner().getLock().lock();
        try
        {
            JNIBridge.killDetector();
        }
        finally
        {
            renderer.getScanner().getLock().unlock();
        }
    }

    public int getCode() { return this.code; }
    public boolean isRunning() { return running; }

    public Bitmap getBarcodeDetectionPreview()
    {
        return this.test;
    }
}
