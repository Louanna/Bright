package com.movit.platform.common.entities;

/**
 * Created by air on 16/7/7.
 *
 */
public class TokenBean {

    private boolean result;
    private String message;
    private String type;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TokenBean{" +
                "result=" + result +
                ", message='" + message + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
