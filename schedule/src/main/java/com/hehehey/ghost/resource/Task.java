package com.hehehey.ghost.resource;

import com.google.gson.Gson;
import com.hehehey.ghost.message.*;
import com.hehehey.ghost.schedule.RedisConnection;
import content.Record;
import schedule.SingleThreadWorker;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Dick Zhou on 3/30/2017.
 *
 */
@Path("/task")
public class Task {

    private Gson gson = new Gson();
    private RedisConnection redisConnection = new RedisConnection();

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
    public String get(String jsonString) {
        TaskAssignment taskAssignment;
        try {
            TaskRequest taskRequest = gson.fromJson(jsonString, TaskRequest.class);
            taskAssignment = SingleThreadWorker.dispatch(taskRequest.getNames(), taskRequest.getUrlSize());
        } catch (Exception e) {
            taskAssignment = new TaskAssignment("", new String[0]);
        }

        return gson.toJson(taskAssignment);
    }


    @GET
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String requestTask(@DefaultValue("10") @QueryParam("size") int size, @PathParam("name") String name) {
        redisConnection.getUrls()
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@PathParam("id") String id) {
        Record[] records = SingleThreadWorker.query(id);

        return gson.toJson(new QueryResponse(id, records));
    }
}
