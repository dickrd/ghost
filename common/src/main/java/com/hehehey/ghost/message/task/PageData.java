package com.hehehey.ghost.message.task;

import java.util.HashMap;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class PageData {

    /**
     * Task id of the data.
     */
    private String id;

    /**
     * Data collected.
     */
    private HashMap<String, String> data[];

    public PageData(String id, HashMap<String, String>[] data) {
        this.id = id;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public HashMap<String, String>[] getData() {
        return data;
    }
}
