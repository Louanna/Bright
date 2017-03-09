package com.movit.platform.cloud.model;

import java.util.ArrayList;

/**
 * Created by air on 16/3/18.
 * Okm File
 */
public class OkmFile {

    ArrayList<Document> folder;
    ArrayList<Document> folders;
    ArrayList<Document> document;
    ArrayList<Document> documents;

    ArrayList<Share> result;

    public ArrayList<Document> getFolder() {
        return folder;
    }

    public void setFolder(ArrayList<Document> folder) {
        this.folder = folder;
    }

    public ArrayList<Document> getFolders() {
        return folders;
    }

    public void setFolders(ArrayList<Document> folders) {
        this.folders = folders;
    }

    public ArrayList<Document> getDocument() {
        return document;
    }

    public void setDocument(ArrayList<Document> document) {
        this.document = document;
    }

    public ArrayList<Share> getResult() {
        return result;
    }

    public void setResult(ArrayList<Share> result) {
        this.result = result;
    }

    public ArrayList<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(ArrayList<Document> documents) {
        this.documents = documents;
    }

    @Override
    public String toString() {
        return "OkmFile{" +
                "folder=" + folder +
                ", document=" + document +
                ", documents=" + documents +
                ", result=" + result +
                '}';
    }
}
