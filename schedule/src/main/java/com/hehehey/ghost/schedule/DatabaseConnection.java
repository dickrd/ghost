package com.hehehey.ghost.schedule;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hehehey.ghost.record.Task;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dick Zhou on 3/30/2017.
 * Connect to database to store or retrieve result.
 */
public class DatabaseConnection {

    private static final String tableTask = "task";

    private MongoDatabase database;
    private Gson gson;

    public DatabaseConnection() {
        MongoClientURI mongoClientURI = new MongoClientURI(MasterConfig.INSTANCE.getMongoUri());
        MongoClient mongoClient = new MongoClient(mongoClientURI);
        database = mongoClient.getDatabase(mongoClientURI.getDatabase());

        gson = new Gson();
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
    public void insertTask(String id, String name) {
        MongoCollection<Document> taskCollection = database.getCollection(tableTask);
        Task task = new Task(name, id, System.currentTimeMillis());
        taskCollection.insertOne(convertToDocument(task));
    }

    /**
     * Save the data to database.
     * @param id   Task id of the data.
     * @param data Data contents.
     */
    public void insertData(String id, HashMap<String, String>[] data) {
        MongoCollection<Document> theCollection = database.getCollection(id);

        ArrayList<Document> documents = new ArrayList<>();
        for (HashMap<String, String> aData: data) {
            documents.add(new Document(Collections.unmodifiableMap(aData)));
        }
        theCollection.insertMany(documents);
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
        MongoCursor<Document> iterator = database.getCollection(id).find().skip(page * size).iterator();

        return null;
    }

    private Document convertToDocument(Object o) {
        return new Document(gson.fromJson(gson.toJson(o), new TypeToken<Map<String, Object>>(){}.getType()));
    }
}
