package com.example.jaycee.pomdpobjectsearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import com.example.jaycee.pomdpobjectsearch.rendering.SurfaceRenderer;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class BarcodeScanner implements Runnable
{
    private static final String TAG = BarcodeScanner.class.getSimpleName();
    private static final double SCALE_FACTOR = 0.25;

    private static final int O_NOTHING = 0;

    private Handler handler = new Handler();

    private BarcodeDetector detector;
    private Bitmap rawBitmap;

    private SurfaceRenderer renderer;

    private boolean stop = false;
    private boolean highQualityScanner;

    private int code = O_NOTHING;
    private int scannerHeight, scannerWidth;

    public BarcodeScanner(Context context, int scannerWidth, int scannerHeight, boolean highQualityScanner, SurfaceRenderer renderer)
    {
        this.renderer = renderer;
        this.highQualityScanner = highQualityScanner;

        this.detector = new BarcodeDetector.Builder(context).setBarcodeFormats(Barcode.QR_CODE).build();
        this.rawBitmap = Bitmap.createBitmap(scannerWidth, scannerHeight, Bitmap.Config.ARGB_8888);

        this.scannerHeight = scannerHeight;
        this.scannerWidth = scannerWidth;

        JNIBridge.initDetector();
    }

    @Override
    public void run()
    {
        Log.v(TAG, "Running barcode scanner");
        code = O_NOTHING;

        rawBitmap.copyPixelsFromBuffer(renderer.getCurrentFrameBuffer());
        JNIBridge.processImage(renderer.getCurrentFrameBuffer());

        int scaledWidth, scaledHeight;
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
        }

        if(!stop) handler.postDelayed(this, 40);
    }

    public void stop()
    {
        this.stop = true;
        handler = null;
        JNIBridge.killDetector();
    }

    public int getCode() { return this.code; }
}
