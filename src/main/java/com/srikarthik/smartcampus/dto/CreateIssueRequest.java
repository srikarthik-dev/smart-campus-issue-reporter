package com.srikarthik.smartcampus.dto;

import com.srikarthik.smartcampus.model.IssueCategory;
import jakarta.validation.constraints.*;

/**
 * DTO for creating a new issue. Priority is not accepted from the client —
 * it is always calculated by the PriorityEngine.
 */
public class CreateIssueRequest {

    @NotBlank(message = "Reporter name is required")
    @Size(max = 100, message = "Reporter name must not exceed 100 characters")
    private String reportedBy;

    @NotNull(message = "Category is required")
    private IssueCategory category;

    @NotBlank(message = "Location is required")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @NotNull(message = "Affected users count is required")
    @Min(value = 1, message = "At least 1 affected user required")
    @Max(value = 10000, message = "Affected users cannot exceed 10000")
    private Integer affectedUsers;

    private boolean safetyImpact;

    // --- Getters and Setters ---

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public IssueCategory getCategory() { return category; }
    public void setCategory(IssueCategory category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getAffectedUsers() { return affectedUsers; }
    public void setAffectedUsers(Integer affectedUsers) { this.affectedUsers = affectedUsers; }

    public boolean isSafetyImpact() { return safetyImpact; }
    public void setSafetyImpact(boolean safetyImpact) { this.safetyImpact = safetyImpact; }
}
