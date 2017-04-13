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
    private String redisUri;
    private String mongoUri;

    private static final String configPath = "ghost.json";

    public static MasterConfig INSTANCE = null;

    public static void reload() throws IOException {
        INSTANCE = JsonConfig.path(configPath)
                .type(MasterConfig.class)
                .read();
    }

    public MasterConfig(String baseUrl, int port, String redisUri, String mongoUri) {
        this.baseUrl = baseUrl;
        this.port = port;
        this.redisUri = redisUri;
        this.mongoUri = mongoUri;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getPort() {
        return port;
    }

    String getRedisUri() {
        return redisUri;
    }

    String getMongoUri() {
        return mongoUri;
    }
}
