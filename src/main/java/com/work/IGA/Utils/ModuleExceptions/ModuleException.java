package com.work.IGA.Utils.ModuleExceptions;

public class ModuleException  extends RuntimeException {
     private static final long serialVersionUID = 1L; 
     private String message;
     private Integer statusCode;

     public ModuleException(String message) {
            super(message);
            this.message  = message;
            this.statusCode = 500;
     }

       public ModuleException(String message , Integer statusCode) {
            super(message);
            this.message = message;
            this.statusCode = statusCode;
       }

       @Override
       public String getMessage() {
            return message;
       }

       public Integer getStatusCode() {
            return statusCode;
       }
}
 

