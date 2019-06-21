package com.example.jaycee.mdpobjectsearch;

import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.ar.core.CameraIntrinsics;

import java.util.Locale;

public class ActivityUnguided extends CameraActivityBase implements CameraSurface.ScreenReadRequest
{
    private static final String TAG = ActivityUnguided.class.getSimpleName();

    private TextToSpeech tts;

    private Objects.Observation target;

    private HandlerThread scannerHandlerThread;
    private Handler scannerHandler;

    @Override
    public void targetSelected(Objects.Observation target)
    {
        this.target = target;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        getCameraSurface().setScreenReadRequest(this);
        getCameraSurface().enableScreenTap(true);

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
    public void onScreenTap()
    {
        scannerHandler.post(barcodeScanner);
    }

    @Override
    public void onBarcodeScannerStart(CameraIntrinsics intrinsics)
    {
        scannerHandlerThread = new HandlerThread("BarcodeScanner thread");
        scannerHandlerThread.start();
        scannerHandler = new Handler(scannerHandlerThread.getLooper());

//        Log.i(TAG, "Focal len: %f principle point: %f" + Arrays.toString(getIntrinsics().getFocalLength()) + Arrays.toString(intrinsics.getPrincipalPoint()));
        barcodeScanner = new BarcodeScanner(this, getCameraSurface().getRenderer(), intrinsics, imageWidth, imageHeight);
        // barcodeScanner = new BarcodeScanner(1440, 2280, surfaceView.getRenderer(), new float[] {5522.19584f, 5496.99633f}, new float[] {2723.53276f, 2723.53276f}, distortionMatrix);    // Params measures from opencv calibration procedure
    }

    @Override
    public void onScanComplete(Objects.Observation observation)
    {
        tts.speak(observation.getFriendlyName(), TextToSpeech.QUEUE_ADD, null, "");

        if(observation == target)
        {
            getVibrator().vibrate(350);
        }
    }
}
