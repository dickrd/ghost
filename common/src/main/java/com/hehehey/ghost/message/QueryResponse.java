package com.hehehey.ghost.message;

import content.Record;

/**
 * Created by Dick Zhou on 3/30/2017.
 *
 */
public class QueryResponse {
    private String id;
    private Record results[];

    public QueryResponse(String id, Record[] results) {
        this.id = id;
        this.results = results;
    }
}
