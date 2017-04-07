package com.hehehey.ghost.message.frontend;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class TaskProgress {

    /**
     * 任务ID
     */
    private String id;

    /**
     * 任务剩余未完成的网址
     */
    private int remainingUrlCount;

    /**
     * 此次任务的当前数据量
     */
    private int dataCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRemainingUrlCount() {
        return remainingUrlCount;
    }

    public void setRemainingUrlCount(int remainingUrlCount) {
        this.remainingUrlCount = remainingUrlCount;
    }

    public int getDataCount() {
        return dataCount;
    }

    public void setDataCount(int dataCount) {
        this.dataCount = dataCount;
    }
}
