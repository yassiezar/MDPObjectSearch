package com.example.jaycee.mdpobjectsearch.guidancetools.mdp;

import android.content.Context;

import com.example.jaycee.mdpobjectsearch.Objects;
import com.example.jaycee.mdpobjectsearch.helpers.ClassHelpers;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;

public class GuidanceManager
{
    private static final String TAG = GuidanceManager.class.getSimpleName();

    private Waypoint waypoint;
    private State state;
    private Policy policy;

    private Pose devicePose;

    public GuidanceManager(Session session, Pose pose, Context context, Objects.Observation target)
    {
        devicePose = pose;
        waypoint = new Waypoint(session, pose);
        state = new State();
        policy = new Policy(context, target);
    }

    public void end()
    {
        state = null;
        waypoint.clear();
        waypoint = null;
    }

    // Called on every sound emission loop
    public void updateDevicePose(Pose pose)
    {
        devicePose = pose;
    }

    public boolean waypointReached()
    {
        float[] phoneRotationAngles = getCameraVector();
        float cameraPan = -phoneRotationAngles[1];
        float cameraTilt = -phoneRotationAngles[2];

        float x = waypoint.getWaypointPose().getTranslation()[0];
        float y = waypoint.getWaypointPose().getTranslation()[1];

        // Compensate for Z-axis going in negative direction, rotating pan around y-axis

/*        Log.d(TAG, String.format("x: %f y %f", Math.cos(-cameraPan+Math.PI/2) + x, Math.sin(cameraTilt) - y));*/
        return Math.abs(Math.sin(cameraTilt) - y) < 0.1 && Math.abs(Math.cos(-cameraPan+Math.PI/2) + x) < 0.1;
    }

    public void provideGuidance(Session session, Objects.Observation observation)
    {
        long action = policy.getAction(state);
        float[] phoneRotationAngles = getCameraVector();
        float cameraPan = -phoneRotationAngles[1];
        float cameraTilt = -phoneRotationAngles[2];

        waypoint.updateWaypoint(action, session, cameraPan, cameraTilt, devicePose.tz());

        state.addObservation(observation, cameraPan, cameraTilt);
    }

    public Pose getWaypointPose() { return waypoint.getWaypointPose(); }

    public float[] getCameraVector()
    {
        ClassHelpers.mVector cameraVector = ClassHelpers.getCameraVector(this.devicePose);
        return cameraVector.getEuler();
    }
}
