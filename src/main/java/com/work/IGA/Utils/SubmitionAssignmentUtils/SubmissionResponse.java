package com.work.IGA.Utils.SubmitionAssignmentUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionResponse<T> {
    private boolean success;
    private String message;
    private T data;
    
    public static <T> SubmissionResponse<T> success(String message, T data) {
        return new SubmissionResponse<>(true, message, data);
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    
    public static <T> SubmissionResponse<T> error(String message) {
        return new SubmissionResponse<>(false, message, null);
    }
}