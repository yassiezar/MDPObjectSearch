package com.example.jaycee.mdpobjectsearch.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public final class PermissionHelper
{
    public static final int ALL_PERMISSION_CODE = 0;
    public static final int CAMERA_PERMISSION_CODE = 1;
    public static final int STORAGE_PERMISSION_CODE = 2;

    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final String STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    public static boolean hasPermissions(Context context)
    {
        String[] permissions = {CAMERA_PERMISSION, STORAGE_PERMISSION};

        if (context != null)
        {
            for (String permission : permissions)
            {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }
        return true;

    }

    public static void requestPermissions(Activity activity)
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION))
        {
            Toast.makeText(activity, "Camera permission required for this app", Toast.LENGTH_LONG).show();
        }
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity, STORAGE_PERMISSION))
        {
            Toast.makeText(activity, "Storage permission required for this app", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(activity, new String[] {CAMERA_PERMISSION, STORAGE_PERMISSION}, ALL_PERMISSION_CODE);
    }

    public static void launchPermissionSettings(Activity activity)
    {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
