package com.hehehey.ghost.message.task;

/**
 * Created by Dick Zhou on 4/6/2017.
 *
 */
public class SourcingResult {
    private String id;
    private String urls[];

    public SourcingResult(String id, String[] urls) {
        this.id = id;
        this.urls = urls;
    }

    public String getId() {
        return id;
    }

    public String[] getUrls() {
        return urls;
    }
}
