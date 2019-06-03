package com.example.jaycee.pomdpobjectsearch;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
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
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

public abstract class CameraActivityBase extends AppCompatActivity implements BarcodeListener, RenderListener
{
    private static final String TAG = CameraActivityBase.class.getSimpleName();

    private static final int O_NOTHING = 0;
    private static final int T_COMPUTER_MONITOR = 1;
    private static final int T_COMPUTER_MOUSE = 3;
    private static final int T_COMPUTER_KEYBOARD = 2;
    private static final int T_DESK = 4;
    private static final int T_MUG = 6;
    private static final int T_OFFICE_SUPPLIES = 7;
    private static final int T_WINDOW = 8;

    private CameraSurface surfaceView;
    private DrawerLayout drawerLayout;
    private Toast toast;

    private Vibrator vibrator;

    private Session session;
    protected Pose devicePose;

    protected Metrics metrics;
    private BarcodeScanner barcodeScanner;

    private boolean requestARCoreInstall = true;

    private long currentTimestamp, startTimestamp;
    protected long frameTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

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
                default: target = null;
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
        onBarcodeScannerStop();

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
        barcodeScanner = new BarcodeScanner(this, 525, 525, surfaceView.getRenderer());
        barcodeScanner.run();
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
    public Frame onFrameRequest()
    {
        try
        {
            Frame newFrame = session.update();

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

/*    public String objectCodeToString(long observation)
    {
        final String val;
        if(observation == 1)
        {
            val = "Monitor";
        }
        else if(observation == 2)
        {
            val = "Keyboard";
        }
        else if(observation == 3)
        {
            val = "Mouse";
        }
        else if(observation == 4)
        {
            val = "Desk";
        }
        else if(observation == 5)
        {
            val = "Laptop";
        }
        else if(observation == 6)
        {
            val = "Mug";
        }
        else if(observation == 7)
        {
            val = "Office supplies";
        }
        else if(observation == 8)
        {
            val = "Window";
        }
        else
        {
            val = "Unknown";
        }
        return val;
    }*/

    public void setDrawWaypoint(boolean drawWaypoint)
    {
        surfaceView.getRenderer().setDrawWaypoint(drawWaypoint);
    }

    protected Session getSession() { return this.session; }
    protected CameraSurface getCameraSurface() { return this.surfaceView; }
    protected Vibrator getVibrator() { return this.vibrator; }

    public abstract void targetSelected(Objects.Observation target);
}