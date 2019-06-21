package com.example.jaycee.mdpobjectsearch.guidancetools;

import com.example.jaycee.mdpobjectsearch.Objects;
import com.google.ar.core.Pose;

public interface GuidanceInterface
{
    void onTargetFound();
    Pose onDevicePoseRequested();
    Pose onWaypointPoseRequested();
    Objects.Observation onObservationRequest();
}
