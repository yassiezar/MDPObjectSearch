package com.example.jaycee.pomdpobjectsearch;

public interface BarcodeListener
{
    void onBarcodeScannerStart();
    void onBarcodeScannerStop();
    Objects.Observation onBarcodeCodeRequest();
}
