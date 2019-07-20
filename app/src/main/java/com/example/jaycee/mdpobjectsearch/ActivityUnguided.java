package com.example.jaycee.mdpobjectsearch;

import android.speech.tts.TextToSpeech;
import android.util.Log;


import com.example.jaycee.mdpobjectsearch.helpers.ClassHelpers;

import java.util.Locale;

import static com.example.jaycee.mdpobjectsearch.Objects.getObservation;
import static com.example.jaycee.mdpobjectsearch.guidancetools.Params.NUM_OBJECTS;

public class ActivityUnguided extends CameraActivityBase
{
    private static final String TAG = ActivityUnguided.class.getSimpleName();
    private static final int SPEECH_FREQUENCY = 1000;       // 1s between utterances

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
            cameraVector.normalise();
            ClassHelpers.mVector markerVector = new ClassHelpers.mVector(marker.getAngles());
            markerVector.normalise();
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

            double mean = 0.0;
            double std = Math.PI/6;
            double max = 1.0/(std*Math.sqrt(2*Math.PI));
            double detectionNoise = max*Math.exp(-0.5*Math.pow((angle - mean)/std, 2));
            int id = marker.getId();

            if(id != 0 && Math.random() > detectionNoise)
            {
                id = 0;
            }

            Log.i(TAG, String.format("ID: %d angle: %f noise %f", id, angle, detectionNoise/max));

            double classifierNoise = getQualitySetting()*NOISE_INTERVAL;
            if(id != 0 && Math.random() < classifierNoise)
            {
                int objectIndex;
                do
                {
                    objectIndex = (int)(Math.random()*(NUM_OBJECTS - 1) + 1);
                }while(objectIndex != id);
                id = objectIndex;
            }

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
