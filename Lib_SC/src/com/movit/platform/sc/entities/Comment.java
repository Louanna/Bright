package com.movit.platform.sc.entities;

public class Comment {
    String cId;
    String sayId;
    String userId;
    String touserId;
    String content;
    String parnetId;
    String rootId;

    public String getSayId() {
        return sayId;
    }

    public void setSayId(String sayId) {
        this.sayId = sayId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getParnetId() {
        return parnetId;
    }

    public void setParnetId(String parnetId) {
        this.parnetId = parnetId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTouserId() {
        return touserId;
    }

    public void setTouserId(String touserId) {
        this.touserId = touserId;
    }

    public String getRootId() {
        return rootId;
    }

    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

}
