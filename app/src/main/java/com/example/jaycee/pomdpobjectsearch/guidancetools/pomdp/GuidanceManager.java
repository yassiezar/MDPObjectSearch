package com.example.jaycee.pomdpobjectsearch.guidancetools.pomdp;

import android.content.Context;

import com.example.jaycee.pomdpobjectsearch.Objects;
import com.example.jaycee.pomdpobjectsearch.helpers.ClassHelpers;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;

import static com.example.jaycee.pomdpobjectsearch.guidancetools.Params.HORIZON_DISTANCE;
import static com.example.jaycee.pomdpobjectsearch.guidancetools.Params.S_STEPS;

public class GuidanceManager
{
    private static final String TAG = GuidanceManager.class.getSimpleName();

    private Waypoint waypoint;
    private State state;
    private Belief belief;
    private Policy policy;
    private Model model;

    private Pose devicePose;

    private int id = -1;

    public GuidanceManager(Session session, Pose pose, Context context, Objects.Observation target)
    {
        this.waypoint = new Waypoint(session, pose);

        this.state = new State();
        this.policy = new Policy(context);
        this.policy.setTarget(target);
        this.model = new Model(context);
        this.belief = new Belief(model);
    }

    public void end()
    {
        waypoint.clear();
        waypoint = null;
        state = null;
        policy = null;
        belief = null;
        model = null;
    }

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
        return Math.abs(Math.sin(cameraTilt) - y) < 0.1 && Math.abs(Math.cos(-cameraPan+Math.PI/2) + x) < 0.1;
    }

    public void provideGuidance(Session session, Objects.Observation observation)
    {
        Policy.ActionId actionId;
        if(id == -1 || state.getEncodedState()[S_STEPS] > HORIZON_DISTANCE)
        {
            actionId = policy.getAction(belief.getBelief(), HORIZON_DISTANCE);
        }
        else
        {
            actionId = policy.getAction(id, state);
        }
        int action = actionId.action;
        belief.updateBeliefState(action, observation.getCode());

        float[] phoneRotationAngles = getCameraVector();
        float cameraPan = -phoneRotationAngles[1];
        float cameraTilt = -phoneRotationAngles[2];

        waypoint.updateWaypoint(action, session, cameraPan, cameraTilt, devicePose.tz());

        state.addObservation(observation, cameraPan, cameraTilt);
    }

    public float[] getCameraVector()
    {
        ClassHelpers.mVector cameraVector = ClassHelpers.getCameraVector(this.devicePose);
        return cameraVector.getEuler();
    }

    public Pose getWaypointPose() { return waypoint.getWaypointPose(); }
}
