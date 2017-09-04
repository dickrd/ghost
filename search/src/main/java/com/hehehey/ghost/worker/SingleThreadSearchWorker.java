package com.hehehey.ghost.worker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hehehey.ghost.message.Response;
import com.hehehey.ghost.message.task.Assignment;
import com.hehehey.ghost.message.task.SourcingResult;
import com.hehehey.ghost.source.SearchSource;
import com.hehehey.ghost.util.HttpClient;
import com.hehehey.ghost.util.JsonConfig;
import com.hehehey.ghost.util.LongTools;

import java.util.HashMap;
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
    private final int page;
    private final HttpClient httpClient;
    private final HashMap<String, String[]> workMap;

    private final SearchSource searchSource;

    public static void main(String[] args) throws Exception {
        // Read config and start search worker.
        SearchWorkerConfig config = JsonConfig.path(configPath)
                .type(SearchWorkerConfig.class)
                .read();
        SingleThreadSearchWorker singleThreadSearchWorker = new SingleThreadSearchWorker(config);
        singleThreadSearchWorker.start();
    }

    private SingleThreadSearchWorker(SearchWorkerConfig config) throws Exception {
        this.searchSource = new SearchSource(config.engines);
        this.workMap = new HashMap<>();
        this.httpClient = new HttpClient();
        this.maxSleepMs = config.maxSleepMs;
        this.masterUrl = config.masterUrl;
        this.page = config.page;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        String id;
        String keywords[];

        //noinspection InfiniteLoopStatement
        while (true) {

            // Wait time in ms.
            long waitMs = 500;

            // While loop to fetch keywords.
            while (workMap.isEmpty()) {
                try {
                    logger.log(Level.FINE, "Waiting for: " + waitMs + " ms.");
                    Thread.sleep(waitMs);

                    // Get response from master.
                    String stringResponse = httpClient.getAsString(masterUrl + pathGetWork);
                    Response response = gson.fromJson(stringResponse, Response.class);

                    // If there is no keywords fetched, sleep time will increase and fetch again.
                    if (response.getStatus() != Response.Status.ok) {
                        logger.log(Level.WARNING, "Master info: " + response.getData());

                        if (response.getStatus() == Response.Status.wait)
                            waitMs = LongTools.increase(waitMs, maxSleepMs);
                        else
                            waitMs = LongTools.notDecrease(waitMs, maxSleepMs);
                        logger.log(Level.FINE, "Waiting for: " + waitMs + " ms.");
                        Thread.sleep(waitMs);
                        continue;
                    }

                    // If keywords fetched, reset wait time.
                    waitMs = 500;

                    // Add the keywords to work map.
                    response = gson.fromJson(stringResponse, new TypeToken<Response<Assignment>>(){}.getType());
                    Assignment assignment = (Assignment) response.getData();
                    if (assignment.getTasks() != null && assignment.getTasks().length > 0) {
                        workMap.put(assignment.getId(), assignment.getTasks());
                    }
                    else {
                        logger.log(Level.INFO, "Empty task.");
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Task query failed.", e);
                    waitMs = LongTools.increase(waitMs, maxSleepMs);
                }
            }

            // Try to pop a keyword from work map.
            try {
                id = workMap.keySet().iterator().next();
                keywords = workMap.remove(id);
            } catch (Exception e) {
                logger.log(Level.WARNING, "No keywords.", e);
                continue;
            }

            // Start search.
            logger.log(Level.INFO, "Search started.");
            for (String keyword : keywords) {
                for (int i = 0; i < page; i++) {
                    try {
                        String[] links = searchSource.searchAll(keyword, i);
                        SourcingResult result = new SourcingResult(id, links);

                        // Put result to master.
                        String s = httpClient.putString(masterUrl + pathPutResult, gson.toJson(result));
                        logger.log(Level.FINE, s);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Keyword encounters error: " + keyword, e);
                    }
                }
            }
        }
    }

    class SearchWorkerConfig {
        String masterUrl;
        int maxSleepMs;
        int page;
        SearchSource.SearchEngine engines[];
    }
}
