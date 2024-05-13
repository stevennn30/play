package com.serafimtech.serafimplay.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.serafimtech.serafimplay.App;
import com.serafimtech.serafimplay.MainActivity;
import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.file.FTP;
import com.serafimtech.serafimplay.ui.tool.DraggableFloatView;
import com.serafimtech.serafimplay.ui.tool.DraggableFloatWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.MainActivity.mStickService;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.ReadFile;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.WriteFile;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.deleteRecursive;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.getFilesAllName;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.saveObject;
import static com.serafimtech.serafimplay.file.value.InternalFileName.ADDED_GAME_LIST;
import static com.serafimtech.serafimplay.file.value.InternalFileName.CUSTOM_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.CUSTOM_INFO_BIND_GAME;
import static com.serafimtech.serafimplay.file.value.InternalFileName.DATA_SYNC;
import static com.serafimtech.serafimplay.file.value.InternalFileName.DEVICE_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.USER_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.r_gamefile;
import static com.serafimtech.serafimplay.file.value.InternalFileName.s_gamefile;

public class Navigation_Gamepage extends Fragment {
    private ImageView returnBtn;
    private ImageView deleteBtn;
    private TextView gameCategory;
    private Button downloadBtn;
    private Button bindBtn;
    private TextView gameDescription;
    private TextView gameDeveloper;
    private TextView gameName;
    private ImageView gamePic;
    private View view3;
    private PDFView pdfView;
    private int gameIndex = 0;
    private Uri uri;
    private DraggableFloatWindow presetFloatWindow;
    private String r1recycleradaptertype;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigation_gamepage, container, false);
        returnBtn = root.findViewById(R.id.return_btn);
        deleteBtn = root.findViewById(R.id.delete_btn);
        gameCategory = root.findViewById(R.id.game_category);
        gameDescription = root.findViewById(R.id.game_description);
        gameDeveloper = root.findViewById(R.id.vendor);
        gameName = root.findViewById(R.id.game_name);
        gamePic = root.findViewById(R.id.game_pic);
        downloadBtn = root.findViewById(R.id.download_btn);
        bindBtn = root.findViewById(R.id.bind_btn);
        pdfView = root.findViewById(R.id.pdf_view);
        view3 = root.findViewById(R.id.view3);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewModel model = new ViewModelProvider(requireActivity()).get(ViewModel.class);
        MutableLiveData<String> mutableLiveData2 = (MutableLiveData<String>) model.getgamepage();
        mutableLiveData2.observe(getViewLifecycleOwner(), s -> {
            switch (Objects.requireNonNull(mutableLiveData2.getValue())) {
                case "Navigation_Home_R":
                case "Navigation_Home_S":
                    returnBtn.setOnClickListener((View v) -> NavHostFragment.findNavController(this).navigate(R.id.home));
                    break;
                case "Navigation_FeaturedGame":
                    returnBtn.setOnClickListener((View v) -> NavHostFragment.findNavController(this).navigate(R.id.featuredGame));
                    break;
            }
        });

        MutableLiveData<Integer> mutableLiveData = (MutableLiveData<Integer>) model.getgametag();
        mutableLiveData.observe(getViewLifecycleOwner(), s -> {
            if (getApp().productName == App.ProductName.S_Series) {
                gameIndex = mutableLiveData.getValue();
                updateUI();
            }

        });
        MutableLiveData<HashMap<String, String>> mutableLiveData3 = (MutableLiveData<HashMap<String, String>>) model.r1gamepage();
        mutableLiveData3.observe(getViewLifecycleOwner(), s -> {
            if (getApp().productName == App.ProductName.R_Series) {
                r1recycleradaptertype = mutableLiveData3.getValue().get("adapterType");
                gameIndex = Integer.parseInt(mutableLiveData3.getValue().get("getTag"));
                updateUI();
            }
        });
    }

    void updateUI() {
        switch (getApp().productName) {
            case R_Series:
                switch (getApp().localeCountryCode) {
                    case "TW":
                    case "CN":
                        downloadBtn.setTextSize(16);
                        break;
                    case "JP":
                        downloadBtn.setTextSize(14);
                        break;
                    default:
                        downloadBtn.setTextSize(12);
                        break;
                }
                if (gameIndex < getApp().gameNames.size()) {
//                    int pdfindex = gameIndex + 1

                    gameCategory.setVisibility(View.VISIBLE);
                    gameDeveloper.setVisibility(View.VISIBLE);
                    gameDescription.setVisibility(View.VISIBLE);
                    pdfView.setVisibility(View.VISIBLE);
                    view3.setVisibility(View.VISIBLE);

                    int gamePosition = 0;

                    switch (r1recycleradaptertype) {
                        case "featured":
                        case "featured_main":
                            gamePosition = getApp().featuredGameSequence[gameIndex];
                            break;
                        case "bike_featured":
                        case "bike_featured_main":
                            gamePosition = getApp().bikeFeaturedGameSequence[gameIndex];
                            break;
                        case "racing_featured":
                        case "racing_featured_main":
                            gamePosition = getApp().racingFeaturedGameSequence[gameIndex];
                            break;
                        default:
                            gamePosition = getApp().featuredGameSequence[gameIndex];
                            break;
                    }

                    Bitmap mBitmap = BitmapFactory.decodeFile(getApp().getDir(r_gamefile, Context.MODE_PRIVATE).getAbsolutePath() + "/" + gamePosition + ".png");
                    gamePic.setImageBitmap(mBitmap);

                    gameName.setText(getApp().gameNames.get(gamePosition));
                    gameDeveloper.setText(getApp().gameVendors.get(gamePosition));
                    gameDescription.setText(getApp().gameDescriptions.get(gamePosition));
                    gameCategory.setText(getApp().gameCategories.get(gamePosition));

                    uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getApp().gamePackageNames.get(gamePosition));
                    try {
                        pdfView.fromFile(new File(getApp().getDir(r_gamefile, Context.MODE_PRIVATE), gamePosition + ".pdf"))
                                .onRender(new OnRenderListener() {
                                    @Override
                                    public void onInitiallyRendered(int nbPages) {
                                        pdfView.fitToWidth(0);
                                    }
                                })
                                .load();
                    } catch (Exception e) {
                        Log.d("PDF", "No Source");
                    }

                    bindBtn.setVisibility(View.GONE);
                    deleteBtn.setVisibility(View.GONE);

                    if (getApp().installedGameList.contains(getApp().gamePackageNames.get(gamePosition))) {
                        try {
                            gamePic.setImageDrawable(getActivity().getPackageManager().getApplicationIcon(getApp().gamePackageNames.get(gamePosition)));
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        downloadBtn.setText(getResources().getString(R.string.open));
                        int finalGamePosition = gamePosition;
                        downloadBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(getApp().gamePackageNames.get(finalGamePosition));
                                startActivity(intent);
                            }
                        });
                    } else {
                        downloadBtn.setText(R.string.download);
                        downloadBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            }
                        });
                    }
                }
                break;
            case S_Series:
                if (gameIndex < getApp().featuredGameSequence.length) {
                    switch (getApp().localeCountryCode) {
                        case "TW":
                        case "CN":
                            downloadBtn.setTextSize(16);
                            bindBtn.setTextSize(16);
                            break;
                        case "JP":
                        case "IT":
                            downloadBtn.setTextSize(14);
                            bindBtn.setTextSize(14);
                            break;
                        default:
                            downloadBtn.setTextSize(12);
                            bindBtn.setTextSize(12);
                            break;
                    }
                    int gamePosition = getApp().featuredGameSequence[gameIndex];


                    switch (getApp().ipCountryCode) {
                        case "TW":
                            uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getApp().gamePackageNames.get(gamePosition) + "&hl=zh_TW");
                            break;
                        case "CN":
                            uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getApp().gamePackageNames.get(gamePosition) + "&hl=zh");
                            break;
                        case "JP":
                            uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getApp().gamePackageNames.get(gamePosition) + "&hl=ja");
                            break;
                        case "IT":
                            uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getApp().gamePackageNames.get(gamePosition) + "&hl=it");
                            break;
                        default:
                            uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getApp().gamePackageNames.get(gamePosition) + "&hl=it");
                            break;
                    }
                    gameCategory.setVisibility(View.VISIBLE);
                    gameDeveloper.setVisibility(View.VISIBLE);
                    gameDescription.setVisibility(View.VISIBLE);
                    pdfView.setVisibility(View.VISIBLE);
                    view3.setVisibility(View.VISIBLE);

                    Bitmap mBitmap = BitmapFactory.decodeFile(getApp().getDir(s_gamefile, Context.MODE_PRIVATE).getAbsolutePath() + "/" + gamePosition + ".png");
                    gamePic.setImageBitmap(mBitmap);
                    try {
                        pdfView.fromFile(new File(getApp().getDir(s_gamefile, Context.MODE_PRIVATE), gamePosition + ".pdf"))
                                .onRender(new OnRenderListener() {
                                    @Override
                                    public void onInitiallyRendered(int nbPages) {
                                        pdfView.fitToWidth(0);
                                    }
                                })
                                .load();
                    } catch (Exception e) {
                        Log.d("PDF", "No Source");
                    }

                    gameName.setText(getApp().gameNames.get(gamePosition));
                    gameCategory.setText(getApp().gameCategories.get(gamePosition));
                    gameDeveloper.setText(getApp().gameVendors.get(gamePosition));
                    gameDescription.setText(getApp().gameDescriptions.get(gamePosition));

                    bindBtn.setVisibility(View.GONE);
                    deleteBtn.setVisibility(View.GONE);
                    if (getApp().installedGameList.contains(getApp().gamePackageNames.get(gamePosition))) {
                        try {
                            gamePic.setImageDrawable(getActivity().getPackageManager().getApplicationIcon(getApp().gamePackageNames.get(gamePosition)));
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        downloadBtn.setText(getResources().getString(R.string.open));
                        downloadBtn.setOnClickListener(v -> {
                            Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(getApp().gamePackageNames.get(gamePosition));
                            startActivity(intent);
                            mStickService.writeDefault(getApp().gamePackageNames.get(gamePosition));
                        });
                    } else {
                        downloadBtn.setText(R.string.download);
                        downloadBtn.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, uri)));
                    }

                } else {
                    gameIndex -= getApp().featuredGameSequence.length;
                    try {
                        gamePic.setImageDrawable(getActivity().getPackageManager().getApplicationIcon(getApp().addedPackageNameInDevice.get(gameIndex)));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    gameName.setText(getApp().addedGameNameInDevice.get(gameIndex));
                    gameCategory.setVisibility(View.INVISIBLE);
                    gameDeveloper.setVisibility(View.INVISIBLE);
                    gameDescription.setVisibility(View.INVISIBLE);
                    bindBtn.setVisibility(View.VISIBLE);
                    bindBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (((MainActivity) getActivity()).rpManager.isGotOverlayPermission()) {
                                presetFloatWindow = new DraggableFloatWindow(getActivity().getApplicationContext(), DraggableFloatView.LayoutType.Preset);
                                presetFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Spinner newSpinner = ((ConstraintLayout) view.getParent()).findViewById(R.id.preset_spinner);
                                        String fileName = "";
                                        if (newSpinner.getSelectedItem() != null) {
                                            fileName = newSpinner.getSelectedItem().toString();
                                        }
                                        switch (view.getTag().toString()) {
                                            case "confirm":
                                                WriteFile(ReadFile(CUSTOM_INFO, fileName), CUSTOM_INFO_BIND_GAME, getApp().addedPackageNameInDevice.get(gameIndex));
                                                if(ReadFile(USER_INFO,DATA_SYNC).contains("true")) {
                                                    FTP.getInstance().jobToDo.add(new Object[]{
                                                            FTP.JobAction.UploadFile,
                                                            CUSTOM_INFO_BIND_GAME+"/"+getApp().addedPackageNameInDevice.get(gameIndex)
                                                    });
                                                }
//                                                getApp().bindGameHashMap.put(getApp().addedPackageNameInDevice.get(gameIndex), fileName);
//                                                saveObject(CUSTOM_INFO_BIND_GAME, BIND_GAME, new Object[]{getApp().bindGameHashMap});
                                                presetFloatWindow.dismiss();
                                                break;
                                            case "cancel":
                                                presetFloatWindow.dismiss();
                                                break;
                                            case "delete":
                                                File directory = getApp().getDir(CUSTOM_INFO, Context.MODE_PRIVATE);
                                                if (!fileName.equals("")) {
                                                    File myPath = new File(directory, fileName);
                                                    DraggableFloatWindow alertFloatWindow;
                                                    alertFloatWindow = new DraggableFloatWindow(getActivity().getApplicationContext(), DraggableFloatView.LayoutType.AlertDialog_2);
                                                    alertFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            switch (view.getTag().toString()) {
                                                                case "confirm":
                                                                    deleteRecursive(myPath);
                                                                    ArrayList<String> adapterStr = getFilesAllName(CUSTOM_INFO);
                                                                    if (adapterStr.size() == 0) {
                                                                        adapterStr = new ArrayList<>();
                                                                    }
                                                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                                                                            android.R.layout.simple_spinner_dropdown_item,
                                                                            adapterStr);
                                                                    newSpinner.setAdapter(adapter);
                                                                    break;
                                                                case "cancel":
                                                                    break;
                                                            }
                                                            alertFloatWindow.dismiss();
                                                        }

                                                        @Override
                                                        public void onSet(View view) {

                                                        }
                                                    });
                                                    alertFloatWindow.show();
                                                }
                                                break;
                                        }
                                    }

                                    @Override
                                    public void onSet(View view) {
                                        ArrayList<String> adapterStr = getFilesAllName(CUSTOM_INFO);
                                        if (adapterStr.size() == 0) {
                                            adapterStr = new ArrayList<>();
                                        }
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                                                android.R.layout.simple_spinner_dropdown_item,
                                                adapterStr);
                                        ((Spinner) view).setAdapter(adapter);
                                    }
                                });
                                presetFloatWindow.show();
                            } else {
                                ((MainActivity) getActivity()).rpManager.getOverlayPermission();
                                Toast.makeText(getActivity(), getResources().getString(R.string.floating_window_permission), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    deleteBtn.setVisibility(View.VISIBLE);
                    deleteBtn.setOnClickListener((View v) -> {
                        DraggableFloatWindow alertFloatWindow;
                        alertFloatWindow = new DraggableFloatWindow(getActivity().getApplicationContext(), DraggableFloatView.LayoutType.AlertDialog_2);
                        alertFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                            @Override
                            public void onClick(View view) {
                                switch (view.getTag().toString()) {
                                    case "confirm":
                                        //TODO
                                        getApp().gameNameInDevice.add(getApp().addedGameNameInDevice.get(gameIndex));
                                        getApp().packageNameInDevice.add(getApp().addedPackageNameInDevice.get(gameIndex));
                                        getApp().addedGameNameInDevice.remove(gameIndex);
                                        getApp().addedPackageNameInDevice.remove(gameIndex);
                                        saveObject(DEVICE_INFO, ADDED_GAME_LIST, new Object[]{getApp().addedPackageNameInDevice, getApp().addedGameNameInDevice});

                                        ViewModel model = new ViewModelProvider(requireActivity()).get(ViewModel.class);
                                        MutableLiveData<Boolean> mutableLiveData = (MutableLiveData<Boolean>) model.getupdaterecycler();
                                        mutableLiveData.setValue(true);

                                        Navigation.findNavController(getView()).navigate(R.id.home);
                                        break;
                                    case "cancel":
                                        break;
                                }
                                alertFloatWindow.dismiss();
                            }

                            @Override
                            public void onSet(View view) {
                            }
                        });
                        alertFloatWindow.show();
                    });
                    downloadBtn.setText(getResources().getString(R.string.open));
                    downloadBtn.setOnClickListener((View v) -> {
                        try {
                            Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(getApp().addedPackageNameInDevice.get(gameIndex));
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), getResources().getString(R.string.has_been_delete), Toast.LENGTH_SHORT).show();
                        }
                    });
                    pdfView.setVisibility(View.INVISIBLE);
                    view3.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }
}
