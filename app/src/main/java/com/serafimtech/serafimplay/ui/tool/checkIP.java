package com.serafimtech.serafimplay.ui.tool;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.serafimtech.serafimplay.App.getApp;

public class checkIP extends AsyncTask<String, String, String> {
    String IP = getApp().localeCountryCode;
//    String IP = "";
//    Downloader.OnDownloadFinishListener listener;

    /**
     * Before starting background thread Show Progress Bar Dialog
     */
    public checkIP() {
//        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected String doInBackground(String... sUrl) {
        try {
            URL u1 = new URL("http://ipv4bot.whatismyipaddress.com/");
            HttpURLConnection c1 = (HttpURLConnection) u1.openConnection();
            c1.setRequestMethod("GET");
            c1.setDoOutput(true);
            c1.connect();
            InputStream in1 = c1.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in1));
            String len1;
            String buffururl = "";
            while ((len1 = reader.readLine()) != null) {
                Log.e("IP", len1);
                buffururl = len1;
            }
            URL u2 = new URL("https://ip2c.org/" + buffururl);
            HttpURLConnection c2 = (HttpURLConnection) u2.openConnection();
            c2.setRequestMethod("GET");
            c2.setDoOutput(true);
            c2.connect();
            InputStream in2 = c2.getInputStream();
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(in2));
            String len2;
            while ((len2 = reader2.readLine()) != null) {
                IP = len2.split(";")[1];
                Log.e("IP country code", IP);
            }
        } catch (Exception e) {
            Log.e("ChooseProductIPfail", "------");
            e.printStackTrace();
        }
        return IP;
    }

    protected void onProgressUpdate(String... progress) {
    }

    @Override
    protected void onPostExecute(String result) {
//        listener.onFinish(result);
        getApp().ipCountryCode = result;
    }
}

