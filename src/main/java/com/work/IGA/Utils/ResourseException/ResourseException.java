package com.work.IGA.Utils.ResourseException;

import org.springframework.http.HttpStatus;

public class ResourseException  extends RuntimeException {
    private HttpStatus status;
    private String message;

    public ResourseException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
