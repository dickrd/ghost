package com.hehehey.ghost.schedule;

import com.hehehey.ghost.record.Task;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.HashMap;

/**
 * Created by Dick Zhou on 3/30/2017.
 * Connect to database to store or retrieve result.
 */
public class DatabaseConnection {

    public DatabaseConnection() {
        MongoClient mongoClient = new MongoClient(MasterConfig.INSTANCE.getMongoUri());
        MongoCollection<Document> collection = mongoClient.getDatabase("").getCollection("");
    }

    /**
     /**
     * Get an array of task record.
     * @param page Page index.
     * @param size Array size of task record.
     * @return Task id array.
     */
    public Task[] selectTask(int page, int size) {
        return null;
    }

    /**
     * Count all data size of a task.
     * @param id Task id to count.
     * @return Data count.
     */
    public int countDataByTask(String id) {
        return 0;
    }

    /**
     * Create a task record.
     * @param id   Task id.
     */
    public void insertData(String id, String name) {

    }

    /**
     * Save the data to database.
     * @param id   Task id of the data.
     * @param data Data contents.
     */
    public void insertData(String id, HashMap<String, String>[] data) {

    }

    /**
     * Get a specific data.
     * @param id The id of the data.
     * @return Data contents.
     */
    public HashMap<String, String> selectData(String id) {
        return null;
    }

    /**
     * Get an array of data of a task.
     * @param id   Task id.
     * @param page Page of data.
     * @param size Data size per page.
     * @return Data array.
     */
    public HashMap<String, String>[] selectDataByTask(String id, int page, int size) {
        return null;
    }
}
