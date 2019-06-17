package com.example.jaycee.mdpobjectsearch;

import android.graphics.Bitmap;

import com.google.ar.core.CameraIntrinsics;

public interface BarcodeListener
{
    void onBarcodeScannerStart();
    void onBarcodeScannerStop();
    Objects.Observation onBarcodeCodeRequest();
    void onPreviewRequest();
}
