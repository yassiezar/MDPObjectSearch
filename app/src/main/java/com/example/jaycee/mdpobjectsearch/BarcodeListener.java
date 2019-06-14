package com.example.jaycee.mdpobjectsearch;

import com.google.ar.core.CameraIntrinsics;

public interface BarcodeListener
{
    void onBarcodeScannerStart();
    void onBarcodeScannerStop();
    Objects.Observation onBarcodeCodeRequest();
}
