package com.work.IGA.Utils;

import java.time.LocalDate;


import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDate timestamp;
    private String Token;

    public ApiResponse(boolean success, String message, T data,  String Token){
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDate.now();
        this.Token = Token;

    }


    public static <T> ApiResponse<T> success(String message, T data, String token){
        return new ApiResponse<>(true, message, data, token);
    }

    public static <T> ApiResponse<T> error(String message){
        return new ApiResponse<T>(false, message, null, null);
    }
}
