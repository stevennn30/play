package com.serafimtech.serafimplay.ui;

import static android.widget.ImageView.ScaleType.FIT_XY;
import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.MainActivity.mStickService;
import static com.serafimtech.serafimplay.file.value.InternalFileName.banner_file;

import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serafimtech.serafimplay.MainActivity;
import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.ui.tool.BannerFlipper;
import com.serafimtech.serafimplay.ui.tool.StickFloatWindow;
import com.serafimtech.serafimplay.ui.tool.StickGameListAdapter;

import java.io.File;
import java.util.ArrayList;

public class Navigation_Home_S extends Fragment {
    //<editor-fold desc="<Variable>">
    CheckBox checkBox;
    RecyclerView featuredRecyclerView;
    BannerFlipper bannerFlipper;

    boolean isLoading1 = false;

    ArrayList<String> rowsArrayList1;
//    ArrayList<String> gamePackageNames;
    StickGameListAdapter mStickGameListAdapter;
    //</editor-fold>

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigation_shome, container, false);
        checkBox = root.findViewById(R.id.custom_check_btn);
        featuredRecyclerView = root.findViewById(R.id.recycler_view_feature);
        featuredRecyclerView.setHasFixedSize(true);
        bannerFlipper = root.findViewById(R.id.banner_flipper);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkBox.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (((MainActivity) getActivity()).rpManager.isGotOverlayPermission()) {
                if (mStickService.mConnectionState == BluetoothProfile.STATE_CONNECTED) {
                    if (isChecked) {
                        if (((MainActivity) getActivity()).stickFloatWindow == null) {
                            ((MainActivity) getActivity()).stickFloatWindow = new StickFloatWindow(getActivity(),mStickService);
                            mStickService.mBusy = false;
                            new Thread(() -> {
                                mStickService.setCustomCharacteristicNotify(true);
                                mStickService.openTouch();
                                mStickService.checkStickPhoneInfo();
                            }).start();
                        }
                    } else {
                        ((MainActivity) getActivity()).closefloatwindow();
                    }
                } else {
                    buttonView.setChecked(false);
                    Toast.makeText(getActivity(), R.string.ble_did_not_connect_service, Toast.LENGTH_SHORT).show();
                }
            } else {
                ((MainActivity) getActivity()).rpManager.getOverlayPermission();
                buttonView.setChecked(false);
                Toast.makeText(getActivity(), getResources().getString(R.string.floating_window_permission), Toast.LENGTH_SHORT).show();
            }
        });

        ViewModel model = new ViewModelProvider(getActivity()).get(ViewModel.class);
        MutableLiveData<String> mutableLiveData = (MutableLiveData<String>) model.getdeviceconnect();
        mutableLiveData.observe(getViewLifecycleOwner(), s -> {
            if (s.contains("disconnect")) {
                checkBox.setChecked(false);
            }else if(s.contains("connect")){
                checkBox.setChecked(true);
            }
        });

        MutableLiveData<Boolean> mutableLiveData2 = (MutableLiveData<Boolean>) model.getupdaterecycler();
        mutableLiveData2.observe(getViewLifecycleOwner(), s -> {
            if (s) {
                initRecycleViewData("featured_main", featuredRecyclerView);
                mutableLiveData2.setValue(false);
            }
        });

//        switch (getApp().ipCountryCode) {
//            case "TW":
//            case "CN":
//                gamePackageNames = gamePackageNamesTW;
//                break;
//            case "JP":
//                gamePackageNames = gamePackageNamesJP;
//                break;
//            case "IT":
//                gamePackageNames = gamePackageNamesGlobal;
//                break;
//            default:
//                gamePackageNames = gamePackageNamesGlobal;
//                break;
//        }

        initRecycleViewData("featured_main", featuredRecyclerView);

        //banner
        String descDir = getApp().getDir(banner_file, Context.MODE_PRIVATE).getAbsolutePath();
        if (new File(descDir).isDirectory()) {
            File files[] = new File(descDir).listFiles();
            for (File value : files) {
                ImageView img = new ImageView(getContext());
                img.setImageBitmap(BitmapFactory.decodeFile(value.getAbsolutePath()));
                img.setScaleType(FIT_XY);
                bannerFlipper.addView(img);
            }
        }
    }

    public void initRecycleViewData(String adapterType, RecyclerView recyclerView) {
        int i = 0;
        isLoading1 = false;

        rowsArrayList1 = new ArrayList<>();
        while (i < getApp().featuredGameSequence.length + getApp().addedGameNameInDevice.size() + 1) {
            if (i < getApp().featuredGameSequence.length) {
                rowsArrayList1.add(getApp().gamePackageNames.get(getApp().featuredGameSequence[i]));
            } else if (i < getApp().featuredGameSequence.length + getApp().addedPackageNameInDevice.size()) {
                rowsArrayList1.add(getApp().addedPackageNameInDevice.get(i - getApp().featuredGameSequence.length));
            } else {
                rowsArrayList1.add("NONE");
            }
            i++;
        }
        mStickGameListAdapter = new StickGameListAdapter(this, adapterType, rowsArrayList1);

        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this.getActivity(), 4) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                // force height of viewHolder here, this will override layout_height from xml
                lp.width = getWidth() / 4;
                lp.height = getWidth() / 4;
                return true;
            }
        };
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(mStickGameListAdapter);
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//                if (!isLoading1) {
//                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == rowsArrayList1.size() - 1) {
//                        //bottom of list!
//                        if (rowsArrayList1.size() < getApp().featuredGameSequence.length + getApp().addedGameNameInDevice.size() + 1) {
//                            rowsArrayList1.add(null);
//                            recyclerView.post(() -> Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(rowsArrayList1.size() - 1));
//                            Handler handler = new Handler();
//                            handler.postDelayed(() -> {
//                                rowsArrayList1.remove(rowsArrayList1.size() - 1);
//                                int scrollPosition = rowsArrayList1.size() - 1;
//                                Objects.requireNonNull(Objects.requireNonNull(recyclerView.getAdapter())).notifyItemRemoved(scrollPosition);
//                                int currentSize = scrollPosition;
//                                int nextLimit;
//                                if (currentSize + 8 < getApp().featuredGameSequence.length + getApp().addedPackageNameInDevice.size() + 1) {
//                                    nextLimit = currentSize + 8;
//                                } else {
//                                    nextLimit = getApp().featuredGameSequence.length + getApp().addedPackageNameInDevice.size();
//                                }
//                                while (currentSize < nextLimit) {
//                                    if (currentSize + 1 < getApp().featuredGameSequence.length) {
//                                        rowsArrayList1.add(getApp().gamePackageNames.get(currentSize + 1));
//                                    } else if (currentSize + 1 < getApp().featuredGameSequence.length + getApp().addedPackageNameInDevice.size()) {
//                                        rowsArrayList1.add(getApp().addedPackageNameInDevice.get(currentSize + 1 - getApp().featuredGameSequence.length));
//                                    } else {
//                                        rowsArrayList1.add("NONE");
//                                    }
//                                    currentSize++;
//                                }
//
//                                recyclerView.getAdapter().notifyDataSetChanged();
//                                isLoading1 = false;
//                            }, 500);
//                        }
//                        isLoading1 = true;
//                    }
//                }
//            }
//        });
    }
}