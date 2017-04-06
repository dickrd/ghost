package com.hehehey.ghost.message.task;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class TaskProgress {

    /**
     * 新建的任务ID
     */
    private String id;

    /**
     * 任务状态
     */
    private Status status;

    /**
     * 任务剩余未完成的网址
     */
    private int remainingUrl;

    /**
     * 详细错误信息
     */
    private String detail;

    public TaskProgress(String id, Status status, int remainingUrl) {
        this.id = id;
        this.status = status;
        this.remainingUrl = remainingUrl;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public enum Status {
        ok,
        unsupported,
        error
    }
}
