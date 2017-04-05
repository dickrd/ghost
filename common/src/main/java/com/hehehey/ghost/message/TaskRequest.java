package com.hehehey.ghost.message;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class TaskRequest {

    /**
     * 支持的网站
     */
    private String names[];

    /**
     * 请求的URL数目
     */
    private int urlSize;

    public String[] getNames() {
        return names;
    }

    public int getUrlSize() {
        return urlSize;
    }
}
