package com.srikarthik.smartcampus.dto;

import com.srikarthik.smartcampus.model.IssueCategory;
import com.srikarthik.smartcampus.model.IssuePriority;
import com.srikarthik.smartcampus.model.IssueStatus;

import java.time.LocalDateTime;

/**
 * Full issue response DTO — includes all fields plus priority score for transparency.
 */
public class IssueResponse {

    private Long id;
    private String issueCode;
    private String reportedBy;
    private IssueCategory category;
    private String location;
    private String description;
    private Integer affectedUsers;
    private boolean safetyImpact;
    private IssuePriority priority;
    private Integer priorityScore;
    private Integer safetyScore;
    private Integer userScore;
    private Integer categoryScore;
    private Integer ageScore;
    private IssueStatus status;
    private String assignedTo;
    private String resolutionNotes;
    private LocalDateTime reportedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private String slaIndicator;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIssueCode() { return issueCode; }
    public void setIssueCode(String issueCode) { this.issueCode = issueCode; }

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

    public IssuePriority getPriority() { return priority; }
    public void setPriority(IssuePriority priority) { this.priority = priority; }

    public Integer getPriorityScore() { return priorityScore; }
    public void setPriorityScore(Integer priorityScore) { this.priorityScore = priorityScore; }

    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getSlaIndicator() { return slaIndicator; }
    public void setSlaIndicator(String slaIndicator) { this.slaIndicator = slaIndicator; }

    public Integer getSafetyScore() { return safetyScore; }
    public void setSafetyScore(Integer safetyScore) { this.safetyScore = safetyScore; }

    public Integer getUserScore() { return userScore; }
    public void setUserScore(Integer userScore) { this.userScore = userScore; }

    public Integer getCategoryScore() { return categoryScore; }
    public void setCategoryScore(Integer categoryScore) { this.categoryScore = categoryScore; }

    public Integer getAgeScore() { return ageScore; }
    public void setAgeScore(Integer ageScore) { this.ageScore = ageScore; }
}
