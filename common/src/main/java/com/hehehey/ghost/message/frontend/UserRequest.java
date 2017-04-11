package com.hehehey.ghost.message.frontend;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class UserRequest {

    private String name;
    private String seeds[];
    private String words[];

    public UserRequest(String name, String[] seeds, String[] words) {
        this.name = name;
        this.seeds = seeds;
        this.words = words;
    }

    public String getName() {
        return name;
    }

    public String[] getSeeds() {
        return seeds;
    }

    public String[] getWords() {
        return words;
    }

}
