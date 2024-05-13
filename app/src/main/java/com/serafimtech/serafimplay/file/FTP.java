package com.serafimtech.serafimplay.file;

import android.content.Context;
import android.util.Log;

import com.serafimtech.serafimplay.App;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.ReadFile;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.WriteFile;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.deleteRecursive;
import static com.serafimtech.serafimplay.file.value.InternalFileName.CUSTOM_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.CUSTOM_INFO_BIND_GAME;
import static com.serafimtech.serafimplay.file.value.InternalFileName.CUSTOM_MACRO_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.DEVICE_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.FTP_ACTION;
import static com.serafimtech.serafimplay.file.value.InternalFileName.FTP_STICK_SHARE_PATH;
import static com.serafimtech.serafimplay.file.value.InternalFileName.FTP_TEST_PATH;
import static com.serafimtech.serafimplay.file.value.InternalFileName.FTP_TEST_SHARE_PATH;
import static com.serafimtech.serafimplay.file.value.InternalFileName.FTP_USER_STICK_DATA_PATH;

public class FTP {
    //<editor-fold desc="Variable">
    public String TAG = "FTP";

    public static final String FTP_CONNECT_SUCCESS = "ftp連線成功";
    public static final String FTP_CONNECT_FAIL = "ftp連線失敗";
    public static final String FTP_DISCONNECT_SUCCESS = "ftp斷開連線";

    public static final String FTP_FILE_NOT_EXISTS = "ftp上檔案不存在";

    public static final String FTP_UPLOAD_SUCCESS = "ftp檔案上傳成功";
    public static final String FTP_UPLOAD_FAIL = "ftp檔案上傳失敗";
    public static final String FTP_UPLOAD_LOADING = "ftp檔案正在上傳";

    public static final String FTP_DOWN_SUCCESS = "ftp檔案下載成功";
    public static final String FTP_DOWN_FAIL = "ftp檔案下載失敗";

    public static final String FTP_DELETE_SUCCESS = "ftp檔案刪除成功";
    public static final String FTP_DELETE_FAIL = "ftp檔案刪除失敗";

    public static final String FTP_FINISH = "ftp任務結束";

    public Timer ftpTimer = null;
    public ArrayList<Object[]> jobToDo;
    public ArrayList<JSONObject> actionJsonObjectArray;
    public boolean jobFlag = false;
    public String dataPath;

    /**
     * 伺服器名.
     */
    final String hostName;
    /**
     * 埠號
     */
    final int serverPort;
    /**
     * 使用者名稱.
     */
    final String userName;
    /**
     * 密碼.
     */
    final String password;
    /**
     * FTP連線.
     */
    final FTPClient ftpClient;

    static FTP ftp;

    public enum JobAction {
        UploadDir,
        UploadFile,
        DownloadDir,
        DownloadFile,
        DeleteFile,
    }

    public enum Action {
        Default,
        Init,
        Remove,
    }
    //</editor-fold>

    //<editor-fold desc="Instance">
    public static FTP getInstance() {
        if (ftp == null) {
            synchronized (App.class) {
                ftp = new FTP();
            }
        }
        return ftp;
    }
    //</editor-fold>

    //<editor-fold desc="Initialize">
    public FTP() {
        this.hostName = "160.251.17.13";
        this.serverPort = 21;
        this.userName = "app";
        this.password = "RdnmhsB8csfWAGDd";
        ftpClient = new FTPClient();
        jobToDo = new ArrayList<>();
        actionJsonObjectArray = new ArrayList<>();
        startFTPWorkTimer();
        if (getApp().betaFlag) {
            dataPath = FTP_TEST_PATH;
        } else {
            dataPath = FTP_USER_STICK_DATA_PATH;
        }
    }

    void startFTPWorkTimer() {
        if (ftpTimer == null) {
            ftpTimer = new Timer();
            final TimerTask task = new TimerTask() {
                @SuppressWarnings("unchecked")
                @Override
                public void run() {
                    if (jobToDo.size() != 0) {
                        if (!jobFlag) {
                            Log.d(TAG, "待辦工作");
                            Log.d(TAG,"---------------------------------------------------------------");
                            for (int i = 0; i < jobToDo.size(); i++) {
                                switch ((JobAction) jobToDo.get(i)[0]) {
                                    case UploadDir:
                                        Log.d(TAG, "上傳目錄:" + jobToDo.get(i)[1]);
                                        break;
                                    case UploadFile:
                                        Log.d(TAG, "上傳檔案:" + jobToDo.get(i)[1]);
                                        break;
                                    case DownloadDir:
                                        Log.d(TAG, "下載目錄:" + jobToDo.get(i)[1]);
                                        break;
                                    case DownloadFile:
                                        Log.d(TAG, "下載檔案:" + jobToDo.get(i)[1]);
                                        break;
                                    case DeleteFile:
                                        Log.d(TAG, "刪除檔案:" + jobToDo.get(i)[1]);
                                        break;
                                }
                            }
                            Log.d(TAG,"---------------------------------------------------------------");
                            jobFlag = true;
                            JobAction jobAction = (JobAction) jobToDo.get(0)[0];
                            FTPProgressListener listener = null;
                            if (jobToDo.get(0).length == 3) {
                                listener = (FTPProgressListener) jobToDo.get(0)[2];
                            }
                            String path = "";
                            ArrayList<String> paths;
                            initFTP(listener);
                            switch (jobAction) {
                                case UploadDir:
                                    paths = (ArrayList<String>) jobToDo.get(0)[1];
                                    uploadDirToFTP(paths, listener);
                                    break;
                                case UploadFile:
                                    path = (String) jobToDo.get(0)[1];
                                    uploadFileToFTP(path, listener);
                                    break;
                                case DownloadDir:
                                    paths = (ArrayList<String>) jobToDo.get(0)[1];
                                    downloadDirFromFTP(paths, listener);
                                    break;
                                case DownloadFile:
                                    path = (String) jobToDo.get(0)[1];
                                    downloadFileFromFTP(path, listener);
                                    break;
                                case DeleteFile:
                                    path = (String) jobToDo.get(0)[1];
                                    deleteFileFromFTP(path, listener);
                                    break;
                            }
                        }
                    } else if (actionJsonObjectArray.size() != 0) {
                        uploadActionToFTP(Action.Default, (currentStep) -> {

                        });
                    }
                }
            };
            ftpTimer.schedule(task, 0, 1500);
            Log.d(TAG, "Start running FTP timer");
        }
    }
    //</editor-fold>

    //<editor-fold desc="Connection">

    /**
     * 開啟FTP服務.
     */
    private void openConnect() throws IOException {
        if (!ftpClient.isConnected()) {
            // 中文轉碼
            ftpClient.setControlEncoding("UTF-8");
            int reply; // 伺服器響應值
// 連線至伺服器
            ftpClient.connect(hostName, serverPort);
// 獲取響應值
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
// 斷開連線
                ftpClient.disconnect();
                throw new IOException("connect fail: " + reply);
            }
// 登入到伺服器
            ftpClient.login(userName, password);

// 獲取響應值
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
// 斷開連線
                ftpClient.disconnect();
                throw new IOException("connect fail: " + reply);
            } else {
// 獲取登入資訊
                FTPClientConfig config = new FTPClientConfig(ftpClient.getSystemType().split(" ")[0]);
                config.setServerLanguageCode("zh");
                ftpClient.configure(config);
// 使用被動模式設為預設
                ftpClient.enterLocalPassiveMode();
// 二進位制檔案支援
                ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            }
        }
    }

    /**
     * 關閉FTP服務.
     */
    private void closeConnect() throws IOException {
        if (ftpClient != null) {
// 退出FTP
            ftpClient.logout();
// 斷開連線
            ftpClient.disconnect();
        }
    }
    //</editor-fold>

    //<editor-fold desc="FTPMethod">

    public void initFTP(FTPProgressListener listener) {
        try {
            try {
                this.openConnect();
                if (listener != null) {
                    listener.onFTPProgress(FTP_CONNECT_SUCCESS);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onFTPProgress(FTP_CONNECT_FAIL);
                }
            }
            if (!ftpClient.printWorkingDirectory().equals("/")) {
                ftpClient.changeWorkingDirectory("/");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finishFTP(FTPProgressListener listener) {
        jobToDo.remove(0);
        jobFlag = false;
        if (listener != null) {
            listener.onFTPProgress(FTP_FINISH);
        }
    }

    public void uploadDirToFTP(ArrayList<String> dirList, FTPProgressListener listener) {
        try {
            for (String dir : dirList) {
                File directory = getApp().getDir(dir, Context.MODE_PRIVATE);
                File[] files = directory.listFiles();
                if (files == null) {
                    Log.d(TAG, "本地目錄:" + dir + "檔案不存在");
                    return;
                }
                String serverPath;
                serverPath = dataPath + dir;
                Log.d(TAG, "上傳到雲端的資料夾:" + serverPath);
                boolean flag;
                FTPFile[] ftpFiles = ftpClient.listFiles(serverPath, FTPFile::isFile);
                ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
                StringBuilder pathBuffer = new StringBuilder();
                for (String pathDir : serverPath.split("/")) {
                    pathBuffer.append(pathDir);
                    pathBuffer.append("/");
                    ftpClient.makeDirectory(pathBuffer.toString());
                }
                ftpClient.changeWorkingDirectory(serverPath);
                for (File file : files) {
                    boolean check = false;
                    for (FTPFile ftpFile : ftpFiles) {
                        if (file.getName().equals(ftpFile.getName())) {
                            check = true;
                        }
                    }
                    if (!check) {
                        Log.d(TAG, "開始上傳" + dir + "/" + file.getName());
                        BufferedInputStream buffIn = new BufferedInputStream(
                                new FileInputStream(file));
                        flag = ftpClient.storeFile(file.getName(), buffIn);
                        buffIn.close();
                        if (flag) {
                            try {
                                actionJsonObjectArray.add(new JSONObject().put("Add", dir + "/" + file.getName()));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG, dir + "/" + file.getName() + "上傳成功");
                            if (listener != null) {
                                listener.onFTPProgress(FTP_UPLOAD_SUCCESS);
                            }
                        } else {
                            Log.d(TAG, dir + "/" + file.getName() + "上傳失敗");
                            if (listener != null) {
                                listener.onFTPProgress(FTP_UPLOAD_FAIL);
                            }
                        }
                    }
                }
                ftpClient.changeWorkingDirectory("/");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finishFTP(listener);
    }

    public void uploadFileToFTP(String path, FTPProgressListener listener) {
        try {
            String dir = path.split("/")[0];
            String fileName = path.split("/")[1];
            Log.d(TAG, "開始上傳" + dir + "/" + fileName);
            File directory = getApp().getDir(dir, Context.MODE_PRIVATE);
            File file = new File(directory, fileName);
            String serverPath;
            serverPath = dataPath + dir;
            ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
            StringBuilder pathBuffer = new StringBuilder();
            for (String pathDir : serverPath.split("/")) {
                pathBuffer.append(pathDir);
                pathBuffer.append("/");
                ftpClient.makeDirectory(pathBuffer.toString());
            }
            ftpClient.changeWorkingDirectory(serverPath);
            BufferedInputStream buffIn = new BufferedInputStream(
                    new FileInputStream(file));
            if (ftpClient.storeFile(file.getName(), buffIn)) {
                if (!dir.equals(DEVICE_INFO)) {
                    try {
                        actionJsonObjectArray.add(new JSONObject().put("Add", dir + "/" + fileName));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "上傳" + dir + "/" + fileName + "成功");
                if (listener != null) {
                    listener.onFTPProgress(FTP_UPLOAD_SUCCESS);
                }
            } else {
                Log.d(TAG, "上傳" + dir + "/" + fileName + "失敗");
                if (listener != null) {
                    listener.onFTPProgress(FTP_UPLOAD_FAIL);
                }
            }
            buffIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finishFTP(listener);
    }

    public void downloadDirFromFTP(ArrayList<String> dirList, FTPProgressListener listener) {
        try {
            for (String dir : dirList) {
                String serverPath;
                serverPath = dataPath + dir;
                FTPFile[] ftpFiles = ftpClient.listFiles(serverPath, FTPFile::isFile);
                if (ftpFiles.length == 0) {
                    if (listener != null) {
                        listener.onFTPProgress(FTP_FILE_NOT_EXISTS);
                    }
                    continue;
                }
                for (FTPFile ftpFile : ftpFiles) {
                    File directory = getApp().getDir(dir, Context.MODE_PRIVATE);
                    if (!directory.exists()) {
                        if (!directory.mkdirs()) {
                            Log.d(TAG, "本地" + dir + "建立失敗");
                        }
                    }
                    String localPath = directory + "/" + ftpFile.getName();
                    Log.d(TAG, "開始下載" + serverPath + "/" + ftpFile.getName());
                    long serverSize = ftpFiles[0].getSize();
                    File localFile = new File(localPath);
                    long localSize;
                    if (localFile.exists()) {
                        localSize = localFile.length(); // 如果本地檔案存在，獲取本地檔案的長度
                        if (localSize >= serverSize) {
                            File file = new File(localPath);
                            if (!file.delete()) {
                                Log.d(TAG, "本地" + file.getName() + "刪除失敗");
                            }
                        }
                    }
                    OutputStream out = new FileOutputStream(localFile, false);
                    InputStream input = ftpClient.retrieveFileStream(serverPath + "/" + ftpFile.getName());
                    byte[] b = new byte[1024];
                    int length;
                    while ((length = input.read(b)) != -1) {
                        out.write(b, 0, length);
                    }
                    out.flush();
                    out.close();
                    input.close();
                    if (ftpClient.completePendingCommand()) {
                        Log.d(TAG, "下載" + serverPath + "/" + ftpFile.getName() + "成功");
                        if (listener != null) {
                            listener.onFTPProgress(FTP_DOWN_SUCCESS);
                        }
                    } else {
                        if (listener != null) {
                            listener.onFTPProgress(FTP_DOWN_FAIL);
                        }
                    }
                }
                Log.d(TAG, "下載到本地端的資料夾:" + dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finishFTP(listener);
    }

    public void downloadFileFromFTP(String path, FTPProgressListener listener) {
        try {
            String dir = path.split("/")[0];
            String fileName = path.split("/")[1];
            String serverPath;
            serverPath = dataPath + dir;
            Log.d(TAG, "開始下載" + serverPath + "/" + fileName);
            FTPFile[] ftpFiles = ftpClient.listFiles(serverPath, ftpFile -> {
                if (ftpFile.getName().equals(fileName)) {
                    return ftpFile.isFile();
                }
                return false;
            });
            if (ftpFiles.length == 0) {
                if (listener != null) {
                    listener.onFTPProgress(FTP_FILE_NOT_EXISTS);
                }
                return;
            }
            File directory = getApp().getDir(dir, Context.MODE_PRIVATE);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    Log.d(TAG, "本地" + dir + "建立失敗");
                }
            }
            String localPath = directory + "/" + fileName;
            long serverSize = ftpFiles[0].getSize();
            File localFile = new File(localPath);
            long localSize;
            if (localFile.exists()) {
                localSize = localFile.length(); // 如果本地檔案存在，獲取本地檔案的長度
                if (localSize >= serverSize) {
                    File file = new File(localPath);
                    if (!file.delete()) {
                        Log.d(TAG, "本地" + file.getName() + "刪除失敗");
                    }
                }
            }
            OutputStream out = new FileOutputStream(localFile, false);
            InputStream input = ftpClient.retrieveFileStream(serverPath + "/" + fileName);
            byte[] b = new byte[1024];
            int length;
            while ((length = input.read(b)) != -1) {
                out.write(b, 0, length);
            }
            out.flush();
            out.close();
            input.close();
            if (ftpClient.completePendingCommand()) {
                Log.d(TAG, "下載" + serverPath + "/" + fileName + "成功");
                if (listener != null) {
                    listener.onFTPProgress(FTP_DOWN_SUCCESS);
                }
            } else {
                Log.d(TAG, "下載" + serverPath + "/" + fileName + "失敗");
                if (listener != null) {
                    listener.onFTPProgress(FTP_DOWN_FAIL);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finishFTP(listener);
    }

    public void deleteFileFromFTP(String path, FTPProgressListener listener) {
        try {
            String serverPath;
            String dir = path.split("/")[0];
            String fileName = path.split("/")[1];
            serverPath = dataPath + dir;
            Log.d(TAG, "開始刪除" + serverPath + "/" + fileName);
            FTPFile[] ftpFiles = ftpClient.listFiles(serverPath, ftpFile -> {
                if (ftpFile.getName().equals(fileName)) {
                    return ftpFile.isFile();
                }
                return false;
            });
            if (ftpFiles.length == 0) {
                Log.d(TAG, FTP_FILE_NOT_EXISTS);
                if (listener != null) {
                    listener.onFTPProgress(FTP_FILE_NOT_EXISTS);
                }
                return;
            }
            if (ftpClient.deleteFile(serverPath + "/" + fileName)) {
                try {
                    actionJsonObjectArray.add(new JSONObject().put("Delete", dir + "/" + fileName));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "刪除" + serverPath + "/" + fileName + "成功");
                if (listener != null) {
                    listener.onFTPProgress(FTP_DELETE_SUCCESS);
                }
            } else {
                Log.d(TAG, "刪除" + serverPath + "/" + fileName + "失敗");
                if (listener != null) {
                    listener.onFTPProgress(FTP_DELETE_FAIL);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finishFTP(listener);
    }

    public void shareFileToFTP(File file, String shareName, FTPProgressListener listener) {
        try {
            String serverPath;
            if (getApp().betaFlag) {
                serverPath = FTP_TEST_SHARE_PATH;
            } else {
                serverPath = FTP_STICK_SHARE_PATH;
            }
            ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
            StringBuilder pathBuffer = new StringBuilder();
            for (String pathDir : serverPath.split("/")) {
                pathBuffer.append(pathDir);
                pathBuffer.append("/");
                ftpClient.makeDirectory(pathBuffer.toString());
            }
            ftpClient.changeWorkingDirectory(serverPath);
            boolean flag;
            BufferedInputStream buffIn = new BufferedInputStream(
                    new FileInputStream(file));
            flag = ftpClient.storeFile(shareName, buffIn);
            buffIn.close();
            if (flag) {
                if (listener != null) {
                    listener.onFTPProgress(FTP_UPLOAD_SUCCESS);
                }
            } else {
                if (listener != null) {
                    listener.onFTPProgress(FTP_UPLOAD_FAIL);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finishFTP(listener);
    }

    public void obtainFromFTP(String getCode, String presetName, FTPProgressListener listener) {
        try {
            String serverPath;
            if (getApp().betaFlag) {
                serverPath = FTP_TEST_SHARE_PATH;
            } else {
                serverPath = FTP_STICK_SHARE_PATH;
            }
            File dir = getApp().getDir(CUSTOM_INFO, Context.MODE_PRIVATE);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.d(TAG, "本地" + dir + "建立失敗");
                }
            }
            new Downloader.DownloadTask().setOnDownloadFinishListener(result -> {
                WriteFile(result, CUSTOM_INFO, presetName);
            }).execute("http://app.serafim-tech.com/" + serverPath + getCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finishFTP(listener);
    }

    public void uploadActionToFTP(Action actionEnum, FTP.FTPProgressListener listener) {
        jobToDo.add(new Object[]{
                FTP.JobAction.DownloadFile,
                DEVICE_INFO + "/" + FTP_ACTION,
                (FTP.FTPProgressListener) (currentStep) -> {
                    try {
                        JSONObject deviceJsonObject = new JSONObject();
                        JSONArray actionJsonArray;
                        String action = "";
                        String dir = "";
                        if (currentStep.equals(FTP_DOWN_SUCCESS)) {
                            deviceJsonObject = new JSONObject(ReadFile(DEVICE_INFO, FTP_ACTION));
                            Log.d(TAG, deviceJsonObject.toString());
                            switch (actionEnum) {
                                case Default:
                                    for (JSONObject actionJsonObject : actionJsonObjectArray) {
                                        if (actionJsonObject.has("Add")) {
                                            action = "Add";
                                            dir = actionJsonObject.getString(action);
                                        } else if (actionJsonObject.has("Delete")) {
                                            action = "Delete";
                                            dir = actionJsonObject.getString(action);
                                        }
                                        Iterator<String> keys = deviceJsonObject.keys();
                                        while (keys.hasNext()) {
                                            String key = keys.next();
                                            if (deviceJsonObject.get(key) instanceof JSONArray && !key.equals(getApp().androidID)) {
                                                actionJsonArray = deviceJsonObject.getJSONArray(key);
                                                JSONArray actionJsonArrayBuffer = actionJsonArray;
                                                for (int i = 0; i < actionJsonArrayBuffer.length(); i++) {
                                                    if (actionJsonArrayBuffer.getJSONObject(i).has("Add")) {
                                                        if (actionJsonArrayBuffer.getJSONObject(i).getString("Add").equals(dir)) {
                                                            actionJsonArray.remove(i);
                                                        }
                                                    } else if (actionJsonArrayBuffer.getJSONObject(i).has("Delete")) {
                                                        if (actionJsonArrayBuffer.getJSONObject(i).getString("Delete").equals(dir)) {
                                                            actionJsonArray.remove(i);
                                                        }
                                                    }
                                                }
                                                actionJsonArray.put(actionJsonObject);
                                                deviceJsonObject.put(key, actionJsonArray);
                                            }
                                        }
                                    }
                                    deviceJsonObject.put(getApp().androidID, new JSONArray());
                                    actionJsonObjectArray = new ArrayList<>();
                                    break;
                                case Init:
                                    if (deviceJsonObject.has(getApp().androidID)) {
                                        actionJsonArray = deviceJsonObject.getJSONArray(getApp().androidID);
                                        for (int i = 0; i < actionJsonArray.length(); i++) {
                                            if (actionJsonArray.getJSONObject(i).has("Add")) {
                                                String[] path = actionJsonArray.getJSONObject(i).getString("Add").split("/");
                                                jobToDo.add(new Object[]{
                                                        FTP.JobAction.DownloadFile,
                                                        path[0] + "/" + path[1],
                                                        (FTP.FTPProgressListener) (currentStep1) -> {

                                                        }
                                                });
                                            } else if (actionJsonArray.getJSONObject(i).has("Delete")) {
                                                String[] path = actionJsonArray.getJSONObject(i).getString("Delete").split("/");
                                                Log.d(TAG, "刪除本地檔案" + path[0] + "/" + path[1]);
                                                File localDir = getApp().getDir(path[0], Context.MODE_PRIVATE);
                                                File localFile = new File(localDir, path[1]);
                                                if (localFile.exists()) {
                                                    deleteRecursive(localFile);
                                                }
                                            }
                                        }
                                        actionJsonArray = new JSONArray();
                                        deviceJsonObject.put(getApp().androidID, actionJsonArray);
                                    }else{
                                        jobToDo.add(new Object[]{
                                                FTP.JobAction.DownloadDir,
                                                new ArrayList<String>() {{
                                                    add(CUSTOM_INFO);
                                                    add(CUSTOM_INFO_BIND_GAME);
                                                    add(CUSTOM_MACRO_INFO);
                                                }}
                                        });

                                        jobToDo.add(new Object[]{
                                                FTP.JobAction.UploadDir,
                                                new ArrayList<String>() {{
                                                    add(CUSTOM_INFO);
                                                    add(CUSTOM_INFO_BIND_GAME);
                                                    add(CUSTOM_MACRO_INFO);
                                                }}
                                        });

                                        uploadActionToFTP(FTP.Action.Default,null);
                                    }
                                    break;
                                case Remove:
                                    deviceJsonObject.remove(getApp().androidID);
                                    break;
                            }
                            Log.d(TAG, deviceJsonObject.toString());
                            WriteFile(deviceJsonObject.toString(), DEVICE_INFO, FTP_ACTION);
                        } else if (currentStep.equals(FTP_FILE_NOT_EXISTS)) {
                            deviceJsonObject.put(getApp().androidID, new JSONArray());
                            WriteFile(deviceJsonObject.toString(), DEVICE_INFO, FTP_ACTION);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        });
        jobToDo.add(new Object[]{
                FTP.JobAction.UploadFile,
                DEVICE_INFO + "/" + FTP_ACTION,
                listener
        });
    }

    //</editor-fold>

    //<editor-fold desc="Callback">
    public interface FTPProgressListener {
        void onFTPProgress(String currentStep);
    }
    //</editor-fold>
}
