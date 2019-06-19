package com.example.jaycee.mdpobjectsearch;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.locks.ReentrantLock;

public class ScannerWindow
{
    private static final String TAG = ScannerWindow.class.getSimpleName();
    private static final int INT_SIZE = 4;

    private ReentrantLock lock = new ReentrantLock();

    private ByteBuffer imageBuffer;

    private int width, height;

    public ScannerWindow(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.imageBuffer = ByteBuffer.allocateDirect(width*height*INT_SIZE);
        this.imageBuffer.order(ByteOrder.nativeOrder());
    }

    public ReentrantLock getLock() { return lock; }

    public ByteBuffer getBuffer() { return this.imageBuffer; }
}
