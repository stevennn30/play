package com.serafimtech.serafimplay.tool;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
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
import com.serafimtech.serafimplay.R;

public class RequestPermissionManager {

    private static final int GET_PERMISSION = 0;
    private static final int APP_USAGE_PERMISSION = 1;
    private static final int OVERLAY_PERMISSION = 2;
    public Context context;
    public String TAG = RequestPermissionManager.class.getName();

    public RequestPermissionManager(Context context) {
        this.context = context;
    }

    public boolean isConnected() {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    public boolean isGotReadAndWritePermission() {
        Log.d("DEBUG", "isGotReadAndWritePermission(): " + ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE));
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isGotUsageStatsPermission() {
        return ((AppOpsManager) context.getApplicationContext().getSystemService(Context.APP_OPS_SERVICE)).checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(),
                context.getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }

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
        Log.d("DEBUG", "isGotReadAndWritePermission(): " + ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION));
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    public boolean isGotQUERYALLPACKAGESPermission(){
        return  ContextCompat.checkSelfPermission(context,Manifest.permission.QUERY_ALL_PACKAGES)==PackageManager.PERMISSION_GRANTED;
    }

    public boolean isGotgetManiFestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("DEBUG", "SDK_INT >= Build.VERSION_CODES.S  : BLUETOOTH_SCAN");
            //return  ContextCompat.checkSelfPermission(context,Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
            return  ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            Log.d("DEBUG", "ACCESS_COARSE_LOCATION");
            return  ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void getManiFestPermission(String required_permission) {
//        if (isGotAccessCoarseLocationPermission() && isGotReadAndWritePermission() && isGotQUERYALLPACKAGESPermission()) {
//            Log.d("Permission", "Access coarse location permission has already gotten");
//        } else {
//            ActivityCompat.requestPermissions((Activity) context,
//                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.QUERY_ALL_PACKAGES}, GET_PERMISSION);
//        }
        Log.d(TAG, "getManiFestPermission()");
        String permission = Manifest.permission.class.getField("required_permission").get(null).toString();
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Access coarse location permission has already gotten");
        } else if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
            goToAppSetting();
        }
        else {
            Log.d(TAG, "directly ask for the permission.");
            ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, GET_PERMISSION);
        }
    }

    private void goToAppSetting() {
        Log.d(TAG, "goToAppSetting()");
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage(R.string.goToAppSetting)
                .setPositiveButton(R.string.setting, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS, Uri.parse("package:" + context.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                }).create();
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.show();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult() ");
        switch (requestCode) {
            case GET_PERMISSION:
                // Check if permission is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission", "Access coarse location permission has already gotten");
                } else {
                    goToAppSetting();
                }
                break;
            // Add other cases if you have multiple permissions
            default:
                // Ignore other requests
                // 忽略其他请求
                break;
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

    public boolean isGotOverlayPermission() {
        return Settings.canDrawOverlays(context);
    }

    public void getOverlayPermission() {
        if (isGotOverlayPermission()) {
            Log.d("Permission", "Overlay permission has already gotten");
        } else {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                ((Activity) context).startActivityForResult(intent, OVERLAY_PERMISSION);
                Handler handler = new Handler();
                Runnable checkOverlaySetting = new Runnable() {
                    @Override
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
