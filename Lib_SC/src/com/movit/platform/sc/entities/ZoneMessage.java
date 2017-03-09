package com.movit.platform.sc.entities;

import java.io.Serializable;

public class ZoneMessage implements Serializable {
    private String cId;
    private String cUserId;
    private String cToUserId;
    private String cSayId;
    private String iType;  // 1.评论,2.@,3.赞
    private Integer iHasRead;
    private String dCreateTime;
    private String sMessage;

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getcUserId() {
        return cUserId;
    }

    public void setcUserId(String cUserId) {
        this.cUserId = cUserId;
    }

    public String getcSayId() {
        return cSayId;
    }

    public void setcSayId(String cSayId) {
        this.cSayId = cSayId;
    }

    public String getiType() {
        return iType;
    }

    public void setiType(String iType) {
        this.iType = iType;
    }

    public Integer getiHasRead() {
        return iHasRead;
    }

    public void setiHasRead(Integer iHasRead) {
        this.iHasRead = iHasRead;
    }

    public String getdCreateTime() {
        return dCreateTime;
    }

    public void setdCreateTime(String dCreateTime) {
        this.dCreateTime = dCreateTime;
    }

    public String getsMessage() {
        return sMessage;
    }

    public void setsMessage(String sMessage) {
        this.sMessage = sMessage;
    }

    public String getcToUserId() {
        return cToUserId;
    }

    public void setcToUserId(String cToUserId) {
        this.cToUserId = cToUserId;
    }

}
