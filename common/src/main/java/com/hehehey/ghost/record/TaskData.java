package com.hehehey.ghost.record;

/**
 * Created by Dick Zhou on 4/10/2017.
 * Task record as store in database.
 */
public class TaskData {

    /**
     * 用于显示的任务名称
     */
    private String name;

    /**
     * 任务ID
     */
    private String id;

    /**
     * 任务创建时间
     */
    private long createdAt;

    public TaskData(String name, String id, long createdAt) {
        this.name = name;
        this.id = id;
        this.createdAt = createdAt;
    }
}
