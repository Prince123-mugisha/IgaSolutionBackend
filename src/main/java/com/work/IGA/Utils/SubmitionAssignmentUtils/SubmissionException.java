package com.work.IGA.Utils.SubmitionAssignmentUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SubmissionException extends RuntimeException {
    private String errorCode;

    public SubmissionException(String message) {
        super(message);
    }

    public SubmissionException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public SubmissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
