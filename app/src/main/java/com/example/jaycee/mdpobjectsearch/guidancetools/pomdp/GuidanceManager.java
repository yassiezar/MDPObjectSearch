package com.example.jaycee.mdpobjectsearch.guidancetools.pomdp;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.jaycee.mdpobjectsearch.CameraActivityBase;
import com.example.jaycee.mdpobjectsearch.Objects;
import com.example.jaycee.mdpobjectsearch.SoundGenerator;
import com.example.jaycee.mdpobjectsearch.guidancetools.GuidanceInterface;
import com.example.jaycee.mdpobjectsearch.helpers.ClassHelpers;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;

import java.io.Serializable;

import static com.example.jaycee.mdpobjectsearch.guidancetools.Params.HORIZON_DISTANCE;
import static com.example.jaycee.mdpobjectsearch.guidancetools.Params.S_STEPS;

public class GuidanceManager implements Runnable
{
    private static final String TAG = GuidanceManager.class.getSimpleName();

    private Waypoint waypoint;
    private Session session;

    private HandlerThread guidanceHandlerThread;
    private Handler guidanceHandler;

    private State state;
    private Belief belief;
    private Policy policy;
    private Model model;

    private Pose devicePose;

    private GuidanceInterface guidanceInterface;

    private SoundGenerator soundGenerator;

    private Objects.Observation observation = Objects.Observation.O_NOTHING;

    public GuidanceManager(Session session, Pose pose, Context context, Objects.Observation target, int noise)
    {
        this.guidanceInterface = (GuidanceInterface)context;

        this.waypoint = new Waypoint(session, pose);
        this.session = session;

        this.state = new State();
        this.policy = new Policy(context);
        this.model = new Model(context, target);
        this.belief = new Belief(model);
        this.policy.setTarget(target, noise);

        this.soundGenerator = new SoundGenerator();

        guidanceHandlerThread = new HandlerThread("GuidanceThread");
        guidanceHandlerThread.start();
        guidanceHandler = new Handler(guidanceHandlerThread.getLooper());
    }

    public void end()
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

        waypoint.clear();
        waypoint = null;
        state = null;
        policy = null;
        belief = null;
        model = null;
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

    public void provideGuidance(Objects.Observation observation)
    {
        Policy.ActionId actionId;
        if(observation.getCode() == -1 || state.getEncodedState()[S_STEPS] > HORIZON_DISTANCE)
        {
            actionId = policy.getAction(belief.getBelief(), HORIZON_DISTANCE);
        }
        else
        {
            actionId = policy.getAction(observation.getCode(), state);
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

    @Override
    public void run()
    {
        Log.d(TAG, "Guidance running");
        devicePose = guidanceInterface.onDevicePoseRequested();

        Objects.Observation newObservation = guidanceInterface.onObservationRequest();

        if(waypointReached() || (newObservation != observation && newObservation != Objects.Observation.O_NOTHING))
        {
            observation = newObservation;
            provideGuidance(newObservation);
        }

        soundGenerator.playSound(waypoint.getWaypointPose(), getCameraVector());
        guidanceHandler.postDelayed(this, 40);
    }

    public void start()
    {
        guidanceHandler.post(this);
    }
}
