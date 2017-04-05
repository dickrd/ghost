package com.hehehey.ghost.resource;

import com.google.gson.Gson;
import com.hehehey.ghost.message.*;
import com.hehehey.ghost.schedule.RedisConnection;
import storage.DatabaseConnection;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;

/**
 * Created by Dick Zhou on 3/30/2017.
 *
 */
@Path("/task")
public class Task {

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
     * Update a task progress by providing the result.
     * @param jsonString The (partial) result of the task.
     * @return Task status.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String submitTask(String jsonString) {
        TaskProgress taskProgress;
        try {
            TaskSubmission taskSubmission = gson.fromJson(jsonString, TaskSubmission.class);

            //TODO Save data and count remaining urls.
            taskSubmission.getData();
            taskProgress = new TaskProgress(taskSubmission.getId(), TaskProgress.Status.ok, 0);
        } catch (Exception e) {
            taskProgress = new TaskProgress("", TaskProgress.Status.error, 0);
            taskProgress.setDetail(e.getLocalizedMessage());
        }

        return gson.toJson(taskProgress);
    }


    /**
     * Get urls matching the given name.
     * @param size Max size requested.
     * @param id   Task id. A random one if not provided.
     * @param name Name of the website those urls belongs to.
     * @return Matching urls. Empty array if none exist.
     */
    @GET
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String requestTask(@DefaultValue("10") @QueryParam("size") int size,
                              @DefaultValue("") @QueryParam("id") String id,
                              @PathParam("name") String name) {
        if (id.contentEquals(""))
            id = redisConnection.getTask();

        String[] urls = redisConnection.getUrls(id, name, size);

        return gson.toJson(new TaskAssignment(id, urls));
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
