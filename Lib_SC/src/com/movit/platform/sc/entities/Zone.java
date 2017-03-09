package com.movit.platform.sc.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Zone implements Serializable {
    String cId;
    String content;
    String cUserId;
    String dCreateTime;
    String dLastCommentTime;
    int iIsSecret;
    String sImages;
    List<Comment> comments;
    int iSayType;
    int iDel;
    ArrayList<String> imageNames;
    ArrayList<String> imageSizes;
    ArrayList<String> likers;

    String iTop;//1 置顶。0，正常

    String videoPicPath;

    public String getVideoPicPath() {
        return videoPicPath;
    }

    public void setVideoPicPath(String videoPicPath) {
        this.videoPicPath = videoPicPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    String videoPath;

    @Override
    public String toString() {
        return "Zone [cId=" + cId + ", content=" + content + ", cUserId="
                + cUserId + ", dCreateTime=" + dCreateTime
                + ", dLastCommentTime=" + dLastCommentTime + ", iIsSecret="
                + iIsSecret + ", sImages=" + sImages + ", comments=" + comments
                + ", iSayType=" + iSayType + ", iDel=" + iDel + ", imageNames="
                + imageNames + ", imageSizes=" + imageSizes + ", likers="
                + likers + ", iTop=" + iTop+ ", videoPicPath=" + videoPicPath+ ", videoPath=" + videoPath + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cId == null) ? 0 : cId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Zone other = (Zone) obj;
        if (cId == null) {
            if (other.cId != null)
                return false;
        } else if (!cId.equals(other.cId))
            return false;
        return true;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getcUserId() {
        return cUserId;
    }

    public void setcUserId(String cUserId) {
        this.cUserId = cUserId;
    }

    public String getdCreateTime() {
        return dCreateTime;
    }

    public void setdCreateTime(String dCreateTime) {
        this.dCreateTime = dCreateTime;
    }

    public String getdLastCommentTime() {
        return dLastCommentTime;
    }

    public void setdLastCommentTime(String dLastCommentTime) {
        this.dLastCommentTime = dLastCommentTime;
    }

    public int getiIsSecret() {
        return iIsSecret;
    }

    public void setiIsSecret(int iIsSecret) {
        this.iIsSecret = iIsSecret;
    }

    public String getsImages() {
        return sImages;
    }

    public void setsImages(String sImages) {
        this.sImages = sImages;
    }

    public int getiSayType() {
        return iSayType;
    }

    public void setiSayType(int iSayType) {
        this.iSayType = iSayType;
    }

    public int getiDel() {
        return iDel;
    }

    public void setiDel(int iDel) {
        this.iDel = iDel;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public ArrayList<String> getImageNames() {
        return imageNames;
    }

    public void setImageNames(ArrayList<String> imageNames) {
        this.imageNames = imageNames;
    }

    public ArrayList<String> getImageSizes() {
        return imageSizes;
    }

    public void setImageSizes(ArrayList<String> imageSizes) {
        this.imageSizes = imageSizes;
    }

    public ArrayList<String> getLikers() {
        return likers;
    }

    public void setLikers(ArrayList<String> likers) {
        this.likers = likers;
    }

    public String getiTop() {
        return iTop;
    }

    public void setiTop(String iTop) {
        this.iTop = iTop;
    }

}
