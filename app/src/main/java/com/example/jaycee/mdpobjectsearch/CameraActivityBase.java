package com.example.jaycee.mdpobjectsearch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.CameraIntrinsics;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.google.ar.core.ImageMetadata.LENS_RADIAL_DISTORTION;

public abstract class CameraActivityBase extends AppCompatActivity implements BarcodeListener, RenderListener
{
    private static final String TAG = CameraActivityBase.class.getSimpleName();

    private CameraSurface surfaceView;
    private DrawerLayout drawerLayout;
    private Toast toast;

    private Vibrator vibrator;

    private Session session;
    private CameraIntrinsics intrinsics;
    protected Pose devicePose;

    protected Metrics metrics;
    private BarcodeScanner barcodeScanner;

    private boolean requestARCoreInstall = true;
    private boolean highQualityScanner = false;

    private long currentTimestamp, startTimestamp;
    protected long frameTimestamp;
    private long barcodePreviewCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Intent intent = getIntent();
        highQualityScanner = intent.getBooleanExtra("ADD_NOISE", false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        surfaceView = findViewById(R.id.surfaceview);

        drawerLayout = findViewById(R.id.layout_drawer_objects);
        NavigationView navigationView = findViewById(R.id.navigation_view_objects);

        navigationView.setNavigationItemSelectedListener(item ->
        {
            Objects.Observation target;
            switch (item.getItemId())
            {
                case R.id.item_object_backpack:
                    target = Objects.Observation.T_BACKPACK;
                    break;
                case R.id.item_object_chair:
                    target = Objects.Observation.T_CHAIR;
                    break;
                case R.id.item_object_couch:
                    target = Objects.Observation.T_COUCH;
                    break;
                case R.id.item_object_desk:
                    target = Objects.Observation.T_DESK;
                    break;
                case R.id.item_object_door:
                    target = Objects.Observation.T_DOOR;
                    break;
                case R.id.item_object_keyboard:
                    target = Objects.Observation.T_COMPUTER_KEYBOARD;
                    break;
                case R.id.item_object_laptop:
                    target = Objects.Observation.T_LAPTOP;
                    break;
                case R.id.item_object_monitor:
                    target = Objects.Observation.T_COMPUTER_MONITOR;
                    break;
                case R.id.item_object_mouse:
                    target = Objects.Observation.T_COMPUTER_MOUSE;
                    break;
                case R.id.item_object_mug:
                    target = Objects.Observation.T_MUG;
                    break;
                case R.id.item_object_plant:
                    target = Objects.Observation.T_PLANT;
                    break;
                case R.id.item_object_telephone:
                    target = Objects.Observation.T_TELEPHONE;
                    break;
                case R.id.item_object_whiteboard:
                    target = Objects.Observation.T_WHITEBOARD;
                    break;
                default: target = Objects.Observation.O_NOTHING;
            }

            startTimestamp = 0;
            targetSelected(target);
            item.setCheckable(true);

            drawerLayout.closeDrawers();

            return true;
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(vibrator == null)
        {
            this.vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
        }

        if(session == null)
        {
            try
            {
                switch(ArCoreApk.getInstance().requestInstall(this, requestARCoreInstall))
                {
                    case INSTALLED:
                        break;
                    case INSTALL_REQUESTED:
                        requestARCoreInstall = false;
                        return;
                }

                session = new Session(this);

                // Set config settings
                Config conf = new Config(session);
                conf.setFocusMode(Config.FocusMode.AUTO);
                session.configure(conf);
            }
            catch(UnavailableUserDeclinedInstallationException | UnavailableArcoreNotInstalledException e)
            {
                Log.e(TAG, "Please install ARCore.");
                return;
            }
            catch(UnavailableDeviceNotCompatibleException e)
            {
                Log.e(TAG, "This device does not support ARCore.");
                return;
            }
            catch(UnavailableApkTooOldException e)
            {
                Log.e(TAG, "Please update the app.");
                return;
            }
            catch(UnavailableSdkTooOldException e)
            {
                Log.e(TAG, "Please update ARCore. ");
                return;
            }
            catch(Exception e)
            {
                Log.e(TAG, "Failed to create AR session.");
            }
        }

        try
        {
            session.resume();
        }
        catch(CameraNotAvailableException e)
        {
            session = null;
            Log.e(TAG, "Camera not available. Please restart app.");
            return;
        }

        surfaceView.onResume();

        if(!JNIBridge.initSound())
        {
            Log.e(TAG, "OpenAL init error");
        }
    }

    @Override
    protected void onPause()
    {
//        onBarcodeScannerStop();

        if(vibrator != null)
        {
            vibrator.cancel();
            vibrator = null;
        }

        if(session != null)
        {
            surfaceView.onPause();
            session.pause();
        }

        if(!JNIBridge.killSound())
        {
            Log.e(TAG, "OpenAL exit error");
        }

        if(barcodeScanner != null)
        {
            barcodeScanner.stop();
            barcodeScanner = null;
        }

        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Objects.Observation onBarcodeCodeRequest()
    {
        barcodeScanner.run();

        Objects.Observation scannedObject = Objects.Observation.O_NOTHING;
        if(barcodeScanner != null)
        {
            scannedObject = Objects.getObservation(barcodeScanner.getCode());
        }

        if(metrics != null)
        {
            metrics.updateObservation(scannedObject.getCode());
        }

        if(scannedObject != Objects.Observation.O_NOTHING)
        {
            if (toast != null)
            {
                toast.cancel();
            }
            toast = Toast.makeText(this, scannedObject.getFriendlyName(), Toast.LENGTH_SHORT);
            toast.show();
        }

        return scannedObject;
    }

    @Override
    public void onBarcodeScannerStart()
    {
        // TODO: Replace distortion matrix with real one
        float[] distortionMatrix = new float[] {1.f, 0.00486219f, -0.44772422f, -0.01138138f, 0.0291972f, 0.70109351f};

        barcodeScanner = new BarcodeScanner(1440, 2280, surfaceView.getRenderer(), intrinsics.getFocalLength(), intrinsics.getPrincipalPoint(), distortionMatrix);
        // barcodeScanner = new BarcodeScanner(525, 525, surfaceView.getRenderer(), new float[] {5522.19584f, 5496.99633f}, new float[] {2723.53276f, 2723.53276f}, distortionMatrix);    // Params measures from opencv calibration procedure
    }

    @Override
    public void onBarcodeScannerStop()
    {
        if(barcodeScanner != null)
        {
            barcodeScanner.stop();
            barcodeScanner = null;
        }
    }

    @Override
    public void onPreviewRequest()
    {
        try
        {
            String path = Environment.getExternalStorageDirectory().toString();
            OutputStream fOut = null;
            File file = new File(path, "whyconpreview"+barcodePreviewCounter+".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
            barcodePreviewCounter++;
            fOut = new FileOutputStream(file);

            Bitmap pictureBitmap = barcodeScanner.getBarcodeDetectionPreview(); // obtaining the Bitmap
            pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush(); // Not really required
            fOut.close(); // do not forget to close the stream

            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
        }
        catch(IOException e)
        {
            Log.e(TAG, "IO Error: " + e);
        }
    }

    @Override
    public Frame onFrameRequest()
    {
        try
        {
            Frame newFrame = session.update();

            intrinsics = newFrame.getCamera().getImageIntrinsics();

            frameTimestamp = newFrame.getTimestamp();
            currentTimestamp = System.currentTimeMillis();

            if(startTimestamp == 0)
            {
                startTimestamp = currentTimestamp;
            }

            if(currentTimestamp - startTimestamp > 180000)
            {
                finish();
            }

            devicePose = newFrame.getCamera().getPose();

            if(barcodeScanner == null)
            {
                onBarcodeScannerStart();
            }

            return newFrame;
        }
        catch (CameraNotAvailableException e)
        {
            Log.e(TAG, "AR Camera not available: " + e);
            return null;
        }
    }

    @Override
    public void onViewportChange(int width, int height)
    {
        try
        {
            int displayRotation = getSystemService(WindowManager.class).getDefaultDisplay().getRotation();
            session.setDisplayGeometry(displayRotation, width, height);
        }
        catch(NullPointerException e)
        {
            Log.e(TAG, "Default display exception: " + e);
        }
    }

    @Override
    public void onDrawRequest(int textureId)
    {
        session.setCameraTextureName(textureId);
    }

    public void setDrawWaypoint(boolean drawWaypoint)
    {
        surfaceView.getRenderer().setDrawWaypoint(drawWaypoint);
    }

    protected Session getSession() { return this.session; }
    protected CameraSurface getCameraSurface() { return this.surfaceView; }
    protected Vibrator getVibrator() { return this.vibrator; }

    public abstract void targetSelected(Objects.Observation target);
}