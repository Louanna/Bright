package com.movit.platform.cloud.model;

/**
 * Created by air on 16/3/23.
 *
 */
public class Share {

    private String createBy;//": "2016-03-23 10:28:10",
    private String createDate;//": "okmAdmin",
    private String id;//": 4,
    private String nbsName;//": "20151214153458672-验收单.pdf",
    private String shareUser;//": "okmAdmin",
    private String shareUuid;//": "ab59f0dd-11c2-4c44-b8a8-f4c914593c78",
    private String type;//": 0 文件夹 1文件
    private double size;//":
    private int download;// 0 未下载 ，1 成功（已下载），2 下载中，3 等待
    private int progress;
    private int positionInAdapter;

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNbsName() {
        return nbsName;
    }

    public void setNbsName(String nbsName) {
        this.nbsName = nbsName;
    }

    public String getShareUser() {
        return shareUser;
    }

    public void setShareUser(String shareUser) {
        this.shareUser = shareUser;
    }

    public String getShareUuid() {
        return shareUuid;
    }

    public void setShareUuid(String shareUuid) {
        this.shareUuid = shareUuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
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
        return "Share{" +
                "createBy='" + createBy + '\'' +
                ", createDate='" + createDate + '\'' +
                ", id='" + id + '\'' +
                ", nbsName='" + nbsName + '\'' +
                ", shareUser='" + shareUser + '\'' +
                ", shareUuid='" + shareUuid + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", download=" + download +
                ", progress=" + progress +
                ", positionInAdapter=" + positionInAdapter +
                '}';
    }
}
