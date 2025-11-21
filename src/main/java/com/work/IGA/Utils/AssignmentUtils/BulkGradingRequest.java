package com.work.IGA.Utils.AssignmentUtils;

import java.util.List;

public class BulkGradingRequest {
    private List<GradingRequest> gradingRequests;
    
    // Constructors
    public BulkGradingRequest() {}
    
    public BulkGradingRequest(List<GradingRequest> gradingRequests) {
        this.gradingRequests = gradingRequests;
    }
    
    // Getters and Setters
    public List<GradingRequest> getGradingRequests() {
        return gradingRequests;
    }
    
    public void setGradingRequests(List<GradingRequest> gradingRequests) {
        this.gradingRequests = gradingRequests;
    }
}