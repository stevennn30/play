package com.serafimtech.serafimplay.tool;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;

/**
 * Created by Koerfer on 16.02.2016.
 */
public class WifiP2pHandler implements WifiP2pManager.ConnectionInfoListener{

    public WifiP2pManager Manager;
    public WifiP2pManager.Channel Channel;
    public boolean p2pFlag = false;

    public WifiP2pHandler(Context context) {
        Manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        Channel = Manager.initialize(context, Looper.getMainLooper(), null);
        requestConnectionInfo();
    }

    public void requestConnectionInfo(){
        Manager.requestConnectionInfo(Channel, this);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        p2pFlag = info.groupOwnerAddress != null;
    }
}