package com.example.jaycee.mdpobjectsearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import com.example.jaycee.mdpobjectsearch.rendering.SurfaceRenderer;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class BarcodeScanner implements Runnable
{
    private static final String TAG = BarcodeScanner.class.getSimpleName();
    private static final double SCALE_FACTOR = 0.25;

    private static final int O_NOTHING = 0;

    // private Handler handler = new Handler();
    Bitmap rawBitmap;

    private SurfaceRenderer renderer;

    private boolean stop = false;

    private int code = O_NOTHING;
    private int scannerHeight, scannerWidth;

    public BarcodeScanner(int scannerWidth, int scannerHeight, SurfaceRenderer renderer, float[] focalLength, float[] principlePoint, float[] distortionMatrix)
    {
        this.renderer = renderer;

        // this.detector = new BarcodeDetector.Builder(context).setBarcodeFormats(Barcode.QR_CODE).build();
        this.rawBitmap = Bitmap.createBitmap(scannerWidth, scannerHeight, Bitmap.Config.ARGB_8888);

        this.scannerHeight = scannerHeight;
        this.scannerWidth = scannerWidth;

        JNIBridge.initDetector(scannerWidth, scannerHeight, focalLength, principlePoint, distortionMatrix);
    }

    @Override
    public void run()
    {
        Log.v(TAG, "Running scanner");
        if(!stop)
        {
            code = O_NOTHING;

            rawBitmap.copyPixelsFromBuffer(renderer.getScanner().getBuffer().asIntBuffer());
            Log.v(TAG, "Requesting lock");
            renderer.getScanner().getLock().lock();
            try
            {
                Log.v(TAG, "Got lock");
                JNIBridge.processImage(renderer.getScanner().getBuffer());
                renderer.getScanner().setProcessed(true);
            }
            finally
            {
                renderer.getScanner().getLock().unlock();
            }
            // run();
            // handler.postDelayed(this, 50);
        }

/*        int scaledWidth, scaledHeight;
        Frame bitmapFrame;
        if(highQualityScanner)
        {
            scaledWidth = (int)(scannerWidth*SCALE_FACTOR);
            scaledHeight = (int)(scannerHeight*SCALE_FACTOR);
            Bitmap bitmap = Bitmap.createScaledBitmap(rawBitmap, scaledWidth, scaledHeight, false);
            bitmap = Bitmap.createScaledBitmap(bitmap, scannerWidth, scannerHeight, false);
            bitmapFrame = new Frame.Builder().setRotation(180).setBitmap(bitmap).build();
        }
        else
        {
            bitmapFrame = new Frame.Builder().setRotation(180).setBitmap(rawBitmap).build();
        }

        SparseArray<Barcode> barcodes = detector.detect(bitmapFrame);
        if(barcodes.size() > 0)
        {
            for(int i = 0; i < barcodes.size(); i++)
            {
                int key = barcodes.keyAt(i);
                Log.d(TAG, String.format("Object found, coords %d %d", barcodes.get(key).getBoundingBox().right, barcodes.get(key).getBoundingBox().bottom));
                Log.i(TAG, String.format("Barcode content: %s", barcodes.get(key).rawValue));
                this.code = Integer.parseInt(barcodes.get(key).rawValue);
            }
        }*/

        // if(!stop) handler.postDelayed(this, 40);
    }

    public void stop()
    {
        this.stop = true;
        // handler.removeCallbacks(this);
        // handler = null;

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
}
