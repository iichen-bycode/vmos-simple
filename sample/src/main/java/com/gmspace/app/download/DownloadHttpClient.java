package com.gmspace.app.download;

import android.os.SystemClock;
import android.util.SparseArray;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadHttpClient {
    private static final SparseArray<DownloadInfo> infos = new SparseArray<DownloadInfo>();
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS).build();

    public interface OnDownloadProgressListener {
        void onProgress(int progress, long position, long contentLength, long speedLength);
    }

    public static boolean download(@NotNull String url, @NotNull File output, OnDownloadProgressListener listener) {
        DownloadInfo downloadInfo = new DownloadInfo(url, output, 0L, 0L);
        infos.put(downloadInfo.getKey(), downloadInfo);
        boolean succeed = false;
        File tempFile = new File(output.getParent(), output.getName() + ".tmp");

        try {
            if (tempFile.exists()) {
                tempFile.delete();
            }

            Response response = okHttpClient.newCall((new Request.Builder()).get().url(url).build()).execute();
            ResponseBody responseBody = response.body();
            if (response.isSuccessful() && responseBody != null) {
                long contentLength = responseBody.contentLength();
                ReadableByteChannel readableByteChannel = Channels.newChannel(responseBody.byteStream());
                FileChannel outputChannel = (new FileOutputStream(tempFile)).getChannel();
                long speedLength = 0L;
                long speedMarkLength = 0L;
                long speedMarkTime = SystemClock.uptimeMillis();
                long callbackTime = 0L;
                long position = 0L;
                long chunkSize = 81920L;

                while (position < contentLength) {
                    position += outputChannel.transferFrom(readableByteChannel, position, chunkSize);
                    if (SystemClock.uptimeMillis() - speedMarkTime >= 1000L) {
                        speedMarkTime = SystemClock.uptimeMillis();
                        speedLength = position - speedMarkLength;
                        speedMarkLength = position;
                    }

                    if (SystemClock.uptimeMillis() - callbackTime >= 500L) {
                        callbackTime = SystemClock.uptimeMillis();
                        if (listener != null) {
                            int progress = (int) (((float) position / contentLength) * 100);
                            listener.onProgress(progress, position, contentLength, speedLength);
                        }
                    }
                }

                if (tempFile.length() == contentLength) {
                    tempFile.renameTo(output);
                    succeed = true;
                }

                outputChannel.close();
                readableByteChannel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        infos.remove(downloadInfo.getKey());
        if (tempFile.exists()) {
            tempFile.delete();
        }

        return succeed;
    }
}
