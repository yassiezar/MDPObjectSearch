package com.example.jaycee.mdpobjectsearch.rendering;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.example.jaycee.mdpobjectsearch.ScannerWindow;
import com.google.ar.core.Frame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BackgroundRenderer
{
    private static final String TAG = BackgroundRenderer.class.getSimpleName();

    private static final String VERTEX_SHADER_NAME = "shaders/screenquad.vert";
    private static final String FRAGMENT_SHADER_NAME = "shaders/screenquad.frag";

    private static final int COORDS_PER_VERTEX = 3;
    private static final int TEXCOORDS_PER_VERTEX = 2;
    private static final int FLOAT_SIZE = 4;
    private static final int INT_SIZE = 4;

    private ScannerWindow scanner;

    private FloatBuffer quadVertices;
    private FloatBuffer quadTexCoord;
    private FloatBuffer quadTexCoordTransformed;

    private ByteBuffer currentFrameBuffer;

    private int quadProgram;
    private int quadPositionParam;
    private int quadTexCoordParam;
    private int textureId = -1;

    private int scannerWidth, scannerHeight;
    private int scannerX, scannerY;

    public BackgroundRenderer(int scannerX, int scannerY, int scannerWidth, int scannerHeight)
    {
        this.scannerWidth = scannerWidth;
        this.scannerHeight = scannerHeight;
        this.scannerX = scannerX;
        this.scannerY = scannerY;

        this.scanner = new ScannerWindow(scannerWidth, scannerHeight);
    }

    public int getTextureId() { return this.textureId; }

    public void createOnGlThread(Context context) throws IOException
    {
        Log.i(TAG, "Initialised background renderer");

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];

        int textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

        GLES20.glBindTexture(textureTarget, textureId);
        GLES20.glTexParameterf(textureTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(textureTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(textureTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(textureTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        int numVertices = 4;
        if(numVertices != QUAD_COORDS.length / COORDS_PER_VERTEX)
        {
            throw new RuntimeException("Unexpected number of vertices in BackgroundRenderer");
        }

        ByteBuffer bbVertices = ByteBuffer.allocateDirect(QUAD_COORDS.length * FLOAT_SIZE);
        bbVertices.order(ByteOrder.nativeOrder());
        quadVertices = bbVertices.asFloatBuffer();
        quadVertices.put(QUAD_COORDS);
        quadVertices.position(0);

        ByteBuffer bbTexCoords = ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE);
        bbTexCoords.order(ByteOrder.nativeOrder());
        quadTexCoord = bbTexCoords.asFloatBuffer();
        quadTexCoord.put(QUAD_TEXCOORDS);
        quadTexCoord.position(0);

        ByteBuffer bbTexCoordsTransformed = ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE);
        bbTexCoordsTransformed.order(ByteOrder.nativeOrder());
        quadTexCoordTransformed = bbTexCoordsTransformed.asFloatBuffer();

        int vertexShader = ShaderUtils.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
        int fragmentShader = ShaderUtils.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME);

        quadProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(quadProgram, vertexShader);
        GLES20.glAttachShader(quadProgram, fragmentShader);
        GLES20.glLinkProgram(quadProgram);
        GLES20.glUseProgram(quadProgram);

        ShaderUtils.checkGLError(TAG, "Program creation.");

        quadPositionParam = GLES20.glGetAttribLocation(quadProgram, "a_Position");
        quadTexCoordParam = GLES20.glGetAttribLocation(quadProgram, "a_TexCoord");

        ShaderUtils.checkGLError(TAG, "Program parameters.");
    }

    public void draw(Frame frame)
    {
        Log.v(TAG, "Drawing background");
        if(frame.hasDisplayGeometryChanged())
        {
            frame.transformDisplayUvCoords(quadTexCoord, quadTexCoordTransformed);
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(false);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        GLES20.glUseProgram(quadProgram);

        GLES20.glVertexAttribPointer(quadPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadVertices);

        GLES20.glVertexAttribPointer(quadTexCoordParam, TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadTexCoordTransformed);

        GLES20.glEnableVertexAttribArray(quadPositionParam);
        GLES20.glEnableVertexAttribArray(quadTexCoordParam);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(quadPositionParam);
        GLES20.glDisableVertexAttribArray(quadTexCoordParam);

        GLES20.glDepthMask(true);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        ShaderUtils.checkGLError(TAG, "Draw");

        boolean lockAcquired = scanner.getLock().tryLock();
        if(lockAcquired && !scanner.getLock().hasQueuedThreads())
        {
            try
            {
                Log.v(TAG, "Saving camera image to buffer");
                GLES20.glReadPixels(scannerX, scannerY, scannerWidth, scannerHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, scanner.getBuffer());
            }
            finally
            {
                scanner.getLock().unlock();
            }
        }
    }

    public ScannerWindow getScanner()
    {
        return scanner;
    }

    private static final float[] QUAD_COORDS =
            new float[] {-1.f, -1.f, 0.f, -1.f, 1.f, 0.f, 1.f, -1.f, 0.f, 1.f, 1.f, 0.f};
    private static final float[] QUAD_TEXCOORDS =
            new float[] {0.f, 1.f, 0.f, 0.f, 1.f, 1.f, 1.f, 0.f};

}
