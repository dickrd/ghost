package com.hehehey.ghost.message.task;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class Assignment {

    /**
     * 任务ID
     */
    private String id;

    /**
     * 满足条件的任务。包括URL和关键词
     */
    private String tasks[];

    public Assignment(String id, String[] tasks) {
        this.id = id;
        this.tasks = tasks;
    }

    public String getId() {
        return id;
    }

    public String[] getTasks() {
        return tasks;
    }
}
