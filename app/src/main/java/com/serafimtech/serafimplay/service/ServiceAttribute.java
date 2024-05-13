package com.serafimtech.serafimplay.service;

import java.util.UUID;

public class ServiceAttribute {
    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_READ = "ACTION_DATA_READ";
    public final static String ACTION_DATA_NOTIFY = "ACTION_DATA_NOTIFY";
    public final static String ACTION_DATA_WRITE = "ACTION_DATA_WRITE";
    public final static String EXTRA_DATA = "EXTRA_DATA";
    public final static String EXTRA_UUID = "EXTRA_UUID";
    public final static String EXTRA_STATUS = "EXTRA_STATUS";
    public final static String ACTION_SCREEN_ROTATE = "ACTION_SCREEN_ROTATE";
    public final static UUID OAD_SERVICE_UUID = UUID.fromString("f000ffc0-0451-4000-b000-000000000000");
    public final static UUID CC_SERVICE_UUID = UUID.fromString("0000ccc0-0000-1000-8000-00805f9b34fb");
    public final static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID SERAFIM_SERVICE_UUID = UUID.fromString("0000ffa0-0000-1000-8000-00805f9b34fb");
    public final static UUID FW_VER_SERVICE_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public final static UUID FW_VER_CHARACTERISTIC_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
}
