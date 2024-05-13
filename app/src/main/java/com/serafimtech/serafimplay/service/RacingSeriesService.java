package com.serafimtech.serafimplay.service;

import android.annotation.TargetApi;
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.MainActivity.serviceConnected;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.ReadFile;
import static com.serafimtech.serafimplay.file.value.InternalFileName.r_DEFAULT_INFO;

public class RacingSeriesService extends Service {
    //<editor-fold desc="<Variable>">
    private final String TAG = RacingSeriesService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();

    private BluetoothGatt mBluetoothGatt;
    public BluetoothDevice device;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGattService mOadService = null;
    private BluetoothGattService mConnControlService = null;
    private BluetoothGattService mSerafimService = null;

    public ArrayList<String> connectedAddress = new ArrayList<>();

    public String connectedDeviceAddress = "";
    public String connectedDeviceName = "";
    private String packageNameBuffer = "";
    private String mBluetoothDeviceAddress;

    private int rotationBuffer = 0;
    private int interval = 100;
    public int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
    private float[] pRacingRateListBuffer = {0.155F, 0.895F, 0.155F, 0.105F};
    private float statusBarHeightRate;

    private Handler mHandler;
    private Timer rTimer;

    private byte screenOrientation = 0x00;

    private boolean timeOut = false;
    public boolean mBusy = false; // Write/read pending response
    public boolean fwUpdateFlag = false;
    //</editor-fold>

    //<editor-fold desc="<Broadcast>">
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return intentFilter;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        @TargetApi(19)
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, action);
            try {
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                            Log.d(TAG, "bluetooth is not enabled");
                            if (mConnectionState == BluetoothProfile.STATE_CONNECTED) {
                                disconnect();
                            }
                            connectedAddress = new ArrayList<>();
                        }
                        if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                            if (serviceConnected) {
                                getRacingSeriesConnectedAddress();
                            }
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

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
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
        connectedAddress.clear();
        unregisterReceiver(mGattUpdateReceiver);
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
        public RacingSeriesService getService() {
            return RacingSeriesService.this;
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
                if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
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
                intentAction = ServiceAttribute.ACTION_GATT_DISCONNECTED;
                connectedDeviceAddress = "";
                connectedDeviceName = "";
                broadcastUpdate(intentAction);
//                intentAction = BluetoothDevice.ACTION_ACL_DISCONNECTED;
//                broadcastUpdate(intentAction);
                mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered");
                checkOad();
                broadcastUpdate(ServiceAttribute.ACTION_GATT_SERVICES_DISCOVERED);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        timeOut = true;
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
//            Log.d(TAG, "onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ServiceAttribute.ACTION_DATA_READ, characteristic, status);
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    Log.d(TAG, "Read data" + byteToString(data).toString());
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
                Log.d(TAG, "Notification data" + byteToString(data).toString());
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
        if (!fwUpdateFlag && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.getDevice().getBondState() != 12 && !fwUpdateFlag) {
                return false;
            }
            if (mBluetoothGatt.connect()) {
                mConnectionState = BluetoothProfile.STATE_CONNECTING;
                return true;
            }
        }
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
//        broadcastUpdate(ACTION_GATT_DISCONNECTED);
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

    public void getRacingSeriesConnectedAddress() {
        Log.d("getRacingSeries", "start");
        try {//得到连接状态的方法
            connectedAddress.clear();
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(adapter, (Object[]) null);

            if (state == BluetoothAdapter.STATE_CONNECTED) {
                Set<BluetoothDevice> devices = adapter.getBondedDevices();

                for (BluetoothDevice device : devices) {
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if (isConnected) {
                        if (device.getName() != null) {
                            Log.d(TAG, "get connected device name:" + device.getName());
                            if (device.getName().contains("Serafim R")) {
                                connectedAddress.add(device.getAddress());
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            getRacingSeriesBondedAddress();
        }
    }

    public void getRacingSeriesBondedAddress() {
        connectedAddress.clear();
        for (BluetoothDevice bluetoothDevice : mBluetoothAdapter.getBondedDevices()) {
//            Log.d(TAG, bluetoothDevice.getName());
            if (bluetoothDevice.getName() != null) {
                if (bluetoothDevice.getName().contains("Serafim R")) {
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
    //</editor-fold>

    //<editor-fold desc="<PacakgeName>">
    public String getTopAppName(Context context) {
        String strName = "";
        try {
            strName = getLollipopFGAppPackageName(context);
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
                Log.d(TAG, event.getPackageName() + ":" + event.getEventType());
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

    //<editor-fold desc="<writeCharacteristic>">

    public boolean writeCharacteristic(KeyboCmdForRacing cmdEnum, int switchNum, int[][] pRacingList) {
        return this.writeCharacteristic(cmdEnum, null, switchNum,
                pRacingList, null, null, null);
    }

    public boolean writeCharacteristic(KeyboCmdForRacing cmdEnum, byte[] gameCode,
                                       int switchNumber, int[][] pRacingList, byte[] deviceConnectState,
                                       byte[] dataMode, byte[] phoneType) {
        waitIdle(100);
        //check mBluetoothGatt is available
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }
        BluetoothGattService Service = mBluetoothGatt.getService(UUID.fromString("0000ffa0-0000-1000-8000-00805f9b34fb"));
        if (Service == null) {
            Log.e(TAG, "write r service not found!");
            return false;
        }
        BluetoothGattCharacteristic charac = Service.getCharacteristic(UUID.fromString("0000ffa1-0000-1000-8000-00805f9b34fb"));
        if (charac == null) {
            Log.e(TAG, "char not found!");
            return false;
        }

        byte switchQTY = 0x00;
        if (pRacingList != null)
            switchQTY = (byte) (pRacingList.length);
        else
            switchQTY = 0x00;

        byte[] writeData = new byte[16];

        byte racingMode = 0x11;
        writeData[0] = racingMode;

        byte resp = 0x01;
        switch (cmdEnum) {
            case Information:
                writeData[1] = 0x00;
                writeData[2] = 0x01;
                writeData[3] = resp;
                writeData[4] = 0x0B;
                writeData[5] = switchQTY;
                writeData[6] = deviceConnectState[0];
                writeData[7] = (byte) (getApp().windowWidth >> 8);
                writeData[8] = (byte) getApp().windowWidth;
                writeData[9] = (byte) (getApp().windowHeight >> 8);
                writeData[10] = (byte) getApp().windowHeight;
                writeData[11] = screenOrientation;
                writeData[12] = gameCode[0];
                writeData[13] = gameCode[1];
                writeData[14] = gameCode[2];
                writeData[15] = gameCode[3];
                break;
            case DeviceFunction:
                writeData[1] = 0x00;
                writeData[2] = 0x02;
                writeData[3] = resp;
                writeData[4] = 0x03;
                writeData[5] = deviceConnectState[0];
                writeData[6] = dataMode[0];
                writeData[7] = phoneType[0];
                break;
            case ScreenResolution:
                writeData[1] = 0x00;
                writeData[2] = 0x03;
                writeData[3] = resp;
                writeData[4] = 0x04;
                writeData[7] = (byte) ((int) getApp().windowWidth >> 8);
                writeData[8] = (byte) getApp().windowWidth;
                writeData[9] = (byte) ((int) getApp().windowHeight >> 8);
                writeData[10] = (byte) getApp().windowHeight;
                break;
            case ScreenOrientation:
                writeData[1] = 0x00;
                writeData[2] = 0x04;
                writeData[3] = resp;
                writeData[4] = 0x01;
                writeData[5] = screenOrientation;
                break;
            case GameCode:
                writeData[1] = 0x00;
                writeData[2] = 0x05;
                writeData[3] = resp;
                writeData[4] = 0x04;
                writeData[5] = gameCode[0];
                writeData[6] = gameCode[1];
                writeData[7] = gameCode[2];
                writeData[8] = gameCode[3];
                break;
            case DeviceSerialNumber:
                writeData[1] = 0x00;
                writeData[2] = 0x06;
                writeData[3] = 0x01;
                writeData[4] = 0x01;
                writeData[5] = 0x00;
                break;
            case FWVersion:
                writeData[1] = 0x00;
                writeData[2] = 0x07;
                writeData[3] = 0x01;
                writeData[4] = 0x00;
                break;
            case TouchLocation:
                if (pRacingList != null) {
                    writeData[1] = 0x00;
                    writeData[2] = 0x08;
                    writeData[3] = resp;
                    writeData[4] = 0x0B;
                    writeData[5] = switchQTY;
                    writeData[6] = (byte) switchNumber;
                    writeData[7] = (byte) (pRacingList[switchNumber - 1][0] >> 8);
                    writeData[8] = (byte) (int) pRacingList[switchNumber - 1][0];
                    writeData[9] = (byte) (pRacingList[switchNumber - 1][1] >> 8);
                    writeData[10] = (byte) (int) pRacingList[switchNumber - 1][1];
                    switchNumber++;
                    if (switchQTY >= switchNumber) {
                        writeData[11] = (byte) (switchNumber);
                        writeData[12] = (byte) (pRacingList[switchNumber - 1][0] >> 8);
                        writeData[13] = (byte) (int) pRacingList[switchNumber - 1][0];
                        writeData[14] = (byte) (pRacingList[switchNumber - 1][1] >> 8);
                        writeData[15] = (byte) (int) pRacingList[switchNumber - 1][1];
                    } else {
                        writeData[11] = 0x00;
                        writeData[12] = 0x00;
                        writeData[13] = 0x00;
                        writeData[14] = 0x00;
                        writeData[15] = 0x00;
                        break;
                    }
                }
                break;
            case GameKitType:
                writeData[1] = 0x00;
                writeData[2] = 0x09;
                writeData[3] = 0x01;
                writeData[4] = 0x00;
                break;
        }
        charac.setValue(writeData);
        charac.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBusy = true;
        return mBluetoothGatt.writeCharacteristic(charac);
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte b) {
        if (!checkGatt()) {
            return false;
        }

        byte[] val = new byte[1];
        val[0] = b;
        characteristic.setValue(val);

        mBusy = true;
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }

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

    //</editor-fold>

    //<editor-fold desc="<ServiceTimer>">
    public void stopRacingTimer() {
        Log.d(TAG, "Stop Racing Timer");
        if (rTimer != null) {
            rTimer.cancel();
            rTimer = null;
        }
    }

    public void startRacingTimer() {
        if (rTimer == null) {
            rTimer = new Timer();
            final TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (mConnectionState == BluetoothProfile.STATE_CONNECTED) {
                            String packageName = getTopAppName(getApplicationContext());
                            int rotation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                                    .getDefaultDisplay().getRotation();
                            if (mBluetoothGatt != null) {
                                if (!packageName.equals("") && !packageNameBuffer.equals(packageName)) {
                                    Message msg = new Message();
                                    msg.what = 1;
                                    mHandler.sendMessage(msg);
                                    String file = ReadFile(r_DEFAULT_INFO, packageName);
                                    if(file.equals("")){
                                        return;
                                    }
                                    packageNameBuffer = packageName;
//                                    if (raceFirstGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.16F, 0.892F, 0.16F, 0.1057F});
//                                    else if (raceSecondGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.16F, 0.892F, 0.16F, 0.1057F});
//                                    else if (raceThirdGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.27F, 0.95F, 0.27F, 0.85F});
//                                    else if (raceFourthGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.11F, 0.681F, 0.11F, 0.2427F});
//                                    else if (raceFifthGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.46F, 0.833F, 0.46F, 0.1198F});
//                                    else if (raceSixthGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0F, 0F, 0.13F, 0.5005F});
//                                    else if (raceSeventhGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.29F, 0.254F, 0.29F, 0.7849F});
//                                    else if (raceEighthGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.15F, 0.133F, 0.15F, 0.7516F});
//                                    else if (raceNinethGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.1203F, 0.9509F, 0.1203F, 0.8101F});
//                                    else if (raceTenthGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.2F, 0.9F, 0.2F, 0.1F});
//                                    else if (raceEleventhGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.16F, 0.95F, 0.16F, 0.15F});
//                                    else if (raceTwelfthGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.275F, 0.9381F, 0.121F, 0.8333F});
//                                    else if (raceThirteenthGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.1F, 0.901F, 0.1F, 0.754F});
//                                    else if (raceFourteenthGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.1213F, 0.7337F, 0F, 0F});
//                                    else if (raceFifteenthGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.131F, 0.9365F, 0.131F, 0.0708F});
//                                    else if (raceSixteenthGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.1213F, 0.9096F, 0.1213F, 0.2392F});
//                                    else if (raceSeventeenthGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{1F, 0F, 0.16F, 0.1057F});
//                                    else if (raceEighteenthGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.08F, 0.884F, 0.08F, 0.1257F});
//                                    else if (raceNineteenthGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.1213F, 0.7708F, 0.1213F, 0.2292F});
//                                    else if (raceTwentythGameCodeList.contains(packageNameBuffer))
//                                        writeRacingTouchLocation(new float[]{0.45F, 0.12F, 0.1213F, 0.12F});
//                                    else if (raceTwentyOnethGameCodeList.contains(packageName))
//                                        writeRacingTouchLocation(new float[]{0.5F, 0.9F, 0.5F, 0.1F});
                                    try {

                                        JSONArray jsonArray = new JSONArray(file);
                                        writeRacingTouchLocation(new float[]{(float) jsonArray.getDouble(0),
                                                (float) jsonArray.getDouble(1),
                                                (float) jsonArray.getDouble(2),
                                                (float) jsonArray.getDouble(3)});
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
//                            else if (drumFirstGameCodeList.contains(packageNameBuffer))
//                                write4TouchLocation(0.875F, 0.7078F, 0.625F, 0.7078F, 0.375F, 0.7078F, 0.125F, 0.7078F);
//                            else if (drumSecondGameCodeList.contains(packageNameBuffer))
//                                write4TouchLocation(0.2259F, 0.8797F, 0.212F, 0.6484F, 0.212F, 0.3516F, 0.2259F, 0.1203F);
                                } else if (rotationBuffer != rotation) {
                                    rotationBuffer = rotation;
                                    switch (rotation) {
                                        case Surface.ROTATION_0:
                                            screenOrientation = 0x00;
                                            Log.i("Rotation", "0");
                                            break;
                                        case Surface.ROTATION_90:
                                            screenOrientation = 0x01;
                                            Log.i("Rotation", "90");
                                            break;
                                        case Surface.ROTATION_180:
                                            screenOrientation = 0x03;
                                            Log.i("Rotation", "180");
                                            break;
                                        case Surface.ROTATION_270:
                                            screenOrientation = 0x02;
                                            Log.i("Rotation", "270");
                                            break;
                                    }
                                    if (!getApp().P2pHandler.p2pFlag) {
                                        if (writeRacingTouchLocation(pRacingRateListBuffer)) {
                                            Log.d("Write rotated location", "Success");
                                        } else {
                                            Log.d("Write rotated location", "Fail");
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            rTimer.schedule(task, 0, 1500);
        }
    }
    //</editor-fold>

    //<editor-fold desc="<RacingInfoUpdate>">
    public boolean writeRacingTouchLocation(float[] pRacingRateList) {
        float xRate1 = pRacingRateList[0];
        float yRate1 = pRacingRateList[1];
        float xRate2 = pRacingRateList[2];
        float yRate2 = pRacingRateList[3];
        //小心沒瀏海版本
        getApp().P2pHandler.requestConnectionInfo();
        Log.d(TAG, "P2pFlag: " + getApp().P2pHandler.p2pFlag);
        statusBarHeightRate = 0;
        Log.d("CoordinateXY", "p1:" + xRate1 + "," + yRate1 + ",p2:" + xRate2 + "," + yRate2);
        int[][] pRacingList = {{0, 0}, {0, 0}};
        statusBarHeightRate = getApp().statusBarHeight / getApp().windowHeight * 32767F;
        if (!getApp().P2pHandler.p2pFlag) {
//            Log.d("Custom flag", customFlag + "");
//            if (customFlag) {
//                pRacingList[0][0] = (int) (32767 * xRate1);
//                pRacingList[0][1] = (int) (32767 * yRate1);
//                pRacingList[1][0] = (int) (32767 * xRate2);
//                pRacingList[1][1] = (int) (32767 * yRate2);
//            } else {
            pRacingList[0][0] = (int) (32767 * xRate1);
            pRacingList[0][1] = (int) (statusBarHeightRate + (32767 - statusBarHeightRate) * yRate1);
            pRacingList[1][0] = (int) (32767 * xRate2);
            pRacingList[1][1] = (int) (statusBarHeightRate + (32767 - statusBarHeightRate) * yRate2);
//            }
        } else {
            pRacingList[0][0] = 5000;
            pRacingList[0][1] = 24476;
            pRacingList[1][0] = 26600;
            pRacingList[1][1] = 24476;
        }
        Log.d("Coordinate", "p1:" + pRacingList[0][0] + "," + pRacingList[0][1] + ",p2:" + pRacingList[1][0] + "," + pRacingList[1][1]);
        timeOut = false;
        mHandler.postDelayed(() -> timeOut = true, 3000);
        if (rotationBuffer == Surface.ROTATION_270 && !getApp().P2pHandler.p2pFlag) {
            while (!writeCharacteristic(KeyboCmdForRacing.TouchLocation, 1, rotationRacingTouchLocation(pRacingList)) && !timeOut) {
//                Log.d(TAG, "Write touch location: Fail");
            }
        } else {
            while (!writeCharacteristic(KeyboCmdForRacing.TouchLocation, 1, pRacingList) && !timeOut) {
//                Log.d(TAG, "Write touch location: Fail");
            }
        }
        if (timeOut) {
//            Log.d(TAG, "Write touch location: Time out");
            return false;
        } else {
            pRacingRateListBuffer = pRacingRateList;
            Log.d(TAG, "Write touch location: Success");
            return true;
        }
    }

    public int[][] rotationRacingTouchLocation(int[][] pRacingList) {
//        if (customFlag) {
//            pRacingList[0][0] = 32767 - pRacingList[0][0];
//            pRacingList[0][1] = (int) (32767 - pRacingList[0][1] + bias);
//            pRacingList[1][0] = 32767 - pRacingList[1][0];
//            pRacingList[1][1] = (int) (32767 - pRacingList[1][1] + bias);
//        } else {
        pRacingList[0][0] = 32767 - pRacingList[0][0];
        pRacingList[0][1] = (int) (32767 - pRacingList[0][1] - statusBarHeightRate);
        pRacingList[1][0] = 32767 - pRacingList[1][0];
        pRacingList[1][1] = (int) (32767 - pRacingList[1][1] - statusBarHeightRate);
//        }
        Log.d("Rotated coordinate", "p1:" + pRacingList[0][0] + "," + pRacingList[0][1] + ",p2:" + pRacingList[1][0] + "," + pRacingList[1][1]);
        return pRacingList;
    }

    enum KeyboCmdForRacing {
        Information,
        DeviceFunction,
        ScreenResolution,
        ScreenOrientation,
        GameCode,
        DeviceSerialNumber,
        FWVersion,
        TouchLocation,
        GameKitType,
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
}
    //</editor-fold>