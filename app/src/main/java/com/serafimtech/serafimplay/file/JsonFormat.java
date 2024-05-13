package com.serafimtech.serafimplay.file;

import android.util.Log;

import com.serafimtech.serafimplay.tool.SerializableSparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class JsonFormat {

    public static JSONObject PresetObjectToJson(Object[] obj) {
        try {
            int i;
            JSONObject presetJSONObject = new JSONObject();
            JSONObject locationJSONObject = new JSONObject();
            JSONObject functionJSONObject = new JSONObject();
            JSONObject combinationJSONObject = new JSONObject();
            JSONObject macroKeysJSONObject = new JSONObject();
            presetJSONObject.put("orientation", (byte) obj[0]);
            for (i = 0; i < ((SerializableSparseArray<float[]>) obj[1]).size(); i++) {
                JSONArray locationJSONArray = new JSONArray();
                for (float f : (float[]) (((SerializableSparseArray<float[]>) obj[1]).valueAt(i))) {
                    locationJSONArray.put(f);
                }
                locationJSONObject.put(String.valueOf(((SerializableSparseArray<float[]>) obj[1]).keyAt(i)), locationJSONArray);
            }
            presetJSONObject.put("location", locationJSONObject);
            for (i = 0; i < ((SerializableSparseArray<byte[]>) obj[2]).size(); i++) {
                JSONArray functionJSONArray = new JSONArray();
                for (byte b : (byte[]) ((SerializableSparseArray<byte[]>) obj[2]).valueAt(i)) {
                    functionJSONArray.put(b);
                }
                functionJSONObject.put(String.valueOf(((SerializableSparseArray<byte[]>) obj[2]).keyAt(i)), functionJSONArray);
            }
            presetJSONObject.put("function", functionJSONObject);
            presetJSONObject.put("combination_main_key", (int) obj[3]);
            for (i = 0; i < ((SerializableSparseArray<byte[]>) obj[4]).size(); i++) {
                JSONArray combinationJSONArray = new JSONArray();
                for (float f : (float[]) ((SerializableSparseArray<float[]>) obj[4]).valueAt(i)) {
                    combinationJSONArray.put(f);
                }
                combinationJSONObject.put(String.valueOf(((SerializableSparseArray<byte[]>) obj[4]).keyAt(i)), combinationJSONArray);
            }
            presetJSONObject.put("combination_key", combinationJSONObject);
            if (obj.length == 6) {
                Log.e("obj.length == 6", "obj.length == 6");
                for (i = 0; i < ((SerializableSparseArray<String>) obj[5]).size(); i++) {
                    macroKeysJSONObject.put(String.valueOf(((SerializableSparseArray<String>) obj[5]).keyAt(i)), ((SerializableSparseArray<String>) obj[5]).valueAt(i));
                }
            } else {
                Log.e("obj.length != 6", "obj.length != 6");
            }
            presetJSONObject.put("macro_keys", macroKeysJSONObject);
            return presetJSONObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object[] PresetJsonToObject(String data) {
        try {
            byte phoneOrientation = 0x01;
            SerializableSparseArray<float[]> pData = new SerializableSparseArray<>();
            SerializableSparseArray<byte[]> fData = new SerializableSparseArray<>();
            SerializableSparseArray<float[]> cData = new SerializableSparseArray<>();
            SerializableSparseArray<String> macroKeys = new SerializableSparseArray<>();
            int combinationKey = 0;
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has("orientation")) {
                phoneOrientation = (byte) jsonObject.getInt("orientation");
                Log.d("orientation", jsonObject.getString("orientation"));
            }
            if (jsonObject.has("location")) {
                for (Iterator<String> it = jsonObject.getJSONObject("location").keys(); it.hasNext(); ) {
                    String i = it.next();
                    pData.put(Integer.parseInt(i),
                            new float[]{(float) jsonObject.getJSONObject("location").getJSONArray(i).getDouble(0),
                                    (float) jsonObject.getJSONObject("location").getJSONArray(i).getDouble(1)});
                    Log.d("location", "key:" + i + ",value:" + jsonObject.getJSONObject("location").getJSONArray(i));
                }
            }
            if (jsonObject.has("function")) {
                for (Iterator<String> it = jsonObject.getJSONObject("function").keys(); it.hasNext(); ) {
                    String i = it.next();
                    StringBuilder str = new StringBuilder();
                    str.append("0x");
                    int length = jsonObject.getJSONObject("function").getJSONArray(i).length();
                    byte[] b = new byte[length];
                    for (int j = 0; j < length; j++) {
                        b[j] = (byte) jsonObject.getJSONObject("function").getJSONArray(i).getInt(j);
                        str.append(String.format("%02X", b[j]));
                    }
                    fData.put(Integer.parseInt(i), b);
                    Log.d("function", "key:" + i + ",value:" + str);
                }
            }
            if (jsonObject.has("combination_main_key")) {
                combinationKey = jsonObject.getInt("combination_main_key");
                Log.d("combination_main_key", jsonObject.getString("combination_main_key"));
            }
            if (jsonObject.has("combination_key")) {
                for (Iterator<String> it = jsonObject.getJSONObject("combination_key").keys(); it.hasNext(); ) {
                    String i = it.next();
                    cData.put(Integer.parseInt(i),
                            new float[]{(float) jsonObject.getJSONObject("combination_key").getJSONArray(i).getDouble(0),
                                    (float) jsonObject.getJSONObject("combination_key").getJSONArray(i).getDouble(1)});
                    Log.d("combination_key", "key:" + i + ",value:" + jsonObject.getJSONObject("combination_key").getJSONArray(i));
                }
            }
            if (jsonObject.has("macro_keys")) {
                for (Iterator<String> it = jsonObject.getJSONObject("macro_keys").keys(); it.hasNext(); ) {
                    String i = it.next();
                    macroKeys.put(Integer.parseInt(i), jsonObject.getJSONObject("macro_keys").getString(i));
                    Log.d("macro_keys", "key:" + i + ",value:" + jsonObject.getJSONObject("macro_keys").getString(i));
                }
            }
            return new Object[]{phoneOrientation, pData, fData, combinationKey, cData, macroKeys};
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject MacroPresetObjectToJson(Object[] obj) {
        try {
            JSONObject macroJSONObject = new JSONObject();
            if (obj.length != 2) {
                JSONArray btnListJSONArray = new JSONArray((ArrayList<Integer>) obj[0]);
                JSONArray btnIntervalTimeJSONArray = new JSONArray((ArrayList<Integer>) obj[1]);
                JSONArray btnPressedTimeJSONArray = new JSONArray((ArrayList<Integer>) obj[2]);
                JSONArray btnDataJSONArray = new JSONArray((ArrayList<byte[]>) obj[3]);
                JSONArray btnHashMapJSONArray = new JSONArray((ArrayList<ArrayList<Integer>>) obj[4]);
                macroJSONObject.put("btn_list", btnListJSONArray);
                macroJSONObject.put("btn_interval_time", btnIntervalTimeJSONArray);
                macroJSONObject.put("btn_pressed_time", btnPressedTimeJSONArray);
                macroJSONObject.put("btn_data", btnDataJSONArray);
                macroJSONObject.put("btn_hash_map", btnHashMapJSONArray);
            } else {
                JSONArray macroDataJSONArray = new JSONArray((ArrayList<byte[]>) obj[1]);
                macroJSONObject.put("duration", obj[0]);
//            for(byte[] b:(ArrayList<byte[]>)obj[1]){
//                JSONArray byteJSONArray = new JSONArray();
//                for (byte b2:b){
//                    byteJSONArray.put((b2 & 0xFF));
//                }
//                macroDataJSONArray.put(byteJSONArray);
//            }
                macroJSONObject.put("data", macroDataJSONArray);
            }
            return macroJSONObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object[] MacroPresetJsonToObject(String data) {
        try {
            ArrayList<Integer> btnList = new ArrayList<>();
            ArrayList<Integer> btnIntervalTime = new ArrayList<>();
            ArrayList<Integer> btnPressedTime = new ArrayList<>();
            ArrayList<byte[]> btnDataJSONArray = new ArrayList<>();
            ArrayList<ArrayList<Integer>> btnHashMap = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.length() != 2) {
                if (jsonObject.has("btn_list")) {
                    for (int i = 0; i < jsonObject.getJSONArray("btn_list").length(); i++) {
                        btnList.add(jsonObject.getJSONArray("btn_list").getInt(i));
                    }
                    Log.d("btn_list", btnList + "");
                }
                if (jsonObject.has("btn_interval_time")) {
                    for (int i = 0; i < jsonObject.getJSONArray("btn_interval_time").length(); i++) {
                        btnIntervalTime.add(jsonObject.getJSONArray("btn_interval_time").getInt(i));
                    }
                    Log.d("btn_interval_time", btnIntervalTime + "");
                }
                if (jsonObject.has("btn_pressed_time")) {
                    for (int i = 0; i < jsonObject.getJSONArray("btn_pressed_time").length(); i++) {
                        btnPressedTime.add(jsonObject.getJSONArray("btn_pressed_time").getInt(i));
                    }
                    Log.d("btn_pressed_time", btnPressedTime + "");
                }
                if (jsonObject.has("btn_data")) {
                    for (int i = 0; i < jsonObject.getJSONArray("btn_data").length(); i++) {
                        StringBuilder str = new StringBuilder();
                        str.append("0x");
                        byte[] b = new byte[jsonObject.getJSONArray("btn_data").getJSONArray(i).length()];
                        for (int i2 = 0; i2 < b.length; i2++) {
                            b[i2] = (byte) jsonObject.getJSONArray("btn_data").getJSONArray(i).getInt(i2);
                            str.append(String.format("%02X", b[i2]));
                        }
                        btnDataJSONArray.add(b);
                        Log.d("btn_data", str.toString());
                    }
                }
                if (jsonObject.has("btn_hash_map")) {
                    for (int i = 0; i < jsonObject.getJSONArray("btn_hash_map").length(); i++) {
                        ArrayList<Integer> arrayList = new ArrayList<>();
                        for (int j = 0; j < jsonObject.getJSONArray("btn_hash_map").getJSONArray(i).length(); j++) {
                            arrayList.add(jsonObject.getJSONArray("btn_hash_map").getJSONArray(i).getInt(j));
                        }
                        btnHashMap.add(arrayList);
                    }
                    Log.d("btn_hash_map", btnHashMap.toString());
                }
                return new Object[]{btnList, btnIntervalTime, btnPressedTime, btnDataJSONArray, btnHashMap};
            } else {
                int recordDuration = 0;
                ArrayList<byte[]> recordNotificationData = new ArrayList<>();
                if (jsonObject.has("duration")) {
                    recordDuration = jsonObject.getInt("duration");
                    Log.d("recordDuration", jsonObject.getInt("duration") + "");
                }
                if (jsonObject.has("data")) {
                    for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
                        StringBuilder str = new StringBuilder();
                        str.append("0x");
                        byte[] b = new byte[jsonObject.getJSONArray("data").getJSONArray(i).length()];
                        for (int i2 = 0; i2 < b.length; i2++) {
                            b[i2] = (byte) jsonObject.getJSONArray("data").getJSONArray(i).getInt(i2);
                            str.append(String.format("%02X", b[i2]));
                        }
                        recordNotificationData.add(b);
                        Log.d("recordData", str.toString());
                    }
                }
                return new Object[]{recordDuration, recordNotificationData};
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
