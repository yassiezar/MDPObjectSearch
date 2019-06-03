package com.example.jaycee.pomdpobjectsearch;

import android.util.Log;

import com.example.jaycee.pomdpobjectsearch.mdptools.GuidanceInterface;
import com.example.jaycee.pomdpobjectsearch.mdptools.GuidanceManager;
import com.google.ar.core.Pose;

public class ActivityGuided extends CameraActivityBase implements GuidanceInterface
{
    private static final String TAG = ActivityGuided.class.getSimpleName();

    private SoundGenerator soundGenerator;
    private GuidanceManager guidanceManager;

    @Override
    protected void onPause()
    {
        onGuidanceEnd();

        if(soundGenerator != null)
        {
            soundGenerator.stop();
            soundGenerator = null;
        }

        if(!JNIBridge.killSound())
        {
            Log.e(TAG, "OpenAL kill error");
        }

        super.onPause();
    }

    // Triggers pose update, returns timer status
    @Override
    public void onGuidanceLoop()
    {
        if(metrics != null)
        {
            metrics.updateWaypointPosition(guidanceManager.getWaypointPose());
            metrics.updateDevicePose(devicePose);
            metrics.updateTimestamp(frameTimestamp);

        }
        if(guidanceManager != null)
        {
            guidanceManager.updateDevicePose(devicePose);
        }
    }

    @Override
    public void onGuidanceStart(int target)
    {
        guidanceManager = new GuidanceManager(getSession(), devicePose, ActivityGuided.this, target);
        metrics = new Metrics();
        metrics.updateTarget(target);
        metrics.run();

        soundGenerator = new SoundGenerator(this);
        soundGenerator.setTarget(target);
        soundGenerator.run();

        setDrawWaypoint(true);
    }

    @Override
    public void onGuidanceEnd()
    {
        getVibrator().vibrate(350);

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
    public boolean onWaypointReached()
    {
        if(guidanceManager != null)
        {
            return guidanceManager.waypointReached();
        }

        return false;
    }

    @Override
    public void onGuidanceRequested(long observation)
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
    public Pose onWaypointPoseRequested()
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
    }

    public void targetSelected(int target)
    {
        onGuidanceStart(target);
    }
}
