package com.hehehey.ghost.record;

/**
 * Created by Dick Zhou on 4/10/2017.
 * Task record as store in database.
 */
public class TaskData {
    private String name;
    private String id;
    private long createdAt;

    public TaskData(String name, String id, long createdAt) {
        this.name = name;
        this.id = id;
        this.createdAt = createdAt;
    }
}
