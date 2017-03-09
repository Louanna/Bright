package com.movit.platform.sc.module.zone.model;

/**
 * Created by air on 16/9/28.
 * 知识地图
 */

public class KnowledgeSay {

    private  String id;
    private  String type;
    private  String content;
    private  String images;
    private  String attachments;
    private  String createBy;
    private  String createDate;
    private  String updateBy;
    private  String updateDate;
    private  String remarks;
    private  String delFlag;
    private  String participantIds;
    private  String name;
    private  String createAppDate;
    private  String attachmentsUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

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

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(String participantIds) {
        this.participantIds = participantIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateAppDate() {
        return createAppDate;
    }

    public void setCreateAppDate(String createAppDate) {
        this.createAppDate = createAppDate;
    }

    public String getAttachmentsUrl() {
        return attachmentsUrl;
    }

    public void setAttachmentsUrl(String attachmentsUrl) {
        this.attachmentsUrl = attachmentsUrl;
    }

    @Override
    public String toString() {
        return "KnowledgeSay{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", images='" + images + '\'' +
                ", attachments='" + attachments + '\'' +
                ", createBy='" + createBy + '\'' +
                ", createDate='" + createDate + '\'' +
                ", updateBy='" + updateBy + '\'' +
                ", updateDate='" + updateDate + '\'' +
                ", remarks='" + remarks + '\'' +
                ", delFlag='" + delFlag + '\'' +
                ", participantIds='" + participantIds + '\'' +
                ", name='" + name + '\'' +
                ", createAppDate='" + createAppDate + '\'' +
                ", attachmentsUrl='" + attachmentsUrl + '\'' +
                '}';
    }
}
