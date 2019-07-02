package com.example.jaycee.mdpobjectsearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.jaycee.mdpobjectsearch.rendering.SurfaceRenderer;
import com.google.ar.core.CameraIntrinsics;

import static com.example.jaycee.mdpobjectsearch.Objects.getObservation;

public class BarcodeScanner implements Runnable
{
    private static final String TAG = BarcodeScanner.class.getSimpleName();

    private Bitmap test;

    private SurfaceRenderer renderer;
    private BarcodeListener barcodeListener;

    private boolean running = false;

    public interface BarcodeListener
    {
        void onBarcodeScannerStart(CameraIntrinsics intrinsics);
        void onBarcodeScannerStop();
        void onScanComplete(Objects.Observation observation);
    }

    public BarcodeScanner(Context context, SurfaceRenderer renderer, CameraIntrinsics intrinsics, int scannerWidth, int scannerHeight)
    {
        this.renderer = renderer;
        this.barcodeListener = (BarcodeListener)context;

        this.test = Bitmap.createBitmap(scannerWidth, scannerHeight, Bitmap.Config.ARGB_8888);

        float[] focalLength = intrinsics.getFocalLength().clone();
        float[] principlePoint = intrinsics.getPrincipalPoint().clone();
        float[] distortionMatrix = new float[] {1.f, 0.00486219f, -0.44772422f, -0.01138138f, 0.0291972f, 0.70109351f};

        if(!JNIBridge.initDetector(scannerWidth, scannerHeight, focalLength, principlePoint, distortionMatrix))
        {
            Log.e(TAG, "Error starting scanner: ");
        }
    }

    @Override
    public void run()
    {
        running = true;
        Log.v(TAG, "Running scanner");
        Objects.Observation observation = Objects.Observation.O_NOTHING;

        Log.v(TAG, "Requesting lock");
        renderer.getScanner().getLock().lock();
        try
        {
            Log.v(TAG, "Got lock");
            int id = JNIBridge.processImage(test, renderer.getScanner().getBuffer());
            observation = getObservation(id);
        }
        catch(Exception e)
        {
            Log.e(TAG, "Scanning error: " + e);
        }
        finally
        {
            renderer.getScanner().getLock().unlock();
        }
        running = false;
        barcodeListener.onScanComplete(observation);
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

    public boolean isRunning() { return running; }

    public Bitmap getBarcodeDetectionPreview()
    {
        return this.test;
    }
}
