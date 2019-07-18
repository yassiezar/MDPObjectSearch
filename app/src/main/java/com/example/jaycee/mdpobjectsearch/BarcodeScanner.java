package com.example.jaycee.mdpobjectsearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.jaycee.mdpobjectsearch.helpers.ClassHelpers;
import com.example.jaycee.mdpobjectsearch.rendering.SurfaceRenderer;
import com.google.ar.core.CameraIntrinsics;

import java.util.Arrays;

import static com.example.jaycee.mdpobjectsearch.Objects.getObservation;

public class BarcodeScanner implements Runnable
{
    private static final String TAG = BarcodeScanner.class.getSimpleName();

    private Bitmap test;

    private SurfaceRenderer renderer;
    private BarcodeListener barcodeListener;

    private boolean running = false;

    public interface BarcodeListener
    {
        void onBarcodeScannerStart(CameraIntrinsics intrinsics);
        void onBarcodeScannerStop();
        void onScanRequest();
        void onScanComplete(BarcodeInformation barcode);
    }

    public BarcodeScanner(Context context, SurfaceRenderer renderer, CameraIntrinsics intrinsics, int scannerWidth, int scannerHeight)
    {
        this.renderer = renderer;
        this.barcodeListener = (BarcodeListener)context;

        this.test = Bitmap.createBitmap(scannerWidth, scannerHeight, Bitmap.Config.ARGB_8888);

        float[] focalLength = intrinsics.getFocalLength().clone();
        float[] principlePoint = intrinsics.getPrincipalPoint().clone();
        float[] distortionMatrix = new float[] {1.f, 0.00486219f, -0.44772422f, -0.01138138f, 0.0291972f, 0.70109351f};

        if(!JNIBridge.initDetector(scannerWidth, scannerHeight, focalLength, principlePoint, distortionMatrix))
        {
            Log.e(TAG, "Error starting scanner: ");
        }
    }

    @Override
    public void run()
    {
        running = true;
        BarcodeInformation info = new BarcodeInformation(-1, false, 0, 0, 0, 0, 0, 0);
        renderer.getScanner().getLock().lock();
        try
        {
            info = JNIBridge.processImage(test, renderer.getScanner().getBuffer());
//            Log.i(TAG, String.format("Barcode ID: %d angles: %s quaternion: %s", info.getId(), Arrays.toString(info.getAngles()), Arrays.toString(info.getRotationQuaternion())));
        }
        catch(Exception e)
        {
            Log.e(TAG, "Scanning error: " + e);
        }
        finally
        {
            renderer.getScanner().getLock().unlock();
        }
        running = false;
        barcodeListener.onScanComplete(info);
    }

    public void stop()
    {
        renderer.getScanner().getLock().lock();
        try
        {
            JNIBridge.killDetector();
        }
        finally
        {
            renderer.getScanner().getLock().unlock();
        }
    }

    public boolean isRunning() { return running; }

    public Bitmap getBarcodeDetectionPreview()
    {
        return this.test;
    }

    public class BarcodeInformation
    {
        private float[] angles = new float[3];
        private float roll, pitch, yaw, x, y, z, d;
        private int id;
        private boolean valid;

        public BarcodeInformation(int id, boolean valid, float roll, float pitch, float yaw, float x, float y, float z)
        {
            this.id = id;
            this.roll = roll;//+(float)Math.PI/4;
            this.pitch = pitch;//-(float)Math.PI/3;
            this.yaw = yaw;
            this.x = x;
            this.y = y;
            this.z = z;
            this.angles[0] = roll;//+(float)Math.PI/4;
            this.angles[1] = pitch;//-(float)Math.PI/3;
            this.angles[2] = yaw;

/*            this.yaw = 0;
            this.angles[2] = 0;*/
        }

        public int getId()
        {
            return id;
        }
        public float[] getAngles() { return angles; }
        public boolean getValid() { return valid; }

        public float[] getRotationQuaternion()
        {
            // x y z w
            float[] quat = new float[4];

            /* From phone perspective
             * 1. yaw around y
             * 2. pitch around x
             * 3. roll around z
             */

            /* From barcode perspective
             * 1. roll around y
             * 2. pitch around x
             * 3. yaw around z
             */

            // https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
            // 1st conversion
/*            quat[0] = (float)(Math.sin(roll/2)*Math.cos(pitch/2)*Math.cos(yaw/2) - Math.cos(roll/2)*Math.sin(pitch/2)*Math.sin(yaw/2));
            quat[1] = (float)(Math.cos(roll/2)*Math.sin(pitch/2)*Math.cos(yaw/2) + Math.sin(roll/2)*Math.cos(pitch/2)*Math.sin(yaw/2));
            quat[2] = (float)(Math.cos(roll/2)*Math.cos(pitch/2)*Math.sin(yaw/2) - Math.sin(roll/2)*Math.sin(pitch/2)*Math.cos(yaw/2));
            quat[3] = (float)(Math.cos(roll/2)*Math.cos(pitch/2)*Math.cos(yaw/2) + Math.sin(roll/2)*Math.sin(pitch/2)*Math.sin(yaw/2));*/
            // 2nd conversion method
            quat[0] = (float)(Math.cos(yaw/2)*Math.cos(pitch/2)*Math.sin(roll/2) - Math.sin(yaw/2)*Math.sin(pitch/2)*Math.cos(roll/2));
            quat[1] = (float)(Math.sin(yaw/2)*Math.cos(pitch/2)*Math.sin(roll/2) + Math.cos(yaw/2)*Math.sin(pitch/2)*Math.cos(roll/2));
            quat[2] = (float)(Math.sin(yaw/2)*Math.cos(pitch/2)*Math.cos(roll/2) - Math.cos(yaw/2)*Math.sin(pitch/2)*Math.sin(roll/2));
            quat[3] = (float)(Math.cos(yaw/2)*Math.cos(pitch/2)*Math.cos(roll/2) + Math.sin(yaw/2)*Math.sin(pitch/2)*Math.sin(roll/2));

/*            quat[0] = (float)(Math.cos(pitch/2)*Math.cos(roll/2)*Math.sin(yaw/2) - Math.sin(pitch/2)*Math.sin(roll/2)*Math.cos(yaw/2));
            quat[1] = (float)(Math.sin(pitch/2)*Math.cos(roll/2)*Math.sin(yaw/2) + Math.cos(pitch/2)*Math.sin(roll/2)*Math.cos(yaw/2));
            quat[2] = (float)(Math.sin(pitch/2)*Math.cos(roll/2)*Math.cos(yaw/2) - Math.cos(pitch/2)*Math.sin(roll/2)*Math.sin(yaw/2));
            quat[3] = (float)(Math.cos(pitch/2)*Math.cos(roll/2)*Math.cos(yaw/2) + Math.sin(pitch/2)*Math.sin(roll/2)*Math.sin(yaw/2));*/

            // Normalise
            float z = 0;
            for(float i : quat)
            {
                z += (i*i);
            }
            z = (float)Math.sqrt(z);
            for(int i = 0; i < quat.length; i++)
            {
                quat[i] /= z;
            }

            return quat;
        }

        public ClassHelpers.mVector getSurfaceNormal()
        {
            ClassHelpers.mQuaternion barcodeQuaternion = new ClassHelpers.mQuaternion(getRotationQuaternion());
            barcodeQuaternion.normalise();
            ClassHelpers.mVector surfaceNormal = new ClassHelpers.mVector(0, 0, 1);
            surfaceNormal.normalise();

            surfaceNormal.rotateByQuaternion(barcodeQuaternion);
            surfaceNormal.normalise();

            return surfaceNormal;

/*            float x = (float)(Math.sin(pitch));
            float y = (float)(Math.sin(roll));
            float z = (float)(Math.cos(pitch));

            ClassHelpers.mVector ret = new ClassHelpers.mVector(new float[] {x, y, z});
            ret.normalise();

            return ret;*/
        }
    }
}
