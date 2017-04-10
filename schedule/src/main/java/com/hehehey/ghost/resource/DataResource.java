package com.hehehey.ghost.resource;

import com.google.gson.Gson;
import com.hehehey.ghost.message.Response;
import com.hehehey.ghost.message.task.PageData;
import com.hehehey.ghost.schedule.DatabaseConnection;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;

/**
 * Created by Dick Zhou on 4/6/2017.
 * Api related to store and query data extracted from web pages.
 */
@Path("/data")
public class DataResource {

    private Gson gson = new Gson();
    private DatabaseConnection databaseConnection = new DatabaseConnection();

    /**
     * Update a task progress by providing the result.
     * @param jsonString The (partial) result of the task.
     * @return Task status.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String submitTask(String jsonString) {
        Response response;
        try {
            PageData pageData = gson.fromJson(jsonString, PageData.class);
            String id = pageData.getId();
            HashMap<String, String>[] data = pageData.getData();
            databaseConnection.insertData(id, data);
            response = new Response<>(Response.Status.ok, "");
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.getLocalizedMessage());
        }

        return gson.toJson(response);
    }

    /**
     * Get a single piece of data by its id.
     * @param id The data id.
     * @return Data contents.
     */
    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@PathParam("id") String id) {
        Response response;

        try {
            HashMap<String, String> data = databaseConnection.selectData(id);
            response = new Response<>(Response.Status.ok, data);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.getLocalizedMessage());
        }

        return gson.toJson(response);
    }

    /**
     * Get data of a task.
     * @param id   Task id.
     * @param size Page size required.
     * @param page Page count.
     * @return Web page data.
     */
    @GET
    @Path("/task/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@PathParam("id") String id,
                        @DefaultValue("10") @QueryParam("size") int size,
                        @DefaultValue("0") @QueryParam("page") int page) {
        Response response;

        try {
            HashMap<String, String> data[] = databaseConnection.selectDataByTask(id, page, size);
            response = new Response<>(Response.Status.ok, data);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.getLocalizedMessage());
        }

        return gson.toJson(response);
    }
}
