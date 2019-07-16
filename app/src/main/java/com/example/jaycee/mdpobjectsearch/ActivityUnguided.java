package com.example.jaycee.mdpobjectsearch;

import android.speech.tts.TextToSpeech;
import android.util.Log;


import com.google.ar.core.CameraIntrinsics;

import java.util.Locale;

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
    public void onScanRequest(CameraIntrinsics intrinsics)
    {
        super.onScanRequest(intrinsics);
        if(barcodeScanner == null)
        {
            onBarcodeScannerStart(intrinsics);
        }

        if(!barcodeScanner.isRunning() && System.currentTimeMillis() - initTime > SPEECH_FREQUENCY)
        {
            scannerHandler.post(barcodeScanner);
            initTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onScanComplete(Objects.Observation observation)
    {
        super.onScanComplete(observation);
        tts.speak(observation.getFriendlyName(), TextToSpeech.QUEUE_ADD, null, "");
        if(observation == target)
        {
            onTargetFound();
        }
    }
}
