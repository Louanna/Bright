package com.movitech.EOP.module.events.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/12/29.
 *
 * 活动
 */
public class Activities implements Serializable {

    //活动ID
    private String id;
    //活动名称
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
