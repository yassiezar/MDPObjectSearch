package com.example.jaycee.mdpobjectsearch;

import android.speech.tts.TextToSpeech;
import android.util.Log;


import com.example.jaycee.mdpobjectsearch.helpers.ClassHelpers;

import java.util.Arrays;
import java.util.Locale;

import static com.example.jaycee.mdpobjectsearch.Objects.getObservation;

public class ActivityUnguided extends CameraActivityBase
{
    private static final String TAG = ActivityUnguided.class.getSimpleName();
    private static final int SPEECH_FREQUENCY = 500;       // 1s between utterances

    private TextToSpeech tts;

    private Objects.Observation target;

    private long initTime = System.currentTimeMillis();

    @Override
    public void targetSelected(Objects.Observation target)
    {
        super.targetSelected(target);

        this.target = target;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        tts = new TextToSpeech(ActivityUnguided.this, status ->
        {
            if(status == TextToSpeech.SUCCESS)
            {
                int result = tts.setLanguage(Locale.UK);
                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                {
                    Log.e("error", "This Language is not supported");
                }
            }
            else
            {
                Log.e("error", "TTS initialisation Failed!");
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(tts != null)
        {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    @Override
    public void onScanRequest()
    {
        super.onScanRequest();
        if(markerScanner != null && !markerScanner.isRunning() && System.currentTimeMillis() - initTime > SPEECH_FREQUENCY)
        {
            scannerHandler.post(markerScanner);
            initTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onScanComplete(MarkerScanner.MarkerInformation[] markers)
    {
        super.onScanComplete(markers);

        for(MarkerScanner.MarkerInformation marker : markers)
        {
            ClassHelpers.mVector cameraVector = ClassHelpers.getCameraVector(devicePose);
//            cameraVector.normalise();
            // swap to make axes line up properly
            float tmp = cameraVector.x;
            cameraVector.x = -cameraVector.y;
            cameraVector.y = -tmp;
            ClassHelpers.mVector markerVector = new ClassHelpers.mVector(marker.getPosition());
            markerVector.rotateByQuaternion(devicePose.getRotationQuaternion());
            markerVector.normalise();

            double angle = cameraVector.getAngleBetweenVectors(markerVector);
            if(angle > Math.PI/2)
            {
                angle -= Math.PI;
            }
            else if(angle < -Math.PI/2)
            {
                angle += Math.PI;
            }

            int id = marker.getId();
//            Log.i(TAG, String.format("ID: %d angle: %f", id, angle));

            id = addNoise(id, angle);

            Objects.Observation observation = getObservation(id);
            metrics.addFilteredObservation(observation);
            if (observation != Objects.Observation.O_NOTHING && observation != Objects.Observation.UNDEFINED)
            {
                tts.speak(observation.getFriendlyName(), TextToSpeech.QUEUE_ADD, null, "");
            }
            if(observation == target)
            {
                onTargetFound();
                break;
            }
        }
    }
}
