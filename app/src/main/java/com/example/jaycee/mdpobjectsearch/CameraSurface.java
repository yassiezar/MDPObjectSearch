package com.example.jaycee.mdpobjectsearch;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.example.jaycee.mdpobjectsearch.rendering.SurfaceRenderer;

public class CameraSurface extends GLSurfaceView implements SurfaceHolder.Callback
{
    private static final String TAG = CameraSurface.class.getSimpleName();

    private SurfaceRenderer renderer;

    public CameraSurface(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        renderer = new SurfaceRenderer(context);

        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder)
    {
        super.surfaceCreated(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height)
    {
        super.surfaceChanged(surfaceHolder, format, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder)
    {
        super.surfaceDestroyed(surfaceHolder);
    }

    public SurfaceRenderer getRenderer()
    {
        return renderer;
    }
}
