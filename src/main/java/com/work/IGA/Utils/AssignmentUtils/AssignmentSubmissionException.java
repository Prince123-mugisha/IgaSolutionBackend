package com.work.IGA.Utils.AssignmentUtils;

public class AssignmentSubmissionException extends Exception {
    private String errorCode;
    private String message;

    public AssignmentSubmissionException(String message) {
        super(message);
        this.errorCode = "ASSIGNMENT_SUBMISSION_ERROR";
    }

    public AssignmentSubmissionException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}