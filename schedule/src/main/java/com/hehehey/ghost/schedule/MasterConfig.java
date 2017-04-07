package com.hehehey.ghost.schedule;

import com.hehehey.ghost.util.JsonConfig;

import java.io.IOException;

/**
 * Created by Dick Zhou on 4/7/2017.
 *
 */
public class MasterConfig {
    private String baseUrl;
    private int port;
    private String redisHost;

    private static final String configPath = "ghost.json";

    public static MasterConfig INSTANCE = null;

    public static void reload() throws IOException {
        INSTANCE = new JsonConfig<MasterConfig>(configPath).read();
    }

    public MasterConfig(String baseUrl, int port, String redisHost) {
        this.baseUrl = baseUrl;
        this.port = port;
        this.redisHost = redisHost;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getPort() {
        return port;
    }

    String getRedisHost() {
        return redisHost;
    }
}
