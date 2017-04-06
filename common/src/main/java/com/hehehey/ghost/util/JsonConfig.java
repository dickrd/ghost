package com.hehehey.ghost.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;

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

    public T read() throws IOException {
        return gson.fromJson(new FileReader(path), new TypeToken<T>(){}.getType());
    }
}
