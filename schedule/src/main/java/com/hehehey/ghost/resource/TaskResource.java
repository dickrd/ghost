package com.hehehey.ghost.resource;

import com.google.gson.Gson;
import com.hehehey.ghost.message.Response;
import com.hehehey.ghost.message.frontend.TaskProgress;
import com.hehehey.ghost.message.frontend.UserRequest;
import com.hehehey.ghost.message.task.Assignment;
import com.hehehey.ghost.record.TaskData;
import com.hehehey.ghost.schedule.DatabaseConnection;
import com.hehehey.ghost.schedule.RedisConnection;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 3/30/2017.
 * Api related to task creation, query and etc.
 */
@Path("/task")
public class TaskResource {

    private static final Logger logger = Logger.getLogger(TaskResource.class.getName());

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
            String id = redisConnection.addTask(userRequest.getSeeds(), userRequest.getWords());
            databaseConnection.insertTask(id, userRequest.getTaskName().trim());
            response = new Response<>(Response.Status.ok, id);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
            logger.log(Level.INFO, "", e);
        }

        return gson.toJson(response);
    }

    /**
     * Get task list.
     * @param size List size.
     * @param page Page count.
     * @return Task list.
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String getTasks(@DefaultValue("10") @QueryParam("size") int size,
                           @DefaultValue("0") @QueryParam("page") int page) {
        Response response;

        try {
            TaskData[] tasks = databaseConnection.selectTask(page, size);
            response = new Response<>(Response.Status.ok, tasks);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
            logger.log(Level.INFO, "", e);
        }

        return gson.toJson(response);
    }

    /**
     * Get keywords of the task.
     * @param source Source type. "words" or "seeds".
     * @param size   Keywords array size.
     * @return Keywords of the task.
     */
    @GET
    @Path("/{source}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String dispatch(@PathParam("source") String source, @DefaultValue("1") @QueryParam("size") int size) {
        Response response;
        try {
            String id = redisConnection.getTask();
            response = getSource(id, source, size);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
            logger.log(Level.INFO, "", e);
        }

        return gson.toJson(response);
    }

    /**
     * Get current results status of the task, not including data.
     * @param id The task.
     * @return Task status.
     */
    @GET
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@PathParam("id") String id) {
        Response response;

        try {
            long urlCount = redisConnection.count(id);
            long dataCount = databaseConnection.countDataByTask(id);
            TaskProgress progress = new TaskProgress();
            progress.setId(id);
            progress.setDataCount(dataCount);
            progress.setRemainingUrlCount(urlCount);
            response = new Response<>(Response.Status.ok, progress);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
            logger.log(Level.INFO, "", e);
        }

        return gson.toJson(response);
    }

    private Response getSource(String id, String source, int size) {
        String[] content;
        if (id == null) {
            return new Response<>(Response.Status.wait, "No more tasks.");
        }

        if (source.contentEquals("words")){
            content = redisConnection.getWords(id, size);
        }
        else if (source.contentEquals("seeds")){
            content = redisConnection.getSeeds(id, size);
        }
        else {
            return new Response<>(Response.Status.unsupported, "Unsupported source type: " + source
                    + ", only supports \"words\" and \"seeds\".");
        }

        if (content.length > 0)
            return new Response<>(Response.Status.ok, new Assignment(id, content));
        else
            return new Response<>(Response.Status.wait, "No more source: " + source);
    }
}
