package com.work.IGA.Utils.ResourseException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceResponse<T> {


     private String message;
    private boolean success;
    private Object data;
    private Integer statusCode;


}
