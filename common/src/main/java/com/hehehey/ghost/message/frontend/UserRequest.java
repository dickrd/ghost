package com.hehehey.ghost.message.frontend;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class UserRequest {

    private String taskName;
    private SourceType type;
    private String keywords[];

    public UserRequest(String taskName, SourceType type, String[] keywords) {
        this.taskName = taskName;
        this.type = type;
        this.keywords = keywords;
    }

    public String getTaskName() {
        return taskName;
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
