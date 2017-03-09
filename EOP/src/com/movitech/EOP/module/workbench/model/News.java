package com.movitech.EOP.module.workbench.model;

/**
 * Created by air on 16/9/27.
 * 公司新闻
 */

public class News {

    private String title;
    private String title2;
    private String time;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle2() {
        return title2;
    }

    public void setTitle2(String title2) {
        this.title2 = title2;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "News{" +
                "title='" + title + '\'' +
                ", title2='" + title2 + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
