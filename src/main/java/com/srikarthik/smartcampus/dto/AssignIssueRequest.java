package com.srikarthik.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for assigning a staff member to an issue.
 */
public class AssignIssueRequest {

    @NotBlank(message = "Staff member name is required for assignment")
    @Size(max = 100, message = "Assignee name must not exceed 100 characters")
    private String assignedTo;

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
}
