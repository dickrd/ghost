package com.hehehey.ghost.record;

import java.util.HashMap;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class PageData {

    /**
     * 网页数据爬取的URL
     */
    private String url;

    /**
     * 爬取时间戳
     */
    private long createdAt;

    /**
     * 爬取的内容
     */
    private HashMap<String, Object> data;

    public PageData(String url, long createdAt, HashMap<String, Object> data) {
        this.url = url;
        this.createdAt = createdAt;
        this.data = data;
    }
}
