package com.serafimtech.serafimplay;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;
import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.App.productName;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.ReadFile;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.WriteFile;
import static com.serafimtech.serafimplay.file.value.InternalFileName.BLE_DEVICE_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.R_SERIES_ADDRESS;
import static com.serafimtech.serafimplay.file.value.InternalFileName.S_SERIES_ADDRESS;
import static com.serafimtech.serafimplay.service.ServiceAttribute.EXTRA_DATA;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.serafimtech.serafimplay.service.MyFirebaseMessagingService;
import com.serafimtech.serafimplay.service.RacingSeriesService;
import com.serafimtech.serafimplay.service.ServiceAttribute;
import com.serafimtech.serafimplay.service.StickSeriesService;
import com.serafimtech.serafimplay.tool.RequestPermissionManager;
import com.serafimtech.serafimplay.ui.ViewModel;
import com.serafimtech.serafimplay.ui.tool.StickFloatWindow;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

//import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends FragmentActivity {
    public String TAG = MainActivity.class.getName();
    public RequestPermissionManager rpManager;
    public StickFloatWindow stickFloatWindow;
    private AlertDialog alertDialog;
    private AlertDialog alertDialog2;
    private boolean sBtnDisconnect = false;
    private boolean rBtnDisconnect = false;
    public static final String TAG1= MyFirebaseMessagingService.TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(!task.isSuccessful())return;
                String token = task.getResult();
                Log.d(TAG1,"oncomplete:"+token);
            }
        });
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = getResources().getConfiguration().locale;
        }

        FirebaseMessaging.getInstance().subscribeToTopic(locale.getCountry());
        FirebaseMessaging.getInstance().subscribeToTopic("Fu123");
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }

            String newToken = task.getResult();
            Log.e("newToken222", newToken);
            getPreferences(Context.MODE_PRIVATE).edit().putString("fb", newToken).apply();
        });
        Log.d("newToken", getPreferences(Context.MODE_PRIVATE).getString("fb", "empty :("));

        FullScreen();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        registerReceiver();

        rpManager = new RequestPermissionManager(this);

        alertDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.access_usage_permission)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    rpManager.getUsageStatsPermission();
                    dialog.dismiss();
                }).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);

        alertDialog2 = new AlertDialog.Builder(this)
                .setMessage(R.string.access_overlay_permission)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    rpManager.getOverlayPermission();
                    dialog.dismiss();
                }).create();
        alertDialog2.setCanceledOnTouchOutside(false);
        alertDialog2.setCancelable(false);
        getDefaultDisplayThread.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        rpManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        FullScreen();
        if (!rpManager.isGotUsageStatsPermission()) {
            alertDialog.show();
        } else if (!rpManager.isGotOverlayPermission()) {
            alertDialog.dismiss();
            alertDialog2.show();
        } else if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {//(!rpManager.isGotReadAndWritePermission() || !rpManager.isGotAccessCoarseLocationPermission()) {
            alertDialog.dismiss();
            rpManager.getManiFestPermission("ACCESS_COARSE_LOCATION");
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            }
        }else if(ActivityCompat.checkSelfPermission(this,Manifest.permission.QUERY_ALL_PACKAGES)!=PackageManager.PERMISSION_GRANTED){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.QUERY_ALL_PACKAGES},1);
            }
        }else {
            alertDialog.dismiss();
            alertDialog2.dismiss();
            rpManager.openBLE();

            //<editor-fold desc="推播">
            if (App.getApp().notificationFlag) {
                App.getApp().notificationFlag = false;
                String result = getIntent().getStringExtra("data");
                if (result != null) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(result));
                    Log.d("result", result);
                    startActivity(intent);
                }
            }
            //</editor-fold>
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closefloatwindow();
        unbindService();
        unregisterReceiver();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus) {
            FullScreen();
        }
    }

    void FullScreen() {
        //<editor-fold desc="<Full Screen>">
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
        //</editor-fold>
    }

    //<editor-fold desc="<Broadcast>">
    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServiceAttribute.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ServiceAttribute.ACTION_GATT_CONNECTED);
        intentFilter.addAction(ServiceAttribute.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ServiceAttribute.ACTION_DATA_NOTIFY);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(ServiceAttribute.ACTION_DATA_READ);
        intentFilter.addAction(ServiceAttribute.ACTION_DATA_WRITE);
        intentFilter.addAction(ServiceAttribute.ACTION_SCREEN_ROTATE);
        return intentFilter;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            BluetoothDevice device;
            byte[] data;
            byte[] dataSplit = new byte[11];
            data = intent.getByteArrayExtra(EXTRA_DATA);
            try {
                switch (action) {
                    case ServiceAttribute.ACTION_GATT_SERVICES_DISCOVERED:
                        switch (productName) {
                            case S_Series:
                                stopStickAutoConnectTimer();
                                mStickService.startStickTimer();
                                break;
                            case R_Series:
                                stopRacingAutoConnectTimer();
                                mRacingService.startRacingTimer();
                                break;
                        }
                        break;
                    case ServiceAttribute.ACTION_GATT_CONNECTED:
                        Toast.makeText(context, R.string.connected_success, Toast.LENGTH_SHORT).show();
                        switch (productName) {
                            case S_Series:
                                WriteFile(mStickService.connectedDeviceAddress, BLE_DEVICE_INFO, S_SERIES_ADDRESS);
                                break;
                            case R_Series:
                                WriteFile(mRacingService.connectedDeviceAddress, BLE_DEVICE_INFO, R_SERIES_ADDRESS);
                                break;
                        }
                        updateBLEui("connect");
                        break;
                    case ServiceAttribute.ACTION_GATT_DISCONNECTED:
                        switch (productName) {
                            case S_Series:
                                if (!sBtnDisconnect) {
                                    startStickAutoConnectTimer();
                                } else {
                                    sBtnDisconnect = false;
                                }
                                mStickService.stopStickTimer();
//                                setCheckBtn();
                                closefloatwindow();
                                break;
                            case R_Series:
                                if (!rBtnDisconnect) {
                                    startRacingAutoConnectTimer();
                                } else {
                                    rBtnDisconnect = false;
                                }
                                mRacingService.stopRacingTimer();
                                break;
                        }
                        updateBLEui("disconnect");
                        break;
                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device != null) {
                            if (device.getName().contains("Serafim")) {
                                switch (productName) {
                                    case S_Series:
                                        if (!mStickService.connectedAddress.contains(device.getAddress())) {
                                            Log.d(TAG, device.getAddress() + " ACTION_ACL_CONNECTED");
                                            mStickService.connectedAddress.add(device.getAddress());
                                        }
                                        break;
                                    case R_Series:
                                        if (!mRacingService.connectedAddress.contains(device.getAddress())) {
                                            Log.d(TAG, device.getAddress() + " ACTION_ACL_CONNECTED");
                                            mRacingService.connectedAddress.add(device.getAddress());
                                        }
                                        break;
                                }
                                updateBLEui("update");
                            }
                        }
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        Log.d("ACL", "DISCONNECTED");
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device != null) {
                            if (device.getName() != null) {
                                if (device.getName().contains("Serafim")) {
                                    switch (productName) {
                                        case S_Series:
                                            if (mStickService.connectedAddress.contains(device.getAddress())) {
                                                if (mStickService.mConnectionState == STATE_CONNECTED) {
                                                    mStickService.disconnect();
                                                }
                                                Log.d(TAG, device.getAddress() + " ACTION_ACL_DISCONNECTED");
                                                mStickService.connectedAddress.remove(device.getAddress());
                                            }
                                            break;
                                        case R_Series:
                                            if (mRacingService.connectedAddress.contains(device.getAddress())) {
                                                if (mRacingService.mConnectionState == STATE_CONNECTED) {
                                                    mRacingService.disconnect();
                                                }
                                                Log.d(TAG, device.getAddress() + " ACTION_ACL_DISCONNECTED");
                                                mRacingService.connectedAddress.remove(device.getAddress());
                                            }
                                            break;
                                    }
                                    updateBLEui("update");
                                }
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                            Log.d(TAG, "bluetooth is not enabled");
                            if (serviceConnected) {
                                switch (productName) {
                                    case S_Series:
                                        if (mStickService.mConnectionState == STATE_CONNECTED) {
                                            mStickService.disconnect();
                                        }
                                        mStickService.connectedAddress.clear();
                                        stopStickAutoConnectTimer();
                                        mStickService.stopStickTimer();
                                        break;
                                    case R_Series:
                                        if (mRacingService.mConnectionState == STATE_CONNECTED) {
                                            mRacingService.disconnect();
                                        }
                                        mRacingService.connectedAddress.clear();
                                        stopRacingAutoConnectTimer();
                                        mRacingService.stopRacingTimer();
                                        break;
                                }
                            }
                        } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                            Log.d(TAG, "bluetooth is enabled");
                            if (serviceConnected) {
                                switch (productName) {
                                    case S_Series:
                                        mStickService.getStickSeriesConnectedAddress();
                                        startStickAutoConnectTimer();
                                        break;
                                    case R_Series:
                                        mRacingService.getRacingSeriesConnectedAddress();
                                        startRacingAutoConnectTimer();
                                        break;
                                }
                            }
                        }
                        updateBLEui("update");
                        break;
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device != null && device.getBondState() == BluetoothDevice.BOND_BONDED) {
                            //means device paired
                        }
                        break;
                    case Intent.ACTION_SCREEN_OFF:
                        Log.d(TAG, "Screen off");
                        if (productName != null) {
                            switch (productName) {
                                case S_Series:
                                    mStickService.stopStickTimer();
                                    stopStickAutoConnectTimer();
                                    break;
                                case R_Series:
                                    mRacingService.stopRacingTimer();
                                    stopRacingAutoConnectTimer();
                                    break;
                            }
                        }
                        break;
                    case Intent.ACTION_SCREEN_ON:
                        Log.d(TAG, "Screen on");
//                    case Intent.ACTION_USER_PRESENT:
//                        Log.d(TAG, "ACTION_USER_PRESENT");
                        FullScreen();
                        switch (productName) {
                            case S_Series:
                                if (mStickService.mConnectionState == STATE_DISCONNECTED) {
                                    startStickAutoConnectTimer();
                                } else if (mStickService.mConnectionState == STATE_CONNECTED) {
                                    mStickService.startStickTimer();
                                }
                                break;
                            case R_Series:
                                if (mRacingService.mConnectionState == STATE_DISCONNECTED) {
                                    startRacingAutoConnectTimer();
                                } else if (mRacingService.mConnectionState == STATE_CONNECTED) {
                                    mRacingService.startRacingTimer();
                                }
                                break;
                        }
                        break;
                    //<editor-fold desc="<Stickfloatwindow>">
                    case ServiceAttribute.ACTION_SCREEN_ROTATE:
                        if (stickFloatWindow.checkButtonFlag) {
                            stickFloatWindow.removePreviewButton();
                            stickFloatWindow.initPreviewButton();
                        }
                        break;
                    case ServiceAttribute.ACTION_DATA_NOTIFY:
                        if (data != null && data.length > 0) {
                            System.arraycopy(data, 0, dataSplit, 0, 11);
                            if (dataSplit[0] != (byte) 0xFF) {
                                if (mStickService.hidFlag) {
                                    stickFloatWindow.BLECalculation(data, StickFloatWindow.BroadcastCalculate.NotPlay);
                                } else if (mStickService.notifyFlag) {
                                    stickFloatWindow.BLECalculation(data, StickFloatWindow.BroadcastCalculate.Notify);
                                } else if (mStickService.recordFlag) {
                                    stickFloatWindow.recordNotificationData.add(dataSplit);
                                } else if (stickFloatWindow.setMacroFlag) {
                                    stickFloatWindow.BLECalculation(data, StickFloatWindow.BroadcastCalculate.Macro);
                                } else if (mStickService.playFlag) {
                                    stickFloatWindow.BLECalculation(data, StickFloatWindow.BroadcastCalculate.play);
                                }
                            }
                        }
                        break;
                    case ServiceAttribute.ACTION_DATA_READ:
                        if (stickFloatWindow != null) {
                            stickFloatWindow.BLECalculation(data, StickFloatWindow.BroadcastCalculate.Read);
                        }
                        break;
                    //</editor-fold>
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void registerReceiver() {
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    public void unregisterReceiver() {
        unregisterReceiver(mGattUpdateReceiver);
    }
    //</editor-fold>

    Thread getDefaultDisplayThread = new Thread(new Runnable() {
        @Override
        public void run() {
            final DisplayMetrics metrics = new DisplayMetrics();
            Display display = getWindowManager().getDefaultDisplay();
            display.getRealMetrics(metrics);
            getApp().windowWidth = metrics.widthPixels;
            getApp().windowHeight = metrics.heightPixels;
            getApp().windowDensity = metrics.density;
            Log.d("dimensions", metrics.xdpi + ":" + metrics.ydpi + ";" + metrics.density + ";" + metrics.scaledDensity + ";" + metrics.densityDpi);
            Log.d("window size", getApp().windowWidth + "," + getApp().windowHeight);
            int resourceId = getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                getApp().statusBarHeight = getApplicationContext().getResources().getDimensionPixelSize(resourceId);
            }
            int navigationBarID = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (navigationBarID > 0) {
                getApp().navigationBarHeight = getApplicationContext().getResources().getDimensionPixelSize(navigationBarID);
            }
            Log.d("status bar height", String.valueOf(getApp().statusBarHeight));
            Log.d("navigation bar height", String.valueOf(getApp().navigationBarHeight));
        }
    });

    public void closefloatwindow() {
        if (stickFloatWindow != null) {
            stickFloatWindow.dismissAllFloatWindow();
            stickFloatWindow = null;
        }
    }

    //<editor-fold desc="<ViewModel>">
    private ViewModel model;

    private void updateBLEui(String state) {
        model = new ViewModelProvider(this).get(ViewModel.class);
        final MutableLiveData<String> mutableLiveData = (MutableLiveData<String>) model.getdeviceconnect();
        mutableLiveData.setValue(state);
    }
    //</editor-fold>

    //<editor-fold desc="<AutoConnect>">
    Timer sTimer;
    Timer rTimer;

    public void startConnect() {
        if (serviceConnected) {
            switch (productName) {
                case S_Series:
                    mStickService.getStickSeriesConnectedAddress();
                    mStickService.startStickTimer();
                    startStickAutoConnectTimer();
                    break;
                case R_Series:
                    mRacingService.getRacingSeriesConnectedAddress();
                    mRacingService.startRacingTimer();
                    startRacingAutoConnectTimer();
                    break;
            }
            updateBLEui("update");
        }
    }

    void startStickAutoConnectTimer() {
        if (sTimer == null) {

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mStickService.mConnectionState == STATE_DISCONNECTED) {
                sTimer = new Timer();
                final TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        if (mStickService != null && !mStickService.fwUpdateFlag && mStickService.mConnectionState == STATE_DISCONNECTED) {
                            if (mStickService.connectedAddress.size() != 0) {
                                String recentAddress = ReadFile(BLE_DEVICE_INFO, S_SERIES_ADDRESS).replaceAll("\\p{C}", "");
                                Log.d(TAG, "recent address:" + recentAddress);
                                if (mStickService.connectedAddress.contains(recentAddress)) {
                                    ConnectToService(recentAddress);
                                } else {
                                    if (mStickService.connectedAddress.size() != 0) {
                                        ConnectToService(mStickService.connectedAddress.get(0));
                                    }
                                }
                            }
                        } else {
                            stopStickAutoConnectTimer();
                        }
                    }
                };
                sTimer.schedule(task, 0, 750);
                Log.d(TAG, "Start running Stick timer");
            }
        }
    }

    void startRacingAutoConnectTimer() {
        if (rTimer == null) {

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mRacingService.mConnectionState == STATE_DISCONNECTED) {
                rTimer = new Timer();
                final TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        if (mRacingService != null && !mRacingService.fwUpdateFlag && mRacingService.mConnectionState == STATE_DISCONNECTED) {
                            if (mRacingService.connectedAddress.size() != 0) {
                                String recentAddress = ReadFile(BLE_DEVICE_INFO, R_SERIES_ADDRESS).replaceAll("\\p{C}", "");
                                Log.d(TAG, "recent address:" + recentAddress);
                                if (mRacingService.connectedAddress.contains(recentAddress)) {
                                    ConnectToService(recentAddress);
                                } else {
                                    if (mRacingService.connectedAddress.size() != 0) {
                                        ConnectToService(mRacingService.connectedAddress.get(0));
                                    }
                                }
                            }
                        } else {
                            stopRacingAutoConnectTimer();
                        }
                    }
                };
                rTimer.schedule(task, 0, 750);
                Log.d(TAG, "Start running Racing timer");
            }
        }
    }

    void stopStickAutoConnectTimer() {
        if (sTimer != null) {
            sTimer.cancel();
            sTimer = null;
        }
    }

    void stopRacingAutoConnectTimer() {
        if (rTimer != null) {
            rTimer.cancel();
            rTimer = null;
        }
    }

    public void ConnectToService(String address) {
        switch (productName) {
            case S_Series:
                if (mStickService.mConnectionState == STATE_CONNECTED) {
                    Log.d("Try to disconnect", "yes");
                    sBtnDisconnect = true;
                    mStickService.disconnect();
                    try {
                        Thread.sleep(250);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (mStickService.mConnectionState == STATE_DISCONNECTED) {
                    Log.d("Try to connect to", address);
                    mStickService.connect(address);
                }
                break;
            case R_Series:
                if (mRacingService.mConnectionState == STATE_CONNECTED) {
                    Log.d("Try to disconnect", "yes");
                    rBtnDisconnect = true;
                    mRacingService.disconnect();
                    try {
                        Thread.sleep(250);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (mRacingService.mConnectionState == STATE_DISCONNECTED) {
                    Log.d("Try to connect to", address);
                    mRacingService.connect(address);
                }
                break;
        }
    }

    //</editor-fold>

    //<editor-fold desc="<Service>">
    static boolean serviceFlag = false;
    public static boolean serviceConnected = false;
    public static StickSeriesService mStickService = null;
    public static RacingSeriesService mRacingService = null;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            try {
                switch (productName) {
                    case R_Series:
                        mRacingService = ((RacingSeriesService.LocalBinder) service).getService();
                        if (!mRacingService.initialize()) {
                            Log.e(TAG, "Unable to initialize Bluetooth");
                        } else {
                            Log.d(TAG, "ConnectToR_Series");
                            serviceConnected = true;
                            startConnect();
                        }
                        break;
                    case S_Series:
                        mStickService = ((StickSeriesService.LocalBinder) service).getService();
                        if (!mStickService.initialize()) {
                            Log.e(TAG, "Unable to initialize Bluetooth");
                        } else {
                            Log.d(TAG, "ConnectToS_Series");
                            serviceConnected = true;
                            startConnect();
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");
            switch (productName) {
                case R_Series:
                    mRacingService = null;
                    break;
                case S_Series:
                    mStickService = null;
                    break;
                default:
                    break;
            }
            serviceConnected = false;
        }
    };

    public void bindService() {
        try {
            if (!serviceFlag) {
                switch (productName) {
                    case R_Series:
                        serviceFlag = bindService(new Intent(this, RacingSeriesService.class), mServiceConnection, BIND_AUTO_CREATE);
                        Log.d(TAG, "Bindracingservice:" + serviceFlag);
                        break;
                    case S_Series:
                        serviceFlag = bindService(new Intent(this, StickSeriesService.class), mServiceConnection, BIND_AUTO_CREATE);
                        Log.d(TAG, "Bindstickservice:" + serviceFlag);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unbindService() {
        try {
            if (serviceFlag) {
                Log.e(TAG, "unbindService");
                serviceFlag = false;
                unbindService(mServiceConnection);
                serviceConnected = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

}