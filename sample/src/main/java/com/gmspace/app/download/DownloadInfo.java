package com.gmspace.app.download;

import java.io.File;

public class DownloadInfo {

    private final int key;
    private String url;
    private File output;
    private long currentLength;
    private long totalLength;

    public DownloadInfo(String url, File output, long currentLength, long totalLength) {
        this.key = String.format("%s_%s", url, output.getAbsolutePath()).hashCode();
        this.url = url;
        this.output = output;
        this.currentLength = currentLength;
        this.totalLength = totalLength;
    }

    public int getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public long getCurrentLength() {
        return currentLength;
    }

    public void setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }
}
