package com.hehehey.ghost.resource;

import com.google.gson.Gson;
import com.hehehey.ghost.message.frontend.QueryResponse;
import com.hehehey.ghost.message.frontend.UserRequest;
import com.hehehey.ghost.message.task.TaskProgress;
import com.hehehey.ghost.schedule.RedisConnection;
import com.hehehey.ghost.storage.DatabaseConnection;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;

/**
 * Created by Dick Zhou on 3/30/2017.
 * Api related to task creation, query and etc.
 */
@Path("/task")
public class TaskResource {

    private Gson gson = new Gson();
    private RedisConnection redisConnection = new RedisConnection();
    private DatabaseConnection databaseConnection = new DatabaseConnection();

    /**
     * Generate a new task.
     * @param jsonString Task request detail.
     * @return Task status.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String newTask(String jsonString) {
        TaskProgress taskProgress;
        String id = "";
        try {
            UserRequest userRequest = gson.fromJson(jsonString, UserRequest.class);
            switch (userRequest.getType()) {
                case search:
                    id = redisConnection.addTask(UserRequest.SourceType.search, userRequest.getKeywords());
                    taskProgress = new TaskProgress(id, TaskProgress.Status.ok, 0);
                    break;
                default:
                    taskProgress = new TaskProgress(id, TaskProgress.Status.unsupported, 0);
                    break;
            }
        } catch (Exception e) {
            taskProgress = new TaskProgress(id, TaskProgress.Status.error, 0);
            taskProgress.setDetail(e.getLocalizedMessage());
        }

        return gson.toJson(taskProgress);
    }

    /**
     * Get current results of the task, including data.
     * @param id The task.
     * @return Task results.
     */
    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@PathParam("id") String id) {
        HashMap<String, String>[] data = databaseConnection.get(id);
        return gson.toJson(new QueryResponse(id, data));
    }
}
