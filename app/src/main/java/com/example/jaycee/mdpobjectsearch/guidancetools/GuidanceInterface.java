package com.example.jaycee.mdpobjectsearch.guidancetools;

import com.example.jaycee.mdpobjectsearch.Objects;
import com.google.ar.core.Pose;

public interface GuidanceInterface
{
    void onGuidanceStart(Objects.Observation target);
    void onGuidanceEnd();
    void onGuidanceRequested(Objects.Observation observation);
    void onGuidanceLoop();
    boolean onWaypointReached();
    Pose onDrawWaypoint();
    Pose onWaypointPoseRequested();
    float[] onCameraVectorRequested();
}
