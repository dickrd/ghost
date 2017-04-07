package com.hehehey.ghost.message;

/**
 * Created by Dick Zhou on 4/7/2017.
 * General api response.
 */
public class Response<T> {

    private Status status;
    private T data;

    public Response(Status status, T data) {
        this.status = status;
        this.data = data;
    }

    public enum Status {
        ok,
        unsupported,
        error
    }
}
