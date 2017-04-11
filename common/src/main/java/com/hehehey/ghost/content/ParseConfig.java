package com.hehehey.ghost.content;

/**
 * Created by Dick Zhou on 4/5/2017.
 *  Provide the method the parse a document and gives the resulting field name. Store the result in that name is assumed.
 */
public class ParseConfig {

    /**
     * Name of the parsed result.
     */
    private String field;

    /**
     * Method to call when parse.
     */
    private MethodType method;

    /**
     * Content type to parse out of.
     */
    private ContentType type;

    /**
     * The information needed when calling the method.
     */
    private String data;

    /**
     * Matches the chars to strip out.
     */
    private String strip;

    public ParseConfig(String field, MethodType method, ContentType type, String data, String strip) {
        this.field = field;
        this.method = method;
        this.type = type;
        this.data = data;
        this.strip = strip;
    }

    public String getField() {
        return field;
    }

    public MethodType getMethod() {
        return method;
    }

    public ContentType getType() {
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

    public enum ContentType {
        link,
        text,
        number,
        iamge
    }
}
