package com.example.jaycee.pomdpobjectsearch;

import com.example.jaycee.pomdpobjectsearch.helpers.ClassHelpers;
import com.example.jaycee.pomdpobjectsearch.guidancetools.GuidanceInterface;
import com.google.ar.core.Pose;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class SoundGenerator implements Runnable
{
    private static final String TAG = SoundGenerator.class.getSimpleName();

    private static final int O_NOTHING = 0;

    private Objects.Observation prevCameraObservation = Objects.Observation.O_NOTHING;
    private Objects.Observation target = Objects.Observation.O_NOTHING;

    private Handler handler = new Handler();

    private boolean stop = false;

    private BarcodeListener barcodeListener;
    private GuidanceInterface guidanceInterface;

    SoundGenerator(Context context)//, SurfaceRenderer renderer)
    {
//        this.renderer = renderer;
        this.barcodeListener = (BarcodeListener)context;
        this.guidanceInterface = (GuidanceInterface)context;
    }

    void stop()
    {
        this.stop = true;
        handler.removeCallbacks(this);
        handler = null;
    }

    @Override
    public void run()
    {
        Objects.Observation observation = barcodeListener.onBarcodeCodeRequest();

        if(observation == target)
        {
            Log.i(TAG, "Target found");
            guidanceInterface.onGuidanceEnd();

            return;
        }

        if(!stop)
        {
            guidanceInterface.onGuidanceLoop();

            if(guidanceInterface.onWaypointReached() || (observation != prevCameraObservation && observation != Objects.Observation.O_NOTHING))
            {
                prevCameraObservation = observation;
                guidanceInterface.onGuidanceRequested(observation);
                Log.i(TAG, "Setting new waypoint");
            }

            float pitch;
            // From config file; HI setting
            int pitchHighLim = 12;
            int pitchLowLim = 6;

            // Compensate for the Tango's default position being 90deg upright
            float[] deviceOrientation = guidanceInterface.onCameraVectorRequested();
            float deviceTilt = deviceOrientation[1];
            Pose waypointPose = guidanceInterface.onWaypointPoseRequested();
            ClassHelpers.mVector waypointVector = new ClassHelpers.mVector(waypointPose.getTranslation());
            float waypointTilt = waypointVector.getEuler()[1];

            float tilt = waypointTilt - deviceTilt;

            if(tilt >= Math.PI / 2)
            {
                pitch = (float)(Math.pow(2, 64));
            }

            else if(tilt <= -Math.PI / 2)
            {
                pitch = (float)(Math.pow(2, pitchHighLim));
            }

            else
            {
                double gradientAngle = Math.toDegrees(Math.atan((pitchHighLim - pitchLowLim) / Math.PI));

                float grad = (float)(Math.tan(Math.toRadians(gradientAngle)));
                float intercept = (float)(pitchHighLim - Math.PI / 2 * grad);

                pitch = (float)(Math.pow(2, grad * -tilt + intercept));
            }

            float gain;
            if(Math.abs(tilt) > 0.175)      // 0.175rad = ~10deg
            {
                gain = 1.f;
            }
            else
            {
                gain = (float)(0.5/0.175*Math.abs(tilt) + 0.5);
            }

            JNIBridge.playSound(waypointPose.getTranslation(), deviceOrientation, gain, pitch);

            if(!stop) handler.postDelayed(this, 40);
        }
    }

    public void setTarget(Objects.Observation target)
    {
        this.target = target;
/*        this.targetSet = true;
        this.targetFound = false;*/

        prevCameraObservation = Objects.Observation.O_NOTHING;
    }

/*    public void setObservation(long observation)
    {
        this.observation = observation;
    }*/
}
