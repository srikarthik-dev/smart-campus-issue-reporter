package com.srikarthik.smartcampus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Core domain entity representing a campus issue reported by a student or staff member.
 */
@Entity
@Table(name = "issues", indexes = {
        @Index(name = "idx_issue_code", columnList = "issueCode", unique = true),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_priority", columnList = "priority"),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_reported_by", columnList = "reportedBy"),
        @Index(name = "idx_reported_at", columnList = "reportedAt")
})
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String issueCode;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Reporter name is required")
    @Size(max = 100, message = "Reporter name must not exceed 100 characters")
    private String reportedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Category is required")
    private IssueCategory category;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "Location is required")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @Column(nullable = false)
    @Min(value = 1, message = "Affected users must be at least 1")
    @Max(value = 10000, message = "Affected users cannot exceed 10000")
    private Integer affectedUsers;

    @Column(nullable = false)
    private boolean safetyImpact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private IssuePriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private IssueStatus status;

    @Column(length = 100)
    private String assignedTo;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(nullable = false)
    private LocalDateTime reportedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime resolvedAt;

    // Priority engine score stored for transparency/auditing
    @Column
    private Integer priorityScore;

    // --- Lifecycle callbacks ---

    @PrePersist
    protected void onCreate() {
        this.reportedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = IssueStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Constructors ---

    public Issue() {}

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

    public Integer getPriorityScore() { return priorityScore; }
    public void setPriorityScore(Integer priorityScore) { this.priorityScore = priorityScore; }
}
