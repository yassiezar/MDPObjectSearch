package com.example.jaycee.mdpobjectsearch;

import android.speech.tts.TextToSpeech;
import android.util.Log;


import com.example.jaycee.mdpobjectsearch.helpers.ClassHelpers;
import com.google.ar.core.CameraIntrinsics;

import java.util.Arrays;
import java.util.Locale;

import static com.example.jaycee.mdpobjectsearch.Objects.getObservation;

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
        if(barcodeScanner != null && !barcodeScanner.isRunning() && System.currentTimeMillis() - initTime > SPEECH_FREQUENCY)
        {
            scannerHandler.post(barcodeScanner);
            initTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onScanComplete(BarcodeScanner.BarcodeInformation barcode)
    {
        super.onScanComplete(barcode);

        ClassHelpers.mVector cameraVector = ClassHelpers.getCameraVector(devicePose);
//        Log.i(TAG, String.format("ID: %d, Surface normal: %s Quaternion: %s", barcode.getId(), Arrays.toString(barcode.getSurfaceNormal().asFloat()), Arrays.toString(barcode.getRotationQuaternion())));
        Log.i(TAG, String.format("ID: %d, angles: %s", barcode.getId(), Arrays.toString(barcode.getAngles())));

        Objects.Observation observation = getObservation(barcode.getId());
        tts.speak(observation.getFriendlyName(), TextToSpeech.QUEUE_ADD, null, "");
        if(observation == target)
        {
            onTargetFound();
        }
    }
}
