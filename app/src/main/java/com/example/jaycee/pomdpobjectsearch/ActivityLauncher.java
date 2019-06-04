package com.example.jaycee.pomdpobjectsearch;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Switch;

import com.example.jaycee.pomdpobjectsearch.helpers.PermissionHelper;

public class ActivityLauncher extends AppCompatActivity
{
    private static final int ACTIVITY_GUIDED = 1;
    private static final int ACTIVITY_UNGUIDED = 2;

    private int activityToLaunch = -1;
    private boolean highQuality = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        Switch qualitySwitch = findViewById(R.id.quality_switch);
        qualitySwitch.setOnCheckedChangeListener((view, isChecked) ->
        {
            if(isChecked)
            {
                highQuality = false;
            }
            else
            {
                highQuality = true;
            }
        });

        findViewById(R.id.button_guided).setOnClickListener(view ->
        {
            activityToLaunch = ACTIVITY_GUIDED;
            if(!PermissionHelper.hasCameraPermission(ActivityLauncher.this))
            {
                PermissionHelper.requestCameraPermission(ActivityLauncher.this);
                return;
            }
            Intent intent = new Intent(ActivityLauncher.this, ActivityGuided.class);
            intent.putExtra("ADD_NOISE", highQuality);
            startActivity(intent);
        });
        findViewById(R.id.button_unguided).setOnClickListener(view ->
        {
            activityToLaunch = ACTIVITY_UNGUIDED;
            if(!PermissionHelper.hasCameraPermission(ActivityLauncher.this))
            {
                PermissionHelper.requestCameraPermission(ActivityLauncher.this);
                return;
            }
            Intent intent = new Intent(ActivityLauncher.this, ActivityUnguided.class);
            intent.putExtra("ADD_NOISE", highQuality);
            startActivity(intent);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results)
    {
        if(requestCode == PermissionHelper.CAMERA_PERMISSION_CODE)
        {
            if(results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED)
            {
                launchActivity();
            }
            else
            {
                PermissionHelper.requestCameraPermission(this);
            }
        }
    }

    public void launchActivity()
    {
        switch(activityToLaunch)
        {
            case ACTIVITY_GUIDED: startActivity(new Intent(ActivityLauncher.this, ActivityGuided.class));
            case ACTIVITY_UNGUIDED: startActivity(new Intent(ActivityLauncher.this, ActivityUnguided.class));
        }
    }
}
