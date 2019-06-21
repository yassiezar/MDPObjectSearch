package com.example.jaycee.mdpobjectsearch;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.jaycee.mdpobjectsearch.guidancetools.GuidanceInterface;
import com.example.jaycee.mdpobjectsearch.guidancetools.pomdp.GuidanceManager;
import com.google.ar.core.CameraIntrinsics;
import com.google.ar.core.Pose;

public class ActivityGuided extends CameraActivityBase implements GuidanceInterface
{
    private static final String TAG = ActivityGuided.class.getSimpleName();

//    private SoundGenerator soundGenerator;
    private GuidanceManager guidanceManager;

    private Objects.Observation targetObservation, observation;

/*    private HandlerThread soundHandlerThread;
    private Handler soundHandler;*/
    private HandlerThread scannerHandlerThread, guidanceHandlerThread;
    private Handler scannerHandler, guidanceHandler;

    @Override
    protected void onResume()
    {
        super.onResume();

        JNIBridge.initSound();

//        onBarcodeScannerStart();

/*        soundHandlerThread = new HandlerThread("SoundGenerator thread");
        soundHandlerThread.start();
        soundHandler = new Handler(soundHandlerThread.getLooper());*/
    }

    @Override
    protected void onPause()
    {
        JNIBridge.killSound();

        endGuidance();

//        onGuidanceEnd();

/*        if(!JNIBridge.killSound())
        {
            Log.e(TAG, "OpenAL kill error");
        }*/

/*        if(soundHandler != null)
        {
            soundHandlerThread.quitSafely();
            try
            {
                Log.v(TAG, "Stopping scanner thread");
                soundHandlerThread.join();
                soundHandlerThread = null;
                soundHandler = null;
            }
            catch (InterruptedException e)
            {
                Log.e(TAG, "Error closing scanner thread: " + e);
            }
        }*/

        super.onPause();
    }

    // Triggers pose update, returns timer status
/*

    @Override
    public boolean onWaypointReached()
    {
        if(guidanceManager != null)
        {
            return guidanceManager.waypointReached();
        }

        return false;
    }

    @Override
    public void onGuidanceRequested(Objects.Observation observation)
    {
        if(guidanceManager != null)
        {
            guidanceManager.provideGuidance(getSession(), observation);
        }
    }

    @Override
    public Pose onDrawWaypoint()
    {
        if(guidanceManager != null)
        {
            return guidanceManager.getWaypointPose();
        }
        return null;
    }


    @Override
    public float[] onCameraVectorRequested()
    {
        if(guidanceManager != null)
        {
            return guidanceManager.getCameraVector();
        }

        return null;
    }*/

    public void targetSelected(Objects.Observation target)
    {
        targetObservation = target;

//        onGuidanceStart(target);
        guidanceHandlerThread = new HandlerThread("GuidanceThread");
        guidanceHandlerThread.start();
        guidanceHandler = new Handler(guidanceHandler.getLooper());
        guidanceManager = new GuidanceManager(getSession(), devicePose, ActivityGuided.this, target);
        guidanceHandler.postDelayed(guidanceManager, 40);

        metrics = new Metrics();
        metrics.updateTarget(target);
        metrics.run();

        setDrawWaypoint(true);
    }

    @Override
    public void onBarcodeScannerStart(CameraIntrinsics intrinsics)
    {
        scannerHandlerThread = new HandlerThread("BarcodeScanner thread");
        scannerHandlerThread.start();
        scannerHandler = new Handler(scannerHandlerThread.getLooper());


//        Log.i(TAG, "Focal len: %f principle point: %f" + Arrays.toString(getIntrinsics().getFocalLength()) + Arrays.toString(intrinsics.getPrincipalPoint()));
        barcodeScanner = new BarcodeScanner(this, getCameraSurface().getRenderer(), intrinsics, imageWidth, imageHeight);
        // barcodeScanner = new BarcodeScanner(1440, 2280, surfaceView.getRenderer(), new float[] {5522.19584f, 5496.99633f}, new float[] {2723.53276f, 2723.53276f}, distortionMatrix);    // Params measures from opencv calibration procedure
    }

    @Override
    public void onTargetFound()
    {
        getVibrator().vibrate(350);
        endGuidance();
    }

    private void endGuidance()
    {
        if(guidanceHandler != null)
        {
            guidanceHandlerThread.quitSafely();
            try
            {
                guidanceHandlerThread.join();
            }
            catch (InterruptedException e)
            {
                Log.e(TAG, "Guidance Thread end error: " + e);
            }
        }

        if(guidanceManager != null)
        {
            guidanceManager.end();
            guidanceManager = null;
        }

        if(metrics != null)
        {
            metrics.stop();
            metrics = null;
        }

        setDrawWaypoint(false);
    }

    @Override
    public void onScanComplete(Objects.Observation observation)
    {
        this.observation = observation;
        if(observation == targetObservation)
        {
            onTargetFound();
        }
    }

    @Override
    public Objects.Observation onObservationRequest()
    {
        return this.observation;
    }

    @Override
    public Pose onDevicePoseRequested()
    {
        return this.devicePose;
    }

    @Override
    public Pose onWaypointPoseRequested()
    {
        if(guidanceManager != null)
        {
            return guidanceManager.getWaypointPose();
        }
        return null;
    }
}
