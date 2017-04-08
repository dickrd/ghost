package com.hehehey.ghost.schedule;

import java.util.HashMap;

/**
 * Created by Dick Zhou on 3/30/2017.
 * Connect to database to store or retrieve result.
 */
public class DatabaseConnection {

    /**
     /**
     * Get an array of task id.
     * @param page Page index.
     * @param size Array size of task id.
     * @return Task id array.
     */
    public String[] select(int page, int size) {
        return null;
    }

    /**
     * Count all data size of a task.
     * @param id Task id to count.
     * @return Data count.
     */
    public int count(String id) {
        return 0;
    }

    /**
     * Save the data to database. Will create a task record if not exist.
     * @param id   Task id of the data.
     * @param data Data contents.
     */
    public void insert(String id, HashMap<String, String>[] data) {

    }

    /**
     * Get a specific data.
     * @param id The id of the data.
     * @return Data contents.
     */
    public HashMap<String, String> select(String id) {
        return null;
    }

    /**
     * Get an array of data of a task.
     * @param id   Task id.
     * @param page Page of data.
     * @param size Data size per page.
     * @return Data array.
     */
    public HashMap<String, String>[] selectRange(String id, int page, int size) {
        return null;
    }
}
