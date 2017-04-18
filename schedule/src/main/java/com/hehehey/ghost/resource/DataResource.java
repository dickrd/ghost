package com.hehehey.ghost.resource;

import com.google.gson.Gson;
import com.hehehey.ghost.message.Response;
import com.hehehey.ghost.record.PageData;
import com.hehehey.ghost.schedule.DatabaseConnection;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
     * @param jsonString The result of the operation.
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
     * Update a task progress by providing the result.
     * @param id         Task id of the data.
     * @param jsonString The update result of the task.
     * @return Task status.
     */
    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateTask(@PathParam("id") String id,
                             @QueryParam("fields") List<String> fields,
                             String jsonString) {
        Response response;
        try {
            PageData updateData = gson.fromJson(jsonString, PageData.class);
            if (updateData == null || updateData.getUrl() == null || updateData.getUrl().contentEquals(""))
                throw new Exception("Updates provided is not sufficient!");

            PageData oldData = databaseConnection.selectData(id, updateData.getUrl());
            if (oldData == null || oldData.getData() == null)
                throw new Exception("No such data!");

            // Update every field specified.
            for (String field: fields) {
                Object oldField = oldData.getData().get(field);
                Object updateField = updateData.getData().get(field);
                if (oldField instanceof Collection && updateField instanceof Collection) {
                    Collection oldCollection = (Collection) oldField;
                    Collection updateCollection = (Collection) updateField;

                    for (Object obj: updateCollection) {
                        if (!oldCollection.contains(obj)) {
                            try {
                                //noinspection unchecked
                                oldCollection.add(obj);
                            } catch (Exception e) {
                                logger.log(Level.FINE, "Add failed for: " + obj);
                            }
                        }
                    }
                    oldData.getData().put(field, oldCollection);
                } else if (oldField instanceof Map && updateField instanceof Map) {
                    Map oldMap = (Map) oldField;
                    Map updateMap = (Map) updateField;

                    //noinspection unchecked
                    updateMap.forEach((k, v) -> {
                        try {
                            //noinspection unchecked
                            oldMap.put(k, v);
                        } catch (Exception e) {
                            logger.log(Level.FINE, "Put failed for: " + k);
                        }
                    });
                    oldData.getData().put(field, oldMap);
                } else {
                    oldData.getData().put(field, updateField);
                }
            }
            databaseConnection.replaceData(id, oldData);
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
                        @DefaultValue("0") @QueryParam("page") int page,
                        @QueryParam("fields") List<String> fields) {
        Response response;

        try {
            PageData data[] = databaseConnection.selectData(id, page, size, fields);
            if (data.length > 0)
                response = new Response<>(Response.Status.ok, data);
            else
                response = new Response<>(Response.Status.wait, "No data yet for: " + id);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
            logger.log(Level.INFO, "", e);
        }

        return gson.toJson(response);
    }

    /**
     * Get data by its url.
     * @param id   Task id.
     * @param url  Url of the data.
     * @return Web page data.
     */
    @GET
    @Path("/{id}/{url}")
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@PathParam("id") String id, @PathParam("id") String url) {
        Response response;

        try {
            PageData data = databaseConnection.selectData(id, url);
            response = new Response<>(Response.Status.ok, data);
        } catch (Exception e) {
            response = new Response<>(Response.Status.error, e.toString());
            logger.log(Level.INFO, "", e);
        }

        return gson.toJson(response);
    }
}
