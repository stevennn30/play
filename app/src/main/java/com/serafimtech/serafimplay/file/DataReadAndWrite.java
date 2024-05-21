package com.serafimtech.serafimplay.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

import static com.serafimtech.serafimplay.App.getApp;


public class DataReadAndWrite {
    private static final String TAG = DataReadAndWrite.class.getSimpleName();

    //寫入app內部存儲
    public static void WriteFile(String strContent, String dirName, String fileName) {
        FileOutputStream fos = null;
        try {
            // 獲取應用內部存儲中的目錄
            File directory = getApp().getDir(dirName, Context.MODE_PRIVATE);
            // 在該目錄中創建一個表示文件的 File 對象
            File mypath = new File(directory, fileName);

            // 創建 FileOutputStream 將數據寫入該文件
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            fos.write(strContent.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 關閉 FileOutputStream 以釋放系統資源
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String ReadFile(String dirName, String fileName) {
        String content = "";
        try {
            File directory = getApp().getDir(dirName, Context.MODE_PRIVATE);
            File f = new File(directory, fileName);
            FileInputStream input = new FileInputStream(f);
            InputStreamReader inputReader = new InputStreamReader(input, StandardCharsets.UTF_8);
            BufferedReader buffReader = new BufferedReader(inputReader);
            String line;
            while ((line = buffReader.readLine()) != null) {
                content = content + line + "\n";
            }
            input.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, fileName + " doesn't not exist.");
            return "";
        } catch (IOException | NullPointerException e) {
            Log.d(TAG, e.getMessage());
            return "";
        }
        return content;
    }

    public static void saveBitmapToInternalStorage(Bitmap bitmapImage, String dirName, String fileName) {
        FileOutputStream fos = null;
        try {
            File directory = getApp().getDir(dirName, Context.MODE_PRIVATE);
            File mypath = new File(directory, fileName);
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap loadImageFromStorage(String dirName, String fileName) {
        try {
            File directory = getApp().getDir(dirName, Context.MODE_PRIVATE);
            File f = new File(directory, fileName);
            if(f.exists()) {
                return BitmapFactory.decodeStream(new FileInputStream(f));
            }
            return  null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveObject(String dirName, String fileName, Object b) {
        try {
            File directory = getApp().getDir(dirName, Context.MODE_PRIVATE);
            File myPath = new File(directory, fileName);
            FileOutputStream fileOut = new FileOutputStream(myPath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(b);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static Object[] readObject(String dirName, String fileName) {
        try {
            File directory = getApp().getDir(dirName, Context.MODE_PRIVATE);
            File myPath = new File(directory, fileName);
            FileInputStream fileIn = new FileInputStream(myPath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Object obj =  in.readObject();
            in.close();
            fileIn.close();
            if(obj instanceof Object[]){
                return (Object[]) obj;
            }else{
                return new Object[]{obj};
            }
        } catch (IOException i) {
//            i.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
//            c.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> getFilesAllName(String path) {
        File file = getApp().getDir(path, Context.MODE_PRIVATE);
        File[] files = file.listFiles();
        if (files == null) {
            Log.e("error", "no file");
            return new ArrayList<>();
        }
        ArrayList<String> s = new ArrayList<>();
        for (File fileName : files) {
            s.add(fileName.getName());
        }
        Collections.reverse(s);
        return s;
    }

    public static void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        while (!fileOrDirectory.delete()) {
            Log.d(TAG, "File delete failed");
        }
    }

    //寫入外部存儲
//    public void WriteFile2(String strContent, String dirName, String fileName) {
//        FileOutputStream fos = null;
//        try {
//            File root = android.os.Environment.getExternalStorageDirectory();
//            File dir = new File(root.getAbsolutePath() + "/" + dirName);
//            dir.mkdirs();
//            File mypath = new File(root, fileName);
//
//
//            fos = new FileOutputStream(mypath);
//            // Use the compress method on the BitMap object to write image to the OutputStream
//            fos.write(strContent.getBytes());
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                // 關閉 FileOutputStream 以釋放系統資源
//                if (fos != null) {
//                    fos.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
