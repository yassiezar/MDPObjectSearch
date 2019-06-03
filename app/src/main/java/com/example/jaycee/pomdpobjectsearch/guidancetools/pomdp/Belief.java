package com.example.jaycee.pomdpobjectsearch.guidancetools.pomdp;

import java.util.Collections;

import static com.example.jaycee.pomdpobjectsearch.guidancetools.Params.NUM_STATES;

public class Belief
{
    private Policy.DoubleVector belief;
    private int numStates = 0;
    private Model model;

    public Belief(Model model)
    {
        this.numStates = NUM_STATES;
        belief = new Policy.DoubleVector(Collections.nCopies(numStates, 1.0/numStates));
        this.model = model;
    }

    public void updateBeliefState(int a, int o)
    {
        for(int s = 0; s < numStates; s++)
        {
            double sum = 0.0;
            for(int s1 = 0; s1 < numStates; s1++)
            {
                sum += model.getTransitionProbability(s, a, s1);
            }
            belief.vector.set(s, model.getObservationProbability(s, a, o)*sum);
        }
    }

    public Policy.DoubleVector getBelief() { return belief; }
}
