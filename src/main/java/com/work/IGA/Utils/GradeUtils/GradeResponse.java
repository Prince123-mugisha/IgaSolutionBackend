package com.work.IGA.Utils.GradeUtils;

public class GradeResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> GradeResponse<T> success(String message, T data) {
        return new GradeResponse<>(true, message, data);
    }

    public GradeResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public static <T> GradeResponse<T> error(String message) {
        return new GradeResponse<>(false, message, null);
    }
    
}
