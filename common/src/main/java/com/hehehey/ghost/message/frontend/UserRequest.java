package com.hehehey.ghost.message.frontend;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class UserRequest {

    private String taskName;
    private String seeds[];
    private String words[];

    public UserRequest(String taskName, String[] seeds, String[] words) {
        this.taskName = taskName;
        this.seeds = seeds;
        this.words = words;
    }

    public String getTaskName() {
        return taskName;
    }

    public String[] getSeeds() {
        return seeds;
    }

    public String[] getWords() {
        return words;
    }

}
