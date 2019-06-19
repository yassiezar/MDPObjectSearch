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

    private boolean stop = false;

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
        Log.v(TAG, "Running scanner");
        if(!stop)
        {
            code = O_NOTHING;

            Log.v(TAG, "Requesting lock");
            renderer.getScanner().getLock().lock();
            try
            {
                Log.v(TAG, "Got lock");
                JNIBridge.processImage(test, renderer.getScanner().getBuffer());
                renderer.getScanner().setProcessed(true);
            }
            finally
            {
                renderer.getScanner().getLock().unlock();
            }
        }
    }

    public void stop()
    {
        this.stop = true;

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

    public Bitmap getBarcodeDetectionPreview()
    {
        return this.test;
    }
}
