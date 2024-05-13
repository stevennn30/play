package com.serafimtech.serafimplay.file;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static com.serafimtech.serafimplay.file.value.InternalFileName.FileDir;

public class Downloader {

    public interface OnDownloadFinishListener {
        void onFinish(String result);
    }

    public static class DownloadTask extends AsyncTask<String, String, String> {
        OnDownloadFinishListener mOnDownloadFinishListener;

//        public DownloadTask(Downloader.OnDownloadFinishListener OnDownloadFinishListener){
//            mOnDownloadFinishListener = OnDownloadFinishListener;
//        }

        public DownloadTask setOnDownloadFinishListener(Downloader.OnDownloadFinishListener OnDownloadFinishListener) {
            this.mOnDownloadFinishListener = OnDownloadFinishListener;
            return this;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                Log.d("URL", "" + params[0]);
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            onFinish(result);
//            try {
//                JSONObject jsonObject = new JSONObject(result);
//                Log.d("app_version",String.valueOf(jsonObject.get("app_version")));
//                Log.d("app_update_flag",String.valueOf(jsonObject.get("app_update_flag")));
//                Log.d("app_download_link",String.valueOf(jsonObject.get("app_download_link")));
//                Log.d("banner_ver",String.valueOf(jsonObject.get("banner_ver")));
//                Log.d("banners",String.valueOf(jsonObject.get("banners")));
//                Log.d("jp",String.valueOf(jsonObject.get("jp")));
//                Log.d("official_website_link",new JSONObject(String.valueOf(jsonObject.get("jp"))).get("official_website_link")+"");
//                Log.d("tw",String.valueOf(jsonObject.get("tw")));
//                Log.d("cn",String.valueOf(jsonObject.get("cn")));
//                Log.d("us",String.valueOf(jsonObject.get("us")));
//                Log.d("kr",String.valueOf(jsonObject.get("kr")));
//            }catch (JSONException e){
//                e.printStackTrace();
//            }
        }

        protected void onFinish(String result) {
            if (mOnDownloadFinishListener != null) {
                mOnDownloadFinishListener.onFinish(result);
            }
        }
    }

//    public static class SupportCountryListDownloadTask extends DownloadTask {
//        @Override
//        protected void onFinish(String result) {
//            try {
//                JSONArray jsonArray = new JSONArray(result);
//                Log.d("Support Country", jsonArray.toString());
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    Log.e("Support Country", jsonArray.getString(i));
//                    if (getApp().ipCountryCode.equals(jsonArray.getString(i))) {
//                        getApp().supportFlag = true;
//                        break;
//                    }
//                }
//                SupportGameListDownloadTask supportGameListDownloadTask = new SupportGameListDownloadTask();
//                supportGameListDownloadTask.mOnDownloadFinishListener = this.mOnDownloadFinishListener;
//                if (getApp().supportFlag) {
//                    supportGameListDownloadTask.execute("http://app.serafim-tech.com/SerafimPlay/S_Series/stick_" + getApp().ipCountryCode.toLowerCase() + "/SupportGameList.txt");
//                } else {
//                    supportGameListDownloadTask.execute("http://app.serafim-tech.com/SerafimPlay/S_Series/stick_global/SupportGameList.txt");
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            } catch (NullPointerException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    public static class SupportGameListDownloadTask extends DownloadTask {
//
//        @SuppressWarnings("unchecked")
//        @Override
//        protected void onFinish(String result) {
//            //預設方案支援清單建立
//            try {
//                JSONObject jsonObject = new JSONObject(result);
//                for (int i = 0; i < jsonObject.getJSONArray("game_list").length(); i++) {
//                    StickGameValue.SupportGameList.add(jsonObject.getJSONArray("game_list").getString(i));
//                }
//                Log.d("gameList", StickGameValue.SupportGameList + "");
//                saveObject(DEFAULT_INFO, InternalFileName.SUPPORTED_GAME_LIST, new Object[]{StickGameValue.SupportGameList});
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            if (StickGameValue.SupportGameList.size() != 0) {
//                Log.d("Default Game Setting", getFilesAllName(DEFAULT_INFO).toString());
//                for (String gamePackageName : StickGameValue.SupportGameList) {
//                    if (!(getFilesAllName(DEFAULT_INFO).contains(gamePackageName))) {
//                        FileDownloadTask fileDownloadTask = new FileDownloadTask();
//                        fileDownloadTask.setLocalDir(DEFAULT_INFO);
//                        fileDownloadTask.setFileName(gamePackageName);
//                        if (getApp().supportFlag) {
//                            fileDownloadTask.execute("http://app.serafim-tech.com//SerafimPlay/S_Series/stick_" + getApp().ipCountryCode.toLowerCase() + "/" + gamePackageName + ".txt");
//                        } else {
//                            fileDownloadTask.execute("http://app.serafim-tech.com//SerafimPlay/S_Series/stick_global/" + gamePackageName + ".txt");
//                        }
//                    }
//                }
//            }
//            super.onFinish(result);
//        }
//    }

    public static class BasicImageDownloader {

        private final BasicImageDownloader.OnImageLoaderListener mImageLoaderListener;
        private final Set<String> mUrlsInProgress = new HashSet<>();

        public BasicImageDownloader(@NonNull BasicImageDownloader.OnImageLoaderListener listener) {
            this.mImageLoaderListener = listener;
        }

        public interface OnImageLoaderListener {
            void onError(BasicImageDownloader.ImageError error);

            void onProgressChange(int percent);

            void onComplete(Bitmap result);
        }

        @SuppressLint("StaticFieldLeak")
        public void download(@NonNull final String imageUrl, final boolean displayProgress) {
            if (mUrlsInProgress.contains(imageUrl)) {
                return;
            }

            new AsyncTask<Void, Integer, Bitmap>() {

                private BasicImageDownloader.ImageError error;

                protected void onPreExecute() {
                    mUrlsInProgress.add(imageUrl);
                }

                protected void onCancelled() {
                    mUrlsInProgress.remove(imageUrl);
                    mImageLoaderListener.onError(error);
                }

                protected void onProgressUpdate(Integer... values) {
                    mImageLoaderListener.onProgressChange(values[0]);
                }

                @SuppressLint("StaticFieldLeak")
                @Override
                protected Bitmap doInBackground(Void... params) {
                    Bitmap bitmap = null;
                    HttpURLConnection connection = null;
                    InputStream is = null;
                    ByteArrayOutputStream out = null;
                    try {
                        connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                        if (displayProgress) {
                            connection.connect();
                            final int length = connection.getContentLength();
                            if (length <= 0) {
                                error = new BasicImageDownloader.ImageError("Invalid content length. The URL is probably not pointing to a file").setErrorCode();
                                this.cancel(true);
                            }
                            is = new BufferedInputStream(connection.getInputStream(), 8192);
                            out = new ByteArrayOutputStream();
                            byte bytes[] = new byte[8192];
                            int count;
                            long read = 0;
                            while ((count = is.read(bytes)) != -1) {
                                read += count;
                                out.write(bytes, 0, count);
                                publishProgress((int) ((read * 100) / length));
                            }
                            bitmap = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
                        } else {
                            is = connection.getInputStream();
                            bitmap = BitmapFactory.decodeStream(is);
                        }
                    } catch (Throwable e) {
                        if (!this.isCancelled()) {
                            error = new BasicImageDownloader.ImageError(e).setErrorCode();
                            this.cancel(true);
                        }
                    } finally {
                        try {
                            if (connection != null)
                                connection.disconnect();
                            if (out != null) {
                                out.flush();
                                out.close();
                            }
                            if (is != null)
                                is.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return bitmap;
                }

                protected void onPostExecute(Bitmap result) {
                    if (result == null) {
                        mImageLoaderListener.onError(new BasicImageDownloader.ImageError("downloaded file could not be decoded as bitmap").setErrorCode());
                    } else {
                        mImageLoaderListener.onComplete(result);
                    }
                    mUrlsInProgress.remove(imageUrl);
                    System.gc();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        public static final class ImageError extends Throwable {

            public ImageError(@NonNull String message) {
                super(message);
            }

            public ImageError(@NonNull Throwable error) {
                super(error.getMessage(), error.getCause());
                this.setStackTrace(error.getStackTrace());
            }

            public ImageError setErrorCode() {
                return this;
            }
        }
    }

    public static class DownloadTaskKV extends AsyncTask<String, String, String> {
        OnDownloadFinishListener mOnDownloadFinishListener;

        public DownloadTaskKV setOnDownloadFinishListener(Downloader.OnDownloadFinishListener OnDownloadFinishListener) {
            this.mOnDownloadFinishListener = OnDownloadFinishListener;
            return this;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            try {
                String filename = "filename";
                HttpURLConnection connection;
                Log.e("URL", "" + params[0]);
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in1 = connection.getInputStream();
                FileOutputStream f1 = new FileOutputStream(new File(FileDir, filename));
                byte[] buffer1 = new byte[1024];
                int len1;
                while ((len1 = in1.read(buffer1)) > 0) {
                    f1.write(buffer1, 0, len1);
                }
                f1.close();
//                //假如下載的是壓縮檔
//                File zipFile1 = new File(FileDir, filename);
//                unZipFiles2(zipFile1, "stick");
//                if (zipFile1.delete()) {
//                    Log.e("zipFile1.delete", "SUCCESS");
//                } else {
//                    Log.e("zipFile1.delete", "FAIL");
//                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e("Downloadfail", "------");
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            onFinish(result);
//            try {
//                JSONObject jsonObject = new JSONObject(result);
//                Log.d("app_version",String.valueOf(jsonObject.get("app_version")));
//                Log.d("app_update_flag",String.valueOf(jsonObject.get("app_update_flag")));
//                Log.d("app_download_link",String.valueOf(jsonObject.get("app_download_link")));
//                Log.d("banner_ver",String.valueOf(jsonObject.get("banner_ver")));
//                Log.d("banners",String.valueOf(jsonObject.get("banners")));
//                Log.d("jp",String.valueOf(jsonObject.get("jp")));
//                Log.d("official_website_link",new JSONObject(String.valueOf(jsonObject.get("jp"))).get("official_website_link")+"");
//                Log.d("tw",String.valueOf(jsonObject.get("tw")));
//                Log.d("cn",String.valueOf(jsonObject.get("cn")));
//                Log.d("us",String.valueOf(jsonObject.get("us")));
//                Log.d("kr",String.valueOf(jsonObject.get("kr")));
//            }catch (JSONException e){
//                e.printStackTrace();
//            }
        }

        protected void onFinish(String result) {
            if (mOnDownloadFinishListener != null) {
                mOnDownloadFinishListener.onFinish(result);
            }
        }
    }

    public static class checkIP extends AsyncTask<String, String, String> {
        OnDownloadFinishListener mOnDownloadFinishListener;
//    String IP = "";
//    Downloader.OnDownloadFinishListener listener;

        /**
         * Before starting background thread Show Progress Bar Dialog
         */
        public checkIP setOnDownloadFinishListener(Downloader.OnDownloadFinishListener OnDownloadFinishListener) {
            this.mOnDownloadFinishListener = OnDownloadFinishListener;
            return this;
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
                String IP = "";
                while ((len2 = reader2.readLine()) != null) {
                    IP = len2.split(";")[1];
                    Log.e("IP country code", IP);
                }
                return IP;
            } catch (Exception e) {
                Log.e("ChooseProductIPfail", "------");
                e.printStackTrace();
            }
            return "";
        }

        protected void onProgressUpdate(String... progress) {
        }

        @Override
        protected void onPostExecute(String result) {
            onFinish(result);
        }

        protected void onFinish(String result) {
            if (mOnDownloadFinishListener != null) {
                mOnDownloadFinishListener.onFinish(result);
            }
        }
    }
}
