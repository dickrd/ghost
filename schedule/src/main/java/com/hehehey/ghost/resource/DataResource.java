package com.hehehey.ghost.resource;

import com.google.gson.Gson;
import com.hehehey.ghost.message.Response;
import com.hehehey.ghost.record.PageData;
import com.hehehey.ghost.schedule.DatabaseConnection;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 4/6/2017.
 * Api related to store and query data extracted from web pages.
 */
@Path("/data")
public class DataResource {

    private static final Logger logger = Logger.getLogger(DataResource.class.getName());

    private Gson gson = new Gson();
    private DatabaseConnection databaseConnection = new DatabaseConnection();

    /**
     * Update a task progress by providing the result.
     * @param id         Task id of the data.
     * @param jsonString The (partial) result of the task.
     * @return Task status.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String submitTask(@PathParam("id") String id, String jsonString) {
        Response response;
        try {
            PageData pageData = gson.fromJson(jsonString, PageData.class);
            databaseConnection.insertData(id, pageData);
            response = new Response<>(Response.Status.ok, "");
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
            logger.log(Level.INFO, "", e);
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
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@PathParam("id") String id,
                        @DefaultValue("10") @QueryParam("size") int size,
                        @DefaultValue("0") @QueryParam("page") int page) {
        Response response;

        try {
            PageData data[] = databaseConnection.selectData(id, page, size);
            if (data.length > 0)
                response = new Response<>(Response.Status.ok, data);
            else
                response = new Response<>(Response.Status.wait, "Error task id or no data yet.");
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
            logger.log(Level.INFO, "", e);
        }

        return gson.toJson(response);
    }
}
