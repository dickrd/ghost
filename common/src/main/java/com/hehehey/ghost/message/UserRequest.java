package com.hehehey.ghost.message;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class UserRequest {

    private SourceType type;
    private String keywords[];

    public UserRequest(SourceType type, String[] keywords) {
        this.type = type;
        this.keywords = keywords;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public SourceType getType() {
        return type;
    }

    /**
     * Created by Dick Zhou on 4/5/2017.
     */
    public enum SourceType {
        search,
        seedUrl
    }
}
