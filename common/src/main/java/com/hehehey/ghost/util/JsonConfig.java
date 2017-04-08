package com.hehehey.ghost.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by Dick Zhou on 4/6/2017.
 * Read configuration on the disk.
 */
public class JsonConfig<T> {

    private final Gson gson;
    private final String path;

    public JsonConfig(String path) {
        this.path = path;

        gson = new Gson();
    }

    public T read(Type type) throws IOException {
        File configFile = new File(path);
        if (!configFile.exists())
            throw new FileNotFoundException(configFile.getAbsolutePath());

        return gson.fromJson(new FileReader(configFile), type);
    }
}
