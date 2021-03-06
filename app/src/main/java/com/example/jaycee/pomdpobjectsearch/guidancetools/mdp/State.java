package com.example.jaycee.pomdpobjectsearch.guidancetools.mdp;

import com.example.jaycee.pomdpobjectsearch.Objects;

import static com.example.jaycee.pomdpobjectsearch.guidancetools.Params.ANGLE_INTERVAL;
import static com.example.jaycee.pomdpobjectsearch.guidancetools.Params.GRID_SIZE_PAN;
import static com.example.jaycee.pomdpobjectsearch.guidancetools.Params.GRID_SIZE_TILT;
import static com.example.jaycee.pomdpobjectsearch.guidancetools.Params.MAX_STEPS;
import static com.example.jaycee.pomdpobjectsearch.guidancetools.Params.NUM_OBJECTS;
import static com.example.jaycee.pomdpobjectsearch.guidancetools.Params.S_OBS;
import static com.example.jaycee.pomdpobjectsearch.guidancetools.Params.S_STATE_VISITED;
import static com.example.jaycee.pomdpobjectsearch.guidancetools.Params.S_STEPS;

class State
{
    private static final String TAG = State.class.getSimpleName();

    private int state;

    private int steps = 0;
    private int stateVisted = 0;

    private Objects.Observation observation = Objects.Observation.O_NOTHING;

    private int[] panHistory = new int[GRID_SIZE_PAN];
    private int[] tiltHistory = new int[GRID_SIZE_TILT];

    State()
    {
        for(int i = 0; i < GRID_SIZE_PAN; i ++)
        {
            panHistory[i] = 0;
        }
        for(int i = 0; i < GRID_SIZE_TILT; i ++)
        {
            tiltHistory[i] = 0;
        }
    }

    int getDecodedState()
    {
        int state = 0;
        int multiplier = 1;

        state += (multiplier * observation.getCode());
        multiplier *= NUM_OBJECTS;
        state += (multiplier * steps);
        multiplier *= MAX_STEPS;
        state += (multiplier * stateVisted);

        return state;
    }

    int[] getEncodedState()
    {
        int[] stateVector = new int[3];
        int state = this.state;

        stateVector[S_OBS] = state % NUM_OBJECTS;
        state /= NUM_OBJECTS;
        stateVector[S_STEPS] = state % MAX_STEPS;
        state /= MAX_STEPS;
        stateVector[S_STATE_VISITED] = state % 2;

        return stateVector;
    }

    void addObservation(Objects.Observation observation, float fpan, float ftilt)
    {
        // Origin is top right, not bottom left
        int pan = (int)((Math.floor(Math.toDegrees(fpan)/ANGLE_INTERVAL)) + GRID_SIZE_PAN/2 - 1);
        int tilt = (int)((Math.floor(Math.toDegrees(ftilt)/ANGLE_INTERVAL)) + GRID_SIZE_TILT/2 - 1);

        if(pan < 0) pan = GRID_SIZE_PAN - 1;
        if(pan > GRID_SIZE_PAN - 1) pan = 0;
        if(tilt < 0) tilt = GRID_SIZE_TILT - 1;
        if(tilt > GRID_SIZE_TILT - 1) tilt = 0;

        this.observation = observation;
        if(this.steps != MAX_STEPS-1) this.steps ++;

        if(panHistory[pan] == 1 && tiltHistory[tilt] == 1) this.stateVisted = 1;
        else this.stateVisted = 0;

        panHistory[pan] = 1;
        tiltHistory[tilt] = 1;

        this.state = getDecodedState();
    }
}
