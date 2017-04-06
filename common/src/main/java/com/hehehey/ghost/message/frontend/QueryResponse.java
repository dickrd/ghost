package com.hehehey.ghost.message.frontend;

import java.util.HashMap;

/**
 * Created by Dick Zhou on 3/30/2017.
 *
 */
public class QueryResponse {

    /**
     * Task id requested.
     */
    private String id;

    /**
     * Current results.
     */
    private HashMap<String, String> results[];

    public QueryResponse(String id, HashMap<String, String>[] results) {
        this.id = id;
        this.results = results;
    }
}
