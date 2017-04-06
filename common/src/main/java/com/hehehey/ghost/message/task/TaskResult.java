package com.hehehey.ghost.message.task;

import java.util.HashMap;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class TaskResult {
    /**
     * Data collected.
     */
    private HashMap<String, String> data[];

    public TaskResult(HashMap<String, String>[] data) {
        this.data = data;
    }

    public HashMap<String, String>[] getData() {
        return data;
    }
}
