package com.example.jaycee.mdpobjectsearch;

public interface BarcodeListener
{
    void onBarcodeScannerStart();
    void onBarcodeScannerStop();
    Objects.Observation onBarcodeCodeRequest();
}
