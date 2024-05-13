package com.serafimtech.serafimplay;

import static com.serafimtech.serafimplay.file.DataReadAndWrite.ReadFile;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.readObject;
import static com.serafimtech.serafimplay.file.value.InternalFileName.ADDED_GAME_LIST;
import static com.serafimtech.serafimplay.file.value.InternalFileName.DEFAULT_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.DEVICE_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.r_DEFAULT_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.r_gamefile;
import static com.serafimtech.serafimplay.file.value.InternalFileName.s_gamefile;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.util.Log;

import com.serafimtech.serafimplay.tool.WifiP2pHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("ALL")
public class App extends Application {
    public final String TAG = "App";
    public static App app;
    public HashMap<String, String> bindGameHashMap;
    public WifiP2pHandler P2pHandler;

    public final String APP_ID = "wxf2b22ba876350199";
    public String androidID = "";

    public boolean notificationFlag = false;

    public boolean supportFlag = false;
    public boolean betaFlag;

    public enum ProductName {
        R_Series,
        S_Series,
    }

    public static ProductName productName;

    public void onCreate() {
        super.onCreate();
        app = this;
        androidID = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.d("Android ID", androidID);
        bindGameHashMap = new HashMap<>();
        P2pHandler = new WifiP2pHandler(this);
//        checkip();

//        checkaddedPackageName();
    }

    public static App getApp() {
        if (app == null) {
            synchronized (App.class) {
                app = new App();
            }
        }
        return app;
    }

    public boolean isConnected() {
        return ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    //TODO getDefaultDisplay
    //<editor-fold desc="<WindowWidth&Height>">
    public int windowWidth;
    public int windowHeight;
    public float windowDensity;
    public float statusBarHeight;
    public float navigationBarHeight;
    //</editor-fold>

    //<editor-fold desc="<Checkip>">
    public String localeCountryCode;
    public String ipCountryCode;

//    private void checkip() {
//        Locale locale;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            locale = getResources().getConfiguration().getLocales().get(0);
//        } else {
//            locale = getResources().getConfiguration().locale;
//        }
//        localeCountryCode = locale.getCountry();
//        ipCountryCode = localeCountryCode;
//        if (isConnected()) {
////            Downloader.OnDownloadFinishListener listener = new Downloader.OnDownloadFinishListener() {
////                @Override
////                public void onFinish(String result) {
////                    ipCountryCode = result;
////                }
////            };
//            checkIP mcheckIP = new checkIP();
//            mcheckIP.execute();
//        } else {
//            ipCountryCode = localeCountryCode;
//        }
//    }
    //</editor-fold>

    //<editor-fold desc="<PackageName>">
    public ArrayList<String> gameNameInDevice = new ArrayList<>();
    public ArrayList<String> packageNameInDevice = new ArrayList<>();
    public ArrayList<String> addedGameNameInDevice = new ArrayList<>();
    public ArrayList<String> addedPackageNameInDevice = new ArrayList<>();
    public ArrayList<String> installedGameList = new ArrayList<>();

    public void checkAddedPackageName() {
        new Thread(() -> {
            ArrayList<Integer> checkAddedGameList = new ArrayList<>();
            Object[] obj = readObject(DEVICE_INFO, ADDED_GAME_LIST);
            if (obj != null) {
                addedPackageNameInDevice = (ArrayList<String>) obj[0];
                addedGameNameInDevice = (ArrayList<String>) obj[1];

                for (int i = 0; i < addedPackageNameInDevice.size(); i++) {
                    checkAddedGameList.add(i);
                }
            }

            packageNameInDevice = new ArrayList<>();
            PackageManager pm;
            pm = getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(0);

            for (PackageInfo info : packages) {
                String packageName = info.packageName;
                int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
                try {
                    //檢查包名是否符合條件
                    if ((pm.getApplicationInfo(packageName, 0).flags & mask) == 0) {
                        //檢查包名是否在支援清單中，是的話加入到手機已安裝支援遊戲清單中
                        if (gamePackageNames.containsValue(packageName)) {
                            installedGameList.add(packageName);
                        }
                        //檢查包名是否在手機加入遊戲清單中，是的話從檢查清單中清除
                        else if (addedPackageNameInDevice.contains(packageName)) {
                            checkAddedGameList.remove((Object) addedPackageNameInDevice.indexOf(packageName));
                        }
                        //若都不是以上條件，將應用加入顯示清單中
                        else {
                            gameNameInDevice.add((String) pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)));
                            packageNameInDevice.add(packageName);
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //</editor-fold>

    //<editor-fold desc="<gamefile>">
    public int[] featuredGameSequence;
    public int[] bikeFeaturedGameSequence;
    public int[] racingFeaturedGameSequence;

    public HashMap<Integer, String> gamePackageNames;
    public HashMap<Integer, String> gameNames;
    public HashMap<Integer, String> gameDescriptions;
    public HashMap<Integer, String> gameVendors;
    public HashMap<Integer, String> gameCategories;

    public void S_gamefile() {
        featuredGameSequence = new int[0];
        gamePackageNames = new HashMap<>();
        gameNames = new HashMap<>();
        gameDescriptions = new HashMap<>();
        gameVendors = new HashMap<>();
        gameCategories = new HashMap<>();

        String file = ReadFile(s_gamefile, "priority.txt");
        if (!file.equals("")) {
            try {
                JSONArray jsonArray = new JSONArray(file);
                featuredGameSequence = new int[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    featuredGameSequence[i] = jsonArray.getInt(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        file = ReadFile(DEFAULT_INFO, "SupportGameList");
        if (!file.equals("")) {
            try {
                JSONObject jsonObject;
                jsonObject = new JSONObject(file);
                Iterator<String> sIterator = jsonObject.keys();
                while (sIterator.hasNext()) {
                    String key = sIterator.next();
                    String value = jsonObject.getString(key);
                    gamePackageNames.put(Integer.valueOf(key), value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (Integer key : gamePackageNames.keySet()) {
            file = ReadFile(s_gamefile, String.valueOf(key) + ".txt");
            if (!file.equals("")) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(file);
                    if (jsonObject.has("Name")) {
                        gameNames.put(key, jsonObject.getString("Name"));
                    }
                    if (jsonObject.has("Descriptions")) {
                        gameDescriptions.put(key, jsonObject.getString("Descriptions"));
                    }
                    if (jsonObject.has("Vendors")) {
                        gameVendors.put(key, jsonObject.getString("Vendors"));
                    }
                    if (jsonObject.has("gameCategories")) {
                        gameCategories.put(key, jsonObject.getString("gameCategories"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void R_gamefile() {
        featuredGameSequence = new int[0];
        bikeFeaturedGameSequence = new int[0];
        racingFeaturedGameSequence = new int[0];
        gamePackageNames = new HashMap<>();
        gameNames = new HashMap<>();
        gameDescriptions = new HashMap<>();
        gameVendors = new HashMap<>();
        gameCategories = new HashMap<>();

        String file = ReadFile(r_gamefile, "priority.txt");
        if (!file.equals("")) {
            try {
                JSONObject jsonObject;
                jsonObject = new JSONObject(file);

                featuredGameSequence = new int[jsonObject.getJSONArray("featuredGameSequencess").length()];
                for (int i = 0; i < jsonObject.getJSONArray("featuredGameSequencess").length(); i++) {
                    featuredGameSequence[i] = jsonObject.getJSONArray("featuredGameSequencess").getInt(i);
                }

                bikeFeaturedGameSequence = new int[jsonObject.getJSONArray("bikeFeaturedGameSequencess").length()];
                for (int i = 0; i < jsonObject.getJSONArray("bikeFeaturedGameSequencess").length(); i++) {
                    bikeFeaturedGameSequence[i] = jsonObject.getJSONArray("bikeFeaturedGameSequencess").getInt(i);
                }

                racingFeaturedGameSequence = new int[jsonObject.getJSONArray("racingFeaturedGameSequencess").length()];
                for (int i = 0; i < jsonObject.getJSONArray("racingFeaturedGameSequencess").length(); i++) {
                    racingFeaturedGameSequence[i] = jsonObject.getJSONArray("racingFeaturedGameSequencess").getInt(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        file = ReadFile(r_DEFAULT_INFO, "SupportGameList");
        if (!file.equals("")) {
            try {
                JSONObject jsonObject;
                jsonObject = new JSONObject(file);
                Iterator<String> sIterator = jsonObject.keys();
                while (sIterator.hasNext()) {
                    String key = sIterator.next();
                    String value = jsonObject.getString(key);
                    gamePackageNames.put(Integer.valueOf(key), value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (int key : gamePackageNames.keySet()) {
            file = ReadFile(r_gamefile, key + ".txt");
            if (!file.equals("")) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(file);
                    if (jsonObject.has("Name")) {
                        gameNames.put(key, jsonObject.getString("Name"));
                    }
                    if (jsonObject.has("Descriptions")) {
                        gameDescriptions.put(key, jsonObject.getString("Descriptions"));
                    }
                    if (jsonObject.has("Vendors")) {
                        gameVendors.put(key, jsonObject.getString("Vendors"));
                    }
                    if (jsonObject.has("gameCategories")) {
                        gameCategories.put(key, jsonObject.getString("gameCategories"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //</editor-fold>
}
