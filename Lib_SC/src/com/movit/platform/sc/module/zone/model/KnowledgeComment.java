package com.movit.platform.sc.module.zone.model;

/**
 * Created by air on 16/9/28.
 * 评论
 */

public class KnowledgeComment {

    private String knowledgeId;
    private String userId;
    private String name;
    private String content;
    private String createDate;

    public KnowledgeComment(){
    }

    public KnowledgeComment(String createDate, String knowledgeId, String userId, String name, String content) {
        this.createDate = createDate;
        this.knowledgeId = knowledgeId;
        this.userId = userId;
        this.name = name;
        this.content = content;
    }

    public String getKnowledgeId() {
        return knowledgeId;
    }

    public void setKnowledgeId(String knowledgeId) {
        this.knowledgeId = knowledgeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "KnowledgeComment{" +
                "knowledgeId='" + knowledgeId + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                ", createDate='" + createDate + '\'' +
                '}';
    }
}
