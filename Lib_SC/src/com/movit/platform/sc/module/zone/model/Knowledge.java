package com.movit.platform.sc.module.zone.model;

import java.util.List;

/**
 * Created by air on 16/9/28.
 * 知识地图model
 */

public class Knowledge {

    private KnowledgeSay klnowledgeSay;
    private List<KnowledgeLike> knowledgeLike;
    private List<KnowledgeComment> knowledgeComment;

    public KnowledgeSay getKlnowledgeSay() {
        return klnowledgeSay;
    }

    public void setKlnowledgeSay(KnowledgeSay klnowledgeSay) {
        this.klnowledgeSay = klnowledgeSay;
    }

    public List<KnowledgeLike> getKnowledgeLike() {
        return knowledgeLike;
    }

    public void setKnowledgeLike(List<KnowledgeLike> knowledgeLike) {
        this.knowledgeLike = knowledgeLike;
    }

    public List<KnowledgeComment> getKnowledgeComment() {
        return knowledgeComment;
    }

    public void setKnowledgeComment(List<KnowledgeComment> knowledgeComment) {
        this.knowledgeComment = knowledgeComment;
    }

    @Override
    public String toString() {
        return "Knowledge{" +
                "klnowledgeSay=" + klnowledgeSay +
                ", knowledgeLike=" + knowledgeLike +
                ", knowledgeComment=" + knowledgeComment +
                '}';
    }
}
