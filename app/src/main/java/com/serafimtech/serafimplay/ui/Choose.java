package com.serafimtech.serafimplay.ui;

import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.App.productName;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.ReadFile;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.WriteFile;
import static com.serafimtech.serafimplay.file.ZipUtils.unZipFiles2;
import static com.serafimtech.serafimplay.file.value.InternalFileName.DEFAULT_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.FileDir;
import static com.serafimtech.serafimplay.file.value.InternalFileName.JSON;
import static com.serafimtech.serafimplay.file.value.InternalFileName.banner_file;
import static com.serafimtech.serafimplay.file.value.InternalFileName.r_DEFAULT_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.r_Manual;
import static com.serafimtech.serafimplay.file.value.InternalFileName.r_gamefile;
import static com.serafimtech.serafimplay.file.value.InternalFileName.r_update_version;
import static com.serafimtech.serafimplay.file.value.InternalFileName.s_Manual;
import static com.serafimtech.serafimplay.file.value.InternalFileName.s_gamefile;
import static com.serafimtech.serafimplay.file.value.InternalFileName.s_update_version;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.serafimtech.serafimplay.App;
import com.serafimtech.serafimplay.MainActivity;
import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.file.Downloader;
import com.serafimtech.serafimplay.tool.RequestPermissionManager;
import com.serafimtech.serafimplay.ui.tool.ProgressDialogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings("unchecked")
public class Choose extends Fragment {
    ConstraintLayout s1;
    ConstraintLayout r1;
    ProgressDialogUtil mProgressDialogUtil;
    Boolean button_flag = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.choose, container, false);
        s1 = root.findViewById(R.id.s1view);
        r1 = root.findViewById(R.id.r1view);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RequestPermissionManager rpManager = new RequestPermissionManager(getActivity());

        ViewModel model = new ViewModelProvider(getActivity()).get(ViewModel.class);
        MutableLiveData<String> mutableLiveData2 = (MutableLiveData<String>) model.getnavigationpage();
        mutableLiveData2.setValue(getClass().getSimpleName());

        s1.setOnClickListener((View v) -> {
            if (!button_flag) {
                button_flag = true;
                mProgressDialogUtil = new ProgressDialogUtil();
                mProgressDialogUtil.showProgressDialogWithMessage(getActivity(), getResources().getString(R.string.setting));

                productName = App.ProductName.S_Series;

//                //上架前記得再驗證一次
//                Object[] obj;
//                File directory;
//                File myPath;
//                obj = readObject(CUSTOM_INFO_BIND_GAME, BIND_GAME);
//                if (obj != null) {
//                    HashMap<String, String> hashMap = (HashMap<String, String>) obj[0];
//                    for (String key : hashMap.keySet()) {
//                        obj = readObject(CUSTOM_INFO, hashMap.get(key));
//                        JSONObject presetJSONObject = null;
//                        if (obj != null) {
//                            presetJSONObject = PresetObjectToJson(obj);
//                        }
//                        if (presetJSONObject != null) {
//                            WriteFile(presetJSONObject.toString(), CUSTOM_INFO_BIND_GAME, key);
//                        }
////                        if(ReadFile(USER_INFO,DATA_SYNC).contains("true")) {
////                            FTP.getInstance().uploadFileToFTP(CUSTOM_INFO_BIND_GAME, key);
////                        }
//                    }
//                }
//                if (getFilesAllName(CUSTOM_INFO_BIND_GAME).contains(BIND_GAME)) {
//                    directory = getApp().getDir(CUSTOM_INFO_BIND_GAME, Context.MODE_PRIVATE);
//                    myPath = new File(directory, BIND_GAME);
//                    deleteRecursive(myPath);
//                }
//                for (String str : getFilesAllName(CUSTOM_INFO)) {
//                    obj = readObject(CUSTOM_INFO, str);
//                    if (obj != null) {
//                        JSONObject presetJSONObject = PresetObjectToJson(obj);
//                        if (presetJSONObject != null) {
//                            WriteFile(presetJSONObject.toString(), CUSTOM_INFO, str);
//                        }
////                        if(ReadFile(USER_INFO,DATA_SYNC).contains("true")) {
////                            FTP.getInstance().uploadFileToFTP(CUSTOM_INFO, str);
////                        }
//                    }
//                }
//                for (String str : getFilesAllName(CUSTOM_MACRO_INFO)) {
//                    obj = readObject(CUSTOM_MACRO_INFO, str);
//                    if (obj != null) {
//                        JSONObject macroJSONObject = MacroPresetObjectToJson(obj);
//                        if (macroJSONObject != null) {
//                            WriteFile(macroJSONObject.toString(), CUSTOM_MACRO_INFO, str);
//                        }
////                        if(ReadFile(USER_INFO,DATA_SYNC).contains("true")) {
////                            FTP.getInstance().uploadFileToFTP(CUSTOM_MACRO_INFO, str);
////                        }
//                    }
//                }
//                for (String str : getFilesAllName(DEFAULT_INFO)) {
//                    if (str.equals(SUPPORTED_GAME_LIST) || str.equals(ADDED_GAME_LIST)) {
//                        continue;
//                    }
//                    obj = readObject(DEFAULT_INFO, str);
//                    if (obj != null) {
//                        JSONObject presetJSONObject = PresetObjectToJson(obj);
//                        if (presetJSONObject != null) {
//                            WriteFile(presetJSONObject.toString(), CUSTOM_INFO_BIND_GAME, str);
//                        }
////                        if(ReadFile(USER_INFO,DATA_SYNC).contains("true")) {
////                            FTP.getInstance().uploadFileToFTP(CUSTOM_INFO_BIND_GAME, str);
////                        }
//                    }
//                }

                if (rpManager.isConnected()) {
//                    if (ReadFile(USER_INFO, DATA_SYNC).contains("true")) {
//                        FTP.getInstance().uploadActionToFTP(FTP.Action.Init, null);
//                    }
                    new Downloader.checkIP().setOnDownloadFinishListener(result -> {
                        Locale locale;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            locale = getResources().getConfiguration().getLocales().get(0);
                        } else {
                            locale = getResources().getConfiguration().locale;
                        }

                        getApp().localeCountryCode = locale.getCountry();
                        if (result.equals("")) {
                            getApp().ipCountryCode = getApp().localeCountryCode;
                        } else {
                            getApp().ipCountryCode = result;
                        }

                        s_update();
                    }).execute();
                } else {
                    Toast.makeText(getActivity(), R.string.no_internet_connected, Toast.LENGTH_SHORT).show();
                }
            }
        });

        r1.setOnClickListener((View v) -> {
            if (!button_flag) {
                button_flag = true;
                mProgressDialogUtil = new ProgressDialogUtil();
                mProgressDialogUtil.showProgressDialogWithMessage(getActivity(), getResources().getString(R.string.setting));

                productName = App.ProductName.R_Series;

                if (rpManager.isConnected()) {
                    new Downloader.checkIP().setOnDownloadFinishListener(result -> {
                        Locale locale;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            locale = getResources().getConfiguration().getLocales().get(0);
                        } else {
                            locale = getResources().getConfiguration().locale;
                        }
                        getApp().localeCountryCode = locale.getCountry();
                        if (result.equals("")) {
                            getApp().ipCountryCode = getApp().localeCountryCode;
                        } else {
                            getApp().ipCountryCode = result;
                        }

                        r_update();
                    }).execute();

                } else {
                    Toast.makeText(getActivity(), R.string.no_internet_connected, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).unbindService();
        button_flag = false;
    }

    //<editor-fold desc="<update>">
    HashMap<String, Integer> preset = new HashMap<>();
    HashMap<String, Integer> gamefile = new HashMap<>();
    HashMap<String, Integer> Manual = new HashMap<>();
    int banner = 0;

    HashMap<String, Integer> preset_ftp = new HashMap<>();
    HashMap<String, Integer> gamefile_ftp = new HashMap<>();
    HashMap<String, Integer> Manual_ftp = new HashMap<>();
    int banner_ftp = 0;

    void s_update() {
        preset = new HashMap<>();
        gamefile = new HashMap<>();
        Manual = new HashMap<>();
        preset_ftp = new HashMap<>();
        gamefile_ftp = new HashMap<>();
        Manual_ftp = new HashMap<>();

        String file = ReadFile(JSON, s_update_version);
        if (!file.equals("")) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(file);
                if (jsonObject.getJSONObject("preset").has(getApp().ipCountryCode)) {
                    preset.put(getApp().ipCountryCode, jsonObject.getJSONObject("preset").getInt(getApp().ipCountryCode));
                } else {
                    preset.put("global", jsonObject.getJSONObject("preset").getInt("global"));
                }
                if (jsonObject.getJSONObject("gamefile").has(getApp().localeCountryCode)) {
                    gamefile.put(getApp().localeCountryCode, jsonObject.getJSONObject("gamefile").getInt(getApp().localeCountryCode));
                } else {
                    gamefile.put("global", jsonObject.getJSONObject("gamefile").getInt("global"));
                }
                if (jsonObject.getJSONObject("Manual").has(getApp().localeCountryCode)) {
                    Manual.put(getApp().localeCountryCode, jsonObject.getJSONObject("Manual").getInt(getApp().localeCountryCode));
                } else {
                    Manual.put("global", jsonObject.getJSONObject("Manual").getInt("global"));
                }
                if (jsonObject.has("banner")) {
                    banner = jsonObject.getInt("banner");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        new Downloader.DownloadTask().setOnDownloadFinishListener(result -> {
            try {
                JSONObject jsonObjectftp = new JSONObject(result);
                if (jsonObjectftp.getJSONObject("preset").has(getApp().ipCountryCode)) {
                    preset_ftp.put(getApp().ipCountryCode, jsonObjectftp.getJSONObject("preset").getInt(getApp().ipCountryCode));
                } else {
                    preset_ftp.put("global", jsonObjectftp.getJSONObject("preset").getInt("global"));
                }
                if (jsonObjectftp.getJSONObject("gamefile").has(getApp().localeCountryCode)) {
                    gamefile_ftp.put(getApp().localeCountryCode, jsonObjectftp.getJSONObject("gamefile").getInt(getApp().localeCountryCode));
                } else {
                    gamefile_ftp.put("global", jsonObjectftp.getJSONObject("gamefile").getInt("global"));
                }
                if (jsonObjectftp.getJSONObject("Manual").has(getApp().localeCountryCode)) {
                    Manual_ftp.put(getApp().localeCountryCode, jsonObjectftp.getJSONObject("Manual").getInt(getApp().localeCountryCode));
                } else {
                    Manual_ftp.put("global", jsonObjectftp.getJSONObject("Manual").getInt("global"));
                }
                if (jsonObjectftp.has("banner")) {
                    banner_ftp = jsonObjectftp.getInt("banner");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            s_update("preset");
        }).execute("http://app.serafim-tech.com/SerafimPlay/app_info/s_update_version.txt");
    }

    void s_update(String type) {
        switch (type) {
            case "preset":
                if (preset_ftp.containsKey(getApp().ipCountryCode)) {
                    if (!preset.containsKey(getApp().ipCountryCode)) {
                        preset.put(getApp().ipCountryCode, 0);
                    }
                    if (preset.get(getApp().ipCountryCode) < preset_ftp.get(getApp().ipCountryCode)) {
                        new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                            //假如下載的是壓縮檔
                            File zipFile1 = new File(FileDir, "filename");
                            try {
                                unZipFiles2(zipFile1, DEFAULT_INFO);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (zipFile1.delete()) {
                                Log.e("zipFile1.delete", "SUCCESS");
                            } else {
                                Log.e("zipFile1.delete", "FAIL");
                            }
                            preset.put(getApp().ipCountryCode, preset_ftp.get(getApp().ipCountryCode));
                            s_update("gamefile");
                        }).execute("http://app.serafim-tech.com/SerafimPlay/S_Series/stick/" + getApp().ipCountryCode + ".zip");
                    } else {
                        s_update("gamefile");
                    }
                } else {
                    if (!preset.containsKey("global")) {
                        preset.put("global", 0);
                    }
                    if (preset.get("global") < preset_ftp.get("global")) {
                        new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                            //假如下載的是壓縮檔
                            File zipFile1 = new File(FileDir, "filename");
                            try {
                                unZipFiles2(zipFile1, DEFAULT_INFO);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (zipFile1.delete()) {
                                Log.e("zipFile1.delete", "SUCCESS");
                            } else {
                                Log.e("zipFile1.delete", "FAIL");
                            }
                            preset.put("global", preset_ftp.get("global"));
                            s_update("gamefile");
                        }).execute("http://app.serafim-tech.com/SerafimPlay/S_Series/stick/stick_global.zip");
                    } else {
                        s_update("gamefile");
                    }
                }
                break;
            case "gamefile":
                if (gamefile_ftp.containsKey(getApp().localeCountryCode)) {
                    if (!gamefile.containsKey(getApp().localeCountryCode)) {
                        gamefile.put(getApp().localeCountryCode, 0);
                    }
                    if (gamefile.get(getApp().localeCountryCode) < gamefile_ftp.get(getApp().localeCountryCode)) {
                        new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                            //假如下載的是壓縮檔
                            File zipFile1 = new File(FileDir, "filename");
                            try {
                                unZipFiles2(zipFile1, s_gamefile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (zipFile1.delete()) {
                                Log.e("zipFile1.delete", "SUCCESS");
                            } else {
                                Log.e("zipFile1.delete", "FAIL");
                            }
                            gamefile.put(getApp().localeCountryCode, gamefile_ftp.get(getApp().localeCountryCode));
                            s_update("Manual");
                        }).execute("http://app.serafim-tech.com/SerafimPlay/S_Series/gamefile/" + getApp().localeCountryCode + ".zip");
                    } else {
                        s_update("Manual");
                    }
                } else {
                    if (!gamefile.containsKey("global")) {
                        gamefile.put("global", 0);
                    }
                    if (gamefile.get("global") < gamefile_ftp.get("global")) {
                        new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                            //假如下載的是壓縮檔
                            File zipFile1 = new File(FileDir, "filename");
                            try {
                                unZipFiles2(zipFile1, s_gamefile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (zipFile1.delete()) {
                                Log.e("zipFile1.delete", "SUCCESS");
                            } else {
                                Log.e("zipFile1.delete", "FAIL");
                            }
                            gamefile.put("global", gamefile_ftp.get("global"));
                            s_update("Manual");
                        }).execute("http://app.serafim-tech.com/SerafimPlay/S_Series/gamefile/gamefile_en.zip");
                    } else {
                        s_update("Manual");
                    }
                }
                break;
            case "Manual":
                if (Manual_ftp.containsKey(getApp().localeCountryCode)) {
                    if (!Manual.containsKey(getApp().localeCountryCode)) {
                        Manual.put(getApp().localeCountryCode, 0);
                    }
                    if (Manual.get(getApp().localeCountryCode) < Manual_ftp.get(getApp().localeCountryCode)) {
                        new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                            //假如下載的是壓縮檔
                            File zipFile1 = new File(FileDir, "filename");
                            try {
                                unZipFiles2(zipFile1, s_Manual);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (zipFile1.delete()) {
                                Log.e("zipFile1.delete", "SUCCESS");
                            } else {
                                Log.e("zipFile1.delete", "FAIL");
                            }
                            Manual.put(getApp().localeCountryCode, Manual_ftp.get(getApp().localeCountryCode));
                            s_update("banner");
                        }).execute("http://app.serafim-tech.com/SerafimPlay/S_Series/manual/" + getApp().localeCountryCode + ".zip");
                    } else {
                        s_update("banner");
                    }
                } else {
                    if (!Manual.containsKey("global")) {
                        Manual.put("global", 0);
                    }
                    if (Manual.get("global") < Manual_ftp.get("global")) {
                        new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                            //假如下載的是壓縮檔
                            File zipFile1 = new File(FileDir, "filename");
                            try {
                                unZipFiles2(zipFile1, s_Manual);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (zipFile1.delete()) {
                                Log.e("zipFile1.delete", "SUCCESS");
                            } else {
                                Log.e("zipFile1.delete", "FAIL");
                            }
                            Manual.put("global", Manual_ftp.get("global"));
                            s_update("banner");
                        }).execute("http://app.serafim-tech.com/SerafimPlay/S_Series/manual/manual_en.zip");
                    } else {
                        s_update("banner");
                    }
                }
                break;
            case "banner":
                if (banner_ftp > banner) {
                    new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                        //假如下載的是壓縮檔
                        File zipFile1 = new File(FileDir, "filename");
                        try {
                            unZipFiles2(zipFile1, banner_file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (zipFile1.delete()) {
                            Log.e("zipFile1.delete", "SUCCESS");
                        } else {
                            Log.e("zipFile1.delete", "FAIL");
                        }

                        banner = banner_ftp;

                        getApp().S_gamefile();
                        getApp().checkAddedPackageName();

                        Navigation.findNavController(requireView()).navigate(R.id.action_choose_to_main_drawer);
                        mProgressDialogUtil.dismiss();
                        try {
                            JSONObject presetJSONObject = new JSONObject();
                            JSONObject gamefileJSONObject = new JSONObject();
                            JSONObject ManualJSONObject = new JSONObject();
                            JSONObject JSONObject = new JSONObject();
                            if (preset.containsKey(getApp().ipCountryCode)) {
                                presetJSONObject.put(getApp().ipCountryCode, preset.get(getApp().ipCountryCode));
                            } else {
                                presetJSONObject.put("global", preset.get("global"));
                            }
                            if (gamefile.containsKey(getApp().localeCountryCode)) {
                                gamefileJSONObject.put(getApp().localeCountryCode, gamefile.get(getApp().localeCountryCode));
                            } else {
                                gamefileJSONObject.put("global", gamefile.get("global"));
                            }

                            if (Manual.containsKey(getApp().localeCountryCode)) {
                                ManualJSONObject.put(getApp().localeCountryCode, Manual.get(getApp().localeCountryCode));
                            } else {
                                ManualJSONObject.put("global", Manual.get("global"));
                            }
                            JSONObject.put("preset", presetJSONObject);
                            JSONObject.put("gamefile", gamefileJSONObject);
                            JSONObject.put("Manual", ManualJSONObject);
                            JSONObject.put("banner", banner);
                            WriteFile(JSONObject.toString(), JSON, s_update_version);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }).execute("http://app.serafim-tech.com/SerafimPlay/S_Series/banner/banner.zip");
                } else {

                    getApp().S_gamefile();
                    getApp().checkAddedPackageName();

                    Navigation.findNavController(requireView()).navigate(R.id.action_choose_to_main_drawer);
                    mProgressDialogUtil.dismiss();
                    try {
                        JSONObject presetJSONObject = new JSONObject();
                        JSONObject gamefileJSONObject = new JSONObject();
                        JSONObject ManualJSONObject = new JSONObject();
                        JSONObject JSONObject = new JSONObject();
                        if (preset.containsKey(getApp().ipCountryCode)) {
                            presetJSONObject.put(getApp().ipCountryCode, preset.get(getApp().ipCountryCode));
                        } else {
                            presetJSONObject.put("global", preset.get("global"));
                        }
                        if (gamefile.containsKey(getApp().localeCountryCode)) {
                            gamefileJSONObject.put(getApp().localeCountryCode, gamefile.get(getApp().localeCountryCode));
                        } else {
                            gamefileJSONObject.put("global", gamefile.get("global"));
                        }

                        if (Manual.containsKey(getApp().localeCountryCode)) {
                            ManualJSONObject.put(getApp().localeCountryCode, Manual.get(getApp().localeCountryCode));
                        } else {
                            ManualJSONObject.put("global", Manual.get("global"));
                        }
                        JSONObject.put("preset", presetJSONObject);
                        JSONObject.put("gamefile", gamefileJSONObject);
                        JSONObject.put("Manual", ManualJSONObject);
                        JSONObject.put("banner", banner);
                        WriteFile(JSONObject.toString(), JSON, s_update_version);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    void r_update() {
        preset = new HashMap<>();
        gamefile = new HashMap<>();
        Manual = new HashMap<>();
        preset_ftp = new HashMap<>();
        gamefile_ftp = new HashMap<>();
        Manual_ftp = new HashMap<>();

        String file = ReadFile(JSON, r_update_version);
        if (!file.equals("")) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(file);
                if (jsonObject.getJSONObject("preset").has(getApp().ipCountryCode)) {
                    preset.put(getApp().ipCountryCode, jsonObject.getJSONObject("preset").getInt(getApp().ipCountryCode));
                } else {
                    preset.put("global", jsonObject.getJSONObject("preset").getInt("global"));
                }
                if (jsonObject.getJSONObject("gamefile").has(getApp().localeCountryCode)) {
                    gamefile.put(getApp().localeCountryCode, jsonObject.getJSONObject("gamefile").getInt(getApp().localeCountryCode));
                } else {
                    gamefile.put("global", jsonObject.getJSONObject("gamefile").getInt("global"));
                }
                if (jsonObject.getJSONObject("Manual").has(getApp().localeCountryCode)) {
                    Manual.put(getApp().localeCountryCode, jsonObject.getJSONObject("Manual").getInt(getApp().localeCountryCode));
                } else {
                    Manual.put("global", jsonObject.getJSONObject("Manual").getInt("global"));
                }
                if (jsonObject.has("banner")) {
                    banner = jsonObject.getInt("banner");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        new Downloader.DownloadTask().setOnDownloadFinishListener(result -> {
            try {
                JSONObject jsonObjectftp = new JSONObject(result);
                if (jsonObjectftp.getJSONObject("preset").has(getApp().ipCountryCode)) {
                    preset_ftp.put(getApp().ipCountryCode, jsonObjectftp.getJSONObject("preset").getInt(getApp().ipCountryCode));
                } else {
                    preset_ftp.put("global", jsonObjectftp.getJSONObject("preset").getInt("global"));
                }
                if (jsonObjectftp.getJSONObject("gamefile").has(getApp().localeCountryCode)) {
                    gamefile_ftp.put(getApp().localeCountryCode, jsonObjectftp.getJSONObject("gamefile").getInt(getApp().localeCountryCode));
                } else {
                    gamefile_ftp.put("global", jsonObjectftp.getJSONObject("gamefile").getInt("global"));
                }
                if (jsonObjectftp.getJSONObject("Manual").has(getApp().localeCountryCode)) {
                    Manual_ftp.put(getApp().localeCountryCode, jsonObjectftp.getJSONObject("Manual").getInt(getApp().localeCountryCode));
                } else {
                    Manual_ftp.put("global", jsonObjectftp.getJSONObject("Manual").getInt("global"));
                }
                if (jsonObjectftp.has("banner")) {
                    banner_ftp = jsonObjectftp.getInt("banner");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            r_update("preset");
        }).execute("http://app.serafim-tech.com/SerafimPlay/app_info/r_update_version.txt");
    }

    void r_update(String type) {
        switch (type) {
            case "preset":
                if (preset_ftp.containsKey(getApp().ipCountryCode)) {
                    if (!preset.containsKey(getApp().ipCountryCode)) {
                        preset.put(getApp().ipCountryCode, 0);
                    }
                    if (preset.get(getApp().ipCountryCode) < preset_ftp.get(getApp().ipCountryCode)) {
                        new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                            //假如下載的是壓縮檔
                            File zipFile1 = new File(FileDir, "filename");
                            try {
                                unZipFiles2(zipFile1, r_DEFAULT_INFO);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (zipFile1.delete()) {
                                Log.e("zipFile1.delete", "SUCCESS");
                            } else {
                                Log.e("zipFile1.delete", "FAIL");
                            }
                            preset.put(getApp().ipCountryCode, preset_ftp.get(getApp().ipCountryCode));
                            r_update("gamefile");
                        }).execute("http://app.serafim-tech.com/SerafimPlay/R_Series/stick/" + getApp().ipCountryCode + ".zip");
                    } else {
                        r_update("gamefile");
                    }
                } else {
                    if (!preset.containsKey("global")) {
                        preset.put("global", 0);
                    }
                    if (preset.get("global") < preset_ftp.get("global")) {
                        new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                            //假如下載的是壓縮檔
                            File zipFile1 = new File(FileDir, "filename");
                            try {
                                unZipFiles2(zipFile1, r_DEFAULT_INFO);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (zipFile1.delete()) {
                                Log.e("zipFile1.delete", "SUCCESS");
                            } else {
                                Log.e("zipFile1.delete", "FAIL");
                            }
                            preset.put("global", preset_ftp.get("global"));
                            r_update("gamefile");
                        }).execute("http://app.serafim-tech.com/SerafimPlay/R_Series/stick/stick_global.zip");
                    } else {
                        r_update("gamefile");
                    }
                }
                break;
            case "gamefile":
                if (gamefile_ftp.containsKey(getApp().localeCountryCode)) {
                    if (!gamefile.containsKey(getApp().localeCountryCode)) {
                        gamefile.put(getApp().localeCountryCode, 0);
                    }
                    if (gamefile.get(getApp().localeCountryCode) < gamefile_ftp.get(getApp().localeCountryCode)) {
                        new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                            //假如下載的是壓縮檔
                            File zipFile1 = new File(FileDir, "filename");
                            try {
                                unZipFiles2(zipFile1, r_gamefile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (zipFile1.delete()) {
                                Log.e("zipFile1.delete", "SUCCESS");
                            } else {
                                Log.e("zipFile1.delete", "FAIL");
                            }
                            gamefile.put(getApp().localeCountryCode, gamefile_ftp.get(getApp().localeCountryCode));
                            r_update("Manual");
                        }).execute("http://app.serafim-tech.com/SerafimPlay/R_Series/gamefile/" + getApp().localeCountryCode + ".zip");
                    } else {
                        r_update("Manual");
                    }
                } else {
                    if (!gamefile.containsKey("global")) {
                        gamefile.put("global", 0);
                    }
                    if (gamefile.get("global") < gamefile_ftp.get("global")) {
                        new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                            //假如下載的是壓縮檔
                            File zipFile1 = new File(FileDir, "filename");
                            try {
                                unZipFiles2(zipFile1, r_gamefile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (zipFile1.delete()) {
                                Log.e("zipFile1.delete", "SUCCESS");
                            } else {
                                Log.e("zipFile1.delete", "FAIL");
                            }
                            gamefile.put("global", gamefile_ftp.get("global"));
                            r_update("Manual");
                        }).execute("http://app.serafim-tech.com/SerafimPlay/R_Series/gamefile/gamefile_en.zip");
                    } else {
                        r_update("Manual");
                    }
                }
                break;
            case "Manual":
                if (Manual_ftp.containsKey(getApp().localeCountryCode)) {
                    if (!Manual.containsKey(getApp().localeCountryCode)) {
                        Manual.put(getApp().localeCountryCode, 0);
                    }
                    if (Manual.get(getApp().localeCountryCode) < Manual_ftp.get(getApp().localeCountryCode)) {
                        new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                            //假如下載的是壓縮檔
                            File zipFile1 = new File(FileDir, "filename");
                            try {
                                unZipFiles2(zipFile1, r_Manual);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (zipFile1.delete()) {
                                Log.e("zipFile1.delete", "SUCCESS");
                            } else {
                                Log.e("zipFile1.delete", "FAIL");
                            }
                            Manual.put(getApp().localeCountryCode, Manual_ftp.get(getApp().localeCountryCode));
                            r_update("banner");
                        }).execute("http://app.serafim-tech.com/SerafimPlay/R_Series/manual/" + getApp().localeCountryCode + ".zip");
                    } else {
                        r_update("banner");
                    }
                } else {
                    if (!Manual.containsKey("global")) {
                        Manual.put("global", 0);
                    }
                    if (Manual.get("global") < Manual_ftp.get("global")) {
                        new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                            //假如下載的是壓縮檔
                            File zipFile1 = new File(FileDir, "filename");
                            try {
                                unZipFiles2(zipFile1, r_Manual);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (zipFile1.delete()) {
                                Log.e("zipFile1.delete", "SUCCESS");
                            } else {
                                Log.e("zipFile1.delete", "FAIL");
                            }
                            Manual.put("global", Manual_ftp.get("global"));
                            r_update("banner");
                        }).execute("http://app.serafim-tech.com/SerafimPlay/R_Series/manual/manual_en.zip");
                    } else {
                        r_update("banner");
                    }
                }
                break;
            case "banner":
                if (banner_ftp > banner) {
                    new Downloader.DownloadTaskKV().setOnDownloadFinishListener(result -> {
                        //假如下載的是壓縮檔
                        File zipFile1 = new File(FileDir, "filename");
                        try {
                            unZipFiles2(zipFile1, banner_file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (zipFile1.delete()) {
                            Log.e("zipFile1.delete", "SUCCESS");
                        } else {
                            Log.e("zipFile1.delete", "FAIL");
                        }

                        banner = banner_ftp;

                        getApp().R_gamefile();
                        getApp().checkAddedPackageName();

                        Navigation.findNavController(requireView()).navigate(R.id.action_choose_to_main_drawer);
                        mProgressDialogUtil.dismiss();
                        try {
                            JSONObject presetJSONObject = new JSONObject();
                            JSONObject gamefileJSONObject = new JSONObject();
                            JSONObject ManualJSONObject = new JSONObject();
                            JSONObject JSONObject = new JSONObject();
                            if (preset.containsKey(getApp().ipCountryCode)) {
                                presetJSONObject.put(getApp().ipCountryCode, preset.get(getApp().ipCountryCode));
                            } else {
                                presetJSONObject.put("global", preset.get("global"));
                            }
                            if (gamefile.containsKey(getApp().localeCountryCode)) {
                                gamefileJSONObject.put(getApp().localeCountryCode, gamefile.get(getApp().localeCountryCode));
                            } else {
                                gamefileJSONObject.put("global", gamefile.get("global"));
                            }
                            if (Manual.containsKey(getApp().localeCountryCode)) {
                                ManualJSONObject.put(getApp().localeCountryCode, Manual.get(getApp().localeCountryCode));
                            } else {
                                ManualJSONObject.put("global", Manual.get("global"));
                            }
                            JSONObject.put("preset", presetJSONObject);
                            JSONObject.put("gamefile", gamefileJSONObject);
                            JSONObject.put("Manual", ManualJSONObject);
                            JSONObject.put("banner", banner);
                            WriteFile(JSONObject.toString(), JSON, r_update_version);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }).execute("http://app.serafim-tech.com/SerafimPlay/R_Series/banner/banner.zip");
                } else {

                    getApp().R_gamefile();
                    getApp().checkAddedPackageName();

                    Navigation.findNavController(requireView()).navigate(R.id.action_choose_to_main_drawer);
                    mProgressDialogUtil.dismiss();
                    try {
                        JSONObject presetJSONObject = new JSONObject();
                        JSONObject gamefileJSONObject = new JSONObject();
                        JSONObject ManualJSONObject = new JSONObject();
                        JSONObject JSONObject = new JSONObject();
                        if (preset.containsKey(getApp().ipCountryCode)) {
                            presetJSONObject.put(getApp().ipCountryCode, preset.get(getApp().ipCountryCode));
                        } else {
                            presetJSONObject.put("global", preset.get("global"));
                        }
                        if (gamefile.containsKey(getApp().localeCountryCode)) {
                            gamefileJSONObject.put(getApp().localeCountryCode, gamefile.get(getApp().localeCountryCode));
                        } else {
                            gamefileJSONObject.put("global", gamefile.get("global"));
                        }
                        if (Manual.containsKey(getApp().localeCountryCode)) {
                            ManualJSONObject.put(getApp().localeCountryCode, Manual.get(getApp().localeCountryCode));
                        } else {
                            ManualJSONObject.put("global", Manual.get("global"));
                        }
                        JSONObject.put("preset", presetJSONObject);
                        JSONObject.put("gamefile", gamefileJSONObject);
                        JSONObject.put("Manual", ManualJSONObject);
                        JSONObject.put("banner", banner);
                        WriteFile(JSONObject.toString(), JSON, r_update_version);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
    //</editor-fold>
}
