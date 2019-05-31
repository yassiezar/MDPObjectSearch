package com.example.jaycee.pomdpobjectsearch;

import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class ActivityUnguided extends CameraActivityBase implements CameraSurface.ScreenReadRequest
{
    private static final String TAG = ActivityUnguided.class.getSimpleName();

    private TextToSpeech tts;

    private int target;

    @Override
    public void targetSelected(int target)
    {
        this.target = target;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        getCameraSurface().setScreenReadRequest(this);

        tts = new TextToSpeech(ActivityUnguided.this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
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
    public void onScreenTap()
    {
        long observation = onBarcodeCodeRequest();

        tts.speak(objectCodeToString(observation), TextToSpeech.QUEUE_ADD, null, "");

        if(observation == target)
        {
            getVibrator().vibrate(350);
        }
    }
}
