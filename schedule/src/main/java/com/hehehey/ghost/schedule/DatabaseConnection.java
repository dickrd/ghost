package com.hehehey.ghost.schedule;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hehehey.ghost.record.PageData;
import com.hehehey.ghost.record.TaskData;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

/**
 * Created by Dick Zhou on 3/30/2017.
 * Connect to database to store or retrieve result.
 */
public class DatabaseConnection {

    private static final String tableTask = "task";

    private static MongoDatabase database;

    private Gson gson;

    public static void newConnection() {
        MongoClientURI mongoClientURI = new MongoClientURI(MasterConfig.INSTANCE.getMongoUri());
        MongoClient mongoClient = new MongoClient(mongoClientURI);
        database = mongoClient.getDatabase(mongoClientURI.getDatabase());
    }

    public DatabaseConnection() {
        gson = new Gson();
    }

    /**
     * Create a task record.
     * @param id   Task id.
     */
    public void insertTask(String id, String name) {
        MongoCollection<Document> taskCollection = database.getCollection(tableTask);
        TaskData taskData = new TaskData(name, id, System.currentTimeMillis());
        taskCollection.insertOne(toDocument(taskData));
    }

    /**
     /**
     * Get an array of task record.
     * @param page Page index.
     * @param size Array size of task record.
     * @return Task id array.
     */
    public TaskData[] selectTask(int page, int size) {
        MongoCursor<Document> iterator = database.getCollection(tableTask).find().skip(page * size).iterator();

        List<TaskData> dataList = new ArrayList<>();
        for (int i = 0; i < size && iterator.hasNext(); i++) {
            dataList.add(gson.fromJson(gson.toJson(iterator.next()), TaskData.class));
        }
        return dataList.toArray(new TaskData[0]);
    }

    /**
     * Change the name of a task.
     * @param id   Id of the task to change.
     * @param name The name to change to.
     */
    public void updateTaskName(String id, String name) {
        MongoCollection<Document> taskCollection = database.getCollection(tableTask);
        taskCollection.updateOne(eq("id", id), set("name", name));
    }

    /**
     * Save the data to database.
     * @param id   Task id of the data.
     * @param page Data contents.
     */
    public void insertData(String id, PageData page) throws Exception {
        if (database.getCollection(tableTask).find(eq("id", id)).first() == null) {
            throw new Exception("Task not exist.");
        }
        MongoCollection<Document> theCollection = database.getCollection(id);

        theCollection.insertOne(toDocument(page));
    }

    /**
     * Get an array of data of a task.
     * @param id   Task id.
     * @param page Page of data.
     * @param size Data size per page.
     * @return Data array.
     */
    public PageData[] selectData(String id, int page, int size) {
        MongoCursor<Document> iterator = database.getCollection(id).find().skip(page * size).iterator();

        List<PageData> dataList = new ArrayList<>();
        for (int i = 0; i < size && iterator.hasNext(); i++) {
            dataList.add(gson.fromJson(gson.toJson(iterator.next()), PageData.class));
        }
        return dataList.toArray(new PageData[0]);
    }

    /**
     * Count all data size of a task.
     * @param id Task id to count.
     * @return Data count.
     */
    public long countData(String id) {
        return database.getCollection(id).count();
    }

    private Document toDocument(Object o) {
        return new Document(gson.fromJson(gson.toJson(o), new TypeToken<Map<String, Object>>(){}.getType()));
    }
}
