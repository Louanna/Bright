package com.movit.platform.cloud.model;


import com.movit.platform.cloud.view.DonutProgress;

/**
 * Created by air on 16/3/25.
 *
 */
public class ProgressButton {
    private DonutProgress button;
    private double totalSize;
    private String filePath;
    private int positionInAdapter;
    private boolean download;
    private int progress;

    public DonutProgress getButton() {
        return button;
    }

    public void setButton(DonutProgress button) {
        this.button = button;
    }

    public double getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(double totalSize) {
        this.totalSize = totalSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getPositionInAdapter() {
        return positionInAdapter;
    }

    public void setPositionInAdapter(int positionInAdapter) {
        this.positionInAdapter = positionInAdapter;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "ProgressButton{" +
                "button=" + button +
                ", totalSize=" + totalSize +
                ", filePath='" + filePath + '\'' +
                ", positionInAdapter=" + positionInAdapter +
                ", download=" + download +
                ", progress=" + progress +
                '}';
    }
}
