package com.serafimtech.serafimplay.tool;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.serafimtech.serafimplay.MainActivity;

public class RequestPermissionManager {

    public static int MANIFEST_PERMISSION = 0;
    public static int APP_USAGE_PERMISSION = 1;
    public static int OVERLAY_PERMISSION = 2;
    public Context context;

    public RequestPermissionManager(Context context) {
        this.context = context;
    }

    public boolean isConnected() {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    public boolean isGotReadAndWritePermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isGotUsageStatsPermission() {
        return ((AppOpsManager) context.getApplicationContext().getSystemService(Context.APP_OPS_SERVICE)).checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(),
                context.getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void getUsageStatsPermission() {
        if (isGotUsageStatsPermission()) {
            getManiFestPermission();
            Log.d("Permission", "Usage stats permission has already gotten");
        } else {
            try {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:" + context.getPackageName()));
                ((Activity) context).startActivityForResult(intent, APP_USAGE_PERMISSION);
                Handler handler = new Handler();
                Runnable checkUsageSetting = new Runnable() {
                    @Override
                    @TargetApi(23)
                    public void run() {
                        if (isGotUsageStatsPermission()) {
                            //You have the permission, re-launch MainActivity
//                            ((Activity) context).finish();
//                            Intent i = new Intent(context, MainActivity.class);
//                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            context.startActivity(i);
                            return;
                        }
                        handler.postDelayed(this, 1000);
                    }
                };
                handler.postDelayed(checkUsageSetting, 1000);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                ((Activity) context).startActivityForResult(intent, APP_USAGE_PERMISSION);
            }
        }
    }

    public boolean isGotAccessCoarseLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    public boolean isGotQUERYALLPACKAGESPermission(){
        return  ContextCompat.checkSelfPermission(context,Manifest.permission.QUERY_ALL_PACKAGES)==PackageManager.PERMISSION_GRANTED;
    }



    public void getManiFestPermission() {

        if (isGotAccessCoarseLocationPermission() && isGotReadAndWritePermission() && isGotQUERYALLPACKAGESPermission()) {
            Log.d("Permission", "Access coarse location permission has already gotten");
        } else {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.QUERY_ALL_PACKAGES}, MANIFEST_PERMISSION);
        }
    }

    public boolean isBLEEnabled() {
        return ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled();
    }

    public void openBLE() {
        if (isBLEEnabled()) {
            Log.d("Permission", "Bluetooth is opened");
        } else {
            context.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }
    }

    @TargetApi(23)
    public boolean isGotOverlayPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            return Settings.canDrawOverlays(context);
        } else {
            return true;
        }
    }

    public void getOverlayPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (isGotOverlayPermission()) {
                Log.d("Permission", "Overlay permission has already gotten");
            } else {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                    ((Activity) context).startActivityForResult(intent, OVERLAY_PERMISSION);
                    Handler handler = new Handler();
                    Runnable checkOverlaySetting = new Runnable() {
                        @Override
                        @TargetApi(23)
                        public void run() {
                            if (isGotOverlayPermission()) {
                                //You have the permission, re-launch MainActivity
                                Intent i = new Intent(context, MainActivity.class);
                                i.putExtra("product", "S_Series");
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                context.startActivity(i);
                                return;
                            }
                            handler.postDelayed(this, 1000);
                        }
                    };
                    handler.postDelayed(checkOverlaySetting, 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.d("Permission", "Overlay permission has already gotten");
        }
    }

    public void openLocation(){
        if (isLocationEnabled()) {
            Log.d("Permission", "Location is opened");
        } else {
            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    public boolean isLocationEnabled(){
        LocationManager lm = (LocationManager)context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}
        return gps_enabled;
    }
}
