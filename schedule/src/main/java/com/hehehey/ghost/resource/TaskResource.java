package com.hehehey.ghost.resource;

import com.google.gson.Gson;
import com.hehehey.ghost.message.Response;
import com.hehehey.ghost.message.frontend.TaskProgress;
import com.hehehey.ghost.message.frontend.UserRequest;
import com.hehehey.ghost.message.task.Assignment;
import com.hehehey.ghost.record.Task;
import com.hehehey.ghost.schedule.DatabaseConnection;
import com.hehehey.ghost.schedule.RedisConnection;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
        Response<String> response;
        try {
            UserRequest userRequest = gson.fromJson(jsonString, UserRequest.class);
            switch (userRequest.getType()) {
                case search:
                    String id = redisConnection.addTask(UserRequest.SourceType.search, userRequest.getKeywords());
                    response = new Response<>(Response.Status.ok, id);
                    databaseConnection.insertData(id, userRequest.getTaskName());
                    break;
                default:
                    response = new Response<>(Response.Status.unsupported, "");
                    break;
            }
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
        }

        return gson.toJson(response);
    }

    /**
     * Get task list.
     * @return Task list.
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String getTasks(@DefaultValue("10") @QueryParam("size") int size,
                           @DefaultValue("0") @QueryParam("page") int page) {
        Response response;

        try {
            Task[] tasks = databaseConnection.selectTask(page, size);
            response = new Response<>(Response.Status.ok, tasks);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
        }

        return gson.toJson(response);
    }

    /**
     * Get keywords of the task.
     * @param id   The task id. A random one if not provided.
     * @param size Keywords array size.
     * @return Keywords of the task.
     */
    @GET
    @Path("/words")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String dispatch(@DefaultValue("") @QueryParam("id") String id, @DefaultValue("1") @QueryParam("size") int size) {
        Response response;
        try {
            if (id.contentEquals(""))
                id = redisConnection.getTask();

            if (id == null) {
                response = new Response<>(Response.Status.wait, "No more tasks.");
            }
            else {
                String[] words = redisConnection.getSource(id, UserRequest.SourceType.search, size);
                response = new Response<>(Response.Status.ok, new Assignment(id, words));
            }
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
        }

        return gson.toJson(response);
    }

    /**
     * Get current results status of the task, not including data.
     * @param id The task.
     * @return Task status.
     */
    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@PathParam("id") String id) {
        Response response;

        try {
            int urlCount = redisConnection.count(id);
            int dataCount = databaseConnection.countDataByTask(id);
            TaskProgress progress = new TaskProgress();
            progress.setId(id);
            progress.setDataCount(dataCount);
            progress.setRemainingUrlCount(urlCount);
            response = new Response<>(Response.Status.ok, progress);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
        }

        return gson.toJson(response);
    }
}
