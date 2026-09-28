package com.srikarthik.smartcampus.dto;

import com.srikarthik.smartcampus.model.IssueStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating the status of an issue.
 */
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private IssueStatus status;

    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }
}
