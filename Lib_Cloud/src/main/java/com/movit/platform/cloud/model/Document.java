package com.movit.platform.cloud.model;

/**
 * Created by air on 16/3/17.
 * 文件
 */
public class Document extends Folder{

    private DocumentVersion actualVersion;
    private String checkedOut;//": false,
    private String convertibleToPdf;//": false,
    private String convertibleToSwf;//": false,
    private String description;//": "",
    private String language;//": "",
    private String lastModified;//": "2016-03-17T14:09:34+08:00",
    private String locked;//": false,
    private String mimeType;//": "application/pdf",
    private String title;//": ""
    private int download;// 0 未下载 ，1 成功（已下载），2 下载中，3 等待
    private int progress;
    private int positionInAdapter;

    public DocumentVersion getActualVersion() {
        return actualVersion;
    }

    public void setActualVersion(DocumentVersion actualVersion) {
        this.actualVersion = actualVersion;
    }

    public String getCheckedOut() {
        return checkedOut;
    }

    public void setCheckedOut(String checkedOut) {
        this.checkedOut = checkedOut;
    }

    public String getConvertibleToPdf() {
        return convertibleToPdf;
    }

    public void setConvertibleToPdf(String convertibleToPdf) {
        this.convertibleToPdf = convertibleToPdf;
    }

    public String getConvertibleToSwf() {
        return convertibleToSwf;
    }

    public void setConvertibleToSwf(String convertibleToSwf) {
        this.convertibleToSwf = convertibleToSwf;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getLocked() {
        return locked;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int isDownload() {
        return download;
    }

    public void setDownload(int download) {
        this.download = download;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getPositionInAdapter() {
        return positionInAdapter;
    }

    public void setPositionInAdapter(int positionInAdapter) {
        this.positionInAdapter = positionInAdapter;
    }

    @Override
    public String toString() {
        return "Document{" +
                "actualVersion=" + actualVersion +
                ", checkedOut='" + checkedOut + '\'' +
                ", convertibleToPdf='" + convertibleToPdf + '\'' +
                ", convertibleToSwf='" + convertibleToSwf + '\'' +
                ", description='" + description + '\'' +
                ", language='" + language + '\'' +
                ", lastModified='" + lastModified + '\'' +
                ", locked='" + locked + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", title='" + title + '\'' +
                ", download=" + download +
                ", progress=" + progress +
                ", positionInAdapter=" + positionInAdapter +
                '}';
    }
}
