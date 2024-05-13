package com.serafimtech.serafimplay.service;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.serafimtech.serafimplay.tool.SerializableSparseArray;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.ReadFile;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.getFilesAllName;
import static com.serafimtech.serafimplay.file.JsonFormat.PresetJsonToObject;
import static com.serafimtech.serafimplay.file.value.InternalFileName.CUSTOM_INFO_BIND_GAME;
import static com.serafimtech.serafimplay.file.value.InternalFileName.DEFAULT_INFO;
import static com.serafimtech.serafimplay.service.ServiceAttribute.ACTION_GATT_DISCONNECTED;

import androidx.core.app.ActivityCompat;

@SuppressWarnings("unchecked")
public class StickSeriesService extends Service {
    //<editor-fold desc="<Variable>">
    private final String TAG = StickSeriesService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();

    public BluetoothDevice device;
    public BluetoothGattService mOadService = null;
    public BluetoothGattService mConnControlService = null;
    public BluetoothGattService mSerafimService = null;
    public BluetoothGatt mBluetoothGatt;
    public BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

    public SerializableSparseArray<float[]> pStick = new SerializableSparseArray<>();
    public SerializableSparseArray<float[]> sStick = new SerializableSparseArray<>();
    public SerializableSparseArray<byte[]> fStick = new SerializableSparseArray<>();
    public SerializableSparseArray<float[]> cStick = new SerializableSparseArray<>();
    public SerializableSparseArray<byte[]> fStickBuffer = new SerializableSparseArray<>();
    public SerializableSparseArray<float[]> sStickBuffer = new SerializableSparseArray<>();
    public SerializableSparseArray<float[]> cStickBuffer = new SerializableSparseArray<>();
    public SerializableSparseArray<float[]> pStickBuffer = new SerializableSparseArray<>();
    public SerializableSparseArray<String> macroKeys = new SerializableSparseArray<>();

    public ArrayList<String> connectedAddress = new ArrayList<>();

    public String packageNameBuffer = "com.serafimtech.serafimplay";
    public String presetName;
    public String presetNameBuffer;
    public String connectedDeviceAddress = "";
    public String connectedDeviceName = "";
    private String mBluetoothDeviceAddress;
    public String S1FWVersion = "0.0.0";

    private final byte stickMode = 0x10;

    public int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
    public int rotationBuffer = 0;
    public int rotationFlag;
    public int interval = 100;
    public int combinationKey = 0;
    public int combinationKeyBuffer = 0;

    public Handler mHandler;
    public Timer sTimer;

    public byte screenOrientation = 0x00;

    public volatile boolean mBusy = false; // Write/read pending response
    public boolean S1FWVersionUpdateFlag;
    public boolean fwUpdateFlag = false;
    public boolean fwUpdateAvailableFlag = true;
    public boolean playFlag = false;
    public boolean hidFlag = false;
    public boolean recordFlag = false;
    public boolean notifyFlag = false;
    public boolean MacroFlag = false;
    //</editor-fold>

    //<editor-fold desc="<Broadcast>">

    public void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
        mBusy = false;
        interval = 100;
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic, final int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(ServiceAttribute.EXTRA_UUID, characteristic.getUuid().toString());
        intent.putExtra(ServiceAttribute.EXTRA_DATA, characteristic.getValue());
        intent.putExtra(ServiceAttribute.EXTRA_STATUS, status);
        sendBroadcast(intent);
        mBusy = false;
        interval = 100;
    }
    //</editor-fold>

    //<editor-fold desc="<LifeCycle>">
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        Log.d(TAG, this + "");
        mHandler = new Handler((Message msg) -> {
            Log.i("Package name", packageNameBuffer);
//            Toast.makeText(getApplicationContext(), packageNameBuffer, Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
        connectedAddress.clear();
        Log.d(TAG, "Thread interrupted:" + Thread.interrupted());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public StickSeriesService getService() {
            return StickSeriesService.this;
        }
    }
    //</editor-fold>

    //<editor-fold desc="<Initialize>">
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        Log.d(TAG, "Initialize");

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//        }
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="<BluetoothGatt>">
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("AASA0", "COMING");
                if (mBluetoothAdapter.isEnabled()) {
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt.discoverServices());
                    interval = 100;
                    intentAction = ServiceAttribute.ACTION_GATT_CONNECTED;
                    connectedDeviceAddress = gatt.getDevice().getAddress();
                    connectedDeviceName = gatt.getDevice().getName();
                    broadcastUpdate(intentAction);
                    mConnectionState = BluetoothProfile.STATE_CONNECTED;
                    Log.i(TAG, "Connected to GATT server.");
                    // Attempts to discover services after successful connection.
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                intentAction = ACTION_GATT_DISCONNECTED;
//                connectedDeviceAddress = "";
//                connectedDeviceName = "";
//                broadcastUpdate(intentAction);
//                intentAction = BluetoothDevice.ACTION_ACL_DISCONNECTED;
//                broadcastUpdate(intentAction);
//                mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered");
                checkOad();
                //TODO ifFwUpdate
//                if (!QackScreenManager.currentActivity().getClass().getSimpleName().equals(StickFwUpdateActivity.class.getSimpleName())) {
//                    readFirmwareVer();
//                }
                readFirmwareVer();
                broadcastUpdate(ServiceAttribute.ACTION_GATT_SERVICES_DISCOVERED);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    }
                }, 5000);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
//            Log.i(TAG, "onCharacteristicWrite");
            final byte[] data = characteristic.getValue();
            Log.d(TAG, "Write data" + byteToString(data).toString());
//            readCharacteristic();
            broadcastUpdate(ServiceAttribute.ACTION_DATA_WRITE, characteristic, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            //TODO FwUpdate Check
            Log.d(TAG, "onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ServiceAttribute.ACTION_DATA_READ, characteristic, status);
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    Log.d(TAG, "Read data" + byteToString(data).toString());
                    if (characteristic.getUuid().equals(ServiceAttribute.FW_VER_CHARACTERISTIC_UUID)) {
                        if (getApp().isConnected()) {
                            try {
                                S1FWVersion = new String(data, StandardCharsets.UTF_8);
//                                Downloader.FWInfoDownloadTask fwInfoDownloadTask = new Downloader.FWInfoDownloadTask();
//                                fwInfoDownloadTask.setOnDownloadFinishListener(new Downloader.OnDownloadFinishListener() {
//                                    @Override
//                                    public void onFinish(String result) {
//                                        try {
//                                            JSONObject jsonObject = new JSONObject(result);
//                                            fwUpdateAvailableFlag = jsonObject.getBoolean("update_flag");
//                                            if (jsonObject.getBoolean("info_flag")) {
//                                                int[] S1VerInt = new int[3];
//                                                int[] deviceVer = new int[3];
//                                                for (int i = 0; i < 3; i++) {
//                                                    S1VerInt[i] = Integer.parseInt(jsonObject.getString("ver").split("\\.")[i]);
//                                                    deviceVer[i] = Integer.parseInt(S1FWVersion.split("\\.")[i]);
//                                                }
//                                                S1FWVersionUpdateFlag = false;
//                                                if (S1VerInt[0] > deviceVer[0]) {
//                                                    S1FWVersionUpdateFlag = true;
//                                                } else if (S1VerInt[0] == deviceVer[0]) {
//                                                    if (S1VerInt[1] > deviceVer[1]) {
//                                                        S1FWVersionUpdateFlag = true;
//                                                    } else if (S1VerInt[1] == deviceVer[1]) {
//                                                        if (S1VerInt[2] > deviceVer[2]) {
//                                                            S1FWVersionUpdateFlag = true;
//                                                        }
//                                                    }
//                                                }
//                                                StringBuilder updateInfo;
//                                                if (jsonObject.has("update_info_" + getApp().localeCountryCode.toLowerCase())) {
//                                                    updateInfo = new StringBuilder();
//                                                    for (int i = 0; i < jsonObject.getJSONArray("update_info_" + getApp().localeCountryCode.toLowerCase()).length(); i++) {
//                                                        updateInfo.append("(")
//                                                                .append(i + 1)
//                                                                .append(") ")
//                                                                .append(jsonObject.getJSONArray("update_info_" + getApp().localeCountryCode.toLowerCase()).getString(i));
//                                                        if (i < jsonObject.getJSONArray("update_info_" + getApp().localeCountryCode.toLowerCase()).length() - 1) {
//                                                            updateInfo.append("\n");
//                                                        }
//                                                    }
//                                                    Log.e(TAG, "Update info" + getApp().localeCountryCode.toLowerCase() + ":" + updateInfo);
//                                                } else {
//                                                    updateInfo = new StringBuilder();
//                                                    for (int i = 0; i < jsonObject.getJSONArray("update_info_en").length(); i++) {
//                                                        updateInfo.append("(")
//                                                                .append(i + 1)
//                                                                .append(") ")
//                                                                .append(jsonObject.getJSONArray("update_info_en").getString(i));
//                                                        if (i < jsonObject.getJSONArray("update_info_" + getApp().localeCountryCode.toLowerCase()).length() - 1) {
//                                                            updateInfo.append("\n");
//                                                        }
//                                                    }
//                                                    Log.e(TAG, "Update info en:" + updateInfo);
//                                                }
//                                                Log.d("S1FWVersionUpdateFlag", S1FWVersionUpdateFlag + "");
//                                                String checkInfo = ReadFile(FW_INFO, FW_UPDATE_INFO);
//                                                if (S1FWVersionUpdateFlag) {
//                                                    if (!checkInfo.contains(gatt.getDevice().getAddress())) {
//                                                        getApp().fwUpdateDialog(updateInfo.toString(), gatt.getDevice().getAddress());
//                                                    }
//                                                }
//                                            }
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });
//                                fwInfoDownloadTask.execute("http://app.serafim-tech.com//SerafimPlay/S_Series/fw/fw_info.txt");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
//            Log.i(TAG, "onCharacteristicChanged");
            broadcastUpdate(ServiceAttribute.ACTION_DATA_NOTIFY, characteristic,
                    BluetoothGatt.GATT_SUCCESS);
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
//                Log.d("Service id",StickSeriesService.this+"");
//                Log.d(TAG, "Notification data " + byteToString(data).toString());
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {
            mBusy = false;
            interval = 100;
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "onDescriptorWrite: " + descriptor.getUuid().toString());
            mBusy = false;
            interval = 100;
        }
    };

    private void checkOad() {
        // Check if OAD is supported (needs OAD and Connection Control service)
        mOadService = null;
        mConnControlService = null;

        for (int i = 0; i < getSupportedGattServices().size() && (mOadService == null); i++) {
            BluetoothGattService srv = getSupportedGattServices().get(i);
            if (srv.getUuid().equals(ServiceAttribute.OAD_SERVICE_UUID)) {
                mOadService = srv;
                Log.d(TAG, "mOadService=" + mOadService);
            }
            if (srv.getUuid().equals(ServiceAttribute.CC_SERVICE_UUID)) {
                mConnControlService = srv;
                Log.d(TAG, "mConnControlService=" + mConnControlService);
            }
            if (srv.getUuid().equals(ServiceAttribute.SERAFIM_SERVICE_UUID)) {
                mSerafimService = srv;
                Log.d(TAG, "mSerafimService=" + mSerafimService);
            }
        }
    }

    private boolean checkGatt() {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return false;
        }

        if (mBusy) {
            Log.w(TAG, "LeService busy");
            return false;
        }
        return true;

    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
//        if (!fwUpdateFlag && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            Log.d("Bonded State", "" + mBluetoothGatt.getDevice().getBondState());
//            if (mBluetoothGatt.getDevice().getBondState() != 12 && !fwUpdateFlag) {
//                return false;
//            }
//            if (mBluetoothGatt.connect()) {
//                mConnectionState = BluetoothProfile.STATE_CONNECTING;
//                return true;
//            }
//        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        this.device = device;
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = BluetoothProfile.STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        Log.d(TAG, "disconnect gatt");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        connectedDeviceAddress = "";
        connectedDeviceName = "";
        broadcastUpdate(ACTION_GATT_DISCONNECTED);
        mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
//        Log.i(TAG, "Disconnected from GATT server.");
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    //</editor-fold>

    //<editor-fold desc="<GetBLEInfo>">
    public List<BluetoothGattCharacteristic> getOadCharacteristics() {
        if (mOadService != null) {
            return mOadService.getCharacteristics();
        }
        return null;
    }

    public List<BluetoothGattCharacteristic> getConnControlCharacteristics() {
        if (mConnControlService != null) {
            return mConnControlService.getCharacteristics();
        }
        return null;
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public void getStickSeriesConnectedAddress() {
        Log.d("getStickSeries", "start");
        try {//得到连接状态的方法
            connectedAddress.clear();
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(adapter, (Object[]) null);

            if (state == BluetoothAdapter.STATE_CONNECTED) {
                Log.i("BLUETOOTH", "BluetoothAdapter.STATE_CONNECTED");
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                Log.i("BLUETOOTH", "devices:" + devices.size());

                for (BluetoothDevice device : devices) {
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if (isConnected) {
                        if (device.getName() != null) {
                            Log.d(TAG, "get connected device name:" + device.getName());
                            if (device.getName().contains("Serafim S")) {
                                connectedAddress.add(device.getAddress());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            getStickSeriesBondedAddress();
        }
    }

    public void getStickSeriesBondedAddress() {
        connectedAddress.clear();

        for (BluetoothDevice bluetoothDevice : mBluetoothAdapter.getBondedDevices()) {
//            Log.d(TAG, bluetoothDevice.getName());
            if (bluetoothDevice.getName() != null) {
                if (bluetoothDevice.getName().contains("Serafim S")) {
                    Log.d(TAG, bluetoothDevice.getName());
                    Log.d(TAG, bluetoothDevice.getBondState() + "");
                    connectedAddress.add(bluetoothDevice.getAddress());
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="<OpenBLENotify>">
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        if (!checkGatt()) {
            return false;
        }

        boolean ok = false;
        if (mBluetoothGatt.setCharacteristicNotification(characteristic, enable)) {

            BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(ServiceAttribute.CLIENT_CHARACTERISTIC_CONFIG);
            if (clientConfig != null) {

                if (enable) {
                    Log.i(TAG, "Enable notification: " +
                            characteristic.getUuid().toString());
                    ok = clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    Log.i(TAG, "Disable notification: " +
                            characteristic.getUuid().toString());
                    ok = clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }

                if (ok) {
                    mBusy = true;
                    ok = mBluetoothGatt.writeDescriptor(clientConfig);
                    Log.i(TAG, "writeDescriptor: " +
                            characteristic.getUuid().toString());
                }
            }
        }
        return ok;
    }

    public void setCustomCharacteristicNotify(boolean mode) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("0000FFA0-0000-1000-8000-00805f9b34fb"));
        if (mCustomService == null) {
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        BluetoothGattCharacteristic msetCharNotify = mCustomService.getCharacteristic(UUID.fromString("0000FFA3-0000-1000-8000-00805f9b34fb"));

        while (!setCharacteristicNotification(msetCharNotify, mode)) ; //True/False
    }
    //</editor-fold>

    //<editor-fold desc="<PacakgeName>">
    public String getTopAppName(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String strName = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                strName = getLollipopFGAppPackageName(context);
            } else {
                strName = mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strName;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private String getLollipopFGAppPackageName(Context ctx) {

        try {
            String recentPkg = "";
            long time = System.currentTimeMillis();
            UsageStatsManager usageStatsManager = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
            UsageEvents usageEvents = usageStatsManager.queryEvents(time - 10 * 1000, time);
            UsageEvents.Event event = new UsageEvents.Event();
            // get last event
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
//                Log.d(TAG, event.getPackageName() + ":" + event.getEventType());
                if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                    recentPkg = event.getPackageName();
                }
            }
            return recentPkg;
//            Date date = new Date();
//            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, date.getTime() - time, date.getTime());
//            long recentTime = 0;
//            String recentPkg = "";
//            for (int i = 0; i < queryUsageStats.size(); i++) {
//                UsageStats stats = queryUsageStats.get(i);
//                if (stats.getLastTimeUsed() > recentTime) {
//                    recentTime = stats.getLastTimeUsed();
//                    recentPkg = stats.getPackageName();
//                }
//            }
//            Log.d("eventss",event.getEventType()+"");
//            if (recentPkg.equals(event.getPackageName()) && event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
//                return recentPkg;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    //</editor-fold>

    //    <editor-fold desc="<CheckStickInfo>">

    public void checkStickPhoneInfo() {
        while (!CheckPhoneInfo()) {
//            Log.d(TAG, "Write CheckPhoneInfo Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
        while (!readCharacteristic()) {
//            Log.d(TAG, "Read Characteristic Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
    }

    public boolean CheckPhoneInfo() {
//        Log.d(TAG, "CheckPhoneInfo");
        byte[] data = new byte[16];
        data[0] = stickMode;
        data[1] = 0x0B;
        data[2] = 0x01;
        return writeCharacteristic(data);
    }

    public void checkStickLocationInfo(ArrayList<Integer> checkList) {
        for (int i : checkList) {
            while (!CheckLocationInfo((byte) i)) {
//                Log.d(TAG, "Write CheckLocationInfo Failed");
                if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                    break;
                }
            }
            while (!readCharacteristic()) {
//                Log.d(TAG, "Read Characteristic Failed");
                if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                    break;
                }
            }
        }
    }

    public boolean CheckLocationInfo(byte keyCode) {
//        Log.d(TAG, "CheckLocationInfo");
        byte[] data = new byte[16];
        data[0] = stickMode;
        data[1] = 0x05;
        data[2] = 0x01;
        data[3] = 0x01;
        data[4] = keyCode;
        data[5] = 0x01;
        return writeCharacteristic(data);
    }

    public void checkStickFunctionParameter(ArrayList<Integer> checkList) {
        for (int i : checkList) {
            switch (i) {
                case 17:
                case 18:
                    while (!CheckFunctionParameter((byte) (i - 17))) {
//                        Log.d(TAG, "Write CheckFunctionParameter Failed");
                        if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                            break;
                        }
                    }
                    while (!readCharacteristic()) {
//                        Log.d(TAG, "Read Characteristic Failed");
                        if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                            break;
                        }
                    }
                    break;
                default:
                    while (!CheckFunctionParameter((byte) (i + 1))) {
//                        Log.d(TAG, "Write CheckFunctionParameter Failed");
                        if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                            break;
                        }
                    }
                    while (!readCharacteristic()) {
//                        Log.d(TAG, "Read Characteristic Failed");
                        if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                            break;
                        }
                    }
                    break;
            }
        }
    }

    public boolean CheckFunctionParameter(byte functionCode) {
//        Log.d(TAG, "CheckFunctionParameter");
        byte[] data = new byte[16];
        data[0] = stickMode;
        data[1] = 0x09;
        data[2] = 0x01;
        data[3] = 0x01;
        data[4] = functionCode;
        data[5] = 0x01;
        return writeCharacteristic(data);
    }

    public void checkStickBondKeyLocationInfo(byte mainKey, ArrayList<Integer> checkList) {
        for (int i : checkList) {
            while (!CheckBondKeyLocationInfo(mainKey, (byte) i)) {
//                Log.d(TAG, "Write CheckBondKeyLocationInfo Failed");
                if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                    break;
                }
            }
            while (!readCharacteristic()) {
//                Log.d(TAG, "Read Characteristic Failed");
                if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                    break;
                }
            }
        }
    }

    public boolean CheckBondKeyLocationInfo(byte mainKey, byte keyCode) {
//        Log.d(TAG, "CheckBondKeyLocationInfo");
        byte[] data = new byte[16];
        data[0] = stickMode;
        data[1] = 0x10;
        data[2] = 0x01;
        data[3] = 0x03;
        data[4] = mainKey;
        data[5] = keyCode;
        data[6] = 0x01;
        return writeCharacteristic(data);
    }

    //</editor-fold>

    //<editor-fold desc="<writeCharacteristic>">

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (!checkGatt())
            return false;
/*
        checkcount = 0;
        while(mBusy){
            checkcount++;
            if (checkcount > 5120){
                Log.i(TAG, "writeCharacteristic -> mBusy =  " + mBusy + " -> break\n");
                break;
            }
        }
*/
        mBusy = true;
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public boolean writeCharacteristic(byte[] data) {
//        Log.d("SSSSS",byteToString(data)+"");
        if (interval > 200) {
//            mBluetoothGatt.disconnect();
//            connectedDeviceAddress = "";
//            connectedDeviceName = "";
//            broadcastUpdate(ACTION_GATT_DISCONNECTED);
//            mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
//            Log.i(TAG, "Disconnected from GATT server.");
            return true;
        }
        interval += 10;
        waitIdle(100);
        //check mBluetoothGatt is available
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }
        BluetoothGattService Service = mBluetoothGatt.getService(UUID.fromString("0000ffa0-0000-1000-8000-00805f9b34fb"));
        if (Service == null) {
            Log.e(TAG, "write s service not found!");
            return false;
        }
        BluetoothGattCharacteristic charac = Service.getCharacteristic(UUID.fromString("0000ffa1-0000-1000-8000-00805f9b34fb"));
        if (charac == null) {
            Log.e(TAG, "char not found!");
            return false;
        }
        charac.setValue(data);
        charac.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBusy = true;
        return mBluetoothGatt.writeCharacteristic(charac);
    }

    //</editor-fold>

    //<editor-fold desc="<readCharacteristic>">
    public boolean readCharacteristic() {
//        try {
//            Thread.sleep(interval);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (mBusy) {
//            interval += 10;
//            return false;
//        } else if (interval > 0) {
//            interval -= 10;
//        }
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }
        BluetoothGattService Service = mBluetoothGatt.getService(UUID.fromString("0000ffa0-0000-1000-8000-00805f9b34fb"));
        if (Service == null) {
            Log.e(TAG, "read service not found!");
            return false;
        }
        BluetoothGattCharacteristic charac = Service.getCharacteristic(UUID.fromString("0000ffa2-0000-1000-8000-00805f9b34fb"));
        if (charac == null) {
            Log.e(TAG, "char not found!");
            return false;
        }
        mBusy = true;
        return mBluetoothGatt.readCharacteristic(charac);
    }

    public boolean readFirmwareVer() {
//        try {
//            Thread.sleep(interval);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (mBusy) {
//            interval += 10;
//            return false;
//        } else if (interval > 0) {
//            interval -= 10;
//        }
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }
        BluetoothGattService Service = mBluetoothGatt.getService(ServiceAttribute.FW_VER_SERVICE_UUID);
        if (Service == null) {
            Log.e(TAG, "read service not found!");
            return false;
        }
        BluetoothGattCharacteristic charac = Service.getCharacteristic(ServiceAttribute.FW_VER_CHARACTERISTIC_UUID);
        if (charac == null) {
            Log.e(TAG, "char not found!");
            return false;
        }
        mBusy = true;
        return mBluetoothGatt.readCharacteristic(charac);
    }
    //</editor-fold>

    //<editor-fold desc="<ServiceTimer>">
    @SuppressWarnings("unchecked")
    public void startStickTimer() {
        if (sTimer == null) {
            sTimer = new Timer();
            final TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    int key;
//                    Log.d(TAG, "start stick Timer");
                    try {
                        if (mConnectionState == BluetoothProfile.STATE_CONNECTED) {
                            String packageName = getTopAppName(getApplicationContext());
                            int rotation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                                    .getDefaultDisplay().getRotation();
                            if (mBluetoothGatt != null && !mBusy) {
                                if (!packageName.equals("") && !packageNameBuffer.equals(packageName)) {
                                    Message msg = new Message();
                                    msg.what = 1;
                                    mHandler.sendMessage(msg);
                                    packageNameBuffer = packageName;
                                    //TODO Download
//                                    if (StickGameValue.supportGameList.contains(packageName)) {
//                                        while (!(Arrays.toString(getFilesAllName(DEFAULT_INFO))).contains(packageName)) {
//                                            Downloader.PresetDownloadTask presetDownloadTask = new Downloader.PresetDownloadTask();
//                                            presetDownloadTask.setGamePackageName(packageName);
//                                            if (getApp().supportFlag) {
//                                                presetDownloadTask.execute("http://app.serafim-tech.com/SerafimPlay/S_Series/stick_" + getApp().ipCountryCode.toLowerCase() + "/" + packageName + ".txt");
//                                            } else {
//                                                presetDownloadTask.execute("http://app.serafim-tech.com/SerafimPlay/S_Series/stick_global/" + packageName + ".txt");
//                                            }
//                                            try {
//                                                Thread.sleep(500);
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    }
                                    if (getFilesAllName(CUSTOM_INFO_BIND_GAME).contains(packageName)) {
                                        openObjFunction(CUSTOM_INFO_BIND_GAME,packageName);
                                    } else if (getFilesAllName(DEFAULT_INFO).contains(packageName)) {
                                        openObjFunction(DEFAULT_INFO, packageName);
                                    }
                                }
                                if (rotationBuffer != rotation) {
                                    rotationBuffer = rotation;
                                    switch (rotation) {
                                        case Surface.ROTATION_0:
                                            screenOrientation = 0x00;
                                            Log.i("Rotation", "0");
                                            if (rotationFlag == 2) {
                                                for (int i = 0; i < pStickBuffer.size(); i++) {
                                                    key = pStickBuffer.keyAt(i);
                                                    pStickBuffer.put(key, new float[]{1 - pStickBuffer.get(key)[0], 1 - pStickBuffer.get(key)[1]});
                                                }
                                                for (int i = 0; i < cStickBuffer.size(); i++) {
                                                    key = cStickBuffer.keyAt(i);
                                                    cStickBuffer.put(key, new float[]{1 - cStickBuffer.get(key)[0], 1 - cStickBuffer.get(key)[1]});
                                                }
                                                for (int i = 0; i < sStickBuffer.size(); i++) {
                                                    key = sStickBuffer.keyAt(i);
                                                    sStickBuffer.put(key, new float[]{1 - sStickBuffer.get(key)[0], 1 - sStickBuffer.get(key)[1]});
                                                    switch (fStickBuffer.get(key)[1]) {
                                                        case 1:
                                                        case 3:
                                                            System.arraycopy(new byte[]{
                                                                    (byte) ((int) (sStickBuffer.get(key)[0] * 32767) >> 8),
                                                                    (byte) (int) (sStickBuffer.get(key)[0] * 32767),
                                                                    (byte) ((int) (sStickBuffer.get(key)[1] * 32767) >> 8),
                                                                    (byte) (int) (sStickBuffer.get(key)[1] * 32767),
                                                            }, 0, fStickBuffer.get(key), 2, 4);
                                                            break;
                                                        case 2:
                                                            System.arraycopy(new byte[]{
                                                                    (byte) ((int) (sStickBuffer.get(key)[0] * 32767) >> 8),
                                                                    (byte) (int) (sStickBuffer.get(key)[0] * 32767),
                                                                    (byte) ((int) (sStickBuffer.get(key)[1] * 32767) >> 8),
                                                                    (byte) (int) (sStickBuffer.get(key)[1] * 32767),
                                                            }, 0, fStickBuffer.get(key), 4, 4);
                                                            break;
                                                    }
                                                }
                                                updateStickTouchLocation(pStickBuffer, fStickBuffer);
                                                updateStickFunction(fStickBuffer);
                                                updateStickCombinationTouchLocation((byte) combinationKeyBuffer, cStickBuffer, false);
                                            }
                                            break;
                                        case Surface.ROTATION_90:
                                            screenOrientation = 0x01;
                                            Log.i("Rotation", "90");
                                            if (rotationFlag == 2) {
                                                for (int i = 0; i < pStickBuffer.size(); i++) {
                                                    key = pStickBuffer.keyAt(i);
                                                    pStickBuffer.put(key, new float[]{1 - pStickBuffer.get(key)[0], 1 - pStickBuffer.get(key)[1]});
                                                }
                                                for (int i = 0; i < cStickBuffer.size(); i++) {
                                                    key = cStickBuffer.keyAt(i);
                                                    cStickBuffer.put(key, new float[]{1 - cStickBuffer.get(key)[0], 1 - cStickBuffer.get(key)[1]});
                                                }
                                                for (int i = 0; i < sStickBuffer.size(); i++) {
                                                    key = sStickBuffer.keyAt(i);
                                                    sStickBuffer.put(key, new float[]{1 - sStickBuffer.get(key)[0], 1 - sStickBuffer.get(key)[1]});
                                                    switch (fStickBuffer.get(key)[1]) {
                                                        case 1:
                                                        case 3:
                                                            System.arraycopy(new byte[]{
                                                                    (byte) ((int) (sStickBuffer.get(key)[0] * 32767) >> 8),
                                                                    (byte) (int) (sStickBuffer.get(key)[0] * 32767),
                                                                    (byte) ((int) (sStickBuffer.get(key)[1] * 32767) >> 8),
                                                                    (byte) (int) (sStickBuffer.get(key)[1] * 32767),
                                                            }, 0, fStickBuffer.get(key), 3, 4);
                                                            break;
                                                        case 2:
                                                            System.arraycopy(new byte[]{
                                                                    (byte) ((int) (sStickBuffer.get(key)[0] * 32767) >> 8),
                                                                    (byte) (int) (sStickBuffer.get(key)[0] * 32767),
                                                                    (byte) ((int) (sStickBuffer.get(key)[1] * 32767) >> 8),
                                                                    (byte) (int) (sStickBuffer.get(key)[1] * 32767),
                                                            }, 0, fStickBuffer.get(key), 5, 4);
                                                            break;
                                                    }
                                                }
                                                updateStickTouchLocation(pStickBuffer, fStickBuffer);
                                                updateStickFunction(fStickBuffer);
                                                updateStickCombinationTouchLocation((byte) combinationKeyBuffer, cStickBuffer, false);
                                            }
                                            break;
                                        case Surface.ROTATION_180:
                                            screenOrientation = 0x03;
                                            Log.i("Rotation", "180");
                                            break;
                                        case Surface.ROTATION_270:
                                            screenOrientation = 0x02;
                                            Log.i("Rotation", "270");
                                            if (rotationFlag == 1 || rotationFlag == 0) {
                                                for (int i = 0; i < pStickBuffer.size(); i++) {
                                                    key = pStickBuffer.keyAt(i);
                                                    pStickBuffer.put(key, new float[]{1 - pStickBuffer.get(key)[0], 1 - pStickBuffer.get(key)[1]});
                                                }
                                                for (int i = 0; i < cStickBuffer.size(); i++) {
                                                    key = cStickBuffer.keyAt(i);
                                                    cStickBuffer.put(key, new float[]{1 - cStickBuffer.get(key)[0], 1 - cStickBuffer.get(key)[1]});
                                                }
                                                for (int i = 0; i < sStickBuffer.size(); i++) {
                                                    key = sStickBuffer.keyAt(i);
                                                    sStickBuffer.put(key, new float[]{1 - sStickBuffer.get(key)[0], 1 - sStickBuffer.get(key)[1]});
                                                    switch (fStickBuffer.get(key)[1]) {
                                                        case 1:
                                                        case 3:
                                                            System.arraycopy(new byte[]{
                                                                    (byte) ((int) (sStickBuffer.get(key)[0] * 32767) >> 8),
                                                                    (byte) (int) (sStickBuffer.get(key)[0] * 32767),
                                                                    (byte) ((int) (sStickBuffer.get(key)[1] * 32767) >> 8),
                                                                    (byte) (int) (sStickBuffer.get(key)[1] * 32767),
                                                            }, 0, fStickBuffer.get(key), 3, 4);
                                                            break;
                                                        case 2:
                                                            System.arraycopy(new byte[]{
                                                                    (byte) ((int) (sStickBuffer.get(key)[0] * 32767) >> 8),
                                                                    (byte) (int) (sStickBuffer.get(key)[0] * 32767),
                                                                    (byte) ((int) (sStickBuffer.get(key)[1] * 32767) >> 8),
                                                                    (byte) (int) (sStickBuffer.get(key)[1] * 32767),
                                                            }, 0, fStickBuffer.get(key), 5, 4);
                                                            break;
                                                    }
                                                }
                                                updateStickTouchLocation(pStickBuffer, fStickBuffer);
                                                updateStickFunction(fStickBuffer);
                                                updateStickCombinationTouchLocation((byte) combinationKeyBuffer, cStickBuffer, false);
                                            }
                                            break;
                                    }
                                    rotationFlag = screenOrientation;
                                    updateStickPhoneInfo(screenOrientation);
                                    broadcastUpdate(ServiceAttribute.ACTION_SCREEN_ROTATE);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            sTimer.schedule(task, 0, 1500);
        }
    }

    public void stopStickTimer() {
        Log.d(TAG, "Stop Stick Timer");
        if (sTimer != null) {
            sTimer.cancel();
            sTimer = null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="<StickInfoUpdate>">

    //<editor-fold desc="<StickDefaultUpdate>">
    @SuppressWarnings("unchecked")
    public void writeDefault(String packageName) {
        if (mConnectionState == BluetoothProfile.STATE_CONNECTED) {
            Object[] obj;
            int key;
            if (getFilesAllName(DEFAULT_INFO).contains(packageName)) {
                obj = PresetJsonToObject(ReadFile(DEFAULT_INFO,packageName));
                if (obj != null) {
                    sStick.clear();
                    pStick.clear();
                    fStick.clear();
                    cStick.clear();
                    pStick = (SerializableSparseArray<float[]>) obj[1];
                    fStick = (SerializableSparseArray<byte[]>) obj[2];
                    combinationKey = (int) obj[3];
                    cStick = (SerializableSparseArray<float[]>) obj[4];
                    if (obj.length == 6) {
                        Log.e("obj.length == 6", "obj.length == 6");
                        macroKeys = (SerializableSparseArray<String>) obj[5];
                    } else {
                        Log.e("obj.length != 6", "obj.length != 6");
                        macroKeys = new SerializableSparseArray<>();
                    }
                    for (int i = 0; i < fStick.size(); i++) {
                        key = fStick.keyAt(i);
                        byte[] data = fStick.get(key);
                        float x, y;
                        if (key != 17 && key != 18) {
                            switch (data[1]) {
                                case 1:
                                case 3:
                                    x = (((data[3] & 0xFF) << 8) | (data[4] & 0xFF)) / 32767F;
                                    y = (((data[5] & 0xFF) << 8) | (data[6] & 0xFF)) / 32767F;
                                    sStick.put(key, new float[]{x, y});
                                    break;
                                case 2:
                                    x = (((data[5] & 0xFF) << 8) | (data[6] & 0xFF)) / 32767F;
                                    y = (((data[7] & 0xFF) << 8) | (data[8] & 0xFF)) / 32767F;
                                    sStick.put(key, new float[]{x, y});
                                    break;
                            }
                        }
                    }
                }
                combinationKeyBuffer = combinationKey;
                pStickBuffer = (SerializableSparseArray<float[]>) pStick.clone();
                fStickBuffer = (SerializableSparseArray<byte[]>) fStick.clone();
                sStickBuffer = (SerializableSparseArray<float[]>) sStick.clone();
                cStickBuffer = (SerializableSparseArray<float[]>) cStick.clone();
                presetName = "";
                presetNameBuffer = "";
                updateStickPhoneInfo((byte) obj[0]);
                updateStickTouchLocation(pStickBuffer, fStickBuffer);
                updateStickFunction(fStickBuffer);
                updateStickCombinationTouchLocation((byte) combinationKeyBuffer, cStickBuffer, false);
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="<StickPhoneUpdate>">
    public boolean SetupPhoneInfo(byte rotation) {
//        Log.d(TAG, "SetupPhoneInfo");
        byte[] data = new byte[16];
        data[0] = stickMode;
        data[1] = 0x0A;
        data[2] = 0x01;
        data[3] = 0x05;
        data[4] = rotation;
        data[5] = (byte) (getApp().windowWidth >> 8);
        data[6] = (byte) getApp().windowWidth;
        data[7] = (byte) (getApp().windowHeight >> 8);
        data[8] = (byte) getApp().windowHeight;
        return writeCharacteristic(data);
    }

    public void updateStickPhoneInfo(byte rotation) {
        rotationFlag = rotation;
        while (!SetupPhoneInfo(rotation)) {
//            Log.d(TAG, "Write SetupPhoneInfo Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="<StickLocationUpdate>">

    public void updateStickTouchLocation(SerializableSparseArray<float[]> pStick, SerializableSparseArray<byte[]> fStick) {
        while (!ClearAllLocationInfo()) {
//            Log.d(TAG, "Write ClearAllLocationInfo Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
        writeStickTouchLocation(pStick, fStick);
    }

    public boolean ClearAllLocationInfo() {
//        Log.d(TAG, "ClearAllLocationInfo");
        byte[] data = new byte[16];
        data[0] = stickMode;
        data[1] = 0x03;
        data[2] = 0x01;
        return writeCharacteristic(data);
    }

    public void writeStickTouchLocation(SerializableSparseArray<float[]> pStickRateList, SerializableSparseArray<byte[]> fStickList) {
        float xRate;
        float yRate;
        int keyCode;
        int[] pStickList = {0, 0};
        for (int i = 0; i < pStickRateList.size(); i++) {
            keyCode = pStickRateList.keyAt(i);
            if (fStickList.get(keyCode)[1] != (byte) 0xF0) {
                xRate = pStickRateList.get(keyCode)[0];
                yRate = pStickRateList.get(keyCode)[1];
                pStickList[0] = (int) (32767 * xRate);
                pStickList[1] = (int) (32767 * yRate);
                while (!SetupLocationInfo((byte) keyCode, pStickList)) {
//                Log.d(TAG, "Write SetupLocationInfo Failed");
                    if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                        break;
                    }
                }
            }

        }
    }

    public boolean SetupLocationInfo(byte keyCode, int[] pStickList) {
//        Log.d(TAG, "SetupLocationInfo");
        byte[] data = new byte[16];
        data[0] = stickMode;
        data[1] = 0x02;
        data[2] = 0x01;
        data[3] = 0x05;
        if (pStickList != null) {
            data[4] = keyCode;
            data[5] = (byte) (pStickList[0] >> 8);
            data[6] = (byte) pStickList[0];
            data[7] = (byte) (pStickList[1] >> 8);
            data[8] = (byte) pStickList[1];
        }
        return writeCharacteristic(data);
    }
    //</editor-fold>

    //<editor-fold desc="<StickFunctionUpdate>">

    public void openObjFunction(String dirName, String objName) {
        int key;
        Object[] obj = PresetJsonToObject(ReadFile(dirName,objName));
        if (obj != null) {
            sStick.clear();
            pStick.clear();
            fStick.clear();
            cStick.clear();
            pStick = (SerializableSparseArray<float[]>) obj[1];
            fStick = (SerializableSparseArray<byte[]>) obj[2];
            combinationKey = (int) obj[3];
            cStick = (SerializableSparseArray<float[]>) obj[4];
            if (obj.length == 6) {
                Log.e("obj.length == 6", "obj.length == 6");
                macroKeys = (SerializableSparseArray<String>) obj[5];
            } else {
                Log.e("obj.length != 6", "obj.length != 6");
                macroKeys = new SerializableSparseArray<>();
            }
            for (int i = 0; i < fStick.size(); i++) {
                key = fStick.keyAt(i);
                byte[] data = fStick.get(key);
                float x, y;
                if (key != 17 && key != 18) {
                    switch (data[1]) {
                        case 1:
                        case 3:
                            x = (((data[3] & 0xFF) << 8) | (data[4] & 0xFF)) / 32767F;
                            y = (((data[5] & 0xFF) << 8) | (data[6] & 0xFF)) / 32767F;
                            sStick.put(key, new float[]{x, y});
                            break;
                        case 2:
                            x = (((data[5] & 0xFF) << 8) | (data[6] & 0xFF)) / 32767F;
                            y = (((data[7] & 0xFF) << 8) | (data[8] & 0xFF)) / 32767F;
                            sStick.put(key, new float[]{x, y});
                            break;
                    }
                }
            }
        }
        combinationKeyBuffer = combinationKey;
        pStickBuffer = (SerializableSparseArray<float[]>) pStick.clone();
        fStickBuffer = (SerializableSparseArray<byte[]>) fStick.clone();
        sStickBuffer = (SerializableSparseArray<float[]>) sStick.clone();
        cStickBuffer = (SerializableSparseArray<float[]>) cStick.clone();
        presetName = "";
        presetNameBuffer = "";
        openSetupWithClear();
        updateStickPhoneInfo((byte) obj[0]);
        updateStickTouchLocation(pStickBuffer, fStickBuffer);
        updateStickFunction(fStickBuffer);
        updateStickCombinationTouchLocation((byte) combinationKeyBuffer, cStickBuffer, false);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.out.println("got interrupted!");
        }
        openTouch();
    }

    public void updateStickFunction(SerializableSparseArray<byte[]> fStick) {
        while (!ClearAllFunParameter()) {
//            Log.d(TAG, "Write ClearAllFunParameter Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
        writeStickFunction(fStick);

        if (MacroFlag) {
            MacroFlag = false;
        }
        for (int tag = 1; tag < 15; tag++) {
            if (fStick.get(tag) != null) {
                if ((byte) fStick.get(tag)[1] == (byte) 0xF0) {
                    MacroFlag = true;
                }
            }
        }
        openTouch();
    }

    public boolean ClearAllFunParameter() {
//        Log.d(TAG, "ClearAllFunParameter");
        byte[] data = new byte[16];
        data[0] = stickMode;
        data[1] = 0x07;
        data[2] = 0x01;
        return writeCharacteristic(data);
    }

    public void writeStickFunction(SerializableSparseArray<byte[]> fStickList) {
        byte[] values;
//        while (!SetupFunctionParameter(new byte[]{0x02,0x01,0x01,0x3A,(byte)0xE6,0x29,(byte)0xE6})) {
//            Log.d(TAG, "Write SetupFunctionParameter Failed");
//        }
        for (int i = 0; i < fStickList.size(); i++) {
            values = fStickList.valueAt(i);
//            Log.d(fStickList.keyAt(i) + "", byteToString(values).toString());
            while (!SetupFunctionParameter(values)) {
//                Log.d(TAG, "Write SetupFunctionParameter Failed");
                if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                    break;
                }
            }
        }
    }

    public boolean SetupFunctionParameter(byte[] functionContent) {
//        Log.d(TAG, "SetupFunctionParameter");
        byte[] data = new byte[16];
        data[0] = stickMode;
        data[1] = 0x06;
        data[2] = 0x01;
        data[3] = (byte) functionContent.length;
        System.arraycopy(functionContent, 0, data, 4, functionContent.length);
        return writeCharacteristic(data);
    }
    //</editor-fold>

    //<editor-fold desc="<StickCombnationUpdate>">

    public void updateStickCombinationTouchLocation(byte mainKey, SerializableSparseArray<float[]> cfStick, boolean readFlag) {
        while (!SetupBondKeyLocationInfo((byte) 0, (byte) 0, new int[]{0, 0}, (byte) 1)) {
//            Log.d(TAG, "Write ClearAllCombinationLocationInfo Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
        writeStickCombinationTouchLocation(mainKey, cfStick, (byte) 0);
        if (readFlag) {
            while (!readCharacteristic()) {
//                Log.d(TAG, "Read Characteristic Failed");
                if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                    break;
                }
            }
        }
    }

    public boolean SetupBondKeyLocationInfo(byte mainKey, byte keyCode, int[] pStickList, byte clearFlag) {
//        Log.d(TAG, "SetupBondKeyLocationInfo");
        byte[] data = new byte[16];
        data[0] = stickMode;
        data[1] = 0x0F;
        data[2] = 0x01;
        data[3] = 0x07;
        if (pStickList != null) {
            data[4] = mainKey;
            data[5] = keyCode;
            data[6] = (byte) (pStickList[0] >> 8);
            data[7] = (byte) pStickList[0];
            data[8] = (byte) (pStickList[1] >> 8);
            data[9] = (byte) pStickList[1];
            data[10] = clearFlag;
        }
        return writeCharacteristic(data);
    }

    public void writeStickCombinationTouchLocation(byte mainKey, SerializableSparseArray<float[]> cfStickRateList, byte clearFlag) {
        float xRate;
        float yRate;
        int keyCode;
        int[] cfStickList = {0, 0};
        for (int i = 0; i < cfStickRateList.size(); i++) {
            keyCode = cfStickRateList.keyAt(i);
            xRate = cfStickRateList.get(keyCode)[0];
            yRate = cfStickRateList.get(keyCode)[1];
            cfStickList[0] = (int) (32767 * xRate);
            cfStickList[1] = (int) (32767 * yRate);
            while (!SetupBondKeyLocationInfo(mainKey, (byte) keyCode, cfStickList, clearFlag)) {
//                Log.d(TAG, "Write SetupLocationInfo Failed");
                if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                    break;
                }
            }
        }
    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="<StickMacro>">
    public boolean WriteAppKeyBytes(byte[] b) {
//        Log.d(TAG, "WriteAppKeyBytes");
        byte[] data = new byte[16];
        data[0] = 0x20;
        System.arraycopy(b, 0, data, 1, 11);
        return writeCharacteristic(data);
    }
    //</editor-fold>

    //<editor-fold desc="<StickMode>">
    public void openMacroPlay() {
        Log.d(TAG, "Open Macro Play");
        while (!ModeEnableSetting((byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00)) {
            Log.d(TAG, "Write ModeEnableSetting Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
        hidFlag = false;
        notifyFlag = false;
        recordFlag = false;
        playFlag = true;
    }

    public void openMacroRecord() {
        Log.d(TAG, "Open Macro Record");
        while (!ModeEnableSetting((byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00)) {
            Log.d(TAG, "Write ModeEnableSetting Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
        hidFlag = false;
        notifyFlag = false;
        recordFlag = true;
        playFlag = false;
    }

    public void openNotify() {
        Log.d(TAG, "Open Notify");
        while (!ModeEnableSetting((byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00)) {
            Log.d(TAG, "Write ModeEnableSetting Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
        recordFlag = false;
        playFlag = false;
        hidFlag = false;
        notifyFlag = true;
    }

    public void openTouch() {
        Log.d(TAG, "Open Touch");
        while (!SetTouchClear()) {
            Log.d(TAG, "Write SetTouchClear Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
        if (MacroFlag) {
            while (!ModeEnableSetting((byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01)) {
                Log.d(TAG, "Write ModeEnableSetting Failed");
                if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                    break;
                }
            }
        } else {
            while (!ModeEnableSetting((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00)) {
                Log.d(TAG, "Write ModeEnableSetting Failed");
                if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                    break;
                }
            }
        }

        notifyFlag = false;
        recordFlag = false;
        playFlag = false;
        hidFlag = true;
    }

    public void openSetup() {
        Log.d(TAG, "Open Setup");
        while (!SetTouchClear()) {
            Log.d(TAG, "Write SetTouchClear Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
        notifyFlag = false;
        recordFlag = false;
        playFlag = false;
        hidFlag = false;
    }

    public void openSetupWithClear() {
        Log.d(TAG, "Open Setup With Clear");
        while (!ModeEnableSetting((byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00)) {
            Log.d(TAG, "Write ModeEnableSetting Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
        while (!SetTouchClear()) {
            Log.d(TAG, "Write SetTouchClear Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
        notifyFlag = false;
        recordFlag = false;
        playFlag = false;
        hidFlag = false;
    }

    public boolean ModeEnableSetting(byte b, byte b2, byte b3, byte b4, byte b5) {
//        Log.d(TAG, "ModeEnableSetting");
        byte[] data = new byte[16];
        data[0] = stickMode;
        data[1] = 0x01;
        data[2] = 0x01;
        data[3] = 0x01;
        data[4] = b;
        data[5] = b2;
        data[6] = b3;
        data[7] = b4;
        data[8] = b5;
        return writeCharacteristic(data);
    }

    public boolean SetTouchClear() {
        Log.d(TAG, "SetTouchClear");
        byte[] data = new byte[16];
        data[0] = stickMode;
        data[1] = 0x12;
        data[2] = 0x01;
        return writeCharacteristic(data);
    }
    //</editor-fold>

    //<editor-fold desc="<StickOS>">
    public void setStickOSType(int type) {
        while (!SetOSType(type)) {
//            Log.d(TAG, "Write CheckOSType Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
        while (!readCharacteristic()) {
//            Log.d(TAG, "Read Characteristic Failed");
            if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED || interval > 200) {
                break;
            }
        }
    }

    public boolean SetOSType(int type) {
//        Log.d(TAG, "CheckOSType");
        byte[] data = new byte[16];
        data[0] = stickMode;
        data[1] = 0x11;
        data[2] = 0x01;
        data[3] = 0x01;
        data[4] = (byte) type;
        return writeCharacteristic(data);
    }
    //</editor-fold>

    //<editor-fold desc="<WaitTime>">
    public boolean waitIdle(int timeout) {
        timeout /= 10;
        while (--timeout > 0) {
            if (mBusy)
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            else
                break;
        }

        return timeout > 0;
    }
    //</editor-fold>

    //<editor-fold desc="<Debug>">
    public StringBuilder byteToString(byte[] data) {
        StringBuilder str = new StringBuilder();
        str.append("0x");
        for (int i = 0; i < data.length; i++)
            str.append(String.format("%02X", data[i]));
        return str;
    }
    //</editor-fold>
}