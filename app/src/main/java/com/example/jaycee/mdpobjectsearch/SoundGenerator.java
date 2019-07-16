package com.example.jaycee.mdpobjectsearch;

import com.example.jaycee.mdpobjectsearch.helpers.ClassHelpers;
import com.example.jaycee.mdpobjectsearch.guidancetools.GuidanceInterface;
import com.google.ar.core.Pose;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

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
        float deviceTilt = cameraVector[2];
        ClassHelpers.mVector waypointVector = new ClassHelpers.mVector(waypointPose.getTranslation());
        float waypointTilt = waypointVector.getEuler()[1];

        if (waypointTilt > Math.PI/2)
        {
            waypointTilt -= (float) Math.PI;
        }
        else if (waypointTilt < Math.PI/2)
        {
            waypointTilt += (float) Math.PI;
        }
//        Log.d(TAG, String.format("%f %s", waypointTilt, Arrays.toString(cameraVector)));
//        Log.d(TAG, String.format("%f %s", waypointTilt, deviceTilt));

        float elevation = -(deviceTilt - waypointTilt);
        Log.d(TAG, String.format("%f", elevation));

        if(elevation >= Math.PI / 2)
        {
            pitch = (float)(Math.pow(2, 64));
        }

        else if(elevation <= -Math.PI / 2)
        {
            pitch = (float)(Math.pow(2, pitchHighLim));
        }

        else
        {
            double gradientAngle = Math.toDegrees(Math.atan((pitchHighLim - pitchLowLim) / Math.PI));

            float grad = (float)(Math.tan(Math.toRadians(gradientAngle)));
            float intercept = (float)(pitchHighLim - Math.PI / 2 * grad);

            pitch = (float)(Math.pow(2, grad * -elevation + intercept));
        }

        float gain;
/*        if(Math.abs(tilt) > 0.175)      // 0.175rad = ~10deg
        {
            gain = 1.f;
        }
        else
        {
            gain = (float)(0.5/0.175*Math.abs(tilt) + 0.5);
        }*/
        gain = 1.f;

//        Log.d(TAG, String.format("waypoint %f %f %f", waypointPose.getTranslation()[0], waypointPose.getTranslation()[1], waypointPose.getTranslation()[2]));
//        Log.d(TAG, String.format("Cam %f %f %f", cameraVector[0], cameraVector[1], cameraVector[2]));
        JNIBridge.playSound(waypointPose.getTranslation(), cameraVector, gain, pitch);
    }
}
