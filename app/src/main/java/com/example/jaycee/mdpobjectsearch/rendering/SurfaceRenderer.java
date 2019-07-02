package com.example.jaycee.mdpobjectsearch.rendering;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.jaycee.mdpobjectsearch.RenderListener;
import com.example.jaycee.mdpobjectsearch.ScannerWindow;
import com.example.jaycee.mdpobjectsearch.guidancetools.GuidanceInterface;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SurfaceRenderer implements GLSurfaceView.Renderer
{
    private static final String TAG = SurfaceRenderer.class.getSimpleName();

    private Context context;

    private BackgroundRenderer backgroundRenderer;
//    private ObjectRenderer objectRenderer;
    private ObjectRenderer waypointRenderer;

    private GuidanceInterface guidanceInterface;
    private RenderListener renderListener;

     private ScannerWindow scanner;

    private final float[] anchorMatrix = new float[16];

    private boolean drawWaypoint = false;

    public SurfaceRenderer(Context context)
    {
        this.context = context;

        this.renderListener = (RenderListener)context;

        init();
    }

    public void init()
    {
        backgroundRenderer = new BackgroundRenderer();
//        objectRenderer = new ObjectRenderer();
        waypointRenderer = new ObjectRenderer();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig)
    {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        Log.i(TAG, "Surface created");
        try
        {
            backgroundRenderer.createOnGlThread(context);
//            objectRenderer.createOnGlThread(context, "models/arrow/Arrow.obj", "models/arrow/Arrow_S.tga");
            waypointRenderer.createOnGlThread(context, "models/andy.obj", "models/andy.png");

//            objectRenderer.setMaterialProperties(0.f, 2.f, 0.5f, 6.f);
            waypointRenderer.setMaterialProperties(0.f, 2.f, 0.5f, 6.f);
        }
        catch(IOException e)
        {
            Log.e(TAG, "Failed to read asset file. ", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height)
    {
        Log.i(TAG, "Surface changed");
        renderListener.onViewportChange(width, height);
        if(scanner == null)
        {
            this.scanner = new ScannerWindow(width, height);
            backgroundRenderer.setScannerWindow(scanner);
        }
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10)
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        renderListener.onDrawRequest(backgroundRenderer.getTextureId());
        try
        {
            Frame frame = renderListener.onFrameRequest();
            Camera camera = frame.getCamera();

            backgroundRenderer.draw(frame);

            float[] projectionMatrix = new float[16];
            camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f);

            float[] viewMatrix = new float[16];
            camera.getViewMatrix(viewMatrix, 0);

            // Compute lighting from average intensity of the image.
            // The first three components are color scaling factors.
            // The last one is the average pixel intensity in gamma space.
            final float[] colourCorrectionRgba = new float[4];
            frame.getLightEstimate().getColorCorrection(colourCorrectionRgba, 0);

            float scaleFactor = 1.f;

            // Update the debug object
            if(camera.getTrackingState() == TrackingState.TRACKING)
            {
                if(drawWaypoint)
                {
                    Pose waypointPose = guidanceInterface.onWaypointPoseRequested();
                    if(waypointPose != null)
                    {
                        // Draw the waypoints as an Andyman
                        waypointPose.toMatrix(anchorMatrix, 0);
                        waypointRenderer.updateModelMatrix(anchorMatrix, scaleFactor);
                        waypointRenderer.draw(viewMatrix, projectionMatrix, colourCorrectionRgba);
                    }
                }
            }
            else
            {
                Log.v(TAG, "Camera not tracking or target not set. ");
            }
        }
        catch(Throwable t)
        {
            Log.e(TAG, "Exception on the GL Thread: " + t);
        }
    }

    public ScannerWindow getScanner()
    {
        return scanner;
    }

    public void setDrawWaypoint(boolean drawWaypoint)
    {
        this.guidanceInterface = (GuidanceInterface)context;
        this.drawWaypoint = drawWaypoint;
    }
}
