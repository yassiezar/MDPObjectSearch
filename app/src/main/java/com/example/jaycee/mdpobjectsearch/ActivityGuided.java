package com.example.jaycee.mdpobjectsearch;

import android.util.Log;

import com.example.jaycee.mdpobjectsearch.guidancetools.GuidanceInterface;
import com.example.jaycee.mdpobjectsearch.guidancetools.pomdp.GuidanceManager;
import com.example.jaycee.mdpobjectsearch.helpers.ClassHelpers;
import com.google.ar.core.Pose;

import static com.example.jaycee.mdpobjectsearch.Objects.getObservation;
import static com.example.jaycee.mdpobjectsearch.guidancetools.Params.NUM_OBJECTS;

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

        guidanceManager = new GuidanceManager(getSession(), devicePose, ActivityGuided.this, target, getQualitySetting());
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
    public void onScanComplete(MarkerScanner.MarkerInformation[] markers)
    {
        super.onScanComplete(markers);

        for(MarkerScanner.MarkerInformation marker : markers)
        {
            ClassHelpers.mVector cameraVector = ClassHelpers.getCameraVector(devicePose);
            cameraVector.normalise();
            ClassHelpers.mVector markerVector = new ClassHelpers.mVector(marker.getAngles());
            markerVector.normalise();
            markerVector.rotateByQuaternion(devicePose.getRotationQuaternion());
            markerVector.normalise();

            double angle = cameraVector.getAngleBetweenVectors(markerVector);
            if(angle > Math.PI/2)
            {
                angle -= Math.PI;
            }
            else if(angle < -Math.PI/2)
            {
                angle += Math.PI;
            }

            int id = marker.getId();
            Log.i(TAG, String.format("ID: %d angle: %f", id, angle));

            id = addNoise(id, angle);
/*            double mean = 0.0;
            double std = Math.PI/6;
            double max = 1.0/(std*Math.sqrt(2*Math.PI));
            double detectionNoise = max*Math.exp(-0.5*Math.pow((angle - mean)/std, 2));

            if(id != 0 && Math.random() > detectionNoise)
            {
                id = 0;
            }

            Log.i(TAG, String.format("ID: %d angle: %f noise %f", id, angle, detectionNoise/max));

            double classifierNoise = getQualitySetting()*NOISE_INTERVAL;
            if(id != 0 && Math.random() < classifierNoise)
            {
                int objectIndex;
                do
                {
                    objectIndex = (int)(Math.random()*(NUM_OBJECTS - 1) + 1);
                }while(objectIndex != id);
                id = objectIndex;
            }*/

            this.observation = getObservation(id);
            metrics.addFilteredObservation(observation);
            if(observation == targetObservation)
            {
                onTargetFound();
                break;
            }
        }
    }

    @Override
    public void onScanRequest()
    {
        super.onScanRequest();
        if(markerScanner != null && !markerScanner.isRunning())
        {
            scannerHandler.post(markerScanner);
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
            Pose waypointPose = guidanceManager.getWaypointPose();
            metrics.updateWaypointPosition(waypointPose);

            return waypointPose;
        }
        return null;
    }
}
