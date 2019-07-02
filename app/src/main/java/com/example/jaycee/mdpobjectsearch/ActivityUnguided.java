package com.example.jaycee.mdpobjectsearch;

import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.ar.core.CameraIntrinsics;

import java.util.Locale;

public class ActivityUnguided extends CameraActivityBase
{
    private static final String TAG = ActivityUnguided.class.getSimpleName();

    private TextToSpeech tts;

    private Objects.Observation target;

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
                Log.e("error", "Initialisation Failed!");
            }
        });
    }

    @Override
    protected void onPause()
    {
        if(tts != null)
        {
            tts.shutdown();
        }

        super.onPause();
    }

    @Override
    public void onScanComplete(Objects.Observation observation)
    {
        tts.speak(observation.getFriendlyName(), TextToSpeech.QUEUE_ADD, null, "");

        if(observation == target)
        {
            onTargetFound();
        }
    }
}
