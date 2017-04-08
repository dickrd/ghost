package com.hehehey.ghost.worker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hehehey.ghost.message.Response;
import com.hehehey.ghost.message.task.Assignment;
import com.hehehey.ghost.message.task.SourcingResult;
import com.hehehey.ghost.source.SearchSource;
import com.hehehey.ghost.util.HttpClient;
import com.hehehey.ghost.util.JsonConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 3/28/2017.
 * A worker that performs download and parse task in a single thread.
 */
public class SingleThreadSearchWorker extends Thread {

    private static final Logger logger = Logger.getLogger(SingleThreadSearchWorker.class.getName());

    private static final String configPath = "search.json";
    private static final String pathGetWork = "/task/words";
    private static final String pathPutResult = "/url";

    private final Gson gson;
    private final String masterUrl;
    private final int maxSleepMs;
    private final HttpClient httpClient;
    private final Random random;
    private final HashMap<String, String[]> workMap;

    private final SearchSource searchSource;

    public static void main(String[] args) throws IOException {
        WorkerConfig config = new JsonConfig<WorkerConfig>(configPath).read(new TypeToken<WorkerConfig>(){}.getType());
        SingleThreadSearchWorker singleThreadSearchWorker = new SingleThreadSearchWorker(config);
        singleThreadSearchWorker.start();
    }

    private SingleThreadSearchWorker(WorkerConfig config) {
        this.searchSource = new SearchSource(config.engines);
        this.workMap = new HashMap<>();
        this.httpClient = new HttpClient();
        this.random = new Random();
        this.maxSleepMs = config.maxSleepMs;
        this.masterUrl = config.masterUrl;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        String id;
        String keywords[];

        //noinspection InfiniteLoopStatement
        while (true) {
            while (workMap.isEmpty()) {
                try {
                    String stringResponse = httpClient.getAsString(masterUrl + pathGetWork);
                    Response response = gson.fromJson(stringResponse, Response.class);
                    if (response.getStatus() != Response.Status.ok) {
                        logger.log(Level.WARNING, "Master info: " + response.getData());
                        Thread.sleep(random.nextInt(maxSleepMs));
                        continue;
                    }

                    response = gson.fromJson(stringResponse, new TypeToken<Response<Assignment>>(){}.getType());
                    Assignment assignment = (Assignment) response.getData();
                    if (assignment.getTasks() != null && assignment.getTasks().length > 0) {
                        workMap.put(assignment.getId(), assignment.getTasks());
                    }
                    else {
                        logger.log(Level.INFO, "Waiting for new task.");
                        Thread.sleep(random.nextInt(maxSleepMs));
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Task query failed.", e);
                }
            }

            try {
                id = workMap.keySet().iterator().next();
                keywords = workMap.remove(id);
            } catch (Exception e) {
                logger.log(Level.WARNING, "No keywords.", e);
                continue;
            }

            logger.log(Level.INFO, "Search started.");
            for (String keyword : keywords) {
                try {
                    String[] links = searchSource.searchAll(keyword);
                    SourcingResult result = new SourcingResult(id, links);
                    httpClient.putString(masterUrl + pathPutResult, gson.toJson(result));
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Keyword failed: " + keyword, e);
                }
            }
        }
    }

    class WorkerConfig {
        String masterUrl;
        int maxSleepMs;
        SearchSource.SearchEngine engines[];
    }
}
