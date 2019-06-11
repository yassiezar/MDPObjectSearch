package com.example.jaycee.mdpobjectsearch;

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

    public static native boolean initDetector();
    public static native boolean killDetector();
    public static native void processImage(IntBuffer data);
}
