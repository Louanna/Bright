package com.movit.platform.cloud.model;

/**
 * Created by air on 16/3/17.
 * 文件属性
 */
public class DocumentVersion {

    private String actual;//": true,
    private String author;//": "okmAdmin",
    private String checksum;//": "6facb7d39210e53a3f98b0d7d8ac47d6",
    private String created;//": "2016-03-17T14:09:34+08:00",
    private String name;//": 1,
    private double size;//": 116603

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "DocumentVersion{" +
                "actual='" + actual + '\'' +
                ", author='" + author + '\'' +
                ", checksum='" + checksum + '\'' +
                ", created='" + created + '\'' +
                ", name='" + name + '\'' +
                ", size='" + size + '\'' +
                '}';
    }
}
