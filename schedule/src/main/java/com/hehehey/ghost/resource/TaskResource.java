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
     * @return Adding status.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String newTask(String jsonString) {
        Response<String> response;
        try {
            UserRequest userRequest = gson.fromJson(jsonString, UserRequest.class);
            String id = redisConnection.addTask(userRequest.getSeeds(), userRequest.getWords());
            databaseConnection.insertTask(id, userRequest.getName().trim());
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
    @Produces(MediaType.APPLICATION_JSON)
    public String getTasks(@DefaultValue("10") @QueryParam("size") int size,
                           @DefaultValue("0") @QueryParam("page") int page) {
        Response response;

        try {
            TaskData[] tasks = databaseConnection.selectTask(page, size);
            if (tasks.length > 0)
                response = new Response<>(Response.Status.ok, tasks);
            else
                response = new Response<>(Response.Status.wait, "No task yet.");
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
    @Path("/{source: words|seeds}")
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
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@PathParam("id") String id) {
        Response response;

        try {
            Long[] redisCount = redisConnection.count(id);
            long dataCount = databaseConnection.countData(id);
            TaskProgress progress = new TaskProgress();
            progress.setId(id);
            progress.setDataCount(dataCount);
            progress.setRemainingUrlCount(redisCount[0]);
            progress.setRemainingSourceCount(redisCount[1]);
            response = new Response<>(Response.Status.ok, progress);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
            logger.log(Level.INFO, "", e);
        }

        return gson.toJson(response);
    }

    /**
     * Get source of a task.
     * @param id         Task id to get.
     * @return Source.
     */
    @GET
    @Path("/{id}/{source: words|seeds}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSourceOf(@PathParam("id") String id,
                            @PathParam("source") String source,
                            @DefaultValue("1") @QueryParam("size") int size) {
        Response response;
        try {
            response = getSource(id, source, size);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
            logger.log(Level.INFO, "", e);
        }

        return gson.toJson(response);
    }

    /**
     * Add source to a task.
     * @param id         Task id to add.
     * @param jsonString Task request detail.
     * @return Adding status.
     */
    @PUT
    @Path("/{id}/{source: words|seeds}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addSource(@PathParam("id") String id, String jsonString) {
        Response<String> response;
        try {
            UserRequest userRequest = gson.fromJson(jsonString, UserRequest.class);
            redisConnection.addSource(id, userRequest.getSeeds(), userRequest.getWords());
            response = new Response<>(Response.Status.ok, id);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
            logger.log(Level.INFO, "", e);
        }

        return gson.toJson(response);
    }

    /**
     * Modify a task.
     * @param id         Task id to modify.
     * @param jsonString Task details.
     * @return Modification status.
     */
    @POST
    @Path("/{id}/name")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String changeTaskName(@PathParam("id") String id, String jsonString) {
        Response<String> response;
        try {
            UserRequest userRequest = gson.fromJson(jsonString, UserRequest.class);
            databaseConnection.updateTaskName(id, userRequest.getName().trim());
            response = new Response<>(Response.Status.ok, id);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
            logger.log(Level.INFO, "", e);
        }

        return gson.toJson(response);
    }

    private Response getSource(String id, String source, int size) {
        String[] content = new String[0];
        if (id == null) {
            return new Response<>(Response.Status.wait, "No more task.");
        }

        if (source.contentEquals("words")){
            content = redisConnection.getWords(id, size);
        }
        else if (source.contentEquals("seeds")){
            content = redisConnection.getSeeds(id, size);
        }

        if (content.length > 0)
            return new Response<>(Response.Status.ok, new Assignment(id, content));
        else
            return new Response<>(Response.Status.wait, "No more source for task: " + source + ", " + id);
    }
}
