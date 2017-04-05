package com.hehehey.ghost.message;

import java.util.HashMap;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class TaskSubmission {

    /**
     * Task id.
     */
    private String id;

    /**
     * Data collected.
     */
    private HashMap<String, String> data[];

    public TaskSubmission(String id, HashMap<String, String>[] data) {
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
