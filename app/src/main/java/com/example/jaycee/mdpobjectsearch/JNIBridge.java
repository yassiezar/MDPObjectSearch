package com.example.jaycee.mdpobjectsearch;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class JNIBridge
{
    static
    {
        System.loadLibrary("JNI");
    }

    public static native boolean initSound();
    public static native boolean killSound();
    public static native void playSound(float[] src, float[] list, float gain, float pitch);

    public static native boolean initDetector(int width, int height, float[] focalLength, float[] principlePoint, float[] distortionMatrix);
    public static native boolean killDetector();
    public static native BarcodeScanner.BarcodeInformation processImage(Bitmap bitmap, ByteBuffer data);
    public static native boolean getBitmap(Bitmap bitmap, ByteBuffer data, int width, int height, int bpp);
}
