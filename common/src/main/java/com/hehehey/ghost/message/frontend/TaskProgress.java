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
    private long remainingUrlCount;

    /**
     * 剩余的关键词和种子URL总数
     */
    private long remainingSourceCount;

    /**
     * 此次任务的当前数据量
     */
    private long dataCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getRemainingUrlCount() {
        return remainingUrlCount;
    }

    public void setRemainingUrlCount(long remainingUrlCount) {
        this.remainingUrlCount = remainingUrlCount;
    }

    public long getRemainingSourceCount() {
        return remainingSourceCount;
    }

    public void setRemainingSourceCount(long remainingSourceCount) {
        this.remainingSourceCount = remainingSourceCount;
    }

    public long getDataCount() {
        return dataCount;
    }

    public void setDataCount(long dataCount) {
        this.dataCount = dataCount;
    }
}
