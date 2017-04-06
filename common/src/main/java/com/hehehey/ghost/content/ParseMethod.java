package com.hehehey.ghost.content;

/**
 * Created by Dick Zhou on 4/5/2017.
 *  Provide the method the parse a document and gives the resulting field name. Store the result in that name is assumed.
 */
public class ParseMethod {

    /**
     * Name of the parsed result.
     */
    private String field;

    /**
     * Method to call when parse.
     */
    private MethodType type;

    /**
     * The information needed when calling the method.
     */
    private String data;

    /**
     * Matches the chars to strip out.
     */
    private String strip;

    public ParseMethod(String field, MethodType type, String data, String strip) {
        this.field = field;
        this.type = type;
        this.data = data;
        this.strip = strip;
    }

    public String getField() {
        return field;
    }

    public MethodType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public String getStrip() {
        return strip;
    }

    public enum MethodType {
        jsoup,
        regex
    }
}
