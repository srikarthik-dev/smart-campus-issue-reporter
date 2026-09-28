package com.srikarthik.smartcampus.model;

/**
 * Lifecycle statuses for a campus issue.
 * Valid transitions: OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED
 */
public enum IssueStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}
