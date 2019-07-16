package com.example.jaycee.mdpobjectsearch;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.jaycee.mdpobjectsearch.guidancetools.GuidanceInterface;
import com.example.jaycee.mdpobjectsearch.guidancetools.pomdp.GuidanceManager;
import com.google.ar.core.Pose;

public class ActivityGuided extends CameraActivityBase implements GuidanceInterface
{
    private static final String TAG = ActivityGuided.class.getSimpleName();

    private GuidanceManager guidanceManager;

    private Objects.Observation targetObservation, observation;

    @Override
    protected void onResume()
    {
        super.onResume();

        JNIBridge.initSound();
    }

    @Override
    protected void onPause()
    {
        JNIBridge.killSound();

        endGuidance();

        super.onPause();
    }

    @Override
    public void targetSelected(Objects.Observation target)
    {
        super.targetSelected(target);

        targetObservation = target;

        guidanceManager = new GuidanceManager(getSession(), devicePose, ActivityGuided.this, target);
        guidanceManager.start();

        setDrawWaypoint(true);
    }

    @Override
    public void onTargetFound()
    {
        super.onTargetFound();
        endGuidance();
    }

    private void endGuidance()
    {
        if(guidanceManager != null)
        {
            guidanceManager.end();
            guidanceManager = null;
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
    public void onScanRequest()
    {
        super.onScanRequest();
        if(barcodeScanner != null && !barcodeScanner.isRunning())
        {
            scannerHandler.post(barcodeScanner);
        }
    }

    @Override
    public Objects.Observation onObservationRequest()
    {
        if(this.observation == null)
        {
            return Objects.Observation.O_NOTHING;
        }
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
