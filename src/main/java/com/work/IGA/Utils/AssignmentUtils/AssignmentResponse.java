package com.work.IGA.Utils.AssignmentUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentResponse {
    private boolean success;
    private String message;
    private Object data;
    private int statusCode;
    
}
