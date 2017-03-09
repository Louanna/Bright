package com.movitech.EOP.module.events.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/12/29.
 * <p/>
 * 活动---奖项
 */
public class Awards implements Serializable {

    //奖项ID
    private String id;
    //奖项名称
    private String awardsName;
    //奖品名称
    private String prizeName;
    //中奖状态
    private String status;
    //中奖人数
    private String num;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAwardsName() {
        return awardsName;
    }

    public void setAwardsName(String awardsName) {
        this.awardsName = awardsName;
    }

    public String getPrizeName() {
        return prizeName;
    }

    public void setPrizeName(String prizeName) {
        this.prizeName = prizeName;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
