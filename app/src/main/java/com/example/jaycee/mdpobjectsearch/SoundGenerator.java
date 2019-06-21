package com.example.jaycee.mdpobjectsearch;

import com.example.jaycee.mdpobjectsearch.helpers.ClassHelpers;
import com.example.jaycee.mdpobjectsearch.guidancetools.GuidanceInterface;
import com.google.ar.core.Pose;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class SoundGenerator
{
    private static final String TAG = SoundGenerator.class.getSimpleName();

    public SoundGenerator()
    {
    }

    public void playSound(Pose waypointPose, float[] cameraVector)
    {
        float pitch;
        // From config file; HI setting
        int pitchHighLim = 12;
        int pitchLowLim = 6;

        // Compensate for the Tango's default position being 90deg upright
        float deviceTilt = cameraVector[1];
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

        JNIBridge.playSound(waypointPose.getTranslation(), cameraVector, gain, pitch);
    }
}
