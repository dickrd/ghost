package com.hehehey.ghost.resource;

import com.google.gson.Gson;
import com.hehehey.ghost.message.task.Assignment;
import com.hehehey.ghost.schedule.RedisConnection;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Dick Zhou on 4/6/2017.
 * Api related to web page url, including add new urls and retrieve them.
 */
@Path("/url")
public class UrlResource {

    private Gson gson = new Gson();
    private RedisConnection redisConnection = new RedisConnection();

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

        return gson.toJson(new Assignment(id, urls));
    }
}
