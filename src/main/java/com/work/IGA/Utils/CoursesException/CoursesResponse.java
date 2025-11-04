package com.work.IGA.Utils.CoursesException;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoursesResponse {
   private String message;
   private boolean success;
   private Object data;
} 
