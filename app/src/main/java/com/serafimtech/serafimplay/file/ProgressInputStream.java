package com.serafimtech.serafimplay.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ProgressInputStream extends InputStream {
    private static final int TEN_KILOBYTES = 1024 * 10; //每上傳10K返回一次
    private InputStream inputStream;
    private long progress;
    private long lastUpdate;
    private boolean closed;
    private File localFile;
    private FTP.FTPProgressListener listener;

    public ProgressInputStream(InputStream inputStream, FTP.FTPProgressListener listener, File localFile) {
        this.inputStream = inputStream;
        this.progress = 0;
        this.lastUpdate = 0;
        this.listener = listener;
        this.localFile = localFile;
        this.closed = false;
    }
    @Override
    public int read() throws IOException {
        int count = inputStream.read();
        return incrementCounterAndUpdateDisplay(count);
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = inputStream.read(b, off, len);
        return incrementCounterAndUpdateDisplay(count);
    }
    @Override
    public void close() throws IOException {
        super.close();
        if (closed)
            throw new IOException("already closed");
        closed = true;
    }
    private int incrementCounterAndUpdateDisplay(int count) {
        if (count > 0)
            progress  = count;
        lastUpdate = maybeUpdateDisplay(progress, lastUpdate);
        return count;
    }
    private long maybeUpdateDisplay(long progress, long lastUpdate) {
        if (progress - lastUpdate > TEN_KILOBYTES) {
            lastUpdate = progress;
//            this.listener.onFTPProgress(FTP_UPLOAD_LOADING, progress, this.localFile);
        }
        return lastUpdate;
    }
}
