package com.serafimtech.serafimplay.file;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static com.serafimtech.serafimplay.App.getApp;

/**
 * Java utils 實現的Zip工具
 *
 * @author once
 */
public class ZipUtils {
    private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte

    /**
     * 解壓縮一個檔案
     *
     * @param zipFile    壓縮檔案
     * @param folderPath 解壓縮的目標目錄
     * @throws IOException 當解壓縮過程出錯時丟擲
     */
    public static void upZipFile(File zipFile, String folderPath) throws IOException {
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }
        ZipFile zf = new ZipFile(zipFile);
        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = ((ZipEntry) entries.nextElement());
            InputStream in = zf.getInputStream(entry);
            String str = folderPath + File.separator + entry.getName();
            str = new String(str.getBytes("8859_1"), "GB2312");
            File desFile = new File(str);
            if (!desFile.exists()) {
                File fileParentDir = desFile.getParentFile();
                if (!fileParentDir.exists()) {
                    fileParentDir.mkdirs();
                }
                desFile.createNewFile();
            }
            OutputStream out = new FileOutputStream(desFile);
            byte buffer[] = new byte[BUFF_SIZE];
            int realLength;
            while ((realLength = in.read(buffer)) > 0) {
                out.write(buffer, 0, realLength);
            }
            in.close();
            out.close();
        }
    }

    /**
     * 獲得壓縮檔案內壓縮檔案物件以取得其屬性
     *
     * @param zipFile 壓縮檔案
     * @return 返回一個壓縮檔案列表
     * @throws ZipException 壓縮檔案格式有誤時丟擲
     * @throws IOException  IO操作有誤時丟擲
     */
    public static void unZipFiles2(File zipFile, String descDir) throws IOException {
        descDir = getApp().getDir(descDir, Context.MODE_PRIVATE).getAbsolutePath();

        //刪除資料夾原有檔案
        if (new File(descDir).isDirectory()) {
            File files[] = new File(descDir).listFiles();
            for (File value : files) {
                Log.e(value.getName(), "delete");
                value.delete();
            }
        }

        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        ZipFile zip = new ZipFile(zipFile);

        for (Enumeration<?> entries = zip.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String zipEntryName = entry.getName();
            InputStream in = zip.getInputStream(entry);
            String outPath = (descDir + "/" + zipEntryName).replaceAll("\\*", "/");
            //判斷路徑是否存在,不存在則建立檔案路徑
            File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
            Log.d("Entry3", outPath.substring(0, outPath.lastIndexOf('/')));
            if (!file.exists()) {
                file.mkdirs();
            }
            //判斷檔案全路徑是否為資料夾,如果是上面已經上傳,不需要解，並且刪除資料夾原有檔案
//            if (new File(outPath).isDirectory()) {
//                File files[] = new File(outPath).listFiles();
//                for (File value : files) {
//                    Log.e(value.getName(), "delete");
//                    value.delete();
//                }
//                continue;
//            }

            //輸出檔案路徑資訊
            System.out.println(outPath);
            OutputStream out = new FileOutputStream(outPath);
            byte[] buf1 = new byte[1024];
            int len;
            while ((len = in.read(buf1)) > 0) {
                out.write(buf1, 0, len);
            }
            in.close();
            out.close();
        }
        System.out.println("******************解壓完畢********************");
    }
}