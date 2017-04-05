package com.hehehey.ghost.message;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class TaskAssignment {

    /**
     * 任务ID
     */
    private String id;

    /**
     * 满足条件的URL
     */
    private String urls[];

    public TaskAssignment(String id, String[] urls) {
        this.id = id;
        this.urls = urls;
    }
}
