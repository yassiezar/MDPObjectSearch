package com.example.jaycee.pomdpobjectsearch.pomdptools;

final class Params
{
    static final int GRID_SIZE_TILT = 6;
    static final int GRID_SIZE_PAN = 6;
    static final int ANGLE_INTERVAL = 15;

/*    static final int O_NOTHING = 0;
    static final int O_COMPUTER_MONITOR = 1;
    static final int O_COMPUTER_KEYBOARD = 2;
    static final int O_COMPUTER_MOUSE = 3;
    static final int O_DESK = 4;
    static final int O_LAPTOP = 5;
    static final int O_MUG = 6;
    static final int O_OFFICE_SUPPLIES = 7;
    static final int O_WINDOW = 8;*/

    static final int A_UP = 0;
    static final int A_DOWN = 1;
    static final int A_LEFT = 2;
    static final int A_RIGHT = 3;

    static final long SEARCH_TIME_LIMIT = 180000;
    static final int HORIZON_DISTANCE = 15;

    static final int NUM_OBJECTS = 16;
    static final int MAX_STEPS = 16;
    static final int NUM_STATES = NUM_OBJECTS*MAX_STEPS*2;

    static final int S_OBS = 0;
    static final int S_STEPS = 1;
    static final int S_STATE_VISITED = 2;
}
