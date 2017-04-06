package com.hehehey.ghost.resource;

import com.google.gson.Gson;
import com.hehehey.ghost.message.task.PageData;
import com.hehehey.ghost.message.task.TaskProgress;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Dick Zhou on 4/6/2017.
 * Api related to store and query data extracted from web pages.
 */
@Path("/data")
public class DataResource {

    private Gson gson = new Gson();

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
            PageData pageData = gson.fromJson(jsonString, PageData.class);

            //TODO Save data and count remaining urls.
            pageData.getData();
            taskProgress = new TaskProgress(pageData.getId(), TaskProgress.Status.ok, 0);
        } catch (Exception e) {
            taskProgress = new TaskProgress("", TaskProgress.Status.error, 0);
            taskProgress.setDetail(e.getLocalizedMessage());
        }

        return gson.toJson(taskProgress);
    }
}
