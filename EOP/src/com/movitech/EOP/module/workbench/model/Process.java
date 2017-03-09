package com.movitech.EOP.module.workbench.model;

import java.util.ArrayList;

/**
 * Created by air on 16/10/20.
 *
 */

public class Process {

    private ArrayList<ProcessModel> item;
    private String TODO;
    private String DONE;

    public ArrayList<ProcessModel> getItem() {
        return item;
    }

    public void setItem(ArrayList<ProcessModel> item) {
        this.item = item;
    }

    public String getTODO() {
        return TODO;
    }

    public void setTODO(String TODO) {
        this.TODO = TODO;
    }

    public String getDONE() {
        return DONE;
    }

    public void setDONE(String DONE) {
        this.DONE = DONE;
    }

    @Override
    public String toString() {
        return "Process{" +
                "item=" + item +
                ", TODO='" + TODO + '\'' +
                ", DONE='" + DONE + '\'' +
                '}';
    }
}
