package com.movit.platform.cloud.model;

import java.util.ArrayList;

/**
 * Created by air on 16/3/17.
 * 文件夹
 */
public class Folder {

    private String author;//": "okmAdmin",
    private String created;//": "2016-03-16T10:14:56+08:00",
    private String createDate;//": "2016-03-16 10:14:56",
    private String path;//": "/okm:root",
    private String permissions;//": 15,
    private String subscribed;//": false,
    private String uuid;//": "46946c01-b584-4b4b-87d5-9c31aca0623b",
    private boolean hasChildren;//": true
    private ArrayList<String> childrenIds;
    private String supperId;//": "64fc4179-d23c-4524-9602-10c253fe46b7",


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public String getSubscribed() {
        return subscribed;
    }

    public void setSubscribed(String subscribed) {
        this.subscribed = subscribed;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public ArrayList<String> getChildrenIds() {
        return childrenIds;
    }

    public void setChildrenIds(ArrayList<String> childrenIds) {
        this.childrenIds = childrenIds;
    }

    public String getSupperId() {
        return supperId;
    }

    public void setSupperId(String supperId) {
        this.supperId = supperId;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "author='" + author + '\'' +
                ", created='" + created + '\'' +
                ", createDate='" + createDate + '\'' +
                ", path='" + path + '\'' +
                ", permissions='" + permissions + '\'' +
                ", subscribed='" + subscribed + '\'' +
                ", uuid='" + uuid + '\'' +
                ", hasChildren=" + hasChildren +
                ", childrenIds=" + childrenIds +
                ", supperId='" + supperId + '\'' +
                '}';
    }
}
