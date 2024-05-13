package com.serafimtech.serafimplay.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.serafimtech.serafimplay.MainActivity;
import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.ui.tool.ProgressDialogUtil;

import java.util.ArrayList;
import java.util.HashMap;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.App.productName;
import static com.serafimtech.serafimplay.MainActivity.mRacingService;
import static com.serafimtech.serafimplay.MainActivity.mStickService;

public class Navigation_Ble extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView pairedItemList;
    private boolean firstTimeUpdateUIFlag;
    ProgressDialogUtil mBLEProgressDialog;
    HashMap<String, Button> btnMap;
    HashMap<String, ImageView> imgMap;
    private Handler mHandler;
    MutableLiveData<String> mutableLiveData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigation_ble, container, false);

        swipeRefreshLayout = root.findViewById(R.id.refresh_layout);
        pairedItemList = root.findViewById(R.id.paired_item_list);

        mBLEProgressDialog = new ProgressDialogUtil();

        btnMap = new HashMap<>();
        imgMap = new HashMap<>();
        mHandler = new Handler();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            ((MainActivity) requireActivity()).rpManager.openBLE();
            switch (productName){
                case S_Series:
                    if(mStickService.connectedAddress.size() == 0){
                        mStickService.getStickSeriesBondedAddress();
                    }
                    break;
                case R_Series:
                    if(mRacingService.connectedAddress.size() == 0){
                        mRacingService.getRacingSeriesBondedAddress();
                    }
                    break;
            }
            updateUI();
            mHandler.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 1000);
        });

        firstTimeUpdateUIFlag = true;

        ViewModel model = new ViewModelProvider(requireActivity()).get(ViewModel.class);
        mutableLiveData = (MutableLiveData<String>) model.getdeviceconnect();
        //對LiveData進行監聽
        mutableLiveData.observe(getViewLifecycleOwner(), s -> updateUI());

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("BLE","onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    //<editor-fold desc="<BluetoothDeviceAdapter>">
    public void updateUI() {
        if (!firstTimeUpdateUIFlag) {
            try {
                Thread.sleep(250);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            firstTimeUpdateUIFlag = false;
        }
        LeDeviceListAdapter mPairedDeviceListAdapter = new LeDeviceListAdapter();
        switch (productName){
            case R_Series:
                for (String address : mRacingService.connectedAddress) {
                    mPairedDeviceListAdapter.addDevice(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
                }
                break;
            case S_Series:
                for (String address : mStickService.connectedAddress) {
                    mPairedDeviceListAdapter.addDevice(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
                }
                break;
        }
        mPairedDeviceListAdapter.notifyDataSetChanged();
        pairedItemList.setAdapter(mPairedDeviceListAdapter);
        if (mBLEProgressDialog != null)
            mBLEProgressDialog.dismiss();
    }

    static final class ViewHolder {
        ImageView img;
        TextView name;
        TextView address;
        Button stateBtn;
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        ArrayList<BluetoothDevice> mLeDevices;
        LayoutInflater mInflater;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mInflater = requireActivity().getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, final ViewGroup viewGroup) {
            final int position = i;
            final ViewHolder viewHolder;
            final BluetoothDevice device = mLeDevices.get(i);
            final String name = device.getName();
            final String address = device.getAddress();
            if (view == null) {
                view = mInflater.inflate(R.layout.device_listitem, null);
                viewHolder = new ViewHolder();
                viewHolder.img = view.findViewById(R.id.device_img);
                viewHolder.address = view.findViewById(R.id.device_address);
                viewHolder.name = view.findViewById(R.id.device_name);
                viewHolder.stateBtn = view.findViewById(R.id.state_btn);
                btnMap.put(address, viewHolder.stateBtn);
                imgMap.put(address, viewHolder.img);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.address.setText(device.getAddress());
            viewHolder.img.setImageResource(R.drawable.ic_btle);
            viewHolder.name.setText(name);
            switch (productName){
                case R_Series:
                    if (device.getAddress().equals(mRacingService.connectedDeviceAddress)) {
                        viewHolder.stateBtn.setText(R.string.disconnect_device);
                        viewHolder.img.setImageResource(R.drawable.ic_btleb);
                    } else {
                        viewHolder.stateBtn.setText(R.string.connect_device);
                    }
                    if (position % 2 == 0) {
                        view.setBackgroundColor(getResources().getColor(R.color.lighterBlack));
                    }
                    Log.d("Device name", mRacingService.connectedDeviceName);
                    break;
                case S_Series:
                    if (device.getAddress().equals(mStickService.connectedDeviceAddress)) {
                        viewHolder.stateBtn.setText(R.string.disconnect_device);
                        viewHolder.img.setImageResource(R.drawable.ic_btleb);
                    } else {
                        viewHolder.stateBtn.setText(R.string.connect_device);
                    }
                    if (position % 2 == 0) {
                        view.setBackgroundColor(getResources().getColor(R.color.lighterBlack));
                    }
                    Log.d("Device name", mStickService.connectedDeviceName);
                    break;
            }
            switch (getApp().localeCountryCode) {
                case "TW":
                case "CN":
                case "JP":
                    viewHolder.stateBtn.setTextSize(16);
                    break;
                default:
                    viewHolder.stateBtn.setTextSize(12);
                    break;
            }
            viewHolder.stateBtn.setOnClickListener((View v) -> {
                if (viewHolder.stateBtn.getText().toString().equals(getResources().getString(R.string.connect_device))) {
                    mBLEProgressDialog.showProgressDialogWithMessage(getActivity(), getResources().getString(R.string.connecting));
                    mHandler.postDelayed(() -> {
                        switch (productName){
                            case R_Series:
                                if (mRacingService.mConnectionState != STATE_CONNECTED) {
//                                Toast.makeText(fragmentActivity, R.string.connected_fail, Toast.LENGTH_SHORT).show();
                                    mBLEProgressDialog.dismiss();
                                }
                                break;
                            case S_Series:
                                if (mStickService.mConnectionState != STATE_CONNECTED) {
//                                Toast.makeText(fragmentActivity, R.string.connected_fail, Toast.LENGTH_SHORT).show();
                                    mBLEProgressDialog.dismiss();
                                }
                                break;
                        }
                    }, 5000);
                    ((MainActivity)getActivity()).ConnectToService(address);
                }
            });
            return view;
        }
    }
    //</editor-fold>
}