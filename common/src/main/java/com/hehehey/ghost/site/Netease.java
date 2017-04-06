package com.hehehey.ghost.site;

import com.hehehey.ghost.util.HttpClient;

import java.io.IOException;

/**
 * Created by Dick Zhou on 3/29/2017.
 * Web request related to Netease.
 */
public class Netease {

    private static final String apiUrlNeteaseCount = "http://sdk.comment.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/%s";
    private static final String charset = "gbk";
    private HttpClient client;

    public Netease() {
        this.client = new HttpClient();
    }

    public String getNeteaseCount(String url) throws IOException {
        String[] split = url.split("/");
        String last = split[split.length - 1];
        String id = last.substring(0, last.indexOf("."));
        return client.getAsString(String.format(apiUrlNeteaseCount, id));
    }

    public String getHtml(String url) throws IOException {
        return client.getAsString(url, charset);
    }
}
