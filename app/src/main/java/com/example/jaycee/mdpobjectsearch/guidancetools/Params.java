package com.example.jaycee.mdpobjectsearch.guidancetools;

public final class Params
{
    public static final int GRID_SIZE_TILT = 6;
    public static final int GRID_SIZE_PAN = 12;
    public static final int ANGLE_INTERVAL = 15;

    public static final int A_UP = 0;
    public static final int A_DOWN = 1;
    public static final int A_LEFT = 2;
    public static final int A_RIGHT = 3;

    public static final long SEARCH_TIME_LIMIT = 120000;
    public static final int HORIZON_DISTANCE = 6;

    public static final int NUM_OBJECTS = 15;
    public static final int MAX_STEPS = 8;
    public static final int NUM_STATES = NUM_OBJECTS*MAX_STEPS*2;

    public static final int S_OBS = 0;
    public static final int S_STEPS = 1;
    public static final int S_STATE_VISITED = 2;
}
