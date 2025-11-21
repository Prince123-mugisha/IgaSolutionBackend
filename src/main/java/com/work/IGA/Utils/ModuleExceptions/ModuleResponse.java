package com.work.IGA.Utils.ModuleExceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleResponse {
    private String message;
    private boolean success;
    private Object data;
    private Integer statusCode;

    
}
