package com.movit.platform.sc.module.zone.model;

/**
 * Created by air on 16/9/28.
 *
 */

public class BaseModel {

    private String value;
    private Object objValue;
    private Boolean ok;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Object getObjValue() {
        return objValue;
    }

    public void setObjValue(Object objValue) {
        this.objValue = objValue;
    }

    public Boolean getOk() {
        return ok;
    }

    public void setOk(Boolean ok) {
        this.ok = ok;
    }

    @Override
    public String toString() {
        return "BaseModel{" +
                "value='" + value + '\'' +
                ", objValue=" + objValue +
                ", ok='" + ok + '\'' +
                '}';
    }
}
