package com.hehehey.ghost.util;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by Dick Zhou on 4/6/2017.
 * Read configuration on the disk.
 */
public class JsonConfig {

    /**
     * Required parameter to construct the config.
     */
    private final String path;

    /**
     * Optional. Defaults to object.
     */
    private Type type;

    private JsonConfig(String path) {
        this.path = path;

        type = Object.class;
    }

    public static JsonConfig path(String path) {
        return new JsonConfig(path);
    }

    public JsonConfig type(Type type) {
        this.type = type;
        return this;
    }

    public <T> T read() throws IOException {
        File configFile = new File(path);
        if (!configFile.exists())
            throw new FileNotFoundException(configFile.getAbsolutePath());

        return new Gson().fromJson(new FileReader(configFile), type);
    }
}
