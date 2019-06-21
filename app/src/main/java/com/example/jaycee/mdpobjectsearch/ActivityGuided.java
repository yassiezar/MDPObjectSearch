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

    private SoundGenerator soundGenerator;
    private GuidanceManager guidanceManager;

    private HandlerThread soundHandlerThread;
    private Handler soundHandler;

    @Override
    protected void onResume()
    {
        super.onResume();

        soundHandlerThread = new HandlerThread("SoundGenerator thread");
        soundHandlerThread.start();
        soundHandler = new Handler(soundHandlerThread.getLooper());
    }

    @Override
    protected void onPause()
    {
        onGuidanceEnd();

        if(!JNIBridge.killSound())
        {
            Log.e(TAG, "OpenAL kill error");
        }

        if(soundHandler != null)
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
    public void onGuidanceStart(Objects.Observation target)
    {
        guidanceManager = new GuidanceManager(getSession(), devicePose, ActivityGuided.this, target);
        metrics = new Metrics();
        metrics.updateTarget(target);
        metrics.run();

        soundGenerator = new SoundGenerator(this);
        soundGenerator.setTarget(target);
        soundHandler.postDelayed(soundGenerator, 40);

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

    public void targetSelected(Objects.Observation target)
    {
        onGuidanceStart(target);
    }
}
