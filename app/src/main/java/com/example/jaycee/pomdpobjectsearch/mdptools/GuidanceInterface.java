package com.example.jaycee.pomdpobjectsearch.mdptools;

import com.google.ar.core.Pose;

public interface GuidanceInterface
{
    void onGuidanceStart(int target);
    void onGuidanceEnd();
    void onGuidanceRequested(long observation);
    void onGuidanceLoop();
    boolean onWaypointReached();
    Pose onDrawWaypoint();
    Pose onWaypointPoseRequested();
    float[] onCameraVectorRequested();
}
