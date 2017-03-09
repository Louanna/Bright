package com.movit.platform.common.entities;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Administrator on 2015/12/16.
 */
public class SerializableObj implements Serializable {
    private Map<String, Integer> map;

    public Map<String, Integer> getMap() {
        return map;
    }

    public void setMap(Map<String, Integer> map) {
        this.map = map;
    }
}
